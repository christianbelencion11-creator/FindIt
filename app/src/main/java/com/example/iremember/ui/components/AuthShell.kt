package com.example.iremember.ui.components

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.iremember.R
import com.example.iremember.ui.theme.IRememberGradientEnd
import com.example.iremember.ui.theme.IRememberGradientStart
import com.example.iremember.ui.theme.Spacing

val AuthCardColor = Color.White
val AuthTextColor = Color(0xFF102015)
val AuthMutedColor = Color(0xFF66756A)
val AuthFieldBorderColor = Color(0xFFD9E7DC)

@Composable
fun AuthErrorText(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.fillMaxWidth()
    )
}

/** Content height of the green wave (status bar is added on top). */
private val HeaderMax = 196.dp
private val HeaderMin = 72.dp

/** Mascot size matching Figma Sign-in ~75% of green branding area. */
private val BrandMascotSize = 156.dp
private const val BrandMascotScale = 1.2f

@Composable
fun AuthShell(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
    centerContent: Boolean = false,
    @Suppress("UNUSED_PARAMETER") contentTopPadding: Dp = 0.dp,
    footerLink: Pair<String, () -> Unit>? = null,
    stickyBottomBar: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    val headerHeight: Dp
    val characterAlpha: Float
    if (scrollable) {
        val scrolledPx = scrollState.value.toFloat()
        val collapseRange = with(density) { (HeaderMax - HeaderMin).toPx() }
        val fraction = (scrolledPx / collapseRange).coerceIn(0f, 1f)
        headerHeight = (HeaderMax - (HeaderMax - HeaderMin) * fraction).coerceIn(HeaderMin, HeaderMax)
        val fadeRange = with(density) { 56.dp.toPx() }
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
        AuthWaveHeader(height = headerHeight, characterAlpha = characterAlpha)

        val scrollMod = if (scrollable) Modifier.verticalScroll(scrollState) else Modifier
        val hasSticky = stickyBottomBar != null
        val bottomPadding = when {
            hasSticky -> Spacing.lg
            footerLink != null -> Spacing.xxxl
            else -> Spacing.xl
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .then(scrollMod)
                .then(if (hasSticky) Modifier else Modifier.navigationBarsPadding())
                .padding(
                    start = Spacing.xl,
                    end = Spacing.xl,
                    top = Spacing.lg,
                    bottom = bottomPadding
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

            if (!hasSticky) {
                footerLink?.let { (label, onClick) ->
                    Spacer(Modifier.height(Spacing.lg))
                    TextButton(
                        onClick = onClick,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = Spacing.sm),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(label)
                    }
                }
            }
        }

        stickyBottomBar?.let { bar ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AuthCardColor)
                    .navigationBarsPadding()
                    .padding(horizontal = Spacing.xl, vertical = Spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                bar()
                footerLink?.let { (label, onClick) ->
                    TextButton(
                        onClick = onClick,
                        modifier = Modifier.padding(top = Spacing.xs),
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
}

/**
 * Shared green wave header: brand left + large mascot right (Figma Sign-in / Get Started).
 * Status-bar padding is applied inside so [contentHeight] is the green branding area only.
 */
@Composable
fun BrandWaveHeader(
    contentHeight: Dp = HeaderMax,
    characterAlpha: Float = 1f,
    modifier: Modifier = Modifier
) {
    // Auth / intro flow always uses the original bright-green brand gradient,
    // independent of theme — dark mode only applies inside the app after login.
    val gradientColors = listOf(IRememberGradientStart, IRememberGradientEnd)
    // Status bar room + content height so brand/mascot keep full size.
    val totalHeight = contentHeight + 40.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val wavePath = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, h * 0.82f)
                cubicTo(w * 0.78f, h * 0.99f, w * 0.56f, h * 0.86f, w * 0.48f, h * 0.76f)
                cubicTo(w * 0.30f, h * 0.60f, w * 0.20f, h * 0.80f, 0f, h * 0.72f)
                close()
            }
            drawPath(wavePath, Brush.linearGradient(gradientColors))

            val pat = Color.White.copy(alpha = 0.06f)
            repeat(4) { i ->
                drawCircle(
                    color = pat,
                    radius = 36.dp.toPx() + (i % 2) * 10.dp.toPx(),
                    center = Offset(w * (0.15f + i * 0.18f), h * (0.22f + (i % 2) * 0.08f)),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }

        // Brand sits in mid-green band (below status bar, above left wave dip).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .alpha(characterAlpha)
                .padding(horizontal = Spacing.xl)
                .padding(top = 32.dp)
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = Spacing.sm)
            ) {
                Text(
                    text = "IRemember",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 26.sp,
                        lineHeight = 34.sp,
                        letterSpacing = (-0.3).sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip
                )
                Text(
                    text = "Never lose track again",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    ),
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip
                )
            }

            // PNG has transparent padding — scale up so the fox fills like Figma.
            Box(
                modifier = Modifier
                    .width(BrandMascotSize)
                    .height(BrandMascotSize)
                    .clipToBounds()
                    .offset(y = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.iremember_character),
                    contentDescription = "IRemember character",
                    modifier = Modifier
                        .size(BrandMascotSize)
                        .graphicsLayer {
                            scaleX = BrandMascotScale
                            scaleY = BrandMascotScale
                        },
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
private fun AuthWaveHeader(height: Dp, characterAlpha: Float) {
    BrandWaveHeader(
        contentHeight = height,
        characterAlpha = characterAlpha
    )
}
