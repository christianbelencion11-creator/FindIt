package com.example.iremember.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.iremember.ui.theme.Dimensions
import com.example.iremember.ui.theme.Spacing
import com.example.iremember.ui.theme.gradientEndColor
import com.example.iremember.ui.theme.gradientStartColor

// Kept for backward compatibility — all new screens use PremiumScaffold directly.
@Composable
fun GradientHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    showLogo: Boolean = false,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (content != null) Dimensions.headerContentStd + 56.dp else Dimensions.headerContentStd)
            .background(
                Brush.linearGradient(
                    colors = listOf(gradientStartColor(), gradientEndColor())
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl, vertical = Spacing.xxl)
        ) {
            if (showLogo) {
                IRememberTopBar(title = title, subtitle = subtitle)
            } else {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                }
            }
            content?.invoke()
        }
    }
}
