package com.trackgod.app.feature.ocr

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
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

        // 1. Preprocess image for better OCR on metallic gym machine labels
        val preprocessed = preprocessForOcr(bitmap)

        // 2. Run ML Kit
        val image = InputImage.fromBitmap(preprocessed, 0)
        val visionText = recognizer.process(image).await()

        // 3. Extract all text blocks into a single string
        val rawText = visionText.textBlocks
            .joinToString(" ") { it.text }
            .trim()

        // 4. Clean and normalize
        val cleanedText = rawText
            .replace(Regex("[^A-Za-z0-9 ]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

        // 5. Find matches
        val matches = if (cleanedText.isNotBlank()) {
            findMatches(cleanedText)
        } else {
            emptyList()
        }

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
            cleanedText = cleanedText,
            matches = matches,
            confidence = confidence,
            processingTimeMs = processingTimeMs,
        )
    }

    /**
     * Preprocess captured image for optimal OCR on gym machine labels.
     * Converts to grayscale, boosts contrast, and downscales for faster processing.
     */
    private fun preprocessForOcr(bitmap: Bitmap): Bitmap {
        // Downscale to max 1280px wide for faster ML Kit processing
        val scaled = if (bitmap.width > 1280) {
            val ratio = 1280f / bitmap.width
            Bitmap.createScaledBitmap(
                bitmap,
                1280,
                (bitmap.height * ratio).toInt(),
                true,
            )
        } else {
            bitmap
        }

        // Convert to grayscale (removes color noise from gym lighting)
        val grayscale = toGrayscale(scaled)

        // Boost contrast (makes embossed/metallic text stand out)
        return adjustContrast(grayscale, factor = 1.8f)
    }

    private fun toGrayscale(src: Bitmap): Bitmap {
        val colorMatrix = ColorMatrix().apply { setSaturation(0f) }
        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(colorMatrix) }
        val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        Canvas(result).drawBitmap(src, 0f, 0f, paint)
        return result
    }

    private fun adjustContrast(src: Bitmap, factor: Float): Bitmap {
        val translate = 128f * (1 - factor)
        val cm = ColorMatrix(
            floatArrayOf(
                factor, 0f, 0f, 0f, translate,
                0f, factor, 0f, 0f, translate,
                0f, 0f, factor, 0f, translate,
                0f, 0f, 0f, 1f, 0f,
            )
        )
        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(cm) }
        val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        Canvas(result).drawBitmap(src, 0f, 0f, paint)
        return result
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
