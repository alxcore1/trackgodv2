package com.trackgod.app.util

import java.util.Locale

/**
 * Formats volume for display with appropriate unit suffix.
 * Uses comma-separated thousands for mid-range values.
 * Examples: "1,234" (1234), "14.2K" (14200), "1.5M" (1500000)
 */
fun formatVolume(volume: Float): String = when {
    volume >= 1_000_000f -> String.format(Locale.US, "%.1fM", volume / 1_000_000f)
    volume >= 10_000f    -> String.format(Locale.US, "%.1fK", volume / 1_000f)
    volume >= 1_000f     -> String.format(Locale.US, "%,.0f", volume)
    else                 -> String.format(Locale.US, "%.0f", volume)
}

/**
 * Short format for compact displays (stat cards, headers).
 * Always abbreviates thousands with K suffix.
 * Examples: "500" (500), "1.5K" (1500), "1.5M" (1500000)
 */
fun formatVolumeShort(volume: Float): String = when {
    volume >= 1_000_000f -> String.format(Locale.US, "%.1fM", volume / 1_000_000f)
    volume >= 1_000f     -> String.format(Locale.US, "%.1fK", volume / 1_000f)
    else                 -> String.format(Locale.US, "%.0f", volume)
}
