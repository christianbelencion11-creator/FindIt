package com.example.findit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.findit.ui.theme.Spacing
import com.example.findit.util.WeatherSnapshot

@Composable
fun WeatherWidget(
    weather: WeatherSnapshot?,
    loading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
            contentAlignment = Alignment.Center
        ) {
            when {
                loading && weather == null -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                }
                weather != null -> {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            AnimatedWeatherIcon(
                                condition = weather.condition,
                                size = 28.dp,
                                onDarkGreenHeader = true
                            )
                            Column {
                                Text(
                                    text = weather.condition,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f)
                                )
                                Text(
                                    text = "${weather.temperatureC}°",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        // Location the weather reflects, so the user knows what place is shown.
                        if (weather.locationName.isNotBlank()) {
                            Row(
                                modifier = Modifier.padding(top = Spacing.xs),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = weather.locationName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 132.dp)
                                )
                            }
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
}

/** Static Material mapping kept for any non-animated call sites. */
fun weatherIconForCondition(condition: String): ImageVector = when (weatherKindForCondition(condition)) {
    WeatherIconKind.Clear -> Icons.Default.WbSunny
    WeatherIconKind.Rain -> Icons.Default.Grain
    WeatherIconKind.Storm -> Icons.Default.Thunderstorm
    WeatherIconKind.Cloudy -> Icons.Default.Cloud
}
