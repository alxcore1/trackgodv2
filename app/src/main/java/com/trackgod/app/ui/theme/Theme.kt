package com.trackgod.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val TrackGodColorScheme = darkColorScheme(
    // Primary
    primary = Blood,
    onPrimary = TextPrimary,
    primaryContainer = Blood,
    onPrimaryContainer = BloodBright,

    // Secondary (mapped to blood variants)
    secondary = BloodDeep,
    onSecondary = TextPrimary,
    secondaryContainer = BloodDeep,
    onSecondaryContainer = BloodBright,

    // Tertiary
    tertiary = BloodWarm,
    onTertiary = Void,
    tertiaryContainer = BloodDeep,
    onTertiaryContainer = BloodBright,

    // Background
    background = Void,
    onBackground = TextPrimary,

    // Surface hierarchy
    surface = SurfaceLow,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceMid,
    onSurfaceVariant = BloodWarm,
    surfaceTint = Blood,
    inverseSurface = TextPrimary,
    inverseOnSurface = Void,

    // Surface container levels
    surfaceContainerLowest = VoidDeep,
    surfaceContainerLow = SurfaceLow,
    surfaceContainer = SurfaceMid,
    surfaceContainerHigh = SurfaceHigh,
    surfaceContainerHighest = SurfaceHighest,
    surfaceBright = SurfaceBright,
    surfaceDim = VoidDeep,

    // Outline
    outline = TextTertiary,
    outlineVariant = GhostBorder,

    // Error
    error = ErrorColor,
    onError = Void,
    errorContainer = BloodDeep,
    onErrorContainer = ErrorColor,

    // Inverse primary
    inversePrimary = BloodBright,

    // Scrim
    scrim = Void,
)

@Composable
fun TrackGodTheme(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
    ) {
        MaterialTheme(
            colorScheme = TrackGodColorScheme,
            typography = TrackGodTypography,
            shapes = TrackGodShapes,
            content = content,
        )
    }
}

/** Convenient accessor: `TrackGodTheme.spacing.md` */
object TrackGodTheme {
    val spacing: Spacing
        @Composable get() = LocalSpacing.current
}
