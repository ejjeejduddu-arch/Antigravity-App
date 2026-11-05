package com.example.gemini.theme

import androidx.compose.ui.graphics.Color

// Opera Design System Tokens (Light Theme)
val OperaRed = Color(0xFFFF1A1A)             // Primary Opera Red (interaction color, buttons, user bubbles)
val OperaRedBright = Color(0xFFFF4D4D)       // Primary bright (pressed / hover feedback)
val OperaCream = Color(0xFFFEFBF6)           // Warm editorial cream canvas
val OperaGroupedBg = Color(0xFFFAF9F6)       // Grouped canvas background
val OperaSurface = Color(0xFFFFFFFF)         // Clean white cards and fields
val OperaSurfaceMuted = Color(0xFFF9F9F9)    // Muted surface
val OperaSurfaceSubtle = Color(0xFFF5F5F5)   // Subtle surface
val OperaSurfacePressed = Color(0xFFEAEAEA)  // Pressed row / feedback surface
val OperaSurfaceCode = Color(0xFFF0F0F0)     // Code block host surface
val OperaBorder = Color(0x0D000000)          // 2px sticker outline (8% ink)
val OperaBorderOnPrimary = Color(0x52FFFFFF) // Border on primary red surfaces
val OperaShadowInk = Color(0x13000000)       // Hard offset sticker shadow ink

val OperaTextPrimary = Color(0xFF000000)     // Full-black text on cream for crisp editorial contrast
val OperaTextSecondary = Color(0xFF444444)   // Secondary body text
val OperaTextMuted = Color(0xFF888888)       // Muted captions & help text
val OperaTextOnPrimary = Color(0xFFFFFFFF)   // White on red

// Opera Design System Tokens (Dark Theme)
val OperaDarkBackground = Color(0xFF0F0F0E)       // Deep canvas background
val OperaDarkGroupedBg = Color(0xFF0B0B0A)        // Deep grouped background
val OperaDarkSurface = Color(0xFF1A1A1A)          // Dark surface
val OperaDarkSurfaceMuted = Color(0xFF1F1F1F)     // Dark muted surface
val OperaDarkSurfaceSubtle = Color(0xFF242424)    // Dark subtle surface
val OperaDarkSurfacePressed = Color(0xFF2C2C2C)   // Dark pressed surface
val OperaDarkBorder = Color(0x12FFFFFF)           // Dark subtle border
val OperaDarkShadowInk = Color(0x80000000)        // Dark shadow ink

val OperaDarkTextPrimary = Color(0xFFFFFFFF)      // Pure white text
val OperaDarkTextSecondary = Color(0xFFC9C9C9)    // Secondary light gray
val OperaDarkTextMuted = Color(0xFF8A8A8A)        // Muted gray

// Opera Incognito (Ephemeral Mode)
val OperaIncognito = Color(0xFF565A75)            // Purple-gray identity
val OperaIncognitoBright = Color(0xFF70748E)      // Pressed incognito
val OperaIncognitoPressed = Color(0xFF3E4157)     // Pressed dark
val OperaIncognitoSurface = Color(0xFF2A2A35)     // Composer dark surface

// Code & Status
val OperaCodeBlockBg = Color(0xFF1E1E1E)          // Light theme code block
val OperaCodeBlockBgDark = Color(0xFF0A0A0A)      // Dark theme code block
val OperaCodeInlineText = Color(0xFFD63384)       // Pink inline code
val OperaCodeInlineTextDark = Color(0xFFFF7AB6)   // Dark mode inline code pink
val OperaSnackbarBg = Color(0xFF444444)           // Dark floating pill snackbar
val OperaSnackbarBgDark = Color(0xFF2E2E2E)

// Voice / Accent Colors
val VoiceRed = Color(0xFFFF1A1A)
val VoicePink = Color(0xFFEE8695)
val VoiceAmber = Color(0xFFE49D22)
val VoiceBlue = Color(0xFF1A6DD2)

// Status & Feedback Colors
val SuccessGreen = Color(0xFF22C55E)
val ErrorRed = Color(0xFFFF4444)
val WarningAmber = Color(0xFFF59E0B)

// Backward-compatible design tokens mapped to Opera Design System
val LightBackground = OperaCream
val LightSurface = OperaSurface
val LightSurfaceInput = OperaSurface
val LightSurfaceCard = OperaSurface
val LightBorder = OperaBorder
val LightPrimaryText = OperaTextPrimary
val LightMutedText = OperaTextMuted
val LightAccent = OperaRed

val DarkBackground = OperaDarkBackground
val DarkSurface = OperaDarkSurface
val DarkSurfaceInput = OperaDarkSurfaceSubtle
val DarkSurfaceCard = OperaDarkSurfaceMuted
val DarkBorder = OperaDarkBorder
val DarkPrimaryText = OperaDarkTextPrimary
val DarkMutedText = OperaDarkTextMuted
val DarkAccent = OperaRed

val ClaudeBackground = OperaCream
val ClaudeSurface = OperaSurface
val ClaudeBorder = OperaBorder
val ClaudePrimaryText = OperaTextPrimary
val ClaudeSecondaryText = OperaTextSecondary
val ClaudeMutedText = OperaTextMuted
val ClaudeAccent = OperaRed

val ClaudeDarkBackground = OperaDarkBackground
val ClaudeDarkSurface = OperaDarkSurface
val ClaudeDarkBorder = OperaDarkBorder
val ClaudeDarkPrimaryText = OperaDarkTextPrimary
val ClaudeDarkSecondaryText = OperaDarkTextSecondary

val ClaudeTerracotta = OperaRed
val ClaudeTerracottaDark = OperaRedBright
val ClaudeCream = OperaCream
val ClaudeWarmSurface = OperaSurface
val ClaudeUserBubbleLight = OperaRed
val ClaudeThinkingLight = OperaSurfaceSubtle

val ClaudeDarkBg = OperaDarkBackground
val ClaudeUserBubbleDark = OperaRed
val ClaudeThinkingDark = OperaDarkSurfaceMuted

val CodeBlockBgDark = OperaCodeBlockBgDark
val TextPrimaryLight = OperaTextPrimary
val TextSecondaryLight = OperaTextSecondary
val TextPrimaryDark = OperaDarkTextPrimary
val TextSecondaryDark = OperaDarkTextSecondary

val QuotaGreen = SuccessGreen
val QuotaAmber = WarningAmber
val QuotaRed = ErrorRed

val GeminiBlue = Color(0xFF3B82F6)
val GeminiPurple = Color(0xFF8B5CF6)


