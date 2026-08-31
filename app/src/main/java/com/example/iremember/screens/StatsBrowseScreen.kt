package com.example.iremember.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.iremember.model.Item
import com.example.iremember.ui.components.CategoryChip
import com.example.iremember.ui.components.EmptyState
import com.example.iremember.ui.components.IRememberSearchBar
import com.example.iremember.ui.components.ItemCard
import com.example.iremember.ui.components.PremiumScaffold
import com.example.iremember.ui.components.TabMenuHeader
import com.example.iremember.ui.components.categoryIcon
import com.example.iremember.ui.theme.Dimensions
import com.example.iremember.ui.theme.Spacing
import com.example.iremember.ui.theme.darkCardGradientFill
import com.example.iremember.ui.theme.darkSurfaceBorder
import com.example.iremember.ui.theme.isAppDarkTheme
import com.example.iremember.ui.theme.mainTabBottomScrollPadding
import com.example.iremember.viewmodel.ItemViewModel
import kotlinx.coroutines.launch
import java.util.Locale

enum class StatsBrowseMode {
    AllItems,
    Categories,
    Recent
}

private enum class ItemSort(val label: String) {
    Newest("Newest first"),
    Oldest("Oldest first"),
    NameAsc("Name (A–Z)"),
    RecentlyFound("Recently found")
}

private enum class ItemStatusFilter(val label: String) {
    All("All items"),
    NotFound("Not found yet"),
    Found("Found")
}

private fun Item.matchesQuery(query: String): Boolean {
    if (query.isBlank()) return true
    return listOf(name, location, category, notes).any { field ->
        field.contains(query, ignoreCase = true)
    }
}

private fun itemsLabel(count: Int): String = if (count == 1) "1 item" else "$count items"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsBrowseScreen(
    mode: StatsBrowseMode,
    viewModel: ItemViewModel,
    onBackClick: () -> Unit,
    onItemClick: (Long) -> Unit,
    onCategoryOpenSearch: (String) -> Unit = {},
    onAddItemClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val allItems by viewModel.allItems.collectAsState()
    val weekAgo = remember { System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000) }

    var focusedCategory by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var statusFilter by remember { mutableStateOf(ItemStatusFilter.All) }
    var sortOption by remember { mutableStateOf(ItemSort.Newest) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // --- Voice search (same capability as the Search tab) ---
    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val spoken = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
            ?.trim()
            .orEmpty()
        if (spoken.isNotEmpty()) query = spoken
    }

    fun launchSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say an item name…")
        }
        try {
            isListening = true
            speechLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            isListening = false
            Toast.makeText(context, "Speech recognition not available", Toast.LENGTH_SHORT).show()
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchSpeechRecognition()
        } else {
            Toast.makeText(
                context,
                "Microphone permission is needed for voice search",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun onVoiceSearchClick() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            launchSpeechRecognition()
        } else {
            micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }

    fun deleteWithUndo(item: Item) {
        viewModel.deleteItem(item.id) { ok ->
            if (!ok) return@deleteItem
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "Deleted \"${item.name}\"",
                    actionLabel = "Undo",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.restoreItem(item.id)
                }
            }
        }
    }

    // Categories overview (no focused category) shows the category picker, not an item list.
    val isCategoryPicker = mode == StatsBrowseMode.Categories && focusedCategory == null
    val showCategoryChips = mode == StatsBrowseMode.AllItems || mode == StatsBrowseMode.Recent
    val showAddFab = mode == StatsBrowseMode.AllItems && onAddItemClick != null

    fun handleBack() {
        if (mode == StatsBrowseMode.Categories && focusedCategory != null) {
            focusedCategory = null
            query = ""
            selectedCategory = null
            statusFilter = ItemStatusFilter.All
        } else {
            onBackClick()
        }
    }

    // Inside a drilled-in category, system back returns to the category list first.
    BackHandler(enabled = mode == StatsBrowseMode.Categories && focusedCategory != null) {
        handleBack()
    }

    // Items for the current view before search / chip / status / sort are applied.
    val baseItems: List<Item> = remember(allItems, mode, focusedCategory, weekAgo) {
        when (mode) {
            StatsBrowseMode.AllItems -> allItems
            StatsBrowseMode.Recent -> allItems.filter { it.dateCreated >= weekAgo }
            StatsBrowseMode.Categories -> {
                val cat = focusedCategory
                if (cat == null) emptyList()
                else allItems.filter { it.category.equals(cat, ignoreCase = true) }
            }
        }
    }

    // Category chips are derived from the data that is actually present.
    val availableCategories = remember(baseItems) {
        baseItems.map { it.category }
            .filter { it.isNotBlank() }
            .distinct()
            .sortedBy { it.lowercase() }
    }

    // Keep the selected chip valid if the underlying data changes.
    LaunchedEffect(availableCategories, selectedCategory) {
        if (selectedCategory != null && selectedCategory !in availableCategories) {
            selectedCategory = null
        }
    }

    val displayedItems: List<Item> =
        remember(baseItems, query, selectedCategory, statusFilter, sortOption) {
            var list = baseItems.filter { it.matchesQuery(query) }
            selectedCategory?.let { cat ->
                list = list.filter { it.category.equals(cat, ignoreCase = true) }
            }
            list = when (statusFilter) {
                ItemStatusFilter.All -> list
                ItemStatusFilter.Found -> list.filter { it.lastFoundAt > 0 }
                ItemStatusFilter.NotFound -> list.filter { it.lastFoundAt == 0L }
            }
            when (sortOption) {
                ItemSort.Newest -> list.sortedByDescending { it.dateCreated }
                ItemSort.Oldest -> list.sortedBy { it.dateCreated }
                ItemSort.NameAsc -> list.sortedBy { it.name.lowercase() }
                ItemSort.RecentlyFound -> list.sortedByDescending { it.lastFoundAt }
            }
        }

    val isFiltering = query.isNotBlank() ||
        selectedCategory != null ||
        statusFilter != ItemStatusFilter.All

    fun clearFilters() {
        query = ""
        selectedCategory = null
        statusFilter = ItemStatusFilter.All
    }

    val title = when (mode) {
        StatsBrowseMode.AllItems -> "All items"
        StatsBrowseMode.Categories -> focusedCategory ?: "Categories"
        StatsBrowseMode.Recent -> "Recent items"
    }
    val subtitle = when {
        isCategoryPicker -> {
            val n = allItems.map { it.category }.filter { it.isNotBlank() }.distinct().size
            "$n categor${if (n == 1) "y" else "ies"}"
        }
        isFiltering -> "${displayedItems.size} of ${baseItems.size} shown"
        mode == StatsBrowseMode.AllItems -> "${allItems.size} saved"
        mode == StatsBrowseMode.Recent -> "${baseItems.size} from the last 7 days"
        else -> itemsLabel(baseItems.size)
    }

    val categoryRows = remember(allItems) {
        allItems
            .filter { it.category.isNotBlank() }
            .groupBy { it.category }
            .map { (name, items) -> name to items.size }
            .sortedByDescending { it.second }
    }

    val bottomPadding = mainTabBottomScrollPadding(bottomNavVisible = false) +
        if (showAddFab) 72.dp else 0.dp

    Box(modifier = Modifier.fillMaxSize()) {
        PremiumScaffold(
            headerHeight = Dimensions.headerContentWithMenu,
            headerContent = { collapseFraction ->
                TabMenuHeader(
                    title = title,
                    subtitle = subtitle,
                    onMenuClick = { handleBack() },
                    collapseFraction = collapseFraction,
                    leadingIsBack = true
                )
            },
            bodyContent = { scrollModifier ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().then(scrollModifier),
                    contentPadding = PaddingValues(
                        start = Spacing.xl,
                        end = Spacing.xl,
                        top = Spacing.xl,
                        bottom = bottomPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    if (isCategoryPicker) {
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
                        return@LazyColumn
                    }

                    // --- Search + filter toolbar ---
                    item(key = "toolbar-search") {
                        IRememberSearchBar(
                            query = query,
                            onQueryChange = { query = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = "Search these items…",
                            onFilterClick = { showFilterSheet = true },
                            onVoiceClick = { onVoiceSearchClick() },
                            isListening = isListening
                        )
                    }

                    if (showCategoryChips && availableCategories.isNotEmpty()) {
                        item(key = "toolbar-chips") {
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                            ) {
                                CategoryChip(
                                    category = "All",
                                    selected = selectedCategory == null,
                                    onClick = { selectedCategory = null }
                                )
                                availableCategories.forEach { cat ->
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

                    item(key = "toolbar-summary") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = itemsLabel(displayedItems.size),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isFiltering) {
                                    TextButton(onClick = { clearFilters() }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(Spacing.xxs))
                                        Text(
                                            text = "Clear",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                TextButton(onClick = { showFilterSheet = true }) {
                                    Icon(
                                        imageVector = Icons.Default.SwapVert,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(Spacing.xxs))
                                    Text(
                                        text = sortOption.label,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    if (displayedItems.isEmpty()) {
                        item(key = "empty") {
                            val message = when {
                                query.isNotBlank() -> "No items match \"$query\"."
                                isFiltering -> "No items match the selected filters."
                                mode == StatsBrowseMode.Recent ->
                                    "No items added in the last 7 days."
                                else -> "No items yet.\nTap the + button to add your first item."
                            }
                            EmptyState(
                                icon = Icons.Default.Search,
                                message = message
                            )
                        }
                    } else {
                        items(displayedItems, key = { it.id }) { item ->
                            SwipeToDeleteItem(
                                onDelete = { deleteWithUndo(item) }
                            ) {
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = Spacing.md)
                // Lift above the FAB so the Undo action stays tappable.
                .padding(bottom = if (showAddFab) Spacing.huge + Spacing.xxl else Spacing.sm)
        )

        if (showAddFab) {
            ExtendedFloatingActionButton(
                onClick = { onAddItemClick?.invoke() },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                },
                text = { Text("Add item") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(Spacing.xl)
            )
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
                    .navigationBarsPadding()
                    .padding(horizontal = Spacing.xl)
                    .padding(bottom = Spacing.xl)
            ) {
                Text(
                    text = "Sort by",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(Spacing.sm))
                ItemSort.entries.forEach { option ->
                    SheetOptionRow(
                        label = option.label,
                        selected = sortOption == option,
                        onClick = { sortOption = option }
                    )
                }

                Spacer(Modifier.height(Spacing.md))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(Spacing.md))

                Text(
                    text = "Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(Spacing.sm))
                ItemStatusFilter.entries.forEach { option ->
                    SheetOptionRow(
                        label = option.label,
                        selected = statusFilter == option,
                        onClick = { statusFilter = option }
                    )
                }

                Spacer(Modifier.height(Spacing.md))
                TextButton(
                    onClick = {
                        clearFilters()
                        sortOption = ItemSort.Newest
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reset to defaults")
                }
            }
        }
    }
}

@Composable
private fun SheetOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteItem(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(Dimensions.cardCornerRadius))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = Spacing.xl),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Text(
                        text = "Delete",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete item",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    ) {
        content()
    }
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
