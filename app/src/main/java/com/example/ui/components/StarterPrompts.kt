package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Agent
import com.example.ui.theme.BabyBlueAccent
import com.example.ui.theme.DeepNavyBorder
import com.example.ui.theme.DeepNavyElevated
import com.example.ui.theme.DeepNavySurface
import com.example.ui.theme.TextLightMuted
import com.example.ui.theme.TextWhite

data class StarterPrompt(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val prompt: String
)

@Composable
fun StarterPromptsView(
    agent: Agent,
    onSelectPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val starters = when (agent.id) {
        "scout" -> listOf(
            StarterPrompt(
                title = "Technical Comparison",
                subtitle = "Compare Jetpack Compose vs Flutter in 2026",
                icon = Icons.Default.Psychology,
                prompt = "Provide a deep comparative analysis between Jetpack Compose and Flutter for mobile development."
            ),
            StarterPrompt(
                title = "গবেষণা ও তথ্য বিশ্লেষণ",
                subtitle = "কোয়ান্টাম কম্পিউটিং কীভাবে কাজ করে সহজ ভাষায় বলুন",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                prompt = "কোয়ান্টাম কম্পিউটিং সাধারণ কম্পিউটারের চেয়ে কীভাবে আলাদা এবং এটি কীভাবে কাজ করে সহজ ভাষায় বাংলায় ব্যাখ্যা করুন।"
            )
        )
        "forge" -> listOf(
            StarterPrompt(
                title = "Kotlin & Room Implementation",
                subtitle = "Write a production-grade Room DAO & Repository",
                icon = Icons.Default.Code,
                prompt = "Write a complete, clean Kotlin Android Room Database DAO and Repository with Flow."
            ),
            StarterPrompt(
                title = "Python ডেটা অ্যালগরিদম",
                subtitle = "JSON ডেটা পার্স ও ক্লিন করার স্ক্রিপ্ট লিখুন",
                icon = Icons.Default.Code,
                prompt = "পাইথনে একটি নেস্টেড JSON ফাইল পার্স এবং ডেটা ভ্যালিডেট করার সম্পূর্ণ কোড লিখুন।"
            )
        )
        "pixel" -> listOf(
            StarterPrompt(
                title = "Dark Navy UI Design Palette",
                subtitle = "Suggest a modern palette with baby blue & lime accents",
                icon = Icons.Default.Lightbulb,
                prompt = "Provide a refined Material 3 color system with deep navy, cream, baby blue, and lime accents."
            ),
            StarterPrompt(
                title = "আকর্ষণীয় প্রোডাক্ট কপিরাইটিং",
                subtitle = "একটি নতুন এআই অ্যাপের জন্য সুন্দর ট্যাগলাইন ও পরিচিতি",
                icon = Icons.Default.Lightbulb,
                prompt = "Open AI Agent অ্যাপের জন্য বাংলায় ৩টি মনোমুগ্ধকর ট্যাগলাইন এবং সংক্ষিপ্ত পরিচিতি লিখুন।"
            )
        )
        "guardian" -> listOf(
            StarterPrompt(
                title = "Security & Privacy Audit",
                subtitle = "Checklist for Android API key protection",
                icon = Icons.Default.Shield,
                prompt = "Provide an exhaustive security audit checklist for protecting API keys and SQLite storage in Android."
            ),
            StarterPrompt(
                title = "বাগ ও এজ-কেস টেস্ট",
                subtitle = "নেটওয়ার্ক ফেইলিওর ও এজ-কেস হ্যান্ডলিং নির্দেশিকা",
                icon = Icons.Default.Shield,
                prompt = "মোবাইল অ্যাপে নেটওয়ার্ক টাইমআউট এবং অফলাইন স্টেট হ্যান্ডলিংয়ের জন্য গুরুত্বপূর্ণ টেস্ট কেসসমূহ কী কী?"
            )
        )
        else -> listOf( // Mochi
            StarterPrompt(
                title = "3-Phase Project Roadmap",
                subtitle = "Decompose building a mobile AI assistant",
                icon = Icons.Default.Psychology,
                prompt = "Act as Mochi the Manager: Break down developing a multi-agent AI assistant into a 3-phase structured roadmap."
            ),
            StarterPrompt(
                title = "টিম টাস্ক কোঅর্ডিনেশন",
                subtitle = "৫টি এজেন্টের মধ্যে দায়িত্ব বণ্টনের কর্মপরিকল্পনা",
                icon = Icons.Default.Lightbulb,
                prompt = "Mochi AI টিম (Scout, Forge, Pixel, Guardian) এর মধ্যে একটি নতুন প্রজেক্টের কাজ কীভাবে বণ্টন করবেন তার একটি পরিকল্পনা দিন।"
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Agent Emblem Portrait
        AgentAvatar(
            agentId = agent.id,
            size = 80.dp,
            showPulse = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = agent.name,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = agent.title,
            style = MaterialTheme.typography.bodyMedium,
            color = agent.color,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = agent.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        starters.forEachIndexed { index, item ->
            StarterCard(
                prompt = item,
                accentColor = agent.color,
                onClick = { onSelectPrompt(item.prompt) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .testTag("starter_prompt_$index")
            )
        }
    }
}

@Composable
fun StarterCard(
    prompt: StarterPrompt,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(accentColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = prompt.icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = prompt.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = prompt.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
