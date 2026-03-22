package com.trackgod.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.BloodGlow
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.VoidDeep

/**
 * Navigation tab definition.
 */
data class NavTab(
    val route: String,
    val label: String,
    val icon: ImageVector? = null,
    val iconRes: Int? = null,
)

/** The four primary navigation destinations. */
val NavTabs = listOf(
    NavTab(route = "altar", label = "ALTAR", iconRes = com.trackgod.app.R.drawable.ic_pentagram),
    NavTab(route = "workout", label = "WORKOUT", icon = Icons.Default.FitnessCenter),
    NavTab(route = "stats", label = "STATS", icon = Icons.Default.BarChart),
    NavTab(route = "profile", label = "PROFILE", icon = Icons.Default.Person),
)

/**
 * 4-tab bottom navigation bar with industrial styling.
 *
 * Active tab: red top border with glow, bright accent color.
 * Inactive tab: muted tertiary color.
 *
 * @param currentRoute The currently active route string.
 * @param onNavigate Callback with the destination route.
 */
@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val glowColor = BloodGlow.copy(alpha = 0.15f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                // Red glow bleed at the top edge
                drawIntoCanvas { canvas ->
                    val paint = Paint().asFrameworkPaint().apply {
                        isAntiAlias = true
                        color = Color.Transparent.toArgb()
                        setShadowLayer(20.dp.toPx(), 0f, -4.dp.toPx(), glowColor.toArgb())
                    }
                    canvas.drawLine(
                        p1 = Offset(0f, 0f),
                        p2 = Offset(size.width, 0f),
                        paint = androidx.compose.ui.graphics.Paint().apply {
                            asFrameworkPaint().set(paint)
                        },
                    )
                }
            }
            .background(VoidDeep),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            NavTabs.forEach { tab ->
                val isActive = currentRoute == tab.route
                NavTabItem(
                    tab = tab,
                    isActive = isActive,
                    onClick = { onNavigate(tab.route) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Safe area inset
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars),
        )
    }
}

@Composable
private fun NavTabItem(
    tab: NavTab,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconTint = if (isActive) BloodBright else TextTertiary
    val labelColor = if (isActive) BloodBright else TextTertiary.copy(alpha = 0.7f)

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .height(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Active top border (thicker for visibility)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isActive) 3.dp else 1.dp)
                .background(if (isActive) Blood else Color.Transparent),
        )

        Spacer(modifier = Modifier.weight(1f))

        if (tab.iconRes != null) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(tab.iconRes),
                contentDescription = tab.label,
                tint = iconTint,
                modifier = Modifier.size(22.dp),
            )
        } else if (tab.icon != null) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                tint = iconTint,
                modifier = Modifier.size(22.dp),
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = tab.label,
            color = labelColor,
            fontSize = 9.sp,
            fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold,
            letterSpacing = 2.sp,
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun BottomNavBarPreview() {
    BottomNavBar(
        currentRoute = "altar",
        onNavigate = {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun BottomNavBarWorkoutPreview() {
    BottomNavBar(
        currentRoute = "workout",
        onNavigate = {},
    )
}
