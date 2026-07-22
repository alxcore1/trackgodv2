package com.trackgod.app.feature.stats

import com.trackgod.app.util.formatVolumeShort
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import com.trackgod.app.ui.component.ButtonVariant
import com.trackgod.app.ui.component.TrackGodButton
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.trackgod.app.ui.theme.SurfaceHighest
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextSecondary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.TrackGodTheme
import com.trackgod.app.ui.theme.screenPadding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    StatsContent(
        state = state,
        onStatsModeChanged = viewModel::onStatsModeChanged,
        onTimeRangeChanged = viewModel::onTimeRangeChanged,
    )
}

@Composable
internal fun StatsContent(
    state: StatsState,
    onStatsModeChanged: (StatsMode) -> Unit = {},
    onTimeRangeChanged: (TimeRange) -> Unit = {},
) {
    val spacing = TrackGodTheme.spacing

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

    if (!state.hasData && state.selectedStatsMode == StatsMode.Performance) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = spacing.xs)
                .padding(horizontal = spacing.screenPadding),
        ) {
            TrackGodHeader()

            Spacer(modifier = Modifier.height(spacing.md))

            StatsModeSelector(
                selected = state.selectedStatsMode,
                onSelect = onStatsModeChanged,
            )

            Spacer(modifier = Modifier.height(spacing.lg))

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
                color = Blood,
                maxLines = 1,
            )

            Spacer(modifier = Modifier.height(spacing.xl))

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
            .padding(top = spacing.xs),
    ) {
        // Inline progress bar for time-range switches
        if (state.isRefreshing) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = Blood,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenPadding),
        ) {
        TrackGodHeader()

        Spacer(modifier = Modifier.height(spacing.md))

        StatsModeSelector(
            selected = state.selectedStatsMode,
            onSelect = onStatsModeChanged,
        )

        Spacer(modifier = Modifier.height(spacing.lg))

        if (state.selectedStatsMode == StatsMode.Personal) {
            PersonalDashboard(
                data = state.personalDashboard,
            )
            Spacer(modifier = Modifier.height(spacing.xxl))
            return@Column
        }

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

        Spacer(modifier = Modifier.height(spacing.lg))

        if (!state.hasWorkoutsInSelectedRange && state.rangeEmptyMessage.isNotBlank()) {
            RangeEmptyNotice(message = state.rangeEmptyMessage)
            Spacer(modifier = Modifier.height(spacing.lg))
        }

        Text(
            text = "PROGRESS",
            style = MaterialTheme.typography.labelLarge,
            color = Blood,
        )
        Spacer(modifier = Modifier.height(spacing.md))

        if (state.volumeByPeriod.isNotEmpty()) {
            TrackGodCard {
                VolumeChart(
                    data = state.volumeByPeriod,
                    insight = state.volumeInsight,
                )
            }
            Spacer(modifier = Modifier.height(spacing.lg))
        }

        if (state.exerciseProgressions.isNotEmpty()) {
            ExerciseProgressSection(
                progressions = state.exerciseProgressions,
                weightUnit = state.weightUnit,
            )
            Spacer(modifier = Modifier.height(spacing.lg))
        }

        if (state.personalRecords.isNotEmpty()) {
            PersonalRecordsSection(
                records = state.personalRecords,
                weightUnit = state.weightUnit,
            )
            Spacer(modifier = Modifier.height(spacing.lg))
        }

        var showOneRepMaxSheet by remember { mutableStateOf(false) }
        TrackGodButton(
            text = "1RM CALCULATOR",
            onClick = { showOneRepMaxSheet = true },
            variant = ButtonVariant.Secondary,
            modifier = Modifier
                .fillMaxWidth(),
        )
        if (showOneRepMaxSheet) {
            OneRepMaxSheet(
                weightUnit = state.weightUnit,
                onDismiss = { showOneRepMaxSheet = false },
            )
        }

        Spacer(modifier = Modifier.height(spacing.xl))

        Text(
            text = "CONSISTENCY",
            style = MaterialTheme.typography.labelLarge,
            color = Blood,
        )
        Spacer(modifier = Modifier.height(spacing.md))

        if (state.heatmapData.isNotEmpty()) {
            TrackGodCard {
                HeatmapChart(
                    data = state.heatmapData,
                    insight = state.heatmapInsight,
                )
            }
            Spacer(modifier = Modifier.height(spacing.lg))
        }

        TrackGodCard {
            ConsistencySection(
                currentStreak = state.currentStreak,
                longestStreak = state.longestStreak,
                workoutsPerWeek = state.workoutsPerWeek,
            )
        }

        Spacer(modifier = Modifier.height(spacing.xl))

        Text(
            text = "BALANCE",
            style = MaterialTheme.typography.labelLarge,
            color = Blood,
        )
        Spacer(modifier = Modifier.height(spacing.md))

        if (state.strengthBalance.isNotEmpty()) {
            TrackGodCard {
                StrengthBalanceSection(
                    data = state.strengthBalance,
                )
            }
            Spacer(modifier = Modifier.height(spacing.lg))
        }

        if (state.muscleGroupVolumes.isNotEmpty()) {
            TrackGodCard {
                MuscleGroupChart(
                    data = state.muscleGroupVolumes,
                )
            }
            Spacer(modifier = Modifier.height(spacing.lg))
        }

        if (state.exerciseFrequency.isNotEmpty()) {
            TrackGodCard {
                ExerciseFrequencySection(
                    data = state.exerciseFrequency,
                )
            }
            Spacer(modifier = Modifier.height(spacing.lg))
        }

        // Bottom padding for nav bar clearance
        Spacer(modifier = Modifier.height(spacing.xxl))
        } // inner scrollable Column
    } // outer Column with progress bar
    } // MetalTextureBackground
}

@Composable
private fun RangeEmptyNotice(message: String) {
    TrackGodCard {
        Text(
            text = message,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
                color = Blood,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "SWITCH TO ALL TO REVIEW HISTORICAL TRAINING",
            style = MaterialTheme.typography.labelMedium,
            color = TextTertiary,
        )
    }
}

@Composable
private fun StatsModeSelector(
    selected: StatsMode,
    onSelect: (StatsMode) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        StatsMode.entries.forEach { mode ->
            TimeRangeChip(
                label = mode.label,
                isActive = mode == selected,
                onClick = { onSelect(mode) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PersonalDashboard(data: PersonalDashboardData) {
    val spacing = TrackGodTheme.spacing

    Text(
        text = "PERSONAL DASHBOARD",
        style = MaterialTheme.typography.titleLarge,
        color = TextPrimary,
        fontWeight = FontWeight.Black,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "PROFILE SIGNALS :: 90 DAY HABITS",
        style = MaterialTheme.typography.labelSmall,
        color = TextTertiary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
    Spacer(modifier = Modifier.height(spacing.lg))

    PersonalSnapshot(
        recommendedKcal = data.recommendedKcal,
        currentWeight = data.currentWeight,
        targetAdherence = data.targetAdherence,
    )

    Spacer(modifier = Modifier.height(spacing.md))

    PersonalMetricSection(
        title = "BODY",
        metrics = listOf(
            data.currentWeight,
            data.lastWeighIn,
            data.age,
            data.objective,
            data.appTenure,
        ),
    )

    Spacer(modifier = Modifier.height(spacing.md))

    PersonalMetricSection(
        title = "NUTRITION",
        metrics = listOf(
            data.recommendedKcal,
            data.maintenanceKcal,
            data.targetAdherence,
        ),
    )

    Spacer(modifier = Modifier.height(spacing.md))

    PersonalMetricSection(
        title = "TRAINING RHYTHM",
        metrics = listOf(
            data.averageWorkoutsPerWeek,
            data.favoriteWeekday,
            data.favoriteTimeWindow,
            data.averageSessionDuration.copy(
                title = "SESSION / REST",
                detail = "REST GAP: ${data.longestRestGap.value}",
            ),
        ),
    )
}

@Composable
private fun PersonalSnapshot(
    recommendedKcal: PersonalMetricCardData,
    currentWeight: PersonalMetricCardData,
    targetAdherence: PersonalMetricCardData,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceLow, RectangleShape)
            .border(1.dp, Blood.copy(alpha = 0.42f), RectangleShape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            SnapshotMetric(
                data = recommendedKcal,
                modifier = Modifier.weight(1f),
                isPrimary = true,
            )
            Box(
                modifier = Modifier
                    .padding(start = 12.dp, top = 3.dp)
                    .height(42.dp)
                    .width(2.dp)
                    .background(Blood, RectangleShape),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SnapshotMetric(
                data = currentWeight,
                modifier = Modifier.weight(1f),
            )
            SnapshotMetric(
                data = targetAdherence,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SnapshotMetric(
    data: PersonalMetricCardData,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
) {
    val display = data.toPersonalDisplay()
    Column(modifier = modifier) {
        Text(
            text = display.title,
            style = MaterialTheme.typography.labelSmall,
            color = if (isPrimary) Blood else TextTertiary,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(if (isPrimary) 6.dp else 4.dp))
        Text(
            text = display.value,
            style = when {
                data.isLocked -> MaterialTheme.typography.labelMedium
                isPrimary -> MaterialTheme.typography.headlineMedium
                else -> MaterialTheme.typography.titleLarge
            },
            fontWeight = FontWeight.Black,
            color = if (data.isLocked) TextSecondary else TextPrimary,
            maxLines = if (data.isLocked) 2 else 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (display.detail.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = display.detail,
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PersonalMetricSection(
    title: String,
    metrics: List<PersonalMetricCardData>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceLow.copy(alpha = 0.58f), RectangleShape)
            .border(1.dp, TextTertiary.copy(alpha = 0.12f), RectangleShape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = Blood,
            fontWeight = FontWeight.Black,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.height(8.dp))
        metrics.forEachIndexed { index, metric ->
            PersonalMetricRow(data = metric)
            if (index != metrics.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(TextTertiary.copy(alpha = 0.1f), RectangleShape),
                )
            }
        }
    }
}

@Composable
private fun PersonalMetricRow(data: PersonalMetricCardData) {
    val display = data.toPersonalDisplay()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(0.44f)) {
            Text(
                text = display.title,
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (display.detail.isNotBlank()) {
                Text(
                    text = display.detail,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary.copy(alpha = 0.74f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Text(
            text = display.value,
            modifier = Modifier.weight(0.56f),
            style = if (data.isLocked) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = if (data.isLocked) TextSecondary else TextPrimary,
            textAlign = TextAlign.End,
            maxLines = if (data.isLocked) 2 else 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private data class PersonalMetricDisplay(
    val title: String,
    val value: String,
    val detail: String,
)

private fun PersonalMetricCardData.toPersonalDisplay(): PersonalMetricDisplay {
    if (isLocked) {
        return PersonalMetricDisplay(
            title = title,
            value = unlockPrompt(),
            detail = "",
        )
    }

    return when (title) {
        "AGE" -> PersonalMetricDisplay(title, value.withSuffix("YRS"), detail)
        "RECOMMENDED KCAL" -> PersonalMetricDisplay(title, value.withSuffix("KCAL"), detail.ifBlank { "DAILY TARGET" })
        "MAINTENANCE KCAL" -> PersonalMetricDisplay(title, value.withSuffix("KCAL"), "TDEE")
        "LAST WEIGH-IN" -> PersonalMetricDisplay(title, detail.ifBlank { value }, value.asDisplayDate())
        else -> PersonalMetricDisplay(title, value, detail.ifBlank { defaultMetricDetail() })
    }
}

private fun PersonalMetricCardData.unlockPrompt(): String =
    when (title) {
        "AGE" -> "ADD BIRTHDAY TO UNLOCK AGE"
        "RECOMMENDED KCAL" -> value
        "MAINTENANCE KCAL" -> value
        else -> value
    }

private fun PersonalMetricCardData.defaultMetricDetail(): String =
    when (title) {
        "CURRENT WEIGHT" -> "LATEST LOG"
        "TARGET ADHERENCE" -> "LAST 90 DAYS"
        "APP TENURE" -> "SINCE JOIN"
        "OBJECTIVE" -> "ACTIVE GOAL"
        "AVG WORKOUTS / WEEK" -> "LAST 90 DAYS"
        "FAVORITE WEEKDAY" -> "LAST 90 DAYS"
        "FAVORITE TIME" -> "START TIME"
        else -> ""
    }

private fun String.withSuffix(suffix: String): String =
    if (contains(suffix, ignoreCase = true)) this else "$this $suffix"

private fun String.asDisplayDate(): String =
    runCatching {
        LocalDate.parse(this).format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)).uppercase(Locale.US)
    }.getOrDefault(this)

// ── Hero Section ─────────────────────────────────────────────────────────────

@Composable
private fun HeroSection(
    totalVolume: Float,
    weightUnit: String,
) {
    val spacing = TrackGodTheme.spacing
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
                    color = Blood,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.height(spacing.xs))
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
                    text = formatVolumeShort(totalVolume),
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
                hasWorkoutsInSelectedRange = true,
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
