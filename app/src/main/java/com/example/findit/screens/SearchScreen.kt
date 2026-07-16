package com.example.findit.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.findit.model.Item
import com.example.findit.ui.components.CategoryChip
import com.example.findit.ui.components.EmptyState
import com.example.findit.ui.components.FindItSearchBar
import com.example.findit.ui.components.ItemCard
import com.example.findit.ui.components.PremiumScaffold
import com.example.findit.ui.components.TabMenuHeader
import com.example.findit.ui.theme.Dimensions
import com.example.findit.ui.theme.Spacing
import com.example.findit.ui.theme.mainTabBottomScrollPadding
import com.example.findit.viewmodel.ItemViewModel

private val searchCategories = listOf(
    "Electronics", "Keys", "Documents", "Wallet", "Bags", "Others"
)

private enum class SearchStatusFilter {
    All,
    Found,
    NotFound
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: ItemViewModel,
    onItemClick: (Long) -> Unit,
    onBackClick: () -> Unit = {},
    embedded: Boolean = false,
    onMenuClick: () -> Unit = {},
    onUserInteraction: () -> Unit = {},
    bottomNavVisible: Boolean = true
) {
    val query by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val allItems by viewModel.allItems.collectAsState()

    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var statusFilter by remember { mutableStateOf(SearchStatusFilter.All) }
    var showAllRecent by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val baseItems: List<Item> = remember(query, searchResults, allItems) {
        if (query.isBlank()) allItems.sortedByDescending { it.dateCreated }
        else searchResults
    }

    val displayedItems = remember(baseItems, selectedCategory, statusFilter, showAllRecent, query) {
        var list = baseItems
        if (selectedCategory != null) {
            list = list.filter { it.category.contains(selectedCategory!!, ignoreCase = true) }
        }
        list = when (statusFilter) {
            SearchStatusFilter.All -> list
            SearchStatusFilter.Found -> list.filter { it.lastFoundAt > 0 }
            SearchStatusFilter.NotFound -> list.filter { it.lastFoundAt == 0L }
        }
        if (query.isBlank() && !showAllRecent) list.take(10) else list
    }

    val bottomScrollPadding = mainTabBottomScrollPadding(bottomNavVisible)
    val sectionTitle = if (query.isBlank()) "Recent Items" else "Results"
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    PremiumScaffold(
        onUserInteraction = onUserInteraction,
        headerHeight = Dimensions.headerContentWithMenu,
        headerContent = { collapseFraction ->
            TabMenuHeader(
                title = "Search",
                subtitle = "Find your saved items",
                onMenuClick = onMenuClick,
                collapseFraction = collapseFraction
            )
        }
    ) { scrollModifier ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .then(scrollModifier),
            contentPadding = PaddingValues(
                start = Spacing.xl,
                end = Spacing.xl,
                top = Spacing.xl,
                bottom = bottomScrollPadding
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    FindItSearchBar(
                        query = query,
                        onQueryChange = {
                            viewModel.updateSearchQuery(it)
                            showAllRecent = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        onFilterClick = { showFilterSheet = true }
                    )
                    if (query.isNotBlank()) {
                        Text(
                            text = "${displayedItems.size} match${if (displayedItems.size == 1) "" else "es"} — tap to see where it is",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        CategoryChip(
                            category = "All",
                            selected = selectedCategory == null,
                            onClick = {
                                selectedCategory = null
                                showAllRecent = false
                            }
                        )
                        searchCategories.forEach { cat ->
                            CategoryChip(
                                category = cat,
                                selected = selectedCategory == cat,
                                onClick = {
                                    selectedCategory = if (selectedCategory == cat) null else cat
                                    showAllRecent = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sectionTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (query.isBlank() && !showAllRecent && baseItems.size > 10) {
                        TextButton(onClick = { showAllRecent = true }) {
                            Text(
                                text = "View all",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge
                            )
                            androidx.compose.material3.Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (displayedItems.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.Search,
                        message = if (query.isBlank())
                            "No items yet. Add something so you can find it later."
                        else "No items found for \"$query\""
                    )
                }
            } else {
                items(displayedItems, key = { it.id }) { item ->
                    ItemCard(item = item, onClick = { onItemClick(item.id) })
                }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xl, vertical = Spacing.md)
            ) {
                Text(
                    text = "Filter by status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(Spacing.md))
                SearchStatusFilter.entries.forEach { option ->
                    val label = when (option) {
                        SearchStatusFilter.All -> "All items"
                        SearchStatusFilter.Found -> "Found"
                        SearchStatusFilter.NotFound -> "Not found / Missing"
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = statusFilter == option,
                            onClick = {
                                statusFilter = option
                                showFilterSheet = false
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = Spacing.sm)
                        )
                    }
                }
                Spacer(Modifier.height(Spacing.xl))
            }
        }
    }
}
