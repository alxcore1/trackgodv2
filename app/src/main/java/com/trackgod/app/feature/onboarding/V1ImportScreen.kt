package com.trackgod.app.feature.onboarding

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trackgod.app.ui.component.ButtonVariant
import com.trackgod.app.ui.component.TrackGodButton
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextSecondary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.Void
import com.trackgod.app.ui.theme.VoidDeep

@Composable
fun V1ImportScreen(
    onNavigateBack: () -> Unit,
    onImportComplete: () -> Unit = onNavigateBack,
    viewModel: V1ImportViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult

        // Resolve display name
        val fileName = try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex) else null
            }
        } catch (_: Exception) {
            null
        }

        viewModel.importDatabase(uri, fileName)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Void)
            .padding(horizontal = 24.dp),
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // -- Header ---------------------------------------------------------------
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "TRACKGOD",
                style = MaterialTheme.typography.labelLarge,
                color = TextTertiary,
                letterSpacing = 4.sp,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // -- Heading --------------------------------------------------------------
        Text(
            text = buildAnnotatedString {
                append("IMPORT FROM\n")
                withStyle(SpanStyle(color = Blood)) {
                    append("V1")
                }
            },
            style = MaterialTheme.typography.displaySmall,
            color = TextPrimary,
            lineHeight = 34.sp,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // -- Content based on state -----------------------------------------------
        AnimatedContent(
            targetState = state.status,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "import_status",
        ) { status ->
            Column {
                when (status) {
                    ImportStatus.Idle -> IdleContent()
                    ImportStatus.Importing -> ImportingContent(state.selectedFileName)
                    ImportStatus.Success -> SuccessContent(state)
                    ImportStatus.Error -> ErrorContent(state)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // -- Bottom buttons -------------------------------------------------------
        when (state.status) {
            ImportStatus.Idle -> {
                TrackGodButton(
                    text = "SELECT .DB FILE",
                    onClick = {
                        filePickerLauncher.launch(arrayOf("application/octet-stream", "*/*"))
                    },
                    variant = ButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
                TrackGodButton(
                    text = "BACK",
                    onClick = onNavigateBack,
                    variant = ButtonVariant.Secondary,
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            ImportStatus.Importing -> {
                // No buttons while importing
            }
            ImportStatus.Success -> {
                TrackGodButton(
                    text = "CONTINUE",
                    onClick = onImportComplete,
                    variant = ButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            ImportStatus.Error -> {
                TrackGodButton(
                    text = "TRY AGAIN",
                    onClick = {
                        viewModel.resetState()
                        filePickerLauncher.launch(arrayOf("application/octet-stream", "*/*"))
                    },
                    variant = ButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
                TrackGodButton(
                    text = "BACK",
                    onClick = onNavigateBack,
                    variant = ButtonVariant.Secondary,
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// -- Sub-sections -----------------------------------------------------------------

@Composable
private fun IdleContent() {
    Text(
        text = "Import your workouts, exercises, and body metrics from TrackGod v1.",
        style = MaterialTheme.typography.bodyLarge,
        color = TextPrimary,
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = "Select the exported .db file from your v1 backup. Your existing v2 data will not be overwritten.",
        style = MaterialTheme.typography.bodyMedium,
        color = TextTertiary,
    )
}

@Composable
private fun ImportingContent(fileName: String?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = Blood,
            strokeWidth = 2.5.dp,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Importing data...",
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
        )
    }
    if (fileName != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = fileName,
            style = MaterialTheme.typography.bodySmall,
            color = TextTertiary,
        )
    }
}

@Composable
private fun SuccessContent(state: V1ImportState) {
    val result = state.result ?: return

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = BloodBright,
            modifier = Modifier.size(28.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Import Complete",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Stats card
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VoidDeep, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ImportStatRow("Workouts", result.workoutsImported)
        ImportStatRow("Exercises", result.exercisesImported)
        ImportStatRow("Sets", result.setsImported)
        ImportStatRow("Body Metrics", result.bodyMetricsImported)
        if (result.profileImported) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Text(
                    text = "Imported",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BloodBright,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(state: V1ImportState) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Blood,
            modifier = Modifier.size(28.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Import Failed",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = state.result?.error ?: "An unknown error occurred.",
        style = MaterialTheme.typography.bodyMedium,
        color = TextTertiary,
    )
}

@Composable
private fun ImportStatRow(label: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = BloodBright,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
