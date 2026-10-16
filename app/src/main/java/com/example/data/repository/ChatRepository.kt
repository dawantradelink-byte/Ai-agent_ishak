package com.example.data.repository

import com.example.BuildConfig
import com.example.data.local.ChatDao
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.remote.ApiClient
import com.example.data.remote.ContentDto
import com.example.data.remote.GeminiRequest
import com.example.data.remote.GenerationConfigDto
import com.example.data.remote.PartDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

sealed class SendMessageResult {
    data class Success(val responseText: String) : SendMessageResult()
    data class Error(val errorMessage: String, val isQuotaOrKeyError: Boolean = false) : SendMessageResult()
}

class ChatRepository(private val chatDao: ChatDao) {

    val allSessions: Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesForSession(sessionId)
    }

    suspend fun createNewSession(initialTitle: String = "New Conversation"): String = withContext(Dispatchers.IO) {
        val sessionId = UUID.randomUUID().toString()
        val session = ChatSessionEntity(
            id = sessionId,
            title = initialTitle,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        chatDao.insertSession(session)
        sessionId
    }

    suspend fun deleteSession(sessionId: String) = withContext(Dispatchers.IO) {
        chatDao.deleteSessionById(sessionId)
    }

    suspend fun clearAllSessions() = withContext(Dispatchers.IO) {
        chatDao.deleteAllSessions()
    }

    suspend fun updateSessionTitle(sessionId: String, title: String) = withContext(Dispatchers.IO) {
        val existing = chatDao.getSessionById(sessionId) ?: return@withContext
        chatDao.updateSession(existing.copy(title = title, updatedAt = System.currentTimeMillis()))
    }

    suspend fun saveUserMessage(sessionId: String, text: String): ChatMessageEntity = withContext(Dispatchers.IO) {
        val message = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            role = "user",
            content = text,
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessage(message)

        // Update session title if default
        val session = chatDao.getSessionById(sessionId)
        if (session != null && (session.title == "New Conversation" || session.title.isBlank())) {
            val autoTitle = if (text.length > 36) text.take(36).trim() + "…" else text.trim()
            chatDao.updateSession(session.copy(title = autoTitle, updatedAt = System.currentTimeMillis()))
        } else if (session != null) {
            chatDao.updateSession(session.copy(updatedAt = System.currentTimeMillis()))
        }

        message
    }

    suspend fun saveAssistantMessage(sessionId: String, text: String): ChatMessageEntity = withContext(Dispatchers.IO) {
        val message = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            role = "assistant",
            content = text,
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessage(message)
        val session = chatDao.getSessionById(sessionId)
        if (session != null) {
            chatDao.updateSession(session.copy(updatedAt = System.currentTimeMillis()))
        }
        message
    }

    suspend fun generateGeminiResponse(
        sessionId: String,
        history: List<ChatMessageEntity>,
        newPrompt: String
    ): SendMessageResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        // Check if API key is unset or placeholder
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            val helpfulDemo = generateFallbackDemoResponse(newPrompt)
            saveAssistantMessage(sessionId, helpfulDemo)
            return@withContext SendMessageResult.Success(helpfulDemo)
        }

        // Prepare bilingual system instruction
        val systemInstruction = ContentDto(
            parts = listOf(
                PartDto(
                    text = """
                        You are 'My AI', a polite, smart, and warm AI chatbot inspired by ChatGPT.
                        You support seamless bilingual conversations in English and Bengali (বাংলা).
                        When user writes in Bengali or Banglish, reply in fluent, respectful, grammatically correct Bengali.
                        When user writes in English, reply in natural, clear English.
                        Support code generation with markdown code blocks indicating language.
                        Use clean formatting with bullet points and bold text where helpful.
                    """.trimIndent()
                )
            )
        )

        // Build conversation contents (limit to recent turns to optimize tokens)
        val contents = mutableListOf<ContentDto>()
        val recentHistory = history.takeLast(10)
        for (msg in recentHistory) {
            val role = if (msg.role == "user") "user" else "model"
            contents.add(
                ContentDto(
                    role = role,
                    parts = listOf(PartDto(text = msg.content))
                )
            )
        }
        // Add current user prompt
        contents.add(
            ContentDto(
                role = "user",
                parts = listOf(PartDto(text = newPrompt))
            )
        )

        val request = GeminiRequest(
            contents = contents,
            systemInstruction = systemInstruction,
            generationConfig = GenerationConfigDto(temperature = 0.7f, topP = 0.95f)
        )

        try {
            val response = ApiClient.geminiService.generateContent(apiKey, request)
            if (response.isSuccessful) {
                val body = response.body()
                val text = body?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) {
                    saveAssistantMessage(sessionId, text)
                    SendMessageResult.Success(text)
                } else {
                    val fallback = "দুঃখিত, কোনো উত্তর পাওয়া যায়নি (Empty response)."
                    saveAssistantMessage(sessionId, fallback)
                    SendMessageResult.Success(fallback)
                }
            } else {
                val code = response.code()
                val errBody = response.errorBody()?.string() ?: ""
                val errMessage = when (code) {
                    429 -> "কোটা বা রিকোয়েস্ট লিমিট অতিক্রম করেছে (Quota limit reached: HTTP 429). অনুগ্রহ করে কিছুক্ষণ পর চেষ্টা করুন।"
                    403, 401 -> "API কী বৈধ নয় বা অ্যাক্সেস নেই (Invalid or unauthorized GEMINI_API_KEY: HTTP $code). অনুগ্রহ করে AI Studio Secrets প্যানেলে কী যাচাই করুন।"
                    else -> "Gemini সার্ভার ত্রুটি (HTTP $code): $errBody"
                }
                SendMessageResult.Error(errMessage, isQuotaOrKeyError = (code == 429 || code == 403 || code == 401))
            }
        } catch (e: Exception) {
            val err = "সংযোগ ত্রুটি (Network Error): ${e.localizedMessage ?: "সার্ভারে পৌঁছানো সম্ভব হয়নি"}"
            SendMessageResult.Error(err)
        }
    }

    private fun generateFallbackDemoResponse(prompt: String): String {
        val lower = prompt.lowercase()
        val isBengali = prompt.any { it in '\u0980'..'\u09FF' } || lower.contains("bangla") || lower.contains("kemon")

        return if (isBengali) {
            """
                **নমস্কার! আমি My AI।**

                আপনার বার্তাটি গ্রহণ করেছি: *"$prompt"*

                > **প্রস্তুতি সংক্রান্ত নোট (Setup Notice):**
                > বর্তমানে ডেমো মোডে চলছে কারণ `GEMINI_API_KEY` এখনও AI Studio Secrets প্যানেলে সেট করা হয়নি। আসল Gemini AI সক্রিয় করতে:
                > 1. AI Studio এর **Secrets** প্যানেলে যান।
                > 2. `GEMINI_API_KEY` নামে আপনার Gemini কী যুক্ত করুন।

                বাংলা এবং ইংরেজি উভয় ভাষাতেই My AI সাবলীলভাবে কাজ করতে প্রস্তুত! আপনি যেকোনো প্রশ্ন করতে পারেন, কোড লিখতে বা অনুবাদ করতে বলতে পারেন।

                ```python
                # পাইথন উদাহরণ
                def greet(name: str):
                    return f"হ্যালো {name}, My AI তে স্বাগতম!"

                print(greet("বন্ধু"))
                ```
            """.trimIndent()
        } else {
            """
                **Hello! I am My AI.**

                I received your prompt: *"$prompt"*

                > **Setup Notice:**
                > Running in preview mode because `GEMINI_API_KEY` is not yet configured in the AI Studio Secrets panel.
                > To enable full Gemini 3.5 Flash responses:
                > 1. Open the **Secrets** panel in AI Studio.
                > 2. Add your free Gemini API key as `GEMINI_API_KEY`.

                My AI is fully equipped to write code, answer questions, and hold bilingual conversations in English and Bengali (বাংলা)!

                ```kotlin
                // Kotlin Example
                fun welcome(user: String) {
                    println("Welcome to My AI, " + user + "!")
                }
                ```
            """.trimIndent()
        }
    }
}
