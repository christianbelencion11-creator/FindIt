package com.example.findit.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class WeatherDay(
    val dateMillis: Long,
    val weekdayShort: String,
    val weekdayFull: String,
    val condition: String,
    val highC: Int,
    val lowC: Int,
    val precipChancePct: Int = 0
)

data class WeatherHour(
    val timeMillis: Long,
    val timeLabel: String,
    val condition: String,
    val temperatureC: Int,
    val precipChancePct: Int,
    val isNow: Boolean = false
)

data class WeatherSnapshot(
    val condition: String,
    val temperatureC: Int,
    val windKmh: Int = 0,
    val humidityPct: Int = 0,
    val precipChancePct: Int = 0,
    /** Today + upcoming forecast days (up to 7). */
    val days: List<WeatherDay> = emptyList(),
    /** Next hours for the Today strip. */
    val hours: List<WeatherHour> = emptyList(),
    /** Past days before today (most recent first). Kept for compatibility. */
    val previousDays: List<WeatherDay> = emptyList(),
    /** Reverse-geocoded "City, Province" label for the coordinates used (may be blank). */
    val locationName: String = ""
)

suspend fun fetchWeather(
    context: Context? = null,
    latitude: Double = 14.5995,
    longitude: Double = 120.9842
): WeatherSnapshot? = withContext(Dispatchers.IO) {
    runCatching {
        val url = URL(
            "https://api.open-meteo.com/v1/forecast" +
                "?latitude=$latitude&longitude=$longitude" +
                "&current=temperature_2m,weather_code,wind_speed_10m,relative_humidity_2m" +
                "&hourly=temperature_2m,weather_code,precipitation_probability" +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max" +
                "&forecast_days=7&timezone=auto"
        )
        val connection = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 8_000
            readTimeout = 8_000
            requestMethod = "GET"
        }
        connection.inputStream.bufferedReader().use { reader ->
            val json = JSONObject(reader.readText())
            val current = json.getJSONObject("current")
            val code = current.getInt("weather_code")
            val temp = current.getDouble("temperature_2m").toInt()
            val wind = current.optDouble("wind_speed_10m", 0.0).toInt()
            val humidity = current.optDouble("relative_humidity_2m", 0.0).toInt()
            val (thisWeek, previousWeek) = parseDailyForecastSplit(json)
            val hours = parseHourlyForecast(json)
            val precip = hours.firstOrNull { it.isNow }?.precipChancePct
                ?: thisWeek.firstOrNull()?.precipChancePct
                ?: 0
            val locationName = if (context != null) {
                reverseGeocodeLabel(context, latitude, longitude)
            } else {
                ""
            }
            WeatherSnapshot(
                condition = weatherCodeToLabel(code),
                temperatureC = temp,
                windKmh = wind,
                humidityPct = humidity,
                precipChancePct = precip,
                days = thisWeek,
                hours = hours,
                previousDays = previousWeek,
                locationName = locationName
            )
        }
    }.getOrNull()
}

private fun parseDailyForecastSplit(json: JSONObject): Pair<List<WeatherDay>, List<WeatherDay>> {
    if (!json.has("daily")) return emptyList<WeatherDay>() to emptyList()
    val daily = json.getJSONObject("daily")
    val times = daily.getJSONArray("time")
    val codes = daily.getJSONArray("weather_code")
    val highs = daily.getJSONArray("temperature_2m_max")
    val lows = daily.getJSONArray("temperature_2m_min")
    val precip = if (daily.has("precipitation_probability_max")) {
        daily.getJSONArray("precipitation_probability_max")
    } else {
        null
    }
    val isoParser = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }
    val weekdayShortFmt = SimpleDateFormat("EEE", Locale.getDefault())
    val weekdayFullFmt = SimpleDateFormat("EEEE", Locale.getDefault())
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val all = buildList {
        val count = minOf(times.length(), codes.length(), highs.length(), lows.length())
        for (i in 0 until count) {
            val dateStr = times.getString(i)
            val date = isoParser.parse(dateStr) ?: continue
            add(
                WeatherDay(
                    dateMillis = date.time,
                    weekdayShort = weekdayShortFmt.format(date),
                    weekdayFull = weekdayFullFmt.format(date),
                    condition = weatherCodeToLabel(codes.getInt(i)),
                    highC = highs.getDouble(i).toInt(),
                    lowC = lows.getDouble(i).toInt(),
                    precipChancePct = precip?.optDouble(i, 0.0)?.toInt() ?: 0
                )
            )
        }
    }

    val thisWeek = all.filter { it.dateMillis >= todayStart }.take(7)
    val previousWeek = all
        .filter { it.dateMillis < todayStart }
        .sortedByDescending { it.dateMillis }
        .take(7)
    return thisWeek to previousWeek
}

private fun parseHourlyForecast(json: JSONObject): List<WeatherHour> {
    if (!json.has("hourly")) return emptyList()
    val hourly = json.getJSONObject("hourly")
    val times = hourly.getJSONArray("time")
    val temps = hourly.getJSONArray("temperature_2m")
    val codes = hourly.getJSONArray("weather_code")
    val precip = if (hourly.has("precipitation_probability")) {
        hourly.getJSONArray("precipitation_probability")
    } else {
        null
    }
    val isoParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }
    val timeLabelFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    val now = System.currentTimeMillis()
    val count = minOf(times.length(), temps.length(), codes.length())
    val parsed = buildList {
        for (i in 0 until count) {
            val date = isoParser.parse(times.getString(i)) ?: continue
            add(
                WeatherHour(
                    timeMillis = date.time,
                    timeLabel = timeLabelFmt.format(date),
                    condition = weatherCodeToLabel(codes.getInt(i)),
                    temperatureC = temps.getDouble(i).toInt(),
                    precipChancePct = precip?.optDouble(i, 0.0)?.toInt() ?: 0,
                    isNow = false
                )
            )
        }
    }
    // Prefer from current hour forward; take 12 slots.
    val upcoming = parsed.filter { it.timeMillis + 59 * 60_000L >= now }.take(12)
    if (upcoming.isEmpty()) return parsed.take(12)
    return upcoming.mapIndexed { index, hour ->
        hour.copy(isNow = index == 0)
    }
}

fun weatherCodeToLabel(code: Int): String = when (code) {
    0 -> "Clear"
    1, 2, 3 -> "Partly cloudy"
    45, 48 -> "Foggy"
    51, 53, 55 -> "Drizzle"
    56, 57 -> "Freezing drizzle"
    61, 63, 65 -> "Rainy"
    66, 67 -> "Freezing rain"
    71, 73, 75 -> "Snowy"
    77 -> "Snow grains"
    80, 81, 82 -> "Showers"
    85, 86 -> "Snow showers"
    95 -> "Thunderstorm"
    96, 99 -> "Storm"
    else -> "Cloudy"
}
