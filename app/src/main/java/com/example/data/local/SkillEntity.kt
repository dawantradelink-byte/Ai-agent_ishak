package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agent_skills")
data class SkillEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val agentId: String, // Owner agent (mochi, scout, forge, pixel, guardian)
    val description: String,
    val version: Int = 1,
    val status: String = "PROMOTED", // "DRAFT", "SANDBOX_TESTED", "PROMOTED", "DISABLED"
    val instructions: String,
    val requiredPermissions: String = "web_search", // Comma-separated: web_search, file_read, file_write, terminal_run
    val inputsSchema: String = "{\"query\": \"string\"}",
    val expectedOutputs: String = "Structured markdown report with verified findings",
    val safetyScore: Float = 0.95f,
    val baselineScore: Float = 0.85f,
    val candidateScore: Float = 0.95f,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
