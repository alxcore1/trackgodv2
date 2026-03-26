package com.trackgod.app.feature.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.SurfaceHighest
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextSecondary
import com.trackgod.app.ui.theme.TextTertiary
import java.text.NumberFormat
import java.util.Locale

/**
 * Inline expansion showing exercise-by-exercise breakdown for a workout.
 * Uses AnimatedVisibility to smoothly expand/collapse within the card.
 */
@Composable
fun WorkoutDetailInline(
    exercises: List<ExerciseWithSetsInWorkout>,
    isVisible: Boolean,
    weightUnit: String,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        ) {
            // Thin separator
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SurfaceHighest),
            )

            Spacer(modifier = Modifier.height(12.dp))

            exercises.forEachIndexed { index, exercise ->
                ExerciseSetGroup(
                    index = index + 1,
                    exercise = exercise,
                    weightUnit = weightUnit,
                )
                if (index < exercises.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun ExerciseSetGroup(
    index: Int,
    exercise: ExerciseWithSetsInWorkout,
    weightUnit: String,
) {
    val maxWeight = exercise.sets.maxOfOrNull { it.weight } ?: 1f

    Column(modifier = Modifier.fillMaxWidth()) {
        // Exercise name with index prefix
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "%02d".format(index),
                color = Blood,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = exercise.exerciseName.uppercase(),
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Individual sets with intensity bars
        exercise.sets.forEach { set ->
            SetRow(
                weight = set.weight,
                reps = set.reps,
                weightUnit = weightUnit,
                rpe = set.rpe,
                barFraction = if (maxWeight > 0f) set.weight / maxWeight else 0f,
            )
        }

        // Total volume for this exercise
        val totalVolume = exercise.sets.sumOf { (it.weight * it.reps).toDouble() }.toFloat()
        if (totalVolume > 0f) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "TOTAL: ${NumberFormat.getNumberInstance(Locale.US).format(totalVolume.toLong())} ${weightUnit.uppercase()}",
                color = BloodBright,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 28.dp),
            )
        }
    }
}

@Composable
private fun SetRow(
    weight: Float,
    reps: Int,
    weightUnit: String,
    rpe: Int? = null,
    barFraction: Float = 0f,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 28.dp, top = 2.dp),
    ) {
        Row {
            Text(
                text = "${formatWeight(weight)}$weightUnit x $reps",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.5.sp,
            )
            if (rpe != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "@$rpe",
                    color = TextTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                )
            }
        }
        // Mini intensity bar
        if (barFraction > 0f) {
            Spacer(modifier = Modifier.height(2.dp))
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth(barFraction.coerceIn(0f, 1f))
                    .height(2.dp)
                    .background(Blood.copy(alpha = 0.4f)),
            )
        }
    }
}

/** Format weight: strip trailing .0 for whole numbers. */
private fun formatWeight(weight: Float): String {
    return if (weight == weight.toLong().toFloat()) {
        weight.toLong().toString()
    } else {
        String.format(java.util.Locale.US, "%.1f", weight)
    }
}
