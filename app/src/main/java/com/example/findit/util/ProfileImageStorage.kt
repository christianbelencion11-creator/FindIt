package com.example.findit.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

/**
 * Copies a picked gallery URI into app-internal storage so the profile photo
 * survives process death and content-provider permission expiry.
 * Each Firebase UID gets its own file so account switching does not overwrite photos.
 */
object ProfileImageStorage {

    private const val LEGACY_PROFILE_FILE_NAME = "profile_photo.jpg"

    /** Strip cache-bust query (e.g. `?v=123`) from a stored path. */
    fun stripCacheBust(path: String): String = path.substringBefore("?")

    fun withCacheBust(absolutePath: String): String =
        "${stripCacheBust(absolutePath)}?v=${System.currentTimeMillis()}"

    fun persist(context: Context, sourceUri: String, firebaseUid: String? = null): String {
        if (sourceUri.isBlank()) return ""

        val cleanSource = stripCacheBust(sourceUri)
        val dest = File(context.filesDir, fileNameForUid(firebaseUid))
        if (cleanSource == dest.absolutePath || cleanSource == "file://${dest.absolutePath}") {
            return dest.absolutePath
        }

        return try {
            val uri = Uri.parse(cleanSource)
            context.contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            } ?: run {
                // Absolute file path fallback
                val srcFile = File(cleanSource)
                if (srcFile.exists()) {
                    srcFile.inputStream().use { input ->
                        dest.outputStream().use { output -> input.copyTo(output) }
                    }
                } else {
                    return ""
                }
            }
            dest.absolutePath
        } catch (_: Exception) {
            ""
        }
    }

    fun persistBitmap(
        context: Context,
        bitmap: Bitmap,
        firebaseUid: String? = null,
        quality: Int = 92
    ): String {
        val dest = File(context.filesDir, fileNameForUid(firebaseUid))
        return try {
            FileOutputStream(dest).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            dest.absolutePath
        } catch (_: Exception) {
            ""
        }
    }

    fun fileNameForUid(firebaseUid: String?): String {
        val safeUid = firebaseUid?.takeIf { it.isNotBlank() }?.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        return if (safeUid.isNullOrBlank()) {
            LEGACY_PROFILE_FILE_NAME
        } else {
            "profile_photo_$safeUid.jpg"
        }
    }
}
