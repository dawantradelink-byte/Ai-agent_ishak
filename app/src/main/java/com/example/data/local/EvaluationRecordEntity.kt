package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Audit record evaluating task outcomes against correctness, safety, and user satisfaction.
 */
@Entity(tableName = "task_evaluations")
data class EvaluationRecordEntity(
    @PrimaryKey
    val id: String,
    val taskId: String,
    val agentId: String,
    val score: Float, // 0.0f to 1.0f
    val feedback: String,
    val isSafetyVerified: Boolean,
    val isCorrectnessVerified: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
