package com.example.gemini.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gemini.R
import com.example.gemini.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Authentic Opera Ambient Canvas Background.
 *
 * Implements the signature Opera multi-layer ambient background:
 * 1. Warm cream (#FEFBF6) or deep dark (#0F0F0E) canvas tone.
 * 2. High-fidelity paper/canvas texture overlay (opera_bg_texture).
 * 3. Living, breathing ambient radiant glow aura orbs that slowly oscillate and drift.
 * 4. Multi-stop vertical fade gradient matching Opera design specifications.
 * 5. Optional top and bottom edge fade overlays for seamless message scroll clearance.
 */
@Composable
fun OperaAmbientBackground(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    showEdgeGradients: Boolean = true,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val canvasBg = if (isDarkTheme) OperaDarkBackground else OperaCream
    val textureAlpha = if (isDarkTheme) 0.035f else 0.025f

    // Ambient Breathing Animation Clock (continuous smooth periodic motion)
    val infiniteTransition = rememberInfiniteTransition(label = "opera_ambient_clock")
    val ambientPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "opera_ambient_phase"
    )

    val glowAlphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "opera_glow_pulse"
    )

    val auraPrimary = if (isDarkTheme) {
        OperaRed.copy(alpha = 0.08f * glowAlphaPulse)
    } else {
        OperaRed.copy(alpha = 0.045f * glowAlphaPulse)
    }

    val auraSecondary = if (isDarkTheme) {
        Color(0xFFFF8A65).copy(alpha = 0.05f * glowAlphaPulse)
    } else {
        Color(0xFFFFAB91).copy(alpha = 0.035f * glowAlphaPulse)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(canvasBg)
            .drawBehind {
                val w = size.width
                val h = size.height

                // Orb 1: Upper-left / center warm Opera Red glow drifting harmonically
                val orb1X = w * 0.38f + cos(ambientPhase) * (w * 0.08f)
                val orb1Y = h * 0.28f + sin(ambientPhase * 1.1f) * (h * 0.05f)
                val orb1Radius = (w * 0.75f).coerceAtLeast(320.dp.toPx())

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(auraPrimary, Color.Transparent),
                        center = Offset(orb1X, orb1Y),
                        radius = orb1Radius
                    ),
                    center = Offset(orb1X, orb1Y),
                    radius = orb1Radius
                )

                // Orb 2: Lower-right subtle warm peach/amber aura drifting in counter-phase
                val orb2X = w * 0.65f + sin(ambientPhase * 0.9f) * (w * 0.09f)
                val orb2Y = h * 0.68f + cos(ambientPhase * 1.2f) * (h * 0.06f)
                val orb2Radius = (w * 0.65f).coerceAtLeast(280.dp.toPx())

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(auraSecondary, Color.Transparent),
                        center = Offset(orb2X, orb2Y),
                        radius = orb2Radius
                    ),
                    center = Offset(orb2X, orb2Y),
                    radius = orb2Radius
                )
            }
    ) {
        // High-fidelity background texture
        Image(
            painter = painterResource(id = R.drawable.opera_bg_texture),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(textureAlpha)
        )

        // Authentic multi-stop vertical gradient wash from Opera index.tsx
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to canvasBg.copy(alpha = 0.90f),
                            0.15f to canvasBg.copy(alpha = 0.70f),
                            0.40f to canvasBg.copy(alpha = 0.40f),
                            0.65f to Color.Transparent,
                            0.82f to canvasBg.copy(alpha = 0.35f),
                            0.93f to canvasBg.copy(alpha = 0.75f),
                            1.00f to canvasBg
                        )
                    )
                )
        )

        // Main screen content layer
        content()

        // Top & Bottom Edge Gradient Fades (matching Opera ChatView gradientTop & gradientBottom)
        if (showEdgeGradients) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(canvasBg, canvasBg.copy(alpha = 0.7f), Color.Transparent)
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, canvasBg.copy(alpha = 0.75f), canvasBg)
                        )
                    )
            )
        }
    }
}

/**
 * Authentic Opera Butterfly Hero Mascot & Typography Animation.
 *
 * Faithfully ports Opera's `ButterflyCluster.tsx` and `WelcomeText`:
 * 1. Intro Reveal: Curves smoothly upwards (translateY: 55dp -> 0dp, scale: 0.38 -> 1.0, alpha: 0 -> 1)
 *    using Opera's exact cubic bezier curve: (0.16, 1.0, 0.3, 1.0).
 * 2. Continuous Ambient Floating: Gentle sine-wave hover & organic breathing pulse.
 * 3. Sensor Parallax / Gyroscope Tilt: Subtle organic tilt with spring damping (decay = 0.92, maxShift = 16).
 * 4. Dual-tone Layered Rendering: Base layer + OperaRed tinted Accent layer.
 * 5. Animated Welcome Headline & Tagline: Timed fade-up right as the butterfly lands.
 */
@Composable
fun OperaButterflyHero(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    incognito: Boolean = false,
    title: String = "How can I help you today?",
    subtitle: String = "AI for all • Privacy for freedom • Ask Maestro anything"
) {
    val context = LocalContext.current

    // Intro Ascent Animation
    val introAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        introAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1100,
                delayMillis = 120,
                easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
            )
        )
    }

    // Welcome Text Delayed Fade-Up Animation (fades in right after the butterfly lands)
    val textIntroAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(850)
        textIntroAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 550,
                easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
            )
        )
    }

    // Continuous Ambient Floating Hover & Breathing Scale
    val infiniteTransition = rememberInfiniteTransition(label = "butterfly_ambient")
    val hoverOffset by infiniteTransition.animateFloat(
        initialValue = -5.5f,
        targetValue = 5.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "butterfly_hover"
    )

    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.018f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "butterfly_breathing"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "butterfly_glow"
    )

    // Sensor Parallax (Accelerometer/Tilt) with graceful fallback
    var sensorTiltX by remember { mutableFloatStateOf(0f) }
    var sensorTiltY by remember { mutableFloatStateOf(0f) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            private var lastX = 0f
            private var lastY = 0f
            private val decay = 0.92f

            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                // Accelerometer: X tilts left/right, Y tilts forward/backward
                val rawX = -event.values[0]
                val rawY = event.values[1]

                lastX = lastX * decay + rawX * (1f - decay)
                lastY = lastY * decay + rawY * (1f - decay)

                // Clamp to prevent detachment (-4deg..4deg tilt, -10dp..10dp shift)
                sensorTiltX = (lastX * 1.5f).coerceIn(-4f, 4f)
                sensorTiltY = (lastY * 1.2f).coerceIn(-6f, 6f)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (sensorManager != null && sensor != null) {
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    // Calculated Intro & Motion Transforms
    val p = introAnim.value
    val introTranslateY = (1f - p) * 55f
    val introScale = 0.38f + p * 0.62f
    val introAlpha = (p / 0.35f).coerceIn(0f, 1f)

    val combinedTranslateY = (introTranslateY + hoverOffset + sensorTiltY).dp
    val combinedTranslateX = (sensorTiltX * 1.5f).dp
    val combinedScale = introScale * breathingScale
    val combinedRotate = sensorTiltX

    val accentTint = if (incognito) OperaIncognito else OperaRed
    val textColor = if (isDarkTheme) OperaDarkTextPrimary else OperaTextPrimary
    val subtitleColor = if (isDarkTheme) OperaDarkTextMuted else OperaTextMuted

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Butterfly Cluster Container
        Box(
            modifier = Modifier
                .padding(bottom = 22.dp)
                .size(108.dp)
                .offset(x = combinedTranslateX, y = combinedTranslateY)
                .scale(combinedScale)
                .rotate(combinedRotate)
                .alpha(introAlpha),
            contentAlignment = Alignment.Center
        ) {
            // Ambient Backlight Radial Aura
            Image(
                painter = painterResource(id = R.drawable.opera_logo_glow),
                contentDescription = null,
                modifier = Modifier
                    .size(128.dp)
                    .alpha(glowAlpha)
            )

            // Layer 1: Butterfly Base Layer (structural white/dark elements)
            Image(
                painter = painterResource(id = R.drawable.opera_butterfly_base),
                contentDescription = "Opera Butterfly Base",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(92.dp)
            )

            // Layer 2: Butterfly Accent Layer (Opera Red interaction layer)
            Image(
                painter = painterResource(id = R.drawable.opera_butterfly_accent),
                contentDescription = "Opera Butterfly Accent",
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(accentTint),
                modifier = Modifier.size(92.dp)
            )
        }

        // Welcome Text with Delayed Smooth Editorial Fade-Up
        val textP = textIntroAnim.value
        val textTranslateY = ((1f - textP) * 14f).dp
        val textAlpha = textP

        Column(
            modifier = Modifier
                .offset(y = textTranslateY)
                .alpha(textAlpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontFamily = PetronaFontFamily,
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = subtitle,
                fontFamily = FigtreeFontFamily,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Normal,
                color = subtitleColor.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}
