package com.trackgod.app.feature.profile

import com.trackgod.app.ui.theme.screenPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trackgod.app.ui.component.MetalTextureBackground
import com.trackgod.app.ui.component.SectionDivider
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.SurfaceHigh
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.TrackGodTheme

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = TrackGodTheme.spacing

    // Dialog state for number-input dialogs
    var showRestDurationDialog by remember { mutableStateOf(false) }
    var showIncrementDialog by remember { mutableStateOf(false) }
    var showMaxBackupsDialog by remember { mutableStateOf(false) }
    var showDayPickerDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

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
                text = "SETTINGS",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenPadding),
        ) {
            Spacer(modifier = Modifier.height(spacing.sm))

            // ── WORKOUT ─────────────────────────────────────────────────────
            SectionDivider(text = "WORKOUT", modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(spacing.md))

            SettingToggleRow(
                label = "REST TIMER",
                checked = state.restTimerEnabled,
                onCheckedChange = viewModel::setRestTimerEnabled,
            )
            SettingValueRow(
                label = "REST TIMER DURATION",
                value = "${state.restTimerDuration}S",
                onClick = { showRestDurationDialog = true },
            )
            SettingToggleRow(
                label = "REST TIMER AUTO-START",
                checked = state.restTimerAutoStart,
                onCheckedChange = viewModel::setRestTimerAutoStart,
            )
            SettingValueRow(
                label = "DEFAULT WEIGHT INCREMENT",
                value = "${state.defaultWeightIncrement}",
                onClick = { showIncrementDialog = true },
            )

            Spacer(modifier = Modifier.height(spacing.xl))

            // ── ADVANCED TRACKING ──────────────────────────────────────────
            SectionDivider(text = "ADVANCED TRACKING", modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(spacing.md))

            SettingToggleRowWithSubtitle(
                label = "SHOW RPE",
                subtitle = "Rate of Perceived Exertion \u2014 how hard was the set? (1-10)",
                checked = state.showRpe,
                onCheckedChange = viewModel::setShowRpe,
            )
            SettingToggleRowWithSubtitle(
                label = "SHOW RIR",
                subtitle = "Reps In Reserve \u2014 how many reps could you still do? (0-5)",
                checked = state.showRir,
                onCheckedChange = viewModel::setShowRir,
            )

            Spacer(modifier = Modifier.height(spacing.xl))

            // ── DISPLAY ─────────────────────────────────────────────────────
            SectionDivider(text = "DISPLAY", modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(spacing.md))

            SettingChipRow(
                label = "WEIGHT UNIT",
                options = listOf("KG", "LBS"),
                selected = state.weightUnit.uppercase(),
                onSelect = { viewModel.setWeightUnit(it.lowercase()) },
            )
            SettingChipRow(
                label = "HEIGHT UNIT",
                options = listOf("CM", "FT"),
                selected = state.heightUnit.uppercase(),
                onSelect = { viewModel.setHeightUnit(it.lowercase()) },
            )

            Spacer(modifier = Modifier.height(spacing.xl))

            // ── NOTIFICATIONS ───────────────────────────────────────────────
            SectionDivider(text = "NOTIFICATIONS", modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(spacing.md))

            SettingToggleRow(
                label = "REST TIMER SOUND",
                checked = state.restTimerSound,
                onCheckedChange = viewModel::setRestTimerSound,
            )
            SettingToggleRow(
                label = "WEIGH-IN REMINDER",
                checked = state.weighInReminder,
                onCheckedChange = viewModel::setWeighInReminder,
            )
            SettingValueRow(
                label = "REMINDER DAY",
                value = state.reminderDay.uppercase(),
                onClick = { showDayPickerDialog = true },
            )
            SettingValueRow(
                label = "REMINDER TIME",
                value = state.reminderTime,
                onClick = { showTimePickerDialog = true },
            )

            Spacer(modifier = Modifier.height(spacing.xl))

            // ── DATA ────────────────────────────────────────────────────────
            SectionDivider(text = "DATA", modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(spacing.md))

            SettingToggleRow(
                label = "AUTO-BACKUP",
                checked = state.autoBackup,
                onCheckedChange = viewModel::setAutoBackup,
            )
            SettingValueRow(
                label = "MAX BACKUPS",
                value = "${state.maxBackups}",
                onClick = { showMaxBackupsDialog = true },
            )

            Spacer(modifier = Modifier.height(spacing.xxl))
        }
    }
    } // MetalTextureBackground

    // ── Dialogs ─────────────────────────────────────────────────────────────

    if (showRestDurationDialog) {
        NumberInputDialog(
            title = "REST TIMER DURATION (SECONDS)",
            currentValue = state.restTimerDuration.toString(),
            onConfirm = { value ->
                value.toIntOrNull()?.coerceIn(10, 600)?.let { viewModel.setRestTimerDuration(it) }
                showRestDurationDialog = false
            },
            onDismiss = { showRestDurationDialog = false },
        )
    }

    if (showIncrementDialog) {
        NumberInputDialog(
            title = "DEFAULT WEIGHT INCREMENT",
            currentValue = state.defaultWeightIncrement.toString(),
            onConfirm = { value ->
                value.replace(",", ".").toFloatOrNull()?.coerceIn(0.5f, 20f)?.let { viewModel.setDefaultWeightIncrement(it) }
                showIncrementDialog = false
            },
            onDismiss = { showIncrementDialog = false },
        )
    }

    if (showMaxBackupsDialog) {
        NumberInputDialog(
            title = "MAX BACKUPS",
            currentValue = state.maxBackups.toString(),
            onConfirm = { value ->
                value.toIntOrNull()?.coerceIn(1, 50)?.let { viewModel.setMaxBackups(it) }
                showMaxBackupsDialog = false
            },
            onDismiss = { showMaxBackupsDialog = false },
        )
    }

    if (showDayPickerDialog) {
        ListPickerDialog(
            title = "REMINDER DAY",
            options = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"),
            selected = state.reminderDay,
            onSelect = { day ->
                viewModel.setReminderDay(day)
                showDayPickerDialog = false
            },
            onDismiss = { showDayPickerDialog = false },
        )
    }

    if (showTimePickerDialog) {
        NumberInputDialog(
            title = "REMINDER TIME (HH:MM)",
            currentValue = state.reminderTime,
            onConfirm = { value ->
                // Simple validation: accept HH:MM format
                if (value.matches(Regex("\\d{1,2}:\\d{2}"))) {
                    viewModel.setReminderTime(value)
                }
                showTimePickerDialog = false
            },
            onDismiss = { showTimePickerDialog = false },
        )
    }
}

// -- Setting Row Components ---------------------------------------------------

@Composable
private fun SettingToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val spacing = TrackGodTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(SurfaceLow)
            .padding(horizontal = spacing.lg),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextPrimary,
                checkedTrackColor = Blood,
                uncheckedThumbColor = TextTertiary,
                uncheckedTrackColor = SurfaceHigh,
                uncheckedBorderColor = SurfaceHigh,
            ),
        )
    }
    Spacer(modifier = Modifier.height(spacing.xs))
}

@Composable
private fun SettingToggleRowWithSubtitle(
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val spacing = TrackGodTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceLow)
            .padding(horizontal = spacing.lg, vertical = spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextPrimary,
                checkedTrackColor = Blood,
                uncheckedThumbColor = TextTertiary,
                uncheckedTrackColor = SurfaceHigh,
                uncheckedBorderColor = SurfaceHigh,
            ),
        )
    }
    Spacer(modifier = Modifier.height(spacing.xs))
}

@Composable
private fun SettingValueRow(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    val spacing = TrackGodTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(SurfaceLow)
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.lg),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = TextTertiary,
        )
    }
    Spacer(modifier = Modifier.height(spacing.xs))
}

@Composable
private fun SettingChipRow(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    val spacing = TrackGodTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(SurfaceLow)
            .padding(horizontal = spacing.lg),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            options.forEach { option ->
                ChipSelect(
                    label = option,
                    selected = option == selected,
                    onClick = { onSelect(option) },
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(spacing.xs))
}

// -- Dialogs ------------------------------------------------------------------

@Composable
private fun NumberInputDialog(
    title: String,
    currentValue: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(currentValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RectangleShape,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
        },
        text = {
            com.trackgod.app.ui.component.TrackGodTextField(
                value = text,
                onValueChange = { text = it },
                label = "",
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) {
                Text("OK", color = Blood)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = TextTertiary)
            }
        },
        containerColor = SurfaceLow,
    )
}

@Composable
private fun ListPickerDialog(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val spacing = TrackGodTheme.spacing
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RectangleShape,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
        },
        text = {
            Column {
                options.forEach { option ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (option == selected) Blood else SurfaceLow)
                            .clickable { onSelect(option) }
                            .padding(horizontal = spacing.lg, vertical = spacing.md),
                    ) {
                        Text(
                            text = option.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (option == selected) TextPrimary else TextTertiary,
                        )
                    }
                    Spacer(modifier = Modifier.height(spacing.xs))
                }
            }
        },
        confirmButton = {},
        containerColor = SurfaceLow,
    )
}
