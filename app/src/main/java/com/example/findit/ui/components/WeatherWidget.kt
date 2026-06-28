package com.example.findit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.findit.ui.theme.Spacing
import com.example.findit.util.WeatherSnapshot
import com.example.findit.util.fetchWeather

@Composable
fun WeatherWidget(modifier: Modifier = Modifier) {
    var weather by remember { mutableStateOf<WeatherSnapshot?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        loading = true
        weather = fetchWeather()
        loading = false
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f))
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        contentAlignment = Alignment.Center
    ) {
        when {
            loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
            }
            weather != null -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Icon(
                        imageVector = weatherIconFor(weather!!.condition),
                        contentDescription = weather!!.condition,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = weather!!.condition,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f)
                        )
                        Text(
                            text = "${weather!!.temperatureC}°",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            else -> {
                Text(
                    text = "Weather\nunavailable",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f)
                )
            }
        }
    }
}

private fun weatherIconFor(condition: String): ImageVector = when {
    condition.contains("Clear", ignoreCase = true) -> Icons.Default.WbSunny
    condition.contains("Rain", ignoreCase = true) ||
        condition.contains("Drizzle", ignoreCase = true) ||
        condition.contains("Shower", ignoreCase = true) -> Icons.Default.Grain
    condition.contains("Storm", ignoreCase = true) ||
        condition.contains("Thunder", ignoreCase = true) -> Icons.Default.Thunderstorm
    else -> Icons.Default.Cloud
}
