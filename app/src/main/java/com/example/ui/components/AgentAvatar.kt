@file:Suppress("MagicNumber", "LongMethod")

package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.AgentTeam
import com.openaiagent.ui.components.AgentIdentity
import kotlin.math.cos
import kotlin.math.sin

/**
 * Re-export AgentIdentity so callers can use either package interchangeably.
 */
typealias AgentIdentity = com.openaiagent.ui.components.AgentIdentity

/**
 * High-DPI crisp vector avatar renderer for the 5 distinct agents in the Open AI Agent team:
 * 1. Mochi 🐻: Cute fluffy white bear with luminous sapphire eyes, gentle smile & golden star badge
 * 2. Scout 🔎: Futuristic cyber-explorer helmet with panoramic glowing cyan visor & antenna beacon
 * 3. Forge 🛠️: Robotic inventor with industrial chassis, glowing amber LED matrix visor & ear gears
 * 4. Pixel 🎨: Whimsical creative persona with vibrant magenta swoop, star eyes & paint dabs
 * 5. Guardian 🛡️: Armored cyber-sentinel with emerald shield helm, glowing scanning slit & golden crest
 */
@Composable
fun CharacterAvatar(
    agentId: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    showPulse: Boolean = false,
    showBorder: Boolean = true
) {
    AgentAvatar(
        agentId = agentId,
        modifier = modifier,
        size = size,
        showPulse = showPulse,
        showBorder = showBorder
    )
}

fun getAvatarDrawableRes(agentId: String): Int = when (agentId.lowercase()) {
    "mochi" -> R.drawable.avatar_mochi
    "scout" -> R.drawable.avatar_scout
    "forge" -> R.drawable.avatar_forge
    "pixel" -> R.drawable.avatar_pixel
    "guardian" -> R.drawable.avatar_guardian
    else -> R.drawable.avatar_mochi
}

fun getAvatarDrawableRes(identity: AgentIdentity): Int = when (identity) {
    AgentIdentity.MOCHI -> R.drawable.avatar_mochi
    AgentIdentity.SCOUT -> R.drawable.avatar_scout
    AgentIdentity.FORGE -> R.drawable.avatar_forge
    AgentIdentity.PIXEL -> R.drawable.avatar_pixel
    AgentIdentity.GUARDIAN -> R.drawable.avatar_guardian
}

@Composable
fun AgentAvatar(
    agentId: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    showPulse: Boolean = false,
    showBorder: Boolean = true
) {
    val agent = AgentTeam.getById(agentId)

    val transition = rememberInfiniteTransition(label = "avatar_anim")
    val pulseScale by if (showPulse) {
        transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )
    } else {
        rememberInfiniteTransition(label = "noop").animateFloat(
            initialValue = 1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1000)),
            label = "noop"
        )
    }

    val currentSize = if (showPulse) size * pulseScale else size

    Box(
        modifier = modifier
            .size(currentSize)
            .then(
                if (showBorder) {
                    Modifier.border(1.5.dp, agent.color, CircleShape)
                } else Modifier
            )
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = getAvatarDrawableRes(agentId)),
            contentDescription = "${agent.name} Avatar",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun AgentAvatar(
    identity: AgentIdentity,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier,
    showBackground: Boolean = true
) {
    Image(
        painter = painterResource(id = getAvatarDrawableRes(identity)),
        contentDescription = identity.name,
        contentScale = ContentScale.Fit,
        modifier = modifier.size(size).clip(CircleShape)
    )
}

/** Individual reusable composables */
@Composable
fun MochiAvatar(modifier: Modifier = Modifier, size: Dp = 40.dp, showPulse: Boolean = false, showBorder: Boolean = true) =
    AgentAvatar("mochi", modifier, size, showPulse, showBorder)

@Composable
fun ScoutAvatar(modifier: Modifier = Modifier, size: Dp = 40.dp, showPulse: Boolean = false, showBorder: Boolean = true) =
    AgentAvatar("scout", modifier, size, showPulse, showBorder)

@Composable
fun ForgeAvatar(modifier: Modifier = Modifier, size: Dp = 40.dp, showPulse: Boolean = false, showBorder: Boolean = true) =
    AgentAvatar("forge", modifier, size, showPulse, showBorder)

@Composable
fun PixelAvatar(modifier: Modifier = Modifier, size: Dp = 40.dp, showPulse: Boolean = false, showBorder: Boolean = true) =
    AgentAvatar("pixel", modifier, size, showPulse, showBorder)

@Composable
fun GuardianAvatar(modifier: Modifier = Modifier, size: Dp = 40.dp, showPulse: Boolean = false, showBorder: Boolean = true) =
    AgentAvatar("guardian", modifier, size, showPulse, showBorder)

/* High-DPI Vector Canvas 100x100 Artboard Engine */
private data class Palette(val bg: Color, val main: Color, val secondary: Color, val accent: Color, val outline: Color, val dark: Color)

private fun palette(id: AgentIdentity) = when (id) {
    AgentIdentity.MOCHI -> Palette(Color(0xFFBFE3FF), Color(0xFFFFFCF8), Color(0xFFF2DCD6), Color(0xFF159DE8), Color(0xFF6D9EC9), Color(0xFF142B3D))
    AgentIdentity.SCOUT -> Palette(Color(0xFFB9F4F6), Color(0xFFB9C8D3), Color(0xFF718998), Color(0xFF00D9F2), Color(0xFF287E92), Color(0xFF102C3B))
    AgentIdentity.FORGE -> Palette(Color(0xFFFFD8B0), Color(0xFF66717D), Color(0xFF303A45), Color(0xFFFFA126), Color(0xFF39434E), Color(0xFF171F28))
    AgentIdentity.PIXEL -> Palette(Color(0xFFFFD2F0), Color(0xFFFFB2D9), Color(0xFF8546B9), Color(0xFFFF3DAD), Color(0xFF9C397D), Color(0xFF39213F))
    AgentIdentity.GUARDIAN -> Palette(Color(0xFFC8F4D9), Color(0xFF3A9E79), Color(0xFF17624F), Color(0xFF8DFFD0), Color(0xFF174C43), Color(0xFF102E2C))
}

private fun DrawScope.drawAgentArtboard(identity: AgentIdentity, showBackground: Boolean) {
    val p = palette(identity)
    if (showBackground) {
        drawCircle(p.bg, 48f, Offset(50f, 50f))
        drawCircle(p.outline.copy(alpha = 0.5f), 47f, Offset(50f, 50f), style = Stroke(1.5f))
        drawCircle(Color.White.copy(alpha = 0.22f), 7f, Offset(18f, 22f))
        drawCircle(Color.White.copy(alpha = 0.14f), 3f, Offset(79f, 18f))
    }
    when (identity) {
        AgentIdentity.MOCHI -> drawMochiVector()
        AgentIdentity.SCOUT -> drawScoutVector()
        AgentIdentity.FORGE -> drawForgeVector()
        AgentIdentity.PIXEL -> drawPixelVector()
        AgentIdentity.GUARDIAN -> drawGuardianVector()
    }
}

private fun DrawScope.drawMochiVector() {
    val white = Color(0xFFFFFCF8)
    val pink = Color(0xFFF4A5B4)
    val blue = Color(0xFF159DE8)
    val ink = Color(0xFF142B3D)

    // Outer ears
    val ear1 = Path().apply { moveTo(18f, 35f); cubicTo(9f, 15f, 16f, 5f, 28f, 13f); cubicTo(35f, 17f, 35f, 27f, 32f, 35f); close() }; drawPath(ear1, white)
    val ear2 = Path().apply { moveTo(68f, 34f); cubicTo(65f, 20f, 71f, 7f, 83f, 12f); cubicTo(95f, 17f, 92f, 29f, 83f, 37f); close() }; drawPath(ear2, white)

    // Inner ears
    val inner1 = Path().apply { moveTo(20f, 29f); cubicTo(17f, 19f, 21f, 15f, 27f, 20f); cubicTo(30f, 23f, 29f, 28f, 27f, 31f); close() }; drawPath(inner1, pink)
    val inner2 = Path().apply { moveTo(73f, 29f); cubicTo(73f, 20f, 79f, 16f, 84f, 21f); cubicTo(86f, 25f, 82f, 30f, 79f, 32f); close() }; drawPath(inner2, pink)

    // Fluffy Head Silhouette
    val head = Path().apply {
        moveTo(14f, 48f)
        cubicTo(10f, 38f, 20f, 31f, 29f, 34f)
        cubicTo(35f, 26f, 45f, 30f, 49f, 34f)
        cubicTo(59f, 27f, 69f, 31f, 73f, 37f)
        cubicTo(86f, 33f, 92f, 43f, 87f, 52f)
        cubicTo(94f, 65f, 85f, 83f, 72f, 87f)
        cubicTo(58f, 95f, 35f, 93f, 23f, 86f)
        cubicTo(8f, 78f, 8f, 62f, 14f, 48f)
        close()
    }
    drawPath(head, white)

    // Luminous Sapphire Eyes
    drawCircle(blue, 12f, Offset(35f, 56f))
    drawCircle(blue, 12f, Offset(66f, 56f))
    drawCircle(ink, 9f, Offset(35f, 57f))
    drawCircle(ink, 9f, Offset(66f, 57f))

    // Pupil Sparkles
    drawCircle(Color.White, 3.2f, Offset(31f, 52f))
    drawCircle(Color.White, 3.2f, Offset(62f, 52f))
    drawCircle(Color.White.copy(alpha = 0.85f), 1.5f, Offset(39f, 61f))
    drawCircle(Color.White.copy(alpha = 0.85f), 1.5f, Offset(70f, 61f))

    // Rosy Cheeks
    drawOval(pink.copy(alpha = 0.75f), Offset(20f, 65f), Size(12f, 6f))
    drawOval(pink.copy(alpha = 0.75f), Offset(68f, 65f), Size(12f, 6f))

    // Nose & Smile
    val nose = Path().apply { moveTo(44f, 66f); quadraticTo(50f, 61f, 56f, 66f); quadraticTo(54f, 72f, 50f, 72f); quadraticTo(46f, 72f, 44f, 66f); close() }
    drawPath(nose, Color(0xFF6B443E))
    val mouth = Path().apply { moveTo(50f, 72f); quadraticTo(45f, 78f, 40f, 73f); moveTo(50f, 72f); quadraticTo(55f, 78f, 60f, 73f) }
    drawPath(mouth, Color(0xFF6B443E), style = Stroke(1.8f, cap = StrokeCap.Round))

    // Manager Star Ribbon Collar
    drawRoundRect(Color(0xFF345A88), Offset(35f, 13f), Size(30f, 9f), CornerRadius(4f))
    drawCircle(Color(0xFFFFD45C), 6f, Offset(50f, 13f))
    drawPath(createStar(50f, 13f, 4.2f, 2f, 5), Color(0xFF8E5C14))
}

private fun DrawScope.drawScoutVector() {
    val p = palette(AgentIdentity.SCOUT)

    // Antenna & Glowing Pulse Beacon
    drawLine(p.outline, Offset(50f, 16f), Offset(50f, 25f), 3f, cap = StrokeCap.Round)
    drawCircle(p.accent, 5f, Offset(50f, 13f))
    drawCircle(Color.White, 1.7f, Offset(48.5f, 11.5f))
    drawArc(p.accent, 205f, 130f, false, Offset(38f, 3f), Size(24f, 20f), style = Stroke(1.5f))

    // Titanium Helmet Shell
    drawRoundRect(p.secondary, Offset(15f, 30f), Size(70f, 53f), CornerRadius(17f))
    drawRoundRect(p.main, Offset(20f, 28f), Size(60f, 52f), CornerRadius(15f))
    drawRoundRect(p.outline, Offset(12f, 43f), Size(10f, 19f), CornerRadius(4f))
    drawRoundRect(p.outline, Offset(78f, 43f), Size(10f, 19f), CornerRadius(4f))

    // Visor Cavity
    val vBg = Path().apply { moveTo(22f, 42f); quadraticTo(50f, 33f, 78f, 42f); lineTo(74f, 60f); quadraticTo(50f, 68f, 26f, 60f); close() }
    drawPath(vBg, p.dark)

    // Cyan HUD Visor
    val vGlow = Path().apply { moveTo(26f, 44f); quadraticTo(50f, 38f, 74f, 44f); lineTo(71f, 57f); quadraticTo(50f, 63f, 29f, 57f); close() }
    drawPath(vGlow, p.accent.copy(alpha = 0.35f))

    // Radar & Reticle
    drawLine(p.accent, Offset(29f, 50f), Offset(71f, 50f), 1.2f)
    drawCircle(p.accent, 8f, Offset(50f, 50f), style = Stroke(1.5f))
    drawCircle(p.accent, 2.2f, Offset(50f, 50f))
    drawLine(p.accent, Offset(50f, 39f), Offset(50f, 44f), 1.3f)
    drawLine(p.accent, Offset(50f, 56f), Offset(50f, 61f), 1.3f)
    drawLine(p.accent, Offset(39f, 50f), Offset(44f, 50f), 1.3f)
    drawLine(p.accent, Offset(56f, 50f), Offset(61f, 50f), 1.3f)

    // Jaw Comm Grille
    drawRoundRect(p.dark, Offset(37f, 68f), Size(26f, 7f), CornerRadius(3f))
    for (x in 41..59 step 6) {
        drawLine(p.accent, Offset(x.toFloat(), 70f), Offset(x.toFloat(), 73f), 1.5f)
    }
    drawLine(p.accent, Offset(27f, 67f), Offset(33f, 67f), 1.5f)
    drawLine(p.accent, Offset(67f, 67f), Offset(73f, 67f), 1.5f)
}

private fun DrawScope.drawForgeVector() {
    val p = palette(AgentIdentity.FORGE)

    // Ear Cog Gears
    drawGear(19f, 36f, 9f, p.dark, p.accent)
    drawGear(81f, 36f, 9f, p.dark, p.accent)

    // Robotic Chassis Faceplate
    val base = Path().apply { moveTo(25f, 25f); lineTo(75f, 25f); lineTo(84f, 37f); lineTo(80f, 77f); lineTo(69f, 87f); lineTo(31f, 87f); lineTo(20f, 77f); lineTo(16f, 37f); close() }
    drawPath(base, p.secondary)
    val face = Path().apply { moveTo(29f, 29f); lineTo(71f, 29f); lineTo(78f, 39f); lineTo(74f, 73f); lineTo(66f, 81f); lineTo(34f, 81f); lineTo(26f, 73f); lineTo(22f, 39f); close() }
    drawPath(face, p.main)

    // Hex Rivets
    listOf(Offset(27f, 38f), Offset(73f, 38f), Offset(29f, 72f), Offset(71f, 72f)).forEach {
        drawCircle(Color(0xFFD5DCE2), 2.6f, it)
        drawLine(p.dark, Offset(it.x - 1f, it.y), Offset(it.x + 1f, it.y), 0.9f)
    }

    // LED Screens
    drawRoundRect(p.dark, Offset(27f, 44f), Size(20f, 17f), CornerRadius(4f))
    drawRoundRect(p.dark, Offset(53f, 44f), Size(20f, 17f), CornerRadius(4f))

    // Glowing Amber LED Pixel Eyes
    for (x in listOf(32f, 38f, 58f, 64f)) {
        for (y in listOf(49f, 55f)) {
            drawCircle(p.accent, 1.7f, Offset(x, y))
        }
    }

    // Equalizer Smile
    drawLine(p.accent, Offset(43f, 67f), Offset(57f, 67f), 2.5f, cap = StrokeCap.Round)
    drawRoundRect(p.dark, Offset(36f, 74f), Size(28f, 5f), CornerRadius(2f))
    drawLine(Color(0xFF9DA7B1), Offset(40f, 76f), Offset(60f, 76f), 1f)
}

private fun DrawScope.drawPixelVector() {
    val p = palette(AgentIdentity.PIXEL)

    // Collar
    val collar = Path().apply { moveTo(22f, 73f); quadraticTo(50f, 61f, 78f, 73f); lineTo(87f, 91f); lineTo(13f, 91f); close() }
    drawPath(collar, p.secondary)

    // Face
    drawOval(p.main, Offset(20f, 22f), Size(60f, 63f))
    drawOval(Color(0xFFFFD7E9), Offset(26f, 30f), Size(48f, 48f))

    // Magenta Hair Swoop
    val hair1 = Path().apply { moveTo(19f, 46f); cubicTo(9f, 22f, 27f, 9f, 47f, 18f); cubicTo(63f, 7f, 85f, 20f, 80f, 40f); cubicTo(70f, 31f, 59f, 34f, 48f, 28f); cubicTo(39f, 40f, 28f, 43f, 19f, 46f); close() }
    drawPath(hair1, p.accent)
    val hair2 = Path().apply { moveTo(25f, 31f); cubicTo(36f, 16f, 52f, 17f, 61f, 23f); cubicTo(48f, 21f, 39f, 34f, 25f, 37f); close() }
    drawPath(hair2, Color(0xFFFF83D0))

    // Star-Eyes
    drawCircle(Color.White, 11f, Offset(36f, 52f))
    drawCircle(Color.White, 11f, Offset(64f, 52f))
    drawPath(createStar(36f, 52f, 8f, 3.7f, 5), Color(0xFF5E2E8B))
    drawPath(createStar(64f, 52f, 8f, 3.7f, 5), Color(0xFF5E2E8B))
    drawCircle(Color(0xFFFFE66B), 2.1f, Offset(36f, 52f))
    drawCircle(Color(0xFFFFE66B), 2.1f, Offset(64f, 52f))

    // Paint Dabs
    drawCircle(Color(0xFF00C9C8), 3.4f, Offset(24f, 65f))
    drawCircle(Color(0xFFFFD23F), 3.4f, Offset(31f, 68f))
    drawCircle(Color(0xFFFF5D8F), 3.4f, Offset(38f, 65f))

    // Smirk
    val mouth = Path().apply { moveTo(49f, 69f); quadraticTo(58f, 76f, 68f, 67f) }
    drawPath(mouth, Color(0xFF71365F), style = Stroke(2.4f, cap = StrokeCap.Round))
    drawPath(createStar(76f, 27f, 5f, 2.2f, 4), Color(0xFFFFE66B))
}

private fun DrawScope.drawGuardianVector() {
    val p = palette(AgentIdentity.GUARDIAN)

    // Shoulders
    val shoulderL = Path().apply { moveTo(19f, 36f); lineTo(29f, 28f); lineTo(31f, 72f); lineTo(22f, 77f); lineTo(15f, 63f); close() }; drawPath(shoulderL, p.secondary)
    val shoulderR = Path().apply { moveTo(81f, 36f); lineTo(71f, 28f); lineTo(69f, 72f); lineTo(78f, 77f); lineTo(85f, 63f); close() }; drawPath(shoulderR, p.secondary)

    // Armored Helm
    val helmOuter = Path().apply { moveTo(50f, 12f); lineTo(76f, 25f); lineTo(80f, 48f); cubicTo(79f, 67f, 64f, 81f, 50f, 88f); cubicTo(36f, 81f, 21f, 67f, 20f, 48f); lineTo(24f, 25f); close() }; drawPath(helmOuter, p.outline)
    val helmInner = Path().apply { moveTo(50f, 18f); lineTo(70f, 28f); lineTo(74f, 48f); cubicTo(73f, 63f, 61f, 75f, 50f, 81f); cubicTo(39f, 75f, 27f, 63f, 26f, 48f); lineTo(30f, 28f); close() }; drawPath(helmInner, p.main)

    // Facet Plates
    val facetL = Path().apply { moveTo(28f, 39f); lineTo(41f, 29f); lineTo(37f, 43f); lineTo(28f, 47f); close() }; drawPath(facetL, Color(0xFF5FD5A4))
    val facetR = Path().apply { moveTo(72f, 39f); lineTo(59f, 29f); lineTo(63f, 43f); lineTo(72f, 47f); close() }; drawPath(facetR, Color(0xFF5FD5A4))

    // Laser Visor Slit
    drawRoundRect(p.dark, Offset(27f, 43f), Size(46f, 12f), CornerRadius(4f))
    drawRoundRect(p.accent, Offset(31f, 47f), Size(38f, 3f), CornerRadius(1.5f))
    drawCircle(Color.White, 2f, Offset(50f, 48.5f))
    drawLine(p.accent.copy(alpha = 0.55f), Offset(33f, 58f), Offset(67f, 58f), 1.2f)

    // Golden Eagle Crest
    val crest = Path().apply { moveTo(50f, 21f); lineTo(45f, 27f); lineTo(35f, 24f); lineTo(42f, 32f); lineTo(50f, 29f); lineTo(58f, 32f); lineTo(65f, 24f); lineTo(55f, 27f); close() }
    drawPath(crest, Color(0xFFFFD46A))
    drawCircle(Color(0xFFFFD46A), 2.3f, Offset(50f, 28f))

    // Chin Security Guard
    val jaw = Path().apply { moveTo(32f, 62f); lineTo(42f, 67f); lineTo(50f, 72f); lineTo(58f, 67f); lineTo(68f, 62f); lineTo(63f, 73f); lineTo(50f, 79f); lineTo(37f, 73f); close() }
    drawPath(jaw, p.secondary)
    drawLine(p.accent, Offset(43f, 70f), Offset(57f, 70f), 1.5f)
}

private fun createStar(cx: Float, cy: Float, outer: Float, inner: Float, points: Int): Path = Path().apply {
    val step = (Math.PI / points).toFloat()
    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) outer else inner
        val a = (-Math.PI / 2.0).toFloat() + i * step
        val x = cx + cos(a) * r
        val y = cy + sin(a) * r
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

private fun DrawScope.drawGear(cx: Float, cy: Float, radius: Float, fill: Color, center: Color) {
    val g = Path()
    val teeth = 8
    for (i in 0 until teeth * 4) {
        val r = if (i % 4 < 2) radius else radius * 0.72f
        val a = (-Math.PI / 2.0).toFloat() + i * (Math.PI.toFloat() / (teeth * 2))
        val x = cx + cos(a) * r
        val y = cy + sin(a) * r
        if (i == 0) g.moveTo(x, y) else g.lineTo(x, y)
    }
    g.close()
    drawPath(g, fill)
    drawCircle(center, radius * 0.45f, Offset(cx, cy))
    drawCircle(fill, radius * 0.2f, Offset(cx, cy))
}
