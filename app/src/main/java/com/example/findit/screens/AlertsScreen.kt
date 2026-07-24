package com.example.findit.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.findit.model.Item
import com.example.findit.ui.components.EmptyState
import com.example.findit.ui.components.PremiumScaffold
import com.example.findit.ui.components.TabMenuHeader
import com.example.findit.ui.theme.Dimensions
import com.example.findit.ui.theme.Spacing
import com.example.findit.ui.theme.darkCardGradientFill
import com.example.findit.ui.theme.darkSurfaceBorder
import com.example.findit.ui.theme.isAppDarkTheme
import com.example.findit.ui.theme.mainTabBottomScrollPadding
import com.example.findit.util.ReminderTimeUtils
import com.example.findit.util.formatDateTime
import com.example.findit.viewmodel.ItemViewModel

@Composable
fun AlertsScreen(
    viewModel: ItemViewModel,
    onItemClick: (Long) -> Unit,
    onMenuClick: () -> Unit = {},
    onUserInteraction: () -> Unit = {},
    bottomNavVisible: Boolean = true
) {
    val reminders by viewModel.activeReminders.collectAsState()
    val overdue by viewModel.overdueUnfoundItems.collectAsState()
    val bottomPadding = mainTabBottomScrollPadding(bottomNavVisible)
    val isEmpty = reminders.isEmpty() && overdue.isEmpty()

    PremiumScaffold(
        onUserInteraction = onUserInteraction,
        headerHeight = Dimensions.headerContentWithMenu,
        headerContent = { collapseFraction ->
            TabMenuHeader(
                title = "Alerts",
                subtitle = "Reminders & pending found check-ins",
                onMenuClick = onMenuClick,
                collapseFraction = collapseFraction
            )
        }
    ) { scrollModifier ->
        if (isEmpty) {
            EmptyState(
                icon = Icons.Default.Notifications,
                message = "No alerts yet. Turn on Remind Me on an item, or check back for overdue finds.",
                modifier = Modifier
                    .fillMaxSize()
                    .then(scrollModifier)
                    .padding(Spacing.xl)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .then(scrollModifier),
                contentPadding = PaddingValues(
                    start = Spacing.xl,
                    end = Spacing.xl,
                    top = Spacing.lg,
                    bottom = bottomPadding
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                if (reminders.isNotEmpty()) {
                    item {
                        Text(
                            text = "Active reminders",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    items(reminders, key = { "r_${it.id}" }) { item ->
                        ReminderAlertCard(
                            item = item,
                            onOpen = { onItemClick(item.id) },
                            onSnooze = { viewModel.snoozeReminder(item.id) },
                            onStop = { viewModel.stopReminder(item.id) }
                        )
                    }
                }
                if (overdue.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(Spacing.sm))
                        Text(
                            text = "Still looking?",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Items not marked Found for ${ReminderTimeUtils.OVERDUE_FOUND_DAYS}+ days",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = Spacing.xxs, bottom = Spacing.sm)
                        )
                    }
                    items(overdue, key = { "o_${it.id}" }) { item ->
                        OverdueAlertCard(
                            item = item,
                            onOpen = { onItemClick(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderAlertCard(
    item: Item,
    onOpen: () -> Unit,
    onSnooze: () -> Unit,
    onStop: () -> Unit
) {
    val dark = isAppDarkTheme()
    val shape = RoundedCornerShape(Dimensions.cardCornerRadius)
    Card(
        onClick = onOpen,
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .darkCardGradientFill(dark)
                .padding(Spacing.lg)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Left at ${item.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (item.remindNextAt > 0L) {
                        Text(
                            text = "Next notify: ${formatDateTime(item.remindNextAt)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = Spacing.xxs)
                        )
                    }
                }
            }
            Spacer(Modifier.height(Spacing.md))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OutlinedButton(
                    onClick = onSnooze,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Snooze 1h")
                }
                Button(
                    onClick = onStop,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Stop")
                }
            }
        }
    }
}

@Composable
private fun OverdueAlertCard(
    item: Item,
    onOpen: () -> Unit
) {
    val dark = isAppDarkTheme()
    val shape = RoundedCornerShape(Dimensions.cardCornerRadius)
    Card(
        onClick = onOpen,
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
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Haven't confirmed Found yet · ${item.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Saved ${formatDateTime(item.dateCreated)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xxs)
                )
            }
            Text(
                text = "Open",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
