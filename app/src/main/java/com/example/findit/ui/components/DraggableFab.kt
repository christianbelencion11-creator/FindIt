package com.example.findit.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.findit.ui.theme.Dimensions
import com.example.findit.ui.theme.Spacing
import com.example.findit.util.UiPreferences
import kotlin.math.abs
import kotlin.math.roundToInt

private val FabSize = 56.dp
private val MiniFabSize = 48.dp
private val FabMargin = Spacing.xl
private const val TapSlopPx = 18f
private val LabelShape = RoundedCornerShape(8.dp)

/**
 * Draggable primary FAB. When [onVoiceSearchClick] is set, tap toggles a speed-dial
 * with Microphone + Notes + Add item instead of navigating immediately.
 */
@Composable
fun DraggableFab(
    onClick: () -> Unit,
    uiPreferences: UiPreferences,
    modifier: Modifier = Modifier,
    onVoiceSearchClick: (() -> Unit)? = null,
    onNotesClick: (() -> Unit)? = null
) {
    val density = LocalDensity.current
    val fabSizePx = with(density) { FabSize.toPx() }
    val marginPx = with(density) { FabMargin.toPx() }
    val bottomClearancePx = with(density) { Dimensions.floatingBottomNavClearance.toPx() }
    val defaultOffsetY = -bottomClearancePx

    var offsetX by remember {
        mutableFloatStateOf(
            if (uiPreferences.hasFabOffset()) uiPreferences.getFabOffsetX() else 0f
        )
    }
    var offsetY by remember {
        mutableFloatStateOf(
            if (uiPreferences.hasFabOffset()) uiPreferences.getFabOffsetY() else defaultOffsetY
        )
    }
    var expanded by remember { mutableStateOf(false) }
    val useSpeedDial = onVoiceSearchClick != null

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .zIndex(1f),
        contentAlignment = Alignment.BottomEnd
    ) {
        val maxOffsetX = (constraints.maxWidth - fabSizePx - marginPx * 2).coerceAtLeast(0f)
        val maxOffsetY = (constraints.maxHeight - fabSizePx - marginPx * 2).coerceAtLeast(0f)
        val minOffsetY = -maxOffsetY
        val maxAllowedY = (-bottomClearancePx).coerceAtLeast(minOffsetY)

        offsetX = offsetX.coerceIn(-maxOffsetX, 0f)
        offsetY = offsetY.coerceIn(minOffsetY, maxAllowedY)

        if (expanded && useSpeedDial) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable { expanded = false }
                    .zIndex(0.5f)
            )
        }

        Box(
            modifier = Modifier
                .padding(FabMargin)
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .zIndex(2f),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                AnimatedVisibility(
                    visible = expanded && useSpeedDial,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        SpeedDialAction(
                            icon = Icons.Default.Mic,
                            label = "Microphone",
                            containerColor = Color(0xFF3B82F6),
                            onClick = {
                                expanded = false
                                onVoiceSearchClick?.invoke()
                            }
                        )
                        if (onNotesClick != null) {
                            SpeedDialAction(
                                icon = Icons.AutoMirrored.Filled.Notes,
                                label = "Notes",
                                containerColor = Color(0xFF0D9488),
                                onClick = {
                                    expanded = false
                                    onNotesClick.invoke()
                                }
                            )
                        }
                        SpeedDialAction(
                            icon = Icons.Default.PostAdd,
                            label = "Add item",
                            containerColor = MaterialTheme.colorScheme.primary,
                            onClick = {
                                expanded = false
                                onClick()
                            }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(FabSize)
                        .shadow(6.dp, CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .pointerInput(maxOffsetX, minOffsetY, maxAllowedY, useSpeedDial) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                var dragDistance = 0f

                                val dragged = drag(down.id) { change ->
                                    val delta = change.positionChange()
                                    dragDistance += abs(delta.x) + abs(delta.y)
                                    offsetX = (offsetX + delta.x).coerceIn(-maxOffsetX, 0f)
                                    offsetY = (offsetY + delta.y).coerceIn(minOffsetY, maxAllowedY)
                                    change.consume()
                                }

                                if (!dragged || dragDistance < TapSlopPx) {
                                    if (useSpeedDial) {
                                        expanded = !expanded
                                    } else {
                                        onClick()
                                    }
                                } else {
                                    expanded = false
                                    val snapLeft = abs(offsetX + maxOffsetX / 2f) < abs(offsetX)
                                    offsetX = if (snapLeft) -maxOffsetX else 0f
                                    uiPreferences.setFabOffset(offsetX, offsetY)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = if (expanded) "Close menu" else "Add",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeedDialAction(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier
                .padding(bottom = Spacing.xs, end = Spacing.xs)
                .background(Color.Black.copy(alpha = 0.45f), LabelShape)
                .padding(horizontal = Spacing.sm, vertical = Spacing.xxs)
        )
        Box(
            modifier = Modifier
                .size(MiniFabSize)
                .shadow(4.dp, CircleShape)
        ) {
            Surface(
                onClick = onClick,
                modifier = Modifier.size(MiniFabSize),
                shape = CircleShape,
                color = containerColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
