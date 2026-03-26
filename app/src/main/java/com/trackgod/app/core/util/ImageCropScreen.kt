package com.trackgod.app.core.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trackgod.app.ui.component.ButtonVariant
import com.trackgod.app.ui.component.TrackGodButton
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.Void
import java.io.File
import java.io.FileOutputStream

/**
 * Full-screen crop overlay using Canvas for pixel-accurate rendering.
 * No density conversion issues — everything is in screen pixels.
 */
@Composable
fun ImageCropOverlay(
    sourceUri: Uri,
    onConfirm: (Uri) -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(sourceUri) {
        val stream = context.contentResolver.openInputStream(sourceUri)
        bitmap = stream?.let { BitmapFactory.decodeStream(it) }
        stream?.close()
    }

    val bmp = bitmap ?: return
    val imageBitmap = remember(bmp) { bmp.asImageBitmap() }

    // All values in screen pixels — no density conversion needed
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }

    // scale = how many screen pixels per image pixel
    var scale by remember { mutableFloatStateOf(1f) }
    // imgX/imgY = top-left of image in canvas coordinates
    var imgX by remember { mutableFloatStateOf(0f) }
    var imgY by remember { mutableFloatStateOf(0f) }
    var initialized by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Void),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "POSITION YOUR PHOTO",
                color = BloodBright,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            // Canvas area with gestures
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.1f, 10f)
                            imgX += pan.x
                            imgY += pan.y
                        }
                    },
            ) {
                canvasWidth = size.width
                canvasHeight = size.height

                val viewportSize = minOf(size.width, size.height) * 0.8f
                val vpLeft = (size.width - viewportSize) / 2f
                val vpTop = (size.height - viewportSize) / 2f

                // Initialize: fit image so shortest side fills viewport
                if (!initialized && bmp.width > 0) {
                    val minDim = minOf(bmp.width, bmp.height).toFloat()
                    scale = viewportSize / minDim
                    imgX = (size.width - bmp.width * scale) / 2f
                    imgY = (size.height - bmp.height * scale) / 2f
                    initialized = true
                }

                // Draw image
                drawImage(
                    image = imageBitmap,
                    dstOffset = IntOffset(imgX.toInt(), imgY.toInt()),
                    dstSize = IntSize(
                        (bmp.width * scale).toInt(),
                        (bmp.height * scale).toInt(),
                    ),
                )

                // Draw dimming overlay (4 rects around viewport)
                val dim = Color.Black.copy(alpha = 0.7f)
                drawRect(dim, Offset.Zero, Size(size.width, vpTop)) // top
                drawRect(dim, Offset(0f, vpTop + viewportSize), Size(size.width, size.height - vpTop - viewportSize)) // bottom
                drawRect(dim, Offset(0f, vpTop), Size(vpLeft, viewportSize)) // left
                drawRect(dim, Offset(vpLeft + viewportSize, vpTop), Size(size.width - vpLeft - viewportSize, viewportSize)) // right

                // Draw viewport border
                drawRect(
                    color = Blood,
                    topLeft = Offset(vpLeft, vpTop),
                    size = Size(viewportSize, viewportSize),
                    style = Stroke(2.dp.toPx()),
                )
            }

            // Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceLow)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TrackGodButton(
                    text = "CANCEL",
                    onClick = onCancel,
                    variant = ButtonVariant.Secondary,
                    modifier = Modifier.weight(1f),
                )
                TrackGodButton(
                    text = "CONFIRM",
                    onClick = {
                        val viewportSize = minOf(canvasWidth, canvasHeight) * 0.8f
                        val vpLeft = (canvasWidth - viewportSize) / 2f
                        val vpTop = (canvasHeight - viewportSize) / 2f
                        val result = performCrop(context, bmp, scale, imgX, imgY, vpLeft, vpTop, viewportSize)
                        if (result != null) onConfirm(result) else onCancel()
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * Crop: map viewport rectangle back to image pixel coordinates.
 * All inputs are in screen pixels — no density involved.
 */
private fun performCrop(
    context: android.content.Context,
    original: Bitmap,
    scale: Float,
    imgX: Float,
    imgY: Float,
    vpLeft: Float,
    vpTop: Float,
    viewportSize: Float,
): Uri? {
    if (scale <= 0 || viewportSize <= 0) return null

    // Viewport in image pixel coordinates
    val cropX = ((vpLeft - imgX) / scale).toInt().coerceIn(0, original.width - 1)
    val cropY = ((vpTop - imgY) / scale).toInt().coerceIn(0, original.height - 1)
    val cropSize = (viewportSize / scale).toInt().coerceIn(1, minOf(original.width - cropX, original.height - cropY))

    val cropped = Bitmap.createBitmap(original, cropX, cropY, cropSize, cropSize)
    val scaled = Bitmap.createScaledBitmap(cropped, 512, 512, true)

    // Clean up old avatars
    context.filesDir.listFiles()?.filter { it.name.startsWith("avatar_") }?.forEach { it.delete() }

    val file = File(context.filesDir, "avatar_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { out ->
        scaled.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }

    if (cropped !== original) cropped.recycle()
    if (scaled !== cropped) scaled.recycle()

    return Uri.fromFile(file)
}
