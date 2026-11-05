package com.example.gemini.theme

import androidx.compose.ui.graphics.Color

// LocalMind Shadcn Design Tokens (Dark Theme)
val DarkBackground = Color(0xFF212121) // surface-main
val DarkSurface = Color(0xFF171717) // surface-sidebar
val DarkSurfaceInput = Color(0xFF2F2F2F) // surface-input
val DarkSurfaceCard = Color(0xFF2F2F2F) // surface-card
val DarkBorder = Color(0xFF424242) // border-subtle
val DarkPrimaryText = Color(0xFFECECEC) // text-primary
val DarkMutedText = Color(0xFFB4B4B4) // text-muted
val DarkAccent = Color(0xFFFFFFFF) // primary accent (white on dark)

// LocalMind Shadcn Design Tokens (Light Theme)
val LightBackground = Color(0xFFFAFAFA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceInput = Color(0xFFFFFFFF)
val LightSurfaceCard = Color(0xFFFFFFFF)
val LightBorder = Color(0xFFE5E5E5)
val LightPrimaryText = Color(0xFF0A0A0A)
val LightMutedText = Color(0xFF71717A)
val LightAccent = Color(0xFF0A0A0A) // primary accent (black on light)

// LocalMind Claude Light Theme Tokens
val ClaudeBackground = Color(0xFFF5F4ED)
val ClaudeSurface = Color(0xFFFAF9F5)
val ClaudeBorder = Color(0xFFF0EEE6)
val ClaudePrimaryText = Color(0xFF141413)
val ClaudeSecondaryText = Color(0xFF5E5D59)
val ClaudeMutedText = Color(0xFF87867F)
val ClaudeAccent = Color(0xFFFF6B4A) // deep orange accent

// LocalMind Claude Dark Theme Tokens
val ClaudeDarkBackground = Color(0xFF141413)
val ClaudeDarkSurface = Color(0xFF30302E)
val ClaudeDarkBorder = Color(0xFF30302E)
val ClaudeDarkPrimaryText = Color(0xFFFAF9F5)
val ClaudeDarkSecondaryText = Color(0xFFB0AEA5)

// Status & Feedback Colors
val SuccessGreen = Color(0xFF22C55E)
val ErrorRed = Color(0xFFEF4444)
val WarningAmber = Color(0xFFF59E0B)

// Backward-compatible aliases for seamless compilation
val ClaudeTerracotta = Color(0xFFFF6B4A)
val ClaudeTerracottaDark = Color(0xFFE05335)
val ClaudeCream = LightBackground
val ClaudeWarmSurface = LightSurface
val ClaudeUserBubbleLight = LightSurface
val ClaudeThinkingLight = Color(0xFFF4F4F5)

val ClaudeDarkBg = DarkBackground
val ClaudeUserBubbleDark = DarkSurfaceCard
val ClaudeThinkingDark = Color(0xFF262626)

val CodeBlockBgDark = Color(0xFF18181B)
val TextPrimaryLight = LightPrimaryText
val TextSecondaryLight = LightMutedText
val TextPrimaryDark = DarkPrimaryText
val TextSecondaryDark = DarkMutedText

val QuotaGreen = SuccessGreen
val QuotaAmber = WarningAmber
val QuotaRed = ErrorRed

val GeminiBlue = Color(0xFF3B82F6)
val GeminiPurple = Color(0xFF8B5CF6)

