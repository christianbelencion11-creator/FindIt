package com.example.findit.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AuthPreferences(context: Context) {

    private val prefs: SharedPreferences = run {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun isRegistered(): Boolean = prefs.getBoolean(KEY_REGISTERED, false)

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)

    fun setLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_LOGGED_IN, loggedIn).apply()
    }

    fun hasSeenGetStarted(): Boolean = prefs.getBoolean(KEY_SEEN_GET_STARTED, false)

    fun setHasSeenGetStarted(seen: Boolean = true) {
        prefs.edit().putBoolean(KEY_SEEN_GET_STARTED, seen).apply()
    }

    fun getFullName(): String = prefs.getString(KEY_FULL_NAME, "").orEmpty()

    fun getEmail(): String = prefs.getString(KEY_EMAIL, "").orEmpty()

    fun getUsername(): String {
        val stored = prefs.getString(KEY_USERNAME, "").orEmpty()
        if (stored.isNotBlank()) return stored
        val email = getEmail()
        if (email.endsWith("@users.iremember.app")) {
            return email.removeSuffix("@users.iremember.app")
        }
        return getFullName()
    }

    fun getPhone(): String = prefs.getString(KEY_PHONE, "").orEmpty()

    fun getFirebaseUid(): String = prefs.getString(KEY_FIREBASE_UID, "").orEmpty()

    fun getAuthProvider(): String = prefs.getString(KEY_AUTH_PROVIDER, "").orEmpty()

    /** True when the given Firebase user already has an MPIN on this device. */
    fun isPinSet(uid: String = getFirebaseUid()): Boolean {
        if (uid.isBlank()) return false
        return prefs.getString(pinHashKey(uid), null) != null
    }

    fun isBiometricEnabled(uid: String = getFirebaseUid()): Boolean {
        if (uid.isBlank()) return false
        return prefs.getBoolean(biometricKey(uid), false)
    }

    fun setBiometricEnabled(enabled: Boolean, uid: String = getFirebaseUid()) {
        if (uid.isBlank()) return
        prefs.edit().putBoolean(biometricKey(uid), enabled).apply()
    }

    fun needsUnlock(uid: String = getFirebaseUid()): Boolean =
        uid.isNotBlank() && isLoggedIn() && isPinSet(uid)

    /** Cache Firebase user details locally for PIN, profile, and unlock flows. */
    fun syncFromFirebaseUser(
        fullName: String,
        email: String,
        phone: String,
        provider: String,
        firebaseUid: String,
        username: String = ""
    ) {
        val resolvedUsername = username.trim().ifBlank {
            if (email.endsWith("@users.iremember.app")) {
                email.removeSuffix("@users.iremember.app")
            } else {
                fullName.trim()
            }
        }
        prefs.edit()
            .putBoolean(KEY_REGISTERED, true)
            .putString(KEY_FULL_NAME, fullName.trim().ifBlank { resolvedUsername })
            .putString(KEY_EMAIL, email.trim().lowercase())
            .putString(KEY_USERNAME, resolvedUsername)
            .putString(KEY_PHONE, phone.trim())
            .putString(KEY_FIREBASE_UID, firebaseUid)
            .putString(KEY_AUTH_PROVIDER, provider)
            .putBoolean(KEY_LOGGED_IN, true)
            .putBoolean(KEY_SEEN_GET_STARTED, true)
            .apply()
    }

    fun logout() {
        prefs.edit().putBoolean(KEY_LOGGED_IN, false).apply()
    }

    /** Clears cached user identity on sign-out so a new account does not inherit old data. */
    fun clearSessionData() {
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, false)
            .remove(KEY_FULL_NAME)
            .remove(KEY_EMAIL)
            .remove(KEY_USERNAME)
            .remove(KEY_FIREBASE_UID)
            .remove(KEY_AUTH_PROVIDER)
            .apply()
    }

    fun setPin(pin: String, uid: String = getFirebaseUid()) {
        if (uid.isBlank()) return
        val salt = PasswordUtils.generateSalt()
        prefs.edit()
            .putString(pinSaltKey(uid), salt)
            .putString(pinHashKey(uid), PasswordUtils.hashPin(pin, salt))
            .apply()
        clearLegacyPinStorage()
    }

    fun verifyPin(pin: String, uid: String = getFirebaseUid()): Boolean {
        if (uid.isBlank()) return false
        val salt = prefs.getString(pinSaltKey(uid), null).orEmpty()
        val hash = prefs.getString(pinHashKey(uid), null).orEmpty()
        if (salt.isEmpty() || hash.isEmpty()) return false
        return PasswordUtils.hashPin(pin, salt) == hash
    }

    private fun pinHashKey(uid: String) = "pin_hash_$uid"

    private fun pinSaltKey(uid: String) = "pin_salt_$uid"

    private fun biometricKey(uid: String) = "biometric_enabled_$uid"

    /** Remove old device-wide PIN keys from earlier app versions. */
    private fun clearLegacyPinStorage() {
        prefs.edit()
            .remove(KEY_PIN_HASH)
            .remove(KEY_PIN_SALT)
            .remove(KEY_BIOMETRIC_ENABLED)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "findit_auth_encrypted"
        private const val KEY_REGISTERED = "registered"
        private const val KEY_LOGGED_IN = "logged_in"
        private const val KEY_SEEN_GET_STARTED = "seen_get_started"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_USERNAME = "username"
        private const val KEY_PHONE = "phone"
        private const val KEY_FIREBASE_UID = "firebase_uid"
        private const val KEY_AUTH_PROVIDER = "auth_provider"
        @Deprecated("Legacy device-wide PIN — use per-user keys")
        private const val KEY_PIN_SALT = "pin_salt"
        @Deprecated("Legacy device-wide PIN — use per-user keys")
        private const val KEY_PIN_HASH = "pin_hash"
        @Deprecated("Legacy device-wide biometric flag — use per-user keys")
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    }
}
