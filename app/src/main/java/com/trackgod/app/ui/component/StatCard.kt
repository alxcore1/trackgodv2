package com.trackgod.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary

/**
 * Compact metric display card for the dashboard.
 *
 * Layout (centered column):
 *   LABEL  (10sp, uppercase, tertiary)
 *   Icon   (18dp, tertiary)
 *   VALUE  (28sp, Black weight, primary)
 *   UNIT   (10sp, uppercase, tertiary)
 *
 * @param icon Metric icon.
 * @param label Description above the icon (e.g. "VOLUME").
 * @param value Primary number string (e.g. "12,450").
 * @param unit Suffix below the value (e.g. "KG").
 * @param modifier Modifier for the root column.
 */
@Composable
fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(color = SurfaceLow, shape = RectangleShape)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Label
        Text(
            text = label.uppercase(),
            color = TextTertiary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Icon
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = TextTertiary,
            modifier = Modifier.size(18.dp),
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Value
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Unit
        Text(
            text = unit.uppercase(),
            color = TextTertiary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun StatCardPreview() {
    StatCard(
        icon = Icons.Default.FitnessCenter,
        label = "Volume",
        value = "12,450",
        unit = "KG",
        modifier = Modifier.padding(16.dp),
    )
}
