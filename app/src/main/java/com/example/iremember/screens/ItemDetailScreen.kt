package com.example.iremember.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.iremember.ui.components.FoundConfirmDialog
import com.example.iremember.ui.components.FoundSuccessDialog
import com.example.iremember.ui.components.HeaderIconButton
import com.example.iremember.ui.components.PremiumScaffold
import com.example.iremember.ui.components.RemindMeCard
import com.example.iremember.ui.components.ZoomableImageViewer
import com.example.iremember.ui.components.categoryIcon
import com.example.iremember.ui.theme.Dimensions
import com.example.iremember.ui.theme.IRememberGreen
import com.example.iremember.ui.theme.IRememberGreenDeep
import com.example.iremember.ui.theme.IRememberGreenLight
import com.example.iremember.ui.theme.Spacing
import com.example.iremember.ui.theme.StatBlue
import com.example.iremember.ui.theme.StatBlueDark
import com.example.iremember.util.formatDateTime
import com.example.iremember.viewmodel.ItemViewModel
@Composable
fun ItemDetailScreen(
    viewModel: ItemViewModel,
    itemId: Long,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit = {}
) {
    val item by viewModel.itemById(itemId).collectAsState(initial = null)
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showImageViewer by remember { mutableStateOf(false) }
    var draftHour by remember(item?.id, item?.remindHour) {
        mutableIntStateOf(item?.remindHour ?: 8)
    }
    var draftMinute by remember(item?.id, item?.remindMinute) {
        mutableIntStateOf(item?.remindMinute ?: 0)
    }

    if (showImageViewer && item != null && item!!.imageUri.isNotEmpty()) {
        ZoomableImageViewer(
            imageUri = item!!.imageUri,
            contentDescription = item!!.name,
            onDismiss = { showImageViewer = false }
        )
    }

    if (showConfirmDialog && item != null && item!!.lastFoundAt == 0L) {
        FoundConfirmDialog(
            itemName = item!!.name,
            location = item!!.location,
            onConfirm = {
                showConfirmDialog = false
                viewModel.markItemFound(itemId) { success ->
                    if (success) showSuccessDialog = true
                }
            },
            onCancel = { showConfirmDialog = false }
        )
    }

    if (showSuccessDialog && item != null) {
        FoundSuccessDialog(
            itemName = item!!.name,
            location = item!!.location,
            onDone = {
                showSuccessDialog = false
                onBackClick()
            },
            onFindAnother = { showSuccessDialog = false }
        )
    }

    PremiumScaffold(
        headerHeight = Dimensions.headerContentStd,
        headerContent = { collapseFraction ->
            val secondaryAlpha = (1f - collapseFraction).coerceIn(0f, 1f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Spacing.xl, end = Spacing.xl, top = Spacing.md, bottom = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                HeaderIconButton(
                    onClick = onBackClick,
                    containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
                    iconTint = MaterialTheme.colorScheme.onPrimary
                )
                ItemHeaderThumbnail(
                    category = item?.category.orEmpty(),
                    imageUri = item?.imageUri.orEmpty()
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item?.name ?: "",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    if (item?.category?.isNotEmpty() == true) {
                        Text(
                            text = item!!.category,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .alpha(secondaryAlpha)
                        )
                    }
                }
                if (item != null) {
                    HeaderIconButton(
                        onClick = onEditClick,
                        icon = Icons.Default.Edit,
                        contentDescription = "Edit item",
                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
                        iconTint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.alpha(secondaryAlpha)
                    )
                }
            }
        }
    ) { scrollModifier ->
        when (val currentItem = item) {
            null -> Text(
                "Item not found",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(Spacing.xl)
            )
            else -> {
                val isAlreadyFound = currentItem.lastFoundAt > 0L

                Column(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .then(scrollModifier)
                            .verticalScroll(rememberScrollState())
                            .navigationBarsPadding()
                            .padding(
                                start = Spacing.xl,
                                end = Spacing.xl,
                                top = Spacing.md,
                                bottom = if (isAlreadyFound) Spacing.xxxl else Spacing.lg
                            ),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        LocationHeroCard(location = currentItem.location)

                        if (isAlreadyFound) {
                            FoundBadge(dateText = formatDateTime(currentItem.lastFoundAt))
                        }

                        if (currentItem.imageUri.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(Dimensions.cardCornerRadius),
                                elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showImageViewer = true }
                                ) {
                                    AsyncImage(
                                        model = currentItem.imageUri,
                                        contentDescription = currentItem.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .size(220.dp)
                                            .clip(RoundedCornerShape(Dimensions.cardCornerRadius)),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { showImageViewer = true },
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(Spacing.sm)
                                            .size(40.dp)
                                            .background(
                                                Color.Black.copy(alpha = 0.45f),
                                                CircleShape
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.ZoomIn,
                                            contentDescription = "Enlarge image",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(Dimensions.cardCornerRadius),
                            elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.xl),
                                verticalArrangement = Arrangement.spacedBy(Spacing.xl)
                            ) {
                                DetailRow(Icons.Default.Place, "Location", currentItem.location)
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                DetailRow(Icons.Default.Category, "Category", currentItem.category)
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                DetailRow(Icons.Default.Description, "Notes", currentItem.notes.ifBlank { "—" })
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                DetailRow(
                                    Icons.Default.CalendarToday,
                                    "Saved on",
                                    formatDateTime(currentItem.dateCreated)
                                )
                            }
                        }

                        RemindMeCard(
                            enabled = currentItem.remindEnabled || currentItem.remindActive,
                            hour = draftHour,
                            minute = draftMinute,
                            nextAt = currentItem.remindNextAt,
                            isActive = currentItem.remindActive,
                            showActions = true,
                            onToggle = { checked ->
                                if (!checked) {
                                    viewModel.stopReminder(itemId)
                                }
                            },
                            onTimePicked = { h, m ->
                                draftHour = h
                                draftMinute = m
                            },
                            onEnable = {
                                viewModel.setReminder(itemId, draftHour, draftMinute)
                            },
                            onSnooze = { viewModel.snoozeReminder(itemId) },
                            onStop = { viewModel.stopReminder(itemId) }
                        )
                    }

                    if (!isAlreadyFound) {
                        FoundActionBar(onClick = { showConfirmDialog = true })
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemHeaderThumbnail(
    category: String,
    imageUri: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(StatBlue, StatBlueDark))),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri.isNotEmpty()) {
            AsyncImage(
                model = imageUri,
                contentDescription = category,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = categoryIcon(category),
                contentDescription = category,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun FoundActionBar(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 8.dp
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Spacing.xl, vertical = Spacing.lg)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = Spacing.sm)
                    .size(20.dp)
            )
            Text(
                text = "I Found It!",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun LocationHeroCard(location: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimensions.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation),
        colors = CardDefaults.cardColors(containerColor = IRememberGreenLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(IRememberGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Look here",
                    style = MaterialTheme.typography.labelMedium,
                    color = IRememberGreenDeep.copy(alpha = 0.7f)
                )
                Text(
                    text = location,
                    style = MaterialTheme.typography.titleMedium,
                    color = IRememberGreenDeep,
                    modifier = Modifier.padding(top = Spacing.xxs)
                )
            }
        }
    }
}

@Composable
private fun FoundBadge(dateText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "Found on $dateText",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = Spacing.xs)
            )
        }
    }
}
