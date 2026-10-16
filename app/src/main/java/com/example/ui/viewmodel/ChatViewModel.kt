package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.repository.ChatRepository
import com.example.data.repository.SendMessageResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val activeSessionId: String? = null,
    val activeSessionTitle: String = "New Conversation",
    val sessions: List<ChatSessionEntity> = emptyList(),
    val messages: List<ChatMessageEntity> = emptyList(),
    val isGenerating: Boolean = false,
    val inputText: String = "",
    val errorMessage: String? = null,
    val isQuotaOrKeyError: Boolean = false
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChatRepository
    private var activeJob: Job? = null
    private var messagesObservationJob: Job? = null

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ChatRepository(database.chatDao())

        // Collect sessions
        viewModelScope.launch {
            repository.allSessions.collectLatest { sessionsList ->
                _uiState.update { current ->
                    val activeId = current.activeSessionId
                    // If no active session, pick the first or create one
                    if (activeId == null && sessionsList.isNotEmpty()) {
                        observeMessagesForSession(sessionsList.first().id)
                        current.copy(
                            sessions = sessionsList,
                            activeSessionId = sessionsList.first().id,
                            activeSessionTitle = sessionsList.first().title
                        )
                    } else {
                        val activeTitle = sessionsList.find { it.id == activeId }?.title ?: current.activeSessionTitle
                        current.copy(
                            sessions = sessionsList,
                            activeSessionTitle = activeTitle
                        )
                    }
                }
            }
        }
    }

    private fun observeMessagesForSession(sessionId: String) {
        messagesObservationJob?.cancel()
        messagesObservationJob = viewModelScope.launch {
            repository.getMessagesForSession(sessionId).collectLatest { msgs ->
                _uiState.update { it.copy(messages = msgs) }
            }
        }
    }

    fun onInputTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null, isQuotaOrKeyError = false) }
    }

    fun selectSession(sessionId: String) {
        val session = _uiState.value.sessions.find { it.id == sessionId }
        _uiState.update {
            it.copy(
                activeSessionId = sessionId,
                activeSessionTitle = session?.title ?: "Conversation",
                errorMessage = null
            )
        }
        observeMessagesForSession(sessionId)
    }

    fun startNewChat() {
        viewModelScope.launch {
            val newId = repository.createNewSession("New Conversation")
            _uiState.update {
                it.copy(
                    activeSessionId = newId,
                    activeSessionTitle = "New Conversation",
                    inputText = "",
                    errorMessage = null
                )
            }
            observeMessagesForSession(newId)
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_uiState.value.activeSessionId == sessionId) {
                val remaining = _uiState.value.sessions.filter { it.id != sessionId }
                if (remaining.isNotEmpty()) {
                    selectSession(remaining.first().id)
                } else {
                    startNewChat()
                }
            }
        }
    }

    fun clearAllConversations() {
        viewModelScope.launch {
            repository.clearAllSessions()
            startNewChat()
        }
    }

    fun sendMessage(explicitPrompt: String? = null) {
        val promptToSend = explicitPrompt ?: _uiState.value.inputText.trim()
        if (promptToSend.isBlank() || _uiState.value.isGenerating) return

        // Clear input field if from bottom bar
        if (explicitPrompt == null) {
            _uiState.update { it.copy(inputText = "", errorMessage = null) }
        }

        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            var currentSessionId = _uiState.value.activeSessionId
            if (currentSessionId == null) {
                currentSessionId = repository.createNewSession(promptToSend.take(30))
                _uiState.update { it.copy(activeSessionId = currentSessionId) }
                observeMessagesForSession(currentSessionId)
            }

            // Save user message
            repository.saveUserMessage(currentSessionId, promptToSend)

            _uiState.update { it.copy(isGenerating = true, errorMessage = null) }

            val history = _uiState.value.messages
            val result = repository.generateGeminiResponse(currentSessionId, history, promptToSend)

            when (result) {
                is SendMessageResult.Success -> {
                    _uiState.update { it.copy(isGenerating = false) }
                }
                is SendMessageResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            errorMessage = result.errorMessage,
                            isQuotaOrKeyError = result.isQuotaOrKeyError
                        )
                    }
                }
            }
        }
    }

    fun stopGeneration() {
        activeJob?.cancel()
        activeJob = null
        val sessionId = _uiState.value.activeSessionId
        if (sessionId != null) {
            viewModelScope.launch {
                repository.saveAssistantMessage(sessionId, "*(উত্পাদন স্থগিত করা হয়েছে / Generation stopped by user)*")
            }
        }
        _uiState.update { it.copy(isGenerating = false) }
    }

    fun retryLast() {
        val messages = _uiState.value.messages
        val lastUserMsg = messages.lastOrNull { it.role == "user" } ?: return
        sendMessage(lastUserMsg.content)
    }
}
