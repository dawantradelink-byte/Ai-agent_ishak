package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Versioned agent instructions for controlled self-improvement.
 * Prevents uncontrolled prompt overwrites; changes must be tested against a baseline
 * and promoted or rolled back with human approval.
 */
@Entity(tableName = "agent_instructions")
data class AgentInstructionEntity(
    @PrimaryKey
    val id: String,
    val agentId: String,
    val version: Int,
    val promptContent: String,
    val rationale: String,
    val baselineScore: Float,
    val candidateScore: Float,
    val isPromoted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
