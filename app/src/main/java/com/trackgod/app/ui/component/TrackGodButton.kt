package com.trackgod.app.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.SurfaceHighest
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary

/** Button style variants for the TrackGod design system. */
enum class ButtonVariant { Primary, Secondary, Ghost }

/**
 * Industrial button with press-scale animation.
 *
 * @param text Uppercase label.
 * @param onClick Click handler.
 * @param modifier Modifier for the root element.
 * @param variant Visual style -- Primary (red), Secondary (bordered), or Ghost (transparent).
 * @param enabled Controls interactivity and opacity.
 * @param icon Optional trailing icon.
 */
@Composable
fun TrackGodButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    textColorOverride: Color? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1f,
        animationSpec = tween(durationMillis = 60),
        label = "btnScale",
    )

    val bgColor = when (variant) {
        ButtonVariant.Primary -> Blood
        ButtonVariant.Secondary -> SurfaceLow
        ButtonVariant.Ghost -> Color.Transparent
    }

    val textColor = textColorOverride ?: when (variant) {
        ButtonVariant.Primary -> TextPrimary
        ButtonVariant.Secondary -> TextTertiary
        ButtonVariant.Ghost -> TextTertiary
    }

    val borderModifier = if (variant == ButtonVariant.Secondary) {
        Modifier.border(width = 1.dp, color = SurfaceHighest, shape = RectangleShape)
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(borderModifier)
            .background(color = bgColor, shape = RectangleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .defaultMinSize(minHeight = 48.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text.uppercase(),
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 3.sp,
            textAlign = TextAlign.Center,
        )
        if (icon != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun PrimaryButtonPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TrackGodButton(text = "Start Workout", onClick = {})
        TrackGodButton(text = "With Icon", onClick = {}, icon = Icons.Default.Add)
        TrackGodButton(text = "Disabled", onClick = {}, enabled = false)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun SecondaryButtonPreview() {
    TrackGodButton(
        text = "Secondary",
        onClick = {},
        variant = ButtonVariant.Secondary,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun GhostButtonPreview() {
    TrackGodButton(
        text = "Ghost",
        onClick = {},
        variant = ButtonVariant.Ghost,
    )
}
