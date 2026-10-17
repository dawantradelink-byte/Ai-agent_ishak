package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Persistent log entry for task execution, agent handoffs, and audit traces in Room database.
 */
@Entity(tableName = "task_logs")
data class TaskLogEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val agentId: String,
    val stageIndex: Int = 0,
    val level: String = "INFO", // "INFO", "WARN", "ERROR", "SUCCESS", "HANDOFF"
    val message: String,
    val payload: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
