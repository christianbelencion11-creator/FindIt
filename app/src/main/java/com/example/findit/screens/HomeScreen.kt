package com.example.findit.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.example.findit.R
import com.example.findit.model.Item
import com.example.findit.ui.components.CategoryChip
import com.example.findit.ui.components.EmptyState
import com.example.findit.ui.components.FindItSearchBar
import com.example.findit.ui.components.HomeWeatherSheetContent
import com.example.findit.ui.components.ItemCard
import com.example.findit.ui.components.PremiumScaffold
import com.example.findit.ui.components.ProfileAvatar
import com.example.findit.ui.components.StatsSection
import com.example.findit.ui.components.ThemeModeHeaderControl
import com.example.findit.ui.components.WeatherWidget
import com.example.findit.ui.theme.Dimensions
import com.example.findit.ui.theme.Spacing
import com.example.findit.ui.theme.StatGreen
import com.example.findit.ui.theme.ThemeMode
import com.example.findit.ui.theme.darkCardGradientFill
import com.example.findit.ui.theme.darkSurfaceBorder
import com.example.findit.ui.theme.isAppDarkTheme
import com.example.findit.ui.theme.mainTabBottomScrollPadding
import com.example.findit.util.ReminderTimeUtils
import com.example.findit.util.UiPreferences
import com.example.findit.util.WeatherSnapshot
import com.example.findit.util.computeStats
import com.example.findit.util.fetchWeather
import com.example.findit.util.hasLocationPermission
import com.example.findit.util.lastKnownDeviceLocation
import com.example.findit.viewmodel.ItemViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ItemViewModel,
    onSearchClick: () -> Unit,
    onItemClick: (Long) -> Unit,
    embedded: Boolean = false,
    onAddItemClick: () -> Unit = {},
    onEditItem: (Long) -> Unit = {},
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
    bottomNavVisible: Boolean = true,
    onTotalItemsClick: () -> Unit = {},
    onCategoriesClick: () -> Unit = {},
    onRecentClick: () -> Unit = {},
    onVoiceSearchClick: () -> Unit = {},
    onNotesClick: () -> Unit = {}
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
    var showLocalDataWarning by remember {
        mutableStateOf(!uiPreferences.isLocalDataWarningDismissed())
    }
    var showWeatherSheet by remember { mutableStateOf(false) }
    var weather by remember { mutableStateOf<WeatherSnapshot?>(null) }
    var weatherLoading by remember { mutableStateOf(true) }
    var weatherRefreshKey by remember { mutableIntStateOf(0) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        // Once allowed, re-resolve so the weather + label follow the device location.
        if (granted) weatherRefreshKey += 1
    }
    val weatherSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val weatherDateHeadline = remember {
        val now = Calendar.getInstance().time
        val locale = Locale.getDefault()
        SimpleDateFormat("EEEE, d MMMM", locale).format(now)
    }
    val displayedItems = remember(allItems, selectedCategory) {
        val base = if (selectedCategory != null)
            allItems.filter { it.category.contains(selectedCategory!!, ignoreCase = true) }
        else allItems
        base.take(5)
    }

    LaunchedEffect(Unit) {
        // Ask once (ever) so the weather can follow the user's real location.
        if (!uiPreferences.isLocationPermissionAsked() && !hasLocationPermission(context)) {
            uiPreferences.setLocationPermissionAsked(true)
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    LaunchedEffect(weatherRefreshKey) {
        weatherLoading = true
        val device = lastKnownDeviceLocation(context)
        weather = if (device != null) {
            fetchWeather(context, device.latitude, device.longitude)
        } else {
            fetchWeather(context)
        }
        weatherLoading = false
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
                    onVoiceClick = onVoiceSearchClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (showLocalDataWarning) {
                item {
                    LocalDataHomeBanner(
                        onDismiss = {
                            uiPreferences.setLocalDataWarningDismissed(true)
                            showLocalDataWarning = false
                        }
                    )
                }
            }

            item { MascotAssistantCard(stats.totalItems, stats.categories) }

            item {
                StatsSection(
                    stats = stats,
                    onViewAllClick = onTotalItemsClick,
                    onTotalItemsClick = onTotalItemsClick,
                    onCategoriesClick = onCategoriesClick,
                    onRecentClick = onRecentClick
                )
            }

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
                        onEdit = { onEditItem(item.id) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (showWeatherSheet) Modifier.blur(18.dp) else Modifier)
        ) {
            PremiumScaffold(
                headerHeight = Dimensions.headerContentHome,
                onUserInteraction = onUserInteraction,
                headerContent = { collapseFraction ->
                    HomeHeader(
                        displayName = displayName,
                        profileImageUri = profileImageUri,
                        collapseFraction = collapseFraction,
                        weather = weather,
                        weatherLoading = weatherLoading,
                        onMenuClick = onMenuClick,
                        themeMode = themeMode,
                        onThemeModeChanged = onThemeModeChanged,
                        onProfileClick = onProfileClick,
                        onNotificationsClick = onNotificationsClick,
                        onAlertsClick = onAlertsClick,
                        onWeatherClick = { showWeatherSheet = true }
                    )
                },
                bodyContent = body
            )
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (showWeatherSheet) Modifier.blur(18.dp) else Modifier)
        ) {
            PremiumScaffold(
                headerHeight = Dimensions.headerContentHome,
                onUserInteraction = onUserInteraction,
                headerContent = { collapseFraction ->
                    HomeHeader(
                        displayName = displayName,
                        profileImageUri = profileImageUri,
                        collapseFraction = collapseFraction,
                        weather = weather,
                        weatherLoading = weatherLoading,
                        onMenuClick = onMenuClick,
                        themeMode = themeMode,
                        onThemeModeChanged = onThemeModeChanged,
                        onProfileClick = onProfileClick,
                        onNotificationsClick = onNotificationsClick,
                        onAlertsClick = onAlertsClick,
                        onWeatherClick = { showWeatherSheet = true }
                    )
                },
                bodyContent = body
            )
        }
    }

    if (showWeatherSheet) {
        ModalBottomSheet(
            onDismissRequest = { showWeatherSheet = false },
            sheetState = weatherSheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = Color.Transparent,
            scrimColor = Color.Black.copy(alpha = 0.18f),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = Spacing.md, bottom = Spacing.sm)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.55f))
                )
            }
        ) {
            HomeWeatherSheetContent(
                dateHeadline = weatherDateHeadline,
                weather = weather,
                loading = weatherLoading,
                onRefresh = { weatherRefreshKey += 1 },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
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
    weather: WeatherSnapshot?,
    weatherLoading: Boolean,
    onMenuClick: () -> Unit,
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onAlertsClick: () -> Unit,
    onWeatherClick: () -> Unit
) {
    val secondaryAlpha = (1f - collapseFraction).coerceIn(0f, 1f)
    // Cycle the advice line through the tips; remember() survives header recompositions on scroll.
    var adviceIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(4500)
            adviceIndex = (adviceIndex + 1) % homeAdviceTips.size
        }
    }
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
                ThemeModeHeaderControl(
                    themeMode = themeMode,
                    onThemeModeChanged = onThemeModeChanged
                )
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    // Friendly wave — kept outside the ellipsized name so it never gets clipped.
                    Text(
                        text = "👋",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                // Rotating short advice line — never static; fades to the next tip.
                Crossfade(
                    targetState = adviceIndex,
                    animationSpec = tween(durationMillis = 600),
                    label = "homeAdvice",
                    modifier = Modifier
                        .padding(top = Spacing.xxs)
                        .alpha(secondaryAlpha)
                ) { index ->
                    Text(
                        text = homeAdviceTips[index],
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.80f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            WeatherWidget(
                weather = weather,
                loading = weatherLoading,
                onClick = onWeatherClick,
                modifier = Modifier.alpha(secondaryAlpha)
            )
        }
    }
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
        Surface(
            onClick = onProfileClick,
            modifier = Modifier.shadow(6.dp, CircleShape, clip = false),
            shape = CircleShape,
            color = Color.Transparent
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
    Surface(
        onClick = onClick,
        modifier = modifier.size(36.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDesc,
                modifier = Modifier.size(20.dp),
                tint = iconTint
            )
        }
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
    val dark = isAppDarkTheme()
    val shape = RoundedCornerShape(Dimensions.cardCornerRadius)
    Card(
        onClick = onOpenAlerts,
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = if (dark) {
            darkSurfaceBorder()
        } else {
            BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            )
        },
        colors = CardDefaults.cardColors(
            containerColor = if (dark) Color.Transparent else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .darkCardGradientFill(dark)
                .padding(Spacing.lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AttentionIconBadge(
                    icon = Icons.Default.Notifications,
                    tint = Color(0xFFF59E0B),
                    bg = Color(0xFFF59E0B).copy(alpha = 0.18f)
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
                    iconTint = Color(0xFF38BDF8),
                    iconBg = Color(0xFF38BDF8).copy(alpha = 0.18f),
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
                    iconTint = Color(0xFFFB7185),
                    iconBg = Color(0xFFFB7185).copy(alpha = 0.18f),
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
private fun AttentionIconBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    bg: Color
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun AttentionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String?,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    iconBg: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        AttentionIconBadge(icon = icon, tint = iconTint, bg = iconBg)
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

    val dark = isAppDarkTheme()
    val shape = RoundedCornerShape(Dimensions.cardCornerRadius)
    Card(
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
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Image(
                painter = painterResource(R.drawable.findit_character),
                contentDescription = null,
                modifier = Modifier.size(88.dp),
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

@Composable
private fun LocalDataHomeBanner(onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f))
            .padding(Spacing.lg)
    ) {
        Text(
            text = "Items stay on this device only",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Uninstalling IRemember permanently deletes your saved items and history. They are not backed up to the cloud.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Got it")
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

/** Short, friendly one-liners that rotate under the user's name on Home. */
private val homeAdviceTips = listOf(
    "Let's keep your things safe. 💚",
    "Tap any item to see where it is. 📍",
    "A place for everything. 📦",
    "Snap it, save it, find it. 📸",
    "Never lose track of your stuff. 🔒"
)

private fun timeGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning,"
        hour < 17 -> "Good afternoon,"
        else      -> "Good evening,"
    }
}
