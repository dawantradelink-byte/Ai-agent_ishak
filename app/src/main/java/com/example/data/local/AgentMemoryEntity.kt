package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agent_memories")
data class AgentMemoryEntity(
    @PrimaryKey val id: String,
    val agentId: String, // "mochi", "scout", "forge", "pixel", "guardian", or "shared"
    val category: String, // "PREFERENCE", "PROJECT", "INSTRUCTION", "SHARED_FINDING", "FACT"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isVerified: Boolean = true
)
