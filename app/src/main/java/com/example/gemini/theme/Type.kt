package com.example.gemini.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.gemini.R

// Opera Design System Font Families:
// 1. Petrona (Serif): Display only. Conversation headers, greeting titles, welcome screens
val PetronaFontFamily = FontFamily(
    Font(R.font.petrona_medium, FontWeight.Normal),
    Font(R.font.petrona_medium, FontWeight.Medium),
    Font(R.font.petrona_medium, FontWeight.SemiBold),
    Font(R.font.petrona_medium, FontWeight.Bold)
)

// 2. Figtree (Sans): Reading content. Chat bubbles, markdown body, captions, help text
val FigtreeFontFamily = FontFamily(
    Font(R.font.figtree_regular, FontWeight.Normal),
    Font(R.font.figtree_regular, FontWeight.Medium),
    Font(R.font.figtree_regular, FontWeight.SemiBold)
)

// 3. Fragment Mono: UI Chrome. Buttons, labels, badges, timestamps, code
val FragmentMonoFontFamily = FontFamily(
    Font(R.font.fragment_mono_regular, FontWeight.Normal),
    Font(R.font.fragment_mono_regular, FontWeight.Medium),
    Font(R.font.fragment_mono_regular, FontWeight.SemiBold)
)

// Backward-compatible font aliases
val OperaDisplayFont = PetronaFontFamily
val OperaBodyFont = FigtreeFontFamily
val OperaMonoFont = FragmentMonoFontFamily

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = PetronaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 48.sp,
        lineHeight = 54.sp
    ),
    displayMedium = TextStyle(
        fontFamily = PetronaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 36.sp,
        lineHeight = 42.sp
    ),
    displaySmall = TextStyle(
        fontFamily = PetronaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 26.sp,
        lineHeight = 32.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = PetronaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.2).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PetronaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.2).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = PetronaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FragmentMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.1).sp
    ),
    titleMedium = TextStyle(
        fontFamily = FragmentMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FragmentMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FigtreeFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FigtreeFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FigtreeFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FragmentMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FragmentMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FragmentMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp
    )
)


