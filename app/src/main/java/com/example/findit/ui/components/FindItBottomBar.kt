package com.example.findit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.findit.navigation.BottomNavTab
import com.example.findit.ui.theme.Spacing

private data class BottomNavItem(
    val tab: BottomNavTab,
    val label: String,
    val icon: ImageVector
)

/** Classic floating pill: Home · Search · Add · Profile (Alerts via drawer). */
private val bottomNavItems = listOf(
    BottomNavItem(BottomNavTab.Home, "Home", Icons.Default.Home),
    BottomNavItem(BottomNavTab.Search, "Search", Icons.Default.Search),
    BottomNavItem(BottomNavTab.AddItem, "Add Item", Icons.Default.Add),
    BottomNavItem(BottomNavTab.Profile, "Profile", Icons.Default.Person)
)

@Composable
fun FindItBottomBar(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    // Follow app ThemeMode via MaterialTheme — not system dark alone.
    val isDark = scheme.background.luminance() < 0.5f
    val barColor = scheme.surface
    val selectedContainerColor = scheme.primaryContainer
    val inactiveCircleColor =
        if (isDark) Color.White.copy(alpha = 0.16f) else scheme.surfaceVariant
    val inactiveIconColor = scheme.onSurfaceVariant
    val selectedLabelColor =
        if (isDark) scheme.onSurface else scheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Spacing.xl, vertical = Spacing.sm),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = barColor,
            tonalElevation = if (isDark) 3.dp else 2.dp,
            shadowElevation = if (isDark) 12.dp else 10.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomNavItems.forEach { item ->
                    val selected = selectedTab == item.tab
                    val activeColor = scheme.primary

                    Row(
                        modifier = Modifier
                            .weight(if (selected) 1.35f else 1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                if (selected) selectedContainerColor
                                else Color.Transparent
                            )
                            .clickable { onTabSelected(item.tab) }
                            .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) activeColor
                                    else inactiveCircleColor
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (selected) {
                                    scheme.onPrimary
                                } else {
                                    inactiveIconColor
                                },
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        if (selected) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = selectedLabelColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(start = Spacing.xs)
                            )
                        }
                    }
                }
            }
        }
    }
}
