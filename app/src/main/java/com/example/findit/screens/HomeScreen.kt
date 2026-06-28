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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.findit.R
import com.example.findit.ui.components.CategoryChip
import com.example.findit.ui.components.EmptyState
import com.example.findit.ui.components.FindItSearchBar
import com.example.findit.ui.components.ItemCard
import com.example.findit.ui.components.PremiumScaffold
import com.example.findit.ui.components.ProfileAvatar
import com.example.findit.ui.components.StatsSection
import com.example.findit.ui.components.WeatherWidget
import com.example.findit.ui.theme.Dimensions
import com.example.findit.ui.theme.Spacing
import com.example.findit.util.computeStats
import com.example.findit.viewmodel.ItemViewModel
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: ItemViewModel,
    onSearchClick: () -> Unit,
    onItemClick: (Long) -> Unit,
    embedded: Boolean = false,
    onAddItemClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    profileImageUri: String = "",
    displayName: String = "FindIt User",
    username: String = "findit_user",
    onNotificationsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onLocationClick: (String) -> Unit = {}
) {
    val allItems by viewModel.allItems.collectAsState()
    val stats = computeStats(allItems)
    val uniqueCategories = remember(allItems) { allItems.map { it.category }.distinct() }
    val locationGroups = remember(allItems) {
        allItems.filter { it.location.isNotBlank() }
            .groupBy { it.location }
            .map { (location, items) -> location to items.size }
            .sortedByDescending { it.second }
    }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val displayedItems = remember(allItems, selectedCategory) {
        val base = if (selectedCategory != null)
            allItems.filter { it.category.contains(selectedCategory!!, ignoreCase = true) }
        else allItems
        base.take(5)
    }

    val body: @Composable () -> Unit = {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Spacing.xl, end = Spacing.xl,
                top = Spacing.xl, bottom = 96.dp
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
                    ItemCard(item = item, onClick = { onItemClick(item.id) })
                }
            }
        }
    }

    if (embedded) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = onAddItemClick,
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add item",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            ) { _ ->
                PremiumScaffold(
                    headerHeight = Dimensions.headerContentHome,
                    headerContent = {
                        HomeHeader(
                            displayName = displayName,
                            profileImageUri = profileImageUri,
                            onProfileClick = onProfileClick,
                            onNotificationsClick = onNotificationsClick,
                            onSettingsClick = onSettingsClick
                        )
                    },
                    bodyContent = body
                )
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            PremiumScaffold(
                headerHeight = Dimensions.headerContentHome,
                headerContent = {
                    HomeHeader(
                        displayName = displayName,
                        profileImageUri = profileImageUri,
                        onProfileClick = onProfileClick,
                        onNotificationsClick = onNotificationsClick,
                        onSettingsClick = onSettingsClick
                    )
                },
                bodyContent = body
            )
        }
    }
}

// ─── Header ───────────────────────────────────────────────────────────────────

@Composable
private fun HomeHeader(
    displayName: String,
    profileImageUri: String,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    // Simple vertical stack — date row, then greeting. No absolute positioning,
    // so nothing can overlap regardless of header height.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xl, vertical = Spacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = currentDateString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                HeaderIconButton(
                    icon = Icons.Default.Notifications,
                    contentDesc = "Notifications",
                    onClick = onNotificationsClick
                )
                HeaderIconButton(
                    icon = Icons.Default.Settings,
                    contentDesc = "Settings",
                    onClick = onSettingsClick
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

        Spacer(Modifier.height(Spacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = timeGreeting(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.84f)
                )
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            WeatherWidget()
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDesc: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
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
            tint = MaterialTheme.colorScheme.onPrimary
        )
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
                    text = "FindIt",
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
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
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
            .width(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(Dimensions.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = location,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (count == 1) "1 item" else "$count items",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun currentDateString(): String {
    val cal = Calendar.getInstance()
    val day = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH)
        ?.uppercase() ?: ""
    val month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH) ?: ""
    val date = cal.get(Calendar.DAY_OF_MONTH)
    return "$day, $month $date"
}

private fun timeGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning,"
        hour < 17 -> "Good afternoon,"
        else      -> "Good evening,"
    }
}
