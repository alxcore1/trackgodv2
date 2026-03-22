package com.trackgod.app.feature.weightloss

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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trackgod.app.core.database.entity.WeightLossMilestoneEntity
import com.trackgod.app.ui.component.ButtonVariant
import com.trackgod.app.ui.component.MetalTextureBackground
import com.trackgod.app.ui.component.SectionDivider
import com.trackgod.app.ui.component.TrackGodButton
import com.trackgod.app.ui.component.TrackGodCard
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.SurfaceHighest
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun WeightLossScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPhotoComparison: () -> Unit = {},
    viewModel: WeightLossViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MetalTextureBackground {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 4.dp),
    ) {
        // -- Top bar --
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
                text = "WEIGHT LOSS JOURNEY",
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

            if (state.activeGoal == null && !state.isLoading) {
                // No active goal -- show setup CTA
                NoGoalSection(onSetGoal = viewModel::showGoalSetup)
            } else if (state.activeGoal != null) {
                // -- ACTIVE GOAL --
                SectionDivider(text = "ACTIVE GOAL", modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                GoalOverviewCard(state = state)

                Spacer(modifier = Modifier.height(24.dp))

                // -- WEIGHT PROGRESS --
                SectionDivider(text = "WEIGHT PROGRESS", modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                WeightProgressChart(
                    weightHistory = state.weightHistory,
                    targetWeight = state.activeGoal?.targetWeight,
                    weightUnit = state.weightUnit,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // -- QUICK ACTIONS --
                SectionDivider(text = "QUICK ACTIONS", modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TrackGodButton(
                        text = "LOG WEIGH-IN",
                        onClick = viewModel::showWeighIn,
                        modifier = Modifier.weight(1f),
                    )
                    TrackGodButton(
                        text = "ADD MILESTONE",
                        onClick = viewModel::showMilestone,
                        variant = ButtonVariant.Secondary,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // -- MILESTONES --
                SectionDivider(text = "MILESTONES", modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                if (state.milestones.isEmpty()) {
                    Text(
                        text = "NO MILESTONES SET",
                        color = TextTertiary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center,
                    )
                } else {
                    state.milestones.forEach { milestone ->
                        MilestoneRow(
                            milestone = milestone,
                            weightUnit = state.weightUnit,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // -- REFERENCE (BMR/TDEE) --
                if (state.bmr != null || state.tdee != null) {
                    SectionDivider(text = "REFERENCE", modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(12.dp))
                    ReferenceCards(bmr = state.bmr, tdee = state.tdee)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // -- PROGRESS PHOTOS --
                SectionDivider(text = "PROGRESS PHOTOS", modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                ProgressPhotosSection(
                    photos = state.progressPhotos,
                    onPhotoAdded = viewModel::addProgressPhoto,
                    onCompare = onNavigateToPhotoComparison,
                    onPhotoDeleted = viewModel::deletePhoto,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    } // MetalTextureBackground

    // -- Bottom Sheets --

    if (state.showGoalSetup) {
        GoalSetupSheet(
            currentWeight = state.currentWeight
                ?: state.activeGoal?.startingWeight,
            weightUnit = state.weightUnit,
            onSave = viewModel::saveGoal,
            onDismiss = viewModel::dismissGoalSetup,
        )
    }

    if (state.showWeighIn) {
        WeighInSheet(
            lastWeight = state.currentWeight,
            weightUnit = state.weightUnit,
            onLog = viewModel::logWeighIn,
            onDismiss = viewModel::dismissWeighIn,
        )
    }

    if (state.showMilestone && state.activeGoal != null) {
        MilestoneSheet(
            startingWeight = state.activeGoal!!.startingWeight,
            targetWeight = state.activeGoal!!.targetWeight,
            weightUnit = state.weightUnit,
            onSave = viewModel::saveMilestone,
            onDismiss = viewModel::dismissMilestone,
        )
    }
}

// -- No Goal Section ----------------------------------------------------------

@Composable
private fun NoGoalSection(onSetGoal: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "NO ACTIVE GOAL",
            style = MaterialTheme.typography.headlineMedium,
            color = TextTertiary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "SET A WEIGHT LOSS GOAL TO START TRACKING YOUR JOURNEY",
            style = MaterialTheme.typography.labelMedium,
            color = TextTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        TrackGodButton(
            text = "SET YOUR GOAL",
            onClick = onSetGoal,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// -- Goal Overview Card -------------------------------------------------------

@Composable
private fun GoalOverviewCard(state: WeightLossState) {
    val goal = state.activeGoal ?: return
    val unit = state.weightUnit.uppercase()

    TrackGodCard(accentBorder = true) {
        // Starting / Target row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            StatColumn(label = "STARTING", value = "%.1f %s".format(goal.startingWeight, unit))
            StatColumn(label = "TARGET", value = "%.1f %s".format(goal.targetWeight, unit))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress percentage (large)
        Text(
            text = "%.0f%%".format(state.progressPercent),
            color = BloodBright,
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        Text(
            text = "PROGRESS",
            color = TextTertiary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Remaining / Days left row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            StatColumn(label = "REMAINING", value = "%.1f %s".format(state.weightRemaining, unit))
            StatColumn(label = "DAYS LEFT", value = "${state.daysRemaining}")
        }

        // Motivation text
        if (!goal.motivationText.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "\"${goal.motivationText.uppercase()}\"",
                color = TextTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column {
        Text(
            text = label,
            color = TextTertiary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
        )
    }
}

// -- Milestone Row ------------------------------------------------------------

@Composable
private fun MilestoneRow(
    milestone: WeightLossMilestoneEntity,
    weightUnit: String,
) {
    TrackGodCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Status indicator
            Text(
                text = if (milestone.isAchieved) "\u2713" else "\u25CB",
                color = if (milestone.isAchieved) Blood else SurfaceHighest,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "%.1f %s".format(milestone.targetWeight, weightUnit.uppercase()),
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                )
                if (!milestone.description.isNullOrBlank()) {
                    Text(
                        text = milestone.description.uppercase(),
                        color = TextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                    )
                }
            }

            Text(
                text = if (milestone.isAchieved) "DONE" else "PENDING",
                color = if (milestone.isAchieved) BloodBright else TextTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
            )
        }
    }
}

// -- Reference Cards (BMR / TDEE) ---------------------------------------------

@Composable
private fun ReferenceCards(bmr: Float?, tdee: Float?) {
    val numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault())

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (bmr != null) {
            TrackGodCard(modifier = Modifier.weight(1f)) {
                Text(
                    text = "BMR",
                    color = TextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${numberFormat.format(bmr.toInt())} KCAL",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }

        if (tdee != null) {
            TrackGodCard(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TDEE",
                    color = TextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${numberFormat.format(tdee.toInt())} KCAL",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}
