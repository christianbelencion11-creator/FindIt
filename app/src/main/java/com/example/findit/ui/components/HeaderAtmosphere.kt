package com.example.findit.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.findit.ui.theme.isAppDarkTheme
import kotlin.math.min

/**
 * Sparse decorative accents over the header gradient.
 * - Dark ("midnight emerald"): soft radial green glow orbs for a premium, lit-from-within feel.
 * - Light: a few subtle white/mint shapes.
 */
@Composable
fun HeaderAtmosphere(modifier: Modifier = Modifier) {
    val dark = isAppDarkTheme()
    Canvas(modifier = modifier) {
        val minSide = min(size.width, size.height).coerceAtLeast(1f)

        if (dark) {
            // Large emerald bloom — top-right, the main glow.
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF2FE07C).copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(size.width * 0.86f, size.height * 0.12f),
                    radius = minSide * 0.65f
                )
            )
            // Secondary softer glow — lower-left, adds depth.
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF34D399).copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width * 0.04f, size.height * 0.92f),
                    radius = minSide * 0.5f
                )
            )
            // Faint ring — mid-right, subtle geometric accent.
            drawCircle(
                color = Color(0xFF86EFAC).copy(alpha = 0.14f),
                radius = minSide * 0.16f,
                center = Offset(size.width * 0.78f, size.height * 0.72f),
                style = Stroke(width = (minSide * 0.012f).coerceIn(1.5f, 3f))
            )
        } else {
            val whiteSoft = Color.White.copy(alpha = 0.08f)
            val mintSoft = Color(0xFFBBF7D0).copy(alpha = 0.07f)
            val ringColor = Color.White.copy(alpha = 0.12f)

            // Large soft disc — top-right edge
            drawCircle(
                color = whiteSoft,
                radius = minSide * 0.42f,
                center = Offset(size.width * 0.92f, size.height * 0.15f)
            )
            // Medium soft disc — bottom-left
            drawCircle(
                color = mintSoft,
                radius = minSide * 0.22f,
                center = Offset(size.width * 0.08f, size.height * 0.88f)
            )
            // Faint ring — mid-right
            drawCircle(
                color = ringColor,
                radius = minSide * 0.16f,
                center = Offset(size.width * 0.78f, size.height * 0.72f),
                style = Stroke(width = (minSide * 0.014f).coerceIn(1.5f, 3f))
            )
            // Small accent disc — upper-left (very subtle)
            drawCircle(
                color = Color.White.copy(alpha = 0.06f),
                radius = minSide * 0.1f,
                center = Offset(size.width * 0.18f, size.height * 0.28f)
            )
        }
    }
}
