package com.example.gemini.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gemini.TermuxActivity
import com.example.gemini.data.local.BootstrapSource
import com.example.gemini.data.local.LocalEnvironmentManager
import com.example.gemini.data.local.LocalInstallerState
import com.example.gemini.data.preferences.AuthPreferences
import com.example.gemini.theme.*
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenLocalToolsInstaller(
    authPreferences: AuthPreferences,
    onSkip: () -> Unit,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val installerState by LocalEnvironmentManager.installerState.collectAsState()
    val installerLogs by LocalEnvironmentManager.installerLogs.collectAsState()

    val isInstalling = installerState is LocalInstallerState.Downloading ||
        installerState is LocalInstallerState.Extracting ||
        installerState is LocalInstallerState.Configuring ||
        installerState is LocalInstallerState.Verifying

    val activity = (context as? android.app.Activity) ?: ((context as? android.content.ContextWrapper)?.baseContext as? android.app.Activity)
    DisposableEffect(isInstalling) {
        val window = activity?.window
        if (isInstalling) {
            window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    var selectedSourceType by remember { mutableStateOf(0) } // 0: Auto GitHub, 1: Direct URL, 2: Local ZIP File
    var directUrlText by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var showLogs by remember { mutableStateOf(false) }

    val zipPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            selectedSourceType = 2
        }
    }

    val logsListState = rememberLazyListState()
    LaunchedEffect(installerLogs.size) {
        if (installerLogs.isNotEmpty()) {
            logsListState.animateScrollToItem(installerLogs.size - 1)
        }
    }

    // Advanced Entrance and Interactive Animation States
    var isScreenEntered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isScreenEntered = true
    }

    val opt1Scale = remember { Animatable(1f) }
    val opt2Scale = remember { Animatable(1f) }
    val opt3Scale = remember { Animatable(1f) }

    val heroScale by animateFloatAsState(
        targetValue = if (isScreenEntered) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "heroScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "heroPulse")
    val heroPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroPulseAlpha"
    )

    val headerAlpha by animateFloatAsState(
        targetValue = if (isScreenEntered) 1f else 0f,
        animationSpec = tween(400, delayMillis = 60),
        label = "headerAlpha"
    )
    val headerOffsetY by animateFloatAsState(
        targetValue = if (isScreenEntered) 0f else -20f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "headerOffsetY"
    )

    val hardwareAlpha by animateFloatAsState(
        targetValue = if (isScreenEntered) 1f else 0f,
        animationSpec = tween(400, delayMillis = 120),
        label = "hardwareAlpha"
    )
    val hardwareOffsetY by animateFloatAsState(
        targetValue = if (isScreenEntered) 0f else 25f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "hardwareOffsetY"
    )

    val opt1Alpha by animateFloatAsState(
        targetValue = if (isScreenEntered) 1f else 0f,
        animationSpec = tween(350, delayMillis = 180),
        label = "opt1Alpha"
    )
    val opt1OffsetY by animateFloatAsState(
        targetValue = if (isScreenEntered) 0f else 30f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "opt1OffsetY"
    )

    val opt2Alpha by animateFloatAsState(
        targetValue = if (isScreenEntered) 1f else 0f,
        animationSpec = tween(350, delayMillis = 260),
        label = "opt2Alpha"
    )
    val opt2OffsetY by animateFloatAsState(
        targetValue = if (isScreenEntered) 0f else 30f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "opt2OffsetY"
    )

    val opt3Alpha by animateFloatAsState(
        targetValue = if (isScreenEntered) 1f else 0f,
        animationSpec = tween(350, delayMillis = 340),
        label = "opt3Alpha"
    )
    val opt3OffsetY by animateFloatAsState(
        targetValue = if (isScreenEntered) 0f else 30f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "opt3OffsetY"
    )

    val buttonsAlpha by animateFloatAsState(
        targetValue = if (isScreenEntered) 1f else 0f,
        animationSpec = tween(400, delayMillis = 400),
        label = "buttonsAlpha"
    )
    val buttonsOffsetY by animateFloatAsState(
        targetValue = if (isScreenEntered) 0f else 25f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "buttonsOffsetY"
    )

    val opt1BorderColor by animateColorAsState(
        targetValue = if (selectedSourceType == 0) ClaudeTerracotta else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
        animationSpec = tween(220),
        label = "opt1BorderColor"
    )
    val opt1BorderWidth by animateDpAsState(
        targetValue = if (selectedSourceType == 0) 1.5.dp else 1.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "opt1BorderWidth"
    )
    val opt1ContainerColor by animateColorAsState(
        targetValue = if (selectedSourceType == 0) ClaudeTerracotta.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        animationSpec = tween(220),
        label = "opt1ContainerColor"
    )

    val opt2BorderColor by animateColorAsState(
        targetValue = if (selectedSourceType == 2) ClaudeTerracotta else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
        animationSpec = tween(220),
        label = "opt2BorderColor"
    )
    val opt2BorderWidth by animateDpAsState(
        targetValue = if (selectedSourceType == 2) 1.5.dp else 1.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "opt2BorderWidth"
    )
    val opt2ContainerColor by animateColorAsState(
        targetValue = if (selectedSourceType == 2) ClaudeTerracotta.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        animationSpec = tween(220),
        label = "opt2ContainerColor"
    )

    val opt3BorderColor by animateColorAsState(
        targetValue = if (selectedSourceType == 1) ClaudeTerracotta else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
        animationSpec = tween(220),
        label = "opt3BorderColor"
    )
    val opt3BorderWidth by animateDpAsState(
        targetValue = if (selectedSourceType == 1) 1.5.dp else 1.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "opt3BorderWidth"
    )
    val opt3ContainerColor by animateColorAsState(
        targetValue = if (selectedSourceType == 1) ClaudeTerracotta.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        animationSpec = tween(220),
        label = "opt3ContainerColor"
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Antigravity Environment",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Local Linux & Terminal Setup",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (installerState !is LocalInstallerState.Downloading && installerState !is LocalInstallerState.Extracting) {
                        TextButton(
                            onClick = onSkip,
                            colors = ButtonDefaults.textButtonColors(contentColor = ClaudeTerracotta)
                        ) {
                            Text("Skip for Now", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val screenWidth = maxWidth
            val isCompact = screenWidth < 360.dp
            val isTablet = screenWidth >= 600.dp
            val horizontalPadding = if (isCompact) 12.dp else if (isTablet) 24.dp else 16.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp)
                        .padding(horizontal = horizontalPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Hero Icon with Gradient Glow and Animated Entrance & Pulse
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = heroScale
                                scaleY = heroScale
                            }
                            .size(if (isCompact) 74.dp else 84.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer pulsating halo ring
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(
                                    when (installerState) {
                                        is LocalInstallerState.Success -> QuotaGreen.copy(alpha = heroPulseAlpha)
                                        is LocalInstallerState.Error -> Color.Red.copy(alpha = heroPulseAlpha)
                                        else -> ClaudeTerracotta.copy(alpha = heroPulseAlpha)
                                    }
                                )
                        )

                        // Inner icon container
                        Box(
                            modifier = Modifier
                                .size(if (isCompact) 62.dp else 70.dp)
                                .clip(CircleShape)
                                .background(
                                    when (installerState) {
                                        is LocalInstallerState.Success -> QuotaGreen.copy(alpha = 0.2f)
                                        is LocalInstallerState.Error -> Color.Red.copy(alpha = 0.2f)
                                        else -> ClaudeTerracotta.copy(alpha = 0.2f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (installerState) {
                                    is LocalInstallerState.Success -> Icons.Default.CheckCircle
                                    is LocalInstallerState.Error -> Icons.Default.ErrorOutline
                                    else -> Icons.Outlined.Terminal
                                },
                                contentDescription = null,
                                tint = when (installerState) {
                                    is LocalInstallerState.Success -> QuotaGreen
                                    is LocalInstallerState.Error -> Color.Red
                                    else -> ClaudeTerracotta
                                },
                                modifier = Modifier.size(if (isCompact) 36.dp else 42.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val isTermux = remember { LocalEnvironmentManager.isTermuxPackage(context) }

                    // Header Text with Staggered Fade & Slide Down
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                alpha = headerAlpha
                                translationY = headerOffsetY
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when (installerState) {
                                is LocalInstallerState.Success -> if (isTermux) "Local Linux Tools Ready!" else "Ubuntu Container Ready!"
                                is LocalInstallerState.Error -> "Setup Encountered an Issue"
                                is LocalInstallerState.Idle -> if (isTermux) "Initialize Linux Environment" else "Install Ubuntu Container (ARM64)"
                                is LocalInstallerState.Discovering -> if (isTermux) "Locating Latest Bootstrap..." else "Locating Ubuntu Rootfs..."
                                is LocalInstallerState.AwaitingConfirmation -> if (isTermux) "Confirm Bootstrap Installation" else "Confirm Ubuntu Installation"
                                is LocalInstallerState.Downloading -> "Downloading Toolchain..."
                                is LocalInstallerState.Extracting -> "Extracting & Linking Files..."
                                is LocalInstallerState.Configuring -> "Finalizing Permissions..."
                                is LocalInstallerState.Verifying -> "Verifying Environment..."
                            },
                            fontSize = if (isCompact) 18.sp else 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Text(
                            text = when (installerState) {
                                is LocalInstallerState.Success -> if (isTermux) "Termux-compatible shell, coreutils & AGY tools are installed and ready." else "Ubuntu ARM64 container, bash, glibc & AGY tools are ready."
                                is LocalInstallerState.Error -> "Failed to unpack rootfs. You can retry or pick a local ZIP archive."
                                is LocalInstallerState.Idle -> if (isTermux) "Select how you would like to set up the local Termux environment for Antigravity:" else "Select how you would like to set up the Ubuntu Linux container for Antigravity:"
                                is LocalInstallerState.Discovering -> "Checking releases for the latest verified rootfs archive."
                                is LocalInstallerState.AwaitingConfirmation -> "Review details below and confirm to begin installation."
                                is LocalInstallerState.Downloading -> "Fetching rootfs archive into secure application storage."
                                is LocalInstallerState.Extracting -> "Decompressing packages, setting Unix permissions, and mapping symlinks."
                                is LocalInstallerState.Configuring -> "Configuring startup scripts, environment variables, and shell binaries."
                                is LocalInstallerState.Verifying -> "Running verification checks on installed binaries and symlinks."
                            },
                            fontSize = if (isCompact) 12.sp else 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp, bottom = 16.dp),
                            lineHeight = if (isCompact) 16.sp else 18.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    // Device Architecture & Hardware Capabilities Card with Fade & Slide Up
                    val hardwareInfo = remember { LocalEnvironmentManager.getDeviceHardwareInfo() }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                alpha = hardwareAlpha
                                translationY = hardwareOffsetY
                            }
                            .padding(bottom = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(if (isCompact) 10.dp else 14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Memory,
                                    contentDescription = null,
                                    modifier = Modifier.size(if (isCompact) 14.dp else 16.dp),
                                    tint = ClaudeTerracotta
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Device Hardware Capabilities",
                                    fontSize = if (isCompact) 11.5.sp else 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(if (isCompact) 5.dp else 8.dp)
                            ) {
                                // CPU Architecture Pill
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                        .padding(vertical = if (isCompact) 4.dp else 6.dp, horizontal = if (isCompact) 3.dp else 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "CPU Arch",
                                            fontSize = if (isCompact) 8.5.sp else 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            hardwareInfo.arch,
                                            fontSize = if (isCompact) 10.5.sp else 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = ClaudeTerracotta,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                // Virtual Address Space Pill
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when (hardwareInfo.vaBits) {
                                                48 -> QuotaGreen.copy(alpha = 0.12f)
                                                39 -> Color(0xFFF59E0B).copy(alpha = 0.12f)
                                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                            }
                                        )
                                        .padding(vertical = if (isCompact) 4.dp else 6.dp, horizontal = if (isCompact) 3.dp else 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "Address Space",
                                            fontSize = if (isCompact) 8.5.sp else 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            when (hardwareInfo.vaBits) {
                                                48 -> if (isCompact) "48-bit" else "48-bit (VA48)"
                                                39 -> if (isCompact) "39-bit" else "39-bit (VA39)"
                                                else -> "N/A"
                                            },
                                            fontSize = if (isCompact) 10.sp else 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (hardwareInfo.vaBits) {
                                                48 -> QuotaGreen
                                                39 -> Color(0xFFF59E0B)
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                // LSE Atomics Pill
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when (hardwareInfo.hasAtomics) {
                                                true -> QuotaGreen.copy(alpha = 0.12f)
                                                false -> Color(0xFFEF4444).copy(alpha = 0.12f)
                                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                            }
                                        )
                                        .padding(vertical = if (isCompact) 4.dp else 6.dp, horizontal = if (isCompact) 3.dp else 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "LSE Atomics",
                                            fontSize = if (isCompact) 8.5.sp else 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            when (hardwareInfo.hasAtomics) {
                                                true -> "Supported"
                                                false -> if (isCompact) "Not Supp." else "Not Supported"
                                                else -> "N/A"
                                            },
                                            fontSize = if (isCompact) 10.sp else 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (hardwareInfo.hasAtomics) {
                                                true -> QuotaGreen
                                                false -> Color(0xFFEF4444)
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Dynamic Step / Configuration Content
                    when (val state = installerState) {
                        is LocalInstallerState.Idle -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Option 1: Auto Download (GitHub)
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = opt1ContainerColor,
                                    border = BorderStroke(opt1BorderWidth, opt1BorderColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            alpha = opt1Alpha
                                            translationY = opt1OffsetY
                                            scaleX = opt1Scale.value
                                            scaleY = opt1Scale.value
                                        }
                                        .clickable {
                                            scope.launch {
                                                opt1Scale.animateTo(0.97f, animationSpec = tween(50))
                                                opt1Scale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                            }
                                            selectedSourceType = 0
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(if (isCompact) 10.dp else 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selectedSourceType == 0,
                                            onClick = {
                                                scope.launch {
                                                    opt1Scale.animateTo(0.97f, animationSpec = tween(50))
                                                    opt1Scale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                                }
                                                selectedSourceType = 0
                                            },
                                            colors = RadioButtonDefaults.colors(selectedColor = ClaudeTerracotta),
                                            modifier = if (isCompact) Modifier.size(28.dp) else Modifier
                                        )
                                        Spacer(modifier = Modifier.width(if (isCompact) 6.dp else 8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = if (isTermux) "Auto Download from GitHub" else "Ubuntu ARM64 Base (Light)",
                                                    fontSize = if (isCompact) 13.sp else 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(QuotaGreen.copy(alpha = 0.15f))
                                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        if (isTermux) "Official" else "Recommended",
                                                        fontSize = if (isCompact) 8.5.sp else 9.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = QuotaGreen
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (isTermux) {
                                                    "Downloads verified Termux rootfs (~350MB) with preconfigured tools and environment."
                                                } else {
                                                    "Downloads official minimal Ubuntu ARM64 rootfs container with glibc, bash, and development tools."
                                                },
                                                fontSize = if (isCompact) 11.sp else 12.sp,
                                                lineHeight = if (isCompact) 15.sp else 16.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                // Option 2: Local ZIP File from Storage
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = opt2ContainerColor,
                                    border = BorderStroke(opt2BorderWidth, opt2BorderColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            alpha = opt2Alpha
                                            translationY = opt2OffsetY
                                            scaleX = opt2Scale.value
                                            scaleY = opt2Scale.value
                                        }
                                        .clickable {
                                            scope.launch {
                                                opt2Scale.animateTo(0.97f, animationSpec = tween(50))
                                                opt2Scale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                            }
                                            selectedSourceType = 2
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(if (isCompact) 10.dp else 14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = selectedSourceType == 2,
                                                onClick = {
                                                    scope.launch {
                                                        opt2Scale.animateTo(0.97f, animationSpec = tween(50))
                                                        opt2Scale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                                    }
                                                    selectedSourceType = 2
                                                },
                                                colors = RadioButtonDefaults.colors(selectedColor = ClaudeTerracotta),
                                                modifier = if (isCompact) Modifier.size(28.dp) else Modifier
                                            )
                                            Spacer(modifier = Modifier.width(if (isCompact) 6.dp else 8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = if (isTermux) "Select Local ZIP Archive" else "Custom Ubuntu Rootfs (.zip)",
                                                    fontSize = if (isCompact) 13.sp else 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = if (isTermux) {
                                                        "Pick an existing bootstrap backup or custom rootfs from phone storage."
                                                    } else {
                                                        "Pick an existing container backup or custom rootfs (bootstraps.zip) from phone storage."
                                                    },
                                                    fontSize = if (isCompact) 11.sp else 12.sp,
                                                    lineHeight = if (isCompact) 15.sp else 16.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        if (selectedSourceType == 2) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            OutlinedButton(
                                                onClick = { zipPickerLauncher.launch("*/*") },
                                                modifier = Modifier.fillMaxWidth().height(if (isCompact) 42.dp else 46.dp),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(imageVector = Icons.Outlined.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (selectedFileUri != null) "Change Selected ZIP" else "Browse & Select ZIP File",
                                                    fontSize = if (isCompact) 12.sp else 13.sp
                                                )
                                            }
                                            if (selectedFileUri != null) {
                                                Text(
                                                    text = "Selected: ${selectedFileUri.toString().substringAfterLast("/")}",
                                                    fontSize = if (isCompact) 10.sp else 11.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = QuotaGreen,
                                                    modifier = Modifier.padding(top = 6.dp),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }

                                // Option 3: Base Minimum Termux (Termux build only)
                                if (isTermux) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = opt3ContainerColor,
                                        border = BorderStroke(opt3BorderWidth, opt3BorderColor),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .graphicsLayer {
                                                alpha = opt3Alpha
                                                translationY = opt3OffsetY
                                                scaleX = opt3Scale.value
                                                scaleY = opt3Scale.value
                                            }
                                            .clickable {
                                                scope.launch {
                                                    opt3Scale.animateTo(0.97f, animationSpec = tween(50))
                                                    opt3Scale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                                }
                                                selectedSourceType = 1
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(if (isCompact) 10.dp else 14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = selectedSourceType == 1,
                                                onClick = {
                                                    scope.launch {
                                                        opt3Scale.animateTo(0.97f, animationSpec = tween(50))
                                                        opt3Scale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                                    }
                                                    selectedSourceType = 1
                                                },
                                                colors = RadioButtonDefaults.colors(selectedColor = ClaudeTerracotta),
                                                modifier = if (isCompact) Modifier.size(28.dp) else Modifier
                                            )
                                            Spacer(modifier = Modifier.width(if (isCompact) 6.dp else 8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "Base Minimum Termux",
                                                        fontSize = if (isCompact) 13.sp else 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(Color(0xFF6366F1).copy(alpha = 0.15f))
                                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                                    ) {
                                                        Text(
                                                            "Minimal (~33MB)",
                                                            fontSize = if (isCompact) 8.5.sp else 9.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF6366F1)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Downloads clean official Termux base bootstrap for ${LocalEnvironmentManager.getBootstrapArch()} with core utilities and package manager.",
                                                    fontSize = if (isCompact) 11.sp else 12.sp,
                                                    lineHeight = if (isCompact) 15.sp else 16.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                is LocalInstallerState.Discovering -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = ClaudeTerracotta,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is LocalInstallerState.AwaitingConfirmation -> {
                    val info = state.packageInfo
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Release Tag:", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(info.releaseTag, fontSize = 12.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Architecture:", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(info.arch, fontSize = 12.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = ClaudeTerracotta)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Archive Size:", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(info.sizeFormatted, fontSize = 12.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = QuotaGreen)
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                            Text(
                                text = info.url,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                    }
                }

                is LocalInstallerState.Downloading -> {
                    val downloadedStr = LocalEnvironmentManager.formatFileSize(state.bytesDownloaded)
                    val totalStr = LocalEnvironmentManager.formatFileSize(state.totalBytes)
                    val pct = (state.progressFraction * 100).toInt()

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Downloading Bootstrap", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                                Text("$pct%", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = ClaudeTerracotta)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            val animatedProgress by animateFloatAsState(
                                targetValue = state.progressFraction,
                                animationSpec = tween(300, easing = LinearEasing),
                                label = "progress"
                            )
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = ClaudeTerracotta,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("$downloadedStr / $totalStr", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(state.speedText, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                is LocalInstallerState.Extracting -> {
                    val pct = (state.progressFraction * 100).toInt()
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Unpacking & Extracting", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                                Text("$pct%", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = ClaudeTerracotta)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            val animatedProgress by animateFloatAsState(
                                targetValue = state.progressFraction,
                                animationSpec = tween(200, easing = LinearEasing),
                                label = "extract_progress"
                            )
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = ClaudeTerracotta,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = state.currentFileName,
                                fontSize = 11.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }

                is LocalInstallerState.Configuring -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Configuring Shell & Symlinks", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ClaudeTerracotta, strokeWidth = 2.dp)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { state.progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = ClaudeTerracotta,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = state.stepDescription,
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is LocalInstallerState.Verifying -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Verifying Installation", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ClaudeTerracotta, strokeWidth = 2.dp)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Running test: ${state.testName}",
                                fontSize = 11.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is LocalInstallerState.Success -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = QuotaGreen.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, QuotaGreen.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "✓ Installation Complete",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = QuotaGreen
                            )
                            Text(
                                text = "Bootstrap rootfs successfully configured. The interactive terminal, Go IDE backend, and AGY AI language server are now ready to run.",
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                is LocalInstallerState.Error -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "⚠ Error Occurred",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = state.errorMessage,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Live Terminal Log Expander
            if (installerState !is LocalInstallerState.Idle) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLogs = !showLogs },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Installation Log Output", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(
                            imageVector = if (showLogs) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (showLogs) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(
                            state = logsListState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 80.dp, max = 200.dp)
                                .background(Color(0xFF08090C), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            items(installerLogs) { logLine ->
                                Text(
                                    text = logLine,
                                    fontSize = 10.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 14.sp,
                                    color = if (logLine.contains("⚠") || logLine.contains("Fail") || logLine.contains("Error")) {
                                        Color(0xFFF87171)
                                    } else if (logLine.contains("✓") || logLine.contains("Success") || logLine.contains("ready") || logLine.contains("Extracted")) {
                                        QuotaGreen
                                    } else {
                                        Color(0xFFD1D5DB)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            when (val state = installerState) {
                is LocalInstallerState.Idle -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                alpha = buttonsAlpha
                                translationY = buttonsOffsetY
                            }
                    ) {
                        Button(
                            onClick = {
                                when (selectedSourceType) {
                                    0 -> LocalEnvironmentManager.launchInstall(context, authPreferences, BootstrapSource.Auto)
                                    1 -> LocalEnvironmentManager.launchInstall(context, authPreferences, BootstrapSource.BaseMinimum)
                                    2 -> {
                                        if (selectedFileUri != null) {
                                            val source = BootstrapSource.LocalZipUri(selectedFileUri!!)
                                            LocalEnvironmentManager.launchInstall(context, authPreferences, source)
                                        } else {
                                            zipPickerLauncher.launch("*/*")
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isCompact) 46.dp else 50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ClaudeTerracotta)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(if (isCompact) 16.dp else 18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (selectedSourceType) {
                                    0 -> if (isTermux) "Download & Install Antigravity (~350MB)" else "Download & Install Ubuntu Base"
                                    1 -> "Download Base Termux (~33MB)"
                                    else -> if (selectedFileUri != null) "Install Local ZIP" else "Choose ZIP File"
                                },
                                fontSize = if (isCompact) 13.5.sp else 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onSkip,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isCompact) 42.dp else 46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Skip Setup & Open Antigravity Chat",
                                fontSize = if (isCompact) 12.5.sp else 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                is LocalInstallerState.AwaitingConfirmation -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                alpha = buttonsAlpha
                                translationY = buttonsOffsetY
                            },
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                LocalEnvironmentManager.launchInstall(
                                    context,
                                    authPreferences,
                                    BootstrapSource.DirectUrl(state.packageInfo.url)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(if (isCompact) 44.dp else 48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ClaudeTerracotta)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Confirm & Install",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = if (isCompact) 13.sp else 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        OutlinedButton(
                            onClick = { LocalEnvironmentManager.resetState() },
                            modifier = Modifier
                                .weight(0.6f)
                                .height(if (isCompact) 44.dp else 48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Change",
                                fontSize = if (isCompact) 12.5.sp else 13.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                is LocalInstallerState.Discovering -> {
                    OutlinedButton(
                        onClick = { LocalEnvironmentManager.resetState() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isCompact) 44.dp else 48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Cancel Search",
                            fontSize = if (isCompact) 13.sp else 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                is LocalInstallerState.Downloading,
                is LocalInstallerState.Extracting,
                is LocalInstallerState.Configuring,
                is LocalInstallerState.Verifying -> {
                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isCompact) 44.dp else 48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Run in Background & Open Antigravity",
                            fontSize = if (isCompact) 12.5.sp else 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                is LocalInstallerState.Success -> {
                    Button(
                        onClick = onComplete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isCompact) 46.dp else 50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ClaudeTerracotta)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Get Started",
                            fontSize = if (isCompact) 13.5.sp else 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                is LocalInstallerState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                when (selectedSourceType) {
                                    0 -> LocalEnvironmentManager.launchInstall(context, authPreferences, BootstrapSource.Auto)
                                    1 -> LocalEnvironmentManager.launchInstall(context, authPreferences, BootstrapSource.BaseMinimum)
                                    2 -> {
                                        val source = selectedFileUri?.let { BootstrapSource.LocalZipUri(it) } ?: BootstrapSource.Auto
                                        LocalEnvironmentManager.launchInstall(context, authPreferences, source)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isCompact) 44.dp else 48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ClaudeTerracotta)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Retry Installation",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = if (isCompact) 13.sp else 14.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { LocalEnvironmentManager.resetState() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(if (isCompact) 42.dp else 44.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Change Source", fontSize = if (isCompact) 12.sp else 13.sp)
                            }

                            OutlinedButton(
                                onClick = onSkip,
                                modifier = Modifier
                                    .weight(0.7f)
                                    .height(if (isCompact) 42.dp else 44.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Skip", fontSize = if (isCompact) 12.sp else 13.sp)
                            }
                        }
                    }
                }

                else -> {
                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isCompact) 42.dp else 46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Run in Background & Open Chat",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = if (isCompact) 12.5.sp else 13.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
}
}

