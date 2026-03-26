package com.trackgod.app.feature.stats

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import com.trackgod.app.ui.component.ButtonVariant
import com.trackgod.app.ui.component.TrackGodButton
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trackgod.app.feature.stats.chart.ConsistencySection
import com.trackgod.app.feature.stats.chart.ExerciseFrequencySection
import com.trackgod.app.feature.stats.chart.HeatmapChart
import com.trackgod.app.feature.stats.chart.MuscleGroupChart
import com.trackgod.app.feature.stats.chart.ExerciseProgressSection
import com.trackgod.app.feature.stats.chart.PersonalRecordsSection
import com.trackgod.app.feature.stats.chart.StrengthBalanceSection
import com.trackgod.app.feature.stats.chart.VolumeChart
import com.trackgod.app.ui.component.EmptyState
import com.trackgod.app.ui.component.MetalTextureBackground
import com.trackgod.app.ui.component.SectionDivider
import com.trackgod.app.ui.component.TrackGodCard
import com.trackgod.app.ui.component.TrackGodHeader
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.SurfaceHighest
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.TrackGodTheme

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    StatsContent(
        state = state,
        onTimeRangeChanged = viewModel::onTimeRangeChanged,
    )
}

@Composable
private fun StatsContent(
    state: StatsState,
    onTimeRangeChanged: (TimeRange) -> Unit = {},
) {
    MetalTextureBackground {
    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = Blood)
        }
        return@MetalTextureBackground
    }

    if (!state.hasData) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp)
                .padding(horizontal = 16.dp),
        ) {
            TrackGodHeader()

            Spacer(modifier = Modifier.height(12.dp))

            // Hero heading even in empty state
            Text(
                text = "ARSENAL",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                maxLines = 1,
            )
            Text(
                text = "ANALYTICS",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = BloodBright,
                maxLines = 1,
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionDivider(
                text = "ARSENAL ANALYTICS",
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(48.dp))

            EmptyState(
                icon = Icons.Default.BarChart,
                title = "THE ALTAR AWAITS YOUR FIRST OFFERING",
                subtitle = "Rage. Rip. Repeat.",
            )
        }
        return@MetalTextureBackground
    }

    // Full scrollable analytics dashboard
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 4.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        TrackGodHeader()

        Spacer(modifier = Modifier.height(12.dp))

        // ── Hero Section ─────────────────────────────────────────────────────
        HeroSection(
            totalVolume = state.totalVolume,
            weightUnit = state.weightUnit,
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Time Range Filter Chips ──────────────────────────────────────────
        TimeRangeChips(
            selected = state.selectedTimeRange,
            onSelect = onTimeRangeChanged,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Tools ───────────────────────────────────────────────────────────
        var showOneRepMaxSheet by remember { mutableStateOf(false) }
        TrackGodButton(
            text = "1RM CALCULATOR",
            onClick = { showOneRepMaxSheet = true },
            variant = ButtonVariant.Secondary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
        if (showOneRepMaxSheet) {
            OneRepMaxSheet(
                weightUnit = state.weightUnit,
                onDismiss = { showOneRepMaxSheet = false },
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── 1. Volume Progression ────────────────────────────────────────────
        if (state.volumeByPeriod.isNotEmpty()) {
            TrackGodCard {
                VolumeChart(
                    data = state.volumeByPeriod,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── 2. Consistency Heatmap ───────────────────────────────────────────
        if (state.heatmapData.isNotEmpty()) {
            TrackGodCard {
                HeatmapChart(
                    data = state.heatmapData,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── 3. Personal Records ──────────────────────────────────────────────
        if (state.personalRecords.isNotEmpty()) {
            PersonalRecordsSection(
                records = state.personalRecords,
                weightUnit = state.weightUnit,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── 3b. Exercise Progression Charts ─────────────────────────────────
        if (state.exerciseProgressions.isNotEmpty()) {
            ExerciseProgressSection(
                progressions = state.exerciseProgressions,
                weightUnit = state.weightUnit,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── 4. Strength Balance ──────────────────────────────────────────────
        if (state.strengthBalance.isNotEmpty()) {
            TrackGodCard {
                StrengthBalanceSection(
                    data = state.strengthBalance,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── 5. Muscle Load Distribution ──────────────────────────────────────
        if (state.muscleGroupVolumes.isNotEmpty()) {
            TrackGodCard {
                MuscleGroupChart(
                    data = state.muscleGroupVolumes,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── 6. Exercise Frequency ────────────────────────────────────────────
        if (state.exerciseFrequency.isNotEmpty()) {
            TrackGodCard {
                ExerciseFrequencySection(
                    data = state.exerciseFrequency,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── 7. Consistency ───────────────────────────────────────────────────
        TrackGodCard {
            ConsistencySection(
                currentStreak = state.currentStreak,
                longestStreak = state.longestStreak,
                workoutsPerWeek = state.workoutsPerWeek,
            )
        }

        // Bottom padding for nav bar clearance
        Spacer(modifier = Modifier.height(32.dp))
    }
    } // MetalTextureBackground
}

// ── Hero Section ─────────────────────────────────────────────────────────────

@Composable
private fun HeroSection(
    totalVolume: Float,
    weightUnit: String,
) {
    TrackGodCard(accentBorder = true) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ARSENAL",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    maxLines = 1,
                )
                Text(
                    text = "ANALYTICS",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = BloodBright,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Performance Protocol :: Active",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextTertiary,
                )
            }

            // Total volume number
            Column(
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = formatVolume(totalVolume),
                    style = MaterialTheme.typography.displayMedium,
                    color = TextPrimary,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "TOTAL ${weightUnit.uppercase()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextTertiary,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

// ── Time Range Chips ─────────────────────────────────────────────────────────

@Composable
private fun TimeRangeChips(
    selected: TimeRange,
    onSelect: (TimeRange) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TimeRange.entries.forEach { range ->
            TimeRangeChip(
                label = range.label,
                isActive = range == selected,
                onClick = { onSelect(range) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TimeRangeChip(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor by animateColorAsState(
        targetValue = if (isActive) Blood else SurfaceHighest,
        animationSpec = tween(durationMillis = 150),
        label = "chipBg",
    )
    val textColor by animateColorAsState(
        targetValue = if (isActive) TextPrimary else TextTertiary,
        animationSpec = tween(durationMillis = 150),
        label = "chipText",
    )

    Box(
        modifier = modifier
            .background(color = bgColor, shape = RectangleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 8.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun formatVolume(volume: Float): String = when {
    volume >= 1_000_000f -> String.format(java.util.Locale.US, "%.1fM", volume / 1_000_000f)
    volume >= 1_000f -> String.format(java.util.Locale.US, "%.1fK", volume / 1_000f)
    else -> volume.toInt().toString()
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun StatsScreenEmptyPreview() {
    TrackGodTheme {
        StatsContent(
            state = StatsState(isLoading = false, hasData = false),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF131313, heightDp = 2000)
@Composable
private fun StatsScreenWithDataPreview() {
    TrackGodTheme {
        StatsContent(
            state = StatsState(
                isLoading = false,
                hasData = true,
                totalVolume = 142_800f,
                selectedTimeRange = TimeRange.MONTH,
                volumeByPeriod = listOf(
                    VolumeDataPoint("W1", 28000f, "2026-03-01"),
                    VolumeDataPoint("W2", 35000f, "2026-03-08"),
                    VolumeDataPoint("W3", 42000f, "2026-03-15"),
                    VolumeDataPoint("W4", 37800f, "2026-03-22"),
                ),
                muscleGroupVolumes = listOf(
                    MuscleGroupData("Chest", 45600f, 32f),
                    MuscleGroupData("Back", 39900f, 28f),
                    MuscleGroupData("Legs", 28560f, 20f),
                    MuscleGroupData("Arms", 17136f, 12f),
                    MuscleGroupData("Shoulders", 7140f, 5f),
                    MuscleGroupData("Core", 4284f, 3f),
                ),
                personalRecords = listOf(
                    PersonalRecordData("Deadlift", 180f, 160f, 5),
                    PersonalRecordData("Bench Press", 120f, 100f, 8),
                    PersonalRecordData("Squat", 160f, 140f, 6),
                ),
                strengthBalance = listOf(
                    StrengthBalanceData("Upper", 62736f, 44f),
                    StrengthBalanceData("Lower", 28560f, 20f),
                    StrengthBalanceData("Back", 39900f, 28f),
                    StrengthBalanceData("Core", 4284f, 3f),
                ),
                exerciseFrequency = listOf(
                    ExerciseFrequencyData("Deadlift", 24, 24),
                    ExerciseFrequencyData("Squat", 20, 24),
                    ExerciseFrequencyData("Bench Press", 18, 24),
                    ExerciseFrequencyData("Pull-Ups", 15, 24),
                    ExerciseFrequencyData("Overhead Press", 12, 24),
                ),
                currentStreak = 4,
                longestStreak = 12,
                workoutsPerWeek = listOf(
                    WeeklyConsistencyData("24/2", 3),
                    WeeklyConsistencyData("3/3", 4),
                    WeeklyConsistencyData("10/3", 2),
                    WeeklyConsistencyData("17/3", 5),
                    WeeklyConsistencyData("24/3", 3),
                    WeeklyConsistencyData("31/3", 4),
                    WeeklyConsistencyData("7/4", 2),
                    WeeklyConsistencyData("14/4", 4),
                ),
                totalWorkouts = 27,
                weightUnit = "kg",
            ),
        )
    }
}
