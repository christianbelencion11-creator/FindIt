package com.example.findit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.findit.ui.theme.Spacing
import com.example.findit.util.UiPreferences
import kotlin.math.abs
import kotlin.math.roundToInt

private val FabSize = 56.dp
private val FabMargin = Spacing.xl
private const val TapSlopPx = 18f

@Composable
fun DraggableFab(
    onClick: () -> Unit,
    uiPreferences: UiPreferences,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(uiPreferences.getFabOffsetX()) }
    var offsetY by remember { mutableFloatStateOf(uiPreferences.getFabOffsetY()) }

    val density = LocalDensity.current
    val fabSizePx = with(density) { FabSize.toPx() }
    val marginPx = with(density) { FabMargin.toPx() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .zIndex(1f),
        contentAlignment = Alignment.BottomEnd
    ) {
        val maxOffsetX = (constraints.maxWidth - fabSizePx - marginPx * 2).coerceAtLeast(0f)
        val maxOffsetY = (constraints.maxHeight - fabSizePx - marginPx * 2).coerceAtLeast(0f)

        offsetX = offsetX.coerceIn(-maxOffsetX, 0f)
        offsetY = offsetY.coerceIn(-maxOffsetY, 0f)

        Box(
            modifier = Modifier
                .padding(FabMargin)
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .size(FabSize)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .pointerInput(maxOffsetX, maxOffsetY) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var dragDistance = 0f

                        val dragged = drag(down.id) { change ->
                            val delta = change.positionChange()
                            dragDistance += abs(delta.x) + abs(delta.y)
                            offsetX = (offsetX + delta.x).coerceIn(-maxOffsetX, 0f)
                            offsetY = (offsetY + delta.y).coerceIn(-maxOffsetY, 0f)
                            change.consume()
                        }

                        if (!dragged || dragDistance < TapSlopPx) {
                            onClick()
                        } else {
                            val snapLeft = abs(offsetX + maxOffsetX / 2f) < abs(offsetX)
                            offsetX = if (snapLeft) -maxOffsetX else 0f
                            uiPreferences.setFabOffset(offsetX, offsetY)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add item",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
