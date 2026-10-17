package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// Claude Warm Editorial Design Tokens (Anthropic Warm-Canvas System)
// =========================================================================

// Primary & Accents
val EditorialPrimaryCoral = Color(0xFFCC785C)
val EditorialPrimaryCoralActive = Color(0xFFA9583E)
val EditorialPrimaryCoralDisabled = Color(0xFFE6DFD8)

// Typography & Ink
val EditorialInk = Color(0xFF141413)
val EditorialBody = Color(0xFF3D3D3A)
val EditorialBodyStrong = Color(0xFF252523)
val EditorialMuted = Color(0xFF6C6A64)
val EditorialMutedSoft = Color(0xFF8E8B82)

// Borders & Hairlines
val EditorialHairline = Color(0xFFE6DFD8)
val EditorialHairlineSoft = Color(0xFFEBE6DF)

// Warm Cream Canvas & Card Surfaces
val EditorialCanvas = Color(0xFFFAF9F5)
val EditorialSurfaceSoft = Color(0xFFF5F0E8)
val EditorialSurfaceCard = Color(0xFFEFE9DE)
val EditorialSurfaceCreamStrong = Color(0xFFE8E0D2)

// Dark Product Surfaces (for code mockups, terminals & dark bands)
val EditorialSurfaceDark = Color(0xFF181715)
val EditorialSurfaceDarkElevated = Color(0xFF252320)
val EditorialSurfaceDarkSoft = Color(0xFF1F1E1B)

// On-Surface Typography
val EditorialOnPrimary = Color(0xFFFFFFFF)
val EditorialOnDark = Color(0xFFFAF9F5)
val EditorialOnDarkSoft = Color(0xFFA09D96)

// Semantic & Accents
val EditorialAccentTeal = Color(0xFF5DB8A6)
val EditorialAccentAmber = Color(0xFFE8A55A)
val EditorialSuccess = Color(0xFF5DB872)
val EditorialWarning = Color(0xFFD4A017)
val EditorialError = Color(0xFFC64545)

// =========================================================================
// Harmonized Application Palette (Aligned with Warm Editorial Specs)
// =========================================================================

// Dark Product Surfaces (Anthropic dark navy / warm charcoal)
val DeepNavyBg = EditorialSurfaceDark
val DeepNavySurface = EditorialSurfaceDarkSoft
val DeepNavyElevated = EditorialSurfaceDarkElevated
val DeepNavyBorder = Color(0xFF33312E)

// Warm Cream Surfaces
val CreamSurface = EditorialCanvas
val CreamSurfaceSoft = EditorialSurfaceSoft
val CreamBorder = EditorialHairline

// Signature Primary Accents
val CoralPrimary = EditorialPrimaryCoral
val BabyBlueAccent = EditorialPrimaryCoral // Primary CTA / Action color now warm coral
val PinkHighlight = Color(0xFFD97706)
val LemonLimeAccent = EditorialAccentTeal

// The 5 Specialist Agent Colors
val MochiPurple = Color(0xFF8B5CF6)
val ScoutCyan = EditorialAccentTeal
val ForgeOrange = EditorialPrimaryCoral
val PixelPink = Color(0xFFEC4899)
val GuardianGreen = EditorialSuccess

// Neutral Text
val TextWhite = EditorialOnDark
val TextLightMuted = EditorialOnDarkSoft
val TextDarkInk = EditorialInk
val TextDarkMuted = EditorialMuted

// Status
val StatusActive = EditorialPrimaryCoral
val StatusCompleted = EditorialSuccess
val StatusQueued = EditorialWarning
val StatusError = EditorialError
