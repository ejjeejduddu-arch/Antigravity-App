package com.example.gemini.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.gemini.R
import com.example.gemini.theme.*

/**
 * Authentic Opera Floating Top Navigation Bar.
 *
 * Styled in signature Opera Red (`#FF1A1A`) with subtle floating elevation shadow:
 * - Left: Single Menu drawer icon (☰). The (+) icon has been removed per user preference.
 * - Center: Dead-centered official Antigravity logo lockup in white (`antigravity_lockup_white`).
 * - Right: Settings icon (▦) and optional window management controls (minimize/fullscreen).
 */
@Composable
fun OperaTopBar(
    onMenuClick: () -> Unit,
    onNewChatClick: () -> Unit = {},
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedModelName: String = "",
    onModelSelectorClick: (() -> Unit)? = null,
    showModelSelector: Boolean = false,
    isDarkTheme: Boolean = false,
    incognito: Boolean = false,
    onMinimizeWindow: (() -> Unit)? = null,
    onOpenFullScreen: (() -> Unit)? = null
) {
    val navBg = if (incognito) OperaIncognito else OperaRed
    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 6.dp,
                    shape = shape,
                    ambientColor = Color(0x33000000),
                    spotColor = Color(0x4D000000)
                ),
            shape = shape,
            color = navBg,
            border = BorderStroke(1.5.dp, OperaBorderOnPrimary)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 8.dp)
            ) {
                // Left: Menu Drawer Button (☰) - Single clean action
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onMenuClick),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.opera_icon_more),
                        contentDescription = "Conversations Menu",
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Center: Official Antigravity White Brand Lockup (Dead-centered across the screen)
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .wrapContentSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.antigravity_lockup_white),
                        contentDescription = "Google Antigravity",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .height(20.dp)
                            .wrapContentWidth()
                    )
                }

                // Right: Controls Cluster (Settings ▦ + Optional floating window actions)
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (onMinimizeWindow != null) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onMinimizeWindow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Minimize to Bubble",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (onOpenFullScreen != null) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onOpenFullScreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                contentDescription = "Open Full Screen",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Settings Button (▦)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onSettingsClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.opera_icon_tiles),
                            contentDescription = "Settings & Tools",
                            colorFilter = ColorFilter.tint(Color.White),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }
}
