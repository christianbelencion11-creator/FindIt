package com.example.findit.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.findit.model.Item
import com.example.findit.ui.components.EmptyState
import com.example.findit.ui.components.HeaderIconButton
import com.example.findit.ui.components.ItemCard
import com.example.findit.ui.components.PremiumScaffold
import com.example.findit.ui.components.categoryIcon
import com.example.findit.ui.theme.Dimensions
import com.example.findit.ui.theme.Spacing
import com.example.findit.ui.theme.darkCardGradientFill
import com.example.findit.ui.theme.darkSurfaceBorder
import com.example.findit.ui.theme.isAppDarkTheme
import com.example.findit.viewmodel.ItemViewModel

enum class StatsBrowseMode {
    AllItems,
    Categories,
    Recent
}

@Composable
fun StatsBrowseScreen(
    mode: StatsBrowseMode,
    viewModel: ItemViewModel,
    onBackClick: () -> Unit,
    onItemClick: (Long) -> Unit,
    onCategoryOpenSearch: (String) -> Unit = {}
) {
    val allItems by viewModel.allItems.collectAsState()
    val weekAgo = remember { System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000) }

    var focusedCategory by remember { mutableStateOf<String?>(null) }

    val title = when (mode) {
        StatsBrowseMode.AllItems -> "All items"
        StatsBrowseMode.Categories -> if (focusedCategory != null) focusedCategory!! else "Categories"
        StatsBrowseMode.Recent -> "Recent items"
    }
    val subtitle = when (mode) {
        StatsBrowseMode.AllItems -> "${allItems.size} saved"
        StatsBrowseMode.Categories -> {
            if (focusedCategory != null) {
                val n = allItems.count { it.category.equals(focusedCategory, ignoreCase = true) }
                "$n item${if (n == 1) "" else "s"}"
            } else {
                val n = allItems.map { it.category }.filter { it.isNotBlank() }.distinct().size
                "$n categor${if (n == 1) "y" else "ies"}"
            }
        }
        StatsBrowseMode.Recent -> {
            val n = allItems.count { it.dateCreated >= weekAgo }
            "$n from the last 7 days"
        }
    }

    fun handleBack() {
        if (mode == StatsBrowseMode.Categories && focusedCategory != null) {
            focusedCategory = null
        } else {
            onBackClick()
        }
    }

    val listItems: List<Item> = remember(allItems, mode, focusedCategory, weekAgo) {
        when (mode) {
            StatsBrowseMode.AllItems -> allItems
            StatsBrowseMode.Recent -> allItems.filter { it.dateCreated >= weekAgo }
                .sortedByDescending { it.dateCreated }
            StatsBrowseMode.Categories -> {
                val cat = focusedCategory
                if (cat == null) emptyList()
                else allItems.filter { it.category.equals(cat, ignoreCase = true) }
            }
        }
    }

    val categoryRows = remember(allItems) {
        allItems
            .filter { it.category.isNotBlank() }
            .groupBy { it.category }
            .map { (name, items) -> name to items.size }
            .sortedByDescending { it.second }
    }

    PremiumScaffold(
        headerHeight = Dimensions.headerContentWithMenu,
        headerContent = { collapseFraction ->
            val secondaryAlpha = (1f - collapseFraction).coerceIn(0f, 1f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Spacing.xl,
                        end = Spacing.xl,
                        top = Spacing.md,
                        bottom = Spacing.sm
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderIconButton(
                    onClick = { handleBack() },
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
                    iconTint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.alpha(secondaryAlpha)
                )
                Spacer(modifier = Modifier.width(Spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f),
                        modifier = Modifier.alpha(secondaryAlpha)
                    )
                }
            }
        },
        bodyContent = { scrollModifier ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().then(scrollModifier),
                contentPadding = PaddingValues(
                    start = Spacing.xl,
                    end = Spacing.xl,
                    top = Spacing.xl,
                    bottom = Spacing.huge
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                if (mode == StatsBrowseMode.Categories && focusedCategory == null) {
                    if (categoryRows.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Default.Inventory,
                                message = "No categories yet.\nAdd items to see them here."
                            )
                        }
                    } else {
                        items(categoryRows, key = { it.first }) { (name, count) ->
                            CategoryBrowseRow(
                                name = name,
                                count = count,
                                onClick = { focusedCategory = name },
                                onOpenInSearch = { onCategoryOpenSearch(name) }
                            )
                        }
                    }
                } else {
                    if (listItems.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Default.Inventory,
                                message = when (mode) {
                                    StatsBrowseMode.Recent ->
                                        "No items added in the last 7 days."
                                    else -> "No items yet.\nTap + to add your first item."
                                }
                            )
                        }
                    } else {
                        items(listItems, key = { it.id }) { item ->
                            ItemCard(
                                item = item,
                                onClick = { onItemClick(item.id) }
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun CategoryBrowseRow(
    name: String,
    count: Int,
    onClick: () -> Unit,
    onOpenInSearch: () -> Unit
) {
    val accent = categoryAccentBrush(name)
    val dark = isAppDarkTheme()
    val shape = RoundedCornerShape(Dimensions.cardCornerRadius)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
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
                .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon(name),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (count == 1) "1 item" else "$count items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Search",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onOpenInSearch)
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun categoryAccentBrush(category: String): Brush {
    val c = category.lowercase()
    val colors = when {
        c.contains("electronic") -> listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
        c.contains("key") -> listOf(Color(0xFF84CC16), Color(0xFF65A30D))
        c.contains("document") -> listOf(Color(0xFFF97316), Color(0xFFEA580C))
        c.contains("personal") -> listOf(Color(0xFFA78BFA), Color(0xFF7C3AED))
        c.contains("wallet") -> listOf(Color(0xFF14B8A6), Color(0xFF0F766E))
        c.contains("bag") -> listOf(Color(0xFFEC4899), Color(0xFFDB2777))
        else -> listOf(Color(0xFF6366F1), Color(0xFF4F46E5))
    }
    return Brush.linearGradient(colors)
}
