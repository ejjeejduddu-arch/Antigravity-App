package com.example.gemini.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.UiModeManager
import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
val LocalMindDarkColorScheme = darkColorScheme(
    primary = OperaRed,
    onPrimary = Color.White,
    secondary = OperaRedBright,
    onSecondary = Color.White,
    background = OperaDarkBackground,
    onBackground = OperaDarkTextPrimary,
    surface = OperaDarkSurface,
    onSurface = OperaDarkTextPrimary,
    surfaceVariant = OperaDarkSurfaceSubtle,
    onSurfaceVariant = OperaDarkTextSecondary,
    outline = OperaDarkBorder,
    outlineVariant = OperaDarkSurfacePressed,
    surfaceTint = Color.Transparent,
    error = ErrorRed,
    onError = Color.White
)

val LocalMindLightColorScheme = lightColorScheme(
    primary = OperaRed,
    onPrimary = Color.White,
    secondary = OperaRedBright,
    onSecondary = Color.White,
    background = OperaCream,
    onBackground = OperaTextPrimary,
    surface = OperaSurface,
    onSurface = OperaTextPrimary,
    surfaceVariant = OperaSurfaceSubtle,
    onSurfaceVariant = OperaTextSecondary,
    outline = OperaBorder,
    outlineVariant = OperaSurfacePressed,
    surfaceTint = Color.Transparent,
    error = ErrorRed,
    onError = Color.White
)

val LocalMindClaudeColorScheme = lightColorScheme(
    primary = OperaRed,
    onPrimary = Color.White,
    secondary = OperaTextSecondary,
    onSecondary = Color.White,
    background = OperaCream,
    onBackground = OperaTextPrimary,
    surface = OperaSurface,
    onSurface = OperaTextPrimary,
    surfaceVariant = OperaSurfaceMuted,
    onSurfaceVariant = OperaTextMuted,
    outline = OperaBorder,
    outlineVariant = OperaSurfacePressed,
    surfaceTint = Color.Transparent,
    error = ErrorRed,
    onError = Color.White
)

val LocalMindClaudeDarkColorScheme = darkColorScheme(
    primary = OperaRed,
    onPrimary = Color.White,
    secondary = OperaDarkTextSecondary,
    onSecondary = Color.White,
    background = OperaDarkBackground,
    onBackground = OperaDarkTextPrimary,
    surface = OperaDarkSurface,
    onSurface = OperaDarkTextPrimary,
    surfaceVariant = OperaDarkSurfaceMuted,
    onSurfaceVariant = OperaDarkTextSecondary,
    outline = OperaDarkBorder,
    outlineVariant = OperaDarkSurfacePressed,
    surfaceTint = Color.Transparent,
    error = ErrorRed,
    onError = Color.White
)

private val DarkColorScheme = LocalMindDarkColorScheme
private val LightColorScheme = LocalMindLightColorScheme

val LocalIsDarkTheme = staticCompositionLocalOf { false }

@Composable
fun isAppInDarkTheme(): Boolean = LocalIsDarkTheme.current

@Composable
fun isSystemInDarkThemeRobust(): Boolean {
    val context = LocalContext.current
    val composeIsDark = isSystemInDarkTheme()

    var secureNightMode by remember {
        mutableIntStateOf(
            try {
                Settings.Secure.getInt(context.contentResolver, "ui_night_mode", -1)
            } catch (e: Exception) {
                -1
            }
        )
    }

    DisposableEffect(context) {
        val uri = Settings.Secure.getUriFor("ui_night_mode")
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                try {
                    secureNightMode = Settings.Secure.getInt(context.contentResolver, "ui_night_mode", -1)
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
        try {
            context.contentResolver.registerContentObserver(uri, false, observer)
        } catch (e: Exception) {
            // Ignore
        }
        onDispose {
            try {
                context.contentResolver.unregisterContentObserver(observer)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    val uiModeManager = remember(context) {
        context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
    }
    val isMgrDark = uiModeManager?.nightMode == UiModeManager.MODE_NIGHT_YES

    return when {
        secureNightMode == 2 -> true
        secureNightMode == 1 -> false
        isMgrDark -> true
        else -> composeIsDark
    }
}

@Composable
fun GeminiTheme(
    themeMode: String = "SYSTEM",
    darkTheme: Boolean = isSystemInDarkThemeRobust(),
    content: @Composable () -> Unit
) {
    val effectiveIsDark = when (themeMode) {
        "DARK", "CLAUDE_DARK" -> true
        "LIGHT", "CLAUDE" -> false
        else -> darkTheme
    }

    val colorScheme = when (themeMode) {
        "DARK" -> LocalMindDarkColorScheme
        "LIGHT" -> LocalMindLightColorScheme
        "CLAUDE" -> LocalMindClaudeColorScheme
        "CLAUDE_DARK" -> LocalMindClaudeDarkColorScheme
        else -> if (effectiveIsDark) LocalMindDarkColorScheme else LocalMindLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = view.context as? Activity
        if (activity != null && activity !is com.example.gemini.ui.bubble.FloatingChatActivity) {
            SideEffect {
                val window = activity.window
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !effectiveIsDark
                insetsController.isAppearanceLightNavigationBars = !effectiveIsDark
            }
        }
    }

    CompositionLocalProvider(
        LocalIsDarkTheme provides effectiveIsDark
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

