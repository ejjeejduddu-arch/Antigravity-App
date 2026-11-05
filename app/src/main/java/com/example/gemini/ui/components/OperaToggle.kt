package com.example.gemini.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gemini.theme.*

/**
 * Authentic Opera Toggle Switch Component.
 *
 * Faithfully matches Opera's `Toggle.tsx`:
 * - Red pill (#FF1A1A) with border when checked.
 * - Subtle surface pill when unchecked.
 * - Smooth spring-animated white circular thumb.
 */
@Composable
fun OperaToggle(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val trackWidth = 44.dp
    val trackHeight = 26.dp
    val thumbSize = 20.dp
    val thumbTravel = trackWidth - thumbSize - 6.dp // 18.dp

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) thumbTravel else 3.dp,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = 0.75f
        ),
        label = "opera_toggle_thumb_offset"
    )

    val trackColor by animateColorAsState(
        targetValue = if (checked) {
            OperaRed
        } else if (isDarkTheme) {
            Color(0xFF2C2C2C)
        } else {
            Color(0xFFE5E5E5)
        },
        label = "opera_toggle_track_color"
    )

    val borderColor by animateColorAsState(
        targetValue = if (checked) {
            OperaBorderOnPrimary
        } else if (isDarkTheme) {
            Color(0x22FFFFFF)
        } else {
            Color(0x18000000)
        },
        label = "opera_toggle_border_color"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(width = trackWidth, height = trackHeight)
            .clip(RoundedCornerShape(13.dp))
            .background(trackColor)
            .border(width = 1.5.dp, color = borderColor, shape = RoundedCornerShape(13.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) {
                onCheckedChange?.invoke(!checked)
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Sliding Circular White Thumb with subtle drop shadow
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .shadow(elevation = 2.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}
