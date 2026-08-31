package com.example.iremember.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.iremember.ui.components.HeaderIconButton
import com.example.iremember.ui.theme.Spacing
import com.example.iremember.util.ProfileStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

@Composable
fun ProfilePhotoCropScreen(
    sourceUri: String,
    profileStore: ProfileStore,
    onCancel: () -> Unit,
    onCropped: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var loadError by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var viewW by remember { mutableFloatStateOf(0f) }
    var viewH by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(sourceUri) {
        bitmap = withContext(Dispatchers.IO) {
            decodeSampledBitmap(context, sourceUri, maxSize = 2048)
        }
        loadError = bitmap == null
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderIconButton(
                onClick = onCancel,
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Cancel",
                containerColor = Color.White.copy(alpha = 0.14f),
                iconTint = Color.White
            )
            Spacer(modifier = Modifier.width(Spacing.md))
            Text(
                text = "Crop photo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "Pinch to zoom · drag to reposition",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = Spacing.xl, vertical = Spacing.xs)
        )

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(Spacing.md),
            contentAlignment = Alignment.Center
        ) {
            val density = LocalDensity.current
            viewW = with(density) { maxWidth.toPx() }
            viewH = with(density) { maxHeight.toPx() }
            val cropDiameter = min(viewW, viewH) * 0.78f

            when {
                saving || (bitmap == null && !loadError) -> {
                    CircularProgressIndicator(color = Color.White)
                }
                loadError -> {
                    Text("Could not load this image.", color = Color.White)
                }
                else -> {
                    val bmp = bitmap!!
                    val baseScale = min(viewW / bmp.width, viewH / bmp.height)
                    val displayW = bmp.width * baseScale
                    val displayH = bmp.height * baseScale

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(1f, 5f)
                                    val maxX = (displayW * scale - cropDiameter).coerceAtLeast(0f) / 2f
                                    val maxY = (displayH * scale - cropDiameter).coerceAtLeast(0f) / 2f
                                    offsetX = (offsetX + pan.x).coerceIn(-maxX, maxX)
                                    offsetY = (offsetY + pan.y).coerceIn(-maxY, maxY)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offsetX
                                translationY = offsetY
                            }
                        )

                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                        ) {
                            drawRect(Color.Black.copy(alpha = 0.55f))
                            drawCircle(
                                color = Color.Transparent,
                                radius = cropDiameter / 2f,
                                center = Offset(size.width / 2f, size.height / 2f),
                                blendMode = BlendMode.Clear
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.9f),
                                radius = cropDiameter / 2f,
                                center = Offset(size.width / 2f, size.height / 2f),
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onCancel,
                enabled = !saving,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel", color = Color.White)
            }
            Spacer(modifier = Modifier.width(Spacing.md))
            Button(
                onClick = {
                    val bmp = bitmap ?: return@Button
                    if (viewW <= 0f || viewH <= 0f) return@Button
                    saving = true
                    val s = scale
                    val ox = offsetX
                    val oy = offsetY
                    val vw = viewW
                    val vh = viewH
                    scope.launch {
                        val ok = withContext(Dispatchers.Default) {
                            val cropped = cropBitmapToCircle(bmp, s, ox, oy, vw, vh)
                            if (cropped != null) profileStore.updateImageFromBitmap(cropped) else false
                        }
                        saving = false
                        if (ok) onCropped() else loadError = true
                    }
                },
                enabled = !saving && bitmap != null,
                modifier = Modifier
                    .weight(1.4f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (saving) "Saving…" else "Use photo",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

private fun cropBitmapToCircle(
    source: Bitmap,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    viewWidthPx: Float,
    viewHeightPx: Float
): Bitmap? {
    return try {
        val baseScale = min(viewWidthPx / source.width, viewHeightPx / source.height)
        val totalScale = baseScale * scale
        val cropDiameterView = min(viewWidthPx, viewHeightPx) * 0.78f
        val cropSizeSrc = (cropDiameterView / totalScale).toInt().coerceAtLeast(64)

        val centerXSrc = source.width / 2f - offsetX / totalScale
        val centerYSrc = source.height / 2f - offsetY / totalScale
        val left = (centerXSrc - cropSizeSrc / 2f).toInt()
        val top = (centerYSrc - cropSizeSrc / 2f).toInt()

        val safeLeft = left.coerceIn(0, max(0, source.width - cropSizeSrc))
        val safeTop = top.coerceIn(0, max(0, source.height - cropSizeSrc))
        val safeSize = min(cropSizeSrc, min(source.width - safeLeft, source.height - safeTop))
            .coerceAtLeast(1)

        val square = Bitmap.createBitmap(source, safeLeft, safeTop, safeSize, safeSize)
        val outputSize = 512
        val scaled = Bitmap.createScaledBitmap(square, outputSize, outputSize, true)
        if (square !== scaled && square !== source) square.recycle()

        val output = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawCircle(outputSize / 2f, outputSize / 2f, outputSize / 2f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(scaled, 0f, 0f, paint)
        if (scaled !== source) scaled.recycle()
        output
    } catch (_: Exception) {
        null
    }
}

private fun decodeSampledBitmap(
    context: android.content.Context,
    sourceUri: String,
    maxSize: Int
): Bitmap? {
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(Uri.parse(sourceUri))?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        var inSample = 1
        var halfH = bounds.outHeight / 2
        var halfW = bounds.outWidth / 2
        while (halfH / inSample >= maxSize && halfW / inSample >= maxSize) {
            inSample *= 2
        }
        val opts = BitmapFactory.Options().apply { inSampleSize = inSample.coerceAtLeast(1) }
        context.contentResolver.openInputStream(Uri.parse(sourceUri))?.use {
            BitmapFactory.decodeStream(it, null, opts)
        }
    } catch (_: Exception) {
        null
    }
}
