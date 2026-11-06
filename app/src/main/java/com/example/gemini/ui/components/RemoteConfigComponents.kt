package com.example.gemini.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gemini.data.remote.AnnouncementConfig
import com.example.gemini.data.remote.MaintenanceConfig
import com.example.gemini.data.remote.RemoteConfigManager
import com.example.gemini.data.remote.RemoteVersionConfig
import com.example.gemini.theme.*
import kotlinx.coroutines.launch

/**
 * In-App Broadcast Announcement Banner
 * Placed seamlessly at the top of the chat screen
 */
@Composable
fun AnnouncementBanner(
    announcement: AnnouncementConfig,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!announcement.active || announcement.title.isBlank() && announcement.message.isBlank()) {
        return
    }

    val bannerColor = when (announcement.type.lowercase()) {
        "warning" -> Color(0xFFF59E0B)
        "alert" -> OperaRed
        "success" -> Color(0xFF10B981)
        else -> Color(0xFFFF2A2A) // Opera Red by default
    }

    val icon = when (announcement.type.lowercase()) {
        "warning" -> Icons.Rounded.Warning
        "alert" -> Icons.Rounded.ErrorOutline
        "success" -> Icons.Rounded.CheckCircle
        else -> Icons.Rounded.Campaign
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF141622).copy(alpha = 0.95f),
        border = BorderStroke(1.dp, bannerColor.copy(alpha = 0.35f)),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Pill
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(bannerColor.copy(alpha = 0.15f))
                    .border(1.dp, bannerColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = bannerColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                if (announcement.title.isNotBlank()) {
                    Text(
                        text = announcement.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (announcement.message.isNotBlank()) {
                    Text(
                        text = announcement.message,
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Dismiss Button
            if (announcement.dismissible) {
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Full-Screen High-Tech Opera Red Maintenance Mode
 */
@Composable
fun MaintenanceScreen(
    maintenance: MaintenanceConfig,
    onRefresh: () -> Unit,
    isRefreshing: Boolean
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A0F)),
        contentAlignment = Alignment.Center
    ) {
        // Red Ambient Glow in background
        Box(
            modifier = Modifier
                .size(320.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(OperaRed.copy(alpha = 0.22f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Shield & Gear Icon Badge
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFFF1A1A), Color(0xFFB30000))
                        )
                    )
                    .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Construction,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = maintenance.title.ifBlank { "Maintenance in Progress" },
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = maintenance.message.ifBlank { "We are currently improving server performance. Antigravity will return shortly!" },
                color = Color(0xFF94A3B8),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Refresh Status Button
            Button(
                onClick = onRefresh,
                enabled = !isRefreshing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = OperaRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(48.dp)
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Checking...", fontWeight = FontWeight.SemiBold)
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Check Status", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live Web Status button
            OutlinedButton(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://antigravity-app-tawny.vercel.app"))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.OpenInBrowser,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Status Portal", fontSize = 13.sp)
            }
        }
    }
}

/**
 * Non-dismissible Force Update Dialog
 */
@Composable
fun ForceUpdateDialog(
    versionConfig: RemoteVersionConfig
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = {}, // Cannot be dismissed
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF12141C),
            border = BorderStroke(1.5.dp, OperaRed.copy(alpha = 0.5f)),
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(OperaRed.copy(alpha = 0.15f))
                        .border(1.5.dp, OperaRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SystemUpdate,
                        contentDescription = null,
                        tint = OperaRed,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Update Required",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "A required update (v${versionConfig.latest_version_name}) is available. Please update to continue using Antigravity.",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                if (versionConfig.changelog.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF090A0F), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "What's New:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OperaRed
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        versionConfig.changelog.take(3).forEach { item ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("• ", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                                Text(item, color = Color(0xFFCBD5E1), fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(versionConfig.download_url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OperaRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        "Download Update",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
