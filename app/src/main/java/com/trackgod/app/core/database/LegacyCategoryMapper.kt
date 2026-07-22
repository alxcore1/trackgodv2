package com.trackgod.app.core.database

object LegacyCategoryMapper {
    private val canonicalCategories = setOf(
        "Chest",
        "Back",
        "Legs",
        "Arms",
        "Shoulders",
        "Core",
        "Cardio",
    )

    val mappings: Map<String, String> = mapOf(
        "upper body" to "Chest",
        "lower body" to "Legs",
        "full body" to "Core",
        "chest" to "Chest",
        "back" to "Back",
        "lats" to "Back",
        "traps" to "Back",
        "legs" to "Legs",
        "quadriceps" to "Legs",
        "quads" to "Legs",
        "hamstrings" to "Legs",
        "glutes" to "Legs",
        "calves" to "Legs",
        "arms" to "Arms",
        "biceps" to "Arms",
        "triceps" to "Arms",
        "forearms" to "Arms",
        "shoulders" to "Shoulders",
        "delts" to "Shoulders",
        "deltoids" to "Shoulders",
        "core" to "Core",
        "abs" to "Core",
        "abdominals" to "Core",
        "cardio" to "Cardio",
    )

    fun normalize(rawCategory: String?): String {
        val trimmed = rawCategory?.trim().orEmpty()
        if (trimmed.isBlank()) return "Other"
        val key = trimmed.lowercase()
        return mappings[key] ?: canonicalCategories.firstOrNull { it.equals(trimmed, ignoreCase = true) } ?: trimmed
    }

    fun canReplaceExisting(existingCategory: String?): Boolean {
        val trimmed = existingCategory?.trim().orEmpty()
        if (trimmed.isBlank() || trimmed.equals("Other", ignoreCase = true)) return true
        val normalized = normalize(trimmed)
        return normalized != trimmed && normalized in canonicalCategories
    }

    fun betterCategory(sourceCategory: String?, existingCategory: String?): String? {
        val normalized = normalize(sourceCategory)
        if (normalized.equals("Other", ignoreCase = true)) return null
        return if (canReplaceExisting(existingCategory)) normalized else null
    }
}
