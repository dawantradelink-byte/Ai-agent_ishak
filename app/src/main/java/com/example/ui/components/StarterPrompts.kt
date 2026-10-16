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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCreamStrong
import com.example.ui.theme.TextBody
import com.example.ui.theme.TextInk
import com.example.ui.theme.TextMuted

data class StarterPrompt(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val prompt: String
)

@Composable
fun StarterPromptsView(
    onSelectPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val starters = listOf(
        StarterPrompt(
            title = "অনুপ্রেরণামূলক গল্প",
            subtitle = "বাংলায় একটি ছোট অনুপ্রেরণামূলক গল্প লিখুন",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            prompt = "বাংলায় একটি ছোট কিন্তু গভীর অর্থপূর্ণ অনুপ্রেরণামূলক গল্প বলুন।"
        ),
        StarterPrompt(
            title = "Explain Complex Ideas",
            subtitle = "Simple breakdown of quantum computing",
            icon = Icons.Default.Psychology,
            prompt = "Explain quantum computing in simple, intuitive terms that anyone can understand."
        ),
        StarterPrompt(
            title = "Python কোড ও স্ক্রিপ্ট",
            subtitle = "JSON ডেটা প্রসেসিং পাইথন কোড",
            icon = Icons.Default.Code,
            prompt = "Write a clean, well-commented Python script to parse and clean a nested JSON file."
        ),
        StarterPrompt(
            title = "দৈনন্দিন টিপস ও গাইডলাইন",
            subtitle = "দিনটি সফল ও সুন্দর করার ৫টি পরামর্শ",
            icon = Icons.Default.Lightbulb,
            prompt = "আজকের দিনটি সুন্দর, প্রশান্তিময় এবং সফল করার জন্য ৫টি ব্যবহারিক পরামর্শ দিন।"
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Emblem
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(CoralPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "My AI Mark",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "My AI",
            style = MaterialTheme.typography.displayMedium,
            color = TextInk
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Where would you like to begin today?\nআজ আপনি কী বিষয়ে জানতে চান?",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        starters.forEachIndexed { index, item ->
            StarterCard(
                prompt = item,
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(1.dp, HairlineBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCreamStrong),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = prompt.icon,
                contentDescription = null,
                tint = CoralPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = prompt.title,
                style = MaterialTheme.typography.titleSmall,
                color = TextInk
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = prompt.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextBody,
                maxLines = 1
            )
        }
    }
}
