package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @field:Json(name = "contents") val contents: List<ContentDto>,
    @field:Json(name = "systemInstruction") val systemInstruction: ContentDto? = null,
    @field:Json(name = "generationConfig") val generationConfig: GenerationConfigDto? = null
)

@JsonClass(generateAdapter = true)
data class ContentDto(
    @field:Json(name = "role") val role: String? = null,
    @field:Json(name = "parts") val parts: List<PartDto>
)

@JsonClass(generateAdapter = true)
data class PartDto(
    @field:Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfigDto(
    @field:Json(name = "temperature") val temperature: Float? = 0.7f,
    @field:Json(name = "topP") val topP: Float? = 0.95f,
    @field:Json(name = "maxOutputTokens") val maxOutputTokens: Int? = 4096
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @field:Json(name = "candidates") val candidates: List<CandidateDto>? = null,
    @field:Json(name = "promptFeedback") val promptFeedback: PromptFeedbackDto? = null,
    @field:Json(name = "error") val error: GeminiErrorDto? = null
)

@JsonClass(generateAdapter = true)
data class CandidateDto(
    @field:Json(name = "content") val content: ContentDto? = null,
    @field:Json(name = "finishReason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class PromptFeedbackDto(
    @field:Json(name = "blockReason") val blockReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiErrorDto(
    @field:Json(name = "code") val code: Int? = null,
    @field:Json(name = "message") val message: String? = null,
    @field:Json(name = "status") val status: String? = null
)
