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

    fun getFullName(): String = prefs.getString(KEY_FULL_NAME, "").orEmpty()

    fun getEmail(): String = prefs.getString(KEY_EMAIL, "").orEmpty()

    fun getPhone(): String = prefs.getString(KEY_PHONE, "").orEmpty()

    fun isPinSet(): Boolean = prefs.getString(KEY_PIN_HASH, null) != null

    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun needsUnlock(): Boolean = isLoggedIn() && isPinSet()

    fun register(
        fullName: String,
        email: String,
        phone: String,
        password: String
    ): AuthResult {
        if (isRegistered()) {
            return AuthResult.Error("An account already exists on this device.")
        }
        val salt = PasswordUtils.generateSalt()
        prefs.edit()
            .putBoolean(KEY_REGISTERED, true)
            .putString(KEY_FULL_NAME, fullName.trim())
            .putString(KEY_EMAIL, email.trim().lowercase())
            .putString(KEY_PHONE, phone.trim())
            .putString(KEY_PASSWORD_SALT, salt)
            .putString(KEY_PASSWORD_HASH, PasswordUtils.hashPassword(password, salt))
            .putBoolean(KEY_LOGGED_IN, true)
            .apply()
        return AuthResult.Success
    }

    fun login(email: String, password: String): AuthResult {
        if (!isRegistered()) {
            return AuthResult.Error("No account found. Please register first.")
        }
        val storedEmail = getEmail()
        if (email.trim().lowercase() != storedEmail) {
            return AuthResult.Error("Incorrect email or password.")
        }
        val salt = prefs.getString(KEY_PASSWORD_SALT, null).orEmpty()
        val hash = prefs.getString(KEY_PASSWORD_HASH, null).orEmpty()
        if (!PasswordUtils.verifyPassword(password, salt, hash)) {
            return AuthResult.Error("Incorrect email or password.")
        }
        prefs.edit().putBoolean(KEY_LOGGED_IN, true).apply()
        return AuthResult.Success
    }

    fun logout() {
        prefs.edit().putBoolean(KEY_LOGGED_IN, false).apply()
    }

    fun setPin(pin: String) {
        val salt = PasswordUtils.generateSalt()
        prefs.edit()
            .putString(KEY_PIN_SALT, salt)
            .putString(KEY_PIN_HASH, PasswordUtils.hashPin(pin, salt))
            .apply()
    }

    fun verifyPin(pin: String): Boolean {
        val salt = prefs.getString(KEY_PIN_SALT, null).orEmpty()
        val hash = prefs.getString(KEY_PIN_HASH, null).orEmpty()
        if (salt.isEmpty() || hash.isEmpty()) return false
        return PasswordUtils.hashPin(pin, salt) == hash
    }

    sealed class AuthResult {
        data object Success : AuthResult()
        data class Error(val message: String) : AuthResult()
    }

    companion object {
        private const val PREFS_NAME = "findit_auth_encrypted"
        private const val KEY_REGISTERED = "registered"
        private const val KEY_LOGGED_IN = "logged_in"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_PHONE = "phone"
        private const val KEY_PASSWORD_SALT = "password_salt"
        private const val KEY_PASSWORD_HASH = "password_hash"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    }
}
