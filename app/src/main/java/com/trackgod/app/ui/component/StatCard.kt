package com.trackgod.app.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trackgod.app.R
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
 * @param backgroundAlignment Alignment for the rune pattern crop (each card shows a different area).
 * @param modifier Modifier for the root column.
 */
@Composable
fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    backgroundAlignment: Alignment = Alignment.Center,
) {
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .background(color = SurfaceLow, shape = RectangleShape),
    ) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        // Subtle rune pattern background (inverted so runes are light on dark)
        val invertMatrix = ColorMatrix(
            floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f,
            )
        )
        Image(
            painter = painterResource(id = R.drawable.runes_pattern),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = backgroundAlignment,
            colorFilter = ColorFilter.colorMatrix(invertMatrix),
            modifier = Modifier
                .matchParentSize()
                .alpha(0.22f),
        )

        // Semi-opaque overlay: hides the grey but lets bright rune lines peek through.
        // Diagonal gradient so one corner is slightly more visible than the rest.
        val gradientStart = when (backgroundAlignment) {
            Alignment.TopStart -> Offset(0f, 0f)
            Alignment.TopEnd -> Offset(widthPx, 0f)
            Alignment.BottomStart -> Offset(0f, heightPx)
            Alignment.BottomEnd -> Offset(widthPx, heightPx)
            else -> Offset(0f, 0f)
        }
        val gradientEnd = when (backgroundAlignment) {
            Alignment.TopStart -> Offset(widthPx, heightPx)
            Alignment.TopEnd -> Offset(0f, heightPx)
            Alignment.BottomStart -> Offset(widthPx, 0f)
            Alignment.BottomEnd -> Offset(0f, 0f)
            else -> Offset(widthPx, heightPx)
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colorStops = arrayOf(
                            0.0f to SurfaceLow.copy(alpha = 0.55f),
                            0.5f to SurfaceLow.copy(alpha = 0.75f),
                            1.0f to SurfaceLow.copy(alpha = 0.9f),
                        ),
                        start = gradientStart,
                        end = gradientEnd,
                    )
                ),
        )

        // Card content
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
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
