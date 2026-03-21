package com.trackgod.app.feature.ocr

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trackgod.app.ui.component.ButtonVariant
import com.trackgod.app.ui.component.MetalTextureBackground
import com.trackgod.app.ui.component.SectionDivider
import com.trackgod.app.ui.component.TrackGodButton
import com.trackgod.app.ui.component.TrackGodCard
import com.trackgod.app.ui.component.TrackGodTextField
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.SurfaceHighest
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextSecondary
import com.trackgod.app.ui.theme.TextTertiary
import java.util.concurrent.Executors

@Composable
fun OcrScannerScreen(
    onExerciseSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
    viewModel: OcrViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Navigate back when exercise is confirmed
    LaunchedEffect(state.confirmedExerciseId) {
        state.confirmedExerciseId?.let { id ->
            onExerciseSelected(id)
        }
    }

    // Camera permission
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    MetalTextureBackground {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextTertiary)
            }
            Text(
                text = "SCAN MACHINE",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, "Close", tint = TextTertiary)
            }
        }

        if (state.isScanning) {
            CameraPhase(
                hasPermission = hasPermission,
                isProcessing = state.isProcessing,
                error = state.error,
                onCapture = { bitmap -> viewModel.processCapture(bitmap) },
            )
        } else {
            ResultsPhase(
                state = state,
                onSelectMatch = viewModel::selectMatch,
                onConfirm = viewModel::confirmSelection,
                onManualEntry = viewModel::showManualEntry,
                onScanAgain = viewModel::resetScan,
                onManualNameChange = viewModel::updateManualName,
                onManualCategoryChange = viewModel::updateManualCategory,
                onManualEquipmentChange = viewModel::updateManualEquipment,
                onSaveManual = viewModel::saveManualEntry,
            )
        }
    }
    } // MetalTextureBackground
}

@Composable
private fun CameraPhase(
    hasPermission: Boolean,
    isProcessing: Boolean,
    error: String?,
    onCapture: (Bitmap) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().build() }
    val executor = remember { Executors.newSingleThreadExecutor() }

    if (!hasPermission) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("CAMERA PERMISSION REQUIRED", color = TextTertiary, style = MaterialTheme.typography.labelLarge)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture
                    )
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize(),
        )

        // Scan frame overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    val frameWidth = size.width * 0.8f
                    val frameHeight = 120.dp.toPx()
                    val left = (size.width - frameWidth) / 2
                    val top = (size.height - frameHeight) / 2

                    // Dark overlay with cutout
                    clipRect(left, top, left + frameWidth, top + frameHeight, clipOp = ClipOp.Difference) {
                        drawRect(Color.Black.copy(alpha = 0.6f))
                    }
                    // Red border around scan frame
                    val strokeWidth = 2.dp.toPx()
                    drawRect(Blood, Offset(left, top), Size(frameWidth, frameHeight),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth))
                },
        )

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            error?.let {
                Text(it, color = BloodBright, style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(16.dp))
            }

            Text(
                text = "POINT AT MACHINE LABEL",
                color = TextTertiary,
                style = MaterialTheme.typography.labelLarge,
                letterSpacing = 3.sp,
                modifier = Modifier.padding(bottom = 24.dp),
            )

            if (isProcessing) {
                CircularProgressIndicator(color = Blood, modifier = Modifier.size(64.dp))
            } else {
                // Square capture button (industrial brutalism)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Blood, RectangleShape)
                        .clickable {
                            imageCapture.takePicture(executor,
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                        val bitmap = imageProxy.toBitmap()
                                        // Crop to center scan frame region (~80% width, middle 20% height)
                                        val cropWidth = (bitmap.width * 0.8f).toInt()
                                        val cropHeight = (bitmap.height * 0.2f).toInt()
                                        val cropX = (bitmap.width - cropWidth) / 2
                                        val cropY = (bitmap.height - cropHeight) / 2
                                        val cropped = Bitmap.createBitmap(bitmap, cropX, cropY, cropWidth, cropHeight)
                                        imageProxy.close()
                                        onCapture(cropped)
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        // Error handled via state
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.CameraAlt, "Capture", tint = TextPrimary, modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResultsPhase(
    state: OcrState,
    onSelectMatch: (OcrMatch) -> Unit,
    onConfirm: () -> Unit,
    onManualEntry: () -> Unit,
    onScanAgain: () -> Unit,
    onManualNameChange: (String) -> Unit,
    onManualCategoryChange: (String) -> Unit,
    onManualEquipmentChange: (String) -> Unit,
    onSaveManual: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        // Detected text
        Text("DETECTED TEXT", style = MaterialTheme.typography.labelLarge, color = TextTertiary, letterSpacing = 2.sp)
        Spacer(Modifier.height(8.dp))
        TrackGodCard {
            Text(
                text = state.result?.rawText?.ifBlank { "No text detected" } ?: "No text detected",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }

        Spacer(Modifier.height(24.dp))

        // Matches
        if (state.result?.matches?.isNotEmpty() == true) {
            SectionDivider("MATCHES", Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))

            state.result.matches.forEachIndexed { index, match ->
                val isSelected = state.selectedMatch == match
                TrackGodCard(
                    accentBorder = index == 0 || isSelected,
                    onClick = { onSelectMatch(match) },
                    modifier = Modifier.padding(bottom = 8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = match.exercise.name.uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontWeight = FontWeight.Black,
                            )
                            Text(
                                text = "${match.exercise.category.uppercase()} · ${match.exercise.equipmentType.uppercase()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextTertiary,
                                letterSpacing = 2.sp,
                            )
                        }
                        Text(
                            text = "${(match.similarity * 100).toInt()}%",
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (match.similarity > 0.8f) BloodBright else TextTertiary,
                            fontWeight = FontWeight.Black,
                        )
                    }
                }
            }
        } else {
            Text("NO MATCHES FOUND", style = MaterialTheme.typography.labelLarge, color = TextTertiary, letterSpacing = 2.sp)
        }

        Spacer(Modifier.height(24.dp))

        // Action buttons
        if (state.selectedMatch != null) {
            TrackGodButton(text = "USE SELECTED", onClick = onConfirm, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
        }
        TrackGodButton(text = "ENTER MANUALLY", onClick = onManualEntry, variant = ButtonVariant.Secondary, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        TrackGodButton(text = "SCAN AGAIN", onClick = onScanAgain, variant = ButtonVariant.Ghost, modifier = Modifier.fillMaxWidth())

        // Manual entry section
        AnimatedVisibility(visible = state.showManualEntry) {
            Column(modifier = Modifier.padding(top = 24.dp)) {
                SectionDivider("MANUAL ENTRY", Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))

                TrackGodTextField(
                    value = state.manualName,
                    onValueChange = onManualNameChange,
                    label = "NAME",
                    placeholder = "MACHINE NAME",
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))

                Text("CATEGORY", style = MaterialTheme.typography.labelMedium, color = TextTertiary, letterSpacing = 2.sp)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Chest", "Back", "Shoulders", "Arms", "Legs", "Core").forEach { cat ->
                        val selected = state.manualCategory == cat
                        Box(
                            modifier = Modifier
                                .background(if (selected) Blood else SurfaceHighest, RectangleShape)
                                .clickable { onManualCategoryChange(cat) }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(cat.uppercase(), style = MaterialTheme.typography.labelMedium,
                                color = if (selected) TextPrimary else TextTertiary, letterSpacing = 2.sp)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("EQUIPMENT", style = MaterialTheme.typography.labelMedium, color = TextTertiary, letterSpacing = 2.sp)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("machine", "cable", "barbell", "dumbbell", "bodyweight", "other").forEach { eq ->
                        val selected = state.manualEquipment == eq
                        Box(
                            modifier = Modifier
                                .background(if (selected) Blood else SurfaceHighest, RectangleShape)
                                .clickable { onManualEquipmentChange(eq) }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(eq.uppercase(), style = MaterialTheme.typography.labelMedium,
                                color = if (selected) TextPrimary else TextTertiary, letterSpacing = 2.sp)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                TrackGodButton(
                    text = "SAVE & USE",
                    onClick = onSaveManual,
                    enabled = state.manualName.isNotBlank() && state.manualCategory.isNotBlank() && state.manualEquipment.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}
