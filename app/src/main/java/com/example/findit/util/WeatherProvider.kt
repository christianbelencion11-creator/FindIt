package com.example.findit.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class WeatherSnapshot(
    val condition: String,
    val temperatureC: Int
)

suspend fun fetchWeather(
    latitude: Double = 14.5995,
    longitude: Double = 120.9842
): WeatherSnapshot? = withContext(Dispatchers.IO) {
    runCatching {
        val url = URL(
            "https://api.open-meteo.com/v1/forecast" +
                "?latitude=$latitude&longitude=$longitude" +
                "&current=temperature_2m,weather_code&timezone=auto"
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
            WeatherSnapshot(
                condition = weatherCodeToLabel(code),
                temperatureC = temp
            )
        }
    }.getOrNull()
}

private fun weatherCodeToLabel(code: Int): String = when (code) {
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
