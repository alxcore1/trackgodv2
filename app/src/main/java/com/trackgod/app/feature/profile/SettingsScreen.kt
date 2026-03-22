package com.trackgod.app.feature.profile

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
import com.trackgod.app.ui.theme.Void

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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
            .padding(top = 4.dp),
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
                text = "SETTINGS",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── WORKOUT ─────────────────────────────────────────────────────
            SectionDivider(text = "WORKOUT", modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))

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
            SettingToggleRow(
                label = "SHOW RPE",
                checked = state.showRpe,
                onCheckedChange = viewModel::setShowRpe,
            )
            SettingToggleRow(
                label = "SHOW RIR",
                checked = state.showRir,
                onCheckedChange = viewModel::setShowRir,
            )
            SettingValueRow(
                label = "DEFAULT WEIGHT INCREMENT",
                value = "${state.defaultWeightIncrement}",
                onClick = { showIncrementDialog = true },
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── DISPLAY ─────────────────────────────────────────────────────
            SectionDivider(text = "DISPLAY", modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(24.dp))

            // ── NOTIFICATIONS ───────────────────────────────────────────────
            SectionDivider(text = "NOTIFICATIONS", modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(24.dp))

            // ── DATA ────────────────────────────────────────────────────────
            SectionDivider(text = "DATA", modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(32.dp))
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
                value.toFloatOrNull()?.coerceIn(0.5f, 20f)?.let { viewModel.setDefaultWeightIncrement(it) }
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(SurfaceLow)
            .padding(horizontal = 16.dp),
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
    Spacer(modifier = Modifier.height(2.dp))
}

@Composable
private fun SettingValueRow(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(SurfaceLow)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
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
    Spacer(modifier = Modifier.height(2.dp))
}

@Composable
private fun SettingChipRow(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(SurfaceLow)
            .padding(horizontal = 16.dp),
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
    Spacer(modifier = Modifier.height(2.dp))
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
        containerColor = Void,
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
    AlertDialog(
        onDismissRequest = onDismiss,
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
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Text(
                            text = option.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (option == selected) TextPrimary else TextTertiary,
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        },
        confirmButton = {},
        containerColor = Void,
    )
}
