package com.example.ui.components

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Agent
import com.example.data.model.AgentTeam
import com.example.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import kotlin.math.sin

data class VoiceExchange(
    val speaker: String,
    val text: String,
    val isAgent: Boolean
)

/**
 * Real-time bidirectional Live Voice Conversation Dialog powered by gemini-3.8-live (Live API).
 * Allows fluid, natural speech conversations with Mochi, Scout, Forge, Pixel, and Guardian.
 */
@Composable
fun LiveVoiceConversationDialog(
    viewModel: ChatViewModel,
    initialAgentId: String = "mochi",
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedAgent by remember { mutableStateOf(AgentTeam.getById(initialAgentId)) }
    var isMicActive by remember { mutableStateOf(true) }
    var textInput by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    val speakingAgentId by viewModel.speakingAgentId.collectAsState()
    val isAgentSpeaking = speakingAgentId != null

    val conversation = remember {
        mutableStateListOf(
            VoiceExchange(
                speaker = selectedAgent.name,
                text = "Hello! I am ${selectedAgent.name}, powered by Gemini 3.8 Live API. What would you like to discuss?",
                isAgent = true
            )
        )
    }

    // Audio Visualizer Wave Animations
    val infiniteTransition = rememberInfiniteTransition(label = "audio_wave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Speech Recognition Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                conversation.add(VoiceExchange(speaker = "You", text = spokenText, isAgent = false))
                isProcessing = true

                // Send to Live API via ViewModel
                scope.launch {
                    val reply = viewModel.executeLiveVoiceTurn(selectedAgent.id, spokenText)
                    isProcessing = false
                    conversation.add(VoiceExchange(speaker = selectedAgent.name, text = reply, isAgent = true))
                    viewModel.speakMessage(selectedAgent.id, reply)
                }
            }
        }
    }

    Dialog(
        onDismissRequest = {
            viewModel.stopSpeaking()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, selectedAgent.color.copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Live API Status Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE53935).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFFE53935).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE53935))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE API • gemini-3.8-live",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = Color(0xFFE53935)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            viewModel.stopSpeaking()
                            onDismiss()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Live Conversation",
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hero Agent Avatar with Dynamic Audio Halo
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer pulsating audio halo
                    if (isAgentSpeaking || isProcessing) {
                        Box(
                            modifier = Modifier
                                .size((110 * pulseScale).dp)
                                .clip(CircleShape)
                                .background(selectedAgent.color.copy(alpha = 0.2f))
                        )
                    }

                    AgentAvatar(
                        agentId = selectedAgent.id,
                        size = 80.dp,
                        showPulse = isAgentSpeaking
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = selectedAgent.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${selectedAgent.emoji} ${selectedAgent.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = selectedAgent.color,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Audio Waveform Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val primaryColor = selectedAgent.color
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val midY = h / 2f
                        val barCount = 28
                        val barWidth = (w / barCount) * 0.6f

                        for (i in 0 until barCount) {
                            val x = i * (w / barCount) + barWidth / 2f
                            val factor = if (isAgentSpeaking) {
                                (sin(wavePhase + i * 0.4) * 0.45 + 0.5).toFloat()
                            } else if (isProcessing) {
                                (sin(wavePhase * 2 + i * 0.8) * 0.35 + 0.4).toFloat()
                            } else {
                                0.15f
                            }
                            val barH = h * factor * 0.85f

                            drawLine(
                                color = primaryColor.copy(alpha = if (isAgentSpeaking) 0.9f else 0.4f),
                                start = Offset(x, midY - barH / 2f),
                                end = Offset(x, midY + barH / 2f),
                                strokeWidth = barWidth,
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    if (isProcessing) {
                        Text(
                            text = "Streaming Gemini 3.8 Live API...",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = selectedAgent.color
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live Bidirectional Conversation Transcript
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        conversation.forEach { exchange ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (exchange.isAgent) Arrangement.Start else Arrangement.End
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.92f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (exchange.isAgent) selectedAgent.color.copy(alpha = 0.12f)
                                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        )
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = exchange.speaker,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            color = if (exchange.isAgent) selectedAgent.color else MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = exchange.text,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Conversation Starters
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val starters = listOf(
                        "Plan today's sprint",
                        "Analyze Android performance",
                        "Design kinetic Compose UI",
                        "বাংলায় কথা বলো (Speak in Bengali)",
                        "Audit security & permissions"
                    )
                    items(starters) { prompt ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                .clickable {
                                    conversation.add(VoiceExchange(speaker = "You", text = prompt, isAgent = false))
                                    isProcessing = true
                                    scope.launch {
                                        val reply = viewModel.executeLiveVoiceTurn(selectedAgent.id, prompt)
                                        isProcessing = false
                                        conversation.add(VoiceExchange(speaker = selectedAgent.name, text = reply, isAgent = true))
                                        viewModel.speakMessage(selectedAgent.id, reply)
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = prompt,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Voice & Text Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Speech Recognition Mic Button
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to ${selectedAgent.name} in English or বাংলা…")
                                }
                                speechLauncher.launch(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Voice input not available on emulator; use text below", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("live_mic_speak_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = selectedAgent.color),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Speak",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isProcessing) "Listening..." else "Tap to Speak",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Switch Agent Selector Bar
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            AgentTeam.allAgents.forEach { ag ->
                                val isSel = ag.id == selectedAgent.id
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isSel) ag.color.copy(alpha = 0.25f) else Color.Transparent)
                                        .clickable {
                                            selectedAgent = ag
                                            viewModel.stopSpeaking()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = ag.emoji, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Text Fallback Input Bar for testing without microphone
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Or type voice prompt…", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = selectedAgent.color,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("live_voice_text_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                val p = textInput.trim()
                                textInput = ""
                                conversation.add(VoiceExchange(speaker = "You", text = p, isAgent = false))
                                isProcessing = true
                                scope.launch {
                                    val reply = viewModel.executeLiveVoiceTurn(selectedAgent.id, p)
                                    isProcessing = false
                                    conversation.add(VoiceExchange(speaker = selectedAgent.name, text = reply, isAgent = true))
                                    viewModel.speakMessage(selectedAgent.id, reply)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(selectedAgent.color)
                            .testTag("live_voice_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
