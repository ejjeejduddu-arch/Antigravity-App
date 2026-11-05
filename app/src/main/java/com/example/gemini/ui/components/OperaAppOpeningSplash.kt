package com.example.gemini.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gemini.R
import com.example.gemini.theme.*
import kotlinx.coroutines.delay

/**
 * Stylish Opera-inspired App Opening Splash Screen.
 * Featuring the Opera butterfly icon with a glowing aura, spring bounce animation,
 * Petrona display typography, and smooth editorial wipe-reveal transition into the app.
 */
@Composable
fun OperaAppOpeningSplash(
    isDarkTheme: Boolean,
    onAnimationFinished: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.65f,
        animationSpec = spring(
            dampingRatio = 0.58f,
            stiffness = Spring.StiffnessLow
        ),
        label = "splash_logo_scale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing),
        label = "splash_logo_alpha"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 0.85f else 0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "splash_glow_alpha"
    )

    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = 250, easing = FastOutSlowInEasing),
        label = "splash_text_alpha"
    )

    val subtitleAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 500, delayMillis = 450, easing = FastOutSlowInEasing),
        label = "splash_subtitle_alpha"
    )

    val containerAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        startAnimation = true
        // Keep splash stylishly visible for ~1.35 seconds then gracefully fade into the workspace
        delay(1350)
        containerAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 350, easing = FastOutLinearInEasing)
        )
        onAnimationFinished()
    }

    val backgroundColor = if (isDarkTheme) OperaDarkBackground else OperaCream
    val textColor = if (isDarkTheme) OperaDarkTextPrimary else OperaTextPrimary
    val mutedColor = if (isDarkTheme) OperaDarkTextMuted else OperaTextMuted

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(containerAlpha.value)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // Glowing Ambient Backlight Aura
        Box(
            modifier = Modifier
                .size(240.dp)
                .alpha(glowAlpha)
                .scale(logoScale * 1.15f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            OperaRed.copy(alpha = if (isDarkTheme) 0.35f else 0.22f),
                            OperaRedBright.copy(alpha = if (isDarkTheme) 0.15f else 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Opera Butterfly Mascot / Splash Icon
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.opera_splash_icon),
                    contentDescription = "Opera Antigravity Logo",
                    modifier = Modifier.size(120.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Brand Title in Petrona Editorial Serif
            Text(
                text = "Antigravity",
                fontFamily = PetronaFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 32.sp,
                letterSpacing = (-0.5).sp,
                color = textColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle Tagline in Fragment Mono Technical Chrome
            Text(
                text = "AI FOR ALL • PRIVACY FOR FREEDOM",
                fontFamily = FragmentMonoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                letterSpacing = 2.sp,
                color = mutedColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(subtitleAlpha)
            )
        }
    }
}
