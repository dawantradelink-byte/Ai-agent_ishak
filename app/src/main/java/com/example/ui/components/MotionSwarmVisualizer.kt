package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Agent
import com.example.data.model.AgentTeam
import kotlin.math.cos
import kotlin.math.sin

/**
 * Interactive Kinetic Swarm Visualizer and Marquee inspired by motionsites.ai.
 * Highlights the live real-time interaction mesh between Mochi, Scout, Forge, Pixel, and Guardian.
 */
@Composable
fun KineticMarqueeBanner(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "marquee_anim")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -600f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "marquee_offset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .border(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "LIVE MOTION SWARM",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "5 CORES • 14ms LATENCY • REAL-TIME DISPATCH",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.0.sp,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Interactive Canvas Swarm Radar rendering live kinetic pulses, bezier communication rays,
 * and selectable agent node telemetry.
 */
@Composable
fun MotionSwarmVisualizerCard(
    onSelectAgent: (Agent) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAgent by remember { mutableStateOf<Agent?>(AgentTeam.Mochi) }

    val infiniteTransition = rememberInfiniteTransition(label = "swarm_physics")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_rotation"
    )

    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_radius"
    )

    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_phase"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceColor = MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("motion_swarm_radar_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.12f))
                            .border(1.dp, primaryColor.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Agent Swarm Radar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "motionsites.ai kinetic topology preview",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(primaryColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E7D32))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "60 FPS MESH",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = primaryColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Kinetic Canvas View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(surfaceColor)
                    .border(1.dp, outlineColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                val agents = AgentTeam.allAgents
                val surroundingAgents = remember { agents.filter { it.id != "mochi" } }

                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(8.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val center = Offset(w / 2f, h / 2f)
                    val orbitRadius = (minOf(w, h) / 2f) * 0.72f

                    // Draw subtle grid / concentric radar rings
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.08f),
                        radius = orbitRadius,
                        center = center,
                        style = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    )
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.04f),
                        radius = orbitRadius * 0.5f,
                        center = center,
                        style = Stroke(width = 0.8.dp.toPx())
                    )

                    // Draw animated pulse expanding from center
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.15f / pulseRadius),
                        radius = (orbitRadius * 0.35f) * pulseRadius,
                        center = center
                    )

                    // Draw communication beams to outer agent nodes
                    val angleStep = (2 * Math.PI) / surroundingAgents.size
                    val baseAngleRad = Math.toRadians(rotationAngle.toDouble())

                    surroundingAgents.forEachIndexed { index, ag ->
                        val currentAngle = baseAngleRad + index * angleStep
                        val nodeX = (center.x + orbitRadius * cos(currentAngle)).toFloat()
                        val nodeY = (center.y + orbitRadius * sin(currentAngle)).toFloat()
                        val nodeOffset = Offset(nodeX, nodeY)

                        // Beam line
                        drawLine(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.5f),
                                    ag.color.copy(alpha = 0.4f)
                                ),
                                start = center,
                                end = nodeOffset
                            ),
                            start = center,
                            end = nodeOffset,
                            strokeWidth = 1.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Traveling energy particle
                        val particleX = center.x + (nodeOffset.x - center.x) * particlePhase
                        val particleY = center.y + (nodeOffset.y - center.y) * particlePhase
                        drawCircle(
                            color = ag.color,
                            radius = 3.dp.toPx(),
                            center = Offset(particleX, particleY)
                        )

                        // Outer agent node glow
                        drawCircle(
                            color = ag.color.copy(alpha = 0.2f),
                            radius = 18.dp.toPx(),
                            center = nodeOffset
                        )
                        drawCircle(
                            color = ag.color,
                            radius = 9.dp.toPx(),
                            center = nodeOffset
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = nodeOffset
                        )
                    }

                    // Center Hub (Mochi)
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.25f),
                        radius = 24.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = 13.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 6.dp.toPx(),
                        center = center
                    )
                }

                // Interactive Agent Selector Pills at bottom overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(surfaceColor.copy(alpha = 0.92f))
                        .border(0.5.dp, outlineColor, RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AgentTeam.allAgents.forEach { ag ->
                            val isSel = selectedAgent?.id == ag.id
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSel) ag.color else Color.Transparent)
                                    .clickable {
                                        selectedAgent = ag
                                        onSelectAgent(ag)
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = ag.emoji,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Realtime Selected Agent Telemetry Strip
            selectedAgent?.let { ag ->
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ag.color.copy(alpha = 0.1f))
                        .border(1.dp, ag.color.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AgentAvatar(
                            agentId = ag.id,
                            size = 28.dp,
                            showPulse = true,
                            showBorder = false
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${ag.name} • ${ag.role.uppercase()}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = ag.title,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = ag.color
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = ag.color,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "12ms | ONLINE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = ag.color
                        )
                    }
                }
            }
        }
    }
}
