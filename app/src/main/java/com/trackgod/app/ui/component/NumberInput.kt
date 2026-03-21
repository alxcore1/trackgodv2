package com.trackgod.app.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.VoidDeep

/**
 * Numeric input with decrement/increment buttons flanking a center value.
 *
 * Tapping the center value switches to keyboard input mode.
 *
 * @param value Current numeric text (e.g. "80", "12.5").
 * @param onValueChange Callback with the new string value.
 * @param label Uppercase label above the row.
 * @param unit Unit suffix displayed below the value (e.g. "KG", "REPS").
 * @param step Increment/decrement delta.
 * @param modifier Modifier for the root column.
 */
@Composable
fun NumberInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    unit: String = "",
    step: Float = 1f,
    maxValue: Float = Float.MAX_VALUE,
    modifier: Modifier = Modifier,
) {
    var isEditing by remember { mutableStateOf(false) }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        // Label
        Text(
            text = label.uppercase(),
            color = TextTertiary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // Decrement button
            StepButton(
                icon = Icons.Default.Remove,
                contentDescription = "Decrease",
                onClick = {
                    val current = value.toFloatOrNull() ?: 0f
                    val next = (current - step).coerceAtLeast(0f)
                    onValueChange(formatNumber(next, step))
                },
            )

            // Center value display / edit
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .defaultMinSize(minWidth = 72.dp)
                    .padding(horizontal = 8.dp),
            ) {
                if (isEditing) {
                    BasicTextField(
                        value = value,
                        onValueChange = { newVal ->
                            // Allow only valid numeric input
                            if (newVal.isEmpty() || newVal.matches(Regex("""^\d*\.?\d*$"""))) {
                                onValueChange(newVal)
                            }
                        },
                        modifier = Modifier.defaultMinSize(minWidth = 56.dp),
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        cursorBrush = SolidColor(Blood),
                    )
                } else {
                    Text(
                        text = value.ifEmpty { "0" },
                        color = TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { isEditing = true },
                    )
                }

                if (unit.isNotEmpty()) {
                    Text(
                        text = unit.uppercase(),
                        color = TextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                    )
                }
            }

            // Increment button
            StepButton(
                icon = Icons.Default.Add,
                contentDescription = "Increase",
                onClick = {
                    val current = value.toFloatOrNull() ?: 0f
                    val next = (current + step).coerceAtMost(maxValue)
                    onValueChange(formatNumber(next, step))
                },
            )
        }
    }
}

/**
 * 40x40 step button with press-scale animation.
 */
@Composable
private fun StepButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = tween(durationMillis = 60),
        label = "stepScale",
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(color = SurfaceLow, shape = RectangleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = BloodBright,
            modifier = Modifier.size(20.dp),
        )
    }
}

/**
 * Format the float to a clean string -- no trailing ".0" if the step is integral.
 */
private fun formatNumber(value: Float, step: Float): String {
    return if (step % 1f == 0f) {
        value.toInt().toString()
    } else {
        // Keep one decimal place for fractional steps (e.g., 2.5 KG plates)
        "%.1f".format(value)
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun NumberInputKgPreview() {
    NumberInput(
        value = "80",
        onValueChange = {},
        label = "Weight",
        unit = "KG",
        step = 2.5f,
        modifier = Modifier.padding(16.dp),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun NumberInputRepsPreview() {
    NumberInput(
        value = "12",
        onValueChange = {},
        label = "Reps",
        unit = "REPS",
        step = 1f,
        modifier = Modifier.padding(16.dp),
    )
}
