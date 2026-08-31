package com.example.iremember.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.iremember.ui.theme.isAppDarkTheme

/**
 * Full-screen add-menu overlay: a scrim plus a speed-dial (Microphone · Notes · Add item)
 * anchored just above the docked center "+". Render this beneath the bottom bar so the
 * +/× button stays bright and tappable on top of the scrim.
 */
@Composable
fun AddSpeedDialMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onAddItem: () -> Unit,
    onNotes: () -> Unit,
    onCards: () -> Unit,
    onMicrophone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(180))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .pointerInput(Unit) { detectTapGestures { onDismiss() } }
            )
        }

        AnimatedVisibility(
            visible = expanded,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 128.dp),
            enter = fadeIn(tween(200)) + slideInVertically(tween(220)) { it / 2 },
            exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { it / 2 }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AddMenuAction(
                    icon = Icons.Default.Mic,
                    label = "Microphone",
                    accent = Color(0xFF3B82F6),
                    onClick = onMicrophone
                )
                AddMenuAction(
                    icon = Icons.AutoMirrored.Filled.Notes,
                    label = "Notes",
                    accent = Color(0xFF0D9488),
                    onClick = onNotes
                )
                AddMenuAction(
                    icon = Icons.Outlined.AccountBalanceWallet,
                    label = "Wallet",
                    accent = Color(0xFF6366F1),
                    onClick = onCards
                )
                AddMenuAction(
                    icon = Icons.Default.PostAdd,
                    label = "Add item",
                    accent = MaterialTheme.colorScheme.primary,
                    onClick = onAddItem
                )
            }
        }
    }
}

@Composable
private fun AddMenuAction(
    icon: ImageVector,
    label: String,
    accent: Color,
    onClick: () -> Unit
) {
    val dark = isAppDarkTheme()
    val pillColor = if (dark) Color(0xFF16211B) else Color.White
    val pillBorder = if (dark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.06f)
    val pillShape = RoundedCornerShape(30.dp)

    Row(
        modifier = Modifier
            .shadow(10.dp, pillShape)
            .clip(pillShape)
            .background(pillColor)
            .border(1.dp, pillBorder, pillShape)
            .clickable { onClick() }
            .padding(start = 10.dp, end = 22.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(accent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
