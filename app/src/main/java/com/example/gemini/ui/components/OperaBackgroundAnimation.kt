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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
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
 * Authentic Manus AI 3D Particle Dot Wave Grid Canvas Background.
 *
 * Implements the signature Manus AI interactive undulating dot-matrix wave:
 * 1. Warm clean canvas tone (#FEFBF6 in light mode, #0F0F0E in dark mode).
 * 2. Harmonic 3D fluid wave mathematics (dual-frequency sine & cosine ripples).
 * 3. Perspective 3D spatial projection with dynamic depth foreshortening.
 * 4. Dynamic dot size and opacity modulation on wave crests and troughs.
 * 5. Smooth radial vignette and edge dissolve (matching Manus radial-gradient masking).
 * 6. Sensor tilt parallax for interactive physical depth.
 * 7. Zero memory allocation inside DrawScope for pure 60fps/120fps fluid performance.
 */
@Composable
fun OperaAmbientBackground(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    showEdgeGradients: Boolean = true,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val context = LocalContext.current
    val canvasBg = if (isDarkTheme) OperaDarkBackground else OperaCream

    // 1. Accelerometer Sensor Parallax for interactive 3D depth (zero-recomposition smoothed array)
    val sensorTilt = remember { FloatArray(2) { 0f } }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val listener = object : SensorEventListener {
            private var lastX = 0f
            private var lastY = 0f
            private val decay = 0.94f
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                val rawX = -event.values[0]
                val rawY = event.values[1]
                lastX = lastX * decay + rawX * (1f - decay)
                lastY = lastY * decay + rawY * (1f - decay)
                sensorTilt[0] = (lastX * 1.2f).coerceIn(-5f, 5f)
                sensorTilt[1] = (lastY * 1.0f).coerceIn(-6f, 6f)
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        onDispose { sensorManager?.unregisterListener(listener) }
    }

    // 2. Continuous Fluid Wave Clock (Silky Smooth 60fps/120fps Engine)
    val infiniteTransition = rememberInfiniteTransition(label = "manus_dot_wave_engine")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (20f * PI * 2f).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 80000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dot_wave_time"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(canvasBg)
            .drawBehind {
                val w = size.width
                val h = size.height
                if (w <= 0f || h <= 0f) return@drawBehind

                val cx = w / 2f
                val cy = h * 0.44f

                val tiltX = (sensorTilt[0] / 5f).coerceIn(-1.0f, 1.0f)
                val tiltY = (sensorTilt[1] / 5f).coerceIn(-1.0f, 1.0f)

                // Dot grid spacing (~22dp on screen)
                val step = 22.dp.toPx()
                val cols = (w / step).toInt() + 4
                val rows = (h / step).toInt() + 4

                val baseRadius = 1.35.dp.toPx()
                val maxRadius = 3.4.dp.toPx()
                val minRadius = 0.75.dp.toPx()

                val waveAmp = 10.dp.toPx()
                val tiltShiftX = tiltX * 18.dp.toPx()
                val tiltShiftY = tiltY * 18.dp.toPx()

                // Base dot colors
                val dotBaseColor = if (isDarkTheme) {
                    Color(0xFFE8EAEE)
                } else {
                    Color(0xFF23252A)
                }

                // Wave crest accent color (subtle Opera Red warmth on highest peaks)
                val crestColor = OperaRed

                val maxDistX = w * 0.54f
                val maxDistY = h * 0.54f

                for (col in -2..cols) {
                    val gx = col * step
                    for (row in -2..rows) {
                        val gy = row * step

                        val dx = gx - cx
                        val dy = gy - cy
                        val distSq = dx * dx + dy * dy
                        val dist = kotlin.math.sqrt(distSq)

                        // 3D Harmonic Fluid Wave math:
                        // w1: Concentric expanding ripples
                        // w2: Soft diagonal rolling fluid
                        // w3: Gentle vertical wave breathing
                        val w1 = sin(dist * 0.0075f - time * 2.2f)
                        val w2 = cos(gx * 0.0055f + gy * 0.0055f - time * 1.5f)
                        val w3 = sin(gy * 0.0065f - time * 1.8f)
                        val wave = w1 * 0.52f + w2 * 0.32f + w3 * 0.16f // -1f .. +1f

                        val distSafe = if (dist < 1f) 1f else dist
                        val px = gx + (dx / distSafe) * (wave * 5.dp.toPx()) + tiltShiftX
                        val py = gy + (dy / distSafe) * (wave * 5.dp.toPx()) - (wave * waveAmp) + tiltShiftY

                        // Screen edge vignette fade (smooth cubic falloff matching Manus)
                        val edgeX = (kotlin.math.abs(px - cx) / maxDistX).coerceIn(0f, 1f)
                        val edgeY = (kotlin.math.abs(py - cy) / maxDistY).coerceIn(0f, 1f)
                        val vignette = (1f - edgeX * edgeX * edgeX) * (1f - edgeY * edgeY * edgeY)

                        if (px in 0f..w && py in 0f..h && vignette > 0.02f) {
                            val normWave = ((wave + 1f) * 0.5f).coerceIn(0f, 1f) // 0..1
                            val radius = (baseRadius * (0.85f + 0.65f * normWave)).coerceIn(minRadius, maxRadius)

                            val baseAlpha = if (isDarkTheme) 0.09f else 0.10f
                            val maxWaveAlpha = if (isDarkTheme) 0.22f else 0.18f
                            val alpha = ((baseAlpha + maxWaveAlpha * normWave) * vignette).coerceIn(0.015f, 0.35f)

                            val finalColor = if (normWave > 0.70f) {
                                // Subtle touch of Opera Red on wave crests
                                val blend = ((normWave - 0.70f) / 0.30f) * 0.25f
                                lerp(dotBaseColor, crestColor, blend).copy(alpha = alpha)
                            } else {
                                dotBaseColor.copy(alpha = alpha)
                            }

                            drawCircle(
                                color = finalColor,
                                radius = radius,
                                center = Offset(px, py)
                            )
                        }
                    }
                }
            }
    ) {
        // Main screen content layer
        content()

        // Top & Bottom Edge Gradient Fades for smooth scrolling under bars
        if (showEdgeGradients) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(canvasBg, canvasBg.copy(alpha = 0.65f), Color.Transparent)
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
                            colors = listOf(Color.Transparent, canvasBg.copy(alpha = 0.65f), canvasBg)
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
