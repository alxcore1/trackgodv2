# TrackGod v1 → v2 Database Migration Plan

> Version: 1.0 | Date: 2026-03-21
> Strategy: Export from v1 (JSON), Import into v2 during onboarding
> References: docs/PRD.md, docs/ARCHITECTURE.md, PHASE_1_REPO_MAP.md

---

## 1. Migration Strategy

**Two separate apps.** V1 stays installed. User exports data from v1, installs v2, imports during onboarding.

```
TrackGod v1 (Expo)              TrackGod v2 (Kotlin)
┌──────────────────┐            ┌──────────────────┐
│                  │            │                  │
│  Profile > Data  │            │  Onboarding      │
│  > Export Data   │──► JSON ──►│  > Import v1     │
│                  │   file     │                  │
└──────────────────┘            └──────────────────┘
```

**Why JSON, not .db file?**
- V1 uses Expo SQLite with different schema, table names, and column types
- Room expects its own schema with metadata tables
- JSON is human-readable and debuggable
- We control the transformation, not hope SQLite versions are compatible
- V1 already has a JSON export feature (backupManager.js `exportAllData()`)

---

## 2. V1 Export Format

V1's existing `exportAllData(includePhotos)` in `backupManager.js` produces:

```json
{
  "version": "1.2.17",
  "exportDate": "2026-03-21T14:30:00.000Z",
  "data": {
    "user_profile": [{ ... }],
    "machines": [{ ... }, ...],
    "exercises": [{ ... }, ...],
    "workouts": [{ ... }, ...],
    "entries": [{ ... }, ...],
    "body_metrics": [{ ... }, ...],
    "weight_loss_goals": [{ ... }, ...],
    "weight_loss_milestones": [{ ... }, ...],
    "cardio_sessions": [{ ... }, ...],
    "user_settings": [{ ... }, ...],
    "machine_mapping": [{ ... }, ...]
  },
  "photos": [
    {
      "uri": "file:///path/to/photo.jpg",
      "base64": "..."
    }
  ]
}
```

**We may need to update v1's export** to ensure it includes all fields we need. If that's not feasible, we design the v2 importer to be tolerant of missing fields.

---

## 3. Schema Mapping

### 3.1 Tables That Map Directly

| V1 Table | V2 Table | Transformation |
|----------|----------|----------------|
| `user_profile` | `user_profile` | Add `weight_unit`, `height_unit` fields. Map `goals` text → `primary_objective` enum. |
| `body_metrics` | `body_metrics` | 1:1. Photo URIs need path rewriting. |
| `weight_loss_goals` | `weight_loss_goals` | 1:1. Direct field mapping. |
| `weight_loss_milestones` | `weight_loss_milestones` | 1:1. Direct field mapping. |

### 3.2 Tables That Merge

| V1 Tables | V2 Table | Transformation |
|-----------|----------|----------------|
| `machines` + `exercises` | `exercises` | Merge into unified list. See section 4. |

### 3.3 Tables That Rename + Transform

| V1 Table | V2 Table | Transformation |
|----------|----------|----------------|
| `entries` | `sets` | Rename. Convert `machine_name` (string) → `exercise_id` (FK). Add `set_number`. See section 5. |
| `workouts` | `workouts` | Filter out cardio workouts (`workout_type = 'cardio'`). Add `total_volume` computed field. See section 6. |

### 3.4 Tables That Are Dropped

| V1 Table | Reason |
|----------|--------|
| `cardio_sessions` | No cardio in v2 |
| `machine_mapping` | German translations merged into `alternative_names` in exercises |
| `schema_metadata` | Room handles schema versioning internally |

### 3.5 New Tables in V2

| V2 Table | Purpose |
|----------|---------|
| `backup_metadata` | Tracks backup files (not present in v1) |

---

## 4. Merging Machines + Exercises → Unified Exercises Table

This is the most complex transformation.

### 4.1 The Problem

V1 has two separate tables:
- **machines**: `id, name, brand, series, category, weight_stack, alternative_names, created_at`
- **exercises**: `id, name, muscle_group, equipment_type, instructions, difficulty_level, created_at`

V2 has one table:
- **exercises**: `id, name, category, equipment_type, brand, alternative_names, is_custom, is_active, usage_count, last_used_at, created_at`

### 4.2 Transformation Rules

**Step 1: Import all v1 machines as exercises**

```
V1 machine → V2 exercise:
  name           → name
  category       → category
  brand          → brand
  alternative_names → alternative_names
  -              → equipment_type = "machine"
  -              → is_custom = false (if pre-seeded) / true (if user-added)
  -              → is_active = true
  created_at     → created_at
```

**Step 2: Import all v1 exercises as exercises**

```
V1 exercise → V2 exercise:
  name           → name
  muscle_group   → category
  equipment_type → equipment_type
  -              → brand = null
  -              → alternative_names = null
  -              → is_custom = (check if it was in seed data or user-created)
  -              → is_active = true
  created_at     → created_at
```

**Step 3: Deduplicate**

Some machines and exercises may have the same name (e.g., "Chest Press" exists as both a machine and an exercise). Rules:

1. If a machine and exercise have the **exact same name** (case-insensitive): merge into one entry, keep the machine's brand info, use the exercise's equipment_type if more specific.
2. If names are similar but not identical: keep both. User can clean up later.
3. All entries get a new auto-generated `id` in v2. A lookup map (`old_type + old_id → new_id`) is maintained for the entries/sets transformation.

### 4.3 Building the Lookup Map

```kotlin
data class ExerciseLookupKey(
    val source: String,        // "machine" or "exercise"
    val originalId: Long,      // v1 id
    val originalName: String   // v1 name (fallback for entries that use machine_name)
)

// Map: ExerciseLookupKey → new v2 exercise ID
val exerciseLookup: Map<ExerciseLookupKey, Long>

// Also maintain a name-based lookup for entries.machine_name resolution
val nameLookup: Map<String, Long>  // normalized name → v2 exercise ID
```

---

## 5. Transforming Entries → Sets

### 5.1 The Problem

V1 `entries` reference machines by **name string** (`machine_name`):
```json
{
  "id": 42,
  "workout_id": 5,
  "machine_name": "Hammer Strength Chest Press",
  "weight": 80.0,
  "reps": 10,
  "sets": 1,
  "note": "felt easy",
  "created_at": "2025-09-15T10:30:00.000Z"
}
```

V2 `sets` reference exercises by **foreign key** (`exercise_id`):
```json
{
  "id": 1,
  "workout_id": 5,
  "exercise_id": 12,
  "set_number": 1,
  "weight": 80.0,
  "reps": 10,
  "rpe": null,
  "rir": null,
  "note": "felt easy",
  "created_at": "2025-09-15T10:30:00.000Z"
}
```

### 5.2 Transformation Rules

**Step 1: Resolve `machine_name` → `exercise_id`**

```
For each v1 entry:
  1. Normalize machine_name (trim, lowercase)
  2. Look up in nameLookup map
  3. If found → use the v2 exercise_id
  4. If NOT found → create a new exercise entry:
       name = original machine_name
       category = "Other" (or infer from context if possible)
       equipment_type = "other"
       is_custom = true
       is_active = true
     → use the newly created exercise_id
```

**Step 2: Calculate `set_number`**

V1 entries don't have an explicit set number. They're ordered by `created_at` within a workout+machine group.

```
For each workout:
  Group entries by machine_name
  For each group:
    Sort by created_at ASC
    Assign set_number = 1, 2, 3, ...
```

**Step 3: Map `sets` field**

V1's `sets` column is confusing -- it's always `1` in practice (each row = one set). We ignore it. Each v1 entry row becomes one v2 set row.

**Step 4: RPE and RIR**

V1 has no RPE/RIR data. Set both to `null` in v2.

---

## 6. Transforming Workouts

### 6.1 Filter Out Cardio

```
Only import workouts where:
  workout_type = 'strength' OR workout_type IS NULL OR workout_type = ''

Skip workouts where:
  workout_type = 'cardio' OR workout_type = 'mixed' (if all entries are cardio)
```

For `workout_type = 'mixed'`: import the workout but only include strength-related entries. If zero strength entries remain after filtering, skip the workout entirely.

### 6.2 Field Mapping

```
V1 workout → V2 workout:
  id          → (new auto-generated id, maintain mapping for sets)
  date        → date
  comment     → notes
  start_time  → start_time
  end_time    → end_time
  -           → name = auto_generate_from_exercises(entries)
  -           → duration_seconds = calculate(end_time - start_time)
  -           → total_volume = SUM(weight * reps) from imported sets
  -           → is_completed = (end_time IS NOT NULL)
  created_at  → created_at
```

### 6.3 Auto-Generating Workout Names

V1 workouts don't have names. Generate from the exercises performed:

```kotlin
fun generateWorkoutName(exercises: List<Exercise>): String {
    val categories = exercises.map { it.category }.distinct()
    return when {
        categories.size == 1 -> categories.first()  // "Chest"
        categories.size == 2 -> "${categories[0]} & ${categories[1]}"  // "Chest & Triceps"
        categories.size <= 4 -> categories.joinToString(", ")  // "Chest, Shoulders, Triceps"
        else -> "Full Body"
    }
}
```

---

## 7. User Profile Transformation

```
V1 user_profile → V2 user_profile:
  id               → id
  name             → name
  avatar_uri       → avatar_uri (path rewritten, see section 9)
  gender           → gender
  age              → (calculated from birthday)
  birthday         → birthday
  height           → height
  weight           → weight
  goals            → primary_objective (map: "strength" → GAIN_MUSCLE, "cardio" → GET_FIT, etc.)
  weekly_target    → weekly_target
  experience_level → experience_level
  -                → weight_unit = "kg" (default, user can change)
  -                → height_unit = "cm" (default, user can change)
  created_at       → created_at
  -                → updated_at = import timestamp
```

### 7.1 Goals Mapping

V1 stored goals as a comma-separated string (e.g., "strength,weight_loss,cardio").

V2 has a single `primary_objective` enum: `LOSE_WEIGHT`, `GET_FIT`, `GAIN_MUSCLE`.

```kotlin
fun mapGoals(v1Goals: String): PrimaryObjective {
    val goals = v1Goals.lowercase().split(",").map { it.trim() }
    return when {
        "weight_loss" in goals || "lose_weight" in goals -> PrimaryObjective.LOSE_WEIGHT
        "strength" in goals || "gain_muscle" in goals -> PrimaryObjective.GAIN_MUSCLE
        else -> PrimaryObjective.GET_FIT  // default fallback
    }
}
```

---

## 8. User Settings Transformation

V1 settings are key-value pairs. Only import relevant ones:

| V1 Setting Key | V2 Setting Key | Notes |
|----------------|----------------|-------|
| `rest_duration_seconds` | `rest_timer_duration` | Direct map |
| `rest_notifications_enabled` | `rest_timer_notification` | Direct map |
| `auto_rest_timer` | `rest_timer_auto_start` | Direct map |
| `vibration_enabled` | (kept in v2) | Direct map |
| `default_increment_weight` | `default_weight_increment` | Direct map |
| `weight_unit` | (goes to user_profile) | Map to profile field |
| `distance_unit` | DROP | No cardio |
| `auto_backup_enabled` | `auto_backup` | Direct map |
| `backup_frequency` | DROP | V2 uses fixed daily schedule |
| `theme_mode` | DROP | V2 is always dark |
| `sound_enabled` | (kept in v2) | Direct map |

---

## 9. Photo URI Handling

### 9.1 The Problem

V1 stores photos in Expo's document directory:
```
file:///data/user/0/com.trackgod.app/files/TrackGod/progress_2025-01-15T10-30-45.jpg
```

V2 is a different app with a different package name and file directory.

### 9.2 Solution

**Photos are included in the v1 JSON export as base64.**

During import:
1. Read base64 data from the export JSON
2. Write each photo to v2's app-specific directory: `context.filesDir/photos/`
3. Generate new filename: `progress_{timestamp}.jpg`
4. Update `body_metrics.photo_uri` and `user_profile.avatar_uri` to point to new paths

```kotlin
suspend fun importPhoto(base64Data: String, originalUri: String): String {
    val photosDir = File(context.filesDir, "photos").apply { mkdirs() }
    val timestamp = extractTimestamp(originalUri) ?: System.currentTimeMillis()
    val file = File(photosDir, "progress_$timestamp.jpg")

    val bytes = Base64.decode(base64Data, Base64.DEFAULT)
    file.writeBytes(bytes)

    return file.absolutePath
}
```

### 9.3 Photos Without Base64

If the user exported without photos (`includePhotos = false`), photo URIs in the import will reference paths that don't exist in v2. Handle gracefully:
- Set `photo_uri = null` for entries where the photo file doesn't exist
- Show a notice: "X progress photos could not be imported. Export from v1 with photos included to transfer them."

---

## 10. Import Flow (V2 Side)

### 10.1 User-Facing Flow

```
Onboarding Step: "Coming from TrackGod v1?"
    │
    ├── [Yes] → File picker opens
    │            User selects exported JSON file
    │            │
    │            ├── Validation (see 10.2)
    │            │   ├── Valid → Show import summary
    │            │   │           "Found: 1 profile, 245 workouts, 3,420 sets, 12 photos"
    │            │   │           [Import] [Skip]
    │            │   │
    │            │   └── Invalid → "This file doesn't look like a TrackGod export.
    │            │                   Make sure you exported from TrackGod v1."
    │            │
    │            └── Import executes (with progress bar)
    │                "Importing workouts... (142/245)"
    │                "Importing photos... (8/12)"
    │                │
    │                └── Complete → "Import successful! 245 workouts imported."
    │                               [Continue to TrackGod v2]
    │
    └── [No / Skip] → Continue to database seeding choice
```

### 10.2 Validation

Before importing, verify:

1. **File is valid JSON** -- parseable, has expected structure
2. **Has required sections** -- `data` object with at least `workouts` or `user_profile`
3. **Version check** -- `version` field exists (any v1 version accepted)
4. **Size sanity** -- file is < 500MB (prevent loading absurdly large files into memory)

### 10.3 Import Order

Order matters due to foreign key dependencies:

```
1. user_profile       (no dependencies)
2. exercises          (merge machines + exercises, build lookup map)
3. weight_loss_goals  (no dependencies)
4. weight_loss_milestones (depends on goals)
5. workouts           (filter cardio, generate names)
6. sets               (depends on workouts + exercises, resolve machine_name → exercise_id)
7. body_metrics       (no dependencies, but photo import happens here)
8. user_settings      (no dependencies)
9. photos             (write files, update URIs in body_metrics)
```

### 10.4 Transaction Safety

The entire import runs in a **single Room transaction**. If any step fails, everything rolls back. No partial imports.

```kotlin
@Transaction
suspend fun importV1Data(exportData: V1ExportData) {
    // Step 1-9 inside one transaction
    // On any exception → automatic rollback
}
```

### 10.5 Conflict Resolution

If the user somehow imports twice (unlikely but possible):

- **user_profile**: Overwrite existing profile
- **exercises**: Skip duplicates (match by normalized name + category)
- **workouts**: Skip if a workout with the same `start_time` already exists
- **sets**: Cascade with workouts (if workout skipped, its sets are skipped)
- **body_metrics**: Skip if same date + weight already exists
- **photos**: Skip if file already exists at target path

---

## 11. Edge Cases

### 11.1 Missing Fields

V1 database evolved through 6 schema versions. Early data may not have all fields:

| Missing Field | Fallback |
|---------------|----------|
| `workouts.start_time` | Use `created_at` timestamp |
| `workouts.end_time` | Set `null`, mark as `is_completed = true` anyway (historical data) |
| `workouts.workout_type` | Assume `"strength"` |
| `entries.note` | Set `null` |
| `machines.alternative_names` | Set `null` |
| `machines.brand` | Set `null` |
| `user_profile.birthday` | Set `null`, skip age calculation |
| `user_profile.experience_level` | Default to `"intermediate"` |
| `user_profile.weekly_target` | Default to `4` |

### 11.2 Orphaned Data

V1 had known issues with orphaned entries (entries referencing deleted workouts). During import:

```kotlin
// Only import sets whose workout_id maps to an imported workout
val validWorkoutIds = importedWorkouts.map { it.originalId }.toSet()
val validSets = v1Entries.filter { it.workoutId in validWorkoutIds }
```

### 11.3 Duplicate Machine Names

V1 allowed duplicate machine names in some edge cases. During merge:
- First occurrence wins
- Subsequent duplicates have their entries remapped to the first occurrence's v2 ID

### 11.4 Empty Export

If the export contains no workouts, no profile, or only cardio data:
- Show: "No weight training data found in this export."
- Offer: Continue with empty database

### 11.5 Very Large Exports

For users with 1000+ workouts and years of data:
- Stream the JSON parsing (don't load entire file into memory)
- Show progress bar with counts
- Run import on background coroutine with progress updates to UI

---

## 12. V1 Export Enhancement (Optional)

If we have the ability to ship a v1 update before v2 launches, add a dedicated "Export for v2" button that:

1. Uses the existing `exportAllData(includePhotos=true)` function
2. Adds a `"format": "trackgod_v1_export"` field for easier detection
3. Adds a `"schema_version": 6` field
4. Filters out cardio data before export (smaller file)
5. Names the file `trackgod_v1_export_{date}.json`

This is optional. The v2 importer will work with the existing v1 export format regardless.

---

## 13. Testing the Migration

### 13.1 Test Data Sets

Create test JSON files covering:

1. **Minimal**: 1 profile, 1 workout, 3 sets, 0 photos
2. **Typical**: 1 profile, 50 workouts, 500 sets, 5 photos, 1 weight loss goal
3. **Large**: 1 profile, 500 workouts, 5000 sets, 50 photos, 3 goals, 10 milestones
4. **Mixed**: Workouts with both strength and cardio (verify cardio filtering)
5. **Legacy**: Data from early v1 versions with missing fields
6. **Corrupt**: Malformed JSON, missing sections, invalid data types

### 13.2 Verification Queries

After import, run these checks:

```kotlin
// Counts should match (minus filtered cardio)
assert(v2WorkoutCount == v1StrengthWorkoutCount)
assert(v2SetCount == v1StrengthEntryCount)
assert(v2ExerciseCount >= v1MachineCount)  // merged, may be equal or more

// No orphaned sets
assert(db.setDao().getOrphanedSets().isEmpty())

// All sets have valid exercise_id
assert(db.setDao().getSetsWithInvalidExerciseId().isEmpty())

// Total volume preserved
assert(abs(v2TotalVolume - v1StrengthTotalVolume) < 0.01)

// Photos exist on disk
v2BodyMetrics.filter { it.photoUri != null }.forEach {
    assert(File(it.photoUri).exists())
}
```

### 13.3 Real-World Testing

1. Export from actual v1 app on test device
2. Import into v2 on same device
3. Manually verify: workout count, random workout details, photos visible, weight loss goal intact
4. Compare v1 and v2 side-by-side for data consistency

---

## 14. Rollback Plan

If import fails or produces bad data:

1. Import runs in a single transaction → auto-rollback on failure
2. User sees error message: "Import failed. Your v1 data is untouched. Try again or start fresh."
3. V1 app is still installed with all original data
4. User can re-export from v1 and retry
5. If repeated failures: user starts fresh in v2, keeps v1 for historical reference

No data is ever deleted from v1 during this process. The import is purely additive to v2.
