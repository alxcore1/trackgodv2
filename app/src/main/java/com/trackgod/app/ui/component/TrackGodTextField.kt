package com.trackgod.app.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.VoidDeep

/**
 * Industrial text field with a red left accent bar on focus.
 *
 * Uses [BasicTextField] for full styling control -- no outline, no underline.
 *
 * @param value Current text value.
 * @param onValueChange Callback for value changes.
 * @param label Uppercase label rendered above the field.
 * @param modifier Modifier for the root column.
 * @param placeholder Placeholder text shown when empty.
 * @param keyboardType Keyboard configuration.
 * @param singleLine Whether the field is single-line.
 */
@Composable
fun TrackGodTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
) {
    var isFocused by remember { mutableStateOf(false) }

    val accentColor by animateColorAsState(
        targetValue = if (isFocused) Blood else Color.Transparent,
        animationSpec = tween(durationMillis = 150),
        label = "accentBar",
    )

    Column(modifier = modifier) {
        // Label
        Text(
            text = label.uppercase(),
            color = TextTertiary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Input row: accent bar + field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .background(VoidDeep),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(accentColor),
            )

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 14.dp)
                    .onFocusChanged { isFocused = it.isFocused },
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                ),
                singleLine = singleLine,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                cursorBrush = SolidColor(Blood),
                decorationBox = { innerTextField ->
                    Box {
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(
                                text = placeholder.uppercase(),
                                color = TextTertiary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun TrackGodTextFieldEmptyPreview() {
    TrackGodTextField(
        value = "",
        onValueChange = {},
        label = "Exercise Name",
        placeholder = "Enter name",
        modifier = Modifier.padding(16.dp),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun TrackGodTextFieldFilledPreview() {
    TrackGodTextField(
        value = "BENCH PRESS",
        onValueChange = {},
        label = "Exercise Name",
        modifier = Modifier.padding(16.dp),
    )
}
