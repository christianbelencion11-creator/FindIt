package com.example.findit.ui.theme

enum class ThemeMode {
    Light,
    Dark,
    Auto;

    companion object {
        fun fromName(name: String?): ThemeMode =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: Auto
    }
}
