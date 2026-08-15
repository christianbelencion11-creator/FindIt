package com.example.findit.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.findit.navigation.BottomNavTab
import com.example.findit.ui.theme.Spacing
import com.example.findit.ui.theme.isAppDarkTheme

/**
 * Bottom navigation with a docked center "+" (Add) button.
 * Layout: Home · Search · [ + ] · History · Profile.
 * Colors follow the app theme (light = white panel / green accent,
 * dark = deep-green panel / bright-green accent). Only the raised-center
 * effect was inspired by the reference — not its palette.
 */
@Composable
fun FindItBottomBar(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    onAddClick: () -> Unit = {},
    addExpanded: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme
    val isDark = isAppDarkTheme()

    val addRotation by animateFloatAsState(
        targetValue = if (addExpanded) 45f else 0f,
        animationSpec = tween(220),
        label = "add_fab_rotation"
    )

    // Only the ACTIVE tab turns gold; the gold "moves" as you navigate.
    // Inactive tabs stay a neutral muted tone.
    val panelColor = scheme.surface
    val accent = if (isDark) Color(0xFFFFD24A) else Color(0xFFD4A017)
    val inactive = scheme.onSurfaceVariant
    val fabGreen = if (isDark) Color(0xFF22C55E) else Color(0xFF16A34A)
    val edgeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
    val panelBrush = Brush.verticalGradient(listOf(panelColor, panelColor))

    val barHeight = 72.dp // taller panel so icon + label have breathing room (labels were touching the edge)
    val cornerRadius = 30.dp
    val fabSize = 54.dp
    val podSize = 66.dp
    val podRaise = podSize / 2 // panel pushed down by half the pod so the pod sits above
    val barShape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Spacing.xl, vertical = Spacing.sm)
    ) {
        // Soft themed halo behind the panel, fading into the app background.
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            accent.copy(alpha = if (isDark) 0.20f else 0.14f),
                            accent.copy(alpha = 0.04f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Solid pill panel. The center + floats above it — no molded "pod" ring behind the
        // button, so there's no ugly filled bump/outline showing in the gap beneath the +.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = podRaise)
                .shadow(elevation = 14.dp, shape = barShape, clip = false)
                .clip(barShape)
                .background(panelBrush)
                .border(width = 1.dp, color = edgeColor, shape = barShape)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavTab(
                    icon = Icons.Default.Home,
                    label = "Home",
                    selected = selectedTab == BottomNavTab.Home,
                    accent = accent,
                    inactive = inactive,
                    modifier = Modifier.weight(1f)
                ) { onTabSelected(BottomNavTab.Home) }
                NavTab(
                    icon = Icons.Default.Search,
                    label = "Search",
                    selected = selectedTab == BottomNavTab.Search,
                    accent = accent,
                    inactive = inactive,
                    modifier = Modifier.weight(1f)
                ) { onTabSelected(BottomNavTab.Search) }
                // center slot reserved for the docked + button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                NavTab(
                    icon = Icons.Outlined.History,
                    label = "History",
                    selected = selectedTab == BottomNavTab.History,
                    accent = accent,
                    inactive = inactive,
                    modifier = Modifier.weight(1f)
                ) { onTabSelected(BottomNavTab.History) }
                NavTab(
                    icon = Icons.Default.Person,
                    label = "Profile",
                    selected = selectedTab == BottomNavTab.Profile,
                    accent = accent,
                    inactive = inactive,
                    modifier = Modifier.weight(1f)
                ) { onTabSelected(BottomNavTab.Profile) }
            }
        }

        // Floating center + button. Tapping toggles the add-menu; the + morphs into an ×.
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (podSize - fabSize) / 2)
                .size(fabSize)
                .shadow(elevation = 10.dp, shape = CircleShape, clip = false)
                .clip(CircleShape)
                .background(fabGreen)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onAddClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (addExpanded) "Close menu" else "Add",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .rotate(addRotation)
            )
        }
    }
}

@Composable
private fun NavTab(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    accent: Color,
    inactive: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val color = if (selected) accent else inactive
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(23.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
    }
}
