package com.trackgod.app.feature.ocr

import android.graphics.Bitmap
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.trackgod.app.core.database.entity.ExerciseEntity
import com.trackgod.app.core.repository.ExerciseRepository
import com.trackgod.app.core.util.StringSimilarity
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

// ── Result types ────────────────────────────────────────────────────────────

data class OcrResult(
    val rawText: String,
    val cleanedText: String,
    val matches: List<OcrMatch>,
    val confidence: OcrConfidence,
    val processingTimeMs: Long,
)

data class OcrMatch(
    val exercise: ExerciseEntity,
    val similarity: Float, // 0.0 to 1.0
)

enum class OcrConfidence {
    HIGH,   // best match > 0.8
    MEDIUM, // best match 0.6 - 0.8
    LOW,    // best match 0.4 - 0.6
    NONE,   // no match above 0.4
}

// ── Processor ───────────────────────────────────────────────────────────────

@Singleton
class OcrProcessor @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
) {
    private val recognizer: TextRecognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Run ML Kit text recognition on [bitmap], then fuzzy-match the detected
     * text against all active exercises in the database.
     */
    suspend fun processImage(bitmap: Bitmap): OcrResult {
        val startTime = System.currentTimeMillis()

        // 1. Downscale only (ML Kit handles contrast/normalization well on its own)
        val scaled = try {
            if (bitmap.width > 1920) {
                val ratio = 1920f / bitmap.width
                val scaledHeight = (bitmap.height * ratio).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(bitmap, 1920, scaledHeight, true)
            } else {
                bitmap
            }
        } catch (e: Exception) {
            android.util.Log.e("OcrProcessor", "Bitmap scaling failed", e)
            return OcrResult(rawText = "", cleanedText = "", matches = emptyList(), confidence = OcrConfidence.NONE, processingTimeMs = 0L)
        }

        // 2. Run ML Kit on clean image (no grayscale/contrast - it hurts more than helps)
        val image = try {
            InputImage.fromBitmap(scaled, 0)
        } catch (e: Exception) {
            android.util.Log.e("OcrProcessor", "InputImage creation failed", e)
            return OcrResult(rawText = "", cleanedText = "", matches = emptyList(), confidence = OcrConfidence.NONE, processingTimeMs = 0L)
        }
        val visionText = try {
            recognizer.process(image).await()
        } catch (e: Exception) {
            android.util.Log.e("OcrProcessor", "ML Kit recognition failed", e)
            return OcrResult(rawText = "", cleanedText = "", matches = emptyList(), confidence = OcrConfidence.NONE, processingTimeMs = 0L)
        }

        // 3. Extract text blocks, sorted by size (largest first = most prominent)
        val textBlocks = visionText.textBlocks
            .sortedByDescending { block ->
                val box = block.boundingBox
                if (box != null) box.width() * box.height() else 0
            }

        val rawText = textBlocks.joinToString(" ") { it.text }.trim()

        // 4. Clean and normalize - try both full text and individual blocks
        val cleanedFull = rawText
            .replace(Regex("[^A-Za-z0-9 ]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

        // Also try the largest text block individually (often the machine name)
        val largestBlock = textBlocks.firstOrNull()?.let {
            it.text.replace(Regex("[^A-Za-z0-9 ]"), "").replace(Regex("\\s+"), " ").trim()
        }

        // 5. Find matches against full text and largest block
        val allMatches = mutableListOf<OcrMatch>()
        if (cleanedFull.isNotBlank()) {
            allMatches.addAll(findMatches(cleanedFull))
        }
        if (largestBlock != null && largestBlock != cleanedFull && largestBlock.length >= 5) {
            allMatches.addAll(findMatches(largestBlock))
        }

        // Debug logging for top matches
        allMatches.sortedByDescending { it.similarity }.take(3).forEach {
            android.util.Log.d("OCR_MATCH", "Exercise='${it.exercise.name}' score=${it.similarity} from text='$cleanedFull'")
        }

        // Deduplicate by exercise ID, keeping highest similarity
        val matches = allMatches
            .groupBy { it.exercise.id }
            .map { (_, group) -> group.maxByOrNull { it.similarity }!! }
            .sortedByDescending { it.similarity }
            .take(5)

        // 6. Determine confidence
        val bestScore = matches.firstOrNull()?.similarity ?: 0f
        val confidence = when {
            bestScore > 0.8f -> OcrConfidence.HIGH
            bestScore > 0.6f -> OcrConfidence.MEDIUM
            bestScore > 0.4f -> OcrConfidence.LOW
            else -> OcrConfidence.NONE
        }

        val processingTimeMs = System.currentTimeMillis() - startTime

        return OcrResult(
            rawText = rawText,
            cleanedText = cleanedFull,
            matches = matches,
            confidence = confidence,
            processingTimeMs = processingTimeMs,
        )
    }

    /**
     * Compare [text] variations against every active exercise name.
     * Returns top 5 matches with similarity > 0.55, sorted descending.
     * Requires at least one word overlap to avoid false positives.
     */
    private suspend fun findMatches(text: String): List<OcrMatch> {
        val exercises = exerciseRepository.getAllActiveSnapshot()
        val variations = StringSimilarity.generateVariations(text)
        val ocrWords = text.uppercase().split("\\s+".toRegex()).filter { it.length >= 3 }.toSet()

        val scored = exercises.mapNotNull { exercise ->
            val targets = buildList {
                add(exercise.name)
                exercise.alternativeNames
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    ?.let { addAll(it) }
            }

            // Require at least one fuzzy word overlap to avoid nonsense matches
            // (e.g., "RCERSPRES" should match "PRESS" via fuzzy)
            val exerciseWords = targets.flatMap {
                it.uppercase().split("\\s+".toRegex()).filter { w -> w.length >= 3 }
            }.toSet()
            val hasWordOverlap = ocrWords.any { ocrWord ->
                exerciseWords.any { exWord ->
                    ocrWord == exWord ||
                    ocrWord.contains(exWord) || exWord.contains(ocrWord) ||
                    StringSimilarity.levenshteinSimilarity(ocrWord, exWord) > 0.6f
                }
            }

            val bestSimilarity = variations.maxOf { variation ->
                targets.maxOf { target ->
                    StringSimilarity.similarity(variation, target)
                }
            }

            // Only include if there's word overlap OR very high Levenshtein score
            if (hasWordOverlap || bestSimilarity > 0.75f) {
                OcrMatch(exercise = exercise, similarity = bestSimilarity)
            } else {
                null
            }
        }

        return scored
            .filter { it.similarity > 0.55f }
            .sortedByDescending { it.similarity }
            .take(5)
    }
}

// ── Task.await() ────────────────────────────────────────────────────────────

/**
 * Suspend wrapper for Google [Task] so it plays nicely with coroutines.
 */
private suspend fun <T> Task<T>.await(): T = suspendCoroutine { cont ->
    addOnSuccessListener { result -> cont.resume(result) }
    addOnFailureListener { exception -> cont.resumeWithException(exception) }
}
