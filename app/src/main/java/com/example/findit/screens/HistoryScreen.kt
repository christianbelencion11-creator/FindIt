package com.example.findit.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.findit.model.HistoryAction
import com.example.findit.model.ItemHistory
import com.example.findit.ui.components.EmptyState
import com.example.findit.ui.components.HeaderIconButton
import com.example.findit.ui.components.PremiumScaffold
import com.example.findit.ui.theme.Dimensions
import com.example.findit.ui.theme.Spacing
import com.example.findit.ui.theme.darkCardGradientFill
import com.example.findit.ui.theme.darkSurfaceBorder
import com.example.findit.ui.theme.isAppDarkTheme
import com.example.findit.ui.theme.mainTabBottomScrollPadding
import com.example.findit.util.formatDateTime
import com.example.findit.util.formatRelativeTime
import com.example.findit.viewmodel.ItemViewModel
import java.util.Calendar

private enum class HistoryFilter(val label: String) {
    All("All"),
    Deleted("Recent deleted"),
    Found("Recent found"),
    Edited("Recent edited")
}

/** Section buckets for a date-grouped activity feed. */
private val historySectionLabels = listOf("Today", "Yesterday", "Earlier this week", "Earlier")

private fun historySectionIndex(timestamp: Long, startOfToday: Long): Int {
    val day = 86_400_000L
    return when {
        timestamp >= startOfToday -> 0
        timestamp >= startOfToday - day -> 1
        timestamp >= startOfToday - 6 * day -> 2
        else -> 3
    }
}

private data class HistoryActionStyle(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

@Composable
private fun rememberActionStyle(action: HistoryAction): HistoryActionStyle {
    val dark = isAppDarkTheme()
    return when (action) {
        HistoryAction.FOUND -> HistoryActionStyle(
            "Found", Icons.Outlined.TaskAlt,
            if (dark) Color(0xFF4ADE80) else Color(0xFF16A34A)
        )
        HistoryAction.EDITED -> HistoryActionStyle(
            "Edited", Icons.Outlined.Edit,
            if (dark) Color(0xFF60A5FA) else Color(0xFF2563EB)
        )
        HistoryAction.DELETED -> HistoryActionStyle(
            "Deleted", Icons.Outlined.DeleteOutline,
            if (dark) Color(0xFFF87171) else Color(0xFFDC2626)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: ItemViewModel,
    onBackClick: () -> Unit,
    onItemClick: (Long) -> Unit = {},
    onUserInteraction: () -> Unit = {},
    bottomNavVisible: Boolean = true
) {
    val context = LocalContext.current
    val entries by viewModel.historyEntries.collectAsState()
    val allItems by viewModel.allItems.collectAsState()
    val existingIds = remember(allItems) { allItems.map { it.id }.toSet() }
    var filter by remember { mutableStateOf(HistoryFilter.All) }
    var detailEntry by remember { mutableStateOf<ItemHistory?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }

    val now = remember(entries) { System.currentTimeMillis() }
    val startOfToday = remember(now) {
        Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val filtered = remember(entries, filter) {
        when (filter) {
            HistoryFilter.All -> entries
            HistoryFilter.Deleted -> entries.filter { it.action == HistoryAction.DELETED }
            HistoryFilter.Found -> entries.filter { it.action == HistoryAction.FOUND }
            HistoryFilter.Edited -> entries.filter { it.action == HistoryAction.EDITED }
        }
    }

    // Preserve DESC order within each date bucket; iterate buckets Today -> Earlier.
    val grouped = remember(filtered, startOfToday) {
        filtered.groupBy { historySectionIndex(it.createdAt, startOfToday) }.toSortedMap()
    }

    fun isLive(entry: ItemHistory) = entry.itemId > 0L && entry.itemId in existingIds

    fun openEntry(entry: ItemHistory) {
        if (isLive(entry)) onItemClick(entry.itemId) else detailEntry = entry
    }

    fun restore(entry: ItemHistory) {
        viewModel.restoreItem(entry.itemId) { ok ->
            if (!ok) {
                Toast.makeText(
                    context,
                    "This item can no longer be restored.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val bottomScrollPadding = mainTabBottomScrollPadding(bottomNavVisible)

    PremiumScaffold(
        onUserInteraction = onUserInteraction,
        headerHeight = Dimensions.headerContentWithMenu,
        headerContent = { collapseFraction ->
            val secondaryAlpha = (1f - collapseFraction).coerceIn(0f, 1f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Spacing.xl, end = Spacing.xl, top = Spacing.md, bottom = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderIconButton(
                    onClick = onBackClick,
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
                    iconTint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.alpha(secondaryAlpha)
                )
                Spacer(modifier = Modifier.width(Spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Found, edited, and deleted items",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f * secondaryAlpha),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.alpha(secondaryAlpha)
                    )
                }
                if (entries.isNotEmpty()) {
                    HeaderIconButton(
                        onClick = { showClearConfirm = true },
                        icon = Icons.Outlined.DeleteSweep,
                        contentDescription = "Clear history",
                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
                        iconTint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.alpha(secondaryAlpha)
                    )
                }
            }
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
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item(key = "notice") {
                HistoryRetentionNotice()
            }
            item(key = "filters") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    HistoryFilter.entries.forEach { option ->
                        FilterChip(
                            selected = filter == option,
                            onClick = { filter = option },
                            label = { Text(option.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        icon = Icons.Outlined.History,
                        message = when (filter) {
                            HistoryFilter.All ->
                                "No history yet. Found, edited, and deleted items will show up here."
                            HistoryFilter.Deleted -> "No recently deleted items."
                            HistoryFilter.Found -> "No recently found items."
                            HistoryFilter.Edited -> "No recently edited items."
                        }
                    )
                }
            } else {
                grouped.forEach { (sectionIndex, sectionEntries) ->
                    item(key = "section-$sectionIndex") {
                        HistorySectionHeader(
                            label = historySectionLabels[sectionIndex],
                            count = sectionEntries.size
                        )
                    }
                    items(sectionEntries, key = { it.id }) { entry ->
                        val live = isLive(entry)
                        val restorable = !live && entry.action == HistoryAction.DELETED
                        HistoryEntryCard(
                            entry = entry,
                            now = now,
                            isLive = live,
                            onClick = { openEntry(entry) },
                            onRestore = if (restorable) {
                                { restore(entry) }
                            } else {
                                null
                            }
                        )
                    }
                }
            }
        }
    }

    detailEntry?.let { entry ->
        HistoryDetailSheet(
            entry = entry,
            now = now,
            isLive = isLive(entry),
            onViewItem = {
                val id = entry.itemId
                detailEntry = null
                onItemClick(id)
            },
            onRestore = {
                restore(entry)
                detailEntry = null
            },
            onRemove = {
                viewModel.deleteHistoryEntry(entry.id)
                detailEntry = null
            },
            onDismiss = { detailEntry = null }
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.DeleteSweep,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Clear all history?") },
            text = {
                Text(
                    "This removes every activity record. Your saved items and deleted items are not affected."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showClearConfirm = false
                    }
                ) {
                    Text("Clear all", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun HistorySectionHeader(label: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.sm, bottom = Spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                .padding(horizontal = Spacing.sm, vertical = 1.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun HistoryRetentionNotice() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
            .padding(horizontal = Spacing.lg, vertical = Spacing.md)
    ) {
        Text(
            text = "History and soft-deleted items are automatically cleared after 30 days.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun HistoryEntryCard(
    entry: ItemHistory,
    now: Long,
    isLive: Boolean,
    onClick: () -> Unit,
    onRestore: (() -> Unit)?
) {
    val dark = isAppDarkTheme()
    val style = rememberActionStyle(entry.action)
    val shape = RoundedCornerShape(16.dp)
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = if (dark) Color.Transparent else MaterialTheme.colorScheme.surface,
        border = if (dark) darkSurfaceBorder() else null,
        tonalElevation = if (dark) 0.dp else 1.dp,
        shadowElevation = if (dark) 0.dp else Dimensions.cardElevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .darkCardGradientFill(dark)
                .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(style.color.copy(alpha = if (dark) 0.20f else 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = null,
                    tint = style.color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Text(
                        text = style.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = style.color
                    )
                    Text(
                        text = "·",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatRelativeTime(entry.createdAt, now),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.itemName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (entry.detail.isNotBlank()) {
                    Text(
                        text = entry.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(Spacing.sm))
            if (onRestore != null) {
                IconButton(onClick = onRestore) {
                    Icon(
                        imageVector = Icons.Outlined.Restore,
                        contentDescription = "Restore item",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (isLive) 0.9f else 0.45f
                    ),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryDetailSheet(
    entry: ItemHistory,
    now: Long,
    isLive: Boolean,
    onViewItem: () -> Unit,
    onRestore: () -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val style = rememberActionStyle(entry.action)
    val dark = isAppDarkTheme()
    val canRestore = !isLive && entry.action == HistoryAction.DELETED

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Spacing.xl)
                .padding(bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(style.color.copy(alpha = if (dark) 0.22f else 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = style.icon,
                        contentDescription = null,
                        tint = style.color,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = style.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = style.color
                    )
                    Text(
                        text = entry.itemName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HistoryDetailRow(label = "When", value = formatDateTime(entry.createdAt))
            if (entry.detail.isNotBlank()) {
                HistoryDetailRow(
                    label = when (entry.action) {
                        HistoryAction.DELETED -> "Last location"
                        HistoryAction.FOUND -> "Found at"
                        HistoryAction.EDITED -> "Details"
                    },
                    value = entry.detail
                )
            }

            val availability = when {
                isLive -> "This item is still in your inventory."
                canRestore -> "This item was deleted. You can restore it within 30 days."
                else -> "This item is no longer available."
            }
            Text(
                text = availability,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            when {
                isLive -> Button(
                    onClick = onViewItem,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = Spacing.sm)
                            .size(20.dp)
                    )
                    Text(
                        text = "View item details",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                canRestore -> Button(
                    onClick = onRestore,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Restore,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = Spacing.sm)
                            .size(20.dp)
                    )
                    Text(
                        text = "Restore item",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            OutlinedButton(
                onClick = onRemove,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = Spacing.sm)
                        .size(20.dp)
                )
                Text(
                    text = "Remove from history",
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Composable
private fun HistoryDetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
