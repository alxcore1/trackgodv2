package com.trackgod.app.core.util

/**
 * String similarity utilities for fuzzy-matching OCR text against exercise names.
 *
 * Uses Levenshtein distance normalized to a 0..1 similarity score,
 * plus common OCR error correction heuristics.
 */
object StringSimilarity {

    /**
     * Standard dynamic-programming Levenshtein distance.
     * Returns the minimum number of single-character edits (insertions,
     * deletions, substitutions) required to transform [s1] into [s2].
     */
    fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j

        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        return dp[m][n]
    }

    /**
     * Normalized similarity score between two strings.
     * @return 0.0 (completely different) to 1.0 (identical).
     */
    fun similarity(s1: String, s2: String): Float {
        val a = s1.uppercase().trim()
        val b = s2.uppercase().trim()
        val maxLen = maxOf(a.length, b.length)
        if (maxLen == 0) return 1.0f
        return 1.0f - (levenshteinDistance(a, b).toFloat() / maxLen)
    }

    /**
     * Correct common OCR character misreads.
     * Maps digits to the letters they are most often confused with.
     */
    fun correctOcrErrors(text: String): String {
        return text
            .replace('0', 'O')
            .replace('1', 'I')
            .replace('5', 'S')
            .replace('8', 'B')
    }

    /**
     * Generate search variations from raw OCR text for fuzzy matching.
     * Returns distinct non-blank candidates ordered from most literal
     * to most transformed.
     */
    fun generateVariations(text: String): List<String> {
        val cleaned = text.uppercase().trim()
        val corrected = correctOcrErrors(cleaned)
        val noNumbers = cleaned.replace(Regex("[0-9]"), "").trim()
        val lastTwoWords = cleaned.split("\\s+".toRegex()).takeLast(2).joinToString(" ")
        return listOf(cleaned, corrected, noNumbers, lastTwoWords)
            .filter { it.isNotBlank() }
            .distinct()
    }
}
