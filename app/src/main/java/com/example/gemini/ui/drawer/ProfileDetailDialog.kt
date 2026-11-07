package com.example.gemini.ui.drawer

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Token
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.gemini.data.remote.AgyHubClient
import com.example.gemini.theme.ClaudeTerracotta
import com.example.gemini.theme.QuotaGreen

/**
 * Safely parses base64 data URIs ("data:image/jpeg;base64,...") into ByteArray for Coil,
 * or returns original string for standard HTTP/HTTPS URLs.
 */
fun parseProfileAvatarModel(profilePictureUrl: String?): Any? {
    if (profilePictureUrl.isNullOrBlank()) return null
    if (profilePictureUrl.startsWith("data:image/", ignoreCase = true)) {
        try {
            val commaIdx = profilePictureUrl.indexOf(',')
            if (commaIdx != -1) {
                val base64Part = profilePictureUrl.substring(commaIdx + 1).trim()
                return android.util.Base64.decode(base64Part, android.util.Base64.DEFAULT)
            }
        } catch (_: Exception) {}
    }
    return profilePictureUrl
}

@Composable
fun ProfileDetailDialog(
    authInfo: AgyHubClient.AgyAuthInfo,
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var showMembershipDialog by remember { mutableStateOf(false) }
    val authPrefs = remember { com.example.gemini.data.preferences.AuthPreferences(context) }
    val currentPlan by authPrefs.subscriptionTier.collectAsState(initial = "free")
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Avatar
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(parseProfileAvatarModel(authInfo.profilePictureUrl))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .border(2.5.dp, ClaudeTerracotta, CircleShape),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(ClaudeTerracotta),
                            contentAlignment = Alignment.Center
                        ) {
                            val initial = authInfo.displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "A"
                            Text(
                                text = initial,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(ClaudeTerracotta),
                            contentAlignment = Alignment.Center
                        ) {
                            val initial = authInfo.displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "A"
                            Text(
                                text = initial,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Display Name
                Text(
                    text = authInfo.displayName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (authInfo.email.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = authInfo.email,
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // User Tier / Plan Badge Card (VIP vs Non-VIP)
                val rawTier = (currentPlan ?: authInfo.userTier.ifBlank { "free" }).lowercase()
                val isVip = rawTier in listOf("go", "plus", "pro")
                val badgeText = when (rawTier) {
                    "pro" -> "👑 VIP PLATINUM (Pro)"
                    "plus" -> "👑 VIP GOLD (Plus)"
                    "go" -> "⚡ VIP SILVER (Go)"
                    else -> "🛡️ Non-VIP (Standard)"
                }
                val badgeColor = if (isVip) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                val badgeBg = if (isVip) Color(0xFFFFB300).copy(alpha = 0.14f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                val badgeBorder = if (isVip) Color(0xFFFFB300).copy(alpha = 0.45f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = badgeBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, badgeBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isVip) Icons.Outlined.WorkspacePremium else Icons.Outlined.VerifiedUser,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = badgeText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }

                // Credits Section (if prompt or flow credits available)
                if (authInfo.availablePromptCredits != null || authInfo.availableFlowCredits != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (authInfo.availablePromptCredits != null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.Token,
                                            contentDescription = null,
                                            tint = QuotaGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Prompt Credits",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${authInfo.availablePromptCredits} Available",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            if (authInfo.availablePromptCredits != null && authInfo.availableFlowCredits != null) {
                                Box(
                                    modifier = Modifier
                                        .height(26.dp)
                                        .width(1.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                )
                            }

                            if (authInfo.availableFlowCredits != null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.Speed,
                                            contentDescription = null,
                                            tint = ClaudeTerracotta,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Flow Credits",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${authInfo.availableFlowCredits} Available",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))

                // Details: Email
                if (authInfo.email.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = "Email",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Account Email",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = authInfo.email,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Details: Auth Status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VerifiedUser,
                        contentDescription = "Status",
                        tint = ClaudeTerracotta,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Status",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = if (authInfo.isOffline) "Connected (Offline verification)" else "Active & Authenticated (AGY RPC)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Details: Subscription / Upgrade Plan
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showMembershipDialog = true }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WorkspacePremium,
                        contentDescription = "Upgrade Plan",
                        tint = if (isVip) Color(0xFFFFB300) else ClaudeTerracotta,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Subscription Tier",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = if (isVip) "👑 VIP ${rawTier.uppercase()} (Active • Tap to Manage)" else "🛡️ Standard Free (Tap to Upgrade to VIP)",
                            fontSize = 12.sp,
                            color = if (isVip) Color(0xFFFFB300) else ClaudeTerracotta,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (showMembershipDialog) {
                    com.example.gemini.ui.membership.MembershipPlansDialog(
                        currentPlan = currentPlan ?: "free",
                        onDismiss = { showMembershipDialog = false },
                        onPlanUpdated = { newTier ->
                            scope.launch {
                                authPrefs.setSubscriptionTier(newTier)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Logout Button
                OutlinedButton(
                    onClick = {
                        onLogout()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = "Sign Out",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign Out",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
