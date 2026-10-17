package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agent_tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val agentId: String,
    val title: String,
    val prompt: String,
    val response: String = "",
    val status: String = "COMPLETED", // "ACTIVE", "COMPLETED", "FAILED"
    val timestamp: Long = System.currentTimeMillis(),
    val isRealAi: Boolean = false
)
