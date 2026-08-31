package com.example.iremember.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Apps
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.iremember.ui.theme.Spacing

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

@Composable
private fun resolveCategoryColors(category: String): CategoryColors {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return categoryColors(category, dark)
}

private fun categoryColors(category: String, dark: Boolean): CategoryColors = when {
    category.contains("Electronic", ignoreCase = true) ->
        if (dark) CategoryColors(Color(0xFF1E3A5F), Color(0xFF93C5FD), Color(0xFF3B82F6))
        else CategoryColors(Color(0xFFDBEAFE), Color(0xFF1D4ED8), Color(0xFF3B82F6))
    category.contains("Key", ignoreCase = true) ->
        if (dark) CategoryColors(Color(0xFF365314), Color(0xFFD9F99D), Color(0xFF84CC16))
        else CategoryColors(Color(0xFFECFCCB), Color(0xFF3F6212), Color(0xFF84CC16))
    category.contains("Document", ignoreCase = true) ->
        if (dark) CategoryColors(Color(0xFF7C2D12), Color(0xFFFDBA74), Color(0xFFF97316))
        else CategoryColors(Color(0xFFFFEDD5), Color(0xFF9A3412), Color(0xFFF97316))
    category.contains("Personal", ignoreCase = true) ->
        if (dark) CategoryColors(Color(0xFF4C1D95), Color(0xFFDDD6FE), Color(0xFFA78BFA))
        else CategoryColors(Color(0xFFEDE9FE), Color(0xFF5B21B6), Color(0xFF8B5CF6))
    category.contains("Wallet", ignoreCase = true) ->
        if (dark) CategoryColors(Color(0xFF134E4A), Color(0xFF99F6E4), Color(0xFF14B8A6))
        else CategoryColors(Color(0xFFCCFBF1), Color(0xFF0F766E), Color(0xFF14B8A6))
    category.contains("Bag", ignoreCase = true) ->
        if (dark) CategoryColors(Color(0xFF831843), Color(0xFFFBCFE8), Color(0xFFEC4899))
        else CategoryColors(Color(0xFFFCE7F3), Color(0xFF9D174D), Color(0xFFEC4899))
    else ->
        if (dark) CategoryColors(Color(0xFF312E81), Color(0xFFC7D2FE), Color(0xFF6366F1))
        else CategoryColors(Color(0xFFE0E7FF), Color(0xFF3730A3), Color(0xFF6366F1))
}

/** Small inline badge shown on ItemCard */
@Composable
fun CategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    val c = resolveCategoryColors(category)
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
                color = c.text,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
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
    val c = resolveCategoryColors(category)
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
                modifier = Modifier.size(FilterChipDefaults.IconSize)
            )
        },
        modifier = modifier.height(FilterChipDefaults.Height),
        shape = RoundedCornerShape(8.dp),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderWidth = 0.dp,
            selectedBorderWidth = 0.dp
        ),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = c.bg,
            labelColor = c.text,
            iconColor = c.text,
            selectedContainerColor = c.accent,
            selectedLabelColor = Color.White,
            selectedLeadingIconColor = Color.White
        )
    )
}
