package com.example.iremember.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.iremember.util.PasswordUtils

/**
 * On-device account store for local username/password login. Replaces the old Firebase
 * Auth/Firestore backend entirely — there is no network here.
 *
 * Records are kept in their own EncryptedSharedPreferences file (separate from the session
 * prefs in [com.example.iremember.util.AuthPreferences]) so an account survives sign-out.
 * Passwords and secrets are stored only as salted SHA-256 hashes via [PasswordUtils];
 * the plaintext is never persisted.
 */
class LocalAccountStore(context: Context) {

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

    /** Minimal identity returned to callers — never carries password/secret material. */
    data class Record(
        val username: String,
        val uid: String,
        val email: String
    )

    fun exists(username: String): Boolean {
        val u = UsernameAuth.normalizeUsername(username)
        return prefs.contains(key(u, PWD_HASH))
    }

    fun get(username: String): Record? {
        val u = UsernameAuth.normalizeUsername(username)
        if (!prefs.contains(key(u, PWD_HASH))) return null
        return Record(
            username = u,
            uid = prefs.getString(key(u, UID), null) ?: uidFor(u),
            email = prefs.getString(key(u, EMAIL), "").orEmpty()
        )
    }

    /** Creates the account. Caller must check [exists] first to surface a friendly error. */
    fun create(username: String, email: String, password: String, secret: String): Record {
        val u = UsernameAuth.normalizeUsername(username)
        val emailLower = email.trim().lowercase()
        val pwdSalt = PasswordUtils.generateSalt()
        val secretSalt = PasswordUtils.generateSalt()
        val uid = uidFor(u)
        prefs.edit()
            .putString(key(u, UID), uid)
            .putString(key(u, EMAIL), emailLower)
            .putString(key(u, PWD_SALT), pwdSalt)
            .putString(key(u, PWD_HASH), PasswordUtils.hashPassword(password, pwdSalt))
            .putString(key(u, SECRET_SALT), secretSalt)
            .putString(key(u, SECRET_HASH), PasswordUtils.hashPassword(secret.trim(), secretSalt))
            .putLong(key(u, CREATED), nowMillis())
            .apply()
        return Record(u, uid, emailLower)
    }

    fun verifyPassword(username: String, password: String): Boolean {
        val u = UsernameAuth.normalizeUsername(username)
        val salt = prefs.getString(key(u, PWD_SALT), null) ?: return false
        val hash = prefs.getString(key(u, PWD_HASH), null) ?: return false
        return PasswordUtils.verifyPassword(password, salt, hash)
    }

    fun verifySecret(username: String, secret: String): Boolean {
        val u = UsernameAuth.normalizeUsername(username)
        val salt = prefs.getString(key(u, SECRET_SALT), null) ?: return false
        val hash = prefs.getString(key(u, SECRET_HASH), null) ?: return false
        return PasswordUtils.verifyPassword(secret.trim(), salt, hash)
    }

    fun updatePassword(username: String, newPassword: String) {
        val u = UsernameAuth.normalizeUsername(username)
        if (!prefs.contains(key(u, PWD_HASH))) return
        val salt = PasswordUtils.generateSalt()
        prefs.edit()
            .putString(key(u, PWD_SALT), salt)
            .putString(key(u, PWD_HASH), PasswordUtils.hashPassword(newPassword, salt))
            .apply()
    }

    fun updateSecret(username: String, newSecret: String) {
        val u = UsernameAuth.normalizeUsername(username)
        if (!prefs.contains(key(u, PWD_HASH))) return
        val salt = PasswordUtils.generateSalt()
        prefs.edit()
            .putString(key(u, SECRET_SALT), salt)
            .putString(key(u, SECRET_HASH), PasswordUtils.hashPassword(newSecret.trim(), salt))
            .apply()
    }

    private fun nowMillis(): Long = System.currentTimeMillis()

    private fun key(username: String, field: String) = "acct_${username}_$field"

    companion object {
        private const val PREFS_NAME = "iremember_accounts_encrypted"
        private const val UID = "uid"
        private const val EMAIL = "email"
        private const val PWD_SALT = "pwd_salt"
        private const val PWD_HASH = "pwd_hash"
        private const val SECRET_SALT = "secret_salt"
        private const val SECRET_HASH = "secret_hash"
        private const val CREATED = "created"

        /** Stable per-username id used to scope items/notes/cards and PIN storage. */
        fun uidFor(username: String): String =
            "local:" + UsernameAuth.normalizeUsername(username)
    }
}
