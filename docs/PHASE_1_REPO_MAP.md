# TrackGod v1 - Complete Repo-Map (Phase 1)

> Generated: 2026-03-21 | Source: C:\Projects\TrackGod | ~75,000+ lines of source code
> Purpose: Zero-blind-spot reverse engineering for Kotlin/Jetpack Compose rewrite

---

## 1. APP IDENTITY

**TrackGod** is a professional fitness tracking app built with React Native + Expo SDK 53. It combines gym workout logging (OCR machine scanning, sets/reps/weight tracking) with outdoor cardio GPS tracking, progress analytics, body metrics, achievements, and health device integration.

- **Package**: `com.trackgod.app`
- **Current Version**: 1.2.17 (versionCode 65794129)
- **Database**: SQLite v6 (12 tables, 127 columns, 19 indexes)
- **Target**: Android 14 (SDK 34), min SDK 26

---

## 2. COMPLETE FILE INVENTORY

### 2.1 Root Entry & Configuration

| File | Lines | Summary |
|------|-------|---------|
| `app/index.js` | 9 | Root entry point. Registers gesture handler then calls `registerRootComponent(App)`. |
| `app/App.js` | 538 | Main component. Orchestrates DB init with retry, profile loading, splash/privacy/onboarding flow, app state handling, notification config, and emergency reset. |
| `app/app.json` | 95 | Expo config: app metadata, 25 Android permissions, 7 plugins, SDK versions (target 34, min 26, compile 35), notification icons. |
| `app/package.json` | 89 | 60+ dependencies: React Native 0.79.6, Expo SDK 53, react-native-paper, gifted-charts, mlkit-ocr, health-connect, maps, sensors, notifications. |
| `app/eas.json` | 26 | EAS build profiles: development (dev-client), preview (internal APK), production (release APK), submit (Play Store). |

### 2.2 Navigation

| File | Lines | Summary |
|------|-------|---------|
| `app/navigation/AppNavigator.js` | 310 | Three-level navigation: RootStack (modals) > MainTabNavigator (4 swipeable bottom tabs: Workout/History/Analytics/Profile) > WorkoutStackNavigator (workout flow with coral header). 23 screens registered. |

### 2.3 Screens (26 files, ~33,620 lines)

| File | Lines | Summary |
|------|-------|---------|
| `screens/ProfileScreen.js` | 5,363 | **LARGEST FILE.** Central profile hub: user data editing, fitness features, settings, backup/restore, step tracking, Health Connect, notifications, and secret developer menu (30+ tools unlocked by 5 taps on "Other" header). |
| `screens/WorkoutSessionScreen.js` | 2,908 | Core workout tracking: exercise/machine selection, set logging (weight/reps/notes), rest timer with SDK 53 notifications, session persistence via SessionManager, edit/delete sets, previous session auto-fill. |
| `screens/WeightLossJourneyScreen.js` | 2,741 | Weight loss goals, weigh-in logging, weight chart (14-day LineChart), progress photos with comparison, milestones, weekly reminder notifications, motivational messages. |
| `screens/HistoryScreen.js` | 2,272 | Paginated workout history (10/page) with week/month filtering, bar charts, activity-specific metrics (11 cardio types), step counter integration, workout expansion/deletion/continuation. |
| `screens/CardioInputScreen.js` | 1,815 | Cardio activity logging for 11 activity types (gym + outdoor). Timer tracking, manual entry, Health Connect import, MET-based calorie calculation, GPS routing to LiveTrackingScreen. |
| `screens/AnalyticsScreen.js` | 1,375 | Comprehensive analytics dashboard: muscle group pie chart, volume line chart, personal records with 4/8-week predictions, exercise distribution, workout patterns, cardio analytics. 5 time ranges. |
| `screens/AchievementsScreen.js` | 1,349 | 20+ achievements across 5 categories (volume, consistency, goals, progress, hidden). Progress bars, locked/unlocked states, streak calculation, weekly frequency checks. |
| `screens/OCRScannerScreen.js` | 1,355 | Camera-based gym machine identification: ML Kit OCR, red scan frame crop, fuzzy database matching (fuzzysort), OCR error correction, manual fallback, machine linking. |
| `screens/ProfessionalWorkoutScreen.js` | 1,325 | Enhanced workout hub: today's stats (animated counters + progress ring), weekly progress vs goal, streak, smart resume/start button, cardio activity selector, celebration effects. |
| `screens/ProfessionalLiveTrackingScreen.js` | 1,096 | Full-screen map GPS tracking (Strava-like). Auto-hide UI, haptic feedback, collapsible metrics panel, spring animations. Uses WorkoutTrackingService. |
| `screens/LiveTrackingScreen.js` | 1,227 | Standard GPS cardio tracking: metrics-first display, distance/speed/pace/elevation, optional map toggle, auto-pause detection, crash recovery. |
| `screens/JumpRopeTrackingScreen.js` | 1,095 | Accelerometer-based jump counting: large counter with pulse animation, adaptive sensitivity, rhythm consistency %, session stats, database persistence. |
| `screens/ProfileSetupScreen.js` | 1,165 | 8-step onboarding wizard: gender, birthday, height, weight, goals, experience, weekly target, name/photo. Scroll pickers, validation, unit conversion. |
| `screens/WorkoutProgressScreen.js` | 957 | 4 analytics dashboards: progressive overload (1RM line chart), training heatmap (12-week calendar), strength balance (pie chart), training load (8-week bar chart). |
| `screens/SplashScreen.js` | 801 | Animated intro: heartbeat pulse, "RAGE. RIP. REPEAT." sequence for returning users, "CRUSH WEAKNESS" for new users. Runs DB recovery + auto-seeding in background. |
| `screens/DeveloperToolsScreen.js` | 745 | Dev utilities: retroactive workout creation with date picker + multi-machine selection, database diagnostics, database reset. |
| `screens/MachineSelectionScreen.js` | 737 | Smart machine picker: usage-frequency sorting, multi-filter (search/category/brand), "Frequently Used" tier, OCR scan button, add new machine, single/multi-select modes. |
| `screens/WorkoutRouteReviewScreen.js` | 699 | Post-workout GPS analysis: pace-colored route map, per-km splits, elevation profile, fastest/slowest km, screenshot sharing. |
| `screens/PrivacyPolicyScreen.js` | 600 | Privacy documentation: offline-first architecture, Health Connect, GPS, photos, sensors, permissions. Context-aware Health Connect notice. |
| `screens/PhotoComparisonScreen.js` | 589 | Before/after photo slider using PanResponder drag-to-reveal with "BEFORE"/"AFTER" labels. Photo selection when >2 photos available. |
| `screens/ActivityHistoryScreen.js` | 582 | Achievement timeline: volume milestones (per 1000kg), workout count milestones, personal records, weight loss milestones. Paginated infinite scroll. |
| `screens/WorkoutRouteReviewScreenSafe.js` | 543 | Fallback route review without native maps. Same stats/splits as full version but graceful dashed-border placeholder for map. |
| `screens/ProgressPhotoCameraScreen.js` | 540 | Camera with body silhouette overlay, countdown timer (3/5/10s), flash/flip controls. Auto-saves to TrackGod gallery folder. |
| `screens/WorkoutScreen.js` | 421 | Basic workout hub showing today's progress (exercises/sets/volume), available machines by category, time-based greeting. |
| `screens/AddMachineScreen.js` | 380 | Register new gym machines: name, brand selection/creation, category chips. Validates against duplicates. |
| `screens/ExerciseSelectionScreen.js` | 378 | Exercise database browser: search, muscle group filter, equipment filter, usage badges, single/multi-select modes. |

### 2.4 Components (49 files, ~14,477 lines)

#### Atoms (15 files)

| File | Lines | Summary |
|------|-------|---------|
| `atoms/EnhancedFilterChips.js` | 308 | Animated filter chips: body part single-select + secondary multi-select with haptic feedback, spring animations, summary bar. |
| `atoms/ErrorState.js` | 235 | Three components: ErrorState (full error UI + retry), EmptyState (no-data + action), InlineError (dismissible banner). |
| `atoms/GlassSearchBar.js` | 228 | Glassmorphism search: BlurView, animated clear button, context hints, result count, recent search dropdown. |
| `atoms/Icon.js` | 167 | Unified icon system: 4 libraries (Material, Community, FontAwesome, Ionicons) + AppIcons registry with 40+ semantic mappings. |
| `atoms/LoadingState.js` | 130 | Three loading components: LoadingState (centered spinner), InlineLoading (compact), LoadingOverlay (blocking). |
| `atoms/Button.js` | 128 | Core button: filled (gradient), outlined, text variants. Icon support, loading state, size scale (sm/md/lg). |
| `atoms/CustomIcon.js` | 123 | 9 fitness SVG icons: weight_lifting, stationary_bike, swimming, running, treadmill, elliptical, row_machine, jumping_rope, stairmaster. |
| `atoms/Avatar.js` | 102 | Profile picture with gradient initials fallback. Sizes: sm/md/lg/xl. |
| `atoms/ProgressRing.js` | 92 | SVG circular progress with optional gradient stroke and percentage display. |
| `atoms/GradientButton.js` | 89 | Lightweight gradient button with optional outline variant and icon. |
| `atoms/Surface.js` | 74 | Foundation container: elevation, gradient backgrounds, optional press handler, padding/radius. |
| `atoms/Typography.js` | 65 | Text system with 7 variants (display, h1-h4, body, caption, label) + convenience components. |
| `atoms/WebBlurView.js` | 39 | Platform-aware blur: CSS backdrop-filter on web, expo-blur on native. |
| `atoms/CircularIconButton.js` | 24 | Compact circular icon button with semi-transparent glass background. |
| `atoms/index.js` | 13 | Barrel export for all atoms. |

#### Molecules (10 files)

| File | Lines | Summary |
|------|-------|---------|
| `molecules/BackupHistoryModal.js` | 828 | Full backup management: status dashboard, per-backup actions (restore/export/delete), create/import buttons, progress overlay. |
| `molecules/BackupHistoryModal_Fixed.js` | 784 | Redesigned backup modal with fixed-height scroll, compact dashboard, modern button hierarchy. |
| `molecules/ModernProgressPhotos.js` | 694 | Photo gallery: time-grouped sections, featured layout, selection mode for comparison, full-screen modal, long-press delete. |
| `molecules/GoalCalorieCard.js` | 548 | Personalized calorie target: BMR/TDEE calculation, animated count-up, deficit display, warning for aggressive timelines. |
| `molecules/ActivitySelectorModal.js` | 328 | 9-activity cardio grid: treadmill, bike, elliptical, stairmaster, walking, running, swimming, rowing, jump rope. |
| `molecules/ConfirmationDialog.js` | 200 | Two dialogs: 3-button (cancel/destructive/confirm) and 2-button (cancel/confirm). Dark themed. |
| `molecules/CelebrationAnimation.js` | 199 | Full-screen celebration: Reanimated 2, 5 rotating plates, staggered timing, haptic feedback, auto-dismiss after 4s. |
| `molecules/CelebrationCard.js` | 141 | Polygon-shaped card via SVG MaskedView with glass-like texture and accent glow. |
| `molecules/StatCard.js` | 117 | Metric card: value + unit + label + trend indicator (up/down/neutral). Compact/regular sizing. |
| `molecules/index.js` | 7 | Barrel export. |

#### Onboarding & Permissions (8 files)

| File | Lines | Summary |
|------|-------|---------|
| `onboarding/ProfessionalOnboarding.js` | 1,086 | Glass-morphism onboarding system: AgePicker, GenderSelector, WorkoutTargetPicker, GoalSelector (6 goals), ExperienceLevelSelector, BirthdayPicker. |
| `permissions/LocationPermissionFlow.js` | 600 | 4-step educational flow: privacy education, foreground request, background request, completion status. Platform-specific guidance. |
| `onboarding/ProfessionalLandingPage.js` | 533 | Data-rich landing showing real workout stats from database: progress ring, exercises/sets/volume/hours. |
| `onboarding/LandingPage.js` | 362 | Basic landing: "CRUSH WEAKNESS, EMBRACE POWER", hero image, privacy messaging, CTA button. |
| `onboarding/Enhanced3DPicker.js` | 348 | 3D card-style pickers for age (horizontal scroll), gender (glow cards), and goals (8-option multi-select). |
| `onboarding/FuzzyImage.js` | 201 | TV static/analog tuning reveal effect for landing page images. |
| `permissions/SimpleLocationFlow.js` | 133 | Minimal single-screen location permission with skip option. |
| `onboarding/FuzzyText.js` | 122 | Glitch text effect with chromatic aberration for landing page headlines. |

#### Tracking & Maps (7 files)

| File | Lines | Summary |
|------|-------|---------|
| `tracking/MapLibreWebView.js` | 1,101 | WebView MapLibre GL JS v4.7.1: CDN fallback chain, two-way RN-WebView messaging, free CartoCDN tiles, GeoJSON route rendering. Primary map solution. |
| `tracking/MapTrackingView.js` | 489 | react-native-maps alternative with dark theme, speed-colored polylines, distance markers, accuracy circles. |
| `tracking/GPSMapBridge.js` | 471 | Bridges expo-location GPS with WebView maps: location subscription, route collection, metrics calculation, battery-optimized configs. |
| `tracking/MapLibreTrackingView.js` | 432 | Native MapLibre with CartoCDN tiles: speed-based route coloring, km markers, GPS accuracy indicators. |
| `tracking/MapTrackingViewFallback.js` | 353 | Text-based fallback: GPS status cards, coordinates, speed display, route progress bar. |
| `tracking/MapTrackingWrapper.js` | 105 | Smart selector: WebView MapLibre (primary) > fallback view > error boundary. Memo-optimized. |
| `tracking/MapErrorBoundary.js` | 37 | Map-specific error boundary rendering fallback on crash. |

#### Exercise, Machine & Analytics (6 files)

| File | Lines | Summary |
|------|-------|---------|
| `exercise/ExerciseListSelector.js` | 573 | Filterable exercise list: search, muscle group, usage tier badges, single/multi-select, database-driven. |
| `analytics/EnhancedPieChart.js` | 542 | Interactive donut chart: 5 filter modes (sets/reps/workouts/exercises/volume), clickable legend, balance analysis. |
| `exercise/MuscleGroupSelector.js` | 434 | Interactive SVG body diagram: 9 selectable muscle groups with color-coded feedback + quick-select buttons. |
| `analytics/WorkoutDurationAnalytics.js` | 420 | Duration stats: avg/shortest/longest, weekly pattern bar chart (Mon-Sun), muscle group breakdown. |
| `machine/MachineListSelector.js` | 81 | Simple machine selector: search filtering, single-select, icon + name + category display. |
| `analytics/index.js` | 1 | Barrel export. |

#### Other Components (4 files)

| File | Lines | Summary |
|------|-------|---------|
| `ScreenErrorBoundary.js` | 247 | Screen-level error boundary: detailed error reporting, dev stack traces, error count tracking, reset/restart. |
| `ErrorBoundary.js` | 98 | Global app error boundary: catch-all with retry button and gradient styling. |
| `TestNotificationButton.js` | 24 | Dev utility: triggers test notification after 5-second delay. |
| `index.js` | 21 | Component library barrel export. |

### 2.5 Services (13 files, ~9,116 lines)

| File | Lines | Summary |
|------|-------|---------|
| `services/WorkoutTrackingService.js` | 1,095 | Core GPS tracking engine: background location task, Haversine distance, auto-pause/resume, crash recovery (AsyncStorage), real-time listeners. Singleton. |
| `services/WorkoutStateManager.js` | 745 | 3-tier state persistence: AsyncStorage (L1) > FileSystem (L2) > Emergency backup (L3). Checksum verification, route compression, corruption recovery. Singleton. |
| `services/NotificationService.js` | 700 | Primary notification orchestrator: Expo + native AlarmManager fallback. 3 notification channels, rest timer + weight reminders, Doze mode bypass. |
| `services/GPSCalculatorService.js` | 615 | Enterprise GPS engine: Haversine formula, elevation profiles, activity-specific metrics (pace/cadence/power/VO2max), speed smoothing, calculation cache. |
| `services/JumpRopeDetectionService.js` | 582 | Accelerometer jump detection: adaptive threshold learning, moving average filter, high-pass gravity removal, rhythm analysis, 3 sensitivity levels. |
| `services/HealthConnectService.js` | 532 | Android Health Connect: step aggregation, exercise sessions, multi-source data (Garmin/Samsung), 30-day caching, debug info. |
| `services/AchievementService.js` | 442 | Achievement unlocking: stat detection, notification/celebration triggers, AsyncStorage persistence, app state monitoring for queued celebrations. Singleton. |
| `services/ExpoNotificationService.js` | 421 | Clean Expo-only notifications: TIME_INTERVAL + WEEKLY triggers, Samsung S24 optimization guidance. |
| `services/MLKitOCRService.js` | 400 | On-device text recognition: ML Kit wrapper, universal adaptive scoring, caching, image preprocessing. |
| `services/StepCounterService.js` | 210 | Unified steps: Health Connect (primary on Android) > Pedometer fallback > AsyncStorage cache. Platform-aware. |
| `services/ForegroundServiceManager.js` | 190 | Android foreground service: prevents app termination during workouts via minimal GPS notification. iOS bypass. Singleton. |
| `services/NativeAlarmModule.js` | 178 | Native AlarmManager wrapper: setAlarmClock() for rest timers, setExactAndAllowWhileIdle() for reminders. iOS mock. |
| `services/WorkoutProtectionTask.js` | 128 | Background task registration via TaskManager for foreground service protection at app startup. |

### 2.6 Database Layer (14 files, ~4,978 lines)

| File | Lines | Summary |
|------|-------|---------|
| `db/database.js` | 934 | Core DB: schema creation (12 tables), migration system (v1-v6), integrity validation, backup management, seeding, emergency recovery. |
| `db/models/machines.js` | 817 | Machine CRUD: 140+ machines, alternative names, categories, usage-sorted queries, brand/series filtering. |
| `db/models/exercises.js` | 755 | Exercise CRUD: 150+ exercises, usage tracking, muscle group filtering, recommendations, difficulty levels. |
| `db/models/workouts.js` | 468 | Workout sessions: timestamps, type classification, entry aggregation, period stats, frequency analysis, machine history. |
| `db/models/entries.js` | 390 | Exercise sets: weight/reps/sets/notes, volume calculation, personal records (Brzycki 1RM), date range queries. |
| `db/models/weightLossJourney.js` | 313 | Goals + milestones: active goal tracking, progress stats, motivational messages, milestone achievement. |
| `db/models/cardioSessions.js` | 288 | Cardio tracking: 11 activity types, GPS data, route points, activity-specific stats, auto-migration. |
| `db/models/userSettings.js` | 264 | Settings storage: type-aware serialization (bool/num/json/string), batch get/set, 12 default settings. |
| `db/models/bodyMetrics.js` | 262 | Body measurements: weight entries, progress photos, BMI calculation, weight trends, stats (avg/min/max). |
| `db/TransactionManager.js` | 205 | Transaction handling: retry logic for locked DB, resource locking, batch insert, optimistic versioning. |
| `db/models/userProfile.js` | 125 | User profile: demographics, goals, experience level, weekly target, safe DB access. |
| `db/models/machineMapping.js` | 82 | German-English machine name translation for multilingual OCR support. |
| `db/ensureDatabase.js` | 55 | Dev mode helper: prevents duplicate connections across hot reloads. |
| `db/models/index.js` | 20 | Central model exports. |

### 2.7 Utilities (42 files, ~13,143 lines)

#### Data Management (10 files)

| File | Lines | Summary |
|------|-------|---------|
| `utils/SQLiteBackupManager.js` | 1,550 | Complete SQLite backup: timestamped backups, atomic 4-phase restore with rollback, WAL checkpoint, collision avoidance, emergency restore, retention policy (10 max). |
| `utils/SessionManager.js` | 716 | In-memory session state: strength + cardio sessions, rest timer persistence, notification scheduling, exercise tracking, foreground service coordination. |
| `utils/backupManager.js` | 586 | JSON export backup: all tables + base64 photos, sharing via system dialog, import with FK verification. |
| `utils/UpgradeSafetyManager.js` | 484 | APK upgrade protection: scenario detection, safety backups, 4-point validation, emergency rollback. |
| `utils/DatabaseRecoveryManager.js` | 423 | Startup recovery: missing/corrupted DB detection, multi-attempt backup restore, fresh DB fallback. |
| `utils/DatabasePathManager.js` | 417 | Centralized paths: main DB, backups directory, mirror DB. SQLite header validation, directory initialization. |
| `utils/DatabaseExportManager.js` | 350 | .db file export: current DB or specific backup sharing, size options, batch export, recommendations. |
| `utils/PhotoStorageManager.js` | 304 | Dual-location photos: app directory + device gallery "TrackGod" album. Cleanup orphans, batch export. |
| `utils/AutoSeedingManager.js` | 217 | Startup seeding: 140+ machines + 150+ exercises. Non-destructive (only adds missing). Progress callbacks. |
| `utils/SettingsManager.js` | 190 | AsyncStorage settings: deep merge, listener subscriptions, accessibility toggle, nested path access. |

#### Calculations & Analytics (5 files)

| File | Lines | Summary |
|------|-------|---------|
| `utils/enhancedAnalytics.js` | 543 | Analytics engine: muscle group stats, duration analytics, exercise distribution, workout patterns, performance trends, color system. |
| `utils/calorieCalculator.js` | 341 | Mifflin-St Jeor BMR, TDEE with activity multipliers, MET-based workout calories, weight loss deficit calculation (max 1000 kcal/day). |
| `utils/progressPredictor.js` | 259 | 1RM prediction (Epley), progression rates by experience level, volume tracking, efficiency metrics, strength level determination. |
| `utils/dateTimeFormatter.js` | 156 | Austria locale (de-AT) formatting: UTC to local, relative dates, 24h format, DST detection. |
| `utils/targetDateCalculator.js` | 82 | Weight loss timeline: exponential decay formula, fastest/slowest scenarios, weekly kg loss estimates. |

#### Sample & Test Data (7 files)

| File | Lines | Summary |
|------|-------|---------|
| `utils/sampleData.js` | 891 | Dual generator: RealisticSampleDataGenerator (4-week PPL with progressive overload) + SampleDataGenerator (4 training splits). |
| `utils/testData.js` | 203 | TestDataUtils: comprehensive data, PPL, body part split, upper/lower, full body, 8-week extended. |
| `utils/runTask1Tests.js` - `runTask6Tests.js` | ~1,667 | 6 task-specific test runners for various subsystems. |
| `utils/testAutoSeeding.js` | 140 | Auto-seeding test: verifies machine/exercise seeding works correctly. |
| `utils/seedMachines.js` | 75 | Machine seeding: Hammer Strength catalog population by category. |
| `utils/testNotification.js` | 95 | Notification test utility. |

#### OCR & Visual (4 files)

| File | Lines | Summary |
|------|-------|---------|
| `utils/ocrEnhancements.js` | 368 | Levenshtein fuzzy matching, OCR error correction (0/O, 1/I, 5/S), brand detection, quality scoring, 50-entry cache (1hr TTL). |
| `utils/visualAssets.js` | 324 | Category icons + gradients, exercise emojis, time-based backgrounds, safe SVG generation, animation presets. |
| `utils/imageHash.js` | 48 | Simple URI hashing (32-bit base-36) for OCR result caching. |
| `utils/iconTest.js` | 62 | Grid display of all app icons for visual verification. |

#### Debugging & Diagnostics (6 files)

| File | Lines | Summary |
|------|-------|---------|
| `utils/debugDatabase.js` | 221 | DatabaseDebugger: FK integrity, orphan detection, cleanup, full repair, backup validation. |
| `utils/profileRecoveryTool.js` | 284 | Profile recovery from backups: investigates backups, compares profiles, restores specific records. |
| `utils/emergencyDiagnostic.js` | 175 | Emergency diagnostic: DB file status, backup availability, integrity analysis, auto-recovery attempt. |
| `utils/SafeTableMigration.js` | 199 | Safe migration for cardio_sessions table with CHECK constraints and verification. |
| `utils/userProfileDiagnostic.js` | 155 | Profile diagnostic: table existence, structure check, creation test, onboarding troubleshooting. |
| `utils/Logger.js` | 64 | Environment-aware logging: debug/info/warn/error, GPS timestamps, production silencing. |

#### Other Utilities (5 files)

| File | Lines | Summary |
|------|-------|---------|
| `utils/numberFormatter.js` | 59 | Volume formatting: 1.2k kg, 2.5M kg abbreviations. |
| `utils/backupOptimizer.js` | 87 | Backup frequency control: 30s minimum interval, always-allow triggers. |
| `utils/safetyCheck.js` | 67 | Pre-seeding safety audit: counts workouts/entries/volume, guarantees no data loss. |
| `utils/runCardioMigration.js` | 44 | Migration wrapper for cardio_sessions table creation. |
| `utils/replace-logs.js` | 31 | Build script: replaces console.* with Logger.* in WorkoutTrackingService. |

### 2.8 Theme, Config & Hooks (5 files)

| File | Lines | Summary |
|------|-------|---------|
| `theme/theme.js` | 257 | Complete design system: dark theme (#121212), accent (#FF6E40 coral), 7 typography scales, 8pt spacing grid, elevation levels, component tokens, animation timings. |
| `config/ocr-config.js` | 153 | OCR config: universal machine label optimization, character whitelist, 60% confidence threshold, adaptive scoring, 10s timeout. |
| `hooks/useAccessibility.js` | 101 | Accessibility hook: conditional ARIA props, role-specific helpers (button/text/image/header), settings listener. |
| `hooks/useAndroidBackHandler.js` | 48 | Back button hook: custom handler or exit confirmation, focus-aware lifecycle. |
| `hooks/useLoadingState.js` | 55 | Async state hook: loading/error/data management, execute/retry/reset, convenience flags. |

### 2.9 Build Scripts (5 files)

| File | Lines | Summary |
|------|-------|---------|
| `build.js` | 293 | Release APK builder: version increment, expo prebuild, gradlew assembleRelease, signing. |
| `dev-build.js` | 263 | Debug APK with expo-dev-client for hot reload. |
| `build-playstore.js` | 202 | Play Store AAB: timestamp versionCode, keystore signing, cache clearing. |
| `windows-dev-build.js` | 150 | Windows-specific dev build with PowerShell commands. |
| `dev-start.js` | 79 | Dev server launcher with network IP detection and QR code. |

### 2.10 Expo Plugins (4 files)

| File | Lines | Summary |
|------|-------|---------|
| `app/plugins/withNotificationAlarms.js` | 629 | Generates complete alarm infrastructure: AlarmReceiver, BootReceiver, AlarmScheduler, Module, Package Java files + manifest entries. |
| `app/plugins/withHealthConnect.js` | 184 | Health Connect: PermissionDelegate in MainActivity, PermissionsRationaleActivity, package queries, Android 14+ ViewPermissionUsageActivity. |
| `app/plugins/withAndroidLaunchMode.js` | 50 | singleTask launch mode + large heap + hardware acceleration. |
| `plugins/withNativeAlarms.js` | 102 | Legacy alarm plugin (superseded by withNotificationAlarms). |

### 2.11 Android Native Code (11 files)

| File | Lines | Summary |
|------|-------|---------|
| `app/android/.../AlarmSchedulerModule.java` | 151 | React Native bridge: scheduleRestTimer, scheduleWeightReminder, cancel methods. AlarmManager setAlarmClock + setExactAndAllowWhileIdle. |
| `app/android/.../AlarmReceiver.java` | 143 | BroadcastReceiver: creates notifications with vibration (0,500,200,500,200,500ms), alarm sound, full-screen intent for Doze bypass. |
| `app/android/.../AlarmScheduler.java` | 126 | Static helper: AlarmManager scheduling, SharedPreferences for boot persistence. |
| `app/android/.../MainActivity.kt` | 72 | Entry activity: HealthConnectPermissionDelegate init, singleTask, AppTheme. |
| `app/android/.../MainApplication.kt` | 59 | Application singleton: React host init, Expo module lifecycle, package registration. |
| `app/android/.../BootReceiver.java` | 34 | Restores weight reminder alarms from SharedPreferences after device reboot. |
| `app/android/.../AlarmSchedulerPackage.java` | 23 | ReactPackage wrapper registering AlarmSchedulerModule. |
| `app/android/app/build.gradle` | 178 | App build config: applicationId, versionCode/Name, signing, Hermes, dependencies. |
| `app/android/build.gradle` | 38 | Root Gradle: plugin classpath, repositories. |
| `app/android/settings.gradle` | 40 | Gradle settings: autolinking, React Native plugin. |
| `app/android/.../AndroidManifest.xml` | ~100 | Manifest: 25 permissions, 4 receivers, 2 activities, foreground service declarations. |

### 2.12 Documentation (12 files)

| File | Lines | Summary |
|------|-------|---------|
| `TODO.md` | ~200 | Feature tracker: 150+ exercises done, analytics done, OCR done, notifications done. Deferred items listed. |
| `PRD.md` | 178 | Product requirements: vision, features, design system (Movemate dark theme + coral), component specs, tech stack. |
| `README.md` | 156 | Project overview: structure, setup, ML Kit OCR (offline, 365ms), feature list, schema overview. |
| `CLAUDE.md` | 296 | Dev guidance: SDK 53 patterns, GPS tracking architecture, jump rope ML, Health Connect, testing requirements. |
| `FEEDBACK_BUGS.md` | 669 | 20 issues by priority. Critical: OCR overwrites, PRs not calculating. Most fixed. |
| `APPSTORE_RELEASE.md` | 637 | Google Play Store guide: keystore, dual build system, store listing, release process. |
| `BUGS & FEEDBACK - 23072025.md` | 1,327 | July 2025 audit: 17 issues analyzed, implementation status, code snippets, test results. |
| `Backup&Restore_29072025.md` | ~150 | SQLite backup implementation plan + emergency fix for empty backups (SQL export strategy). |
| `DEVELOPER_DIARY.md` | 399 | Chronological dev log: notification fixes, build improvements, Health Connect challenges. |
| `PILOT_SESSION_3_FEEDBACK.md` | 194 | Pilot testing: 7 issues, OCR overwrites critical, multi-language request. |
| `OCR_SETUP.md` | 78 | OCR.Space API setup (legacy, replaced by ML Kit). |
| `EXPO_DEV_CLIENT_SETUP.md` | 97 | Migration guide from Expo Go to dev client for native modules. |

---

## 3. DATABASE SCHEMA (SQLite v6, 12 Tables)

| Table | Columns | Purpose |
|-------|---------|---------|
| `user_profile` | 12 | Name, avatar, gender, age, birthday, height, weight, goals, weekly_target, experience_level |
| `machines` | 8 | Gym equipment catalog: name, brand, series, category, weight_stack, alternative_names (140+) |
| `exercises` | 7 | Exercise library: name, muscle_group, equipment_type, instructions, difficulty (150+) |
| `workouts` | 8 | Workout sessions: date, start/end time, type (strength/cardio), activity_data JSON |
| `entries` | 8 | Exercise sets: workout_id FK, machine_name, weight, reps, sets, note |
| `body_metrics` | 6 | Body measurements: date, weight, photo_uri, note |
| `weight_loss_goals` | 11 | Goals: starting/target weight, target date, weekly goal, reminder day/time, is_active |
| `weight_loss_milestones` | 8 | Sub-targets: goal_id FK, target_weight, description, is_achieved |
| `cardio_sessions` | 16 | Cardio: workout_id FK, activity_type (11 types), duration, distance, calories, HR, GPS data, route_points |
| `user_settings` | 4 | Key-value settings with type-aware serialization (12 defaults) |
| `schema_metadata` | 4 | Migration tracking: schema_version key |
| `machine_mapping` | 4 | German-English machine name translation |

**Relationships**: workouts -> entries (1:N), workouts -> cardio_sessions (1:N), weight_loss_goals -> milestones (1:N)

---

## 4. DEPENDENCY LIST (60+ packages)

**Core**: react 19.0.0, react-native 0.79.6, expo ~53.0.25
**Navigation**: @react-navigation/native, /bottom-tabs, /stack, /material-top-tabs, react-native-pager-view
**UI**: react-native-paper 5.14.5, react-native-vector-icons, react-native-reanimated ~3.17.4, expo-blur, expo-linear-gradient
**Database**: expo-sqlite ~15.2.14, @react-native-async-storage/async-storage 2.1.2
**Location**: expo-location ~18.1.6, react-native-maps 1.20.1, expo-task-manager ~13.1.6
**Sensors**: expo-sensors ~14.1.4 (accelerometer for jump rope)
**Health**: react-native-health-connect 3.3.3
**Camera/Media**: expo-camera, expo-image-picker, expo-image-manipulator, expo-media-library
**OCR**: react-native-mlkit-ocr 0.3.0, fuzzysort 3.1.0
**Charts**: react-native-gifted-charts 1.4.63
**Notifications**: expo-notifications ~0.31.4
**Other**: expo-haptics, expo-keep-awake, expo-file-system, expo-sharing, expo-document-picker, react-native-webview, googleapis

---

## 5. ANDROID PERMISSIONS (25)

Location (3), Health Connect (6), Camera/Storage (3), Notifications/Alarms (4), System (5), Foreground Service (2), Boot (1), Audio (1)

---

## 6. FEATURE INVENTORY

| Feature | Screens | Services | DB Tables |
|---------|---------|----------|-----------|
| Workout Logging | WorkoutSession, ProfessionalWorkout, Workout | SessionManager | workouts, entries, machines |
| GPS Cardio Tracking | LiveTracking, ProfessionalLiveTracking, CardioInput | WorkoutTrackingService, GPSCalculatorService, ForegroundServiceManager | workouts, cardio_sessions |
| Jump Rope Detection | JumpRopeTracking | JumpRopeDetectionService | workouts, cardio_sessions |
| OCR Machine Scanning | OCRScanner | MLKitOCRService | machines |
| Analytics | Analytics, WorkoutProgress | enhancedAnalytics, progressPredictor | workouts, entries |
| Achievements | Achievements, ActivityHistory | AchievementService | workouts, entries |
| Weight Loss Journey | WeightLossJourney | NotificationService | weight_loss_goals, milestones, body_metrics |
| Progress Photos | ProgressPhotoCamera, PhotoComparison | PhotoStorageManager | body_metrics |
| Profile Management | Profile, ProfileSetup | SettingsManager | user_profile, user_settings |
| Health Connect | Profile (step tracking) | HealthConnectService, StepCounterService | - |
| Backup/Restore | Profile (settings modal) | SQLiteBackupManager, backupManager, DatabaseRecoveryManager | all tables |
| Notifications | Profile (settings), WorkoutSession (rest timer) | NotificationService, ExpoNotificationService, NativeAlarmModule | user_settings |
| Onboarding | Splash, ProfileSetup | AutoSeedingManager | user_profile, machines, exercises |

---

## 7. KNOWN ISSUES FROM USER FEEDBACK

**Critical (unfixed)**:
- Health Connect stationary bike type mapping (wrong exercise constants)
- Multi-language OCR support (German machine names not recognized)

**Fixed in v1.2.x**:
- OCR overwrites previous exercise data
- Personal records not calculating
- Profile weight not syncing with Weight Loss Journey
- Empty backup files (fixed via SQL export strategy)
- Notification reliability (complete SDK 53 overhaul)
- App icon cropping

**User Requests (unimplemented)**:
- Multi-language machine support (alias system needed)
- Cloud backup integration
- Enhanced notification linking

---

**Phase 1 Complete. Awaiting approval to proceed to Phase 2: UI-First Trace.**
