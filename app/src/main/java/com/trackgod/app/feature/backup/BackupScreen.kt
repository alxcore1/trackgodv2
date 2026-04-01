package com.trackgod.app.feature.backup

import com.trackgod.app.ui.theme.screenPadding
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
import com.trackgod.app.feature.workout.session.ConfirmationDialog
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
import com.trackgod.app.ui.theme.TrackGodTheme
import com.trackgod.app.ui.theme.Void
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.app.Activity

@Composable
fun BackupScreen(
    onNavigateBack: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backups by viewModel.backups.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val spacing = TrackGodTheme.spacing

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

    // -- Share intent for CSV export -------------------------------------------
    LaunchedEffect(uiState.csvExportUri) {
        uiState.csvExportUri?.let { uri ->
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "EXPORT CSV"))
            viewModel.clearCsvExportUri()
        }
    }

    // -- Confirm dialog state -------------------------------------------------
    var confirmRestore by remember { mutableStateOf<BackupMetadataEntity?>(null) }
    var confirmDelete by remember { mutableStateOf<BackupMetadataEntity?>(null) }
    var confirmDeleteAll by remember { mutableStateOf(false) }

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
                    // Restart the app properly via activity recreation + process kill
                    val activity = context as? Activity
                    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    context.startActivity(intent)
                    activity?.finish()
                }) {
                    Text("RESTART", color = Blood)
                }
            },
            containerColor = Void,
        )
    }

    // -- Restore confirm dialog -----------------------------------------------
    confirmRestore?.let { backup ->
        ConfirmationDialog(
            title = "RESTORE FROM BACKUP?",
            message = "CURRENT DATA WILL BE REPLACED WITH THIS BACKUP. A SAFETY BACKUP WILL BE CREATED FIRST.",
            confirmText = "RESTORE",
            dismissText = "CANCEL",
            onConfirm = {
                viewModel.restoreFromBackup(backup)
                confirmRestore = null
            },
            onDismiss = { confirmRestore = null },
        )
    }

    // -- Delete confirm dialog ------------------------------------------------
    confirmDelete?.let { backup ->
        ConfirmationDialog(
            title = "DELETE BACKUP?",
            message = "THIS BACKUP WILL BE PERMANENTLY DELETED.",
            confirmText = "DELETE",
            dismissText = "CANCEL",
            onConfirm = {
                viewModel.deleteBackup(backup)
                confirmDelete = null
            },
            onDismiss = { confirmDelete = null },
        )
    }

    // -- Delete All confirm dialog --------------------------------------------
    if (confirmDeleteAll) {
        ConfirmationDialog(
            title = "DELETE ALL DATA?",
            message = "ALL WORKOUT DATA, PROGRESS PHOTOS, AND SETTINGS WILL BE PERMANENTLY DELETED. A SAFETY BACKUP WILL BE CREATED FIRST. THIS CANNOT BE UNDONE.",
            confirmText = "DELETE EVERYTHING",
            dismissText = "CANCEL",
            onConfirm = {
                viewModel.deleteAllData()
                confirmDeleteAll = false
            },
            onDismiss = { confirmDeleteAll = false },
        )
    }

    // -- Layout ---------------------------------------------------------------
    MetalTextureBackground {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = spacing.xs),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.xs, vertical = spacing.sm),
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
                    .padding(horizontal = spacing.screenPadding, vertical = spacing.sm),
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = spacing.screenPadding),
        ) {
            // ── STATUS ──────────────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(spacing.sm))
                SectionDivider(text = "STATUS", modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(spacing.md))
            }

            item {
                StatusDashboard(stats = uiState.stats)
                Spacer(modifier = Modifier.height(spacing.xl))
            }

            // ── ACTIONS ─────────────────────────────────────────────────────
            item {
                SectionDivider(text = "ACTIONS", modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(spacing.md))
            }

            item {
                TrackGodButton(
                    text = "CREATE BACKUP",
                    onClick = { viewModel.createBackup() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                )
                Spacer(modifier = Modifier.height(spacing.sm))
            }

            item {
                TrackGodButton(
                    text = "EXPORT DATABASE",
                    onClick = { viewModel.exportDatabase() },
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Secondary,
                    enabled = !uiState.isLoading,
                )
                Spacer(modifier = Modifier.height(spacing.sm))
            }

            item {
                TrackGodButton(
                    text = "IMPORT DATABASE",
                    onClick = { importLauncher.launch(arrayOf("*/*")) },
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Secondary,
                    enabled = !uiState.isLoading,
                )
                Spacer(modifier = Modifier.height(spacing.sm))
            }

            item {
                TrackGodButton(
                    text = "EXPORT CSV",
                    onClick = { viewModel.exportCsv() },
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Secondary,
                    enabled = !uiState.isLoading,
                )
                Spacer(modifier = Modifier.height(spacing.sm))
            }

            item {
                TrackGodButton(
                    text = "DELETE ALL DATA",
                    onClick = { confirmDeleteAll = true },
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Secondary,
                    enabled = !uiState.isLoading,
                    textColorOverride = Blood,
                )
                Spacer(modifier = Modifier.height(spacing.xl))
            }

            // ── BACKUP HISTORY ──────────────────────────────────────────────
            item {
                SectionDivider(text = "BACKUP HISTORY", modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(spacing.md))
            }

            if (backups.isEmpty()) {
                item {
                    Text(
                        text = "NO BACKUPS YET",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextTertiary,
                        modifier = Modifier.padding(vertical = spacing.lg),
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
                Spacer(modifier = Modifier.height(spacing.sm))
            }

            item { Spacer(modifier = Modifier.height(spacing.xxl)) }
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
    val spacing = TrackGodTheme.spacing
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
            letterSpacing = 2.sp,
        )
        Spacer(modifier = Modifier.height(spacing.xs))
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
    val spacing = TrackGodTheme.spacing
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
                Spacer(modifier = Modifier.height(spacing.xs))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // File size
                    Text(
                        text = formatFileSize(backup.fileSize),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextTertiary,
                    )
                    Spacer(modifier = Modifier.width(spacing.sm))
                    // Type badge
                    TypeBadge(type = backup.backupType)
                }
            }
        }

        Spacer(modifier = Modifier.height(spacing.sm))

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
