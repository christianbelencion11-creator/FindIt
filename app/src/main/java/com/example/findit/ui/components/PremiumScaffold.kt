package com.example.findit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import com.example.findit.ui.theme.Dimensions
import com.example.findit.ui.theme.gradientEndColor
import com.example.findit.ui.theme.gradientStartColor

/**
 * Premium screen layout: fresh green gradient header that transitions into
 * a rounded-corner white body card — the standard fintech dashboard pattern.
 */
@Composable
fun PremiumScaffold(
    modifier: Modifier = Modifier,
    headerHeight: Dp = Dimensions.headerContentStd,
    headerContent: @Composable ColumnScope.() -> Unit,
    bodyContent: @Composable () -> Unit
) {
    val gradientBoxHeight = headerHeight + Dimensions.headerCornerRadius

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(gradientBoxHeight)
                .background(
                    Brush.linearGradient(
                        colors = listOf(gradientStartColor(), gradientEndColor())
                    )
                )
        )

        Column(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
                    .statusBarsPadding(),
                content = headerContent
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(
                    topStart = Dimensions.headerCornerRadius,
                    topEnd   = Dimensions.headerCornerRadius
                ),
                color = MaterialTheme.colorScheme.background
            ) {
                bodyContent()
            }
        }
    }
}
