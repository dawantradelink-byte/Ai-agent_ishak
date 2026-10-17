package com.example.ui.viewmodel

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class AgentVoiceService(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _speakingAgentId = MutableStateFlow<String?>(null)
    val speakingAgentId: StateFlow<String?> = _speakingAgentId.asStateFlow()

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("AgentVoiceService", "Error initializing TTS", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _speakingAgentId.value = utteranceId
                }

                override fun onDone(utteranceId: String?) {
                    if (_speakingAgentId.value == utteranceId) {
                        _speakingAgentId.value = null
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    if (_speakingAgentId.value == utteranceId) {
                        _speakingAgentId.value = null
                    }
                }
            })
        } else {
            isInitialized = false
        }
    }

    fun speakAgentMessage(agentId: String, text: String) {
        if (!isInitialized || tts == null) return

        // Stop any current utterance
        stopSpeaking()

        // Strip markdown symbols for clean, natural speech synthesis
        val cleanText = text
            .replace(Regex("""\*{1,3}|_{1,3}|`{1,3}|#+|>|~{1,2}|\[.*?\]\(.*?\)"""), "")
            .replace(Regex("""^[•\-\*]\s*""", RegexOption.MULTILINE), "")
            .trim()

        if (cleanText.isBlank()) return

        // Detect Bengali Unicode characters (\u0980 to \u09FF)
        val hasBengali = cleanText.any { it in '\u0980'..'\u09FF' }
        if (hasBengali) {
            val bnLocale = Locale.forLanguageTag("bn-BD")
            val supported = tts?.isLanguageAvailable(bnLocale)
            if (supported != null && supported >= TextToSpeech.LANG_AVAILABLE) {
                tts?.language = bnLocale
            } else {
                val inBnLocale = Locale.forLanguageTag("bn-IN")
                if (tts?.isLanguageAvailable(inBnLocale) ?: -1 >= TextToSpeech.LANG_AVAILABLE) {
                    tts?.language = inBnLocale
                }
            }
        } else {
            tts?.language = Locale.US
        }

        // Apply Distinct Character Voice Configurations
        when (agentId.lowercase()) {
            "mochi" -> {
                // Mochi: Warm, gentle, friendly white bear companion
                tts?.setPitch(1.25f)
                tts?.setSpeechRate(1.0f)
            }
            "scout" -> {
                // Scout: Crisp, analytical, alert cyber-researcher
                tts?.setPitch(1.05f)
                tts?.setSpeechRate(1.15f)
            }
            "forge" -> {
                // Forge: Deep, steady, industrial robotic developer
                tts?.setPitch(0.78f)
                tts?.setSpeechRate(0.95f)
            }
            "pixel" -> {
                // Pixel: Playful, dynamic, expressive creative
                tts?.setPitch(1.22f)
                tts?.setSpeechRate(1.08f)
            }
            "guardian" -> {
                // Guardian: Steady, commanding, vigilant security sentinel
                tts?.setPitch(0.85f)
                tts?.setSpeechRate(0.92f)
            }
            else -> {
                tts?.setPitch(1.0f)
                tts?.setSpeechRate(1.0f)
            }
        }

        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, agentId)
    }

    fun stopSpeaking() {
        tts?.stop()
        _speakingAgentId.value = null
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
