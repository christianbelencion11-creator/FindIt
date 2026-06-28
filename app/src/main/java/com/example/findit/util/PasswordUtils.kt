package com.example.findit.util

import java.security.MessageDigest
import java.security.SecureRandom

object PasswordUtils {

    fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashed = digest.digest("$salt:$password".toByteArray(Charsets.UTF_8))
        return hashed.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean =
        hashPassword(password, salt) == expectedHash

    fun hashPin(pin: String, salt: String): String = hashPassword(pin, salt)

    fun isStrongEnough(password: String): Boolean =
        password.length >= 8 &&
            password.any { it.isLetter() } &&
            password.any { it.isDigit() }

    fun isValidEmail(email: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
}
