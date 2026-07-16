package com.example.findit.screens

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.findit.ui.components.PremiumScaffold
import com.example.findit.ui.components.ProfileAvatar
import com.example.findit.ui.components.StatsSection
import com.example.findit.ui.components.TabMenuHeader
import com.example.findit.ui.theme.Dimensions
import com.example.findit.ui.theme.Spacing
import com.example.findit.ui.theme.ThemeMode
import com.example.findit.ui.theme.mainTabBottomScrollPadding
import com.example.findit.util.BiometricHelper
import com.example.findit.util.computeStats
import com.example.findit.viewmodel.ItemViewModel

data class ProfileDetailsUpdate(
    val displayName: String,
    val username: String,
    val bio: String,
    val fullName: String,
    val birthday: String,
    val family: String,
    val phone: String,
    val location: String
)

@Composable
fun ProfileScreen(
    viewModel: ItemViewModel,
    themeMode: ThemeMode = ThemeMode.Auto,
    onThemeModeChanged: (ThemeMode) -> Unit = {},
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
    onProfileDetailsChanged: (ProfileDetailsUpdate) -> Unit = {},
    onChangePassword: () -> Unit = {},
    onLogout: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onUserInteraction: () -> Unit = {},
    bottomNavVisible: Boolean = true
) {
    val allItems by viewModel.allItems.collectAsState()
    val stats = computeStats(allItems)

    var isEditing by remember { mutableStateOf(false) }
    var draftName by remember(displayName) { mutableStateOf(displayName) }
    var draftUsername by remember(username) { mutableStateOf(username) }
    var draftBio by remember(bio) { mutableStateOf(bio) }
    var draftFullName by remember(fullName) { mutableStateOf(fullName) }
    var draftBirthday by remember(birthday) { mutableStateOf(birthday) }
    var draftFamily by remember(family) { mutableStateOf(family) }
    var draftPhone by remember(phone) { mutableStateOf(phone) }
    var draftLocation by remember(location) { mutableStateOf(location) }

    val bottomScrollPadding = mainTabBottomScrollPadding(bottomNavVisible)

    PremiumScaffold(
        onUserInteraction = onUserInteraction,
        headerHeight = Dimensions.headerContentWithMenu,
        headerContent = { collapseFraction ->
            TabMenuHeader(
                title = "My Profile",
                subtitle = "Complete your IRemember identity",
                onMenuClick = onMenuClick,
                collapseFraction = collapseFraction
            )
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
                draftFullName = draftFullName,
                onDraftFullNameChanged = { draftFullName = it },
                draftBirthday = draftBirthday,
                onDraftBirthdayChanged = { draftBirthday = it },
                draftFamily = draftFamily,
                onDraftFamilyChanged = { draftFamily = it },
                draftPhone = draftPhone,
                onDraftPhoneChanged = { draftPhone = it },
                draftLocation = draftLocation,
                onDraftLocationChanged = { draftLocation = it },
                onEditClick = {
                    draftName = displayName
                    draftUsername = username
                    draftBio = bio
                    draftFullName = fullName
                    draftBirthday = birthday
                    draftFamily = family
                    draftPhone = phone
                    draftLocation = location
                    isEditing = true
                },
                onCancelClick = {
                    draftName = displayName
                    draftUsername = username
                    draftBio = bio
                    draftFullName = fullName
                    draftBirthday = birthday
                    draftFamily = family
                    draftPhone = phone
                    draftLocation = location
                    isEditing = false
                },
                onDoneClick = {
                    onProfileDetailsChanged(
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
                    isEditing = false
                }
            )

            PersonalDetailsCard(
                fullName = fullName,
                birthday = birthday,
                family = family,
                phone = phone,
                location = location
            )

            StatsSection(stats = stats)

            SettingsCard(
                themeMode = themeMode,
                onThemeModeChanged = onThemeModeChanged,
                onChangePassword = onChangePassword,
                onLogout = onLogout
            )

            PermissionsCard()

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
    draftFullName: String,
    onDraftFullNameChanged: (String) -> Unit,
    draftBirthday: String,
    onDraftBirthdayChanged: (String) -> Unit,
    draftFamily: String,
    onDraftFamilyChanged: (String) -> Unit,
    draftPhone: String,
    onDraftPhoneChanged: (String) -> Unit,
    draftLocation: String,
    onDraftLocationChanged: (String) -> Unit,
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
                ProfileTextField(draftName, onDraftNameChanged, "Display name")
                Spacer(Modifier.height(Spacing.md))
                ProfileTextField(draftUsername, onDraftUsernameChanged, "Username")
                Spacer(Modifier.height(Spacing.md))
                ProfileTextField(draftBio, onDraftBioChanged, "Bio", minLines = 3)
                Spacer(Modifier.height(Spacing.md))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
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
                ProfileTextField(draftFullName, onDraftFullNameChanged, "Full name")
                Spacer(Modifier.height(Spacing.md))
                ProfileTextField(
                    draftBirthday,
                    onDraftBirthdayChanged,
                    "Birthday (YYYY-MM-DD)"
                )
                Spacer(Modifier.height(Spacing.md))
                ProfileTextField(draftFamily, onDraftFamilyChanged, "Family / status")
                Spacer(Modifier.height(Spacing.md))
                ProfileTextField(
                    draftPhone,
                    onDraftPhoneChanged,
                    "Phone",
                    keyboardType = KeyboardType.Phone
                )
                Spacer(Modifier.height(Spacing.md))
                ProfileTextField(draftLocation, onDraftLocationChanged, "Location")
                Spacer(Modifier.height(Spacing.lg))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    OutlinedButton(
                        onClick = onCancelClick,
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancel") }
                    Button(
                        onClick = onDoneClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) { Text("Done") }
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
private fun PersonalDetailsCard(
    fullName: String,
    birthday: String,
    family: String,
    phone: String,
    location: String
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
                text = "Personal details",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(Spacing.md))
            DetailRow("Full name", fullName)
            DetailRow("Birthday", birthday)
            DetailRow("Family", family)
            DetailRow("Phone", phone)
            DetailRow("Location", location, showDivider = false)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, showDivider: Boolean = true) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value.ifBlank { "Not set" },
            style = MaterialTheme.typography.bodyLarge,
            color = if (value.isBlank()) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.padding(top = 2.dp, bottom = Spacing.md)
        )
        if (showDivider) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(Modifier.height(Spacing.md))
        }
    }
}

@Composable
private fun ProfileTextField(
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

@Composable
private fun SettingsCard(
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit
) {
    var themeMenuExpanded by remember { mutableStateOf(false) }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { themeMenuExpanded = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = themeModeIcon(themeMode),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = Spacing.md)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Appearance",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        when (themeMode) {
                            ThemeMode.Dark -> "Dark"
                            ThemeMode.Light -> "Light"
                            ThemeMode.Auto -> "Auto · following system"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box {
                    TextButton(onClick = { themeMenuExpanded = true }) {
                        Text(themeMode.name)
                    }
                    DropdownMenu(
                        expanded = themeMenuExpanded,
                        onDismissRequest = { themeMenuExpanded = false }
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = themeModeIcon(mode),
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    onThemeModeChanged(mode)
                                    themeMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(Spacing.lg))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(Modifier.height(Spacing.md))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onChangePassword)
                    .padding(vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = Spacing.md)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Change Password",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Update your account password",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(Spacing.md))
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

private fun themeModeIcon(mode: ThemeMode) = when (mode) {
    ThemeMode.Light -> Icons.Default.LightMode
    ThemeMode.Dark -> Icons.Default.DarkMode
    ThemeMode.Auto -> Icons.Default.BrightnessAuto
}

@Composable
private fun PermissionsCard() {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var notifRefresh by remember { mutableStateOf(0) }

    val notificationsGranted = remember(notifRefresh) {
        if (Build.VERSION.SDK_INT < 33) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    val biometricAvailable = remember(activity) {
        activity?.let { BiometricHelper.canAuthenticate(it) } == true
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        notifRefresh++
    }

    fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        )
        context.startActivity(intent)
    }

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
                text = "Permissions",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(Spacing.md))

            PermissionRow(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = if (notificationsGranted) {
                    "Allowed — used for Remind Me alerts"
                } else {
                    "Not allowed — tap to enable reminders"
                },
                onClick = {
                    if (Build.VERSION.SDK_INT >= 33 && !notificationsGranted) {
                        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        openAppSettings()
                    }
                }
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = Spacing.sm),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
            PermissionRow(
                icon = Icons.Default.Fingerprint,
                title = "Biometrics",
                subtitle = if (biometricAvailable) {
                    "Available — used to unlock with fingerprint / face"
                } else {
                    "Not available on this device"
                },
                onClick = { openAppSettings() }
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = Spacing.sm),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
            PermissionRow(
                icon = Icons.Default.CameraAlt,
                title = "Camera & Photos",
                subtitle = "Used when adding item photos or your profile picture",
                onClick = { openAppSettings() }
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = Spacing.sm),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
            PermissionRow(
                icon = Icons.Default.Settings,
                title = "Open app settings",
                subtitle = "Manage all permissions in Android settings",
                onClick = { openAppSettings() }
            )
        }
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = Spacing.md)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
                        "IRemember",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Never lose track again",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
