package com.trackgod.app.core.util

/**
 * Generates a human-readable workout name from the muscle-group categories
 * of the exercises performed. Recognises common training splits
 * (Push, Pull, Full Body, Leg Day) when the category mix matches.
 */
object WorkoutNaming {

    fun generateName(categories: List<String>): String {
        val unique = categories.distinct()
        val set = unique.map { it.lowercase() }.toSet()

        return when {
            unique.isEmpty() -> "Workout"
            unique.size == 1 -> unique.first()

            // Push: chest + shoulders, optionally arms
            set.containsAll(setOf("chest", "shoulders")) &&
                    !set.contains("back") &&
                    !set.contains("legs") -> "Push"

            // Pull: back + arms (or biceps-focused), no chest
            set.contains("back") && set.contains("arms") &&
                    !set.contains("chest") &&
                    !set.contains("legs") -> "Pull"

            // Legs only (possibly with core)
            set.contains("legs") &&
                    set.none { it in setOf("chest", "back", "shoulders", "arms") } -> "Legs"

            // Full Body: 4+ distinct major groups
            set.count { it in setOf("chest", "back", "shoulders", "arms", "legs") } >= 4 -> "Full Body"

            // Default: join first two
            unique.size == 2 -> "${unique[0]} & ${unique[1]}"
            else -> unique.take(2).joinToString(" & ") + " +"
        }
    }
}
