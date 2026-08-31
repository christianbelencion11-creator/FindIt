package com.example.iremember.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary              = IRememberGreen,
    onPrimary            = IRememberOnPrimary,
    primaryContainer     = IRememberGreenLight,
    onPrimaryContainer   = IRememberGreenDeep,
    secondary            = IRememberGreenBright,
    onSecondary          = IRememberOnPrimary,
    background           = IRememberBackground,
    onBackground         = IRememberOnSurface,
    surface              = IRememberSurface,
    onSurface            = IRememberOnSurface,
    surfaceVariant       = IRememberGreenUltraLight,
    onSurfaceVariant     = IRememberOnSurfaceSoft,
    outline              = IRememberGreen.copy(alpha = 0.15f),
    error                = IRememberError,
    onError              = IRememberOnPrimary
)

private val DarkColorScheme = darkColorScheme(
    primary              = Color(0xFF2DA85E),   // medium-dark green: white text gives ~3.5:1 contrast
    onPrimary            = Color.White,          // white — readable on dark gradient AND on buttons
    primaryContainer     = IRememberGreenLightDark,
    onPrimaryContainer   = IRememberOnSurfaceDark,
    secondary            = Color(0xFF2DA85E),
    onSecondary          = Color.White,
    background           = IRememberBackgroundDark,
    onBackground         = IRememberOnSurfaceDark,
    surface              = IRememberSurfaceDark,
    onSurface            = IRememberOnSurfaceDark,
    surfaceVariant       = IRememberSurfaceVarDark,
    onSurfaceVariant     = IRememberOnSurfaceDark.copy(alpha = 0.7f),
    outline              = IRememberGreenDark.copy(alpha = 0.2f),
    error                = Color(0xFFF87171),
    onError              = Color(0xFF450A0A)
)

private val IRememberShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small      = RoundedCornerShape(12.dp),
    medium     = RoundedCornerShape(Dimensions.cardCornerRadius),
    large      = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(Dimensions.headerCornerRadius)
)

@Composable
fun IRememberTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography  = Typography,
        shapes      = IRememberShapes,
        content     = content
    )
}

/** Uses MaterialTheme so ThemeMode (Light/Dark/Auto) stays in sync — not system alone. */
@Composable
fun gradientStartColor(): Color {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (isDark) IRememberGradientStartDark else IRememberGradientStart
}

@Composable
fun gradientEndColor(): Color {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (isDark) IRememberGradientEndDark else IRememberGradientEnd
}
