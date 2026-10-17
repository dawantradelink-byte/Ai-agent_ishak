package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "durable_task_executions")
data class DurableTaskExecutionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val objective: String,
    val initiatorAgentId: String = "mochi",
    val status: String = "QUEUED", // "QUEUED", "RUNNING", "WAITING_APPROVAL", "COMPLETED", "FAILED", "CANCELLED"
    val currentStageIndex: Int = 0,
    val totalStages: Int = 5,
    val checkpointState: String = "", // JSON serialised state and cumulative outputs
    val traceId: String,
    val isRealAi: Boolean = false,
    val guardianApproved: Boolean = false,
    val finalReport: String = "",
    val errorMessage: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
