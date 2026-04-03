package com.trackgod.app.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.TrackGodTheme
import com.trackgod.app.ui.theme.Void

data class BrandItem(
    val name: String,
    val exerciseCount: Int,
    val isSelected: Boolean = false,
)

/**
 * Two-column grid of selectable brand chips in the industrial TrackGod style.
 *
 * Used in onboarding (Step 2) and "My Gym" settings screen.
 *
 * @param brands All available brands with counts and selection state.
 * @param onToggleBrand Called with the brand name when user taps a chip.
 * @param modifier Modifier for the root grid.
 */
@Composable
fun BrandPicker(
    brands: List<BrandItem>,
    onToggleBrand: (String) -> Unit,
    onSelectAll: (() -> Unit)? = null,
    onDeselectAll: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val spacing = TrackGodTheme.spacing
    val selectedCount = brands.count { it.isSelected }
    val allSelected = selectedCount == brands.size

    Column(modifier = modifier) {
        if (onSelectAll != null || onDeselectAll != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$selectedCount OF ${brands.size} SELECTED",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                    ),
                    color = TextTertiary,
                )
                Spacer(modifier = Modifier.weight(1f))
                if (allSelected && onDeselectAll != null) {
                    Text(
                        text = "DESELECT ALL",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.sp,
                        ),
                        color = BloodBright,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onDeselectAll,
                            )
                            .padding(vertical = spacing.sm, horizontal = spacing.xs),
                    )
                } else if (!allSelected && onSelectAll != null) {
                    Text(
                        text = "SELECT ALL",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.sp,
                        ),
                        color = BloodBright,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onSelectAll,
                            )
                            .padding(vertical = spacing.sm, horizontal = spacing.xs),
                    )
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(0.dp),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            items(brands, key = { it.name }) { brand ->
                BrandChip(
                    brand = brand,
                    onToggle = { onToggleBrand(brand.name) },
                )
            }
        }
    }
}

@Composable
private fun BrandChip(
    brand: BrandItem,
    onToggle: () -> Unit,
) {
    val spacing = TrackGodTheme.spacing
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 60),
        label = "brandChipScale",
    )

    val bgColor by animateColorAsState(
        targetValue = if (brand.isSelected) Blood else SurfaceLow,
        animationSpec = tween(durationMillis = 150),
        label = "brandChipBg",
    )

    val nameColor by animateColorAsState(
        targetValue = if (brand.isSelected) TextPrimary else TextTertiary,
        animationSpec = tween(durationMillis = 150),
        label = "brandChipName",
    )

    val subtitleColor by animateColorAsState(
        targetValue = if (brand.isSelected) TextPrimary.copy(alpha = 0.7f) else TextTertiary,
        animationSpec = tween(durationMillis = 150),
        label = "brandChipSub",
    )

    Column(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(color = bgColor, shape = RectangleShape)
            .then(
                if (brand.isSelected) Modifier.border(2.dp, BloodBright, RectangleShape)
                else Modifier
            )
            .semantics(mergeDescendants = true) {
                role = Role.Checkbox
                stateDescription = if (brand.isSelected) "Selected" else "Not selected"
            }
            .toggleable(
                value = brand.isSelected,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Checkbox,
                onValueChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggle()
                },
            )
            .defaultMinSize(minHeight = 72.dp)
            .padding(horizontal = spacing.lg, vertical = spacing.md),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = brand.name.uppercase(),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            ),
            color = nameColor,
        )
        Text(
            text = "${brand.exerciseCount} MACHINES",
            style = MaterialTheme.typography.bodySmall.copy(
                letterSpacing = 1.sp,
            ),
            color = subtitleColor,
        )
    }
}

// -- Previews -----------------------------------------------------------------

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun BrandPickerPreview() {
    val sampleBrands = listOf(
        BrandItem(name = "CYBEX", exerciseCount = 49, isSelected = true),
        BrandItem(name = "HAMMER STRENGTH", exerciseCount = 38, isSelected = true),
        BrandItem(name = "LIFE FITNESS", exerciseCount = 32),
        BrandItem(name = "TECHNOGYM", exerciseCount = 27),
        BrandItem(name = "PRECOR", exerciseCount = 18),
        BrandItem(name = "MATRIX", exerciseCount = 15),
    )

    TrackGodTheme {
        val spacing = TrackGodTheme.spacing
        BrandPicker(
            brands = sampleBrands,
            onToggleBrand = {},
            modifier = Modifier
                .background(Void)
                .padding(spacing.lg),
        )
    }
}
