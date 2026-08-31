package com.example.iremember.model

/**
 * A saved payment card / e-wallet the user wants to remember.
 * Local-only, owner-scoped by account UID (same privacy model as items and notes) —
 * nothing here is ever uploaded. [cardNumber] holds digits only; the UI masks it.
 */
data class BankCard(
    val id: Long = 0,
    val ownerUid: String = "",
    val bankName: String,
    val cardHolder: String = "",
    val cardNumber: String = "",
    val cardType: String = BankCardStyles.DEFAULT_TYPE,
    val balance: Double = 0.0,
    val colorIndex: Int = BankCardStyles.AUTO_COLOR,
    val dateCreated: Long = System.currentTimeMillis(),
    val dateUpdated: Long = System.currentTimeMillis()
)

/**
 * Card-face gradients and type labels. Gradients are always dark enough for white text,
 * so a card looks like a real card in both light and dark app themes.
 *
 * By default a card uses [AUTO_COLOR]: the face auto-matches the brand colors of the bank
 * or e-wallet the user typed (BDO navy, BPI red, GCash blue, Maya green, …), so it looks
 * like the real thing. The user can still override with any manual palette below.
 */
object BankCardStyles {
    /** colorIndex sentinel: pick the gradient from the bank's real brand colors. */
    const val AUTO_COLOR = -1

    // top-left → bottom-right gradient stops for each manual card face (index 0–6).
    val gradients: List<List<Long>> = listOf(
        listOf(0xFF1E3A8A, 0xFF3B82F6), // Ocean
        listOf(0xFF5B21B6, 0xFFA855F7), // Violet
        listOf(0xFF0F766E, 0xFF14B8A6), // Teal
        listOf(0xFF9D174D, 0xFFEC4899), // Rose
        listOf(0xFF9A3412, 0xFFF59E0B), // Sunset
        listOf(0xFF064E3B, 0xFF10B981), // Emerald (brand)
        listOf(0xFF1F2937, 0xFF4B5563)  // Graphite
    )

    val names: List<String> = listOf(
        "Ocean", "Violet", "Teal", "Rose", "Sunset", "Emerald", "Graphite"
    )

    /** Card kinds the user can tag a card with. */
    val types: List<String> = listOf("Debit", "Credit", "E-Wallet", "Savings")

    const val DEFAULT_TYPE = "Debit"

    /**
     * A recognizable bank / e-wallet: any of [keys] appearing in the typed name maps the
     * card face to [stops] (top-left → bottom-right), chosen to match the brand and stay
     * dark enough for white text.
     */
    private data class Brand(val keys: List<String>, val stops: List<Long>)

    // Order matters: the first brand whose key is contained in the typed name wins.
    private val brands: List<Brand> = listOf(
        // Universal / commercial banks
        Brand(listOf("bdo", "banco de oro"), listOf(0xFF002E6D, 0xFF0B57A4)),
        Brand(listOf("bpi", "bank of the philippine"), listOf(0xFF8B1A2B, 0xFFC1272D)),
        Brand(listOf("metrobank", "metro bank"), listOf(0xFF012169, 0xFF0A3D91)),
        Brand(listOf("landbank", "land bank"), listOf(0xFF00532E, 0xFF00A651)),
        Brand(listOf("unionbank", "union bank", "ubp"), listOf(0xFFB8420E, 0xFFF97316)),
        Brand(listOf("security bank", "securitybank"), listOf(0xFF064E3B, 0xFF0E9F6E)),
        Brand(listOf("chinabank", "china bank", "chinabanking"), listOf(0xFF8A0F14, 0xFFCE1B1F)),
        Brand(listOf("eastwest", "east west"), listOf(0xFF7F1D1D, 0xFFB91C1C)),
        Brand(listOf("rcbc", "yuchengco"), listOf(0xFF003DA5, 0xFF0057B8)),
        Brand(listOf("pnb", "philippine national"), listOf(0xFF00448C, 0xFF0072CE)),
        Brand(listOf("psbank", "ps bank"), listOf(0xFF4C1D95, 0xFF7C3AED)),
        Brand(listOf("dbp", "development bank"), listOf(0xFF0B3D91, 0xFF1D6FB8)),
        Brand(listOf("aub", "asia united"), listOf(0xFF1E3A5F, 0xFF2E5A88)),
        // Digital banks
        Brand(listOf("seabank", "sea bank"), listOf(0xFF0B4FC2, 0xFF1E90FF)),
        Brand(listOf("gotyme", "go tyme", "tyme"), listOf(0xFF101B2D, 0xFF25406B)),
        Brand(listOf("cimb", "tonik", "uno digital", "unobank"), listOf(0xFF7A1F2B, 0xFFB4232F)),
        Brand(listOf("maya bank", "mayabank"), listOf(0xFF0B1220, 0xFF00C566)),
        // E-wallets
        Brand(listOf("gcash", "g-cash"), listOf(0xFF0062E6, 0xFF00B2FF)),
        Brand(listOf("paymaya", "maya"), listOf(0xFF0B1220, 0xFF00C566)),
        Brand(listOf("grabpay", "grab"), listOf(0xFF00873C, 0xFF00B14F)),
        Brand(listOf("coins.ph", "coinsph", "coins ph"), listOf(0xFF123A8B, 0xFF2D6BE0)),
        Brand(listOf("shopeepay", "shopee"), listOf(0xFFB8360A, 0xFFEE4D2D)),
        Brand(listOf("paypal"), listOf(0xFF003087, 0xFF009CDE)),
        // Card networks (when typed as the "bank")
        Brand(listOf("mastercard", "master card"), listOf(0xFF7A1E0C, 0xFFEB6D1E)),
        Brand(listOf("visa"), listOf(0xFF13265C, 0xFF1A1F71)),
        Brand(listOf("american express", "amex"), listOf(0xFF00457C, 0xFF016FD0))
    )

    private fun normalize(bankName: String): String =
        bankName.lowercase().trim()

    /** Brand gradient stops for a typed bank/e-wallet name, or null if it isn't recognized. */
    fun brandGradient(bankName: String): List<Long>? {
        val n = normalize(bankName)
        if (n.isBlank()) return null
        return brands.firstOrNull { brand -> brand.keys.any { n.contains(it) } }?.stops
    }

    /** True when the typed name maps to a known brand palette. */
    fun hasBrand(bankName: String): Boolean = brandGradient(bankName) != null

    /**
     * The gradient a card should actually paint with: honor a manual [colorIndex], or when
     * it is [AUTO_COLOR] use the bank's brand colors (falling back to Ocean if unknown).
     */
    fun resolveGradient(colorIndex: Int, bankName: String): List<Long> =
        if (colorIndex == AUTO_COLOR) brandGradient(bankName) ?: gradients[0]
        else gradient(colorIndex)

    fun gradient(index: Int): List<Long> = gradients[index.coerceIn(0, gradients.lastIndex)]

    fun clampColor(index: Int): Int =
        if (index == AUTO_COLOR) AUTO_COLOR else index.coerceIn(0, gradients.lastIndex)
}
