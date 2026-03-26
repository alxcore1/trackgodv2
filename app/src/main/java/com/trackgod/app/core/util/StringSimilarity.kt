package com.trackgod.app.core.util

/**
 * String similarity utilities for fuzzy-matching OCR text against exercise names.
 *
 * Uses a multi-algorithm approach: Levenshtein distance, token-set matching,
 * token-sort matching, and substring containment for robust OCR matching.
 */
object StringSimilarity {

    // ── Known gym brand/series prefixes to strip before matching ────────────

    private val BRAND_PREFIXES = listOf(
        "HAMMER STRENGTH", "LIFE FITNESS", "CYBEX", "NAUTILUS",
        "PRECOR", "TECHNOGYM", "MATRIX", "HOIST", "BODY SOLID",
        "PARAMOUNT", "STAR TRAC", "FREEMOTION", "GYM80", "GYM 80",
        "GLUTEBUILDER", "GENERIC",
    )

    private val SERIES_PREFIXES = listOf(
        "MTS", "ISO-LATERAL", "ISO LATERAL", "ISOLATERAL",
        "INSIGNIA", "SIGNATURE", "SIGNATURE SERIES", "OPTIMA", "CIRCUIT",
        "AXIOM", "SELECT", "PLATE-LOADED", "PLATE LOADED",
        "HD ELITE", "HD", "SYGNUM", "PURE KRAFT",
    )

    // ── Common OCR misread corrections ──────────────────────────────────────

    private val OCR_CHAR_FIXES = mapOf(
        '0' to 'O', '1' to 'I', '5' to 'S', '8' to 'B',
        '6' to 'G', '2' to 'Z', '4' to 'A',
    )

    private val OCR_BRAND_FIXES = mapOf(
        "HANNER" to "HAMMER", "HANMER" to "HAMMER", "HAMMEP" to "HAMMER",
        "HAMNER" to "HAMMER", "HAMNMER" to "HAMMER",
        "TECHNOGYN" to "TECHNOGYM", "TECHNOGYH" to "TECHNOGYM",
        "MATR1X" to "MATRIX", "NATRIX" to "MATRIX",
        "CVBEX" to "CYBEX", "CYBBX" to "CYBEX",
        "PRECDR" to "PRECOR", "PREC0R" to "PRECOR",
        "L1FE" to "LIFE", "LJFE" to "LIFE", "IIFE" to "LIFE",
        "FITNES5" to "FITNESS", "FIINESS" to "FITNESS", "F1TNESS" to "FITNESS",
    )

    // ── Public API ──────────────────────────────────────────────────────────

    /**
     * Multi-algorithm similarity score between OCR text and an exercise name.
     * Returns the best score from Levenshtein, token-sort, token-set, and
     * substring matching. Scores range from 0.0 (no match) to 1.0 (identical).
     */
    fun similarity(ocrText: String, exerciseName: String): Float {
        val a = ocrText.uppercase().trim()
        val b = exerciseName.uppercase().trim()
        if (a.isEmpty() || b.isEmpty()) return 0f

        // Also try matching with brands stripped
        val aStripped = stripBrandsAndSeries(a)
        val bStripped = stripBrandsAndSeries(b)

        return maxOf(
            levenshteinSimilarity(a, b),
            levenshteinSimilarity(aStripped, bStripped),
            tokenSortRatio(a, b),
            tokenSortRatio(aStripped, bStripped),
            tokenSetRatio(a, b),
            tokenSetRatio(aStripped, bStripped),
            containsBonus(a, b),
            containsBonus(aStripped, bStripped),
        )
    }

    /**
     * Correct common OCR character and brand misreads.
     */
    fun correctOcrErrors(text: String): String {
        var result = text.uppercase()

        // Fix character-level misreads
        for ((digit, letter) in OCR_CHAR_FIXES) {
            result = result.replace(digit, letter)
        }

        // Fix brand-level misreads
        for ((wrong, correct) in OCR_BRAND_FIXES) {
            result = result.replace(wrong, correct)
        }

        return result
    }

    /**
     * Generate search variations from raw OCR text for fuzzy matching.
     * Returns distinct non-blank candidates ordered from most literal
     * to most transformed.
     */
    fun generateVariations(text: String): List<String> {
        val cleaned = text.uppercase().trim()
        val corrected = correctOcrErrors(cleaned)
        val stripped = stripBrandsAndSeries(corrected)
        val noNumbers = cleaned.replace(Regex("[0-9]"), "").replace(Regex("\\s+"), " ").trim()

        return listOf(cleaned, corrected, stripped, noNumbers)
            .map { it.replace(Regex("\\s+"), " ").trim() }
            .filter { it.length >= 3 }
            .distinct()
    }

    /**
     * Strip known gym brand and series prefixes from text.
     * "HAMMER STRENGTH MTS ISO-LATERAL FRONT LAT PULLDOWN" -> "FRONT LAT PULLDOWN"
     */
    fun stripBrandsAndSeries(text: String): String {
        var result = text.uppercase()
        // Strip brands first (longer names first to avoid partial matches)
        for (brand in BRAND_PREFIXES.sortedByDescending { it.length }) {
            result = result.replace(brand, "").trim()
        }
        // Then strip series
        for (series in SERIES_PREFIXES.sortedByDescending { it.length }) {
            // Only strip if it's at the start or preceded by space
            result = result.replaceFirst(Regex("^${Regex.escape(series)}\\b\\s*"), "")
            result = result.replaceFirst(Regex("\\s+${Regex.escape(series)}\\b"), "")
        }
        return result.replace(Regex("\\s+"), " ").trim()
    }

    // ── Algorithms ──────────────────────────────────────────────────────────

    /**
     * Standard Levenshtein distance.
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
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost,
                )
            }
        }
        return dp[m][n]
    }

    /**
     * Normalized Levenshtein similarity (0.0 to 1.0).
     */
    fun levenshteinSimilarity(s1: String, s2: String): Float {
        val a = s1.uppercase().trim()
        val b = s2.uppercase().trim()
        val maxLen = maxOf(a.length, b.length)
        if (maxLen == 0) return 1.0f
        return 1.0f - (levenshteinDistance(a, b).toFloat() / maxLen)
    }

    /**
     * Token Sort: sort words alphabetically, then Levenshtein compare.
     * Handles word reordering: "FRONT LAT PULLDOWN" vs "LAT PULLDOWN FRONT" score the same.
     */
    fun tokenSortRatio(s1: String, s2: String): Float {
        val sorted1 = s1.uppercase().split("\\s+".toRegex()).sorted().joinToString(" ")
        val sorted2 = s2.uppercase().split("\\s+".toRegex()).sorted().joinToString(" ")
        return levenshteinSimilarity(sorted1, sorted2)
    }

    /**
     * Token Set: find common tokens, then compare remainders.
     * Handles subset matching: "HAMMER STRENGTH MTS BICEPS CURL" vs "BICEPS CURL"
     * scores high because all tokens of the shorter string are in the longer one.
     */
    fun tokenSetRatio(s1: String, s2: String): Float {
        val tokens1 = s1.uppercase().split("\\s+".toRegex()).filter { it.isNotBlank() }.toSet()
        val tokens2 = s2.uppercase().split("\\s+".toRegex()).filter { it.isNotBlank() }.toSet()
        if (tokens1.isEmpty() || tokens2.isEmpty()) return 0f

        val common = tokens1.intersect(tokens2)
        if (common.isEmpty()) return 0f

        val commonStr = common.sorted().joinToString(" ")
        val rest1 = (tokens1 - common).sorted().joinToString(" ")
        val rest2 = (tokens2 - common).sorted().joinToString(" ")

        val combined1 = "$commonStr $rest1".trim()
        val combined2 = "$commonStr $rest2".trim()

        return maxOf(
            levenshteinSimilarity(commonStr, combined1),
            levenshteinSimilarity(commonStr, combined2),
            levenshteinSimilarity(combined1, combined2),
        )
    }

    /**
     * Bonus for exact substring containment.
     * If one string contains the other verbatim, return a high score.
     */
    fun containsBonus(s1: String, s2: String): Float {
        val a = s1.uppercase().trim()
        val b = s2.uppercase().trim()
        // Only award bonus if the contained string is meaningful (>= 5 chars)
        return when {
            a == b -> 1.0f
            a.contains(b) && b.length >= 5 -> 0.92f
            b.contains(a) && a.length >= 5 -> 0.88f
            else -> 0f
        }
    }
}
