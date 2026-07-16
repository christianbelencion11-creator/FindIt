package com.example.findit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.findit.ui.theme.Spacing

fun categoryIcon(category: String): ImageVector = when {
    category.equals("All", ignoreCase = true) -> Icons.Default.Apps
    category.contains("Key", ignoreCase = true) -> Icons.Default.VpnKey
    category.contains("Document", ignoreCase = true) -> Icons.Default.Description
    category.contains("Electronic", ignoreCase = true) -> Icons.Default.Devices
    category.contains("Wallet", ignoreCase = true) -> Icons.Default.AccountBalanceWallet
    category.contains("Bag", ignoreCase = true) -> Icons.Default.ShoppingBag
    category.contains("Personal", ignoreCase = true) -> Icons.Default.Person
    category.contains("Other", ignoreCase = true) -> Icons.Default.Apps
    else -> Icons.Default.Inventory
}

private data class CategoryColors(val bg: Color, val text: Color, val accent: Color)

private fun categoryColors(category: String): CategoryColors = when {
    category.contains("Electronic", ignoreCase = true) ->
        CategoryColors(Color(0xFFE8F8EF), Color(0xFF047857), Color(0xFF16A34A))
    category.contains("Key", ignoreCase = true) ->
        CategoryColors(Color(0xFFF2FCE8), Color(0xFF3F6212), Color(0xFF84CC16))
    category.contains("Document", ignoreCase = true) ->
        CategoryColors(Color(0xFFFFF8DB), Color(0xFF9A6A00), Color(0xFFEAB308))
    category.contains("Personal", ignoreCase = true) ->
        CategoryColors(Color(0xFFE8FFF5), Color(0xFF0F766E), Color(0xFF14B8A6))
    else ->
        CategoryColors(Color(0xFFF0FDF4), Color(0xFF166534), Color(0xFF22C55E))
}

/** Small inline badge shown on ItemCard */
@Composable
fun CategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    val c = categoryColors(category)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(c.bg)
            .padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = categoryIcon(category),
                contentDescription = null,
                modifier = Modifier.size(10.dp),
                tint = c.text
            )
            Text(
                text = category,
                style = MaterialTheme.typography.labelSmall,
                color = c.text
            )
        }
    }
}

/** Selectable filter chip used in category rows */
@Composable
fun CategoryChip(
    category: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val c = categoryColors(category)
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = category,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip
            )
        },
        leadingIcon = {
            Icon(
                imageVector = categoryIcon(category),
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
        },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = c.bg,
            labelColor = c.text,
            iconColor = c.text,
            // Solid selected fill — low-alpha accents look murky on dark green screens.
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}
