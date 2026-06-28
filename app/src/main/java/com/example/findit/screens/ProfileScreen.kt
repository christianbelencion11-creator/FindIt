package com.example.findit.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.findit.ui.components.PremiumScaffold
import com.example.findit.ui.components.ProfileAvatar
import com.example.findit.ui.components.StatsSection
import com.example.findit.ui.theme.Dimensions
import com.example.findit.ui.theme.Spacing
import com.example.findit.util.computeStats
import com.example.findit.viewmodel.ItemViewModel

@Composable
fun ProfileScreen(
    viewModel: ItemViewModel,
    isDarkTheme: Boolean,
    onDarkThemeChanged: (Boolean) -> Unit,
    profileImageUri: String = "",
    onProfileImageChanged: (String) -> Unit = {},
    displayName: String = "FindIt User",
    username: String = "findit_user",
    bio: String = "Keeping everyday essentials organized and easy to find.",
    onProfileDetailsChanged: (String, String, String) -> Unit = { _, _, _ -> },
    onLogout: () -> Unit = {}
) {
    val allItems by viewModel.allItems.collectAsState()
    val stats = computeStats(allItems)

    var isEditing by remember { mutableStateOf(false) }
    var draftName by remember(displayName) { mutableStateOf(displayName) }
    var draftUsername by remember(username) { mutableStateOf(username) }
    var draftBio by remember(bio) { mutableStateOf(bio) }

    PremiumScaffold(
        headerContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xl, vertical = Spacing.xxl)
            ) {
                Text(
                    text = "My Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "Complete your FindIt identity",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            ProfileCard(
                profileImageUri = profileImageUri,
                onProfileImageChanged = onProfileImageChanged,
                displayName = displayName,
                username = username,
                bio = bio,
                isEditing = isEditing,
                draftName = draftName,
                onDraftNameChanged = { draftName = it },
                draftUsername = draftUsername,
                onDraftUsernameChanged = { draftUsername = it },
                draftBio = draftBio,
                onDraftBioChanged = { draftBio = it },
                onEditClick = { isEditing = true },
                onCancelClick = {
                    draftName = displayName
                    draftUsername = username
                    draftBio = bio
                    isEditing = false
                },
                onDoneClick = {
                    val cleanName = draftName.trim().ifBlank { "FindIt User" }
                    val cleanUsername = draftUsername
                        .trim()
                        .removePrefix("@")
                        .ifBlank { "findit_user" }
                    val cleanBio = draftBio.trim()
                        .ifBlank { "Keeping everyday essentials organized and easy to find." }
                    onProfileDetailsChanged(cleanName, cleanUsername, cleanBio)
                    isEditing = false
                }
            )

            StatsSection(stats = stats)

            SettingsCard(
                isDarkTheme = isDarkTheme,
                onDarkThemeChanged = onDarkThemeChanged,
                onLogout = onLogout
            )

            AboutCard()

            Spacer(Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun ProfileCard(
    profileImageUri: String,
    onProfileImageChanged: (String) -> Unit,
    displayName: String,
    username: String,
    bio: String,
    isEditing: Boolean,
    draftName: String,
    onDraftNameChanged: (String) -> Unit,
    draftUsername: String,
    onDraftUsernameChanged: (String) -> Unit,
    draftBio: String,
    onDraftBioChanged: (String) -> Unit,
    onEditClick: () -> Unit,
    onCancelClick: () -> Unit,
    onDoneClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileAvatar(
                imageUri = profileImageUri,
                onImageSelected = onProfileImageChanged,
                size = 92.dp,
                editable = true
            )
            Spacer(Modifier.height(Spacing.md))

            if (isEditing) {
                ProfileTextField(
                    value = draftName,
                    onValueChange = onDraftNameChanged,
                    label = "Display name"
                )
                Spacer(Modifier.height(Spacing.md))
                ProfileTextField(
                    value = draftUsername,
                    onValueChange = onDraftUsernameChanged,
                    label = "Username"
                )
                Spacer(Modifier.height(Spacing.md))
                ProfileTextField(
                    value = draftBio,
                    onValueChange = onDraftBioChanged,
                    label = "Bio",
                    minLines = 3
                )
                Spacer(Modifier.height(Spacing.lg))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    OutlinedButton(
                        onClick = onCancelClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = onDoneClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Done")
                    }
                }
            } else {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "@$username",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = Spacing.xxs)
                )
                Text(
                    text = bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.sm)
                )
                Spacer(Modifier.height(Spacing.lg))
                Button(
                    onClick = onEditClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.padding(end = Spacing.sm)
                    )
                    Text("Edit Profile")
                }
            }
        }
    }
}

@Composable
private fun ProfileTextField(
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
        shape = MaterialTheme.shapes.small,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
private fun SettingsCard(
    isDarkTheme: Boolean,
    onDarkThemeChanged: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(Spacing.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = Spacing.md)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Dark Mode",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        if (isDarkTheme) "Dark theme active" else "Light theme active",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = onDarkThemeChanged
                )
            }
            Spacer(Modifier.height(Spacing.lg))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(Modifier.height(Spacing.lg))
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    modifier = Modifier.padding(end = Spacing.sm)
                )
                Text("Sign Out")
            }
        }
    }
}

@Composable
private fun AboutCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl)
        ) {
            Text(
                text = "About Application",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(Spacing.md))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(Modifier.height(Spacing.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = Spacing.md)
                )
                Column {
                    Text(
                        "FindIt",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Version 1.0 • Premium item tracker",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
