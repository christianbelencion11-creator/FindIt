package com.example.findit.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.findit.ui.components.CategoryChip
import com.example.findit.ui.components.HeaderIconButton
import com.example.findit.ui.components.ItemSavedSuccessDialog
import com.example.findit.ui.components.PremiumScaffold
import com.example.findit.ui.components.RemindMeCard
import com.example.findit.ui.components.TabMenuHeader
import com.example.findit.ui.theme.Dimensions
import com.example.findit.ui.theme.Spacing
import com.example.findit.ui.theme.mainTabBottomScrollPadding
import com.example.findit.viewmodel.ItemViewModel
import java.io.File

private val predefinedCategories = listOf(
    "Electronics", "Keys", "Documents", "Wallet", "Bags", "Others"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddItemScreen(
    viewModel: ItemViewModel,
    onBackClick: () -> Unit = {},
    embedded: Boolean = false,
    onSaveSuccess: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onUserInteraction: () -> Unit = {},
    bottomNavVisible: Boolean = true,
    itemId: Long? = null
) {
    val isEditMode = itemId != null && itemId > 0L
    val existingItem by viewModel.itemById(itemId ?: 0L).collectAsState(initial = null)

    var itemName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf("") }
    var remindEnabled by remember { mutableStateOf(false) }
    var remindHour by remember { mutableIntStateOf(8) }
    var remindMinute by remember { mutableIntStateOf(0) }
    var formLoaded by remember { mutableStateOf(!isEditMode) }
    var showErrors by remember { mutableStateOf(false) }
    var showPhotoSourcePicker by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var savedItemName by remember { mutableStateOf("") }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val dashColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.85f)

    LaunchedEffect(existingItem?.id) {
        val item = existingItem
        if (isEditMode && item != null && !formLoaded) {
            itemName = item.name
            location = item.location
            category = item.category
            notes = item.notes
            imageUri = item.imageUri
            remindEnabled = item.remindEnabled
            remindHour = item.remindHour
            remindMinute = item.remindMinute
            formLoaded = true
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> imageUri = uri?.toString().orEmpty() }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = pendingCameraUri?.toString().orEmpty()
        }
    }

    val bottomScrollPadding = mainTabBottomScrollPadding(bottomNavVisible)
    val canSave = itemName.isNotBlank() && location.isNotBlank() && category.isNotBlank()
    // Only surface field errors after the user has tried to save, so the form isn't red on open.
    val nameError = showErrors && itemName.isBlank()
    val locationError = showErrors && location.isBlank()
    val categoryError = showErrors && category.isBlank()
    val headerTitle = if (isEditMode) "Edit Item" else "Add Item"
    val headerSubtitle = if (isEditMode) {
        "Update this item in your collection"
    } else {
        "Save a new item to your collection"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PremiumScaffold(
            onUserInteraction = onUserInteraction,
            headerHeight = if (isEditMode && !embedded) {
                Dimensions.headerContentStd
            } else {
                Dimensions.headerContentWithMenu
            },
            headerContent = { collapseFraction ->
                if (isEditMode && !embedded) {
                    val secondaryAlpha = (1f - collapseFraction).coerceIn(0f, 1f)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = Spacing.xl,
                                end = Spacing.xl,
                                top = Spacing.md,
                                bottom = Spacing.sm
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        HeaderIconButton(
                            onClick = onBackClick,
                            containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
                            iconTint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.alpha(secondaryAlpha)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = headerTitle,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = headerSubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .alpha(secondaryAlpha)
                            )
                        }
                    }
                } else {
                    TabMenuHeader(
                        title = headerTitle,
                        subtitle = headerSubtitle,
                        onMenuClick = onBackClick,
                        leadingIsBack = true,
                        collapseFraction = collapseFraction
                    )
                }
            }
        ) { scrollModifier ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(scrollModifier)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = Spacing.xl,
                        end = Spacing.xl,
                        top = Spacing.xl,
                        bottom = bottomScrollPadding
                    ),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                // Upload photo card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clickable { showPhotoSourcePicker = true },
                    shape = RoundedCornerShape(Dimensions.cardCornerRadius),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (imageUri.isEmpty()) {
                                    Modifier.drawBehind {
                                        val stroke = Stroke(
                                            width = 2.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(
                                                floatArrayOf(14f, 10f),
                                                0f
                                            )
                                        )
                                        drawRoundRect(
                                            color = dashColor,
                                            cornerRadius = CornerRadius(Dimensions.cardCornerRadius.toPx()),
                                            style = stroke
                                        )
                                    }
                                } else {
                                    Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri.isNotEmpty()) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Item photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(Dimensions.cardCornerRadius)),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-8).dp, y = 8.dp)
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.55f)),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    onClick = { imageUri = "" },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Text(
                                    "Tap to add a photo",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "JPG, PNG up to 5MB",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                SectionLabel("Item Details")
                IconTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    placeholder = "Enter the name of the item",
                    leadingIcon = Icons.Default.Inventory2,
                    isError = nameError,
                    errorMessage = "Please complete this field"
                )
                IconTextField(
                    value = location,
                    onValueChange = { location = it },
                    placeholder = "Where did you last see it?",
                    leadingIcon = Icons.Default.Place,
                    isError = locationError,
                    errorMessage = "Please complete this field"
                )
                IconTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = "Add any extra details about the item",
                    leadingIcon = Icons.Default.Description
                )

                SectionLabel("Category")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    predefinedCategories.forEach { cat ->
                        CategoryChip(
                            category = cat,
                            selected = category == cat,
                            onClick = { category = cat }
                        )
                    }
                }
                if (categoryError) {
                    Text(
                        text = "Please choose a category",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = Spacing.xs, top = Spacing.xs)
                    )
                }

                RemindMeCard(
                    enabled = remindEnabled,
                    hour = remindHour,
                    minute = remindMinute,
                    onToggle = { remindEnabled = it },
                    onTimePicked = { h, m ->
                        remindHour = h
                        remindMinute = m
                    },
                    onEnable = { remindEnabled = true },
                    onStop = { remindEnabled = false }
                )

                if (!isEditMode) {
                    Text(
                        text = "Saved on this device only. Uninstalling the app permanently deletes your items.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                }

                Spacer(Modifier.height(Spacing.sm))

                Button(
                    onClick = {
                        if (!canSave) {
                            showErrors = true
                            Toast.makeText(
                                context,
                                "Please complete the required fields.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }
                        if (isEditMode) {
                            viewModel.updateItem(
                                itemId = itemId!!,
                                name = itemName,
                                location = location,
                                category = category,
                                notes = notes,
                                imageUri = imageUri,
                                remindEnabled = remindEnabled,
                                remindHour = remindHour,
                                remindMinute = remindMinute,
                                onUpdated = { ok ->
                                    if (ok) {
                                        savedItemName = itemName
                                        showSuccessDialog = true
                                    }
                                }
                            )
                        } else {
                            viewModel.saveItem(
                                name = itemName,
                                location = location,
                                category = category,
                                notes = notes,
                                imageUri = imageUri,
                                remindEnabled = remindEnabled,
                                remindHour = remindHour,
                                remindMinute = remindMinute,
                                onSaved = {
                                    savedItemName = itemName
                                    itemName = ""
                                    location = ""
                                    category = ""
                                    notes = ""
                                    imageUri = ""
                                    remindEnabled = false
                                    remindHour = 8
                                    remindMinute = 0
                                    showErrors = false
                                    showSuccessDialog = true
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isEditMode || formLoaded,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.size(Spacing.sm))
                    Text(
                        if (isEditMode) "Save Changes" else "Save Item",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(Modifier.height(Spacing.xl))
            }
        }

        if (showPhotoSourcePicker) {
            AlertDialog(
                onDismissRequest = { showPhotoSourcePicker = false },
                title = {
                    Text(
                        text = "Add a photo",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        Text(
                            text = "Choose where the item photo will come from.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = {
                                showPhotoSourcePicker = false
                                val uri = createCameraImageUri(context)
                                pendingCameraUri = uri
                                cameraLauncher.launch(uri)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                modifier = Modifier.padding(end = Spacing.sm)
                            )
                            Text("Camera")
                        }
                        TextButton(
                            onClick = {
                                showPhotoSourcePicker = false
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.padding(end = Spacing.sm)
                            )
                            Text("Album")
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showPhotoSourcePicker = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showSuccessDialog) {
            ItemSavedSuccessDialog(
                itemName = savedItemName,
                updated = isEditMode,
                onDone = {
                    showSuccessDialog = false
                    onSaveSuccess()
                }
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun IconTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    minLines: Int = 1,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = minLines == 1,
        minLines = minLines,
        isError = isError,
        placeholder = {
            Text(
                text = placeholder,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        },
        supportingText = if (isError && errorMessage != null) {
            { Text(text = errorMessage, style = MaterialTheme.typography.bodySmall) }
        } else {
            null
        },
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
        )
    )
}

private fun createCameraImageUri(context: Context): Uri {
    val imageDir = File(context.cacheDir, "item_photos").apply { mkdirs() }
    val imageFile = File(imageDir, "findit_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}
