package com.example.iremember.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.iremember.ui.theme.Spacing
import com.example.iremember.util.WeatherDay
import com.example.iremember.util.WeatherHour
import com.example.iremember.util.WeatherSnapshot
import kotlinx.coroutines.launch

private val WeatherDeepBase = Color(0xFF052E1C)
private val WeatherGradientTop = Color(0xFF4ADE80)
private val WeatherGradientMid = Color(0xFF22C55E)
private val WeatherGradientBottom = Color(0xFF15803D)
private val WeatherConditionAccent = Color(0xFFFDE68A)
private val WeatherMuted = Color(0xFFE8E8E8)
private val WeatherHourIdle = Color(0xFF14532D)
private val WeatherUpdating = Color(0xFF5BF87D)
private val WeatherKnowMore = Color(0xFF252020)

/** Kept for callers that still reference sheet container colors. */
val WeatherSheetContainerLight = Color(0xFFECFDF5)
val WeatherSheetContainerDark = WeatherDeepBase

@Composable
fun weatherSheetContainerColor(): Color = WeatherDeepBase

@Composable
fun HomeWeatherSheetContent(
    dateHeadline: String,
    weather: WeatherSnapshot?,
    loading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showWeek by remember { mutableStateOf(false) }
    val sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val greenBrush = Brush.verticalGradient(
        colors = listOf(WeatherGradientTop, WeatherGradientMid, WeatherGradientBottom)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(sheetShape)
            .background(WeatherDeepBase)
    ) {
        when {
            loading && weather == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(greenBrush),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            weather == null -> {
                WeatherUnavailablePane(onRefresh = onRefresh)
            }
            showWeek -> {
                WeatherWeekPane(
                    weather = weather,
                    loading = loading,
                    onBack = { showWeek = false },
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                WeatherHomePane(
                    dateHeadline = dateHeadline,
                    weather = weather,
                    loading = loading,
                    onRefresh = onRefresh,
                    onKnowMore = { showWeek = true },
                    greenBrush = greenBrush,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun WeatherHomePane(
    dateHeadline: String,
    weather: WeatherSnapshot,
    loading: Boolean,
    onRefresh: () -> Unit,
    onKnowMore: () -> Unit,
    greenBrush: Brush,
    modifier: Modifier = Modifier
) {
    val scroll = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(greenBrush)
                .padding(horizontal = Spacing.xl)
                .padding(top = Spacing.md, bottom = Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onRefresh, enabled = !loading) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = "Metro Manila",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(Spacing.sm))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x99252020))
                    .padding(horizontal = Spacing.md, vertical = Spacing.xxs)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = WeatherUpdating
                        )
                        Spacer(modifier = Modifier.width(Spacing.xs))
                    }
                    Text(
                        text = if (loading) "Updating…" else "Up to date",
                        color = WeatherUpdating,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))
            AnimatedWeatherIcon(
                condition = weather.condition,
                size = 168.dp,
                onDarkGradient = true
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "${weather.temperatureC}",
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    lineHeight = 96.sp
                )
                Text(
                    text = "°",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(top = Spacing.md)
                )
            }
            Text(
                text = weather.condition,
                fontSize = 36.sp,
                fontWeight = FontWeight.Medium,
                color = WeatherConditionAccent,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = dateHeadline,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = WeatherMuted
            )

            Spacer(modifier = Modifier.height(Spacing.lg))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .padding(horizontal = Spacing.sm)
                    .background(Color.White.copy(alpha = 0.35f))
            )
            Spacer(modifier = Modifier.height(Spacing.lg))
            MetricsRow(
                windKmh = weather.windKmh,
                humidityPct = weather.humidityPct,
                precipChancePct = weather.precipChancePct
            )
            Spacer(modifier = Modifier.height(Spacing.xl))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(WeatherKnowMore)
                    .clickable(onClick = onKnowMore)
                    .padding(horizontal = Spacing.xl, vertical = Spacing.sm)
            ) {
                Text(
                    text = "Know More",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = WeatherMuted
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(WeatherDeepBase)
                .padding(horizontal = Spacing.xl)
                .padding(top = Spacing.xl, bottom = Spacing.xxl)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Row(
                    modifier = Modifier.clickable(onClick = onKnowMore),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "7 days",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            HourlyStrip(hours = weather.hours)
        }
    }
}

@Composable
private fun WeatherWeekPane(
    weather: WeatherSnapshot,
    loading: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    val tomorrow = weather.days.getOrNull(1) ?: weather.days.firstOrNull()
    val greenBrush = Brush.verticalGradient(
        colors = listOf(WeatherGradientTop, WeatherGradientMid, WeatherGradientBottom)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(greenBrush)
                .padding(horizontal = Spacing.xl)
                .padding(top = Spacing.md, bottom = Spacing.xl)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(
                    text = "7 Days",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onRefresh, enabled = !loading) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))
            if (tomorrow != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedWeatherIcon(
                        condition = tomorrow.condition,
                        size = 112.dp,
                        onDarkGradient = true
                    )
                    Spacer(modifier = Modifier.width(Spacing.md))
                    Column {
                        Text(
                            text = "Tomorrow",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${tomorrow.highC}",
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                lineHeight = 64.sp
                            )
                            Text(
                                text = "/${tomorrow.lowC}°",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.padding(bottom = Spacing.sm)
                            )
                        }
                        Text(
                            text = tomorrow.condition,
                            style = MaterialTheme.typography.titleMedium,
                            color = WeatherMuted
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.lg))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.35f))
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                MetricsRow(
                    windKmh = weather.windKmh,
                    humidityPct = weather.humidityPct,
                    precipChancePct = tomorrow.precipChancePct.takeIf { it > 0 }
                        ?: weather.precipChancePct
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(WeatherDeepBase)
                .padding(horizontal = Spacing.xl)
                .padding(top = Spacing.lg, bottom = Spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            weather.days.forEach { day ->
                WeekDayRow(day = day)
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(50))
                    .background(WeatherGradientBottom)
                    .clickable { scope.launch { scroll.animateScrollTo(0) } }
                    .padding(horizontal = Spacing.xl, vertical = Spacing.sm)
            ) {
                Text(
                    text = "7-day forecast",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = WeatherMuted
                )
            }
        }
    }
}

@Composable
private fun WeekDayRow(day: WeatherDay) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = day.weekdayFull,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.width(118.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        AnimatedWeatherIcon(
            condition = day.condition,
            size = 28.dp,
            onDarkGradient = true
        )
        Spacer(modifier = Modifier.width(Spacing.sm))
        Text(
            text = day.condition,
            style = MaterialTheme.typography.bodyMedium,
            color = WeatherMuted,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${day.highC}°",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = WeatherMuted
        )
    }
}

@Composable
private fun MetricsRow(
    windKmh: Int,
    humidityPct: Int,
    precipChancePct: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MetricCell(
            icon = Icons.Filled.Air,
            value = "$windKmh km/h",
            label = "Wind"
        )
        MetricCell(
            icon = Icons.Filled.WaterDrop,
            value = "$humidityPct%",
            label = "Humidity"
        )
        MetricCell(
            icon = Icons.Outlined.Umbrella,
            value = "$precipChancePct%",
            label = "Chance of rain"
        )
    }
}

@Composable
private fun MetricCell(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(Spacing.xxs))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = WeatherMuted
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = WeatherMuted.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun HourlyStrip(hours: List<WeatherHour>) {
    if (hours.isEmpty()) {
        Text(
            text = "Hourly forecast unavailable",
            style = MaterialTheme.typography.bodyMedium,
            color = WeatherMuted.copy(alpha = 0.7f)
        )
        return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        hours.forEach { hour ->
            HourlyCard(hour = hour)
        }
    }
}

@Composable
private fun HourlyCard(hour: WeatherHour) {
    val shape = RoundedCornerShape(28.dp)
    val bg = if (hour.isNow) {
        Brush.verticalGradient(listOf(WeatherGradientBottom, WeatherGradientTop))
    } else {
        Brush.verticalGradient(listOf(WeatherHourIdle, WeatherHourIdle))
    }
    Column(
        modifier = Modifier
            .width(if (hour.isNow) 86.dp else 78.dp)
            .height(if (hour.isNow) 118.dp else 108.dp)
            .clip(shape)
            .background(bg)
            .then(
                if (hour.isNow) {
                    Modifier.border(1.dp, Color.White.copy(alpha = 0.25f), shape)
                } else {
                    Modifier
                }
            )
            .padding(vertical = Spacing.md, horizontal = Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${hour.precipChancePct}%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        AnimatedWeatherIcon(
            condition = hour.condition,
            size = 36.dp,
            onDarkGradient = true
        )
        Text(
            text = hour.timeLabel,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
private fun WeatherUnavailablePane(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(WeatherGradientTop, WeatherGradientBottom)
                )
            )
            .padding(Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.CloudOff,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        Text(
            text = "Weather unavailable",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        Text(
            text = "Check your connection and try again.",
            style = MaterialTheme.typography.bodyMedium,
            color = WeatherMuted
        )
        Spacer(modifier = Modifier.height(Spacing.lg))
        TextButton(onClick = onRefresh) {
            Icon(Icons.Outlined.Refresh, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text("Refresh", color = Color.White)
        }
    }
}
