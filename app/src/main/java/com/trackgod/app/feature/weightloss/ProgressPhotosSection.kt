package com.trackgod.app.feature.weightloss

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.trackgod.app.core.database.entity.BodyMetricEntity
import com.trackgod.app.ui.component.ButtonVariant
import com.trackgod.app.ui.component.TrackGodButton
import com.trackgod.app.ui.theme.SurfaceHighest
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextTertiary

/**
 * Horizontal gallery of progress photos with TAKE PHOTO and COMPARE actions.
 *
 * Embeds inside [WeightLossScreen] below the milestones section.
 *
 * @param photos Current list of body metric entries that have a photo URI.
 * @param onPhotoAdded Called with the content URI string after the user picks an image.
 * @param onCompare Called when the user taps COMPARE (only enabled with 2+ photos).
 */
@Composable
fun ProgressPhotosSection(
    photos: List<BodyMetricEntity>,
    onPhotoAdded: (String) -> Unit,
    onCompare: () -> Unit,
) {
    val context = LocalContext.current

    // Gallery picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri != null) {
            // Take persistable read permission so the URI survives process death
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            } catch (_: SecurityException) {
                // Some providers don't support persistable permissions -- still use the URI
            }
            onPhotoAdded(uri.toString())
        }
    }

    if (photos.isEmpty()) {
        // Empty state
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(SurfaceLow),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "NO PHOTOS YET",
                color = TextTertiary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            )
        }
    } else {
        // Photo gallery
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(photos, key = { it.id }) { photo ->
                PhotoThumbnail(photo = photo)
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Action buttons
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TrackGodButton(
            text = "TAKE PHOTO",
            onClick = { photoPickerLauncher.launch("image/*") },
            modifier = Modifier.weight(1f),
        )
        TrackGodButton(
            text = "COMPARE",
            onClick = onCompare,
            variant = ButtonVariant.Secondary,
            enabled = photos.size >= 2,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * Single square photo thumbnail with date label.
 */
@Composable
private fun PhotoThumbnail(
    photo: BodyMetricEntity,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .border(width = 1.dp, color = SurfaceHighest, shape = RectangleShape)
                .background(SurfaceLow),
            contentAlignment = Alignment.Center,
        ) {
            if (photo.photoUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(Uri.parse(photo.photoUri))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Progress photo ${photo.date}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(100.dp),
                )
            } else {
                Text(
                    text = "?",
                    color = TextTertiary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = photo.date.uppercase(),
            color = TextTertiary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
        )
    }
}
