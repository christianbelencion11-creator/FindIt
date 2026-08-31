package com.example.iremember.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.iremember.ui.theme.Spacing

/**
 * App-bar style header: hamburger beside title (same row), subtitle under the title.
 */
@Composable
fun TabMenuHeader(
    title: String,
    subtitle: String,
    onMenuClick: () -> Unit,
    collapseFraction: Float = 0f,
    leadingIsBack: Boolean = false,
    modifier: Modifier = Modifier
) {
    val secondaryAlpha = (1f - collapseFraction).coerceIn(0f, 1f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = Spacing.xl, end = Spacing.xl, top = Spacing.md, bottom = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderIconButton(
            onClick = onMenuClick,
            icon = if (leadingIsBack) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Menu,
            contentDescription = if (leadingIsBack) "Back" else "Menu",
            containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
            iconTint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.alpha(secondaryAlpha)
        )
        Spacer(Modifier.width(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .alpha(secondaryAlpha)
            )
        }
    }
}
