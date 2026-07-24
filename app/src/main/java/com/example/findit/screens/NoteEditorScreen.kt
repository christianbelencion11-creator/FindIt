package com.example.findit.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.findit.model.NoteAccents
import com.example.findit.ui.components.HeaderIconButton
import com.example.findit.ui.components.PremiumScaffold
import com.example.findit.ui.theme.Dimensions
import com.example.findit.ui.theme.Spacing
import com.example.findit.ui.theme.darkCardGradientFill
import com.example.findit.ui.theme.darkSurfaceBorder
import com.example.findit.ui.theme.isAppDarkTheme
import com.example.findit.util.ChecklistItem
import com.example.findit.util.NoteChecklistCodec
import com.example.findit.util.ReminderTimeUtils
import com.example.findit.util.formatDateTime
import com.example.findit.viewmodel.NoteViewModel
import java.util.Calendar

@Composable
fun NoteEditorScreen(
    noteId: Long,
    onBackClick: () -> Unit,
    viewModel: NoteViewModel = viewModel(factory = NoteViewModel.Factory)
) {
    val existing by viewModel.noteById(noteId).collectAsState(initial = null)
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var remindEnabled by remember { mutableStateOf(false) }
    var remindAt by remember { mutableLongStateOf(0L) }
    var pinned by remember { mutableStateOf(false) }
    var accent by remember { mutableIntStateOf(0) }
    var isChecklist by remember { mutableStateOf(true) }
    var loaded by remember { mutableStateOf(false) }
    val checklistItems = remember { mutableStateListOf<ChecklistItem>() }

    LaunchedEffect(existing, noteId) {
        if (noteId > 0L && existing != null && !loaded) {
            title = existing!!.title
            body = existing!!.body
            remindEnabled = existing!!.remindEnabled
            remindAt = existing!!.remindAt
            pinned = existing!!.pinned
            accent = existing!!.accent
            isChecklist = existing!!.isChecklist
            checklistItems.clear()
            checklistItems.addAll(NoteChecklistCodec.parse(existing!!.body))
            loaded = true
        } else if (noteId == 0L && !loaded) {
            remindAt = ReminderTimeUtils.nextWallClockMillis(9, 0)
            checklistItems.clear()
            checklistItems.add(ChecklistItem(""))
            loaded = true
        }
    }

    fun bodyForSave(): String =
        if (isChecklist) NoteChecklistCodec.serialize(checklistItems.toList()) else body.trim()

    val dark = isAppDarkTheme()
    val formShape = RoundedCornerShape(Dimensions.cardCornerRadius)

    PremiumScaffold(
        headerHeight = Dimensions.headerContentWithMenu,
        headerContent = { collapseFraction ->
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
                        text = if (noteId > 0L) "Edit note" else "New note",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "Checklist, grocery, reminders",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.alpha(secondaryAlpha)
                    )
                }
                IconButton(onClick = { pinned = !pinned }) {
                    Icon(
                        imageVector = if (pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (pinned) "Unpin" else "Pin",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    ) { scrollModifier ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .then(scrollModifier),
                contentPadding = PaddingValues(
                    start = Spacing.xl,
                    end = Spacing.xl,
                    top = Spacing.xl,
                    bottom = 120.dp
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = formShape,
                        border = if (dark) darkSurfaceBorder() else null,
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (dark) 0.dp else Dimensions.cardElevation
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (dark) {
                                Color.Transparent
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .darkCardGradientFill(dark)
                                .padding(Spacing.xl),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Title") },
                                placeholder = { Text("Grocery list, errands…") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences
                                )
                            )

                            Text(
                                text = "Type",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                            ) {
                                ModeChip(
                                    selected = isChecklist,
                                    label = "Checklist",
                                    icon = Icons.Filled.CheckBox,
                                    onClick = {
                                        if (!isChecklist) {
                                            isChecklist = true
                                            if (checklistItems.isEmpty()) {
                                                checklistItems.addAll(NoteChecklistCodec.parse(body))
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                ModeChip(
                                    selected = !isChecklist,
                                    label = "Freeform",
                                    icon = Icons.AutoMirrored.Filled.Notes,
                                    onClick = {
                                        if (isChecklist) {
                                            isChecklist = false
                                            body = NoteChecklistCodec.serialize(checklistItems.toList())
                                                .lines()
                                                .joinToString("\n") {
                                                    it.replace(Regex("""^\s*[-*]\s*\[[xX ]?\]\s*"""), "")
                                                }
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Text(
                                text = "Color",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                NoteAccents.colors.forEachIndexed { index, colorInt ->
                                    val selected = accent == index
                                    Box(
                                        modifier = Modifier
                                            .size(if (selected) 36.dp else 32.dp)
                                            .clip(CircleShape)
                                            .background(Color(colorInt))
                                            .then(
                                                if (selected) {
                                                    Modifier.border(
                                                        2.dp,
                                                        MaterialTheme.colorScheme.onSurface,
                                                        CircleShape
                                                    )
                                                } else Modifier
                                            )
                                            .clickable { accent = index }
                                    )
                                }
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )

                            if (isChecklist) {
                                Text(
                                    text = "Items",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                checklistItems.forEachIndexed { index, item ->
                                    ChecklistEditorRow(
                                        item = item,
                                        onCheckedChange = { checked ->
                                            checklistItems[index] = item.copy(checked = checked)
                                        },
                                        onTextChange = { text ->
                                            checklistItems[index] = item.copy(text = text)
                                        },
                                        onRemove = {
                                            if (checklistItems.size > 1) {
                                                checklistItems.removeAt(index)
                                            } else {
                                                checklistItems[0] = ChecklistItem("")
                                            }
                                        }
                                    )
                                }
                                TextButton(
                                    onClick = { checklistItems.add(ChecklistItem("")) },
                                    modifier = Modifier.align(Alignment.Start)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.xs))
                                    Text("Add item")
                                }
                                val (done, total) = NoteChecklistCodec.progress(checklistItems.toList())
                                if (total > 0) {
                                    Text(
                                        text = "$done of $total completed",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else {
                                OutlinedTextField(
                                    value = body,
                                    onValueChange = { body = it },
                                    label = { Text("Details") },
                                    placeholder = { Text("Write anything you need to remember…") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 220.dp),
                                    minLines = 8,
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Sentences
                                    )
                                )
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Reminder",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (remindEnabled && remindAt > 0L) {
                                            formatDateTime(remindAt)
                                        } else {
                                            "Notify me about this list"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = remindEnabled,
                                    onCheckedChange = { enabled ->
                                        remindEnabled = enabled
                                        if (enabled && remindAt <= 0L) {
                                            remindAt = ReminderTimeUtils.nextWallClockMillis(9, 0)
                                        }
                                    }
                                )
                            }

                            if (remindEnabled) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                                ) {
                                    listOf(1 to "In 1h", 3 to "In 3h", 6 to "In 6h").forEach { (hours, label) ->
                                        FilterChip(
                                            selected = false,
                                            onClick = {
                                                remindAt = System.currentTimeMillis() +
                                                    hours * 60L * 60L * 1000L
                                            },
                                            label = { Text(label) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        )
                                    }
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            val cal = Calendar.getInstance().apply {
                                                add(Calendar.DAY_OF_YEAR, 1)
                                                set(Calendar.HOUR_OF_DAY, 9)
                                                set(Calendar.MINUTE, 0)
                                                set(Calendar.SECOND, 0)
                                                set(Calendar.MILLISECOND, 0)
                                            }
                                            remindAt = cal.timeInMillis
                                        },
                                        label = { Text("Tomorrow 9 AM") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                tonalElevation = if (dark) 0.dp else 4.dp,
                shadowElevation = if (dark) 0.dp else 8.dp,
                color = if (dark) Color.Transparent else MaterialTheme.colorScheme.surface,
                border = if (dark) darkSurfaceBorder() else null
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .darkCardGradientFill(dark)
                ) {
                    Button(
                        onClick = {
                            viewModel.saveNote(
                                noteId = noteId,
                                title = title,
                                body = bodyForSave(),
                                remindEnabled = remindEnabled,
                                remindAt = remindAt,
                                pinned = pinned,
                                accent = accent,
                                isChecklist = isChecklist
                            ) { onBackClick() }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.xl, vertical = Spacing.md),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Save note",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = Spacing.xs)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeChip(
    selected: Boolean,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        },
        border = if (selected) {
            null
        } else {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant
            )
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.width(Spacing.xs))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun ChecklistEditorRow(
    item: ChecklistItem,
    onCheckedChange: (Boolean) -> Unit,
    onTextChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onCheckedChange(!item.checked) }) {
            Icon(
                imageVector = if (item.checked) {
                    Icons.Filled.CheckBox
                } else {
                    Icons.Outlined.CheckBoxOutlineBlank
                },
                contentDescription = if (item.checked) "Mark incomplete" else "Mark complete",
                tint = if (item.checked) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        OutlinedTextField(
            value = item.text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("List item") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            )
        )
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove item",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
