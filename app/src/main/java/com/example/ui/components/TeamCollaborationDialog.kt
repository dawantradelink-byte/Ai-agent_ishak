package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Agent
import com.example.data.model.AgentTeam
import com.example.ui.theme.BabyBlueAccent
import com.example.ui.theme.DeepNavyBorder
import com.example.ui.theme.DeepNavyElevated
import com.example.ui.theme.DeepNavySurface
import com.example.ui.theme.LemonLimeAccent
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusQueued
import com.example.ui.theme.TextLightMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class CollaborationStage(
    val agent: Agent,
    val stepTitle: String,
    val initialStatus: String, // "QUEUED", "RUNNING", "COMPLETED"
    var output: String = "",
    var isExpanded: Boolean = true
)

@Composable
fun TeamCollaborationDialog(
    viewModel: ChatViewModel,
    onDismiss: () -> Unit,
    initialGoal: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var objectiveInput by remember {
        mutableStateOf(
            initialGoal ?: "Build a production-ready Room database with biometric lock & zero-trust security"
        )
    }
    var isCollaborating by remember { mutableStateOf(false) }
    var currentStepIndex by remember { mutableIntStateOf(-1) }
    var collabJob by remember { mutableStateOf<Job?>(null) }

    val presetGoals = listOf(
        "Build a local encrypted SQLite Room database with biometric lock",
        "Design a modern Android e-commerce shopping experience with Compose",
        "Security audit and Google Play Store compliance review for Android",
        "Create a bilingual English & বাংলা AI assistant interface"
    )

    val stages = remember {
        mutableStateListOf(
            CollaborationStage(
                agent = AgentTeam.Mochi,
                stepTitle = "1. Strategic Planning & Team Delegation",
                initialStatus = "QUEUED",
                output = ""
            ),
            CollaborationStage(
                agent = AgentTeam.Scout,
                stepTitle = "2. Research & Architectural Benchmarks",
                initialStatus = "QUEUED",
                output = ""
            ),
            CollaborationStage(
                agent = AgentTeam.Forge,
                stepTitle = "3. Kotlin Code & Technical Implementation",
                initialStatus = "QUEUED",
                output = ""
            ),
            CollaborationStage(
                agent = AgentTeam.Pixel,
                stepTitle = "4. UI/UX Design System & Microcopy",
                initialStatus = "QUEUED",
                output = ""
            ),
            CollaborationStage(
                agent = AgentTeam.Guardian,
                stepTitle = "5. Security Audit & QA Verification",
                initialStatus = "QUEUED",
                output = ""
            )
        )
    }

    Dialog(
        onDismissRequest = { if (!isCollaborating) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GroupWork,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Multi-Agent Collaboration",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "5 Autonomous Agents Working in Orchestration",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isCollaborating,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Objective Input Box
                Text(
                    text = "TEAM GOAL OR OBJECTIVE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = objectiveInput,
                    onValueChange = { objectiveInput = it },
                    placeholder = { Text("What should the team build or investigate together?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    enabled = !isCollaborating,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("collab_objective_input")
                )

                // Quick preset goals
                if (!isCollaborating) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Or choose a preset goal:",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextLightMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    presetGoals.forEach { goal ->
                        Text(
                            text = "⚡ $goal",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = BabyBlueAccent,
                            modifier = Modifier
                                .clickable { objectiveInput = goal }
                                .padding(vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Launch or Cancel Collaboration Button Row
                if (isCollaborating) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BabyBlueAccent.copy(alpha = 0.25f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            CircularProgressIndicator(
                                color = BabyBlueAccent,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Step ${currentStepIndex + 1}/5 in Progress…",
                                color = TextWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                collabJob?.cancel()
                                isCollaborating = false
                                Toast.makeText(context, "Collaboration cancelled by user.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .height(44.dp)
                                .testTag("cancel_collab_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusError),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            if (objectiveInput.isNotBlank() && !isCollaborating) {
                                isCollaborating = true
                                collabJob = scope.launch {
                                    var accumulated = ""
                                    try {
                                        for (i in 0 until stages.size) {
                                            currentStepIndex = i
                                            stages[i] = stages[i].copy(initialStatus = "RUNNING", output = "")
                                            val (resultText, _) = viewModel.executeCollabStage(
                                                agentId = stages[i].agent.id,
                                                objective = objectiveInput,
                                                stageTitle = stages[i].stepTitle,
                                                previousContext = accumulated
                                            )
                                            stages[i] = stages[i].copy(output = resultText, initialStatus = "COMPLETED")
                                            accumulated += "\n\n[Stage ${i + 1}: ${stages[i].agent.name} (${stages[i].agent.role})]:\n$resultText"
                                        }
                                        currentStepIndex = 5
                                        isCollaborating = false

                                        // Persist into Tasks
                                        viewModel.assignTask(
                                            agentId = "mochi",
                                            title = "Team Collab: " + objectiveInput.take(28),
                                            prompt = "Multi-agent team completed collaboration on: $objectiveInput"
                                        )
                                        // Persist into Long-Term Shared Team Memory
                                        viewModel.addMemory(
                                            agentId = "shared",
                                            category = "SHARED_FINDING",
                                            content = "Team Collaboration on '$objectiveInput': All 5 agent stages completed."
                                        )
                                        Toast.makeText(context, "Collaboration complete! Saved to Tasks & Memory.", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        isCollaborating = false
                                    }
                                }
                            }
                        },
                        enabled = objectiveInput.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("launch_collab_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Deploy Multi-Agent Workflow",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Collaborative Pipeline Stages
                stages.forEachIndexed { index, stage ->
                    val isDone = stage.output.isNotBlank()
                    val isCurrent = currentStepIndex == index

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isCurrent) stage.agent.color else if (isDone) StatusCompleted.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { stage.isExpanded = !stage.isExpanded },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    AgentAvatar(
                                        agentId = stage.agent.id,
                                        size = 36.dp,
                                        showPulse = isCurrent
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = stage.stepTitle,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${stage.agent.name} • ${stage.agent.role}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = stage.agent.color
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isDone) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(StatusCompleted.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "COMPLETED",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = StatusCompleted
                                            )
                                        }
                                    } else if (isCurrent) {
                                        CircularProgressIndicator(
                                            color = stage.agent.color,
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = "QUEUED",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (stage.isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Expanded output
                            AnimatedVisibility(
                                visible = stage.isExpanded && stage.output.isNotBlank(),
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        IconButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("Stage Output", stage.output)
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "${stage.agent.name} stage copied!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy stage output",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(10.dp)
                                    ) {
                                        MarkdownContent(
                                            content = stage.output,
                                            textColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Copy Full Collaborative Report
                if (stages.all { it.output.isNotBlank() }) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            val fullReport = buildString {
                                appendLine("=== MOCHI AI TEAM MULTI-AGENT COLLABORATION REPORT ===")
                                appendLine("Objective: $objectiveInput\n")
                                stages.forEach { stage ->
                                    appendLine(stage.output)
                                    appendLine("\n" + "-".repeat(40) + "\n")
                                }
                            }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Multi-Agent Report", fullReport)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Full report copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusCompleted),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy Full Team Collaboration Report", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
