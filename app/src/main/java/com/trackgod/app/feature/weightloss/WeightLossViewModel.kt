package com.trackgod.app.feature.weightloss

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackgod.app.core.database.dao.UserProfileDao
import com.trackgod.app.core.database.entity.BodyMetricEntity
import com.trackgod.app.core.database.entity.WeightLossGoalEntity
import com.trackgod.app.core.database.entity.WeightLossMilestoneEntity
import com.trackgod.app.core.repository.BodyMetricRepository
import com.trackgod.app.core.repository.SettingsRepository
import com.trackgod.app.core.repository.WeightLossRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class WeightLossState(
    val activeGoal: WeightLossGoalEntity? = null,
    val milestones: List<WeightLossMilestoneEntity> = emptyList(),
    val weightHistory: List<BodyMetricEntity> = emptyList(),
    val progressPhotos: List<BodyMetricEntity> = emptyList(),
    val currentWeight: Float? = null,
    val progressPercent: Float = 0f,
    val weightRemaining: Float = 0f,
    val daysRemaining: Int = 0,
    val bmr: Float? = null,
    val tdee: Float? = null,
    val weightUnit: String = "kg",
    val showGoalSetup: Boolean = false,
    val showWeighIn: Boolean = false,
    val showMilestone: Boolean = false,
    val isLoading: Boolean = true,
)

@HiltViewModel
class WeightLossViewModel @Inject constructor(
    private val weightLossRepository: WeightLossRepository,
    private val bodyMetricRepository: BodyMetricRepository,
    private val userProfileDao: UserProfileDao,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(WeightLossState())
    val state: StateFlow<WeightLossState> = _state.asStateFlow()

    init {
        loadWeightUnit()
        loadActiveGoal()
        loadWeightHistory()
        loadProgressPhotos()
        loadLatestWeight()
        loadBmrTdee()
    }

    private fun loadWeightUnit() {
        val unit = settingsRepository.getWeightUnit()
        _state.value = _state.value.copy(weightUnit = unit)
    }

    private fun loadActiveGoal() {
        viewModelScope.launch {
            weightLossRepository.getActiveGoal().collect { goal ->
                _state.value = _state.value.copy(
                    activeGoal = goal,
                    isLoading = false,
                )
                recalculateProgress()
                if (goal != null) {
                    loadMilestones(goal.id)
                }
            }
        }
    }

    private fun loadMilestones(goalId: Long) {
        viewModelScope.launch {
            weightLossRepository.getMilestones(goalId).collect { milestones ->
                _state.value = _state.value.copy(milestones = milestones)
            }
        }
    }

    private fun loadWeightHistory() {
        viewModelScope.launch {
            bodyMetricRepository.getWeightHistory(30).collect { history ->
                _state.value = _state.value.copy(weightHistory = history)
            }
        }
    }

    private fun loadProgressPhotos() {
        viewModelScope.launch {
            bodyMetricRepository.getProgressPhotos(20).collect { photos ->
                _state.value = _state.value.copy(progressPhotos = photos)
            }
        }
    }

    private fun loadLatestWeight() {
        viewModelScope.launch {
            val latest = bodyMetricRepository.getLatest()
            _state.value = _state.value.copy(currentWeight = latest?.weight)
            recalculateProgress()
        }
    }

    private fun loadBmrTdee() {
        viewModelScope.launch {
            val profile = userProfileDao.getProfileOnce() ?: return@launch
            val weight = profile.weight ?: return@launch
            val height = profile.height ?: return@launch
            val birthday = profile.birthday ?: return@launch
            val gender = profile.gender ?: "male"

            val age = calculateAge(birthday)
            if (age <= 0) return@launch

            // Mifflin-St Jeor equation
            val bmr = if (gender.equals("male", ignoreCase = true)) {
                10f * weight + 6.25f * height - 5f * age - 5f
            } else {
                10f * weight + 6.25f * height - 5f * age + 161f
            }

            // TDEE with moderate activity multiplier (1.55)
            val tdee = bmr * 1.55f

            _state.value = _state.value.copy(
                bmr = bmr,
                tdee = tdee,
            )
        }
    }

    private fun recalculateProgress() {
        val goal = _state.value.activeGoal ?: return
        val current = _state.value.currentWeight ?: return

        val totalToLose = goal.startingWeight - goal.targetWeight
        val lost = goal.startingWeight - current

        val progressPercent = if (totalToLose > 0f) {
            ((lost / totalToLose) * 100f).coerceIn(0f, 100f)
        } else {
            0f
        }

        val weightRemaining = (current - goal.targetWeight).coerceAtLeast(0f)
        val daysRemaining = calculateDaysRemaining(goal.targetDate)

        _state.value = _state.value.copy(
            progressPercent = progressPercent,
            weightRemaining = weightRemaining,
            daysRemaining = daysRemaining,
        )
    }

    private fun calculateDaysRemaining(targetDate: String): Int {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val target = dateFormat.parse(targetDate) ?: return 0
            val now = Date()
            val diff = target.time - now.time
            if (diff < 0) 0 else TimeUnit.MILLISECONDS.toDays(diff).toInt()
        } catch (e: Exception) {
            0
        }
    }

    private fun calculateAge(birthday: String): Int {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val birthDate = dateFormat.parse(birthday) ?: return 0
            val birthCal = Calendar.getInstance().apply { time = birthDate }
            val nowCal = Calendar.getInstance()

            var age = nowCal.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
            if (nowCal.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            age
        } catch (e: Exception) {
            0
        }
    }

    // -- Actions --

    fun showGoalSetup() {
        _state.value = _state.value.copy(showGoalSetup = true)
    }

    fun dismissGoalSetup() {
        _state.value = _state.value.copy(showGoalSetup = false)
    }

    fun showWeighIn() {
        _state.value = _state.value.copy(showWeighIn = true)
    }

    fun dismissWeighIn() {
        _state.value = _state.value.copy(showWeighIn = false)
    }

    fun showMilestone() {
        _state.value = _state.value.copy(showMilestone = true)
    }

    fun dismissMilestone() {
        _state.value = _state.value.copy(showMilestone = false)
    }

    fun saveGoal(
        startWeight: Float,
        targetWeight: Float,
        targetDate: String,
        weeklyGoal: Float?,
        motivation: String?,
    ) {
        viewModelScope.launch {
            weightLossRepository.createGoal(
                startWeight = startWeight,
                targetWeight = targetWeight,
                targetDate = targetDate,
                weeklyGoal = weeklyGoal,
                motivation = motivation,
            )
            _state.value = _state.value.copy(showGoalSetup = false)
        }
    }

    fun logWeighIn(weight: Float, note: String?, photoUri: String?) {
        viewModelScope.launch {
            bodyMetricRepository.logWeighIn(weight, note, photoUri)
            // Refresh latest weight
            val latest = bodyMetricRepository.getLatest()
            _state.value = _state.value.copy(
                currentWeight = latest?.weight,
                showWeighIn = false,
            )
            recalculateProgress()
            checkMilestones(weight)
        }
    }

    private suspend fun checkMilestones(currentWeight: Float) {
        val milestones = _state.value.milestones
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())

        milestones.forEach { milestone ->
            if (!milestone.isAchieved && currentWeight <= milestone.targetWeight) {
                val updated = milestone.copy(
                    isAchieved = true,
                    achievedDate = today,
                )
                weightLossRepository.updateMilestone(updated)
            }
        }
    }

    fun saveMilestone(targetWeight: Float, description: String?) {
        val goalId = _state.value.activeGoal?.id ?: return
        viewModelScope.launch {
            weightLossRepository.createMilestone(
                goalId = goalId,
                targetWeight = targetWeight,
                description = description,
            )
            _state.value = _state.value.copy(showMilestone = false)
        }
    }

    fun deleteMilestone(milestone: WeightLossMilestoneEntity) {
        viewModelScope.launch {
            weightLossRepository.deleteMilestone(milestone)
        }
    }

    /**
     * Save a progress photo as a body-metric entry (no weight, just the URI + date).
     */
    fun addProgressPhoto(uri: String) {
        viewModelScope.launch {
            bodyMetricRepository.addProgressPhoto(uri)
        }
    }

    /**
     * Delete a body-metric entry (e.g. a progress photo).
     */
    fun deletePhoto(metricId: Long) {
        viewModelScope.launch {
            val photos = _state.value.progressPhotos
            val target = photos.find { it.id == metricId }
            if (target != null) {
                bodyMetricRepository.deleteMetric(target)
            }
        }
    }
}
