package com.example.findit.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.findit.ui.components.CategoryChip
import com.example.findit.ui.components.PremiumScaffold
import com.example.findit.ui.theme.Dimensions
import com.example.findit.ui.theme.Spacing
import com.example.findit.viewmodel.ItemViewModel
import java.io.File
import kotlinx.coroutines.launch

private val predefinedCategories = listOf(
    "Electronics", "Keys", "Documents", "Personal", "Clothing", "Other"
)

@Composable
fun AddItemScreen(
    viewModel: ItemViewModel,
    onBackClick: () -> Unit = {},
    embedded: Boolean = false,
    onSaveSuccess: () -> Unit = {}
) {
    var itemName  by remember { mutableStateOf("") }
    var location  by remember { mutableStateOf("") }
    var category  by remember { mutableStateOf("") }
    var notes     by remember { mutableStateOf("") }
    var imageUri  by remember { mutableStateOf("") }
    var showPhotoSourcePicker by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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

    Box(modifier = Modifier.fillMaxSize()) {
        PremiumScaffold(
            headerContent = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.xl, vertical = Spacing.xxl)
                ) {
                    Text(
                        text = "Add Item",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "Save a new item to your collection",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.xl, vertical = Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                // Image picker card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clickable { showPhotoSourcePicker = true },
                    shape = RoundedCornerShape(Dimensions.cardCornerRadius),
                    elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 2.dp,
                                color = if (imageUri.isEmpty())
                                    MaterialTheme.colorScheme.outline
                                else Color.Transparent,
                                shape = RoundedCornerShape(Dimensions.cardCornerRadius)
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
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Text(
                                    "Tap to add a photo",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Form fields
                SectionLabel("Item Details")
                PremiumTextField(value = itemName, onValueChange = { itemName = it }, label = "Item Name")
                PremiumTextField(value = location,  onValueChange = { location  = it }, label = "Location")
                PremiumTextField(value = notes,     onValueChange = { notes     = it }, label = "Notes (optional)", minLines = 3)

                // Category selection
                SectionLabel("Category")
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    predefinedCategories.forEach { cat ->
                        CategoryChip(
                            category = cat,
                            selected = category == cat,
                            onClick  = { category = cat }
                        )
                    }
                }
                if (category.isNotEmpty() && !predefinedCategories.contains(category)) {
                    Text(
                        "Custom: $category",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(Spacing.sm))

                // Save button
                Button(
                    onClick = {
                        if (itemName.isNotBlank() && location.isNotBlank() && category.isNotBlank()) {
                            viewModel.saveItem(
                                name = itemName, location = location,
                                category = category, notes = notes,
                                imageUri = imageUri,
                                onSaved = {
                                    itemName = ""; location = ""; category = ""
                                    notes = ""; imageUri = ""
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Item saved successfully")
                                    }
                                    onSaveSuccess()
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = itemName.isNotBlank() && location.isNotBlank() && category.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        "Save Item",
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
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

private fun createCameraImageUri(context: Context): Uri {
    val imageDir = File(context.cacheDir, "item_photos").apply { mkdirs() }
    val imageFile = File(imageDir, "findit_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}

@Composable
private fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = minLines == 1,
        minLines = minLines,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor    = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor  = MaterialTheme.colorScheme.surface,
            focusedBorderColor       = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor     = MaterialTheme.colorScheme.outline
        )
    )
}
