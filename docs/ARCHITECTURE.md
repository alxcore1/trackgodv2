# TrackGod v2 -- Architecture & Implementation Plan

> Version: 1.0 | Date: 2026-03-21
> References: docs/PRD.md, PHASE_1_REPO_MAP.md

---

## 1. Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer                              │
│           Jetpack Compose + Material 3                  │
│    (Screens, Composables, Navigation, Theme)            │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                  ViewModel Layer                         │
│              (State, Events, Effects)                    │
│         Hilt-injected, Coroutines + Flow                │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                 Repository Layer                         │
│     (Single source of truth, data coordination)         │
│        Abstracts data sources from ViewModels           │
└──────┬─────────────────┬────────────────────┬───────────┘
       │                 │                    │
┌──────▼──────┐  ┌───────▼───────┐  ┌────────▼────────┐
│  Room DB    │  │  DataStore    │  │  File System    │
│  (SQLite)   │  │  (Settings)   │  │  (Photos,       │
│             │  │               │  │   Backups)      │
└─────────────┘  └───────────────┘  └─────────────────┘
```

**Pattern**: MVVM with Repository
**Single Activity**: One `MainActivity` hosting all Compose navigation
**Unidirectional Data Flow**: UI observes State via StateFlow, sends Events to ViewModel

---

## 2. Package Structure

```
com.trackgod.app/
│
├── TrackGodApplication.kt              # Hilt application entry
├── MainActivity.kt                     # Single activity, Compose host
│
├── core/
│   ├── database/
│   │   ├── TrackGodDatabase.kt         # Room database definition
│   │   ├── entity/                     # Room entities (@Entity)
│   │   │   ├── UserProfileEntity.kt
│   │   │   ├── ExerciseEntity.kt
│   │   │   ├── WorkoutEntity.kt
│   │   │   ├── SetEntity.kt
│   │   │   ├── BodyMetricEntity.kt
│   │   │   ├── WeightLossGoalEntity.kt
│   │   │   ├── WeightLossMilestoneEntity.kt
│   │   │   └── BackupMetadataEntity.kt
│   │   ├── dao/                        # Room DAOs
│   │   │   ├── UserProfileDao.kt
│   │   │   ├── ExerciseDao.kt
│   │   │   ├── WorkoutDao.kt
│   │   │   ├── SetDao.kt
│   │   │   ├── BodyMetricDao.kt
│   │   │   ├── WeightLossDao.kt
│   │   │   └── BackupDao.kt
│   │   ├── converter/                  # Room type converters
│   │   │   └── Converters.kt
│   │   └── migration/                  # Room migrations
│   │       └── Migrations.kt
│   │
│   ├── model/                          # Domain models (UI-facing)
│   │   ├── UserProfile.kt
│   │   ├── Exercise.kt
│   │   ├── Workout.kt
│   │   ├── WorkoutSet.kt
│   │   ├── BodyMetric.kt
│   │   ├── WeightLossGoal.kt
│   │   ├── WeightLossMilestone.kt
│   │   └── PersonalRecord.kt
│   │
│   ├── repository/                     # Data repositories
│   │   ├── UserRepository.kt
│   │   ├── ExerciseRepository.kt
│   │   ├── WorkoutRepository.kt
│   │   ├── BodyMetricRepository.kt
│   │   ├── WeightLossRepository.kt
│   │   ├── SettingsRepository.kt
│   │   └── BackupRepository.kt
│   │
│   ├── di/                             # Hilt dependency injection
│   │   ├── DatabaseModule.kt
│   │   ├── RepositoryModule.kt
│   │   └── ServiceModule.kt
│   │
│   └── util/
│       ├── DateUtils.kt                # Date formatting, locale
│       ├── UnitConverter.kt            # kg/lbs, cm/ft conversion
│       ├── FormulaUtils.kt             # 1RM (Epley), BMR, TDEE
│       ├── NumberFormatter.kt          # Volume abbreviations (1.2k, 2.5M)
│       └── StringSimilarity.kt         # Levenshtein distance for OCR
│
├── ui/
│   ├── theme/
│   │   ├── Theme.kt                    # TrackGod dark theme
│   │   ├── Color.kt                    # Color palette
│   │   ├── Typography.kt              # Industrial type system
│   │   ├── Shape.kt                    # Card shapes, borders
│   │   └── Spacing.kt                 # Spacing scale
│   │
│   ├── component/                      # Reusable composables
│   │   ├── TrackGodCard.kt             # Standard card with red accent
│   │   ├── TrackGodButton.kt           # Primary/secondary buttons
│   │   ├── TrackGodTextField.kt        # Styled text input
│   │   ├── NumberInput.kt              # Weight/reps with +/- buttons
│   │   ├── ConfirmDialog.kt            # Confirmation dialogs
│   │   ├── RestTimerDisplay.kt         # Rest timer countdown
│   │   ├── ProgressRing.kt             # Circular progress
│   │   ├── StatCard.kt                 # Metric display card
│   │   ├── EmptyState.kt               # Empty/error states
│   │   ├── SearchBar.kt                # Search input
│   │   ├── FilterChips.kt              # Muscle group filter chips
│   │   └── PhotoViewer.kt              # Full-screen photo viewing
│   │
│   └── navigation/
│       ├── TrackGodNavHost.kt          # Main navigation graph
│       ├── Screen.kt                   # Sealed class of all routes
│       └── BottomNavBar.kt             # 4-tab bottom navigation
│
├── feature/
│   ├── splash/
│   │   ├── SplashScreen.kt            # RAGE. RIP. REPEAT. + SYSTEM_INIT
│   │   └── SplashViewModel.kt         # DB init, integrity check, auto-recovery
│   │
│   ├── onboarding/
│   │   ├── OnboardingScreen.kt         # Multi-step profile setup
│   │   ├── OnboardingViewModel.kt
│   │   ├── SeedingChoiceScreen.kt      # Full/Basics/Empty database choice
│   │   └── V1ImportScreen.kt           # Import from TrackGod v1
│   │
│   ├── altar/
│   │   ├── AltarScreen.kt             # Dashboard
│   │   └── AltarViewModel.kt          # Today stats, weekly goal, recent workouts
│   │
│   ├── workout/
│   │   ├── session/
│   │   │   ├── WorkoutSessionScreen.kt # Active workout UI
│   │   │   ├── WorkoutSessionViewModel.kt
│   │   │   └── RestTimerManager.kt     # Timer logic + notification
│   │   ├── picker/
│   │   │   ├── ExercisePickerScreen.kt # Unified search/filter/select
│   │   │   └── ExercisePickerViewModel.kt
│   │   └── complete/
│   │       └── WorkoutCompleteDialog.kt # Summary + naming
│   │
│   ├── history/
│   │   ├── HistoryScreen.kt            # Workout list
│   │   ├── HistoryViewModel.kt
│   │   └── WorkoutDetailSheet.kt       # Expanded workout detail
│   │
│   ├── stats/
│   │   ├── StatsScreen.kt              # Analytics hub (tabs or scroll)
│   │   ├── StatsViewModel.kt
│   │   ├── chart/
│   │   │   ├── VolumeChart.kt          # Volume progression line chart
│   │   │   ├── MuscleGroupChart.kt     # Donut chart
│   │   │   ├── HeatmapChart.kt         # Training calendar
│   │   │   ├── ConsistencyChart.kt     # Workouts/week bar chart
│   │   │   └── BalanceChart.kt         # Strength balance visual
│   │   └── section/
│   │       ├── PersonalRecordsSection.kt
│   │       └── ExerciseFrequencySection.kt
│   │
│   ├── profile/
│   │   ├── ProfileScreen.kt            # Sectioned menu
│   │   ├── ProfileViewModel.kt
│   │   ├── EditProfileScreen.kt        # Edit all profile fields
│   │   └── SettingsScreen.kt           # All settings
│   │
│   ├── weightloss/
│   │   ├── WeightLossScreen.kt         # Journey hub
│   │   ├── WeightLossViewModel.kt
│   │   ├── WeighInSheet.kt             # Log weight + photo
│   │   ├── GoalSetupSheet.kt           # Create/edit goal
│   │   ├── MilestoneSheet.kt           # Create/edit milestone
│   │   ├── ProgressChart.kt            # Weight over time
│   │   └── PhotoComparisonScreen.kt    # Before/after slider
│   │
│   ├── backup/
│   │   ├── BackupScreen.kt             # Backup management UI
│   │   ├── BackupViewModel.kt
│   │   └── BackupWorker.kt             # WorkManager scheduled backup
│   │
│   └── ocr/
│       ├── OcrScannerScreen.kt         # CameraX preview + scan frame
│       ├── OcrViewModel.kt
│       └── OcrProcessor.kt             # ML Kit + fuzzy matching
│
└── service/
    ├── RestTimerService.kt              # Foreground service for rest timer
    ├── SessionPersistenceManager.kt     # Workout crash recovery
    └── DatabaseIntegrityManager.kt      # Startup checks, auto-recovery
```

---

## 3. Database Design (Room)

### 3.1 Entity Relationship Diagram

```
┌──────────────────┐
│   user_profile    │  (single row)
│──────────────────│
│ id (PK)          │
│ name             │
│ avatar_uri       │
│ gender           │
│ birthday         │
│ height           │
│ weight           │
│ primary_objective│
│ experience_level │
│ weekly_target    │
│ weight_unit      │  ← "kg" or "lbs"
│ height_unit      │  ← "cm" or "ft"
│ created_at       │
│ updated_at       │
└──────────────────┘

┌──────────────────┐
│    exercises      │  (unified: machines + exercises)
│──────────────────│
│ id (PK)          │
│ name             │  ← "Barbell Bench Press" or "Hammer Strength Chest Press"
│ category         │  ← "Chest", "Back", "Shoulders", "Arms", "Legs", "Core"
│ equipment_type   │  ← "barbell", "dumbbell", "machine", "cable", "bodyweight", "other"
│ brand            │  ← nullable ("Hammer Strength", "Life Fitness", null)
│ alternative_names│  ← nullable JSON array for OCR matching
│ is_custom        │  ← user-created vs pre-seeded
│ is_active        │  ← visible in picker (for seeding filter)
│ usage_count      │  ← auto-incremented on use, for sorting
│ last_used_at     │  ← timestamp of last use
│ created_at       │
└──────────────────┘

┌──────────────────┐       ┌──────────────────┐
│    workouts       │       │      sets         │
│──────────────────│       │──────────────────│
│ id (PK)          │──┐    │ id (PK)          │
│ name             │  │    │ workout_id (FK)──│───► workouts.id
│ date             │  │    │ exercise_id (FK)─│───► exercises.id
│ start_time       │  │    │ set_number       │
│ end_time         │  └────│ weight           │
│ duration_seconds │       │ reps             │
│ total_volume     │       │ rpe              │  ← nullable (1-10)
│ notes            │       │ rir              │  ← nullable (0-5)
│ is_completed     │       │ note             │
│ created_at       │       │ created_at       │
└──────────────────┘       └──────────────────┘

┌──────────────────┐       ┌──────────────────────┐
│   body_metrics    │       │  weight_loss_goals    │
│──────────────────│       │──────────────────────│
│ id (PK)          │       │ id (PK)              │
│ date             │       │ starting_weight      │
│ weight           │       │ target_weight        │
│ photo_uri        │       │ target_date          │
│ note             │       │ weekly_goal          │
│ created_at       │       │ motivation_text      │
│                  │       │ reminder_day         │
│                  │       │ reminder_time        │
│                  │       │ is_active            │
│                  │       │ created_at           │
│                  │       │ updated_at           │
│                  │       └──────────┬───────────┘
│                  │                  │
│                  │       ┌──────────▼───────────┐
│                  │       │weight_loss_milestones │
│                  │       │──────────────────────│
│                  │       │ id (PK)              │
│                  │       │ goal_id (FK)─────────│───► weight_loss_goals.id
│                  │       │ target_weight        │
│                  │       │ description          │
│                  │       │ is_achieved          │
│                  │       │ achieved_date        │
│                  │       │ created_at           │
└──────────────────┘       └──────────────────────┘

┌──────────────────┐
│  user_settings    │  (key-value, DataStore preferred but Room for backup portability)
│──────────────────│
│ key (PK)         │
│ value            │
│ data_type        │  ← "string", "int", "boolean", "float"
│ updated_at       │
└──────────────────┘

┌──────────────────┐
│ backup_metadata   │
│──────────────────│
│ id (PK)          │
│ file_path        │
│ file_size        │
│ backup_type      │  ← "auto", "manual", "upgrade_safety"
│ created_at       │
└──────────────────┘
```

### 3.2 Indexes

```kotlin
// Performance-critical queries
@Entity(indices = [
    Index("category"),           // exercises: filter by muscle group
    Index("usage_count"),        // exercises: sort by frequency
    Index("is_active"),          // exercises: seeding filter
])

@Entity(indices = [
    Index("date"),               // workouts: date range queries
    Index("is_completed"),       // workouts: find incomplete sessions
])

@Entity(indices = [
    Index("workout_id"),         // sets: all sets for a workout
    Index("exercise_id"),        // sets: history per exercise
    Index(value = ["workout_id", "exercise_id"]),  // compound: sets per exercise in workout
])

@Entity(indices = [
    Index("date"),               // body_metrics: date range
])

@Entity(indices = [
    Index("is_active"),          // weight_loss_goals: active goal lookup
])

@Entity(indices = [
    Index("goal_id"),            // milestones: milestones per goal
])
```

### 3.3 Key DAO Queries

```kotlin
// ExerciseDao
@Query("SELECT * FROM exercises WHERE is_active = 1 ORDER BY usage_count DESC, name ASC")
fun getAllActive(): Flow<List<ExerciseEntity>>

@Query("SELECT * FROM exercises WHERE is_active = 1 AND category = :category ORDER BY usage_count DESC")
fun getByCategory(category: String): Flow<List<ExerciseEntity>>

@Query("SELECT * FROM exercises WHERE is_active = 1 AND (name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%')")
fun search(query: String): Flow<List<ExerciseEntity>>

// WorkoutDao
@Query("SELECT * FROM workouts ORDER BY start_time DESC LIMIT :limit OFFSET :offset")
fun getPaginated(limit: Int, offset: Int): Flow<List<WorkoutEntity>>

@Query("SELECT * FROM workouts WHERE date BETWEEN :startDate AND :endDate ORDER BY start_time DESC")
fun getByDateRange(startDate: String, endDate: String): Flow<List<WorkoutEntity>>

@Query("SELECT * FROM workouts WHERE is_completed = 0 LIMIT 1")
suspend fun getIncompleteWorkout(): WorkoutEntity?

// SetDao
@Query("SELECT * FROM sets WHERE workout_id = :workoutId ORDER BY set_number ASC")
fun getByWorkout(workoutId: Long): Flow<List<SetEntity>>

@Query("""
    SELECT s.* FROM sets s
    INNER JOIN workouts w ON s.workout_id = w.id
    WHERE s.exercise_id = :exerciseId AND w.is_completed = 1
    ORDER BY w.start_time DESC LIMIT :limit
""")
suspend fun getRecentForExercise(exerciseId: Long, limit: Int = 10): List<SetEntity>

// Analytics queries
@Query("""
    SELECT e.category, SUM(s.weight * s.reps) as total_volume
    FROM sets s INNER JOIN exercises e ON s.exercise_id = e.id
    INNER JOIN workouts w ON s.workout_id = w.id
    WHERE w.date BETWEEN :startDate AND :endDate AND w.is_completed = 1
    GROUP BY e.category
""")
suspend fun getVolumeByCategory(startDate: String, endDate: String): List<CategoryVolume>

@Query("""
    SELECT s.exercise_id, e.name, MAX(s.weight * (1 + 0.0333 * s.reps)) as estimated_1rm
    FROM sets s INNER JOIN exercises e ON s.exercise_id = e.id
    GROUP BY s.exercise_id
    ORDER BY estimated_1rm DESC
""")
suspend fun getPersonalRecords(): List<PersonalRecordResult>
```

---

## 4. Navigation Graph

```
┌─────────────────────────────────────────────────────────┐
│                    NavHost                               │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  START ──► Splash ──┬──► Onboarding ──► SeedingChoice   │
│                     │                        │          │
│                     │          ┌──── V1Import ◄┘         │
│                     │          │                         │
│                     └──► MainScaffold (BottomNav)        │
│                          │                              │
│              ┌───────────┼───────────┬─────────┐        │
│              │           │           │         │        │
│           Altar       History     Stats    Profile      │
│              │           │                    │        │
│              │           └─► WorkoutDetail    ├─► EditProfile
│              │                                ├─► Settings
│              └─► WorkoutSession ──────────┐   ├─► WeightLoss
│                     │                     │   │     ├─► GoalSetup
│                     ├─► ExercisePicker    │   │     ├─► WeighIn
│                     │     └─► OcrScanner  │   │     ├─► Milestone
│                     │                     │   │     └─► PhotoComparison
│                     └─► WorkoutComplete   │   ├─► Backup
│                                           │   └─► PrivacyPolicy
│                                           │        │
│                                           └────────┘
└─────────────────────────────────────────────────────────┘
```

### Route Definitions

```kotlin
sealed class Screen(val route: String) {
    // Top-level
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object SeedingChoice : Screen("seeding_choice")
    data object V1Import : Screen("v1_import")

    // Main tabs
    data object Altar : Screen("altar")
    data object History : Screen("history")
    data object Stats : Screen("stats")
    data object Profile : Screen("profile")

    // Workout flow
    data object WorkoutSession : Screen("workout_session/{workoutId}") {
        fun create(workoutId: Long) = "workout_session/$workoutId"
    }
    data object ExercisePicker : Screen("exercise_picker")
    data object OcrScanner : Screen("ocr_scanner")
    data object WorkoutDetail : Screen("workout_detail/{workoutId}") {
        fun create(workoutId: Long) = "workout_detail/$workoutId"
    }

    // Profile sub-screens
    data object EditProfile : Screen("edit_profile")
    data object Settings : Screen("settings")
    data object WeightLoss : Screen("weight_loss")
    data object PhotoComparison : Screen("photo_comparison")
    data object Backup : Screen("backup")
    data object PrivacyPolicy : Screen("privacy_policy")
}
```

---

## 5. State Management

### 5.1 ViewModel Pattern

Every screen has a dedicated ViewModel. State is exposed as a single `StateFlow<UiState>` sealed interface:

```kotlin
// Example: WorkoutSessionViewModel

data class WorkoutSessionState(
    val workout: Workout? = null,
    val currentExercise: Exercise? = null,
    val completedSets: List<WorkoutSet> = emptyList(),
    val weightInput: String = "",
    val repsInput: String = "",
    val noteInput: String = "",
    val rpeInput: Int? = null,
    val rirInput: Int? = null,
    val isRestTimerRunning: Boolean = false,
    val restTimeRemaining: Int = 0,
    val sessionDuration: Long = 0,
    val lastSessionSets: List<WorkoutSet> = emptyList(),  // smart defaults
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface WorkoutSessionEvent {
    data class SelectExercise(val exercise: Exercise) : WorkoutSessionEvent
    data class UpdateWeight(val value: String) : WorkoutSessionEvent
    data class UpdateReps(val value: String) : WorkoutSessionEvent
    data class IncrementWeight(val delta: Float) : WorkoutSessionEvent
    data class IncrementReps(val delta: Int) : WorkoutSessionEvent
    data object CompleteSet : WorkoutSessionEvent
    data class EditSet(val setId: Long, val weight: Float, val reps: Int) : WorkoutSessionEvent
    data class DeleteSet(val setId: Long) : WorkoutSessionEvent
    data object SkipRestTimer : WorkoutSessionEvent
    data object FinishWorkout : WorkoutSessionEvent
    data object DiscardWorkout : WorkoutSessionEvent
}
```

### 5.2 Session Persistence

Active workout state survives process death via:

1. **Room DB**: Workout + sets saved immediately on each set completion
2. **SavedStateHandle**: ViewModel UI state (current inputs, timer state) survives config changes
3. **SharedPreferences** (lightweight): Active workout ID persisted. On cold start, check for incomplete workout and prompt resume/discard.

No AsyncStorage, no file-system state files, no 3-tier emergency backup for session state. Room handles the data. SharedPreferences handles the "is a workout active?" flag. Simple.

---

## 6. Dependency Injection (Hilt)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TrackGodDatabase {
        return Room.databaseBuilder(context, TrackGodDatabase::class.java, "trackgod.db")
            .addMigrations(*Migrations.ALL)
            .build()
    }

    @Provides fun provideExerciseDao(db: TrackGodDatabase) = db.exerciseDao()
    @Provides fun provideWorkoutDao(db: TrackGodDatabase) = db.workoutDao()
    @Provides fun provideSetDao(db: TrackGodDatabase) = db.setDao()
    @Provides fun provideUserProfileDao(db: TrackGodDatabase) = db.userProfileDao()
    @Provides fun provideBodyMetricDao(db: TrackGodDatabase) = db.bodyMetricDao()
    @Provides fun provideWeightLossDao(db: TrackGodDatabase) = db.weightLossDao()
    @Provides fun provideBackupDao(db: TrackGodDatabase) = db.backupDao()
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides @Singleton
    fun provideExerciseRepository(dao: ExerciseDao) = ExerciseRepository(dao)

    @Provides @Singleton
    fun provideWorkoutRepository(
        workoutDao: WorkoutDao,
        setDao: SetDao,
        exerciseDao: ExerciseDao
    ) = WorkoutRepository(workoutDao, setDao, exerciseDao)

    // ... other repositories
}
```

---

## 7. Key Technical Decisions

### 7.1 Why Room over raw SQLite
- Compile-time SQL verification
- Built-in migration system
- Flow/LiveData integration for reactive UI
- Type-safe DAOs
- No runtime reflection (unlike Expo SQLite wrappers)

### 7.2 Why DataStore is NOT used for settings
Settings are stored in Room (user_settings table) instead of DataStore because:
- Settings must be included in database backup/export
- Single backup file contains everything
- Simpler restore process (one file to import, not DB + DataStore)

### 7.3 Why AlarmManager for rest timer (not WorkManager)
- Rest timer needs exact timing (90 seconds means 90 seconds, not "approximately")
- WorkManager is for deferrable work, not precise timers
- AlarmManager.setExactAndAllowWhileIdle() guarantees Doze-mode delivery
- Paired with a short-lived foreground service notification

### 7.4 Why WorkManager for scheduled backups
- Backups are deferrable -- doesn't matter if it runs at 3:00 AM or 3:15 AM
- Survives app restarts and device reboots
- Respects battery optimization
- Constraint-aware (can require charging, idle, etc.)

### 7.5 Charts library
**Vico** (Compose-native) over MPAndroidChart because:
- Built for Compose (not View-based with AndroidView wrapper)
- Smooth animations
- Active development
- Sufficient chart types (line, bar, column) for our needs
- Custom styling to match TrackGod dark theme

---

## 8. Implementation Phases

### Phase 1: Foundation (Week 1-2)

**Goal**: Empty app that builds, navigates, and has a database.

- [ ] Android project setup (Kotlin, Compose, Gradle version catalogs)
- [ ] Hilt dependency injection configuration
- [ ] Room database with all entities, DAOs, and initial schema
- [ ] TrackGod theme (colors, typography, shapes, spacing)
- [ ] Core composables (TrackGodCard, TrackGodButton, TrackGodTextField, NumberInput, etc.)
- [ ] Navigation shell with 4-tab bottom nav (empty placeholder screens)
- [ ] Splash screen (logo, tagline, SYSTEM_INIT animation)

**Deliverable**: App launches, shows splash, navigates between 4 empty tabs with the TrackGod visual identity.

---

### Phase 2: Core Workout Flow (Week 3-5)

**Goal**: A user can start a workout, pick exercises, log sets, and finish.

- [ ] Exercise database seeding (bundled JSON → Room on first launch)
- [ ] Exercise picker screen (search, filter by category, sorted by usage)
- [ ] Add custom exercise flow
- [ ] Workout session screen (start workout, select exercise, log sets)
- [ ] Set logging with smart defaults from last session
- [ ] Weight/reps input with increment buttons
- [ ] Set list with edit and delete
- [ ] Rest timer (auto-start, configurable duration, notification)
- [ ] Switch between exercises within session
- [ ] Workout completion (summary, auto-name, user rename, save)
- [ ] Session persistence (survive app kill, resume prompt on restart)

**Deliverable**: Full workout session from start to finish. The core loop works.

---

### Phase 3: Dashboard & History (Week 6-7)

**Goal**: Altar shows today's data, history shows past workouts.

- [ ] Altar dashboard: weekly ritual card, today's stats grid, recent workouts
- [ ] Workout history list (paginated, newest first)
- [ ] Week/month filter toggle with period stats
- [ ] Workout detail view (exercise breakdown, per-set data)
- [ ] Workout edit (rename) and delete
- [ ] Continue same-day workout
- [ ] Streak calculation and display

**Deliverable**: Dashboard feels alive. User can review all past workouts.

---

### Phase 4: Analytics (Week 8-9)

**Goal**: Stats tab shows all 7 analytics with charts.

- [ ] Volume progression line chart (Vico)
- [ ] Muscle group distribution donut chart
- [ ] Personal records tracking and display
- [ ] Training heatmap (calendar grid)
- [ ] Strength balance visualization
- [ ] Exercise frequency ranking
- [ ] Workout consistency (streak, workouts/week trend)
- [ ] Time range filtering (week/month/quarter/year/all)

**Deliverable**: Stats tab is the reward for consistent training. All 7 analytics functional.

---

### Phase 5: Profile & Settings (Week 10)

**Goal**: Profile tab fully functional with sectioned menu.

- [ ] Profile header (avatar, name, objective)
- [ ] Sectioned menu (Account, Goals, Data, App)
- [ ] Edit profile screen (all onboarding fields)
- [ ] Settings screen (workout settings, display, notifications, data)
- [ ] RPE/RIR toggle (settings → workout session UI reflects)
- [ ] Unit switching (kg/lbs) with data display update
- [ ] Privacy policy screen

**Deliverable**: User can manage their profile and customize the app.

---

### Phase 6: Weight Loss Journey (Week 11-12)

**Goal**: Full weight loss feature with goals, weigh-ins, photos.

- [ ] Goal creation (starting weight, target, date, weekly goal)
- [ ] Weigh-in logging (weight, note, optional photo)
- [ ] Weight progress chart (line chart with target overlay)
- [ ] Milestones (create, auto-achieve on threshold cross)
- [ ] Progress photo gallery (time-grouped)
- [ ] Before/after photo comparison slider
- [ ] BMR/TDEE reference display
- [ ] Weigh-in reminder notification (WorkManager)

**Deliverable**: Complete body tracking feature for cut/bulk cycles.

---

### Phase 7: Backup & Recovery (Week 13-14)

**Goal**: Bulletproof data protection.

- [ ] Auto-backup after workout save (Room → file copy)
- [ ] Scheduled daily backup (WorkManager)
- [ ] Pre-upgrade safety backup (detect app update)
- [ ] Manual export (.db file via share sheet)
- [ ] Manual import (.db file with validation)
- [ ] JSON export option
- [ ] Startup integrity check
- [ ] Auto-recovery from backup on corruption
- [ ] Emergency multi-backup restore
- [ ] Backup management UI (list, restore, delete, stats)
- [ ] Backup retention policy enforcement

**Deliverable**: Data is protected against every failure scenario.

---

### Phase 8: OCR Scanner (Week 15)

**Goal**: Camera-based machine identification works.

- [ ] CameraX integration with preview
- [ ] Scan frame overlay (crop region)
- [ ] ML Kit Text Recognition (bundled model)
- [ ] Text extraction and cleanup
- [ ] Fuzzy matching against exercise database (Levenshtein)
- [ ] OCR error correction (0/O, 1/I, 5/S substitutions)
- [ ] Match confirmation UI
- [ ] Manual entry fallback
- [ ] Add scanned machine to database

**Deliverable**: User can scan a machine label and start logging.

---

### Phase 9: Onboarding & Import (Week 16)

**Goal**: First-launch experience is polished.

- [ ] Splash screen with brand animation
- [ ] Multi-step onboarding ("FORGE YOUR PROFILE")
- [ ] Database seeding choice (Full/Basics/Empty)
- [ ] V1 import flow (read v1 JSON export, transform, import)
- [ ] Returning user detection (skip onboarding if profile exists)

**Deliverable**: New users and v1 migrants both have a smooth entry.

---

### Phase 10: Polish & Release (Week 17-18)

**Goal**: Production-ready.

- [ ] Animations and transitions (screen transitions, set completion, PR celebration)
- [ ] Edge case handling (empty states, error states, network-less confirmation)
- [ ] Performance audit (startup time, scroll performance, DB query optimization)
- [ ] APK size optimization (R8 shrinking, unused resource removal)
- [ ] Accessibility (content descriptions, touch targets)
- [ ] Internal testing on multiple devices
- [ ] Play Store listing preparation
- [ ] Signed release build

**Deliverable**: App is ready for production users.

---

## 9. Testing Strategy

### Unit Tests
- ViewModel logic (state transformations, event handling)
- Repository methods (data mapping, business logic)
- Utility functions (1RM calculation, unit conversion, date formatting)
- OCR text processing (fuzzy matching, error correction)

### Integration Tests
- Room DAOs (in-memory database)
- Repository + DAO integration
- V1 import data transformation

### UI Tests
- Critical flows: onboarding → first workout → finish → view in history
- Compose UI tests for core composables

### Manual Testing
- Session persistence (force-stop app mid-workout, relaunch)
- Backup/restore cycle (export → delete app → reinstall → import)
- OCR scanning with real gym equipment photos

---

## 10. APK Size Budget

**V1**: 118MB (React Native + Expo + all bundled libraries)

**V2 Target**: <30MB

| Component | Estimated Size |
|-----------|----------------|
| Kotlin + Compose runtime | ~8MB |
| Room + SQLite | ~1MB |
| ML Kit Text Recognition (bundled) | ~10MB |
| CameraX | ~3MB |
| Vico charts | ~1MB |
| Coil image loading | ~1MB |
| App code + resources | ~2MB |
| Bundled exercise database | ~1MB |
| **Total** | **~27MB** |

R8 full mode + resource shrinking should keep us well under 30MB.
