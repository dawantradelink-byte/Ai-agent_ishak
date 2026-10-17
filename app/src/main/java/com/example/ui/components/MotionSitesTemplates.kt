package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Agent
import com.example.data.model.AgentTeam

data class MotionTemplateStep(
    val agent: Agent,
    val phase: String,
    val description: String
)

data class MotionTemplate(
    val id: String,
    val title: String,
    val category: String,
    val categoryKey: String, // "ALL", "SWARM", "CODE", "MOTION", "RESEARCH", "SECURITY"
    val leadAgent: Agent,
    val supportingAgents: List<Agent>,
    val description: String,
    val estimatedTime: String,
    val complexity: String,
    val promptGoal: String,
    val steps: List<MotionTemplateStep>
)

object MotionTemplatesCatalog {
    val templates: List<MotionTemplate> = listOf(
        MotionTemplate(
            id = "template_fullstack",
            title = "Autonomous Full-Stack Sprint",
            category = "ENGINEERING",
            categoryKey = "CODE",
            leadAgent = AgentTeam.Forge,
            supportingAgents = listOf(AgentTeam.Scout, AgentTeam.Pixel, AgentTeam.Guardian),
            description = "End-to-end full-stack feature architecture, robust Kotlin implementation, Room SQLite encryption, and security audit.",
            estimatedTime = "~45s",
            complexity = "ADVANCED",
            promptGoal = "Build a production-ready Room database with biometric lock, zero-trust security & clean MVVM architecture",
            steps = listOf(
                MotionTemplateStep(AgentTeam.Mochi, "1. Architecture & Scope", "Define clean boundary interfaces & entity schemas"),
                MotionTemplateStep(AgentTeam.Scout, "2. Tech Benchmarks", "Retrieve Android Room best practices & crypto specs"),
                MotionTemplateStep(AgentTeam.Forge, "3. Kotlin Implementation", "Generate repository, Dao, and Room migrations"),
                MotionTemplateStep(AgentTeam.Guardian, "4. Security Audit", "Verify cryptographic keys and zero-trust permissions")
            )
        ),
        MotionTemplate(
            id = "template_motion_design",
            title = "Kinetic UI/UX Design System",
            category = "MOTION DESIGN",
            categoryKey = "MOTION",
            leadAgent = AgentTeam.Pixel,
            supportingAgents = listOf(AgentTeam.Mochi, AgentTeam.Forge),
            description = "Warm editorial cream canvas aesthetic, fluid spring micro-interactions, responsive tokens, and adaptive layouts inspired by motionsites.ai.",
            estimatedTime = "~30s",
            complexity = "CRAFT",
            promptGoal = "Create a motionsites.ai inspired UI component system with kinetic bezier canvas graphs, spring micro-interactions, and warm editorial tokens",
            steps = listOf(
                MotionTemplateStep(AgentTeam.Mochi, "1. UX Flow & Intent", "Map user journeys and interactive hierarchy"),
                MotionTemplateStep(AgentTeam.Pixel, "2. Warm Editorial Tokens", "Anchor tinted cream canvas (#FAF9F5) with warm coral (#CC785C)"),
                MotionTemplateStep(AgentTeam.Forge, "3. Spring Physics Engine", "Implement Compose spring specifications & custom canvas painters")
            )
        ),
        MotionTemplate(
            id = "template_deep_research",
            title = "Deep Intelligence & Market Intel",
            category = "RESEARCH",
            categoryKey = "RESEARCH",
            leadAgent = AgentTeam.Scout,
            supportingAgents = listOf(AgentTeam.Mochi, AgentTeam.Guardian),
            description = "Multi-source trend retrieval, architectural trade-offs matrix, and executive bilingual brief in English & বাংলা.",
            estimatedTime = "~25s",
            complexity = "ANALYTIC",
            promptGoal = "Conduct a comprehensive benchmark of multi-agent autonomous AI frameworks comparing latency, memory overhead, and local persistence",
            steps = listOf(
                MotionTemplateStep(AgentTeam.Scout, "1. Intelligence Ingestion", "Gather empirical metrics, latency profiles & memory traces"),
                MotionTemplateStep(AgentTeam.Mochi, "2. Synthesis & Matrix", "Score frameworks on developer velocity and data durability"),
                MotionTemplateStep(AgentTeam.Scout, "3. Bilingual Brief", "Publish executive summary in English and fluent বাংলা")
            )
        ),
        MotionTemplate(
            id = "template_security_audit",
            title = "Zero-Trust Security & App Hardening",
            category = "SECURITY",
            categoryKey = "SECURITY",
            leadAgent = AgentTeam.Guardian,
            supportingAgents = listOf(AgentTeam.Forge, AgentTeam.Scout),
            description = "Vulnerability surface audit, strict permission boundaries, cryptographic data storage, and Google Play compliance.",
            estimatedTime = "~35s",
            complexity = "CRITICAL",
            promptGoal = "Security audit and Google Play Store compliance review for Android, auditing permissions and storage security",
            steps = listOf(
                MotionTemplateStep(AgentTeam.Guardian, "1. Manifest Audit", "Ensure zero broad storage permissions & least privilege"),
                MotionTemplateStep(AgentTeam.Forge, "2. Storage Hardening", "Verify encrypted shared preferences & SQLCipher bindings"),
                MotionTemplateStep(AgentTeam.Guardian, "3. Play Policy Sign-off", "Enforce Google Play 2026 Developer Program rules")
            )
        ),
        MotionTemplate(
            id = "template_swarm_mega",
            title = "Consensus Swarm Orchestration",
            category = "SWARM",
            categoryKey = "SWARM",
            leadAgent = AgentTeam.Mochi,
            supportingAgents = listOf(AgentTeam.Scout, AgentTeam.Forge, AgentTeam.Pixel, AgentTeam.Guardian),
            description = "Full 5-agent collaborative mission executing sequential planning, research, coding, visual craft, and security sign-off.",
            estimatedTime = "~60s",
            complexity = "MAX IMPACT",
            promptGoal = "Launch a full 5-agent collaborative workflow: Mochi plans -> Scout researches -> Forge codes -> Pixel styles -> Guardian audits",
            steps = listOf(
                MotionTemplateStep(AgentTeam.Mochi, "1. Strategic Planning", "Decompose objective into discrete agent work orders"),
                MotionTemplateStep(AgentTeam.Scout, "2. Benchmark Retrieval", "Synthesize references and architectural patterns"),
                MotionTemplateStep(AgentTeam.Forge, "3. Core Code Generation", "Write type-safe, production-grade Kotlin logic"),
                MotionTemplateStep(AgentTeam.Pixel, "4. Visual & Kinetic Polish", "Harmonize typography, microcopy, and motion"),
                MotionTemplateStep(AgentTeam.Guardian, "5. Safety & Sign-off", "Verify zero vulnerabilities and sign execution log")
            )
        )
    )
}

/**
 * Curated Motion Templates Section inspired by motionsites.ai.
 * Allows instant exploration and 1-tap launching of multi-agent workflows.
 */
@Composable
fun MotionTemplatesSection(
    onLaunchTemplate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("ALL") }

    val categories = listOf(
        "ALL" to "All Workflows",
        "SWARM" to "⚡ Swarm",
        "CODE" to "🛠️ Code",
        "MOTION" to "🎨 Motion",
        "RESEARCH" to "🔎 Research",
        "SECURITY" to "🛡️ Security"
    )

    val filteredTemplates = remember(selectedCategory) {
        if (selectedCategory == "ALL") {
            MotionTemplatesCatalog.templates
        } else {
            MotionTemplatesCatalog.templates.filter { it.categoryKey == selectedCategory }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Workspaces,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "MOTIONSITES.AI TEMPLATES",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Curated multi-agent kinetic workflow blueprints",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .border(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${filteredTemplates.size} BLUEPRINTS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { (key, label) ->
                val isSelected = selectedCategory == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedCategory = key }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Template Cards List
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            filteredTemplates.forEach { template ->
                MotionTemplateCard(
                    template = template,
                    onLaunch = { onLaunchTemplate(template.promptGoal) }
                )
            }
        }
    }
}

@Composable
fun MotionTemplateCard(
    template: MotionTemplate,
    onLaunch: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "expand_arrow"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("motion_template_${template.id}")
            .clip(RoundedCornerShape(16.dp))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isExpanded) template.leadAgent.color.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Category & Complexity Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(template.leadAgent.color.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = template.category,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            letterSpacing = 1.0.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = template.leadAgent.color
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = template.estimatedTime,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = template.complexity,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Title & Lead Agent
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AgentAvatar(
                    agentId = template.leadAgent.id,
                    size = 40.dp,
                    showPulse = false
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Lead: ${template.leadAgent.name} (${template.leadAgent.role})",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = template.leadAgent.color
                    )
                }

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(rotationState)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = template.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Supporting Agents Avatars Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Team:",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    template.supportingAgents.forEach { ag ->
                        AgentAvatar(
                            agentId = ag.id,
                            size = 22.dp,
                            showPulse = false,
                            showBorder = false
                        )
                    }
                }

                Text(
                    text = "${template.steps.size} Stages",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Expandable Step-by-Step Breakdown
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            template.steps.forEach { step ->
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    AgentAvatar(
                                        agentId = step.agent.id,
                                        size = 20.dp,
                                        showPulse = false,
                                        showBorder = false
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = step.phase,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = step.agent.color
                                        )
                                        Text(
                                            text = step.description,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onLaunch,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("deploy_template_${template.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = template.leadAgent.color),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Deploy ${template.title}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
