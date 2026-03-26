package com.trackgod.app.feature.onboarding

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Bolt
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trackgod.app.ui.component.ButtonVariant
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.trackgod.app.R
import com.trackgod.app.ui.theme.Void
import com.trackgod.app.ui.component.NumberInput
import com.trackgod.app.ui.component.TrackGodButton
import com.trackgod.app.ui.component.TrackGodTextField
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.BloodGlow
import com.trackgod.app.ui.theme.SurfaceHighest
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextSecondary
import com.trackgod.app.ui.theme.TextTertiary

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Void),
    ) {
        Image(
            painter = painterResource(R.drawable.onboarding_bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.25f),
            contentScale = ContentScale.Crop,
        )
    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (state.currentStep > 0) {
                IconButton(onClick = { viewModel.previousStep() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextTertiary,
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "TRACKGOD",
                style = MaterialTheme.typography.labelLarge,
                color = TextTertiary,
                letterSpacing = 4.sp,
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.size(24.dp))
        }

        // ── Phase label + Heading ───────────────────────────────────────────
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            Text(
                text = "ONBOARDING // STEP %02d".format(state.currentStep + 1),
                style = MaterialTheme.typography.labelLarge,
                color = BloodBright,
                letterSpacing = 3.sp,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = buildAnnotatedString {
                    append("FORGE YOUR\n")
                    withStyle(SpanStyle(color = Blood)) {
                        append("PROFILE")
                    }
                },
                style = MaterialTheme.typography.displaySmall,
                color = TextPrimary,
                lineHeight = 34.sp,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Progress bar ────────────────────────────────────────────────
            val progress = (state.currentStep + 1).toFloat() / state.totalSteps
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(SurfaceHighest, RectangleShape),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(Blood, RectangleShape),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Step content ────────────────────────────────────────────────────
        AnimatedContent(
            targetState = state.currentStep,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                } else {
                    slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                }
            },
            modifier = Modifier.weight(1f),
            label = "stepContent",
        ) { step ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
            ) {
                when (step) {
                    0 -> StepNameAvatar(state, viewModel)
                    1 -> StepGender(state, viewModel)
                    2 -> StepHeightWeight(state, viewModel)
                    3 -> StepUnits(state, viewModel)
                    4 -> StepObjective(state, viewModel)
                    5 -> StepExperience(state, viewModel)
                }
            }
        }

        // ── Bottom CTA ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            val isLastStep = state.currentStep == state.totalSteps - 1
            val buttonText = when {
                state.isSaving -> "SAVING..."
                isLastStep -> "INITIATE PROTOCOL"
                else -> "NEXT PROTOCOL >>"
            }

            TrackGodButton(
                text = buttonText,
                onClick = {
                    if (isLastStep) {
                        viewModel.saveProfile(onComplete = onOnboardingComplete)
                    } else {
                        viewModel.nextStep()
                    }
                },
                enabled = state.canProceed && !state.isSaving,
                icon = if (!isLastStep) Icons.AutoMirrored.Filled.KeyboardArrowRight else null,
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.error!!,
                    style = MaterialTheme.typography.labelMedium,
                    color = BloodBright,
                )
            }
        }
    }
    } // Box (onboarding background)
}

// ═══════════════════════════════════════════════════════════════════════════════
// Step 0: Name + Avatar
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun StepNameAvatar(
    state: OnboardingState,
    viewModel: OnboardingViewModel,
) {
    val context = LocalContext.current

    var pendingCropUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val launchPicker = com.trackgod.app.core.util.rememberAvatarPickerLauncher { uri ->
        pendingCropUri = uri
    }

    // Interactive crop overlay (shown above the onboarding content)
    pendingCropUri?.let { uri ->
        com.trackgod.app.core.util.ImageCropOverlay(
            sourceUri = uri,
            onConfirm = { croppedUri ->
                viewModel.updateAvatarUri(croppedUri.toString())
                pendingCropUri = null
            },
            onCancel = { pendingCropUri = null },
        )
        return  // don't render step content while cropping
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Avatar picker
    Box(
        modifier = Modifier
            .size(80.dp)
            .background(SurfaceLow, RectangleShape)
            .clip(RectangleShape)
            .clickable { launchPicker() },
        contentAlignment = Alignment.Center,
    ) {
        if (!state.avatarUri.isNullOrBlank()) {
            val context = LocalContext.current
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(state.avatarUri)
                    .crossfade(true)
                    .error(android.R.drawable.ic_menu_gallery)
                    .build(),
                contentDescription = "Avatar",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Set avatar",
                tint = TextTertiary,
                modifier = Modifier.size(32.dp),
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = if (state.avatarUri.isNullOrBlank()) "TAP TO SET AVATAR" else "TAP TO CHANGE",
        style = MaterialTheme.typography.labelMedium,
        color = TextTertiary,
        letterSpacing = 2.sp,
    )

    Spacer(modifier = Modifier.height(32.dp))

    TrackGodTextField(
        value = state.name,
        onValueChange = viewModel::updateName,
        label = "NAME",
        placeholder = "ENTER IDENTITY",
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "The altar recognizes its own.",
        style = MaterialTheme.typography.bodyMedium,
        color = TextTertiary,
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// Step 1: Gender
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun StepGender(
    state: OnboardingState,
    viewModel: OnboardingViewModel,
) {
    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "SELECT DESIGNATION",
        style = MaterialTheme.typography.labelLarge,
        color = TextTertiary,
        letterSpacing = 3.sp,
    )

    Spacer(modifier = Modifier.height(24.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GenderCard(
            label = "MALE",
            isSelected = state.gender == "male",
            onClick = { viewModel.updateGender("male") },
            modifier = Modifier.weight(1f),
        )
        GenderCard(
            label = "FEMALE",
            isSelected = state.gender == "female",
            onClick = { viewModel.updateGender("female") },
            modifier = Modifier.weight(1f),
        )
        GenderCard(
            label = "OTHER",
            isSelected = state.gender == "other",
            onClick = { viewModel.updateGender("other") },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun GenderCard(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(88.dp)
            .background(
                color = if (isSelected) Blood else SurfaceLow,
                shape = RectangleShape,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Step 2: Height + Weight
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun StepHeightWeight(
    state: OnboardingState,
    viewModel: OnboardingViewModel,
) {
    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "BODY METRICS",
        style = MaterialTheme.typography.labelLarge,
        color = TextTertiary,
        letterSpacing = 3.sp,
    )

    Spacer(modifier = Modifier.height(32.dp))

    NumberInput(
        value = state.height,
        onValueChange = viewModel::updateHeight,
        label = "HEIGHT",
        unit = state.heightUnit.uppercase(),
        step = 1f,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(32.dp))

    NumberInput(
        value = state.weight,
        onValueChange = viewModel::updateWeight,
        label = "WEIGHT",
        unit = state.weightUnit.uppercase(),
        step = 1f,
        modifier = Modifier.fillMaxWidth(),
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// Step 3: Units
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun StepUnits(
    state: OnboardingState,
    viewModel: OnboardingViewModel,
) {
    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "MEASUREMENT PROTOCOL",
        style = MaterialTheme.typography.labelLarge,
        color = TextTertiary,
        letterSpacing = 3.sp,
    )

    Spacer(modifier = Modifier.height(32.dp))

    Text(
        text = "WEIGHT UNIT",
        style = MaterialTheme.typography.labelMedium,
        color = TextTertiary,
        letterSpacing = 3.sp,
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        UnitChip(
            label = "KG",
            isSelected = state.weightUnit == "kg",
            onClick = { viewModel.updateWeightUnit("kg") },
        )
        UnitChip(
            label = "LBS",
            isSelected = state.weightUnit == "lbs",
            onClick = { viewModel.updateWeightUnit("lbs") },
        )
    }

    Spacer(modifier = Modifier.height(32.dp))

    Text(
        text = "HEIGHT UNIT",
        style = MaterialTheme.typography.labelMedium,
        color = TextTertiary,
        letterSpacing = 3.sp,
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        UnitChip(
            label = "CM",
            isSelected = state.heightUnit == "cm",
            onClick = { viewModel.updateHeightUnit("cm") },
        )
        UnitChip(
            label = "FT",
            isSelected = state.heightUnit == "ft",
            onClick = { viewModel.updateHeightUnit("ft") },
        )
    }
}

@Composable
private fun UnitChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .height(44.dp)
            .width(80.dp)
            .background(
                color = if (isSelected) Blood else SurfaceHighest,
                shape = RectangleShape,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Black,
            letterSpacing = 3.sp,
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Step 4: Objective
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun StepObjective(
    state: OnboardingState,
    viewModel: OnboardingViewModel,
) {
    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "PRIMARY DIRECTIVE",
        style = MaterialTheme.typography.labelLarge,
        color = TextTertiary,
        letterSpacing = 3.sp,
    )

    Spacer(modifier = Modifier.height(24.dp))

    ObjectiveCard(
        title = "LOSE WEIGHT",
        subtitle = "Strategic Fat Reduction",
        icon = Icons.Default.KeyboardArrowDown,
        isSelected = state.primaryObjective == "lose_weight",
        onClick = { viewModel.updatePrimaryObjective("lose_weight") },
    )

    Spacer(modifier = Modifier.height(12.dp))

    ObjectiveCard(
        title = "GET FIT",
        subtitle = "Endurance & Performance",
        icon = Icons.Default.Bolt,
        isSelected = state.primaryObjective == "get_fit",
        onClick = { viewModel.updatePrimaryObjective("get_fit") },
    )

    Spacer(modifier = Modifier.height(12.dp))

    ObjectiveCard(
        title = "GAIN MUSCLE",
        subtitle = "Hypertrophy Protocol",
        icon = Icons.Default.FitnessCenter,
        isSelected = state.primaryObjective == "gain_muscle",
        onClick = { viewModel.updatePrimaryObjective("gain_muscle") },
    )
}

@Composable
private fun ObjectiveCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(
                color = if (isSelected) Blood else SurfaceLow,
                shape = RectangleShape,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) TextPrimary else TextTertiary,
            modifier = Modifier.size(24.dp),
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) TextSecondary else TextTertiary,
            )
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Step 5: Experience + Weekly Target
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun StepExperience(
    state: OnboardingState,
    viewModel: OnboardingViewModel,
) {
    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "TRAINING PROTOCOL",
        style = MaterialTheme.typography.labelLarge,
        color = TextTertiary,
        letterSpacing = 3.sp,
    )

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = "EXPERIENCE LEVEL",
        style = MaterialTheme.typography.labelMedium,
        color = TextTertiary,
        letterSpacing = 3.sp,
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("beginner", "intermediate", "advanced").forEach { level ->
            ExperienceChip(
                label = level.uppercase(),
                isSelected = state.experienceLevel == level,
                onClick = { viewModel.updateExperienceLevel(level) },
            )
        }
    }

    Spacer(modifier = Modifier.height(40.dp))

    NumberInput(
        value = state.weeklyTarget.toString(),
        onValueChange = { value ->
            value.toIntOrNull()?.let { viewModel.updateWeeklyTarget(it) }
        },
        label = "WEEKLY TARGET",
        unit = "DAYS/WEEK",
        step = 1f,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = "How many days per week do you train?",
        style = MaterialTheme.typography.bodyMedium,
        color = TextTertiary,
    )
}

@Composable
private fun ExperienceChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .height(40.dp)
            .background(
                color = if (isSelected) Blood else SurfaceHighest,
                shape = RectangleShape,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
            letterSpacing = 2.sp,
        )
    }
}
