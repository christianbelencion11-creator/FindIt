package com.example.iremember.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

/** True when the app Material theme is dark (ThemeMode), not system alone. */
@Composable
fun isAppDarkTheme(): Boolean =
    MaterialTheme.colorScheme.background.luminance() < 0.5f

/** Soft vertical gradient for dark pressable surfaces. */
fun surfaceCardBrush(): Brush = Brush.verticalGradient(
    colors = listOf(
        IRememberSurfaceGradTopDark,
        IRememberSurfaceDark,
        IRememberSurfaceGradBottomDark
    )
)

private val SurfaceEdgeLight = Color.White.copy(alpha = 0.10f)

/** Soft edge for Material3 Card.border in dark mode (prefer over outer premiumSurface). */
fun darkSurfaceBorder(): BorderStroke =
    BorderStroke(width = 1.dp, color = SurfaceEdgeLight)

/**
 * Dark mode: clip + vertical surface gradient + soft border.
 * Use only for **non-Card** shells (search bar, bottom nav).
 * For Material3 Card, put [surfaceCardBrush] inside the Card so ripples clip to [shape].
 */
fun Modifier.premiumSurface(
    shape: Shape,
    dark: Boolean,
    lightColor: Color
): Modifier = if (dark) {
    this
        .clip(shape)
        .background(surfaceCardBrush())
        .border(width = 1.dp, color = SurfaceEdgeLight, shape = shape)
} else {
    this
        .clip(shape)
        .background(lightColor, shape)
}

/**
 * Dark-only gradient fill (no border). Use as the first child inside a shaped Card.
 */
fun Modifier.premiumSurfaceFill(
    shape: Shape,
    dark: Boolean
): Modifier = if (dark) {
    this.clip(shape).background(surfaceCardBrush())
} else {
    this
}

/** Dark Card content fill — Card already clips to shape, so no extra clip needed. */
fun Modifier.darkCardGradientFill(dark: Boolean): Modifier =
    if (dark) this.background(surfaceCardBrush()) else this
