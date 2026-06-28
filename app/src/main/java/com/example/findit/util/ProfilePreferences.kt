package com.example.findit.util

import android.content.Context
import android.content.SharedPreferences

class ProfilePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getProfileImageUri(): String =
        prefs.getString(KEY_PROFILE_IMAGE_URI, "").orEmpty()

    fun setProfileImageUri(uri: String) {
        prefs.edit().putString(KEY_PROFILE_IMAGE_URI, uri).apply()
    }

    fun getDisplayName(): String =
        prefs.getString(KEY_DISPLAY_NAME, DEFAULT_DISPLAY_NAME) ?: DEFAULT_DISPLAY_NAME

    fun setDisplayName(name: String) {
        prefs.edit().putString(KEY_DISPLAY_NAME, name).apply()
    }

    fun getUsername(): String =
        prefs.getString(KEY_USERNAME, DEFAULT_USERNAME) ?: DEFAULT_USERNAME

    fun getBio(): String =
        prefs.getString(KEY_BIO, DEFAULT_BIO) ?: DEFAULT_BIO

    fun setProfileDetails(
        displayName: String,
        username: String,
        bio: String
    ) {
        prefs.edit()
            .putString(KEY_DISPLAY_NAME, displayName)
            .putString(KEY_USERNAME, username)
            .putString(KEY_BIO, bio)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "findit_profile_prefs"
        private const val KEY_PROFILE_IMAGE_URI = "profile_image_uri"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_USERNAME = "username"
        private const val KEY_BIO = "bio"
        const val DEFAULT_DISPLAY_NAME = "FindIt User"
        const val DEFAULT_USERNAME = "findit_user"
        const val DEFAULT_BIO = "Keeping everyday essentials organized and easy to find."
    }
}
