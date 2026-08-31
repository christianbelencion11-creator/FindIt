package com.example.iremember.util

import android.content.Context
import android.content.SharedPreferences

class ProfilePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        migrateLegacyProfileIfNeeded()
    }

    fun getProfileImageUri(): String {
        readActiveUid()?.let { uid ->
            val forUid = getProfileImageUriForUid(uid)
            if (forUid.isNotBlank()) return forUid
        }
        return prefs.getString(KEY_FALLBACK_PROFILE_IMAGE_URI, "").orEmpty()
    }

    fun setProfileImageUri(uri: String) {
        val uid = readActiveUid()
        if (uid != null) {
            prefs.edit().putString(keyImage(uid), uri).commit()
        } else {
            prefs.edit().putString(KEY_FALLBACK_PROFILE_IMAGE_URI, uri).commit()
        }
    }

    fun markCustomized() {
        val uid = readActiveUid()
        if (uid != null) {
            prefs.edit().putBoolean(keyCustomized(uid), true).commit()
        } else {
            prefs.edit().putBoolean(KEY_FALLBACK_PROFILE_CUSTOMIZED, true).commit()
        }
    }

    fun getDisplayName(): String =
        readActiveUid()?.let { getDisplayNameForUid(it) } ?: DEFAULT_DISPLAY_NAME

    fun setDisplayName(name: String) {
        val uid = requireActiveUid() ?: return
        prefs.edit().putString(keyDisplayName(uid), name).commit()
    }

    fun getUsername(): String =
        readActiveUid()?.let { getUsernameForUid(it) } ?: DEFAULT_USERNAME

    fun getBio(): String =
        readActiveUid()?.let { getBioForUid(it) } ?: DEFAULT_BIO

    fun getFullName(): String =
        readActiveUid()?.let { prefs.getString(keyFullName(it), "").orEmpty() }.orEmpty()

    fun getBirthday(): String =
        readActiveUid()?.let { prefs.getString(keyBirthday(it), "").orEmpty() }.orEmpty()

    fun getFamily(): String =
        readActiveUid()?.let { prefs.getString(keyFamily(it), "").orEmpty() }.orEmpty()

    fun getPhone(): String =
        readActiveUid()?.let { prefs.getString(keyPhone(it), "").orEmpty() }.orEmpty()

    fun getLocation(): String =
        readActiveUid()?.let { prefs.getString(keyLocation(it), "").orEmpty() }.orEmpty()

    fun setPersonalDetails(
        fullName: String,
        birthday: String,
        family: String,
        phone: String,
        location: String
    ) {
        val uid = requireActiveUid() ?: return
        prefs.edit()
            .putString(keyFullName(uid), fullName.trim())
            .putString(keyBirthday(uid), birthday.trim())
            .putString(keyFamily(uid), family.trim())
            .putString(keyPhone(uid), phone.trim())
            .putString(keyLocation(uid), location.trim())
            .putBoolean(keyCustomized(uid), true)
            .commit()
    }

    fun isProfileCustomized(): Boolean {
        val uid = readActiveUid() ?: return false
        return isProfileCustomizedForUid(uid)
    }

    fun setProfileDetails(
        displayName: String,
        username: String,
        bio: String
    ) {
        val uid = requireActiveUid() ?: return
        prefs.edit()
            .putString(keyDisplayName(uid), displayName)
            .putString(keyUsername(uid), username)
            .putString(keyBio(uid), bio)
            .putBoolean(keyCustomized(uid), true)
            .commit()
    }

    fun getLinkedFirebaseUid(): String =
        prefs.getString(KEY_LINKED_FIREBASE_UID, "").orEmpty()

    /**
     * Switches to the signed-in Firebase user and loads that user's saved profile.
     * If the user has no saved profile yet, seeds from Firebase Auth without
     * overwriting other accounts' stored profiles.
     */
    fun applyAuthUser(
        firebaseUid: String,
        displayName: String,
        username: String,
        photoUri: String? = null
    ) {
        if (firebaseUid.isBlank()) return

        val editor = prefs.edit().putString(KEY_LINKED_FIREBASE_UID, firebaseUid)
        val hasSavedProfile = hasStoredProfileForUid(firebaseUid)

        if (!hasSavedProfile) {
            editor
                .putString(keyDisplayName(firebaseUid), displayName)
                .putString(keyUsername(firebaseUid), username)
                .putString(keyBio(firebaseUid), DEFAULT_BIO)
                .putBoolean(keyCustomized(firebaseUid), false)
            if (!photoUri.isNullOrBlank()) {
                editor.putString(keyImage(firebaseUid), photoUri)
            }
        } else if (!isProfileCustomizedForUid(firebaseUid)) {
            editor
                .putString(keyDisplayName(firebaseUid), displayName)
                .putString(keyUsername(firebaseUid), username)
                .putString(keyBio(firebaseUid), DEFAULT_BIO)
        }
        editor.commit()
    }

    /** Seeds profile from auth on first sign-up only — does not mark as user-customized. */
    fun seedFromAuth(displayName: String, username: String, bio: String) {
        val uid = requireActiveUid() ?: return
        if (isProfileCustomizedForUid(uid)) return
        prefs.edit()
            .putString(keyDisplayName(uid), displayName)
            .putString(keyUsername(uid), username)
            .putString(keyBio(uid), bio)
            .commit()
    }

    private fun readActiveUid(): String? =
        getLinkedFirebaseUid().takeIf { it.isNotBlank() }

    private fun requireActiveUid(): String? = readActiveUid()

    private fun hasStoredProfileForUid(uid: String): Boolean {
        return prefs.contains(keyDisplayName(uid)) ||
            prefs.contains(keyUsername(uid)) ||
            prefs.contains(keyBio(uid)) ||
            prefs.contains(keyImage(uid)) ||
            prefs.contains(keyCustomized(uid))
    }

    private fun getProfileImageUriForUid(uid: String): String =
        prefs.getString(keyImage(uid), "").orEmpty()

    private fun getDisplayNameForUid(uid: String): String =
        prefs.getString(keyDisplayName(uid), null)
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_DISPLAY_NAME

    private fun getUsernameForUid(uid: String): String =
        prefs.getString(keyUsername(uid), null)
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_USERNAME

    private fun getBioForUid(uid: String): String =
        prefs.getString(keyBio(uid), null)
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_BIO

    private fun isProfileCustomizedForUid(uid: String): Boolean {
        if (prefs.getBoolean(keyCustomized(uid), false)) return true
        val savedName = prefs.getString(keyDisplayName(uid), null)
        val savedUser = prefs.getString(keyUsername(uid), null)
        val savedBio = prefs.getString(keyBio(uid), null)
        val savedImage = prefs.getString(keyImage(uid), null)
        if (!savedName.isNullOrBlank() && savedName != DEFAULT_DISPLAY_NAME) return true
        if (!savedUser.isNullOrBlank() && savedUser != DEFAULT_USERNAME) return true
        if (!savedBio.isNullOrBlank() && savedBio != DEFAULT_BIO) return true
        if (!savedImage.isNullOrBlank()) return true
        return false
    }

    /**
     * Copies legacy global profile keys into the linked UID profile once.
     */
    private fun migrateLegacyProfileIfNeeded() {
        if (prefs.getBoolean(KEY_LEGACY_MIGRATED, false)) return

        val linkedUid = prefs.getString(KEY_LINKED_FIREBASE_UID, null).orEmpty()
        val legacyName = prefs.getString(KEY_DISPLAY_NAME, null)
        val legacyUser = prefs.getString(KEY_USERNAME, null)
        val legacyBio = prefs.getString(KEY_BIO, null)
        val legacyImage = prefs.getString(KEY_PROFILE_IMAGE_URI, null)
        val legacyCustomized = prefs.getBoolean(KEY_PROFILE_CUSTOMIZED, false)

        val hasLegacyData = !legacyName.isNullOrBlank() ||
            !legacyUser.isNullOrBlank() ||
            !legacyBio.isNullOrBlank() ||
            !legacyImage.isNullOrBlank() ||
            legacyCustomized

        val editor = prefs.edit().putBoolean(KEY_LEGACY_MIGRATED, true)

        if (linkedUid.isNotBlank() && hasLegacyData && !hasStoredProfileForUid(linkedUid)) {
            legacyName?.let { editor.putString(keyDisplayName(linkedUid), it) }
            legacyUser?.let { editor.putString(keyUsername(linkedUid), it) }
            legacyBio?.let { editor.putString(keyBio(linkedUid), it) }
            legacyImage?.let { editor.putString(keyImage(linkedUid), it) }
            editor.putBoolean(keyCustomized(linkedUid), legacyCustomized)
        }

        editor
            .remove(KEY_PROFILE_IMAGE_URI)
            .remove(KEY_DISPLAY_NAME)
            .remove(KEY_USERNAME)
            .remove(KEY_BIO)
            .remove(KEY_PROFILE_CUSTOMIZED)
            .commit()
    }

    private fun keyDisplayName(uid: String) = "profile.$uid.display_name"
    private fun keyUsername(uid: String) = "profile.$uid.username"
    private fun keyBio(uid: String) = "profile.$uid.bio"
    private fun keyImage(uid: String) = "profile.$uid.image_uri"
    private fun keyCustomized(uid: String) = "profile.$uid.customized"
    private fun keyFullName(uid: String) = "profile.$uid.full_name"
    private fun keyBirthday(uid: String) = "profile.$uid.birthday"
    private fun keyFamily(uid: String) = "profile.$uid.family"
    private fun keyPhone(uid: String) = "profile.$uid.phone"
    private fun keyLocation(uid: String) = "profile.$uid.location"

    companion object {
        private const val PREFS_NAME = "iremember_profile_prefs"
        private const val KEY_PROFILE_IMAGE_URI = "profile_image_uri"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_USERNAME = "username"
        private const val KEY_BIO = "bio"
        private const val KEY_PROFILE_CUSTOMIZED = "profile_customized"
        private const val KEY_LINKED_FIREBASE_UID = "linked_firebase_uid"
        private const val KEY_LEGACY_MIGRATED = "profile_legacy_migrated"
        private const val KEY_FALLBACK_PROFILE_IMAGE_URI = "fallback_profile_image_uri"
        private const val KEY_FALLBACK_PROFILE_CUSTOMIZED = "fallback_profile_customized"
        const val DEFAULT_DISPLAY_NAME = "IRemember User"
        const val DEFAULT_USERNAME = "iremember_user"
        const val DEFAULT_BIO = "Keeping everyday essentials organized and easy to find."
    }
}
