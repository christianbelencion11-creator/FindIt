package com.example.iremember.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/** A simple lat/lng pair resolved from the device. */
data class DeviceLocation(val latitude: Double, val longitude: Double)

/** True if the app currently holds either coarse or fine location permission. */
fun hasLocationPermission(context: Context): Boolean {
    val coarse = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val fine = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return coarse || fine
}

/**
 * Best-effort device location using the framework [LocationManager]'s cached
 * last-known fix (no active GPS request, no extra dependency). Returns null when
 * permission is missing or no provider has a cached fix — callers fall back to a
 * default location so weather still works.
 */
@SuppressLint("MissingPermission")
suspend fun lastKnownDeviceLocation(context: Context): DeviceLocation? =
    withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) return@withContext null
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return@withContext null
        var best: Location? = null
        for (provider in manager.getProviders(true)) {
            val fix = runCatching { manager.getLastKnownLocation(provider) }.getOrNull() ?: continue
            if (best == null || fix.accuracy < best!!.accuracy) best = fix
        }
        best?.let { DeviceLocation(it.latitude, it.longitude) }
    }

/**
 * Turns coordinates into a short city label via reverse geocoding.
 * Only the city/locality is returned (no province/region), so the weather chip stays
 * compact. Returns an empty string when geocoding is unavailable or yields nothing, so the
 * UI can simply hide the location line.
 */
suspend fun reverseGeocodeLabel(context: Context, latitude: Double, longitude: Double): String =
    withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) return@withContext ""
        runCatching {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val address = geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
                ?: return@runCatching ""
            // City name only — keeps the header weather chip from crowding the greeting.
            val city = address.locality
                ?: address.subLocality
                ?: address.subAdminArea
                ?: address.adminArea
            city ?: address.countryName ?: ""
        }.getOrDefault("")
    }
