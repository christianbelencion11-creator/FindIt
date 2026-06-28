package com.example.findit.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.findit.ui.components.HeaderIconButton
import com.example.findit.ui.components.PremiumScaffold
import com.example.findit.ui.theme.Dimensions
import com.example.findit.ui.theme.Spacing
import com.example.findit.util.formatDate
import com.example.findit.viewmodel.ItemViewModel
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun ItemDetailScreen(
    viewModel: ItemViewModel,
    itemId: Long,
    onBackClick: () -> Unit
) {
    val item by viewModel.itemById(itemId).collectAsState(initial = null)

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        PremiumScaffold(
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
            headerContent = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.xl, vertical = Spacing.xxl),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    HeaderIconButton(onClick = onBackClick)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item?.name ?: "",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        if (item?.category?.isNotEmpty() == true) {
                            Text(
                                text = item!!.category,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        ) {
            when (val currentItem = item) {
                null -> Text(
                    "Item not found",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(Spacing.xl)
                )
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = Spacing.xl, vertical = Spacing.xl),
                        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                    ) {
                        // Photo card
                        if (currentItem.imageUri.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(Dimensions.cardCornerRadius),
                                elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                            }
                        }

                        // Details card
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
                                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                            ) {
                                DetailRow(Icons.Default.Place,       "Location",    currentItem.location)
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                DetailRow(Icons.Default.Category,    "Category",    currentItem.category)
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                DetailRow(Icons.Default.Description, "Notes",       currentItem.notes.ifBlank { "—" })
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                DetailRow(Icons.Default.CalendarToday, "Date Saved", formatDate(currentItem.dateCreated))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = Spacing.xxs)
            )
        }
    }
}
