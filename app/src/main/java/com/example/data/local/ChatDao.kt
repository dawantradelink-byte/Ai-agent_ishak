package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions ORDER BY updatedAt DESC")
    fun getAllSessions(): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): ChatSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity)

    @Update
    suspend fun updateSession(session: ChatSessionEntity)

    @Query("UPDATE chat_sessions SET activeAgentId = :agentId, updatedAt = :updatedAt WHERE id = :sessionId")
    suspend fun updateSessionAgent(sessionId: String, agentId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: String)

    @Query("DELETE FROM chat_sessions")
    suspend fun deleteAllSessions()

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: String)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)

    // Task Management
    @Query("SELECT * FROM agent_tasks ORDER BY timestamp DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM agent_tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    @Query("DELETE FROM agent_tasks")
    suspend fun deleteAllTasks()

    // Persistent Multi-Layer Memory
    @Query("SELECT * FROM agent_memories ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<AgentMemoryEntity>>

    @Query("SELECT * FROM agent_memories WHERE agentId = :agentId OR agentId = 'shared' ORDER BY timestamp DESC")
    fun getMemoriesForAgent(agentId: String): Flow<List<AgentMemoryEntity>>

    @Query("SELECT * FROM agent_memories WHERE agentId = :agentId ORDER BY timestamp DESC")
    fun getMemoriesStrictlyForAgent(agentId: String): Flow<List<AgentMemoryEntity>>

    @Query("SELECT * FROM agent_memories WHERE category = :category ORDER BY timestamp DESC")
    fun getMemoriesByCategory(category: String): Flow<List<AgentMemoryEntity>>

    @Query("SELECT COUNT(*) FROM agent_memories")
    suspend fun getMemoryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: AgentMemoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemories(memories: List<AgentMemoryEntity>)

    @Query("DELETE FROM agent_memories WHERE id = :memoryId")
    suspend fun deleteMemoryById(memoryId: String)

    @Query("DELETE FROM agent_memories WHERE agentId = :agentId")
    suspend fun deleteMemoriesForAgent(agentId: String)

    @Query("DELETE FROM agent_memories")
    suspend fun deleteAllMemories()

    // Controlled Self-Evolution & Versioned Instructions
    @Query("SELECT * FROM agent_instructions WHERE agentId = :agentId ORDER BY version DESC")
    fun getInstructionsForAgent(agentId: String): Flow<List<AgentInstructionEntity>>

    @Query("SELECT * FROM agent_instructions WHERE agentId = :agentId AND isPromoted = 1 ORDER BY version DESC LIMIT 1")
    suspend fun getActiveInstructionForAgent(agentId: String): AgentInstructionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstruction(instruction: AgentInstructionEntity)

    @Query("UPDATE agent_instructions SET isPromoted = 0 WHERE agentId = :agentId")
    suspend fun demoteAllInstructions(agentId: String)

    @Query("UPDATE agent_instructions SET isPromoted = 1 WHERE id = :id")
    suspend fun promoteInstruction(id: String)

    // Task Evaluations & Audits
    @Query("SELECT * FROM task_evaluations ORDER BY timestamp DESC")
    fun getAllEvaluations(): Flow<List<EvaluationRecordEntity>>

    @Query("SELECT * FROM task_evaluations WHERE agentId = :agentId ORDER BY timestamp DESC")
    fun getEvaluationsForAgent(agentId: String): Flow<List<EvaluationRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvaluation(evaluation: EvaluationRecordEntity)

    @Query("DELETE FROM task_evaluations")
    suspend fun deleteAllEvaluations()

    // Reusable Skills System
    @Query("SELECT * FROM agent_skills ORDER BY updatedAt DESC")
    fun getAllSkills(): Flow<List<SkillEntity>>

    @Query("SELECT * FROM agent_skills WHERE agentId = :agentId ORDER BY updatedAt DESC")
    fun getSkillsForAgent(agentId: String): Flow<List<SkillEntity>>

    @Query("SELECT * FROM agent_skills WHERE id = :skillId LIMIT 1")
    suspend fun getSkillById(skillId: String): SkillEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: SkillEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkills(skills: List<SkillEntity>)

    @Update
    suspend fun updateSkill(skill: SkillEntity)

    @Query("DELETE FROM agent_skills WHERE id = :skillId")
    suspend fun deleteSkillById(skillId: String)

    @Query("DELETE FROM agent_skills")
    suspend fun deleteAllSkills()

    @Query("SELECT COUNT(*) FROM agent_skills")
    suspend fun getSkillCount(): Int

    // Scheduled Routines
    @Query("SELECT * FROM scheduled_routines ORDER BY createdAt DESC")
    fun getAllRoutines(): Flow<List<ScheduledRoutineEntity>>

    @Query("SELECT * FROM scheduled_routines WHERE id = :routineId LIMIT 1")
    suspend fun getRoutineById(routineId: String): ScheduledRoutineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: ScheduledRoutineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutines(routines: List<ScheduledRoutineEntity>)

    @Update
    suspend fun updateRoutine(routine: ScheduledRoutineEntity)

    @Query("DELETE FROM scheduled_routines WHERE id = :routineId")
    suspend fun deleteRoutineById(routineId: String)

    @Query("DELETE FROM scheduled_routines")
    suspend fun deleteAllRoutines()

    @Query("SELECT COUNT(*) FROM scheduled_routines")
    suspend fun getRoutineCount(): Int

    // Durable Task State Machine & Checkpoints
    @Query("SELECT * FROM durable_task_executions ORDER BY createdAt DESC")
    fun getAllDurableExecutions(): Flow<List<DurableTaskExecutionEntity>>

    @Query("SELECT * FROM durable_task_executions WHERE id = :executionId LIMIT 1")
    suspend fun getDurableExecutionById(executionId: String): DurableTaskExecutionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDurableExecution(execution: DurableTaskExecutionEntity)

    @Update
    suspend fun updateDurableExecution(execution: DurableTaskExecutionEntity)

    @Query("DELETE FROM durable_task_executions WHERE id = :executionId")
    suspend fun deleteDurableExecutionById(executionId: String)

    @Query("DELETE FROM durable_task_executions")
    suspend fun deleteAllDurableExecutions()

    // Task Steps
    @Query("SELECT * FROM task_steps WHERE taskId = :taskId ORDER BY stepIndex ASC")
    fun getStepsForTask(taskId: String): Flow<List<TaskStepEntity>>

    @Query("SELECT * FROM task_steps WHERE taskId = :taskId ORDER BY stepIndex ASC")
    suspend fun getStepsForTaskDirect(taskId: String): List<TaskStepEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskStep(step: TaskStepEntity)

    @Update
    suspend fun updateTaskStep(step: TaskStepEntity)

    @Query("DELETE FROM task_steps WHERE taskId = :taskId")
    suspend fun deleteStepsForTask(taskId: String)

    // Cloud Virtual Filesystem
    @Query("SELECT * FROM cloud_files ORDER BY updatedAt DESC")
    fun getAllCloudFiles(): Flow<List<CloudFileEntity>>

    @Query("SELECT * FROM cloud_files WHERE id = :fileId LIMIT 1")
    suspend fun getCloudFileById(fileId: String): CloudFileEntity?

    @Query("SELECT * FROM cloud_files WHERE filePath = :filePath LIMIT 1")
    suspend fun getCloudFileByPath(filePath: String): CloudFileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCloudFile(file: CloudFileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCloudFiles(files: List<CloudFileEntity>)

    @Update
    suspend fun updateCloudFile(file: CloudFileEntity)

    @Query("DELETE FROM cloud_files WHERE id = :fileId")
    suspend fun deleteCloudFileById(fileId: String)

    @Query("DELETE FROM cloud_files")
    suspend fun deleteAllCloudFiles()

    @Query("SELECT COUNT(*) FROM cloud_files")
    suspend fun getCloudFileCount(): Int

    // Human-in-the-Loop Approvals
    @Query("SELECT * FROM approval_requests ORDER BY timestamp DESC")
    fun getAllApprovalRequests(): Flow<List<ApprovalRequestEntity>>

    @Query("SELECT * FROM approval_requests WHERE status = 'PENDING' ORDER BY timestamp DESC")
    fun getPendingApprovalRequests(): Flow<List<ApprovalRequestEntity>>

    @Query("SELECT * FROM approval_requests WHERE id = :requestId LIMIT 1")
    suspend fun getApprovalRequestById(requestId: String): ApprovalRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApprovalRequest(request: ApprovalRequestEntity)

    @Update
    suspend fun updateApprovalRequest(request: ApprovalRequestEntity)

    @Query("DELETE FROM approval_requests WHERE id = :requestId")
    suspend fun deleteApprovalRequestById(requestId: String)

    @Query("DELETE FROM approval_requests")
    suspend fun deleteAllApprovalRequests()

    // Persistent Task Logs
    @Query("SELECT * FROM task_logs WHERE taskId = :taskId ORDER BY timestamp ASC")
    fun getLogsForTask(taskId: String): Flow<List<TaskLogEntity>>

    @Query("SELECT * FROM task_logs WHERE taskId = :taskId ORDER BY timestamp ASC")
    suspend fun getLogsForTaskDirect(taskId: String): List<TaskLogEntity>

    @Query("SELECT * FROM task_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTaskLogs(limit: Int = 100): Flow<List<TaskLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskLog(log: TaskLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskLogs(logs: List<TaskLogEntity>)

    @Query("DELETE FROM task_logs WHERE taskId = :taskId")
    suspend fun deleteLogsForTask(taskId: String)

    @Query("DELETE FROM task_logs")
    suspend fun deleteAllTaskLogs()
}
