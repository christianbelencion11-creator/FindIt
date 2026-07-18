package com.example.findit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.findit.ui.theme.Spacing
import com.example.findit.ui.theme.gradientEndColor
import com.example.findit.ui.theme.gradientStartColor

@Composable
fun FindItNavigationDrawerContent(
    displayName: String,
    username: String = "",
    profileImageUri: String,
    onHome: () -> Unit,
    onSearch: () -> Unit,
    onAddItem: () -> Unit,
    onAlerts: () -> Unit,
    onNews: () -> Unit,
    onHistory: () -> Unit,
    onProfile: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val headerBrush = Brush.linearGradient(
        listOf(gradientStartColor(), gradientEndColor())
    )
    val handle = username.trim().removePrefix("@").ifBlank {
        displayName.trim().lowercase().replace(" ", "")
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth(0.84f)
            .clip(RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBrush)
        ) {
            HeaderAtmosphere(modifier = Modifier.matchParentSize())
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = Spacing.xl, vertical = Spacing.xl),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileAvatar(
                    imageUri = profileImageUri,
                    onImageSelected = {},
                    size = 56.dp,
                    editable = false
                )
                Spacer(modifier.width(Spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "@$handle",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = Spacing.md)
        ) {
            DrawerMenuRow(
                icon = Icons.Outlined.Home,
                title = "Home",
                subtitle = "Dashboard and recent items",
                onClick = onHome
            )
            DrawerMenuRow(
                icon = Icons.Outlined.Search,
                title = "Search",
                subtitle = "Find items by name, place, or notes",
                onClick = onSearch
            )
            DrawerMenuRow(
                icon = Icons.Outlined.AddCircleOutline,
                title = "Add Item",
                subtitle = "Save a new item and its location",
                onClick = onAddItem
            )
            DrawerMenuRow(
                icon = Icons.Outlined.Notifications,
                title = "Alerts",
                subtitle = "Reminders and pending found check-ins",
                onClick = onAlerts
            )
            DrawerMenuRow(
                icon = Icons.AutoMirrored.Filled.Article,
                title = "News",
                subtitle = "Headlines near you",
                onClick = onNews
            )
            DrawerMenuRow(
                icon = Icons.Outlined.History,
                title = "History",
                subtitle = "Found, edited, and deleted items",
                onClick = onHistory
            )
            DrawerMenuRow(
                icon = Icons.Outlined.PersonOutline,
                title = "Profile",
                subtitle = "Edit identity, security, and settings",
                onClick = onProfile,
                showDivider = false
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Spacing.xl, vertical = Spacing.lg)
        ) {
            Button(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = Spacing.sm)
                        .size(20.dp)
                )
                Text(
                    text = "Sign Out",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun DrawerMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = Spacing.xl, vertical = Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = Spacing.xl),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
        }
    }
}
