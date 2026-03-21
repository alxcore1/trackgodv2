package com.trackgod.app.feature.altar

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trackgod.app.core.database.entity.WorkoutEntity
import com.trackgod.app.ui.component.ButtonVariant
import com.trackgod.app.ui.component.SectionDivider
import com.trackgod.app.ui.component.StatCard
import com.trackgod.app.ui.component.TrackGodButton
import com.trackgod.app.ui.component.TrackGodCard
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextSecondary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.TrackGodTheme
import com.trackgod.app.ui.theme.Void
import kotlinx.coroutines.launch

@Composable
fun AltarScreen(
    onStartWorkout: (workoutId: Long) -> Unit = {},
    onResumeWorkout: (workoutId: Long) -> Unit = {},
    viewModel: AltarViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    AltarContent(
        state = state,
        onStartNewWorkout = {
            scope.launch {
                val workoutId = viewModel.startNewWorkout()
                onStartWorkout(workoutId)
            }
        },
        onResumeWorkout = {
            val id = viewModel.resumeWorkout()
            if (id != null) onResumeWorkout(id)
        },
        onDiscardIncomplete = viewModel::discardIncompleteWorkout,
    )
}

// ── Content (stateless, previewable) ────────────────────────────────────────

@Composable
private fun AltarContent(
    state: AltarState,
    onStartNewWorkout: () -> Unit,
    onResumeWorkout: () -> Unit,
    onDiscardIncomplete: () -> Unit,
) {
    var showDiscardConfirm by remember { mutableStateOf(false) }

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Void),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = Blood)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .verticalScroll(rememberScrollState()),
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "TRACKGOD",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Incomplete workout banner ───────────────────────────────────────
        if (state.hasIncompleteWorkout) {
            TrackGodCard(
                accentBorder = true,
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Text(
                    text = "UNFINISHED RITUAL",
                    style = MaterialTheme.typography.labelLarge,
                    color = BloodBright,
                    letterSpacing = 2.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "You left a workout in progress.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (showDiscardConfirm) {
                    Text(
                        text = "DISCARD THIS WORKOUT?",
                        style = MaterialTheme.typography.labelMedium,
                        color = BloodBright,
                        letterSpacing = 2.sp,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        TrackGodButton(
                            text = "KEEP",
                            onClick = { showDiscardConfirm = false },
                            variant = ButtonVariant.Secondary,
                            modifier = Modifier.weight(1f),
                        )
                        TrackGodButton(
                            text = "DISCARD",
                            onClick = {
                                showDiscardConfirm = false
                                onDiscardIncomplete()
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        TrackGodButton(
                            text = "DISCARD",
                            onClick = { showDiscardConfirm = true },
                            variant = ButtonVariant.Ghost,
                            modifier = Modifier.weight(1f),
                        )
                        TrackGodButton(
                            text = "RESUME",
                            onClick = onResumeWorkout,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Goal card + Start CTA ───────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Weekly ritual card
            TrackGodCard(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "WEEKLY RITUAL",
                    color = TextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "TODAY",
                    color = TextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${state.todayWorkoutCount} SESSION${if (state.todayWorkoutCount != 1) "S" else ""}",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                )
            }

            // Start new workout CTA
            TrackGodButton(
                text = "START\nNEW",
                onClick = onStartNewWorkout,
                icon = Icons.Default.Add,
                modifier = Modifier
                    .weight(0.6f)
                    .height(100.dp),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── 2x2 stat grid ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                icon = Icons.Default.LocalFireDepartment,
                label = "STREAK",
                value = state.currentStreak.toString(),
                unit = "DAYS",
                modifier = Modifier.weight(1f),
            )
            StatCard(
                icon = Icons.Default.FitnessCenter,
                label = "VOLUME",
                value = formatVolume(state.todayVolume),
                unit = if (state.todayVolume >= 1000) "TONS" else "KG",
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                icon = Icons.Default.Whatshot,
                label = "SETS",
                value = state.todaySets.toString(),
                unit = "TODAY",
                modifier = Modifier.weight(1f),
            )
            StatCard(
                icon = Icons.Default.AccessTime,
                label = "DURATION",
                value = state.todayDurationMinutes.toString(),
                unit = "MIN",
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Past transmissions ──────────────────────────────────────────────
        SectionDivider(
            text = "PAST TRANSMISSIONS",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (state.recentWorkouts.isEmpty()) {
            Text(
                text = "NO COMPLETED RITUALS YET.",
                color = TextTertiary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
            )
        } else {
            state.recentWorkouts.forEach { workout ->
                RecentWorkoutRow(workout = workout)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ── Recent Workout Row ──────────────────────────────────────────────────────

@Composable
private fun RecentWorkoutRow(workout: WorkoutEntity) {
    TrackGodCard(
        accentBorder = true,
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        Text(
            text = workout.name.uppercase().ifBlank { "UNTITLED WORKOUT" },
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = workout.date,
                color = TextTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            )
            if (workout.totalVolume != null && workout.totalVolume > 0) {
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .background(Blood),
                )
                Text(
                    text = "${formatVolume(workout.totalVolume)}KG",
                    color = TextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
            }
            if (workout.durationSeconds != null) {
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .background(Blood),
                )
                Text(
                    text = "${workout.durationSeconds / 60}MIN",
                    color = TextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
            }
        }
    }
}

// ── Formatting helpers ──────────────────────────────────────────────────────

private fun formatVolume(volume: Float): String {
    return when {
        volume >= 1_000_000 -> "%.1f".format(volume / 1_000_000f)
        volume >= 1_000 -> "%.1f".format(volume / 1_000f)
        else -> "%.0f".format(volume)
    }
}

// ── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun AltarScreenPreview() {
    TrackGodTheme {
        AltarContent(
            state = AltarState(
                todayWorkoutCount = 1,
                todaySets = 24,
                todayVolume = 14_200f,
                todayDurationMinutes = 72,
                currentStreak = 12,
                hasIncompleteWorkout = false,
                recentWorkouts = listOf(
                    WorkoutEntity(
                        id = 1,
                        name = "Heavy Back & Traps",
                        date = "2024-04-12",
                        startTime = 0,
                        totalVolume = 14_500f,
                        durationSeconds = 4320,
                        isCompleted = true,
                        createdAt = 0,
                    ),
                    WorkoutEntity(
                        id = 2,
                        name = "Deadlift Ritual",
                        date = "2024-04-10",
                        startTime = 0,
                        totalVolume = 12_200f,
                        durationSeconds = 3600,
                        isCompleted = true,
                        createdAt = 0,
                    ),
                ),
                isLoading = false,
            ),
            onStartNewWorkout = {},
            onResumeWorkout = {},
            onDiscardIncomplete = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun AltarScreenIncompletePreview() {
    TrackGodTheme {
        AltarContent(
            state = AltarState(
                hasIncompleteWorkout = true,
                incompleteWorkoutId = 42,
                isLoading = false,
            ),
            onStartNewWorkout = {},
            onResumeWorkout = {},
            onDiscardIncomplete = {},
        )
    }
}
