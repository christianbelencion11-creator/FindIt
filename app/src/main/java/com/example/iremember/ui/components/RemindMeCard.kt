package com.example.iremember.ui.components

import android.app.TimePickerDialog
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.iremember.ui.theme.Dimensions
import com.example.iremember.ui.theme.Spacing
import com.example.iremember.util.ReminderTimeUtils
import com.example.iremember.util.formatDateTime

/**
 * Remind Me card — toggle + time picker. Use [onEnable] when turning on / saving time;
 * [onSnooze]/[onStop] when reminder is active on an existing item.
 */
@Composable
fun RemindMeCard(
    enabled: Boolean,
    hour: Int,
    minute: Int,
    nextAt: Long = 0L,
    isActive: Boolean = false,
    showActions: Boolean = false,
    onToggle: (Boolean) -> Unit,
    onTimePicked: (hour: Int, minute: Int) -> Unit,
    onEnable: () -> Unit = {},
    onSnooze: () -> Unit = {},
    onStop: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var localHour by remember(hour) { mutableIntStateOf(hour) }
    var localMinute by remember(minute) { mutableIntStateOf(minute) }
    var pendingEnable by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && pendingEnable) {
            pendingEnable = false
            onToggle(true)
            onEnable()
        } else {
            pendingEnable = false
        }
    }

    fun requestEnable() {
        if (Build.VERSION.SDK_INT >= 33) {
            pendingEnable = true
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onToggle(true)
            onEnable()
        }
    }

    fun openTimePicker() {
        TimePickerDialog(
            context,
            { _, h, m ->
                localHour = h
                localMinute = m
                onTimePicked(h, m)
                if (enabled || isActive) {
                    onEnable()
                }
            },
            localHour,
            localMinute,
            false
        ).show()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimensions.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "Remind Me",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Notify until you snooze or stop",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = enabled || isActive,
                    onCheckedChange = { checked ->
                        if (checked) {
                            requestEnable()
                        } else {
                            onToggle(false)
                            onStop()
                        }
                    }
                )
            }

            if (enabled || isActive) {
                Spacer(Modifier.height(Spacing.md))
                OutlinedButton(
                    onClick = { openTimePicker() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = Spacing.sm)
                            .size(20.dp)
                    )
                    Text(
                        text = "Notify at ${ReminderTimeUtils.formatTime(localHour, localMinute)}"
                    )
                }
                if (nextAt > 0L && isActive) {
                    Text(
                        text = "Next: ${formatDateTime(nextAt)} · repeats every 15 min",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Spacing.sm)
                    )
                }
                if (showActions && isActive) {
                    Spacer(Modifier.height(Spacing.md))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        OutlinedButton(
                            onClick = onSnooze,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Snooze 1h")
                        }
                        Button(
                            onClick = onStop,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Stop")
                        }
                    }
                }
            }
        }
    }
}
