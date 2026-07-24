package com.example.findit.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.findit.ui.theme.ThemeMode

fun themeModeIcon(mode: ThemeMode): ImageVector = when (mode) {
    ThemeMode.Light -> Icons.Default.LightMode
    ThemeMode.Dark -> Icons.Default.DarkMode
    ThemeMode.Auto -> Icons.Default.BrightnessAuto
}

@Composable
fun ThemeModeHeaderButton(
    themeMode: ThemeMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(36.dp),
        shape = RoundedCornerShape(10.dp),
        color = containerColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = themeModeIcon(themeMode),
                contentDescription = "Theme mode",
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** Theme button + Dark / Auto / Light dropdown (shared by Home header and drawer). */
@Composable
fun ThemeModeHeaderControl(
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        ThemeModeHeaderButton(
            themeMode = themeMode,
            onClick = { menuExpanded = true },
            containerColor = containerColor,
            contentColor = contentColor
        )
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
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
                        menuExpanded = false
                    }
                )
            }
        }
    }
}
