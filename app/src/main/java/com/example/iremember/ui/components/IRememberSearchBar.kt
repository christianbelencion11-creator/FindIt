package com.example.iremember.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.iremember.ui.theme.Spacing
import com.example.iremember.ui.theme.isAppDarkTheme
import com.example.iremember.ui.theme.premiumSurface

@Composable
fun IRememberSearchBar(
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
    val dark = isAppDarkTheme()
    val shape = RoundedCornerShape(16.dp)
    val fieldContainer = if (dark) Color.Transparent else MaterialTheme.colorScheme.surface
    val borderColor = if (dark) {
        Color.White.copy(alpha = 0.10f)
    } else {
        MaterialTheme.colorScheme.outline
    }
    val fieldInteraction = remember { MutableInteractionSource() }

    // Shell only (no outer clickable) so press indication stays inside the rounded clip.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .premiumSurface(shape, dark, MaterialTheme.colorScheme.surface)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = fieldInteraction,
                            indication = ripple(bounded = true),
                            onClick = onClick
                        )
                    } else {
                        Modifier
                    }
                ),
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
            // Keep enabled so trailing IconButtons (mic/filter) remain tappable when onClick is set.
            enabled = true,
            singleLine = true,
            shape = shape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = fieldContainer,
                unfocusedContainerColor = fieldContainer,
                disabledContainerColor = fieldContainer,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = borderColor,
                focusedBorderColor = if (dark) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                } else {
                    MaterialTheme.colorScheme.primary
                },
                unfocusedBorderColor = if (dark) Color.Transparent else borderColor
            )
        )
    }
}

@Composable
fun IRememberSearchBarOverlay(
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
    IRememberSearchBar(
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
