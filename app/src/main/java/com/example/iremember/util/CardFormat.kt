package com.example.iremember.util

import java.text.DecimalFormat

/**
 * Formatting helpers for bank cards: peso amounts and card-number grouping/masking.
 * Card numbers are stored as digits only; these helpers shape them for display.
 */
object CardFormat {
    private const val MAX_DIGITS = 19 // longest real PAN (some cards use 19)

    /** Peso amount with thousands separators, e.g. 1234.5 → "₱1,234.50". */
    fun peso(amount: Double): String = "₱" + DecimalFormat("#,##0.00").format(amount)

    /** Keep only digits, capped at [MAX_DIGITS]. */
    fun sanitizeNumber(input: String): String =
        input.filter { it.isDigit() }.take(MAX_DIGITS)

    /** Parse a user-typed balance ("₱1,234.50", "1234.5", " 200 ") into a Double, or null. */
    fun parseBalance(input: String): Double? =
        input.replace("₱", "")
            .replace(",", "")
            .trim()
            .toDoubleOrNull()

    /** Group digits into blocks of 4: "1234567890123456" → "1234 5678 9012 3456". */
    fun group(digits: String): String =
        if (digits.isBlank()) "" else digits.chunked(4).joinToString(" ")

    /**
     * Masked form for display: every digit except the last four becomes a bullet,
     * grouped in fours. Blank input renders an empty placeholder card number.
     */
    fun masked(digits: String): String {
        if (digits.isBlank()) return "•••• •••• •••• ••••"
        val lastFour = digits.takeLast(4)
        val hiddenCount = (digits.length - lastFour.length).coerceAtLeast(0)
        return group("•".repeat(hiddenCount) + lastFour)
    }

    /**
     * Best-effort card network from the leading digits (IIN ranges), so the face can show
     * the right badge like a real card. Returns null until enough digits are typed.
     */
    fun network(digits: String): CardNetwork? {
        val d = digits.filter { it.isDigit() }
        if (d.isEmpty()) return null
        val two = d.take(2).toIntOrNull()
        val four = d.take(4).toIntOrNull()
        return when {
            d.startsWith("4") -> CardNetwork.VISA
            two in 51..55 -> CardNetwork.MASTERCARD
            four != null && four in 2221..2720 -> CardNetwork.MASTERCARD
            two == 34 || two == 37 -> CardNetwork.AMEX
            two == 35 -> CardNetwork.JCB
            two == 62 -> CardNetwork.UNIONPAY
            else -> null
        }
    }
}

/** Payment networks IRemember can badge a card with. */
enum class CardNetwork { VISA, MASTERCARD, AMEX, JCB, UNIONPAY }
