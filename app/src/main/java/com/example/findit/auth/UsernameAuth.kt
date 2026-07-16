package com.example.findit.auth

/**
 * Username-based accounts use a synthetic Firebase Auth email so MPIN / ownership
 * keep working while the UI never shows an email field.
 */
object UsernameAuth {
    private const val DOMAIN = "users.iremember.app"

    fun normalizeUsername(username: String): String =
        username.trim().lowercase()

    fun isValidUsername(username: String): Boolean {
        val normalized = normalizeUsername(username)
        return normalized.length in 3..24 &&
            normalized.matches(Regex("^[a-z0-9_]+$"))
    }

    /** True if the user typed an email into the username field by mistake. */
    fun looksLikeEmail(value: String): Boolean =
        value.contains("@") && value.contains(".")

    fun syntheticEmail(username: String): String =
        "${normalizeUsername(username)}@$DOMAIN"

    fun usernameFromSyntheticEmail(email: String): String? {
        val normalized = email.trim().lowercase()
        if (!normalized.endsWith("@$DOMAIN")) return null
        return normalized.removeSuffix("@$DOMAIN").takeIf { it.isNotBlank() }
    }
}
