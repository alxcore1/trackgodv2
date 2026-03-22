package com.trackgod.app.feature.weightloss

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.trackgod.app.core.database.entity.BodyMetricEntity
import com.trackgod.app.ui.component.MetalTextureBackground
import com.trackgod.app.ui.component.SectionDivider
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.SurfaceHighest
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary

/**
 * Before/after photo comparison with a draggable divider.
 *
 * The BEFORE image is shown on the left (clipped at divider position),
 * the AFTER image on the right. A vertical drag handle in Blood lets the
 * user slide between the two.
 *
 * A thumbnail row at the bottom lets the user pick which photos to compare.
 *
 * @param photos All progress photos (must have at least 2).
 * @param onNavigateBack Called when the user presses back.
 */
@Composable
fun PhotoComparisonScreen(
    photos: List<BodyMetricEntity>,
    onNavigateBack: () -> Unit,
) {
    if (photos.size < 2) {
        // Safety fallback -- should not happen via normal navigation
        onNavigateBack()
        return
    }

    // Selected photo IDs -- default oldest as BEFORE, newest as AFTER
    var beforeId by remember { mutableLongStateOf(photos.last().id) }
    var afterId by remember { mutableLongStateOf(photos.first().id) }

    val beforePhoto = photos.find { it.id == beforeId } ?: photos.last()
    val afterPhoto = photos.find { it.id == afterId } ?: photos.first()

    // Which side the user is selecting for (null = not selecting)
    // 0 = BEFORE, 1 = AFTER
    var selectingSlot by remember { mutableFloatStateOf(-1f) }

    MetalTextureBackground {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 4.dp),
    ) {
        // -- Top bar --
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                )
            }
            Text(
                text = "COMPARE",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        // -- Labels --
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "BEFORE",
                color = TextTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
            )
            Text(
                text = "AFTER",
                color = TextTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // -- Comparison viewport --
        ComparisonViewport(
            beforePhoto = beforePhoto,
            afterPhoto = afterPhoto,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(4.dp))

        // -- Date labels --
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = beforePhoto.date.uppercase(),
                color = TextTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
            Text(
                text = afterPhoto.date.uppercase(),
                color = TextTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // -- Thumbnail selector --
        SectionDivider(
            text = "SELECT PHOTOS",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Instruction
        Text(
            text = if (selectingSlot == 0f) {
                "TAP A PHOTO FOR BEFORE"
            } else if (selectingSlot == 1f) {
                "TAP A PHOTO FOR AFTER"
            } else {
                "TAP BEFORE OR AFTER LABEL, THEN A THUMBNAIL"
            },
            color = TextTertiary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Slot selector buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .background(if (selectingSlot == 0f) Blood else SurfaceLow)
                    .border(
                        width = 1.dp,
                        color = if (selectingSlot == 0f) Blood else SurfaceHighest,
                        shape = RectangleShape,
                    )
                    .clickable { selectingSlot = if (selectingSlot == 0f) -1f else 0f },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "BEFORE",
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .background(if (selectingSlot == 1f) Blood else SurfaceLow)
                    .border(
                        width = 1.dp,
                        color = if (selectingSlot == 1f) Blood else SurfaceHighest,
                        shape = RectangleShape,
                    )
                    .clickable { selectingSlot = if (selectingSlot == 1f) -1f else 1f },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "AFTER",
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(photos, key = { it.id }) { photo ->
                val isSelected = photo.id == beforeId || photo.id == afterId
                SelectableThumbnail(
                    photo = photo,
                    isSelected = isSelected,
                    onClick = {
                        when {
                            selectingSlot == 0f -> {
                                beforeId = photo.id
                                selectingSlot = -1f
                            }
                            selectingSlot == 1f -> {
                                afterId = photo.id
                                selectingSlot = -1f
                            }
                            // Auto-assign: if tapped photo is already selected, ignore
                            photo.id != beforeId && photo.id != afterId -> {
                                // Default: assign to BEFORE first, then AFTER
                                selectingSlot = 0f
                            }
                        }
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
    } // MetalTextureBackground
}

// -- Comparison Viewport with drag divider ------------------------------------

@Composable
private fun ComparisonViewport(
    beforePhoto: BodyMetricEntity,
    afterPhoto: BodyMetricEntity,
    modifier: Modifier = Modifier,
) {
    var dividerFraction by remember { mutableFloatStateOf(0.5f) }
    var containerWidthPx by remember { mutableFloatStateOf(1f) }
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .clipToBounds()
            .border(width = 1.dp, color = SurfaceHighest, shape = RectangleShape)
            .onSizeChanged { size ->
                containerWidthPx = size.width.toFloat()
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (containerWidthPx > 0f) {
                        dividerFraction = (dividerFraction + dragAmount / containerWidthPx)
                            .coerceIn(0.05f, 0.95f)
                    }
                }
            },
    ) {
        // AFTER image (full width, behind)
        if (afterPhoto.photoUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(Uri.parse(afterPhoto.photoUri))
                    .crossfade(true)
                    .error(android.R.drawable.ic_menu_gallery)
                    .build(),
                contentDescription = "After photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceLow),
                contentAlignment = Alignment.Center,
            ) {
                Text("NO IMAGE", color = TextTertiary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // BEFORE image (clipped to divider fraction)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = dividerFraction)
                .clipToBounds(),
        ) {
            if (beforePhoto.photoUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(Uri.parse(beforePhoto.photoUri))
                        .crossfade(true)
                        .error(android.R.drawable.ic_menu_gallery)
                        .build(),
                    contentDescription = "Before photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SurfaceLow),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("NO IMAGE", color = TextTertiary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Drag handle line
        val handleOffsetPx = (dividerFraction * containerWidthPx).toInt()
        val handleWidthDp = with(density) { 4.dp.toPx() }.toInt()

        Box(
            modifier = Modifier
                .offset { IntOffset(handleOffsetPx - handleWidthDp / 2, 0) }
                .width(4.dp)
                .fillMaxHeight()
                .background(Blood),
        )
    }
}

// -- Selectable thumbnail for the bottom row ----------------------------------

@Composable
private fun SelectableThumbnail(
    photo: BodyMetricEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) Blood else SurfaceHighest,
                    shape = RectangleShape,
                )
                .background(SurfaceLow)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (photo.photoUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(Uri.parse(photo.photoUri))
                        .crossfade(true)
                        .error(android.R.drawable.ic_menu_gallery)
                        .build(),
                    contentDescription = "Thumbnail ${photo.date}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(64.dp),
                )
            } else {
                Text(
                    text = "?",
                    color = TextTertiary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = photo.date.uppercase(),
            color = if (isSelected) Blood else TextTertiary,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        )
    }
}
