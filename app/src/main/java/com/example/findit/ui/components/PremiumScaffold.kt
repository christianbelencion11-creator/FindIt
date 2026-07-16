package com.example.findit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.findit.ui.theme.Dimensions
import com.example.findit.ui.theme.gradientEndColor
import com.example.findit.ui.theme.gradientStartColor

/**
 * Premium screen layout: green gradient header that fully collapses on scroll so
 * body content becomes edge-to-edge; header text fades back in when scrolling down.
 */
@Composable
fun PremiumScaffold(
    modifier: Modifier = Modifier,
    headerHeight: Dp = Dimensions.headerContentStd,
    headerMinHeight: Dp = Dimensions.headerContentCompact,
    collapsible: Boolean = true,
    onUserInteraction: () -> Unit = {},
    headerContent: @Composable ColumnScope.(collapseFraction: Float) -> Unit,
    bodyContent: @Composable (scrollModifier: Modifier) -> Unit
) {
    val density = LocalDensity.current
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val collapseRangePx = with(density) {
        (headerHeight - headerMinHeight).toPx().coerceAtLeast(1f)
    }

    var collapseOffsetPx by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember(collapseRangePx, onUserInteraction) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = -available.y
                if (delta == 0f) return Offset.Zero
                val previous = collapseOffsetPx
                collapseOffsetPx = (collapseOffsetPx + delta).coerceIn(0f, collapseRangePx)
                val consumed = collapseOffsetPx - previous
                if (consumed != 0f) onUserInteraction()
                return Offset(0f, -consumed)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = -available.y
                if (delta >= 0f) return Offset.Zero
                val previous = collapseOffsetPx
                collapseOffsetPx = (collapseOffsetPx + delta).coerceIn(0f, collapseRangePx)
                val consumedCollapse = collapseOffsetPx - previous
                if (consumedCollapse != 0f) onUserInteraction()
                return Offset(0f, -consumedCollapse)
            }
        }
    }

    val collapseFraction = if (collapsible) {
        (collapseOffsetPx / collapseRangePx).coerceIn(0f, 1f)
    } else {
        0f
    }

    val expandedHeaderHeight = if (collapsible) {
        headerHeight - (headerHeight - headerMinHeight) * collapseFraction
    } else {
        headerHeight
    }
    val topCornerRadius = if (collapsible) {
        Dimensions.headerCornerRadius * (1f - collapseFraction)
    } else {
        Dimensions.headerCornerRadius
    }
    val bodyTopInset = statusBarTop + expandedHeaderHeight - topCornerRadius
    val gradientHeight = statusBarTop + expandedHeaderHeight
    val headerAlpha = (1f - collapseFraction).coerceIn(0f, 1f)

    val scrollModifier = if (collapsible) {
        Modifier.nestedScroll(nestedScrollConnection)
    } else {
        Modifier
    }

    val bodyShape = if (topCornerRadius > 0.dp) {
        RoundedCornerShape(topStart = topCornerRadius, topEnd = topCornerRadius)
    } else {
        RoundedCornerShape(0.dp)
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (expandedHeaderHeight > 0.dp || topCornerRadius > 0.dp) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(gradientHeight)
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(gradientStartColor(), gradientEndColor())
                            )
                        )
                )
                HeaderAtmosphere(modifier = Modifier.fillMaxSize())
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = bodyTopInset),
            shape = bodyShape,
            color = MaterialTheme.colorScheme.background
        ) {
            bodyContent(scrollModifier)
        }

        if (headerAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = statusBarTop)
                    .height(expandedHeaderHeight)
                    .clip(RectangleShape)
                    .alpha(headerAlpha),
                contentAlignment = Alignment.TopStart
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    content = { headerContent(collapseFraction) }
                )
            }
        }
    }
}
