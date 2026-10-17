package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cloud_files")
data class CloudFileEntity(
    @PrimaryKey
    val id: String,
    val fileName: String,
    val filePath: String, // e.g. "/workspace/reports/architecture.md"
    val fileSize: Long,
    val mimeType: String = "text/plain",
    val content: String,
    val createdByAgentId: String, // forge, scout, pixel, etc.
    val updatedAt: Long = System.currentTimeMillis()
)
