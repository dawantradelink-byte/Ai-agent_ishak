package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_routines")
data class ScheduledRoutineEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val ownerAgentId: String, // mochi, scout, forge, pixel, guardian
    val triggerType: String, // "INTERVAL", "DAILY", "EVENT"
    val triggerConfig: String, // e.g. "Every 4 Hours", "Daily at 09:00", "On Commit"
    val assignedSkillOrPrompt: String,
    val status: String = "ACTIVE", // "ACTIVE", "PAUSED", "COMPLETED", "FAILED"
    val lastRunTimestamp: Long = 0L,
    val nextRunTimestamp: Long = 0L,
    val retryPolicy: String = "Exponential Backoff (Max 3 retries)",
    val lastRunResult: String = "",
    val totalRunsCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
