package com.trackgod.app.feature.backup

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trackgod.app.core.database.entity.BackupMetadataEntity
import com.trackgod.app.ui.component.ButtonVariant
import com.trackgod.app.ui.component.MetalTextureBackground
import com.trackgod.app.ui.component.SectionDivider
import com.trackgod.app.ui.component.TrackGodButton
import com.trackgod.app.ui.component.TrackGodCard
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextSecondary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.Void
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

@Composable
fun BackupScreen(
    onNavigateBack: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backups by viewModel.backups.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // -- File picker for import -----------------------------------------------
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let { viewModel.importDatabase(it) }
    }

    // -- Share intent for export ----------------------------------------------
    LaunchedEffect(uiState.exportUri) {
        uiState.exportUri?.let { uri ->
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "EXPORT DATABASE"))
            viewModel.clearExportUri()
        }
    }

    // -- Confirm dialog state -------------------------------------------------
    var confirmRestore by remember { mutableStateOf<BackupMetadataEntity?>(null) }
    var confirmDelete by remember { mutableStateOf<BackupMetadataEntity?>(null) }

    // -- Restart dialog -------------------------------------------------------
    if (uiState.showRestartDialog) {
        AlertDialog(
            onDismissRequest = { /* block dismiss */ },
            title = {
                Text(
                    text = "RESTART REQUIRED",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
            },
            text = {
                Text(
                    text = "DATABASE RESTORED SUCCESSFULLY. PLEASE RESTART TRACKGOD FOR CHANGES TO TAKE EFFECT.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissRestartDialog()
                    exitProcess(0)
                }) {
                    Text("RESTART", color = Blood)
                }
            },
            containerColor = Void,
        )
    }

    // -- Restore confirm dialog -----------------------------------------------
    confirmRestore?.let { backup ->
        AlertDialog(
            onDismissRequest = { confirmRestore = null },
            title = {
                Text(
                    text = "RESTORE FROM BACKUP?",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
            },
            text = {
                Text(
                    text = "CURRENT DATA WILL BE REPLACED WITH THIS BACKUP. A SAFETY BACKUP WILL BE CREATED FIRST.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.restoreFromBackup(backup)
                    confirmRestore = null
                }) {
                    Text("RESTORE", color = Blood)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmRestore = null }) {
                    Text("CANCEL", color = TextTertiary)
                }
            },
            containerColor = Void,
        )
    }

    // -- Delete confirm dialog ------------------------------------------------
    confirmDelete?.let { backup ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = {
                Text(
                    text = "DELETE BACKUP?",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
            },
            text = {
                Text(
                    text = "THIS BACKUP WILL BE PERMANENTLY DELETED.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteBackup(backup)
                    confirmDelete = null
                }) {
                    Text("DELETE", color = Blood)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) {
                    Text("CANCEL", color = TextTertiary)
                }
            },
            containerColor = Void,
        )
    }

    // -- Layout ---------------------------------------------------------------
    MetalTextureBackground {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        // Top bar
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
                text = "BACKUP & RESTORE",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        // Loading indicator
        if (uiState.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = Blood,
                trackColor = SurfaceLow,
            )
        }

        // Message bar
        uiState.message?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.labelMedium,
                color = BloodBright,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceLow)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
        ) {
            // ── STATUS ──────────────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionDivider(text = "STATUS", modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                StatusDashboard(stats = uiState.stats)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── ACTIONS ─────────────────────────────────────────────────────
            item {
                SectionDivider(text = "ACTIONS", modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                TrackGodButton(
                    text = "CREATE BACKUP",
                    onClick = { viewModel.createBackup() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                TrackGodButton(
                    text = "EXPORT DATABASE",
                    onClick = { viewModel.exportDatabase() },
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Secondary,
                    enabled = !uiState.isLoading,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                TrackGodButton(
                    text = "IMPORT DATABASE",
                    onClick = { importLauncher.launch(arrayOf("*/*")) },
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Secondary,
                    enabled = !uiState.isLoading,
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── BACKUP HISTORY ──────────────────────────────────────────────
            item {
                SectionDivider(text = "BACKUP HISTORY", modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (backups.isEmpty()) {
                item {
                    Text(
                        text = "NO BACKUPS YET",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextTertiary,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                }
            }

            itemsIndexed(backups, key = { _, b -> b.id }) { index, backup ->
                BackupHistoryItem(
                    backup = backup,
                    isFirst = index == 0,
                    onRestore = { confirmRestore = backup },
                    onDelete = { confirmDelete = backup },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
    } // MetalTextureBackground
}

// -- Status Dashboard ---------------------------------------------------------

@Composable
private fun StatusDashboard(stats: com.trackgod.app.core.repository.BackupStats) {
    TrackGodCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatColumn(label = "BACKUPS", value = stats.count.toString())
            StatColumn(label = "TOTAL SIZE", value = formatFileSize(stats.totalSizeBytes))
            StatColumn(
                label = "LAST",
                value = stats.lastBackupTime?.let { formatRelativeTime(it) } ?: "--",
            )
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
            letterSpacing = 2.sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Black,
        )
    }
}

// -- Backup History Item ------------------------------------------------------

@Composable
private fun BackupHistoryItem(
    backup: BackupMetadataEntity,
    isFirst: Boolean,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    TrackGodCard(accentBorder = isFirst) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Date + time
                Text(
                    text = formatTimestamp(backup.createdAt),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // File size
                    Text(
                        text = formatFileSize(backup.fileSize),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextTertiary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Type badge
                    TypeBadge(type = backup.backupType)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TrackGodButton(
                text = "RESTORE",
                onClick = onRestore,
                variant = ButtonVariant.Ghost,
            )
            TrackGodButton(
                text = "DELETE",
                onClick = onDelete,
                variant = ButtonVariant.Ghost,
            )
        }
    }
}

@Composable
private fun TypeBadge(type: String) {
    val color = when (type.uppercase()) {
        "AUTO" -> TextTertiary
        "MANUAL" -> BloodBright
        "SAFETY" -> BloodBright
        else -> TextTertiary
    }
    Text(
        text = type.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
    )
}

// -- Formatting helpers -------------------------------------------------------

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
        bytes < 1024L * 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format(Locale.US, "%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}

private fun formatTimestamp(millis: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy  HH:mm", Locale.US)
    return sdf.format(Date(millis)).uppercase()
}

private fun formatRelativeTime(millis: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - millis
    val minutes = diff / 60_000
    val hours = diff / 3_600_000
    val days = diff / 86_400_000

    return when {
        minutes < 1 -> "NOW"
        minutes < 60 -> "${minutes}M AGO"
        hours < 24 -> "${hours}H AGO"
        days < 7 -> "${days}D AGO"
        else -> formatTimestamp(millis)
    }
}
