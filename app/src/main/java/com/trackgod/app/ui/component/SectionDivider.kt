package com.trackgod.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trackgod.app.ui.theme.SurfaceHighest
import com.trackgod.app.ui.theme.TextTertiary

/**
 * "--- LABEL TEXT ---" pattern separator.
 *
 * Two horizontal lines flank an uppercase label with wide tracking.
 *
 * @param text Label shown between the divider lines.
 * @param modifier Modifier for the root row.
 */
@Composable
fun SectionDivider(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left line
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(SurfaceHighest),
        )

        // Center label
        Text(
            text = text.uppercase(),
            color = TextTertiary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp,
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        // Right line
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(SurfaceHighest),
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun SectionDividerPreview() {
    SectionDivider(
        text = "Recent Workouts",
        modifier = Modifier.padding(16.dp),
    )
}
