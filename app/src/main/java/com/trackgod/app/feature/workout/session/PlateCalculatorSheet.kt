package com.trackgod.app.feature.workout.session

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trackgod.app.ui.component.NumberInput
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextSecondary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.Void

// Plate colors (kg)
private val plateColorsKg = listOf(
    25f to Color(0xFFCC2222),  // Red
    20f to Color(0xFF2244AA),  // Blue
    15f to Color(0xFFCCCC22),  // Yellow
    10f to Color(0xFF22AA22),  // Green
    5f to Color(0xFFDDDDDD),   // White
    2.5f to Color(0xFF444444), // Dark gray
    1.25f to Color(0xFF999999),// Chrome
)

// Plate colors (lbs)
private val plateColorsLbs = listOf(
    45f to Color(0xFF2244AA),  // Blue
    35f to Color(0xFFCCCC22),  // Yellow
    25f to Color(0xFF22AA22),  // Green
    10f to Color(0xFFDDDDDD),  // White
    5f to Color(0xFFCC2222),   // Red
    2.5f to Color(0xFF999999), // Chrome
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlateCalculatorSheet(
    initialWeight: String,
    weightUnit: String,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isLbs = weightUnit.equals("lbs", ignoreCase = true)
    val defaultBar = if (isLbs) 45f else 20f
    val plates = if (isLbs) plateColorsLbs else plateColorsKg

    var targetInput by remember { mutableStateOf(initialWeight) }
    val targetWeight = targetInput.replace(",", ".").toFloatOrNull() ?: 0f
    val plateColorMap = plates.toMap()

    val perSide = calculatePlatesPerSide(targetWeight, defaultBar, plates.map { it.first })

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Void,
        shape = RectangleShape,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
        ) {
            Text(
                text = "PLATE CALCULATOR",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "BAR: ${defaultBar.toInt()}${weightUnit.uppercase()} · EACH SIDE",
                color = TextTertiary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Target weight input
            NumberInput(
                value = targetInput,
                onValueChange = { targetInput = it },
                label = "TARGET WEIGHT",
                unit = weightUnit.uppercase(),
                step = if (isLbs) 5f else 2.5f,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Visual barbell
            if (targetWeight > defaultBar && perSide.isNotEmpty()) {
                BarbellVisualization(
                    platesPerSide = perSide,
                    plateColors = plateColorMap,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Plate breakdown text
                Text(
                    text = "EACH SIDE",
                    color = TextTertiary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                )
                Spacer(modifier = Modifier.height(6.dp))
                perSide.forEach { (plate, count) ->
                    val plateStr = if (plate % 1f == 0f) "${plate.toInt()}" else String.format(java.util.Locale.US, "%.1f", plate)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .height(12.dp)
                                    .background(plateColorMap[plate] ?: TextTertiary),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$plateStr ${weightUnit.uppercase()}",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Text(
                            text = "× $count",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                }
            } else if (targetWeight > 0f && targetWeight <= defaultBar) {
                Text(
                    text = "BARBELL ONLY",
                    color = BloodBright,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                )
            } else if (targetWeight > defaultBar && perSide.isEmpty()) {
                Text(
                    text = "CANNOT LOAD EVENLY",
                    color = Blood,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BarbellVisualization(
    platesPerSide: List<Pair<Float, Int>>,
    plateColors: Map<Float, Color>,
    modifier: Modifier = Modifier,
) {
    val maxPlateWeight = platesPerSide.firstOrNull()?.first ?: 25f

    Canvas(modifier = modifier) {
        val centerY = size.height / 2f
        val barHeight = 8.dp.toPx()
        val barColor = Color(0xFF666666)

        // Draw bar
        drawRect(
            color = barColor,
            topLeft = Offset(0f, centerY - barHeight / 2),
            size = Size(size.width, barHeight),
        )

        // Draw center collar
        val collarWidth = 16.dp.toPx()
        drawRect(
            color = Color(0xFF888888),
            topLeft = Offset(size.width / 2 - collarWidth / 2, centerY - 14.dp.toPx()),
            size = Size(collarWidth, 28.dp.toPx()),
        )

        // Draw plates on each side
        val plateGap = 2.dp.toPx()
        val maxPlateHeight = size.height * 0.9f
        val plateWidth = 10.dp.toPx()

        // Right side
        var xPos = size.width / 2 + collarWidth / 2 + 4.dp.toPx()
        for ((plate, count) in platesPerSide) {
            val heightFraction = (plate / maxPlateWeight).coerceIn(0.3f, 1f)
            val h = maxPlateHeight * heightFraction
            val color = plateColors[plate] ?: Color.Gray
            repeat(count) {
                drawRect(
                    color = color,
                    topLeft = Offset(xPos, centerY - h / 2),
                    size = Size(plateWidth, h),
                )
                xPos += plateWidth + plateGap
            }
        }

        // Left side (mirror)
        xPos = size.width / 2 - collarWidth / 2 - 4.dp.toPx()
        for ((plate, count) in platesPerSide) {
            val heightFraction = (plate / maxPlateWeight).coerceIn(0.3f, 1f)
            val h = maxPlateHeight * heightFraction
            val color = plateColors[plate] ?: Color.Gray
            repeat(count) {
                xPos -= plateWidth
                drawRect(
                    color = color,
                    topLeft = Offset(xPos, centerY - h / 2),
                    size = Size(plateWidth, h),
                )
                xPos -= plateGap
            }
        }
    }
}

/**
 * Calculate which plates to put on each side of the bar.
 * Returns list of (plateWeight, count) pairs, largest first.
 */
private fun calculatePlatesPerSide(
    targetWeight: Float,
    barWeight: Float,
    availablePlates: List<Float>,
): List<Pair<Float, Int>> {
    if (targetWeight <= barWeight) return emptyList()
    var remaining = (targetWeight - barWeight) / 2f
    val result = mutableListOf<Pair<Float, Int>>()
    for (plate in availablePlates.sortedDescending()) {
        val count = (remaining / plate).toInt()
        if (count > 0) {
            result.add(plate to count)
            remaining -= count * plate
        }
    }
    // If remaining weight can't be loaded, return empty to trigger "CANNOT LOAD EVENLY"
    if (remaining > 0.01f) return emptyList()
    return result
}
