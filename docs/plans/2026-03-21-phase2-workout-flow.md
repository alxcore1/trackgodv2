# Phase 2: Core Workout Flow -- Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** A user can start a workout, pick exercises from a seeded database, log sets with smart defaults, use rest timer, switch exercises, edit any set, and finish with auto-generated name.

**Architecture:** Repository layer between DAOs and ViewModels. Hilt-injected ViewModels. Session persistence via SharedPreferences for active workout ID.

**Tech Stack:** Room DAOs (already created), Hilt ViewModels, Compose screens, SharedPreferences for session flag.

**Parallel Execution:** Task 1 must complete first (seeded data needed). Tasks 2-3 can partially overlap. Tasks 4-5 are sequential.

---

### Task 1: Repository Layer + Exercise Seeding

**Goal:** Repository classes wrapping DAOs, plus a bundled JSON file with exercises that gets seeded on first launch.

**Files to Create:**
```
app/src/main/java/com/trackgod/app/core/repository/
  ExerciseRepository.kt
  WorkoutRepository.kt
  SettingsRepository.kt
app/src/main/assets/
  exercises_seed.json
app/src/main/java/com/trackgod/app/core/database/
  SeedDatabase.kt
```

**ExerciseRepository:** Wraps ExerciseDao. Methods: getAllActive(), getByCategory(), search(), getById(), create(), incrementUsage(). Maps Entity to domain if needed, or pass entities directly for now.

**WorkoutRepository:** Wraps WorkoutDao + SetDao + ExerciseDao. Methods: createWorkout() returns ID, addSet(), updateSet(), deleteSet(), getSetsForWorkout(), getRecentSetsForExercise() (for smart defaults), completeWorkout() (calculates total volume, duration, sets isCompleted=true), getIncompleteWorkout(), deleteWorkout(). This is the critical repository -- it orchestrates workout + sets together.

**SettingsRepository:** Wraps SharedPreferences. Methods: getActiveWorkoutId()/setActiveWorkoutId()/clearActiveWorkoutId(), getRestTimerDuration()/setRestTimerDuration(), isRestTimerAutoStart()/setRestTimerAutoStart(), isRestTimerEnabled()/setRestTimerEnabled(), showRpe()/showRir().

**exercises_seed.json:** Bundled JSON with ~80 common exercises (the "Basics" set). Structure:
```json
[
  {"name": "Barbell Bench Press", "category": "Chest", "equipmentType": "barbell"},
  {"name": "Incline Dumbbell Press", "category": "Chest", "equipmentType": "dumbbell"},
  {"name": "Cable Fly", "category": "Chest", "equipmentType": "cable"},
  {"name": "Barbell Back Squat", "category": "Legs", "equipmentType": "barbell"},
  ...
]
```
Include 10-15 per category (Chest, Back, Shoulders, Arms, Legs, Core). Mix of barbell, dumbbell, machine, cable, bodyweight.

**SeedDatabase.kt:** Reads exercises_seed.json from assets, parses, inserts via ExerciseDao.insertAll(). Called from a Hilt-provided initializer or the splash ViewModel. Only seeds if exercise count is 0 (first launch).

**Hilt DI:** Create RepositoryModule providing all repositories.

Build and commit.

---

### Task 2: Exercise Picker Screen

**Goal:** Full-screen exercise picker with search, category filter chips, usage-sorted list, and add custom exercise.

**Files to Create:**
```
app/src/main/java/com/trackgod/app/feature/workout/picker/
  ExercisePickerScreen.kt
  ExercisePickerViewModel.kt
  AddExerciseDialog.kt
```

**ExercisePickerViewModel:**
- State: query, selectedCategory (null = All), exercises list, isLoading
- On init: load all active exercises
- On category change: filter by category
- On search: filter by query
- Events: SelectExercise, Search, FilterCategory, AddCustomExercise

**ExercisePickerScreen:**
- Header: fitness icon + "SELECT WEAPON" title + close button (X)
- Search bar with red left border: "SEARCH EXERCISES..."
- Category filter chips: ALL, CHEST, BACK, SHOULDERS, ARMS, LEGS, CORE (horizontal scroll)
- Exercise list: each item shows name (titleMedium, uppercase) + category/equipment (labelMedium, tertiary). Left border on hover. Tap to select.
- Bottom: "+ ADD CUSTOM" button
- Sorted by usage count (most-used first)

**AddExerciseDialog:**
- Dialog/BottomSheet with fields: Name, Category (dropdown/chips), Equipment Type (dropdown/chips)
- Save creates exercise in DB and selects it

**Navigation:** Called from WorkoutSession. Returns selected exercise via savedStateHandle or callback.

Wire into TrackGodNavHost as a composable destination.

Build and commit.

---

### Task 3: Workout Session Screen

**Goal:** The core workout tracking screen where users log sets with smart defaults.

**Files to Create:**
```
app/src/main/java/com/trackgod/app/feature/workout/session/
  WorkoutSessionScreen.kt
  WorkoutSessionViewModel.kt
  WorkoutCompleteDialog.kt
  RestTimerManager.kt
```

**WorkoutSessionViewModel:**
State:
- workout: WorkoutEntity? (the active workout)
- currentExercise: ExerciseEntity? (selected exercise)
- completedSets: List<SetEntity> (sets for current exercise in this workout)
- allExercisesInSession: List<ExerciseEntity> (all exercises used so far)
- weightInput, repsInput, noteInput: String
- lastSessionWeight, lastSessionReps: Float/Int (smart defaults from previous workout)
- isRestTimerRunning, restTimeRemaining: Int
- sessionDuration: Long (elapsed seconds since start)
- isLoading, error

Events:
- StartWorkout -> creates WorkoutEntity, saves ID to SharedPreferences
- SelectExercise(exercise) -> sets current exercise, loads smart defaults from getRecentForExercise()
- UpdateWeight/UpdateReps/IncrementWeight/IncrementReps
- LogSet -> inserts SetEntity, increments exercise usage, triggers rest timer
- EditSet(setId, weight, reps, note) -> updates in DB
- DeleteSet(setId) -> removes from DB
- SkipRestTimer
- SwitchExercise -> navigate to picker
- FinishWorkout -> shows completion dialog
- ConfirmFinish(name) -> calculates totals, marks complete, clears SharedPreferences
- DiscardWorkout -> deletes workout + sets, clears SharedPreferences

**WorkoutSessionScreen layout:**

When NO exercise selected:
- Header: dumbbell icon + "WORKOUT" + "LIVE" pulsing indicator
- Stats panel: 4-column grid (Exercises, Sets, Volume, Time)
- PAUSE / END buttons
- Section divider: "SELECT AN EXERCISE"
- Two large tile buttons: "+ CHOOSE EXERCISE" and "+ CHOOSE MACHINE" (both go to picker, machine filters equipment_type=machine)

When exercise IS selected:
- Same header + stats panel
- Exercise name (headlineMedium) + category/equipment (labelMedium)
- Weight input: NumberInput with smart default pre-filled
- Reps input: NumberInput with smart default pre-filled
- Optional note: TrackGodTextField
- "LOG SET" primary button
- Completed sets list below (scrollable), each tappable to edit
- "NEXT EXERCISE" button at bottom

**Smart Defaults:** When exercise is selected, query getRecentForExercise() to get last session's most recent set. Pre-fill weight and reps. Show hint "LAST: 80kg x 10" above inputs.

**Rest Timer (RestTimerManager.kt):**
- Manages countdown state
- Starts automatically after LogSet (if autoStart enabled)
- Configurable duration (default 90s, stored in SettingsRepository)
- Displays countdown in large text
- Skip button to dismiss
- Vibrates when complete (use Android Vibrator service)
- Can be disabled entirely via settings

**WorkoutCompleteDialog:**
- Shows summary: total exercises, total sets, total volume, duration
- Auto-generated workout name based on categories used
- Editable name field
- "SAVE" and "DISCARD" buttons

**Session Timer:** A coroutine that ticks every second, tracking elapsed time from workout start.

Wire WorkoutSession into navigation. The Altar's "START NEW WORKOUT" button should create a workout and navigate to the session.

Build and commit.

---

### Task 4: Navigation Wiring + Altar Integration

**Goal:** Wire everything together: Altar starts workouts, picker returns exercises to session, completion returns to Altar.

**Files to Modify:**
```
app/src/main/java/com/trackgod/app/ui/navigation/TrackGodNavHost.kt
app/src/main/java/com/trackgod/app/feature/altar/AltarScreen.kt
app/src/main/java/com/trackgod/app/feature/altar/AltarViewModel.kt (create)
app/src/main/java/com/trackgod/app/feature/splash/SplashScreen.kt (or SplashViewModel)
```

**NavHost updates:**
- Add WorkoutSession composable with workoutId argument
- Add ExercisePicker composable
- Handle exercise picker result (return selected exercise to session via savedStateHandle)

**AltarScreen update:**
- Replace placeholder with real (but simple) dashboard
- Show today's stats if any workouts exist (from WorkoutRepository)
- "START NEW WORKOUT" button creates a workout and navigates to session

**AltarViewModel:**
- Load today's workout count, total volume, total sets
- Check for incomplete workout on init (offer resume)

**Splash update:**
- Seed database on first launch (call SeedDatabase)
- Check for incomplete workout (offer resume vs start fresh)

Build, install, and test the full flow: splash → seed → altar → start workout → pick exercise → log sets → finish → back to altar.

Commit.

---

### Task 5: Session Persistence + Polish

**Goal:** Workout survives app kill. Resume prompt on restart. Edit/delete sets works. Visual polish.

**Files to Modify:**
- WorkoutSessionViewModel.kt (add persistence)
- TrackGodNavHost.kt (add resume flow)
- AltarScreen.kt (show resume banner if incomplete workout exists)

**Session Persistence:**
- On StartWorkout: save workout ID to SharedPreferences
- On ConfirmFinish/Discard: clear SharedPreferences
- On app launch (AltarViewModel.init): check SharedPreferences for active workout ID
- If found: show banner "UNFINISHED RITUAL" with Resume/Discard options
- Resume: navigate to WorkoutSession with that ID
- Discard: delete workout + sets, clear prefs

**Edit Set Flow:**
- Tap a completed set → inline edit mode (weight/reps fields become editable)
- Save/Cancel buttons appear
- Save calls WorkoutRepository.updateSet()

**Delete Set Flow:**
- Long-press or swipe a set → confirmation dialog
- Confirm deletes via WorkoutRepository.deleteSet()

Build, install, test the full flow including kill-and-resume. Commit.

---
