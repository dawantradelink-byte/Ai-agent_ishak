package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.cloud.CloudBackendStatus
import com.example.data.cloud.ToolExecutionResult
import com.example.data.local.AgentInstructionEntity
import com.example.data.local.AgentMemoryEntity
import com.example.data.local.AppDatabase
import com.example.data.local.ApprovalRequestEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.local.CloudFileEntity
import com.example.data.local.DurableTaskExecutionEntity
import com.example.data.local.EvaluationRecordEntity
import com.example.data.local.ScheduledRoutineEntity
import com.example.data.local.SkillEntity
import com.example.data.local.TaskEntity
import com.example.data.local.TaskStepEntity
import com.example.data.model.AgentCapability
import com.example.data.model.AgentTeam
import com.example.data.repository.ChatRepository
import com.example.data.repository.SendMessageResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val activeTab: Int = 0, // 0: Dashboard, 1: Chat, 2: Tasks, 3: Settings
    val activeAgentId: String = "mochi",
    val activeSessionId: String? = null,
    val activeSessionTitle: String = "Mochi AI Chat",
    val sessions: List<ChatSessionEntity> = emptyList(),
    val messages: List<ChatMessageEntity> = emptyList(),
    val tasks: List<TaskEntity> = emptyList(),
    val memories: List<AgentMemoryEntity> = emptyList(),
    val isGenerating: Boolean = false,
    val inputText: String = "",
    val errorMessage: String? = null,
    val isQuotaOrKeyError: Boolean = false,
    val reduceMotion: Boolean = false,
    val isAssigningTask: Boolean = false,
    val isApiKeyConfigured: Boolean = false,

    // Autonomous Cloud AI Team state
    val activeOperationsSubTab: Int = 0, // 0: Tasks/Timeline, 1: Cloud Files, 2: Reusable Skills, 3: Routines, 4: Approvals
    val skills: List<SkillEntity> = emptyList(),
    val routines: List<ScheduledRoutineEntity> = emptyList(),
    val durableExecutions: List<DurableTaskExecutionEntity> = emptyList(),
    val cloudFiles: List<CloudFileEntity> = emptyList(),
    val pendingApprovals: List<ApprovalRequestEntity> = emptyList(),
    val backendStatus: CloudBackendStatus = CloudBackendStatus(),
    val selectedCloudFile: CloudFileEntity? = null,
    val activeDurableTaskId: String? = null,
    val selectedTaskSteps: List<TaskStepEntity> = emptyList(),
    val terminalOutput: String = "",
    val isExecutingCommand: Boolean = false
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChatRepository
    private val voiceService = AgentVoiceService(application)
    val speakingAgentId: StateFlow<String?> = voiceService.speakingAgentId

    private var activeJob: Job? = null
    private var messagesObservationJob: Job? = null
    private var taskStepsObservationJob: Job? = null

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ChatRepository(database.chatDao())

        val key = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        val hasKey = key.isNotBlank() && key != "MY_GEMINI_API_KEY"
        _uiState.update { it.copy(isApiKeyConfigured = hasKey) }

        // Observe Sessions
        viewModelScope.launch {
            repository.allSessions.collectLatest { sessionsList ->
                _uiState.update { current ->
                    val activeId = current.activeSessionId
                    if (activeId == null && sessionsList.isNotEmpty()) {
                        val first = sessionsList.first()
                        observeMessagesForSession(first.id)
                        current.copy(
                            sessions = sessionsList,
                            activeSessionId = first.id,
                            activeSessionTitle = first.title,
                            activeAgentId = first.activeAgentId
                        )
                    } else {
                        val session = sessionsList.find { it.id == activeId }
                        current.copy(
                            sessions = sessionsList,
                            activeSessionTitle = session?.title ?: current.activeSessionTitle
                        )
                    }
                }
            }
        }

        // Observe Legacy Tasks
        viewModelScope.launch {
            repository.allTasks.collectLatest { tasksList ->
                _uiState.update { it.copy(tasks = tasksList) }
            }
        }

        // Seed default persistent memories, baseline instructions, skills, routines, and files
        viewModelScope.launch {
            repository.ensureDefaultMemoriesSeeded()
            repository.ensureDefaultInstructionsSeeded()
            repository.ensureDefaultAutonomousDataSeeded()
            val status = repository.backendClient.getBackendStatus()
            _uiState.update { it.copy(backendStatus = status) }
        }

        // Observe Memories
        viewModelScope.launch {
            repository.allMemories.collectLatest { memList ->
                _uiState.update { it.copy(memories = memList) }
            }
        }

        // Observe Skills
        viewModelScope.launch {
            repository.allSkills.collectLatest { skillsList ->
                _uiState.update { it.copy(skills = skillsList) }
            }
        }

        // Observe Scheduled Routines
        viewModelScope.launch {
            repository.allRoutines.collectLatest { routinesList ->
                _uiState.update { it.copy(routines = routinesList) }
            }
        }

        // Observe Durable Task Executions
        viewModelScope.launch {
            repository.allDurableExecutions.collectLatest { executionsList ->
                _uiState.update { current ->
                    val activeId = current.activeDurableTaskId ?: executionsList.firstOrNull()?.id
                    if (activeId != null && activeId != current.activeDurableTaskId) {
                        observeStepsForTask(activeId)
                    }
                    current.copy(
                        durableExecutions = executionsList,
                        activeDurableTaskId = activeId
                    )
                }
            }
        }

        // Observe Cloud Files
        viewModelScope.launch {
            repository.allCloudFiles.collectLatest { filesList ->
                _uiState.update { current ->
                    current.copy(
                        cloudFiles = filesList,
                        selectedCloudFile = current.selectedCloudFile ?: filesList.firstOrNull()
                    )
                }
            }
        }

        // Observe Pending Approvals
        viewModelScope.launch {
            repository.pendingApprovals.collectLatest { approvalsList ->
                _uiState.update { it.copy(pendingApprovals = approvalsList) }
            }
        }
    }

    private fun observeMessagesForSession(sessionId: String) {
        messagesObservationJob?.cancel()
        messagesObservationJob = viewModelScope.launch {
            repository.getMessagesForSession(sessionId).collectLatest { msgs ->
                _uiState.update { it.copy(messages = msgs) }
            }
        }
    }

    private fun observeStepsForTask(taskId: String) {
        taskStepsObservationJob?.cancel()
        taskStepsObservationJob = viewModelScope.launch {
            repository.getStepsForTask(taskId).collectLatest { steps ->
                _uiState.update { it.copy(selectedTaskSteps = steps) }
            }
        }
    }

    fun switchTab(tab: Int) {
        _uiState.update { it.copy(activeTab = tab, errorMessage = null) }
    }

    fun switchOperationsSubTab(subTab: Int) {
        _uiState.update { it.copy(activeOperationsSubTab = subTab) }
    }

    fun selectAgent(agentId: String) {
        val currentSessionId = _uiState.value.activeSessionId
        _uiState.update { it.copy(activeAgentId = agentId) }
        if (currentSessionId != null) {
            viewModelScope.launch {
                repository.updateSessionAgent(currentSessionId, agentId)
            }
        }
    }

    fun toggleReduceMotion() {
        _uiState.update { it.copy(reduceMotion = !it.reduceMotion) }
    }

    fun onInputTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null, isQuotaOrKeyError = false) }
    }

    fun selectSession(sessionId: String) {
        val session = _uiState.value.sessions.find { it.id == sessionId } ?: return
        _uiState.update {
            it.copy(
                activeSessionId = sessionId,
                activeSessionTitle = session.title,
                activeAgentId = session.activeAgentId
            )
        }
        observeMessagesForSession(sessionId)
    }

    fun createNewSession(agentId: String = _uiState.value.activeAgentId) {
        viewModelScope.launch {
            val agent = AgentTeam.getById(agentId)
            val newSessionId = repository.createNewSession(
                initialTitle = "Chat with ${agent.name}",
                agentId = agentId
            )
            _uiState.update {
                it.copy(
                    activeSessionId = newSessionId,
                    activeSessionTitle = "Chat with ${agent.name}",
                    activeAgentId = agentId
                )
            }
            observeMessagesForSession(newSessionId)
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            val remaining = _uiState.value.sessions.filter { it.id != sessionId }
            if (remaining.isNotEmpty()) {
                selectSession(remaining.first().id)
            } else {
                createNewSession()
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _uiState.update {
                it.copy(
                    activeSessionId = null,
                    messages = emptyList(),
                    tasks = emptyList()
                )
            }
            createNewSession()
        }
    }

    fun clearMemories() {
        viewModelScope.launch {
            repository.clearMemories()
        }
    }

    fun deleteMemory(memoryId: String) {
        viewModelScope.launch {
            repository.deleteMemory(memoryId)
        }
    }

    fun addManualMemory(agentId: String, category: String, content: String) {
        viewModelScope.launch {
            repository.insertMemory(agentId, category, content, isVerified = true)
        }
    }

    fun sendMessage(userText: String = _uiState.value.inputText) {
        if (userText.isBlank()) return
        val currentSessionId = _uiState.value.activeSessionId ?: return
        val currentAgentId = _uiState.value.activeAgentId

        activeJob?.cancel()
        _uiState.update { it.copy(isGenerating = true, inputText = "", errorMessage = null) }

        activeJob = viewModelScope.launch {
            repository.saveUserMessage(currentSessionId, currentAgentId, userText)
            val history = _uiState.value.messages
            val result = repository.generateAgentResponse(currentSessionId, currentAgentId, history, userText)

            when (result) {
                is SendMessageResult.Success -> {
                    _uiState.update { it.copy(isGenerating = false) }
                }
                is SendMessageResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            errorMessage = result.errorMessage,
                            isQuotaOrKeyError = result.isQuotaOrKeyError
                        )
                    }
                }
            }
        }
    }

    fun stopGeneration() {
        activeJob?.cancel()
        _uiState.update { it.copy(isGenerating = false) }
    }

    fun retryLast() {
        val lastUser = _uiState.value.messages.lastOrNull { it.role == "user" }
        if (lastUser != null) {
            sendMessage(lastUser.content)
        }
    }

    fun speakMessage(agentId: String, text: String) {
        voiceService.speakAgentMessage(agentId, text)
    }

    fun startNewChat(agentId: String? = null) {
        val target = agentId ?: _uiState.value.activeAgentId
        createNewSession(target)
    }

    fun assignTask(agentId: String, title: String, prompt: String) {
        assignDirectTask(agentId, title, prompt)
    }

    fun assignDirectTask(agentId: String, title: String, prompt: String) {
        _uiState.update { it.copy(isAssigningTask = true) }
        viewModelScope.launch {
            repository.assignTask(agentId, title, prompt)
            _uiState.update { it.copy(isAssigningTask = false) }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun getMemoriesForAgent(agentId: String): Flow<List<AgentMemoryEntity>> {
        return repository.getMemoriesForAgent(agentId)
    }

    fun addMemory(agentId: String, category: String, content: String) {
        addManualMemory(agentId, category, content)
    }

    fun clearAllMemories() {
        clearMemories()
    }

    suspend fun executeCollabStage(
        agentId: String,
        objective: String,
        stageTitle: String,
        previousContext: String
    ): Pair<String, Boolean> {
        return repository.executeCollabStage(agentId, objective, stageTitle, previousContext)
    }

    // =========================================================================
    // Autonomous Multi-Agent Operations
    // =========================================================================

    fun submitAndRunDurableTask(title: String, objective: String) {
        viewModelScope.launch {
            val task = repository.submitDurableTask(title, objective)
            _uiState.update {
                it.copy(
                    activeDurableTaskId = task.id,
                    activeOperationsSubTab = 0
                )
            }
            observeStepsForTask(task.id)
            repository.runOrResumeDurableTask(task.id)
        }
    }

    fun resumeDurableTask(taskId: String) {
        viewModelScope.launch {
            observeStepsForTask(taskId)
            repository.runOrResumeDurableTask(taskId)
        }
    }

    fun cancelDurableTask(taskId: String) {
        viewModelScope.launch {
            repository.cancelDurableTask(taskId)
        }
    }

    fun selectDurableTask(taskId: String) {
        _uiState.update { it.copy(activeDurableTaskId = taskId) }
        observeStepsForTask(taskId)
    }

    fun resolveApproval(approvalId: String, isApproved: Boolean) {
        viewModelScope.launch {
            repository.resolveApproval(approvalId, isApproved)
        }
    }

    fun createSkill(name: String, agentId: String, description: String, instructions: String) {
        viewModelScope.launch {
            repository.createSkill(name, agentId, description, instructions)
        }
    }

    fun promoteSkill(skillId: String) {
        viewModelScope.launch {
            repository.promoteSkill(skillId)
        }
    }

    fun rollbackSkill(skillId: String) {
        viewModelScope.launch {
            repository.rollbackSkill(skillId)
        }
    }

    fun addRoutine(title: String, ownerId: String, triggerType: String, triggerConfig: String, prompt: String) {
        viewModelScope.launch {
            val routine = ScheduledRoutineEntity(
                id = java.util.UUID.randomUUID().toString(),
                title = title,
                ownerAgentId = ownerId,
                triggerType = triggerType,
                triggerConfig = triggerConfig,
                assignedSkillOrPrompt = prompt,
                status = "ACTIVE"
            )
            repository.addRoutine(routine)
        }
    }

    fun toggleRoutineStatus(routineId: String) {
        viewModelScope.launch {
            repository.toggleRoutineStatus(routineId)
        }
    }

    fun selectCloudFile(file: CloudFileEntity?) {
        _uiState.update { it.copy(selectedCloudFile = file) }
    }

    fun saveCloudFile(path: String, content: String, agentId: String = "forge") {
        viewModelScope.launch {
            repository.saveCloudFile(path, content, agentId)
        }
    }

    fun deleteCloudFile(fileId: String) {
        viewModelScope.launch {
            val result = repository.deleteCloudFile(fileId)
            if (result is ToolExecutionResult.RequiresApproval) {
                // Approval request created
                _uiState.update { it.copy(activeOperationsSubTab = 4) }
            }
        }
    }

    fun runTerminalCommand(command: String) {
        if (command.isBlank()) return
        _uiState.update { it.copy(isExecutingCommand = true) }
        viewModelScope.launch {
            val result = repository.executeTerminalCommand(command)
            val output = when (result) {
                is ToolExecutionResult.Success -> result.output
                is ToolExecutionResult.RequiresApproval -> "⚠️ Action requires human approval before proceeding (Approval ID: ${result.approvalId})"
                is ToolExecutionResult.Error -> "❌ Error: ${result.error}"
            }
            _uiState.update {
                it.copy(
                    terminalOutput = output,
                    isExecutingCommand = false
                )
            }
        }
    }

    fun speakAgentMessage(agentId: String, text: String) {
        voiceService.speakAgentMessage(agentId, text)
    }

    fun stopSpeaking() {
        voiceService.stopSpeaking()
    }

    suspend fun executeLiveVoiceTurn(agentId: String, prompt: String): String {
        return repository.executeLiveVoiceTurn(agentId, prompt)
    }

    fun getCapabilities(): List<AgentCapability> {
        return repository.getCapabilities()
    }

    fun getInstructionsForAgent(agentId: String): Flow<List<AgentInstructionEntity>> {
        return repository.getInstructionsForAgent(agentId)
    }

    fun proposeInstructionEvolution(
        agentId: String,
        updatedPrompt: String,
        rationale: String,
        candidateScore: Float
    ) {
        viewModelScope.launch {
            repository.proposeInstructionEvolution(agentId, updatedPrompt, rationale, candidateScore)
        }
    }

    fun promoteInstruction(agentId: String, instructionId: String) {
        viewModelScope.launch {
            repository.promoteInstruction(agentId, instructionId)
        }
    }

    fun rollbackInstruction(agentId: String, targetId: String) {
        viewModelScope.launch {
            repository.rollbackInstruction(agentId, targetId)
        }
    }

    val allEvaluations: Flow<List<EvaluationRecordEntity>> = repository.allEvaluations

    fun getEvaluationsForAgent(agentId: String): Flow<List<EvaluationRecordEntity>> {
        return repository.getEvaluationsForAgent(agentId)
    }

    fun recordEvaluation(
        taskId: String,
        agentId: String,
        score: Float,
        feedback: String,
        isSafetyVerified: Boolean = true,
        isCorrectnessVerified: Boolean = true
    ) {
        viewModelScope.launch {
            repository.recordEvaluation(taskId, agentId, score, feedback, isSafetyVerified, isCorrectnessVerified)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceService.shutdown()
    }
}
