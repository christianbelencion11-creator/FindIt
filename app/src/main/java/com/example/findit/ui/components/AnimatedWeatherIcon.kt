package com.example.findit.ui.components

import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class WeatherIconKind {
    Clear,
    Cloudy,
    Rain,
    Storm
}

fun weatherKindForCondition(condition: String): WeatherIconKind {
    val c = condition.lowercase()
    return when {
        c.contains("clear") || c.contains("sunny") -> WeatherIconKind.Clear
        c.contains("storm") || c.contains("thunder") -> WeatherIconKind.Storm
        c.contains("rain") || c.contains("drizzle") || c.contains("shower") -> WeatherIconKind.Rain
        else -> WeatherIconKind.Cloudy
    }
}

/**
 * Looping Canvas weather icon: sun glow, drifting clouds, falling rain,
 * yellow lightning flash. Pure Compose — no Lottie/video.
 *
 * @param onDarkGreenHeader brighten colors so the icon reads on dark green / gradient surfaces
 * @param onDarkGradient alias of [onDarkGreenHeader] for Figma-style weather sheet
 */
@Composable
fun AnimatedWeatherIcon(
    condition: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    onDarkGreenHeader: Boolean = false,
    onDarkGradient: Boolean = false
) {
    val kind = remember(condition) { weatherKindForCondition(condition) }
    val onHeader = onDarkGreenHeader || onDarkGradient
    val context = LocalContext.current
    val reduceMotion = remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }

    val transition = rememberInfiniteTransition(label = "weatherIcon")

    val sunPulse by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sunPulse"
    )
    val sunRayAlpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sunRay"
    )
    val sunRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(18_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sunRot"
    )

    val cloudDrift by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloudDrift"
    )

    val rainPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainPhase"
    )

    val boltFlash by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "boltFlash"
    )
    val stormShake by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(80, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "stormShake"
    )

    val pulse = if (reduceMotion) 1f else sunPulse
    val rayA = if (reduceMotion) 0.55f else sunRayAlpha
    val rot = if (reduceMotion) 0f else sunRotation
    val drift = if (reduceMotion) 0f else cloudDrift
    val rain = if (reduceMotion) 0.4f else rainPhase
    val flash = if (reduceMotion) 0.85f else boltFlash
    val shake = if (reduceMotion) 0f else stormShake

    Canvas(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = condition }
    ) {
        when (kind) {
            WeatherIconKind.Clear -> drawSun(
                pulse = pulse,
                rayAlpha = rayA,
                rotationDeg = rot,
                onHeader = onHeader
            )
            WeatherIconKind.Cloudy -> drawClouds(
                drift = drift,
                onHeader = onHeader,
                partlySunny = condition.contains("partly", ignoreCase = true)
            )
            WeatherIconKind.Rain -> drawRain(
                drift = drift,
                rainPhase = rain,
                onHeader = onHeader
            )
            WeatherIconKind.Storm -> drawStorm(
                drift = drift,
                flash = flash,
                shake = shake,
                onHeader = onHeader
            )
        }
    }
}

private fun DrawScope.drawSun(
    pulse: Float,
    rayAlpha: Float,
    rotationDeg: Float,
    onHeader: Boolean
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = size.minDimension * 0.22f
    val sunCore = if (onHeader) Color(0xFFFFF7AE) else Color(0xFFFBBF24)
    val sunEdge = if (onHeader) Color(0xFFFDE68A) else Color(0xFFF59E0B)
    val rayColor = if (onHeader) Color(0xFFFFFBEB) else Color(0xFFF59E0B)

    scale(pulse, pivot = Offset(cx, cy)) {
        rotate(rotationDeg, pivot = Offset(cx, cy)) {
            val rayLen = size.minDimension * 0.38f
            for (i in 0 until 8) {
                val ang = (i * 45f) * (PI.toFloat() / 180f)
                val x1 = cx + cos(ang) * (r * 1.35f)
                val y1 = cy + sin(ang) * (r * 1.35f)
                val x2 = cx + cos(ang) * rayLen
                val y2 = cy + sin(ang) * rayLen
                drawLine(
                    color = rayColor.copy(alpha = rayAlpha * 0.9f),
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = size.minDimension * 0.06f
                )
            }
        }
        drawCircle(
            color = sunEdge.copy(alpha = 0.35f),
            radius = r * 1.45f,
            center = Offset(cx, cy)
        )
        drawCircle(color = sunCore, radius = r, center = Offset(cx, cy))
        drawCircle(color = sunEdge.copy(alpha = 0.5f), radius = r * 0.55f, center = Offset(cx - r * 0.2f, cy - r * 0.2f))
    }
}

private fun DrawScope.drawCloudBlob(
    center: Offset,
    scale: Float,
    light: Color,
    shadow: Color
) {
    val w = size.minDimension * 0.55f * scale
    val h = size.minDimension * 0.28f * scale
    // layered ellipses for a soft cloud
    drawOval(
        color = shadow,
        topLeft = Offset(center.x - w * 0.55f, center.y - h * 0.2f),
        size = Size(w * 1.1f, h * 1.15f)
    )
    drawOval(
        color = light,
        topLeft = Offset(center.x - w * 0.5f, center.y - h * 0.55f),
        size = Size(w * 0.55f, h * 0.95f)
    )
    drawOval(
        color = light,
        topLeft = Offset(center.x - w * 0.15f, center.y - h * 0.7f),
        size = Size(w * 0.65f, h * 1.05f)
    )
    drawOval(
        color = light,
        topLeft = Offset(center.x + w * 0.05f, center.y - h * 0.35f),
        size = Size(w * 0.5f, h * 0.9f)
    )
}

private fun DrawScope.drawClouds(
    drift: Float,
    onHeader: Boolean,
    partlySunny: Boolean
) {
    val dx = drift * size.width * 0.06f
    val light = if (onHeader) Color(0xFFF8FAFC) else Color(0xFFF8FAFC)
    val shadow = if (onHeader) Color(0xFFCBD5E1) else Color(0xFF94A3B8)

    if (partlySunny) {
        val cx = size.width * 0.32f
        val cy = size.height * 0.38f
        drawCircle(color = Color(0xFFFBBF24), radius = size.minDimension * 0.16f, center = Offset(cx, cy))
    }

    translate(left = dx) {
        drawCloudBlob(
            center = Offset(size.width * 0.52f, size.height * 0.52f),
            scale = 1f,
            light = light,
            shadow = shadow
        )
    }
}

private fun DrawScope.drawRain(
    drift: Float,
    rainPhase: Float,
    onHeader: Boolean
) {
    val dx = drift * size.width * 0.04f
    val light = Color(0xFFE2E8F0)
    val shadow = Color(0xFF64748B)
    val drop = if (onHeader) Color(0xFF7DD3FC) else Color(0xFF38BDF8)

    translate(left = dx) {
        drawCloudBlob(
            center = Offset(size.width * 0.5f, size.height * 0.36f),
            scale = 0.92f,
            light = light,
            shadow = shadow
        )
    }

    val dropCount = 5
    for (i in 0 until dropCount) {
        val t = (rainPhase + i / dropCount.toFloat()) % 1f
        val x = size.width * (0.28f + i * 0.11f) + dx * 0.3f
        val yStart = size.height * 0.48f
        val yEnd = size.height * 0.92f
        val y = yStart + (yEnd - yStart) * t
        val alpha = if (t < 0.15f) t / 0.15f else if (t > 0.85f) (1f - t) / 0.15f else 1f
        drawLine(
            color = drop.copy(alpha = alpha * 0.95f),
            start = Offset(x, y),
            end = Offset(x - size.minDimension * 0.02f, y + size.minDimension * 0.12f),
            strokeWidth = size.minDimension * 0.045f
        )
    }
}

private fun DrawScope.drawStorm(
    drift: Float,
    flash: Float,
    shake: Float,
    onHeader: Boolean
) {
    val dx = drift * size.width * 0.03f + shake * size.width * 0.012f
    val light = Color(0xFF94A3B8)
    val shadow = Color(0xFF475569)
    val bolt = Color(0xFFFACC15)

    translate(left = dx) {
        drawCloudBlob(
            center = Offset(size.width * 0.5f, size.height * 0.34f),
            scale = 0.95f,
            light = light,
            shadow = shadow
        )
    }

    // Yellow lightning bolt
    val path = Path().apply {
        val bx = size.width * 0.52f + dx
        val by = size.height * 0.42f
        val s = size.minDimension
        moveTo(bx, by)
        lineTo(bx - s * 0.08f, by + s * 0.22f)
        lineTo(bx + s * 0.02f, by + s * 0.22f)
        lineTo(bx - s * 0.06f, by + s * 0.48f)
        lineTo(bx + s * 0.14f, by + s * 0.18f)
        lineTo(bx + s * 0.02f, by + s * 0.18f)
        lineTo(bx + s * 0.12f, by)
        close()
    }
    drawPath(path, color = bolt.copy(alpha = flash))
    // glow flash behind bolt
    if (flash > 0.7f) {
        drawCircle(
            color = bolt.copy(alpha = (flash - 0.7f) * 0.8f),
            radius = size.minDimension * 0.2f,
            center = Offset(size.width * 0.5f + dx, size.height * 0.62f)
        )
    }
}
