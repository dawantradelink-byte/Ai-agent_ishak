package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "approval_requests")
data class ApprovalRequestEntity(
    @PrimaryKey
    val id: String,
    val taskId: String,
    val agentId: String,
    val actionType: String, // "FILE_DELETE", "TERMINAL_EXECUTE", "SKILL_PROMOTE", "EXTERNAL_PUBLISH"
    val title: String,
    val description: String,
    val proposedActionPayload: String,
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val timestamp: Long = System.currentTimeMillis()
)
