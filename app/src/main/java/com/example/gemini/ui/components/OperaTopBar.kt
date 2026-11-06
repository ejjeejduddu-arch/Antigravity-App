package com.example.gemini.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gemini.R
import com.example.gemini.theme.*

/**
 * Authentic Opera Top Floating Action Bar.
 *
 * Faithfully matches Screenshot 3 and Opera's `TopBar.tsx`:
 * - Left Segmented Pill: [ ☰   + ] for drawer menu and new chat.
 * - Center/Middle Pill: [ 🤖 Model ] for active model selection.
 * - Right Squircle Button: [ ▦ ] for Settings and system tools.
 * - Elevated with Opera's signature sticker shadow and rounded corners.
 */
@Composable
fun OperaTopBar(
    onMenuClick: () -> Unit,
    onNewChatClick: () -> Unit,
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
    val pillBg = if (isDarkTheme) OperaDarkSurface else OperaSurface
    val pillBorder = if (isDarkTheme) OperaDarkBorder else OperaBorder
    val iconTint = if (isDarkTheme) OperaDarkTextPrimary else OperaTextPrimary
    val textTint = if (isDarkTheme) OperaDarkTextPrimary else OperaTextPrimary
    val dividerColor = if (isDarkTheme) Color(0x1AFFFFFF) else Color(0x12000000)

    val shape = RoundedCornerShape(14.dp)
    val pillHeight = 44.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Pill: [ ☰   + ]
        Box(
            modifier = Modifier
                .shadow(elevation = 3.dp, shape = shape, ambientColor = OperaShadowInk, spotColor = OperaShadowInk)
                .clip(shape)
                .background(pillBg)
                .border(BorderStroke(1.5.dp, pillBorder), shape)
                .height(pillHeight)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                // Menu Button (☰)
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = pillHeight)
                        .clickable(onClick = onMenuClick),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.opera_icon_more),
                        contentDescription = "Conversations Menu",
                        colorFilter = ColorFilter.tint(iconTint),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Vertical Divider between buttons
                Box(
                    modifier = Modifier
                        .size(width = 1.dp, height = 20.dp)
                        .background(dividerColor)
                )

                // New Chat Button (+)
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = pillHeight)
                        .clickable(onClick = onNewChatClick),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.opera_icon_add),
                        contentDescription = "New Conversation",
                        colorFilter = ColorFilter.tint(iconTint),
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        // Center / Model Selector Pill: [ 🤖 Model ] (Turned off by default)
        if (showModelSelector && onModelSelectorClick != null) {
            Box(
                modifier = Modifier
                    .shadow(elevation = 3.dp, shape = shape, ambientColor = OperaShadowInk, spotColor = OperaShadowInk)
                    .clip(shape)
                    .background(pillBg)
                    .border(BorderStroke(1.5.dp, pillBorder), shape)
                    .height(pillHeight)
                    .clickable(onClick = onModelSelectorClick)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.opera_icon_bot),
                        contentDescription = "AI Model",
                        colorFilter = ColorFilter.tint(iconTint),
                        modifier = Modifier.size(18.dp)
                    )

                    val displayModel = selectedModelName.ifBlank { "Model" }
                    Text(
                        text = displayModel,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textTint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Right Controls Cluster (Minimize, Fullscreen, Settings)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (onMinimizeWindow != null) {
                Box(
                    modifier = Modifier
                        .shadow(elevation = 3.dp, shape = shape, ambientColor = OperaShadowInk, spotColor = OperaShadowInk)
                        .clip(shape)
                        .background(pillBg)
                        .border(BorderStroke(1.5.dp, pillBorder), shape)
                        .size(pillHeight)
                        .clickable(onClick = onMinimizeWindow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Minimize to Bubble",
                        tint = iconTint.copy(alpha = 0.75f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (onOpenFullScreen != null) {
                Box(
                    modifier = Modifier
                        .shadow(elevation = 3.dp, shape = shape, ambientColor = OperaShadowInk, spotColor = OperaShadowInk)
                        .clip(shape)
                        .background(pillBg)
                        .border(BorderStroke(1.5.dp, pillBorder), shape)
                        .size(pillHeight)
                        .clickable(onClick = onOpenFullScreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = "Open Full Screen",
                        tint = iconTint.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Rightmost Squircle: [ ▦ ] Settings Button
            Box(
                modifier = Modifier
                    .shadow(elevation = 3.dp, shape = shape, ambientColor = OperaShadowInk, spotColor = OperaShadowInk)
                    .clip(shape)
                    .background(pillBg)
                    .border(BorderStroke(1.5.dp, pillBorder), shape)
                    .size(pillHeight)
                    .clickable(onClick = onSettingsClick),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.opera_icon_tiles),
                    contentDescription = "Settings & Tools",
                    colorFilter = ColorFilter.tint(iconTint),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
