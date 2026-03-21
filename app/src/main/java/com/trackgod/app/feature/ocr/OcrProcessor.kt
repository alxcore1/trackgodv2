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

        // 1. Run ML Kit
        val image = InputImage.fromBitmap(bitmap, 0)
        val visionText = recognizer.process(image).await()

        // 2. Extract all text blocks into a single string
        val rawText = visionText.textBlocks
            .joinToString(" ") { it.text }
            .trim()

        // 3. Clean and normalize
        val cleanedText = rawText
            .replace(Regex("[^A-Za-z0-9 ]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

        // 4. Find matches
        val matches = if (cleanedText.isNotBlank()) {
            findMatches(cleanedText)
        } else {
            emptyList()
        }

        // 5. Determine confidence
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
            cleanedText = cleanedText,
            matches = matches,
            confidence = confidence,
            processingTimeMs = processingTimeMs,
        )
    }

    /**
     * Compare [text] variations against every active exercise name.
     * Returns top 5 matches with similarity > 0.4, sorted descending.
     */
    private suspend fun findMatches(text: String): List<OcrMatch> {
        val exercises = exerciseRepository.getAllActiveSnapshot()
        val variations = StringSimilarity.generateVariations(text)

        val scored = exercises.map { exercise ->
            // Best score across all text variations, also checking
            // against alternative names when available.
            val targets = buildList {
                add(exercise.name)
                exercise.alternativeNames
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    ?.let { addAll(it) }
            }

            val bestSimilarity = variations.maxOf { variation ->
                targets.maxOf { target ->
                    StringSimilarity.similarity(variation, target)
                }
            }

            OcrMatch(
                exercise = exercise,
                similarity = bestSimilarity,
            )
        }

        return scored
            .filter { it.similarity > 0.4f }
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
