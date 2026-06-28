package com.example.findit.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import com.example.findit.R
import com.example.findit.ui.theme.Spacing
import com.example.findit.ui.theme.gradientEndColor
import com.example.findit.ui.theme.gradientStartColor

val AuthCardColor = Color.White
val AuthTextColor = Color(0xFF102015)
val AuthMutedColor = Color(0xFF66756A)
val AuthFieldBorderColor = Color(0xFFD9E7DC)

private val HeaderMax = 240.dp
private val HeaderMin = 72.dp

@Composable
fun AuthShell(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
    centerContent: Boolean = false,
    @Suppress("UNUSED_PARAMETER") contentTopPadding: Dp = 0.dp,
    footerLink: Pair<String, () -> Unit>? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    // Header collapses as user scrolls — only for scrollable (Register) screens
    val headerHeight: Dp
    val characterAlpha: Float
    if (scrollable) {
        val scrolledPx = scrollState.value.toFloat()
        val collapseRange = with(density) { (HeaderMax - HeaderMin).toPx() }
        val fraction = (scrolledPx / collapseRange).coerceIn(0f, 1f)
        headerHeight = (HeaderMax - (HeaderMax - HeaderMin) * fraction).coerceIn(HeaderMin, HeaderMax)
        val fadeRange = with(density) { 72.dp.toPx() }
        characterAlpha = (1f - scrolledPx / fadeRange).coerceIn(0f, 1f)
    } else {
        headerHeight = HeaderMax
        characterAlpha = 1f
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuthCardColor)
    ) {
        // Fixed wave header — stays put; only the white body below scrolls
        AuthWaveHeader(height = headerHeight, characterAlpha = characterAlpha)

        val scrollMod = if (scrollable) Modifier.verticalScroll(scrollState) else Modifier

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .then(scrollMod)
                .padding(
                    start = Spacing.xl,
                    end = Spacing.xl,
                    top = Spacing.xl,
                    bottom = 80.dp       // clears Android gesture / button nav bar
                ),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = AuthTextColor,
                textAlign = TextAlign.Start
            )
            Box(
                modifier = Modifier
                    .padding(top = Spacing.xs, bottom = Spacing.sm)
                    .fillMaxWidth(0.18f)
                    .height(3.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = AuthMutedColor,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(bottom = Spacing.md)
            )

            // centerContent uses Spacer(weight) — valid only when NOT scrollable
            if (centerContent) {
                Spacer(Modifier.weight(1f))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    content = content
                )
                Spacer(Modifier.weight(1f))
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    content = content
                )
            }

            footerLink?.let { (label, onClick) ->
                Spacer(Modifier.height(Spacing.md))
                TextButton(
                    onClick = onClick,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(label)
                }
            }
        }
    }
}

@Composable
private fun AuthWaveHeader(height: Dp, characterAlpha: Float) {
    val gradientColors = listOf(gradientStartColor(), gradientEndColor())

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
    ) {
        // Green wave + decorative pattern
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val wavePath = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, h * 0.80f)
                cubicTo(w * 0.78f, h * 0.98f, w * 0.56f, h * 0.84f, w * 0.48f, h * 0.74f)
                cubicTo(w * 0.30f, h * 0.58f, w * 0.20f, h * 0.78f, 0f, h * 0.70f)
                close()
            }
            drawPath(wavePath, Brush.linearGradient(gradientColors))

            val pat = Color.White.copy(alpha = 0.10f)
            repeat(8) { i ->
                drawCircle(
                    color = pat,
                    radius = 44.dp.toPx() + (i % 3) * 12.dp.toPx(),
                    center = Offset(w * ((i + 1) / 9f), h * (0.14f + (i % 4) * 0.12f)),
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }
            repeat(5) { i ->
                val top = h * (0.10f + i * 0.11f)
                val line = Path().apply {
                    moveTo(-40.dp.toPx(), top)
                    cubicTo(
                        w * 0.22f, top - 44.dp.toPx(),
                        w * 0.48f, top + 42.dp.toPx(),
                        w + 40.dp.toPx(), top - 18.dp.toPx()
                    )
                }
                drawPath(line, pat, style = Stroke(width = 1.dp.toPx()))
            }
        }

        // Character + slogan — fades as user scrolls (characterAlpha 1→0)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .alpha(characterAlpha)
                .padding(horizontal = Spacing.xl),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "FindIt",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Never lose\ntrack again",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            Image(
                painter = painterResource(R.drawable.findit_character),
                contentDescription = "FindIt character",
                modifier = Modifier
                    .size(90.dp)
                    .padding(top = Spacing.sm),
                contentScale = ContentScale.Fit
            )
        }
    }
}
