package com.example.iremember.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.iremember.ui.components.HeaderIconButton
import com.example.iremember.ui.components.PremiumScaffold
import com.example.iremember.ui.components.ProfileAvatar
import com.example.iremember.ui.theme.Dimensions
import com.example.iremember.ui.theme.Spacing

@Composable
fun EditProfileScreen(
    profileImageUri: String = "",
    onProfileImageChanged: (String) -> Unit = {},
    displayName: String = "IRemember User",
    username: String = "iremember_user",
    bio: String = "Keeping everyday essentials organized and easy to find.",
    fullName: String = "",
    birthday: String = "",
    family: String = "",
    phone: String = "",
    location: String = "",
    onSave: (ProfileDetailsUpdate) -> Unit,
    onBackClick: () -> Unit
) {
    var draftName by remember(displayName) { mutableStateOf(displayName) }
    var draftUsername by remember(username) { mutableStateOf(username) }
    var draftBio by remember(bio) { mutableStateOf(bio) }
    var draftFullName by remember(fullName) { mutableStateOf(fullName) }
    var draftBirthday by remember(birthday) { mutableStateOf(birthday) }
    var draftFamily by remember(family) { mutableStateOf(family) }
    var draftPhone by remember(phone) { mutableStateOf(phone) }
    var draftLocation by remember(location) { mutableStateOf(location) }

    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    fun saveAndClose() {
        onSave(
            ProfileDetailsUpdate(
                displayName = draftName.trim().ifBlank { "IRemember User" },
                username = draftUsername.trim().removePrefix("@")
                    .ifBlank { "iremember_user" },
                bio = draftBio.trim().ifBlank {
                    "Keeping everyday essentials organized and easy to find."
                },
                fullName = draftFullName.trim(),
                birthday = draftBirthday.trim(),
                family = draftFamily.trim(),
                phone = draftPhone.trim(),
                location = draftLocation.trim()
            )
        )
        onBackClick()
    }

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
                        text = "Edit Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "Update your name and details",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .alpha(secondaryAlpha)
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
                top = Spacing.lg,
                bottom = navBarBottom + Spacing.xxxl
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = Dimensions.cardElevation
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
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
                        Spacer(Modifier.height(Spacing.lg))
                        EditProfileTextField(draftName, { draftName = it }, "Display name")
                        Spacer(Modifier.height(Spacing.md))
                        EditProfileTextField(draftUsername, { draftUsername = it }, "Username")
                        Spacer(Modifier.height(Spacing.md))
                        EditProfileTextField(
                            draftBio,
                            { draftBio = it },
                            "Bio",
                            minLines = 3
                        )
                        Spacer(Modifier.height(Spacing.md))
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                        )
                        Spacer(Modifier.height(Spacing.md))
                        Text(
                            text = "Personal details",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = Spacing.sm)
                        )
                        EditProfileTextField(
                            draftFullName,
                            { draftFullName = it },
                            "Full name"
                        )
                        Spacer(Modifier.height(Spacing.md))
                        EditProfileTextField(
                            draftBirthday,
                            { draftBirthday = it },
                            "Birthday (YYYY-MM-DD)"
                        )
                        Spacer(Modifier.height(Spacing.md))
                        EditProfileTextField(
                            draftFamily,
                            { draftFamily = it },
                            "Family / status"
                        )
                        Spacer(Modifier.height(Spacing.md))
                        EditProfileTextField(
                            draftPhone,
                            { draftPhone = it },
                            "Phone",
                            keyboardType = KeyboardType.Phone
                        )
                        Spacer(Modifier.height(Spacing.md))
                        EditProfileTextField(
                            draftLocation,
                            { draftLocation = it },
                            "Location"
                        )
                        Spacer(Modifier.height(Spacing.lg))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            OutlinedButton(
                                onClick = onBackClick,
                                modifier = Modifier.weight(1f)
                            ) { Text("Cancel") }
                            Button(
                                onClick = ::saveAndClose,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) { Text("Save") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = minLines == 1,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = MaterialTheme.shapes.small,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}
