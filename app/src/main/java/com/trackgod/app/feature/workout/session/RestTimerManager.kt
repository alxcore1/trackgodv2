package com.trackgod.app.feature.workout.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages a countdown rest timer between sets.
 *
 * Ticks every second via [delay], emitting remaining seconds through [timeRemaining].
 * When the countdown reaches 0, [onComplete] is invoked (e.g. to trigger vibration).
 */
class RestTimerManager {

    private var countdownJob: Job? = null
    private var pausedRemaining: Int = 0
    private var activeScope: CoroutineScope? = null
    private var activeOnComplete: (() -> Unit)? = null

    private val _timeRemaining = MutableStateFlow(0)
    val timeRemaining: StateFlow<Int> = _timeRemaining.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    /**
     * Start a countdown from [durationSeconds] down to 0.
     *
     * Any previously running countdown is cancelled first.
     *
     * @param durationSeconds Total seconds to count down.
     * @param scope Coroutine scope for the timer job.
     * @param onComplete Called when the timer reaches 0.
     */
    fun start(durationSeconds: Int, scope: CoroutineScope, onComplete: () -> Unit) {
        countdownJob?.cancel()
        _timeRemaining.value = durationSeconds
        _isRunning.value = true
        _isPaused.value = false
        activeScope = scope
        activeOnComplete = onComplete

        startCountdown(durationSeconds, scope, onComplete)
    }

    private fun startCountdown(seconds: Int, scope: CoroutineScope, onComplete: () -> Unit) {
        countdownJob = scope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1_000L)
                remaining--
                _timeRemaining.value = remaining
            }
            _isRunning.value = false
            _isPaused.value = false
            onComplete()
        }
    }

    /** Pause the current countdown, preserving the remaining time. */
    fun pause() {
        if (_isRunning.value && !_isPaused.value) {
            pausedRemaining = _timeRemaining.value
            countdownJob?.cancel()
            countdownJob = null
            _isPaused.value = true
        }
    }

    /** Resume a paused countdown from where it left off. */
    fun resume() {
        val scope = activeScope ?: return
        val onComplete = activeOnComplete ?: return
        if (_isPaused.value && pausedRemaining > 0) {
            _isPaused.value = false
            startCountdown(pausedRemaining, scope, onComplete)
        }
    }

    /** Adjust the remaining time by [delta] seconds (can be negative). */
    fun adjustTime(delta: Int) {
        val newRemaining = (_timeRemaining.value + delta).coerceAtLeast(0)
        _timeRemaining.value = newRemaining

        // If running (not paused), restart the countdown from the new value
        if (_isRunning.value && !_isPaused.value) {
            val scope = activeScope ?: return
            val onComplete = activeOnComplete ?: return
            countdownJob?.cancel()
            startCountdown(newRemaining, scope, onComplete)
        } else if (_isPaused.value) {
            pausedRemaining = newRemaining
        }
    }

    /** Cancel the current countdown and reset to 0. */
    fun skip() {
        countdownJob?.cancel()
        countdownJob = null
        _timeRemaining.value = 0
        _isRunning.value = false
        _isPaused.value = false
    }

    /** Stop the timer without resetting (used for cleanup). */
    fun stop() {
        countdownJob?.cancel()
        countdownJob = null
        _isRunning.value = false
        _isPaused.value = false
    }
}
