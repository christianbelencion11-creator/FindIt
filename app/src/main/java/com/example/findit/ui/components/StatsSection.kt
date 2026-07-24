package com.example.findit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.findit.ui.theme.Dimensions
import com.example.findit.ui.theme.Spacing
import com.example.findit.ui.theme.StatBlue
import com.example.findit.ui.theme.StatBlueDark
import com.example.findit.ui.theme.StatGreen
import com.example.findit.ui.theme.StatGreenDark
import com.example.findit.ui.theme.StatPurple
import com.example.findit.ui.theme.StatPurpleDark
import com.example.findit.ui.theme.darkCardGradientFill
import com.example.findit.ui.theme.darkSurfaceBorder
import com.example.findit.ui.theme.isAppDarkTheme

data class StatsData(
    val totalItems: Int,
    val categories: Int,
    val recentlyAdded: Int
)

@Composable
fun StatsSection(
    stats: StatsData,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    onTotalItemsClick: (() -> Unit)? = null,
    onCategoriesClick: (() -> Unit)? = null,
    onRecentClick: (() -> Unit)? = null
) {
    if (!visible) return
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        PremiumStatCard(
            label = "Total Items",
            value = stats.totalItems.toString(),
            icon = Icons.Default.Inventory,
            iconBrush = Brush.linearGradient(listOf(StatBlue, StatBlueDark)),
            onClick = onTotalItemsClick,
            modifier = Modifier.weight(1f)
        )
        PremiumStatCard(
            label = "Categories",
            value = stats.categories.toString(),
            icon = Icons.Default.Category,
            iconBrush = Brush.linearGradient(listOf(StatPurple, StatPurpleDark)),
            onClick = onCategoriesClick,
            modifier = Modifier.weight(1f)
        )
        PremiumStatCard(
            label = "Recent",
            value = stats.recentlyAdded.toString(),
            icon = Icons.Default.Schedule,
            iconBrush = Brush.linearGradient(listOf(StatGreen, StatGreenDark)),
            onClick = onRecentClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PremiumStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    iconBrush: Brush,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val dark = isAppDarkTheme()
    val shape = RoundedCornerShape(Dimensions.cardCornerRadius)
    val content: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .darkCardGradientFill(dark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xxs)
                )
            }
        }
    }
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            border = if (dark) darkSurfaceBorder() else null,
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (dark) 0.dp else Dimensions.cardElevation
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (dark) Color.Transparent else MaterialTheme.colorScheme.surface
            ),
            content = { content() }
        )
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            border = if (dark) darkSurfaceBorder() else null,
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (dark) 0.dp else Dimensions.cardElevation
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (dark) Color.Transparent else MaterialTheme.colorScheme.surface
            ),
            content = { content() }
        )
    }
}
