package com.example.findit.util

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ProfileData(
    val imageUri: String = "",
    val displayName: String = ProfilePreferences.DEFAULT_DISPLAY_NAME,
    val username: String = ProfilePreferences.DEFAULT_USERNAME,
    val bio: String = ProfilePreferences.DEFAULT_BIO,
    val fullName: String = "",
    val birthday: String = "",
    val family: String = "",
    val phone: String = "",
    val location: String = ""
)

/**
 * Single source of truth for profile UI — always reads from SharedPreferences.
 */
class ProfileStore(context: Context) {

    val preferences = ProfilePreferences(context.applicationContext)
    private val appContext = context.applicationContext

    private val _state = MutableStateFlow(readFromDisk())
    val state: StateFlow<ProfileData> = _state.asStateFlow()

    fun reload() {
        _state.value = readFromDisk()
    }

    fun updateImage(sourceUri: String) {
        val uid = preferences.getLinkedFirebaseUid().takeIf { it.isNotBlank() }
        val persisted = ProfileImageStorage.persist(appContext, sourceUri, uid)
        preferences.setProfileImageUri(persisted)
        if (persisted.isNotBlank()) {
            preferences.markCustomized()
        }
        reload()
    }

    fun updateDetails(displayName: String, username: String, bio: String) {
        preferences.setProfileDetails(displayName, username, bio)
        reload()
    }

    fun updatePersonalDetails(
        displayName: String,
        username: String,
        bio: String,
        fullName: String,
        birthday: String,
        family: String,
        phone: String,
        location: String
    ) {
        preferences.setProfileDetails(displayName, username, bio)
        preferences.setPersonalDetails(fullName, birthday, family, phone, location)
        reload()
    }

    fun seedFromAuthIfNeeded(displayName: String, username: String, bio: String) {
        preferences.seedFromAuth(displayName, username, bio)
        reload()
    }

    fun syncFromAuthUser(
        firebaseUid: String,
        displayName: String,
        username: String,
        photoUrl: String? = null
    ) {
        preferences.applyAuthUser(firebaseUid, displayName, username, photoUrl)
        reload()
    }

    private fun readFromDisk(): ProfileData {
        val uid = preferences.getLinkedFirebaseUid().takeIf { it.isNotBlank() }
        var imageUri = preferences.getProfileImageUri()
        if (imageUri.startsWith("content://")) {
            val persisted = ProfileImageStorage.persist(appContext, imageUri, uid)
            if (persisted.isNotBlank() && persisted != imageUri) {
                preferences.setProfileImageUri(persisted)
                imageUri = persisted
            }
        }
        return ProfileData(
            imageUri = imageUri,
            displayName = preferences.getDisplayName(),
            username = preferences.getUsername(),
            bio = preferences.getBio(),
            fullName = preferences.getFullName(),
            birthday = preferences.getBirthday(),
            family = preferences.getFamily(),
            phone = preferences.getPhone(),
            location = preferences.getLocation()
        )
    }
}
