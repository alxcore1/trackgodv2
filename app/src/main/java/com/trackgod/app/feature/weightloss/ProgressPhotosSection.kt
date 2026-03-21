package com.trackgod.app.feature.weightloss

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.Void

/**
 * Horizontal gallery of progress photos with TAKE PHOTO and COMPARE actions.
 *
 * Embeds inside [WeightLossScreen] below the milestones section.
 *
 * @param photos Current list of body metric entries that have a photo URI.
 * @param onPhotoAdded Called with the content URI string after the user picks an image.
 * @param onCompare Called when the user taps COMPARE (only enabled with 2+ photos).
 * @param onPhotoDeleted Called with the metric ID when the user confirms photo deletion.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProgressPhotosSection(
    photos: List<BodyMetricEntity>,
    onPhotoAdded: (String) -> Unit,
    onCompare: () -> Unit,
    onPhotoDeleted: (Long) -> Unit = {},
) {
    val context = LocalContext.current
    var photoToDelete by remember { mutableStateOf<BodyMetricEntity?>(null) }

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
                PhotoThumbnail(
                    photo = photo,
                    onLongClick = { photoToDelete = photo },
                )
            }
        }
    }

    // Confirmation dialog for photo deletion
    if (photoToDelete != null) {
        AlertDialog(
            onDismissRequest = { photoToDelete = null },
            title = {
                Text(
                    text = "DELETE PHOTO",
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this progress photo?",
                    color = TextTertiary,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    photoToDelete?.let { onPhotoDeleted(it.id) }
                    photoToDelete = null
                }) {
                    Text("DELETE", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { photoToDelete = null }) {
                    Text("CANCEL", color = TextTertiary)
                }
            },
            containerColor = Void,
        )
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
 * Long-press triggers the [onLongClick] callback for deletion.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoThumbnail(
    photo: BodyMetricEntity,
    onLongClick: () -> Unit,
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
                .background(SurfaceLow)
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (photo.photoUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(Uri.parse(photo.photoUri))
                        .crossfade(true)
                        .error(android.R.drawable.ic_menu_gallery)
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
