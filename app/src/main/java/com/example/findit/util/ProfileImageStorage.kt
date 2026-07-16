package com.example.findit.util

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * Copies a picked gallery URI into app-internal storage so the profile photo
 * survives process death and content-provider permission expiry.
 * Each Firebase UID gets its own file so account switching does not overwrite photos.
 */
object ProfileImageStorage {

    private const val LEGACY_PROFILE_FILE_NAME = "profile_photo.jpg"

    fun persist(context: Context, sourceUri: String, firebaseUid: String? = null): String {
        if (sourceUri.isBlank()) return ""

        val dest = File(context.filesDir, fileNameForUid(firebaseUid))
        if (sourceUri == dest.absolutePath || sourceUri == "file://${dest.absolutePath}") {
            return dest.absolutePath
        }

        return try {
            context.contentResolver.openInputStream(Uri.parse(sourceUri))?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            }
            dest.absolutePath
        } catch (_: Exception) {
            sourceUri
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
