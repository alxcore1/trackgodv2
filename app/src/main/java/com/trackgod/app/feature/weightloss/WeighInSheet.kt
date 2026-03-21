package com.trackgod.app.feature.weightloss

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.trackgod.app.ui.component.ButtonVariant
import com.trackgod.app.ui.component.NumberInput
import com.trackgod.app.ui.component.TrackGodButton
import com.trackgod.app.ui.component.TrackGodTextField
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.Void

/**
 * Bottom sheet for logging a new weigh-in.
 *
 * @param lastWeight Pre-fill from the previous weigh-in.
 * @param weightUnit Display unit (kg or lbs).
 * @param onLog Callback with (weight, note, photoUri).
 * @param onDismiss Called when the sheet is dismissed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeighInSheet(
    lastWeight: Float?,
    weightUnit: String,
    onLog: (Float, String?, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var weight by remember {
        mutableStateOf(lastWeight?.let { "%.1f".format(it) } ?: "")
    }
    var note by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            } catch (_: SecurityException) {
                // Some providers don't support persistable permissions
            }
            photoUri = uri.toString()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Void,
        shape = RectangleShape,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Title
            Text(
                text = "LOG WEIGH-IN",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Weight input
            NumberInput(
                value = weight,
                onValueChange = { weight = it },
                label = "WEIGHT",
                unit = weightUnit.uppercase(),
                step = 0.1f,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Note
            TrackGodTextField(
                value = note,
                onValueChange = { note = it },
                label = "NOTE (OPTIONAL)",
                placeholder = "How are you feeling?",
                singleLine = false,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Photo picker + preview
            if (photoUri != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(Uri.parse(photoUri))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Selected photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .background(SurfaceLow),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "PHOTO ATTACHED",
                        color = TextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.weight(1f),
                    )
                    TrackGodButton(
                        text = "CHANGE",
                        onClick = { photoPickerLauncher.launch("image/*") },
                        variant = ButtonVariant.Ghost,
                    )
                }
            } else {
                TrackGodButton(
                    text = "ADD PHOTO",
                    onClick = { photoPickerLauncher.launch("image/*") },
                    variant = ButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Log
            TrackGodButton(
                text = "LOG WEIGH-IN",
                onClick = {
                    val w = weight.toFloatOrNull() ?: return@TrackGodButton
                    val n = note.ifBlank { null }
                    onLog(w, n, photoUri)
                },
                enabled = weight.toFloatOrNull() != null,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Cancel
            TrackGodButton(
                text = "CANCEL",
                onClick = onDismiss,
                variant = ButtonVariant.Ghost,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
