# TrackGod v2 -- Product Requirements Document

> Version: 1.0 | Date: 2026-03-21
> Platform: Native Android (Kotlin + Jetpack Compose)
> Status: Draft

---

## 1. Product Vision

TrackGod is a weight lifting tracker built for lifters who want honest, accurate logging -- like a physical notebook, but smarter. No social features, no cloud dependency, no bloat. Just iron and data.

**Tagline**: RAGE. RIP. REPEAT.

**Core principle**: A digital notebook, not a social media profile. Users track to be accurate, not to impress. Every set is editable, every number is honest.

---

## 2. Platform & Constraints

| Constraint | Decision |
|------------|----------|
| Platform | Native Android, Kotlin + Jetpack Compose |
| Minimum SDK | 26 (Android 8.0) |
| Target SDK | 34 (Android 14) |
| Network | 100% offline. No cloud, no API calls, no internet permission |
| Storage | Local SQLite database (Room) |
| Focus | Weight lifting only. No cardio, GPS, cycling, swimming, jump rope |
| External integrations | None. No Health Connect, no Garmin, no wearables |

---

## 3. Design Language

### 3.1 Visual Identity

- **Theme**: Dark, militant aesthetic. Near-black backgrounds, deep red accents, industrial typography
- **Background**: #1A1A1A primary, #121212 deep black, #2A2A2A elevated surfaces
- **Accent**: Deep red (#CC0000 range) -- used for highlights, active states, CTAs
- **Text**: White (#FFFFFF) primary, muted gray (#888888) secondary
- **Typography**: Uppercase, letter-spaced, bold. Military/tech feel
- **Cards**: Subtle borders, no heavy shadows, no glassmorphism, no gradients
- **Active indicator**: Red vertical bar on left edge for selected/active states

### 3.2 Brand Voice

The app uses ritual/altar metaphors throughout:

| Standard term | TrackGod term |
|---------------|---------------|
| Dashboard | Altar |
| Workout | Ritual |
| Start workout | Enter the Altar |
| Workout history | Past Transmissions |
| Profile setup | Forge Your Profile |
| Onboarding CTA | Initiate Protocol |
| Weekly goal | Weekly Ritual |

### 3.3 Brand Assets

- **Logo**: TG mark + "TRACKGOD" wordmark (red on dark)
- **Tagline**: "RAGE. RIP. REPEAT." -- appears on splash screen
- **Splash**: Dark background, TG logo with red glow, "SYSTEM_INIT / LOADING" status, "TAP TO ENTER THE ALTAR" CTA

### 3.4 Bottom Navigation (4 tabs)

| Tab | Icon | Label | Purpose |
|-----|------|-------|---------|
| 1 | altar icon | ALTAR | Dashboard -- today's stats, start new workout |
| 2 | dumbbell icon | WORKOUT | Workout history -- past sessions with details |
| 3 | chart icon | STATS | Analytics -- graphs, PRs, trends |
| 4 | user icon | PROFILE | User profile, settings, backup, weight loss journey |

---

## 4. Onboarding

### 4.1 Flow

Branded as "PHASE 01 // INTAKE -- FORGE YOUR PROFILE"

**Step 1**: Avatar upload (optional)
**Step 2**: Name
**Step 3**: Gender (male/female)
**Step 4**: Birthday (age calculated automatically)
**Step 5**: Height (cm or ft/in based on unit choice)
**Step 6**: Weight (kg or lbs based on unit choice)
**Step 7**: Unit preference (kg/lbs -- affects all displays globally)
**Step 8**: Primary objective (Lose Weight / Get Fit / Gain Muscle)
**Step 9**: Experience level (Beginner / Intermediate / Advanced)
**Step 10**: Weekly training target (1-7 days)

**CTA**: "INITIATE PROTOCOL"

### 4.2 Database Seeding Choice

After profile creation, user selects initial database:

| Option | Contents |
|--------|----------|
| **Full Arsenal** | 140+ machines + 150+ exercises. Everything pre-loaded |
| **Basics Only** | ~50-80 common free weight exercises. No machine brands |
| **Empty Slate** | Nothing. User adds everything manually |

All three pull from a bundled database shipped with the app. User can browse the full catalog and activate entries later, or add completely custom entries at any time.

---

## 5. ALTAR (Dashboard)

The home screen. Shows today's snapshot and provides the primary action to start a new workout.

### 5.1 Elements

**Header**: Hamburger menu (left), "TRACKGOD" wordmark (center), user avatar (right)

**Weekly Ritual card**:
- Weekly goal label + percentage complete
- Day indicators (M T W T F S S) showing completed days
- "START NEW" button (primary CTA)

**Today's Stats grid** (4 cards):
- Streak (consecutive training days)
- Volume (total weight lifted today, in tons/kg)
- Sets (total sets today)
- Duration (workout time today)

**Past Transmissions** (recent workouts list):
- Workout name, date, total volume
- Tap to expand/view details
- Shows last 3-5 workouts as preview, full history in WORKOUT tab

---

## 6. Workout Session (Active Workout)

### 6.1 Starting a Workout

User taps "START NEW" on Altar. A new workout session begins with a running timer.

### 6.2 Exercise/Machine Selection

**Unified list** -- machines and exercises live in one searchable, filterable list:

- Named machines (e.g., "Hammer Strength - Chest Press | Chest")
- Free weight exercises (e.g., "Barbell Bench Press | Chest")
- Bodyweight exercises (e.g., "Pull-ups | Back")

**Filtering**:
- Search by name, brand, or muscle group
- Filter by muscle group category (Chest, Back, Shoulders, Arms, Legs, Core)
- Sorted by usage frequency (most-used first)
- "Recently Used" section at top

**OCR**: Secondary action button to scan a machine label via camera (ML Kit). Identified machine is added to the unified list if not already present.

**Add Custom**: User can create new entries (name + muscle group category, optional brand).

### 6.3 Set Logging

For each exercise, the user logs individual sets:

**Fields per set**:
- Weight (kg or lbs -- follows global unit setting)
- Reps
- Note (optional, free text)
- RPE (optional, 1-10 scale -- enabled via settings toggle)
- RIR (optional, 0-5 scale -- enabled via settings toggle)

**Smart defaults**: Weight and reps pre-filled from the user's last session for this exercise. User taps "Done" to confirm, or adjusts using:
- Increment/decrement buttons (+/- 2.5kg for weight, +/- 1 for reps)
- Direct number input (tap the field to type)

**Set list**: All completed sets visible below the input area. Any set is editable at any point during the session -- tap to edit weight, reps, note, RPE, or RIR. Delete a set with swipe or long-press.

### 6.4 Rest Timer

- **Auto-start** (default): Timer begins counting after completing a set
- **Manual mode**: User starts timer manually via dedicated button (toggled in settings)
- **Duration**: Freely configurable (default 90 seconds, adjustable in settings)
- **Off mode**: User can disable the rest timer entirely. Stays off until re-enabled
- **Notification**: Vibration + sound when rest period completes (works in background)
- **Display**: Countdown visible on screen during rest, skippable

### 6.5 Switching Exercises

User taps "Next Exercise" or selects from the exercise picker at any time. The current exercise's sets are preserved. User can switch back to a previous exercise in the session to add more sets.

### 6.6 Finishing a Workout

User taps "Finish Workout". The app:
1. Shows a summary: total exercises, sets, reps, volume, duration
2. Auto-generates a workout name based on muscle groups trained (e.g., "Chest & Triceps")
3. User can edit the name (e.g., rename to "Chest Obliteration")
4. Saves to database
5. Returns to Altar with updated stats

### 6.7 Session Persistence

Active workout state is persisted to survive:
- App backgrounding
- App being killed by OS
- Device restart

On next app launch, if an unfinished workout exists, the user is prompted to resume or discard it.

---

## 7. WORKOUT (History)

### 7.1 Workout List

Chronological list of past workouts (newest first), showing:
- Workout name
- Date and time
- Total volume
- Duration
- Muscle groups trained (as category tags/icons)

### 7.2 Filtering

- Week / Month view toggle
- Period stats at the top (total workouts, total volume, total sets)

### 7.3 Workout Detail

Tap a workout to expand/view full details:
- Exercise-by-exercise breakdown
- Per-set data (weight, reps, RPE/RIR if enabled, notes)
- Total volume per exercise
- Workout duration

### 7.4 Actions

- **Edit**: Modify workout name
- **Delete**: Remove workout with confirmation dialog
- **Continue**: Resume a same-day workout (add more exercises/sets)

---

## 8. STATS (Analytics)

All analytics are computed from local database. No external data sources.

### 8.1 Volume Progression

Line chart showing total weight lifted over time. Filterable by:
- Week / Month / Quarter / Year / All Time

### 8.2 Muscle Group Distribution

Donut/pie chart showing training volume distribution across muscle groups. Filterable by time range.

### 8.3 Personal Records

Per-exercise PR tracking:
- Best 1RM (estimated via Epley formula: weight x (1 + 0.0333 x reps))
- Best weight at any rep count
- PR history timeline

### 8.4 Training Heatmap

Calendar grid showing which days the user trained, with intensity color coding based on volume:
- No workout: dark/empty
- Light: low volume
- Medium: moderate volume
- Heavy: high volume (deep red)

### 8.5 Strength Balance

Comparison of volume across muscle groups to identify imbalances. Visual indicator showing over/under-trained areas.

### 8.6 Exercise Frequency

Which exercises the user does most and least. Top exercises ranked by usage count.

### 8.7 Workout Consistency

- Current streak (consecutive training days)
- Longest streak
- Workouts per week trend (bar chart)
- Weekly goal completion rate

---

## 9. PROFILE

### 9.1 Profile Header

Avatar, name, primary objective, member since date.

### 9.2 Sectioned Menu

**Account**:
- Edit Profile (all onboarding fields editable)
- Privacy Policy

**Goals**:
- Weight Loss Journey (full feature -- see section 10)

**Data**:
- Backup & Restore (full feature -- see section 11)
- Export Database

**App**:
- Settings (see section 12)
- About (version, credits)

---

## 10. Weight Loss Journey

### 10.1 Goal Setting

- Starting weight, target weight, target date
- Weekly loss goal (kg or lbs)
- Motivation text (optional)

### 10.2 Weigh-In Logging

- Manual weight entry with date
- Optional note
- Optional progress photo (from camera or gallery)

### 10.3 Progress Chart

Line chart of weight over time. Shows target line overlay.

### 10.4 Milestones

User-defined intermediate weight targets between starting and goal weight. Marked as achieved when weigh-in crosses the threshold.

### 10.5 Progress Photos

- Photo gallery grouped by time period
- Before/after comparison slider (drag to reveal)
- Full-screen photo viewing
- Delete photos

### 10.6 Reference Numbers

- BMR (Basal Metabolic Rate) calculated from profile data using Mifflin-St Jeor equation
- TDEE (Total Daily Energy Expenditure) with activity multiplier
- Displayed as reference only. No deficit calculation, no meal planning.

### 10.7 Weigh-In Reminders

Configurable weekly notification reminding user to weigh in. User sets day of week and time.

---

## 11. Backup & Restore

### 11.1 Principles

- User must never fear losing data
- Recovery should be automatic when possible
- Multiple layers of protection

### 11.2 Auto-Backup

- Automatic local backup after every workout save
- Scheduled backup via Android WorkManager (daily)
- Pre-upgrade safety backup on app update detection
- Retention policy: keep last N backups (configurable, default 10)

### 11.3 Manual Export/Import

- Export database to .db file (shareable via system share sheet)
- Import database from .db file (with validation)
- JSON export option (human-readable, includes all tables)

### 11.4 Recovery

- Startup integrity check on every app launch
- If database corrupted: auto-restore from latest backup
- If database missing: attempt restore, fall back to fresh database
- Emergency recovery: try multiple backup points in order
- Corrupted databases preserved for forensic debugging

### 11.5 Backup Management UI

- List all backups with date, size
- Restore from specific backup
- Delete old backups
- View backup statistics (count, total size, last backup date)

---

## 12. Settings

### 12.1 Workout Settings

| Setting | Type | Default |
|---------|------|---------|
| Rest timer auto-start | Toggle | On |
| Rest timer duration | Number (seconds) | 90 |
| Rest timer enabled | Toggle | On |
| Show RPE field | Toggle | Off |
| Show RIR field | Toggle | Off |
| Default weight increment | Number | 2.5 |

### 12.2 Display Settings

| Setting | Type | Default |
|---------|------|---------|
| Weight unit | kg / lbs | Set during onboarding |
| Height unit | cm / ft-in | Set during onboarding |

### 12.3 Notification Settings

| Setting | Type | Default |
|---------|------|---------|
| Rest timer notification | Toggle | On |
| Weigh-in reminder | Toggle | Off |
| Weigh-in reminder day | Day picker | Sunday |
| Weigh-in reminder time | Time picker | 08:00 |

### 12.4 Data Settings

| Setting | Type | Default |
|---------|------|---------|
| Auto-backup | Toggle | On |
| Max backups to keep | Number | 10 |

---

## 13. OCR Machine Scanning

### 13.1 Purpose

Secondary feature for identifying gym machines by photographing their labels.

### 13.2 Flow

1. User taps OCR/scan button in exercise picker
2. Camera opens with scan frame overlay
3. User photographs machine label
4. ML Kit processes image on-device (offline)
5. Text extracted and fuzzy-matched against machine database
6. If match found: user confirms and machine is selected
7. If no match: user can manually enter machine name and add it

### 13.3 Technical

- Google ML Kit Text Recognition (bundled model, no network)
- Image cropped to scan frame before processing
- Fuzzy string matching (Levenshtein distance) against database
- OCR error correction for common misreads (0/O, 1/I, 5/S)
- Results cached by image hash to avoid reprocessing

---

## 14. Notifications

Only two notification types in the entire app:

### 14.1 Rest Timer

- Fires when rest period completes between sets
- Vibration + sound
- Works when app is backgrounded
- Uses Android AlarmManager for Doze mode reliability

### 14.2 Weigh-In Reminder

- Weekly recurring notification
- User-configurable day and time
- "Time to weigh in" prompt
- Uses Android WorkManager for reliable scheduling

---

## 15. Personal Records & Streaks

### 15.1 Personal Records

- Tracked per exercise automatically
- 1RM calculated via Epley formula
- Best weight at various rep ranges
- PR detection on set completion (visual celebration -- subtle, not over-the-top)
- PR history viewable in Stats

### 15.2 Streaks

- Current streak: consecutive days with at least one workout
- Longest streak: all-time record
- Displayed on Altar dashboard
- Weekly goal completion tracked

No badge/achievement system. PRs and streaks are the motivation.

---

## 16. Database Schema (v1 -- initial)

### Tables

| Table | Purpose |
|-------|---------|
| `user_profile` | Name, avatar, gender, birthday, height, weight, goals, experience, weekly_target, units |
| `exercises` | Unified exercise/machine catalog: name, category, brand (optional), equipment_type, is_custom |
| `workouts` | Sessions: date, name, start_time, end_time, notes |
| `sets` | Individual sets: workout_id FK, exercise_id FK, set_number, weight, reps, rpe, rir, note |
| `body_metrics` | Weigh-ins: date, weight, photo_uri, note |
| `weight_loss_goals` | Goals: starting_weight, target_weight, target_date, weekly_goal, is_active |
| `weight_loss_milestones` | Sub-targets: goal_id FK, target_weight, description, is_achieved |
| `user_settings` | Key-value settings with typed serialization |
| `backups` | Backup metadata: path, date, size, type |

### Key Changes from v1

- **Merged machines + exercises** into single `exercises` table with optional brand/equipment_type fields
- **Renamed `entries` to `sets`** for clarity (each row = one set, not ambiguous "entry")
- **Removed**: cardio_sessions, machine_mapping, schema_metadata (Room handles migrations)
- **Added**: exercise_id FK in sets (v1 used machine_name string, fragile)
- **Added**: set_number field for ordering within an exercise in a workout

---

## 17. Screens Eliminated from v1

The following v1 screens are NOT in v2:

| Removed Screen | Reason |
|----------------|--------|
| CardioInputScreen | No cardio |
| LiveTrackingScreen | No GPS tracking |
| ProfessionalLiveTrackingScreen | No GPS tracking |
| JumpRopeTrackingScreen | No jump rope |
| WorkoutRouteReviewScreen | No GPS routes |
| WorkoutRouteReviewScreenSafe | No GPS routes |
| ActivityHistoryScreen | Achievement timeline replaced by PR/streak focus |
| DeveloperToolsScreen | Debug builds only |

---

## 18. Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 (themed to TrackGod design) |
| Navigation | Compose Navigation (type-safe) |
| Database | Room (SQLite abstraction) |
| DI | Hilt |
| Async | Kotlin Coroutines + Flow |
| Camera/OCR | CameraX + ML Kit Text Recognition |
| Notifications | Android AlarmManager (rest timer) + WorkManager (reminders) |
| Image loading | Coil |
| Charts | Vico or MPAndroidChart (Compose-compatible) |
| Architecture | MVVM with Repository pattern |
| Build | Gradle with Version Catalogs |

---

## 19. Out of Scope (v2.0)

These features are explicitly NOT planned for initial release:

- Cloud sync / cloud backup
- Social features / sharing
- Workout templates / programs
- AI-powered recommendations
- Wearable integration
- iOS version
- Cardio / GPS tracking
- Health Connect integration
- Multi-language support (English only for v2.0)
- Tablet optimization

---

## 20. Success Criteria

The v2 rewrite is successful when:

1. A user can complete a full workout (select exercises, log sets, rest timer, finish) in fewer taps than v1
2. No data loss occurs under any circumstance (crash, kill, reboot, update)
3. The app launches and is usable within 2 seconds on mid-range devices
4. All v1 weight lifting features have parity or improvement
5. The database is at least 50% smaller in schema complexity than v1
6. The APK size is significantly smaller than v1's 118MB
