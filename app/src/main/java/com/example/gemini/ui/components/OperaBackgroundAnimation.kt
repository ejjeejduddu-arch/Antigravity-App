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
 * 3. 3D Floating Particle Starfield with perspective projection (matching web index).
 * 4. 3D Rotating Geodesic Wireframe Spheres with interactive gyroscope parallax.
 * 5. Multi-layer undulating harmonic energy wave ribbons.
 * 6. Optional top and bottom edge fade overlays for seamless message scroll clearance.
 */
private data class OperaParticle3D(
    val x: Float,
    val y: Float,
    val initialZ: Float,
    val size: Float,
    val alpha: Float,
    val speed: Float
)

@Composable
fun OperaAmbientBackground(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    showEdgeGradients: Boolean = true,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val context = LocalContext.current
    val canvasBg = if (isDarkTheme) OperaDarkBackground else OperaCream
    val textureAlpha = if (isDarkTheme) 0.04f else 0.03f

    // 1. Accelerometer Sensor Parallax for interactive 3D depth
    var sensorTiltX by remember { mutableFloatStateOf(0f) }
    var sensorTiltY by remember { mutableFloatStateOf(0f) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val listener = object : SensorEventListener {
            private var lastX = 0f
            private var lastY = 0f
            private val decay = 0.90f
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                val rawX = -event.values[0]
                val rawY = event.values[1]
                lastX = lastX * decay + rawX * (1f - decay)
                lastY = lastY * decay + rawY * (1f - decay)
                sensorTiltX = (lastX * 1.5f).coerceIn(-6f, 6f)
                sensorTiltY = (lastY * 1.2f).coerceIn(-8f, 8f)
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME) }
        onDispose { sensorManager?.unregisterListener(listener) }
    }

    // 2. Normalized Tilt Factors for 3D Parallax Perspective
    val tiltX = (sensorTiltX / 5f).coerceIn(-1.2f, 1.2f)
    val tiltY = (sensorTiltY / 5f).coerceIn(-1.2f, 1.2f)

    // 3. 3D Particle Starfield (65 particles matching web index app.js)
    val particles = remember {
        val random = kotlin.random.Random(1337)
        List(65) {
            OperaParticle3D(
                x = (random.nextFloat() - 0.5f) * 2000f,
                y = (random.nextFloat() - 0.5f) * 2000f,
                initialZ = random.nextFloat() * 950f + 50f,
                size = random.nextFloat() * 2f + 1f,
                alpha = random.nextFloat() * 0.7f + 0.2f,
                speed = random.nextFloat() * 0.4f + 0.8f
            )
        }
    }

    // 4. 3D Continuous Infinite Animation Clock (Smooth 60fps Play Engine)
    val infiniteTransition = rememberInfiniteTransition(label = "opera_3d_canvas_engine")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (20f * PI * 2f).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 140000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "canvas_time"
    )

    // Reusable Path instances for zero-allocation rendering per frame
    val spherePath = remember { Path() }
    val wavePath = remember { Path() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(canvasBg)
            .drawBehind {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val cy = h / 2f
                val fov = 400f

                // ==========================================
                // LAYER 1: 3D FLOATING PARTICLES (PROJECTED 3D DEPTH)
                // ==========================================
                val particleColor = OperaRed
                val alphaMultiplier = if (isDarkTheme) 0.60f else 0.45f

                for (p in particles) {
                    val z = 1000f - ((p.initialZ + time * 72f * p.speed) % 990f)
                    val scale = fov / z
                    val px = (p.x + tiltX * 200f) * scale + cx
                    val py = (p.y + tiltY * 200f) * scale + cy

                    if (px in 0f..w && py in 0f..h) {
                        val radius = (p.size * scale).coerceIn(0.75f, 6.dp.toPx())
                        drawCircle(
                            color = particleColor.copy(alpha = (p.alpha * alphaMultiplier).coerceIn(0.04f, 1f)),
                            radius = radius,
                            center = Offset(px, py)
                        )
                    }
                }

                // ==========================================
                // LAYER 2: 3D ROTATING GEODESIC WIREFRAME SPHERES
                // ==========================================
                // Primary Geodesic Sphere (Upper-Right)
                val sphere1Radius = (w * 0.32f).coerceIn(120.dp.toPx(), 220.dp.toPx())
                drawWireframeSphere(
                    centerX = cx + w * 0.28f + (sensorTiltX * 4f),
                    centerY = cy - h * 0.05f + (sensorTiltY * 4f),
                    radius = sphere1Radius,
                    t = time,
                    tiltX = tiltX,
                    tiltY = tiltY,
                    isDark = isDarkTheme,
                    path = spherePath
                )

                // Secondary Counter-Orbiting Sphere (Lower-Left)
                val sphere2Radius = (w * 0.22f).coerceIn(85.dp.toPx(), 160.dp.toPx())
                drawWireframeSphere(
                    centerX = cx - w * 0.32f - (sensorTiltX * 4f),
                    centerY = cy + h * 0.22f - (sensorTiltY * 4f),
                    radius = sphere2Radius,
                    t = time * 1.3f,
                    tiltX = -tiltX,
                    tiltY = -tiltY,
                    isDark = isDarkTheme,
                    path = spherePath
                )

                // ==========================================
                // LAYER 3: 3D UNDULATING HARMONIC ENERGY WAVES
                // ==========================================
                drawEnergyWaves(
                    w = w,
                    h = h,
                    t = time,
                    isDark = isDarkTheme,
                    path = wavePath
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

        // Subtle soft ambient wash (preserves 3D visibility while maintaining contrast)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to canvasBg.copy(alpha = 0.45f),
                            0.30f to Color.Transparent,
                            0.70f to Color.Transparent,
                            1.00f to canvasBg.copy(alpha = 0.50f)
                        )
                    )
                )
        )

        // Main screen content layer
        content()

        // Top & Bottom Edge Gradient Fades
        if (showEdgeGradients) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(canvasBg, canvasBg.copy(alpha = 0.6f), Color.Transparent)
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
                            colors = listOf(Color.Transparent, canvasBg.copy(alpha = 0.6f), canvasBg)
                        )
                    )
            )
        }
    }
}

/**
 * Draws a 3D rotating geodesic wireframe sphere with latitude & longitude rings,
 * projected and rotated in 3D space with subtle ambient atmospheric backlighting.
 */
private fun DrawScope.drawWireframeSphere(
    centerX: Float,
    centerY: Float,
    radius: Float,
    t: Float,
    tiltX: Float,
    tiltY: Float,
    isDark: Boolean,
    path: Path
) {
    // 1. Soft atmospheric luminescence glow behind the wireframe sphere
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                OperaRed.copy(alpha = if (isDark) 0.16f else 0.10f),
                Color.Transparent
            ),
            center = Offset(centerX, centerY),
            radius = radius * 1.5f
        ),
        center = Offset(centerX, centerY),
        radius = radius * 1.5f
    )

    val rings = 8
    val rotX = t * 0.5f + tiltY * 0.5f
    val rotY = t * 0.8f + tiltX * 0.8f
    val cosRotX = cos(rotX)
    val sinRotX = sin(rotX)
    val cosRotY = cos(rotY)
    val sinRotY = sin(rotY)

    val strokeColor = if (isDark) {
        OperaRed.copy(alpha = 0.24f)
    } else {
        OperaRed.copy(alpha = 0.18f)
    }
    val strokeWidth = 1.2.dp.toPx()

    // 2. 3D Latitude rings
    for (i in 0 until rings) {
        val angle = (i.toFloat() / rings.toFloat()) * PI.toFloat()
        val r = radius * sin(angle)
        val y = radius * cos(angle)

        path.reset()
        for (j in 0..36) {
            val phi = (j.toFloat() / 36f) * (2f * PI.toFloat())
            val px = r * cos(phi)
            val py = y
            val pz = r * sin(phi)

            // Rotate X
            val y1 = py * cosRotX - pz * sinRotX
            val z1 = py * sinRotX + pz * cosRotX

            // Rotate Y
            val x2 = px * cosRotY + z1 * sinRotY

            val scrX = centerX + x2
            val scrY = centerY + y1

            if (j == 0) {
                path.moveTo(scrX, scrY)
            } else {
                path.lineTo(scrX, scrY)
            }
        }
        path.close()
        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(width = strokeWidth)
        )
    }

    // 3. 3D Longitudinal meridian rings (adds authentic 3D wireframe depth)
    val meridianCount = 4
    for (m in 0 until meridianCount) {
        val mAngle = (m.toFloat() / meridianCount.toFloat()) * PI.toFloat()
        val cosMAngle = cos(mAngle)
        val sinMAngle = sin(mAngle)

        path.reset()
        for (j in 0..36) {
            val theta = (j.toFloat() / 36f) * (2f * PI.toFloat())
            val px = radius * sin(theta) * cosMAngle
            val py = radius * cos(theta)
            val pz = radius * sin(theta) * sinMAngle

            // Rotate X
            val y1 = py * cosRotX - pz * sinRotX
            val z1 = py * sinRotX + pz * cosRotX

            // Rotate Y
            val x2 = px * cosRotY + z1 * sinRotY

            val scrX = centerX + x2
            val scrY = centerY + y1

            if (j == 0) {
                path.moveTo(scrX, scrY)
            } else {
                path.lineTo(scrX, scrY)
            }
        }
        path.close()
        drawPath(
            path = path,
            color = strokeColor.copy(alpha = strokeColor.alpha * 0.85f),
            style = Stroke(width = strokeWidth * 0.9f)
        )
    }
}

/**
 * Draws 3 undulating harmonic wave ribbons across the lower canvas,
 * matching web app.js drawEnergyWaves.
 */
private fun DrawScope.drawEnergyWaves(
    w: Float,
    h: Float,
    t: Float,
    isDark: Boolean,
    path: Path
) {
    val waveCount = 3
    val step = (w / 32f).coerceAtLeast(18.dp.toPx())
    for (i in 0 until waveCount) {
        val waveOffset = (i.toFloat() * PI.toFloat()) / 3f
        val baseY = h * 0.72f + i * 28.dp.toPx()

        path.reset()
        var x = 0f
        var first = true
        while (x <= w + step) {
            val y = baseY +
                    sin(x * 0.003f + t * 1.2f + waveOffset) * 36.dp.toPx() +
                    cos(x * 0.006f - t * 0.8f) * 20.dp.toPx()
            if (first) {
                path.moveTo(x, y)
                first = false
            } else {
                path.lineTo(x, y)
            }
            x += step
        }

        val alpha = if (isDark) {
            (0.20f - i * 0.04f).coerceAtLeast(0.06f)
        } else {
            (0.14f - i * 0.03f).coerceAtLeast(0.04f)
        }

        drawPath(
            path = path,
            color = OperaRed.copy(alpha = alpha),
            style = Stroke(
                width = 2.4.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
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
