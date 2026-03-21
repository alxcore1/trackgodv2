package com.trackgod.app.feature.profile

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.trackgod.app.ui.component.ButtonVariant
import com.trackgod.app.ui.component.SectionDivider
import com.trackgod.app.ui.component.TrackGodButton
import com.trackgod.app.ui.component.TrackGodTextField
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.SurfaceHigh
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let {
            // Take persistent permission so the URI survives process death
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            viewModel.onAvatarUriChanged(it.toString())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                text = "EDIT PROFILE",
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
            Spacer(modifier = Modifier.height(16.dp))

            // -- Avatar --
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.avatarUri.isNotBlank()) {
                    AsyncImage(
                        model = state.avatarUri,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    val initials = state.name
                        .split(" ")
                        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                        .take(2)
                        .joinToString("")
                        .ifEmpty { "?" }

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Blood),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                TrackGodButton(
                    text = "CHANGE PHOTO",
                    onClick = { imagePickerLauncher.launch("image/*") },
                    variant = ButtonVariant.Secondary,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // -- Name --
            TrackGodTextField(
                value = state.name,
                onValueChange = viewModel::onNameChanged,
                label = "NAME",
                placeholder = "Your name",
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.nameError) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "NAME IS REQUIRED",
                    style = MaterialTheme.typography.labelMedium,
                    color = BloodBright,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // -- Gender --
            SectionDivider(text = "GENDER", modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipSelect(
                    label = "MALE",
                    selected = state.gender == "male",
                    onClick = { viewModel.onGenderChanged("male") },
                    modifier = Modifier.weight(1f),
                )
                ChipSelect(
                    label = "FEMALE",
                    selected = state.gender == "female",
                    onClick = { viewModel.onGenderChanged("female") },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // -- Birthday --
            SectionDivider(text = "BIRTHDAY", modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceLow)
                    .clickable {
                        val cal = Calendar.getInstance()
                        if (state.birthday.isNotBlank()) {
                            runCatching {
                                val parts = state.birthday.split("-")
                                cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                            }
                        }
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                viewModel.onBirthdayChanged(
                                    "%04d-%02d-%02d".format(year, month + 1, day)
                                )
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH),
                        ).show()
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Text(
                    text = state.birthday.ifBlank { "TAP TO SELECT" },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (state.birthday.isBlank()) TextTertiary else TextPrimary,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // -- Height --
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                TrackGodTextField(
                    value = state.height,
                    onValueChange = viewModel::onHeightChanged,
                    label = "HEIGHT",
                    placeholder = "0",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = state.heightUnit.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextTertiary,
                    modifier = Modifier.padding(bottom = 14.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // -- Weight --
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                TrackGodTextField(
                    value = state.weight,
                    onValueChange = viewModel::onWeightChanged,
                    label = "WEIGHT",
                    placeholder = "0",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = state.weightUnit.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextTertiary,
                    modifier = Modifier.padding(bottom = 14.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // -- Primary Objective --
            SectionDivider(text = "PRIMARY OBJECTIVE", modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("Lose Weight", "Get Fit", "Gain Muscle").forEach { objective ->
                    ChipSelect(
                        label = objective.uppercase(),
                        selected = state.primaryObjective.equals(objective, ignoreCase = true),
                        onClick = { viewModel.onPrimaryObjectiveChanged(objective) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // -- Experience Level --
            SectionDivider(text = "EXPERIENCE LEVEL", modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("Beginner", "Intermediate", "Advanced").forEach { level ->
                    ChipSelect(
                        label = level.uppercase(),
                        selected = state.experienceLevel.equals(level, ignoreCase = true),
                        onClick = { viewModel.onExperienceLevelChanged(level) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // -- Weekly Target --
            TrackGodTextField(
                value = state.weeklyTarget,
                onValueChange = viewModel::onWeeklyTargetChanged,
                label = "WEEKLY TARGET (DAYS/WEEK)",
                placeholder = "4",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // -- Units --
            SectionDivider(text = "UNITS", modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "WEIGHT",
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipSelect(
                    label = "KG",
                    selected = state.weightUnit == "kg",
                    onClick = { viewModel.onWeightUnitChanged("kg") },
                    modifier = Modifier.weight(1f),
                )
                ChipSelect(
                    label = "LBS",
                    selected = state.weightUnit == "lbs",
                    onClick = { viewModel.onWeightUnitChanged("lbs") },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "HEIGHT",
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipSelect(
                    label = "CM",
                    selected = state.heightUnit == "cm",
                    onClick = { viewModel.onHeightUnitChanged("cm") },
                    modifier = Modifier.weight(1f),
                )
                ChipSelect(
                    label = "FT",
                    selected = state.heightUnit == "ft",
                    onClick = { viewModel.onHeightUnitChanged("ft") },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // -- Save / Cancel --
            TrackGodButton(
                text = "SAVE CHANGES",
                onClick = { viewModel.save(onNavigateBack) },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            TrackGodButton(
                text = "CANCEL",
                onClick = onNavigateBack,
                variant = ButtonVariant.Ghost,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// -- Chip Select --------------------------------------------------------------

@Composable
internal fun ChipSelect(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) Blood else SurfaceLow,
        animationSpec = tween(durationMillis = 150),
        label = "chipBg",
    )

    Box(
        modifier = modifier
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) TextPrimary else TextTertiary,
        )
    }
}
