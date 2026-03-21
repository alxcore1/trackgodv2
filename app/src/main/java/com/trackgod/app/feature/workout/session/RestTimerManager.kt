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

    private val _timeRemaining = MutableStateFlow(0)
    val timeRemaining: StateFlow<Int> = _timeRemaining.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

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

        countdownJob = scope.launch {
            var remaining = durationSeconds
            while (remaining > 0) {
                delay(1_000L)
                remaining--
                _timeRemaining.value = remaining
            }
            _isRunning.value = false
            onComplete()
        }
    }

    /** Cancel the current countdown and reset to 0. */
    fun skip() {
        countdownJob?.cancel()
        countdownJob = null
        _timeRemaining.value = 0
        _isRunning.value = false
    }

    /** Stop the timer without resetting (used for cleanup). */
    fun stop() {
        countdownJob?.cancel()
        countdownJob = null
        _isRunning.value = false
    }
}
