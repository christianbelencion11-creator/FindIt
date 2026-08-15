package com.example.findit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.findit.ui.theme.Dimensions
import com.example.findit.ui.theme.Spacing
import com.example.findit.ui.theme.StatAmber
import com.example.findit.ui.theme.StatEmerald
import com.example.findit.ui.theme.StatPurple
import com.example.findit.ui.theme.darkCardGradientFill
import com.example.findit.ui.theme.darkSurfaceBorder
import com.example.findit.ui.theme.isAppDarkTheme

data class StatsData(
    val totalItems: Int,
    val categories: Int,
    val recentlyAdded: Int
)

/**
 * "Your Overview" dashboard: a titled section with an optional "View all" action and a single
 * unified panel holding three stats. Each stat has a circular tinted icon badge so the block
 * reads as one cohesive card (no floating sub-cards that can misalign).
 */
@Composable
fun StatsSection(
    stats: StatsData,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    onViewAllClick: (() -> Unit)? = null,
    onTotalItemsClick: (() -> Unit)? = null,
    onCategoriesClick: (() -> Unit)? = null,
    onRecentClick: (() -> Unit)? = null
) {
    if (!visible) return
    val dark = isAppDarkTheme()
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Your Overview",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (onViewAllClick != null) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onViewAllClick)
                        .padding(start = Spacing.sm, top = Spacing.xxs, bottom = Spacing.xxs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)
                ) {
                    Text(
                        text = "View all",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Dimensions.cardCornerRadius),
            border = if (dark) darkSurfaceBorder() else null,
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (dark) 0.dp else Dimensions.cardElevation
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (dark) Color.Transparent else MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .darkCardGradientFill(dark)
                    .padding(vertical = Spacing.lg, horizontal = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OverviewStat(
                    value = stats.totalItems.toString(),
                    label = "Total Items",
                    icon = Icons.Outlined.Inventory2,
                    accent = StatEmerald,
                    onClick = onTotalItemsClick,
                    modifier = Modifier.weight(1f)
                )
                OverviewDivider(dark)
                OverviewStat(
                    value = stats.categories.toString(),
                    label = "Categories",
                    icon = Icons.Outlined.GridView,
                    accent = StatPurple,
                    onClick = onCategoriesClick,
                    modifier = Modifier.weight(1f)
                )
                OverviewDivider(dark)
                OverviewStat(
                    value = stats.recentlyAdded.toString(),
                    label = "Recent",
                    icon = Icons.Outlined.Schedule,
                    accent = StatAmber,
                    onClick = onRecentClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun OverviewStat(
    value: String,
    label: String,
    icon: ImageVector,
    accent: Color,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val clickModifier = if (onClick != null) {
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    } else {
        Modifier
    }
    Column(
        modifier = modifier
            .then(clickModifier)
            .padding(vertical = Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.padding(top = Spacing.xxs)
        )
    }
}

/** Hairline separator between the three stats — very faint so the panel still reads as one card. */
@Composable
private fun OverviewDivider(dark: Boolean) {
    Box(
        modifier = Modifier
            .height(44.dp)
            .width(1.dp)
            .background(
                MaterialTheme.colorScheme.onSurface.copy(alpha = if (dark) 0.10f else 0.08f)
            )
    )
}
