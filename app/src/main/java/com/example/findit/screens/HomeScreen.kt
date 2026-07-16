package com.example.findit.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.findit.R
import com.example.findit.model.Item
import com.example.findit.ui.components.CategoryChip
import com.example.findit.ui.components.DraggableFab
import com.example.findit.ui.components.EmptyState
import com.example.findit.ui.components.FindItSearchBar
import com.example.findit.ui.components.ItemCard
import com.example.findit.ui.components.PremiumScaffold
import com.example.findit.ui.components.ProfileAvatar
import com.example.findit.ui.components.StatsSection
import com.example.findit.ui.components.WeatherWidget
import com.example.findit.ui.theme.Dimensions
import com.example.findit.ui.theme.Spacing
import com.example.findit.ui.theme.StatGreen
import com.example.findit.ui.theme.ThemeMode
import com.example.findit.ui.theme.mainTabBottomScrollPadding
import com.example.findit.util.ReminderTimeUtils
import com.example.findit.util.UiPreferences
import com.example.findit.util.computeStats
import com.example.findit.viewmodel.ItemViewModel
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: ItemViewModel,
    onSearchClick: () -> Unit,
    onItemClick: (Long) -> Unit,
    embedded: Boolean = false,
    onAddItemClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    themeMode: ThemeMode = ThemeMode.Auto,
    onThemeModeChanged: (ThemeMode) -> Unit = {},
    profileImageUri: String = "",
    displayName: String = "IRemember User",
    username: String = "iremember_user",
    onNotificationsClick: () -> Unit = {},
    onAlertsClick: () -> Unit = {},
    onLocationClick: (String) -> Unit = {},
    onUserInteraction: () -> Unit = {},
    bottomNavVisible: Boolean = true
) {
    val context = LocalContext.current
    val uiPreferences = remember(context) { UiPreferences(context) }
    val allItems by viewModel.allItems.collectAsState()
    val activeReminders by viewModel.activeReminders.collectAsState()
    val overdueUnfound by viewModel.overdueUnfoundItems.collectAsState()
    val stats = computeStats(allItems)
    val uniqueCategories = remember(allItems) { allItems.map { it.category }.distinct() }
    val locationGroups = remember(allItems) {
        allItems.filter { it.location.isNotBlank() }
            .groupBy { it.location }
            .map { (location, items) -> location to items.size }
            .sortedByDescending { it.second }
    }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var pendingDeleteItem by remember { mutableStateOf<Item?>(null) }
    val displayedItems = remember(allItems, selectedCategory) {
        val base = if (selectedCategory != null)
            allItems.filter { it.category.contains(selectedCategory!!, ignoreCase = true) }
        else allItems
        base.take(5)
    }

    val bottomScrollPadding = mainTabBottomScrollPadding(bottomNavVisible)

    val body: @Composable (Modifier) -> Unit = { scrollModifier ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().then(scrollModifier),
            contentPadding = PaddingValues(
                start = Spacing.xl, end = Spacing.xl,
                top = Spacing.xl, bottom = bottomScrollPadding
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            item {
                FindItSearchBar(
                    query = "",
                    onQueryChange = {},
                    readOnly = true,
                    onClick = onSearchClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { MascotAssistantCard(stats.totalItems, stats.categories) }

            item { StatsSection(stats = stats) }

            if (activeReminders.isNotEmpty() || overdueUnfound.isNotEmpty()) {
                item {
                    NeedsAttentionCard(
                        reminderCount = activeReminders.size,
                        overdueCount = overdueUnfound.size,
                        topReminderName = activeReminders.firstOrNull()?.name,
                        topOverdueName = overdueUnfound.firstOrNull()?.name,
                        onOpenAlerts = onAlertsClick
                    )
                }
            }

            if (locationGroups.isNotEmpty()) {
                item {
                    BrowseByLocation(
                        locations = locationGroups,
                        onLocationClick = onLocationClick
                    )
                }
            }

            if (uniqueCategories.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = "Categories",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = Spacing.sm)
                        )
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
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Recent Items",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (allItems.size > 5) {
                        TextButton(onClick = onSearchClick) {
                            Text(
                                "See all",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (displayedItems.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.Inventory,
                        message = "No items yet.\nTap + to add your first item."
                    )
                }
            } else {
                items(displayedItems, key = { it.id }) { item ->
                    ItemCard(
                        item = item,
                        onClick = { onItemClick(item.id) },
                        onDeleteFound = if (item.lastFoundAt > 0L) {
                            { pendingDeleteItem = item }
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }

    pendingDeleteItem?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDeleteItem = null },
            title = { Text("Remove found item?") },
            text = {
                Text(
                    "“${item.name}” was already found. Remove it from your list? " +
                        "Found items are also cleared automatically after 30 days."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteItem(item.id)
                        pendingDeleteItem = null
                    }
                ) {
                    Text(
                        text = "Remove",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteItem = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (embedded) {
        Box(modifier = Modifier.fillMaxSize()) {
            PremiumScaffold(
                headerHeight = Dimensions.headerContentHome,
                onUserInteraction = onUserInteraction,
                headerContent = { collapseFraction ->
                    HomeHeader(
                        displayName = displayName,
                        profileImageUri = profileImageUri,
                        collapseFraction = collapseFraction,
                        onMenuClick = onMenuClick,
                        themeMode = themeMode,
                        onThemeModeChanged = onThemeModeChanged,
                        onProfileClick = onProfileClick,
                        onNotificationsClick = onNotificationsClick,
                        onAlertsClick = onAlertsClick
                    )
                },
                bodyContent = body
            )
            DraggableFab(
                onClick = onAddItemClick,
                uiPreferences = uiPreferences
            )
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            PremiumScaffold(
                headerHeight = Dimensions.headerContentHome,
                onUserInteraction = onUserInteraction,
                headerContent = { collapseFraction ->
                    HomeHeader(
                        displayName = displayName,
                        profileImageUri = profileImageUri,
                        collapseFraction = collapseFraction,
                        onMenuClick = onMenuClick,
                        themeMode = themeMode,
                        onThemeModeChanged = onThemeModeChanged,
                        onProfileClick = onProfileClick,
                        onNotificationsClick = onNotificationsClick,
                        onAlertsClick = onAlertsClick
                    )
                },
                bodyContent = body
            )
            DraggableFab(
                onClick = onAddItemClick,
                uiPreferences = uiPreferences
            )
        }
    }
}

// ─── Header ───────────────────────────────────────────────────────────────────

@Composable
private fun HomeHeader(
    displayName: String,
    profileImageUri: String,
    collapseFraction: Float,
    onMenuClick: () -> Unit,
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onAlertsClick: () -> Unit
) {
    val secondaryAlpha = (1f - collapseFraction).coerceIn(0f, 1f)
    var themeMenuExpanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xl, vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                modifier = Modifier.alpha(secondaryAlpha)
            ) {
                HeaderIconButton(
                    icon = Icons.Default.Menu,
                    contentDesc = "Menu",
                    onClick = onMenuClick
                )
                Box {
                    HeaderThemeButton(
                        themeMode = themeMode,
                        onClick = { themeMenuExpanded = true }
                    )
                    DropdownMenu(
                        expanded = themeMenuExpanded,
                        onDismissRequest = { themeMenuExpanded = false }
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = themeModeIcon(mode),
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    onThemeModeChanged(mode)
                                    themeMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            HeaderActionsRow(
                profileImageUri = profileImageUri,
                onProfileClick = onProfileClick,
                onNotificationsClick = onNotificationsClick,
                onAlertsClick = onAlertsClick,
                modifier = Modifier.alpha(secondaryAlpha)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = Spacing.md)
            ) {
                Text(
                    text = timeGreeting(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.84f),
                    modifier = Modifier.alpha(secondaryAlpha)
                )
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            WeatherWidget(modifier = Modifier.alpha(secondaryAlpha))
        }
    }
}

@Composable
private fun HeaderThemeButton(
    themeMode: ThemeMode,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = themeModeIcon(themeMode),
            contentDescription = "Theme mode",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

private fun themeModeIcon(mode: ThemeMode) = when (mode) {
    ThemeMode.Light -> Icons.Default.LightMode
    ThemeMode.Dark -> Icons.Default.DarkMode
    ThemeMode.Auto -> Icons.Default.BrightnessAuto
}

@Composable
private fun HeaderActionsRow(
    profileImageUri: String,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onAlertsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        HeaderIconButton(
            icon = Icons.AutoMirrored.Filled.Article,
            contentDesc = "News",
            onClick = onNotificationsClick
        )
        HeaderIconButton(
            icon = Icons.Default.Notifications,
            contentDesc = "Alerts",
            onClick = onAlertsClick,
            iconTint = StatGreen
        )
        Spacer(Modifier.size(Spacing.xs))
        Box(
            modifier = Modifier
                .shadow(6.dp, CircleShape, clip = false)
                .clip(CircleShape)
                .clickable(onClick = onProfileClick)
        ) {
            ProfileAvatar(
                imageUri = profileImageUri,
                onImageSelected = {},
                size = 38.dp,
                editable = false
            )
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDesc: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.onPrimary
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            modifier = Modifier.size(20.dp),
            tint = iconTint
        )
    }
}

// ─── Needs attention (reminders + overdue) ────────────────────────────────────

@Composable
private fun NeedsAttentionCard(
    reminderCount: Int,
    overdueCount: Int,
    topReminderName: String?,
    topOverdueName: String?,
    onOpenAlerts: () -> Unit
) {
    Card(
        onClick = onOpenAlerts,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimensions.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    text = "Needs attention",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Alerts",
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
            Spacer(Modifier.height(Spacing.md))
            if (reminderCount > 0) {
                AttentionRow(
                    icon = Icons.Default.Alarm,
                    title = if (reminderCount == 1) "1 active reminder" else "$reminderCount active reminders",
                    subtitle = topReminderName?.let { "Next up: $it" }
                        ?: "Open Alerts to snooze or stop"
                )
            }
            if (reminderCount > 0 && overdueCount > 0) {
                Spacer(Modifier.height(Spacing.sm))
            }
            if (overdueCount > 0) {
                AttentionRow(
                    icon = Icons.Default.SearchOff,
                    title = if (overdueCount == 1) {
                        "1 item still missing"
                    } else {
                        "$overdueCount items still missing"
                    },
                    subtitle = topOverdueName?.let {
                        "$it · not Found for ${ReminderTimeUtils.OVERDUE_FOUND_DAYS}+ days"
                    } ?: "Mark Found when you locate them"
                )
            }
        }
    }
}

@Composable
private fun AttentionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ─── Mascot assistant card (Tarsi-style) ────────────────────────────────────────

@Composable
private fun MascotAssistantCard(totalItems: Int, categories: Int) {
    val message = if (totalItems > 0) {
        "You have $totalItems items saved across $categories categories. Tap any item to see exactly where it is."
    } else {
        "Add your first item and I'll remember where it belongs."
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimensions.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Image(
                painter = painterResource(R.drawable.findit_character),
                contentDescription = null,
                modifier = Modifier.size(76.dp),
                contentScale = ContentScale.Fit
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "IRemember",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xxs)
                )
            }
        }
    }
}

// ─── Browse by location ─────────────────────────────────────────────────────────

@Composable
private fun BrowseByLocation(
    locations: List<Pair<String, Int>>,
    onLocationClick: (String) -> Unit
) {
    Column {
        Text(
            text = "Browse by Location",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = Spacing.sm)
        )
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            locations.forEach { (location, count) ->
                LocationCard(
                    location = location,
                    count = count,
                    onClick = { onLocationClick(location) }
                )
            }
        }
    }
}

@Composable
private fun LocationCard(
    location: String,
    count: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(Dimensions.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = location,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (count == 1) "1 item" else "$count items",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun timeGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning,"
        hour < 17 -> "Good afternoon,"
        else      -> "Good evening,"
    }
}
