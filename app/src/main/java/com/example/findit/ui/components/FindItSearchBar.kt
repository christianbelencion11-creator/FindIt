package com.example.findit.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.findit.ui.theme.Spacing

@Composable
fun FindItSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search items...",
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    onFilterClick: (() -> Unit)? = null,
    onVoiceClick: (() -> Unit)? = null,
    isListening: Boolean = false
) {
    val showTrailing = onFilterClick != null || onVoiceClick != null

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = if (showTrailing) {
                {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onVoiceClick != null) {
                            IconButton(onClick = onVoiceClick) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice search",
                                    tint = if (isListening) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        if (onFilterClick != null) {
                            IconButton(onClick = onFilterClick) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Filter",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                null
            },
            readOnly = readOnly || onClick != null,
            enabled = onClick == null,
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@Composable
fun FindItSearchBarOverlay(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search items...",
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    onFilterClick: (() -> Unit)? = null,
    onVoiceClick: (() -> Unit)? = null,
    isListening: Boolean = false
) {
    FindItSearchBar(
        query = query,
        onQueryChange = onQueryChange,
        modifier = modifier.padding(top = Spacing.sm),
        placeholder = placeholder,
        readOnly = readOnly,
        onClick = onClick,
        onFilterClick = onFilterClick,
        onVoiceClick = onVoiceClick,
        isListening = isListening
    )
}
