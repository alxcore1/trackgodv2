package com.trackgod.app.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.SurfaceHigh
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextPrimary

/**
 * Industrial surface container with optional red left accent border.
 *
 * @param modifier Modifier for the root container.
 * @param onClick When non-null the card becomes pressable with a surface color shift.
 * @param accentBorder When true a 4.dp red bar is drawn on the left edge.
 * @param content Column-scoped composable content.
 */
@Composable
fun TrackGodCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    accentBorder: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val backgroundColor by animateColorAsState(
        targetValue = if (onClick != null && isPressed) SurfaceHigh else SurfaceLow,
        animationSpec = tween(durationMillis = 80),
        label = "cardBg",
    )

    val rootModifier = modifier
        .fillMaxWidth()
        .background(color = backgroundColor, shape = RectangleShape)
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
            } else {
                Modifier
            }
        )

    if (accentBorder) {
        Row(
            modifier = rootModifier.height(IntrinsicSize.Min),
        ) {
            // 4dp red left accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(Blood),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                content = content,
            )
        }
    } else {
        Column(
            modifier = rootModifier.padding(horizontal = 16.dp, vertical = 12.dp),
            content = content,
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun TrackGodCardPreview() {
    TrackGodCard {
        Text("BASIC CARD", color = TextPrimary)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun TrackGodCardAccentPreview() {
    TrackGodCard(accentBorder = true) {
        Text("ACCENT CARD", color = TextPrimary)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun TrackGodCardClickablePreview() {
    TrackGodCard(onClick = {}, accentBorder = true) {
        Text("CLICKABLE CARD", color = TextPrimary)
    }
}
