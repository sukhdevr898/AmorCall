package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Duora Brand Palette — 100% Visual Match to Design Reference
// Exact Brand Colors from Design Specification:
val DuoraPrimaryPink = Color(0xFFFF2D8D)
val DuoraSecondaryPink = Color(0xFFFF5D88)
val DuoraSoftBlush = Color(0xFFFBEAEB)
val DuoraPaleLavender = Color(0xFFE8DEFF)
val DuoraBg = Color(0xFFFFF6FA)
val DuoraPrimaryText = Color(0xFF21102E)
val DuoraSecondaryText = Color(0xFF89818E)
val DuoraInputBorder = Color(0xFFE8DFE6)
val DuoraSuccess = Color(0xFF16A765)
val DuoraError = Color(0xFFE53945)

// Aliases for backward compatibility
val DuoraHotPink = DuoraPrimaryPink
val DuoraPinkGradientStart = DuoraPrimaryPink
val DuoraPinkGradientEnd = DuoraSecondaryPink
val DuoraPaleSky = DuoraSoftBlush
val DuoraRaspberry = DuoraPrimaryPink

// Supporting Light Tokens
val DuoraLightBg = DuoraBg
val DuoraLightSurface = Color(0xFFFFFFFF)
val DuoraLightPrimaryText = DuoraPrimaryText
val DuoraLightSecondaryText = DuoraSecondaryText
val DuoraLightOutline = DuoraInputBorder

// Supporting Dark Tokens
val DuoraDarkBg = Color(0xFF140D18)
val DuoraDarkSurface = Color(0xFF1E1424)
val DuoraDarkElevatedSurface = Color(0xFF2C1E34)
val DuoraDarkPrimaryText = Color(0xFFFFF5F8)
val DuoraDarkSecondaryText = Color(0xFFD6C8DC)
val DuoraDarkOutline = Color(0xFF4A3456)

// Backward-compatible tokens and functional accents
val DuoraLilac = DuoraPaleSky
val DuoraRose = Color(0xFFFF3B7A)
val DuoraCardSurface = DuoraDarkElevatedSurface
val DuoraCardBorder = Color(0xFFFFDCE6)
val DuoraLightPrimary = DuoraHotPink

// Theme tokens & backward-compatible aliases
val LoveCrimson = DuoraHotPink
val LoveRose = DuoraRose
val LoveBlush = DuoraPaleSky
val LoveGold = Color(0xFFFBBF24)
val LoveDarkBg = DuoraDarkBg
val LoveDarkSurface = DuoraDarkSurface
val LoveCardSurface = DuoraCardSurface
val LoveCardBorder = DuoraCardBorder
val LoveAccentGreen = Color(0xFF10B981)
val LoveWhite = Color(0xFFFBF9FF)
val LoveTextMuted = DuoraDarkSecondaryText
val LoveOverlay = Color(0xB3100D18)

// Dark Slate Theme
val DarkBg = DuoraDarkBg
val DarkSurface = DuoraDarkSurface
val DarkCardSurface = DuoraDarkElevatedSurface
val DarkCardBorder = DuoraDarkOutline
val DarkTextPrimary = DuoraDarkPrimaryText
val DarkTextSecondary = DuoraDarkSecondaryText

// Light Clean Theme
val LightBg = DuoraLightBg
val LightSurface = DuoraLightSurface
val LightCardSurface = DuoraLightSurface
val LightCardBorder = DuoraLightOutline
val LightTextPrimary = DuoraLightPrimaryText
val LightTextSecondary = DuoraLightSecondaryText
