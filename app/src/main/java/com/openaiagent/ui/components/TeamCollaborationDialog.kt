@file:Suppress("LongMethod", "MagicNumber")

package com.openaiagent.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

/**
 * Five-stage collaboration dialog for Open AI Agent.
 *
 * This UI is intentionally decoupled from a particular Gemini SDK or Room schema.
 * Supply a real CollaborationExecutor and a CollaborationPersistence implementation
 * from your app. If no executor is supplied, the UI uses explicitly labelled DEMO MODE.
 */

enum class CollaborationStage(
    val agentName: String,
    val role: String,
    val emoji: String
) {
    MOCHI("Mochi", "Manager · breaks down the task", "🐻"),
    SCOUT("Scout", "Researcher · investigates requirements", "🔎"),
    FORGE("Forge", "Developer · proposes implementation", "🛠️"),
    PIXEL("Pixel", "Creative · defines UI/UX and copy", "🎨"),
    GUARDIAN("Guardian", "Reviewer · checks quality and security", "🛡️")
}

enum class StageStatus {
    PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
}

enum class ExecutionMode {
    REAL_AI, DEMO
}

data class StageResult(
    val stage: CollaborationStage,
    val status: StageStatus = StageStatus.PENDING,
    val output: String = "",
    val errorMessage: String? = null,
    val startedAt: String? = null,
    val finishedAt: String? = null
)

data class CollaborationReport(
    val id: String = UUID.randomUUID().toString(),
    val task: String,
    val mode: ExecutionMode,
    val stages: List<StageResult>,
    val createdAt: String = Instant.now().toString(),
    val cancelled: Boolean = false
) {
    fun toPlainText(): String = buildString {
        appendLine("OPEN AI AGENT · TEAM COLLABORATION REPORT")
        appendLine("Task: $task")
        appendLine("Mode: ${if (mode == ExecutionMode.REAL_AI) "REAL AI" else "DEMO MODE (simulated)"}")
        appendLine("Report ID: $id")
        appendLine("Created: $createdAt")
        appendLine()
        stages.forEach { result ->
            appendLine("${result.stage.emoji} ${result.stage.agentName} — ${result.status}")
            if (result.output.isNotBlank()) appendLine(result.output)
            result.errorMessage?.let { appendLine("Error: $it") }
            appendLine()
        }
        if (cancelled) appendLine("Workflow was cancelled before all stages completed.")
        if (mode == ExecutionMode.DEMO) {
            appendLine("NOTICE: This report contains simulated demo output, not verified model responses.")
        }
    }
}

/** One stage receives the original task plus relevant outputs from previous stages. */
fun interface CollaborationExecutor {
    suspend fun execute(
        stage: CollaborationStage,
        task: String,
        previousResults: List<StageResult>
    ): String
}

/**
 * Persist each update to Room through your repository/DAO.
 * Implement these methods in the app layer; this file does not assume a schema.
 */
interface CollaborationPersistence {
    suspend fun saveReport(report: CollaborationReport)
}

/**
 * Optional hook for real execution. Set this to null to force explicitly labelled demo mode.
 */
@Composable
fun TeamCollaborationDialog(
    onDismiss: () -> Unit,
    executor: CollaborationExecutor? = null,
    persistence: CollaborationPersistence? = null,
    initialTask: String = "",
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    var task by remember(initialTask) { mutableStateOf(initialTask) }
    var running by remember { mutableStateOf(false) }
    var expandedStage by remember { mutableStateOf(CollaborationStage.MOCHI) }
    var workflowJob by remember { mutableStateOf<Job?>(null) }
    var cancelled by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var lastCopied by remember { mutableStateOf(false) }

    val results = remember {
        mutableStateListOf<StageResult>().apply {
            addAll(CollaborationStage.entries.map { StageResult(stage = it) })
        }
    }
    val mode = if (executor == null) ExecutionMode.DEMO else ExecutionMode.REAL_AI

    suspend fun persistSnapshot(isCancelled: Boolean = false) {
        val store = persistence ?: return
        try {
            store.saveReport(
                CollaborationReport(
                    task = task.trim(),
                    mode = mode,
                    stages = results.toList(),
                    cancelled = isCancelled
                )
            )
            saveError = null
        } catch (e: Exception) {
            saveError = e.message ?: "Could not save collaboration report."
        }
    }

    fun startWorkflow() {
        if (running || task.isBlank()) return
        running = true
        cancelled = false
        generalError = null
        saveError = null
        lastCopied = false
        results.indices.forEach { results[it] = StageResult(stage = CollaborationStage.entries[it]) }

        workflowJob = scope.launch {
            try {
                for (stage in CollaborationStage.entries) {
                    if (!running) break
                    val index = results.indexOfFirst { it.stage == stage }
                    val started = results[index].copy(
                        status = StageStatus.RUNNING,
                        startedAt = Instant.now().toString()
                    )
                    results[index] = started
                    persistSnapshot()

                    try {
                        val prior = results.take(index).filter { it.status == StageStatus.COMPLETED }
                        val output = if (executor != null) {
                            executor.execute(stage, task.trim(), prior)
                        } else {
                            demoStageOutput(stage, task.trim(), prior)
                        }
                        if (!running) {
                            results[index] = started.copy(
                                status = StageStatus.CANCELLED,
                                finishedAt = Instant.now().toString()
                            )
                            break
                        }
                        results[index] = started.copy(
                            status = StageStatus.COMPLETED,
                            output = output,
                            finishedAt = Instant.now().toString()
                        )
                        expandedStage = stage
                        persistSnapshot()
                    } catch (e: CancellationException) {
                        results[index] = started.copy(
                            status = StageStatus.CANCELLED,
                            finishedAt = Instant.now().toString()
                        )
                        throw e
                    } catch (e: Exception) {
                        results[index] = started.copy(
                            status = StageStatus.FAILED,
                            errorMessage = e.message ?: "Stage failed unexpectedly.",
                            finishedAt = Instant.now().toString()
                        )
                        generalError = "${stage.agentName} failed. You can review the error below."
                        persistSnapshot()
                        break
                    }
                }
            } catch (_: CancellationException) {
                cancelled = true
            } finally {
                running = false
                persistSnapshot(isCancelled = cancelled)
            }
        }
    }

    fun cancelWorkflow() {
        cancelled = true
        running = false
        workflowJob?.cancel(CancellationException("Cancelled by user"))
    }

    DisposableEffect(Unit) {
        onDispose { workflowJob?.cancel() }
    }

    AlertDialog(
        onDismissRequest = {
            if (!running) onDismiss()
        },
        modifier = modifier,
        title = {
            Column {
                Text("Team collaboration", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    if (mode == ExecutionMode.REAL_AI) "REAL AI · sequential agent execution"
                    else "DEMO MODE · simulated outputs",
                    color = if (mode == ExecutionMode.REAL_AI) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = task,
                    onValueChange = { task = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("What should the team do?") },
                    placeholder = { Text("e.g. Review my Android app architecture") },
                    enabled = !running,
                    minLines = 2,
                    maxLines = 4
                )

                if (mode == ExecutionMode.DEMO) {
                    Text(
                        "No real model is connected. Connect a CollaborationExecutor to run actual AI calls.",
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { startWorkflow() },
                        enabled = !running && task.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(if (running) "Running…" else "Run team")
                    }
                    OutlinedButton(
                        onClick = { cancelWorkflow() },
                        enabled = running
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Cancel workflow")
                        Spacer(Modifier.width(4.dp))
                        Text("Cancel")
                    }
                }

                val completed = results.count { it.status == StageStatus.COMPLETED }
                Text(
                    "Progress: $completed / ${CollaborationStage.entries.size} stages completed",
                    style = MaterialTheme.typography.labelMedium
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(results, key = { it.stage.name }) { result ->
                        StageResultCard(
                            result = result,
                            expanded = expandedStage == result.stage,
                            onToggle = {
                                expandedStage =
                                    if (expandedStage == result.stage) CollaborationStage.MOCHI
                                    else result.stage
                            }
                        )
                    }
                }

                generalError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                saveError?.let {
                    Text("Persistence warning: $it", color = MaterialTheme.colorScheme.error)
                }

                OutlinedButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(
                            CollaborationReport(
                                task = task.trim(),
                                mode = mode,
                                stages = results.toList(),
                                cancelled = cancelled
                            ).toPlainText()
                        ))
                        lastCopied = true
                    },
                    enabled = results.any { it.status != StageStatus.PENDING },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (lastCopied) "Report copied ✓" else "Copy report")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, enabled = !running) { Text("Close") }
        },
        dismissButton = null
    )
}

@Composable
private fun StageResultCard(
    result: StageResult,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val statusColor = when (result.status) {
        StageStatus.COMPLETED -> MaterialTheme.colorScheme.primary
        StageStatus.FAILED -> MaterialTheme.colorScheme.error
        StageStatus.RUNNING -> MaterialTheme.colorScheme.tertiary
        StageStatus.CANCELLED -> MaterialTheme.colorScheme.outline
        StageStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(result.stage.emoji, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(result.stage.agentName, fontWeight = FontWeight.SemiBold)
                    Text(result.stage.role, style = MaterialTheme.typography.bodySmall)
                }
                when (result.status) {
                    StageStatus.RUNNING -> CircularProgressIndicator(
                        modifier = Modifier.width(22.dp).height(22.dp),
                        strokeWidth = 2.dp
                    )
                    StageStatus.COMPLETED -> Icon(
                        Icons.Default.CheckCircle, contentDescription = "Completed", tint = statusColor
                    )
                    StageStatus.FAILED -> Icon(
                        Icons.Default.Error, contentDescription = "Failed", tint = statusColor
                    )
                    StageStatus.CANCELLED, StageStatus.PENDING -> Icon(
                        Icons.Default.HourglassEmpty, contentDescription = result.status.name, tint = statusColor
                    )
                }
                TextButton(onClick = onToggle) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse result" else "Expand result"
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    HorizontalDivider()
                    if (result.output.isNotBlank()) {
                        Text(result.output, style = MaterialTheme.typography.bodyMedium)
                    } else if (result.errorMessage != null) {
                        Text(result.errorMessage, color = MaterialTheme.colorScheme.error)
                    } else {
                        Text(
                            when (result.status) {
                                StageStatus.PENDING -> "Waiting for this stage."
                                StageStatus.RUNNING -> "Working on this stage…"
                                StageStatus.COMPLETED -> "Completed with no text output."
                                StageStatus.FAILED -> "This stage failed."
                                StageStatus.CANCELLED -> "This stage was cancelled."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    result.startedAt?.let { Text("Started: $it", style = MaterialTheme.typography.labelSmall) }
                    result.finishedAt?.let { Text("Finished: $it", style = MaterialTheme.typography.labelSmall) }
                }
            }
        }
    }
}

private fun demoStageOutput(
    stage: CollaborationStage,
    task: String,
    previousResults: List<StageResult>
): String = when (stage) {
    CollaborationStage.MOCHI ->
        "DEMO PLAN for: \"$task\"\n1. Clarify the goal and constraints.\n2. Gather evidence and dependencies.\n3. Implement a solution.\n4. Review usability and security.\n5. Deliver a verified report."
    CollaborationStage.SCOUT ->
        "DEMO RESEARCH: Identify relevant requirements, dependencies, and unknowns for \"$task\". No web search was performed in demo mode."
    CollaborationStage.FORGE ->
        "DEMO IMPLEMENTATION PLAN: Prepare modular implementation, error handling, tests, and documentation for \"$task\". No production code was executed or built."
    CollaborationStage.PIXEL ->
        "DEMO UX PLAN: Use clear hierarchy, accessible contrast, concise bilingual labels, useful loading/error states, and responsive layouts."
    CollaborationStage.GUARDIAN ->
        "DEMO REVIEW: Verify inputs, protect secrets, handle failures, test edge cases, and confirm the build before release. This is a checklist, not a real security audit."
}
