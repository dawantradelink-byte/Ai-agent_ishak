package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_steps")
data class TaskStepEntity(
    @PrimaryKey
    val id: String,
    val taskId: String,
    val stepIndex: Int,
    val assignedAgentId: String, // mochi, scout, forge, pixel, guardian
    val stageName: String,
    val inputContext: String,
    val outputResult: String = "",
    val toolUsed: String = "Reasoning & Plan Synthesis",
    val status: String = "PENDING", // "PENDING", "IN_PROGRESS", "COMPLETED", "FAILED"
    val durationMs: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)
