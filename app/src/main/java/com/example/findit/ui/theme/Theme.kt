package com.example.findit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary              = FindItBlue,
    onPrimary            = FindItOnPrimary,
    primaryContainer     = FindItBlueLight,
    onPrimaryContainer   = FindItBlueDeep,
    secondary            = FindItBlueBright,
    onSecondary          = FindItOnPrimary,
    background           = FindItBackground,
    onBackground         = FindItOnSurface,
    surface              = FindItSurface,
    onSurface            = FindItOnSurface,
    surfaceVariant       = FindItBlueUltraLight,
    onSurfaceVariant     = FindItOnSurfaceSoft,
    outline              = FindItBlue.copy(alpha = 0.15f)
)

private val DarkColorScheme = darkColorScheme(
    primary              = Color(0xFF2DA85E),   // medium-dark green: white text gives ~3.5:1 contrast
    onPrimary            = Color.White,          // white — readable on dark gradient AND on buttons
    primaryContainer     = FindItBlueLightDark,
    onPrimaryContainer   = FindItOnSurfaceDark,
    secondary            = Color(0xFF2DA85E),
    onSecondary          = Color.White,
    background           = FindItBackgroundDark,
    onBackground         = FindItOnSurfaceDark,
    surface              = FindItSurfaceDark,
    onSurface            = FindItOnSurfaceDark,
    surfaceVariant       = FindItSurfaceVarDark,
    onSurfaceVariant     = FindItOnSurfaceDark.copy(alpha = 0.7f),
    outline              = FindItBlueDark.copy(alpha = 0.2f)
)

private val FindItShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small      = RoundedCornerShape(12.dp),
    medium     = RoundedCornerShape(Dimensions.cardCornerRadius),
    large      = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(Dimensions.headerCornerRadius)
)

@Composable
fun FindItTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography  = Typography,
        shapes      = FindItShapes,
        content     = content
    )
}

@Composable
fun gradientStartColor(darkTheme: Boolean = isSystemInDarkTheme()) =
    if (darkTheme) FindItGradientStartDark else FindItGradientStart

@Composable
fun gradientEndColor(darkTheme: Boolean = isSystemInDarkTheme()) =
    if (darkTheme) FindItGradientEndDark else FindItGradientEnd
