package com.example.findit.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.findit.ui.components.CategoryChip
import com.example.findit.ui.components.EmptyState
import com.example.findit.ui.components.FindItSearchBar
import com.example.findit.ui.components.ItemCard
import com.example.findit.ui.components.PremiumScaffold
import com.example.findit.ui.theme.Spacing
import com.example.findit.viewmodel.ItemViewModel

@Composable
fun SearchScreen(
    viewModel: ItemViewModel,
    onItemClick: (Long) -> Unit,
    onBackClick: () -> Unit = {},
    embedded: Boolean = false
) {
    val query by viewModel.searchQuery.collectAsState()
    val filteredItems by viewModel.searchResults.collectAsState()
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val displayedItems = remember(filteredItems, selectedCategory) {
        if (selectedCategory != null)
            filteredItems.filter { it.category.contains(selectedCategory!!, ignoreCase = true) }
        else filteredItems
    }

    val uniqueCategories = remember(filteredItems) {
        filteredItems.map { it.category }.distinct()
    }

    PremiumScaffold(
        headerContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xl, vertical = Spacing.xxl)
            ) {
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "Find your saved items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Spacing.xl, end = Spacing.xl,
                top = Spacing.xl, bottom = Spacing.xl
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            // Search field
            item {
                FindItSearchBar(
                    query = query,
                    onQueryChange = viewModel::updateSearchQuery,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Category filter chips
            if (uniqueCategories.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        uniqueCategories.forEach { cat ->
                            CategoryChip(
                                category = cat,
                                selected = selectedCategory == cat,
                                onClick = {
                                    selectedCategory =
                                        if (selectedCategory == cat) null else cat
                                }
                            )
                        }
                    }
                }
            }

            if (displayedItems.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.Search,
                        message = if (query.isBlank()) "Search for any item by name, location, or category."
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
}
