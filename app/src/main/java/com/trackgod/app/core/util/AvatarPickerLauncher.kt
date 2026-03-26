package com.trackgod.app.core.util

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Returns a launcher function that opens the system photo picker.
 * Uses [PickVisualMedia] (with built-in search) when available,
 * falls back to [GetContent] on older devices.
 */
@Composable
fun rememberAvatarPickerLauncher(
    onImageSelected: (Uri) -> Unit,
): () -> Unit {
    val context = LocalContext.current

    val modernLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri -> uri?.let(onImageSelected) }

    val fallbackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri -> uri?.let(onImageSelected) }

    return remember(context) {
        {
            if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
                modernLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            } else {
                fallbackLauncher.launch("image/*")
            }
        }
    }
}
