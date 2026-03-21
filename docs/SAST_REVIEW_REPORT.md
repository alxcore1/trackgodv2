# TrackGod v2 -- SAST Review Report

> Date: 2026-03-21 | Agents: 20 | Files Reviewed: 97 Kotlin files
> Method: User-perspective backwards testing -- every button tap traced through code

---

## Executive Summary

20 parallel agents traced every user flow from UI interaction through ViewModel, Repository, DAO, and back. **The app is structurally sound** with good reactive patterns, proper Room usage, and correct navigation. However, **3 critical bugs, 6 high-priority issues, and 10 medium/low issues** were found.

---

## CRITICAL BUGS (Must Fix)

### BUG-001: Duplicate Workout Creation
**Severity:** CRITICAL | **Source:** SAST 3
**File:** `WorkoutSessionViewModel.kt:81-95`
**Issue:** ViewModel reads `savedStateHandle.get<Long>("workoutId")` which returns null because NavHost passes workoutId as a composable route argument, not via savedStateHandle. Fallback creates a SECOND workout via `workoutRepository.createWorkout()`.
**Impact:** Every workout creates 2 WorkoutEntity records in the database.
**Fix:** Use the `workoutId` parameter passed to the Screen composable, or set it in savedStateHandle from NavHost before navigation.

### BUG-002: Rest Timer Vibration Not Implemented
**Severity:** CRITICAL | **Source:** SAST 4
**File:** `WorkoutSessionViewModel.kt:296`
**Issue:** Rest timer `onComplete` callback is an empty lambda: `onComplete = { /* vibration trigger handled at UI layer */ }`. No vibration code exists at UI layer either.
**Impact:** Timer completes silently. User gets no feedback that rest period is over.
**Fix:** Add Vibrator service call in onComplete callback, or use haptic feedback in the Composable.

### BUG-003: Weight Loss Goal Allows Invalid Configuration
**Severity:** CRITICAL | **Source:** SAST 13
**File:** `GoalSetupSheet.kt:170-184`, `WeightLossViewModel.kt:220-237`
**Issue:** No validation that startingWeight > targetWeight. Allows weight gain "goals" that break: progress % calculation (negative), milestone achievement (always fails), and weightRemaining (always 0 due to coerceAtLeast).
**Impact:** Entire weight loss feature broken for invalid goal configurations.
**Fix:** Validate startingWeight > targetWeight in GoalSetupSheet before save.

---

## HIGH PRIORITY (Should Fix Before Release)

### HIGH-001: No Error Handling in Database Seeding
**Source:** SAST 1 | **File:** `SeedingChoiceScreen.kt`, `SeedDatabase.kt`
**Issue:** All 3 seeding buttons launch coroutines without try-catch. JSONException from malformed asset file = user stuck on screen, buttons disabled.
**Fix:** Wrap seeding in try-catch, show error message, allow retry.

### HIGH-002: No try-catch on completeWorkout()
**Source:** SAST 6 | **File:** `WorkoutSessionViewModel.kt:412-419`
**Issue:** If DB is locked during completeWorkout(), exception bubbles uncaught. clearActiveWorkoutId() never runs = orphaned incomplete workout on next launch.
**Fix:** Wrap in try-catch, show error state, allow retry.

### HIGH-003: EditProfile Doesn't Sync Units to Settings
**Source:** SAST 11 | **File:** `EditProfileViewModel.kt`
**Issue:** Changing units in EditProfile updates UserProfileEntity but does NOT call `settingsRepository.setWeightUnit()`. WeightLoss and other features read from SettingsRepository, not profile.
**Fix:** Add unit sync calls in EditProfileViewModel.save() like OnboardingViewModel does.

### HIGH-004: No Max Weight/Reps Limits
**Source:** SAST 4 | **File:** `WorkoutSessionViewModel.kt:275`
**Issue:** Only validates > 0, no upper bound. User can enter 99,999kg. Volume calculations could overflow.
**Fix:** Add coerceAtMost(9999f) for weight, coerceAtMost(999) for reps.

### HIGH-005: Photo Deletion UI Missing
**Source:** SAST 14 | **File:** `ProgressPhotosSection.kt`
**Issue:** `deletePhoto()` method exists in WeightLossViewModel but NO UI element calls it. Users cannot delete photos.
**Fix:** Add long-press on thumbnails to trigger delete with confirmation.

### HIGH-006: Invalid Photo URI Not Handled
**Source:** SAST 14 | **File:** Multiple AsyncImage calls
**Issue:** If a stored photo URI becomes invalid (file deleted), AsyncImage silently shows nothing. No error feedback or cleanup.
**Fix:** Add error callback to AsyncImage with fallback UI and option to remove broken entry.

---

## MEDIUM PRIORITY

### MED-001: Zero Sets Allowed to Complete Workout
**Source:** SAST 6 | **File:** `WorkoutCompleteDialog.kt:163`
**Issue:** SAVE button only checks name is non-blank, not that sets > 0.
**Fix:** Add `enabled = workoutName.isNotBlank() && totalSets > 0`

### MED-002: Empty Exercise DB Poor UX
**Source:** SAST 3 | **File:** `ExercisePickerScreen.kt:125`
**Issue:** "Empty Slate" user sees "No Matches Found" with no guidance. Should say "No exercises yet. Add one to begin."
**Fix:** Detect empty DB state, show contextual empty state message.

### MED-003: Heatmap Ignores Time Range
**Source:** SAST 10 | **File:** `StatsViewModel.kt:138-140`
**Issue:** Heatmap always shows 90 days regardless of selected time range.
**Fix:** Use startDate from selected range, or document this as intentional.

### MED-004: Per-Exercise Volume Not Shown in History
**Source:** SAST 8 | **File:** `WorkoutDetailSheet.kt`
**Issue:** Expanded workout detail shows individual sets but no exercise volume subtotal.
**Fix:** Calculate and display sum of weight*reps per exercise group.

### MED-005: Loading State Race on Altar
**Source:** SAST 2 | **File:** `AltarViewModel.kt`
**Issue:** Reactive observer doesn't clear isLoading. Non-reactive does. May cause brief spinner flash.
**Fix:** Set isLoading = false in both observers.

### MED-006: Rest Timer State Lost on Back/App Kill
**Source:** SAST 7
**Issue:** Active rest timer countdown lost if user backs out or app is killed.
**Fix:** Persist restTimeRemaining to SharedPreferences on pause/destroy.

---

## LOW PRIORITY

### LOW-001: Birthday Date Picker Crash Risk
**Source:** SAST 11 | **File:** `EditProfileScreen.kt:216-220`
**Issue:** `split("-")` + `toInt()` without try-catch. Corrupted birthday string crashes.

### LOW-002: Silent Validation Failures
**Source:** SAST 4 | **Issue:** logSet() returns silently on invalid input. No Toast/Snackbar.

### LOW-003: Streak Not Recalculated After Delete
**Source:** SAST 9 | **Issue:** Deleting a workout updates today's stats but streak only recalculates on screen reload.

### LOW-004: KB File Size Truncation
**Source:** SAST 15 | **File:** `BackupScreen.kt:446`
**Issue:** `${bytes / 1024} KB` uses integer division. 1536 bytes shows as "1 KB".

---

## VERIFIED WORKING (No Issues Found)

- OCR scanner full flow (camera, ML Kit, fuzzy match, manual entry) -- SAST 16
- Edit/delete sets (tap edit, long-press delete, confirmation, DB update, list refresh) -- SAST 5
- Session persistence (SharedPreferences, resume banner, discard, stale ID cleanup) -- SAST 7
- History search + date filter + expand/collapse -- SAST 8
- History rename/delete with cascading FK -- SAST 9
- Backup create/export/import/restore with safety backups -- SAST 15
- Levenshtein distance implementation -- SAST 16
- Splash profile detection and routing -- SAST 2
- Workout completion flow (summary, auto-name, volume calculation, backup trigger) -- SAST 6
- Exercise picker search + category filter combo -- SAST 3

---

## Architecture Strengths Noted

1. **Reactive patterns** -- Room Flow + collectAsStateWithLifecycle throughout
2. **Two-tier recovery** -- SharedPreferences + DB fallback for incomplete workouts
3. **Cascade FK** -- Deleting workouts auto-deletes sets
4. **Atomic persistence** -- Sets written to Room immediately, not batched
5. **Safety backups** -- Created before every destructive operation
6. **Three-layer input validation** -- UI regex + ViewModel regex + parse check
