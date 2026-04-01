# Brand-Aware Exercise Seeding & Gym Management

**Date:** 2026-04-01
**Status:** Approved

## Problem

The exercise database has grown to 393 exercises across 13 brands. Users need to:
1. Select which machine brands their gym has during onboarding (not all-or-nothing)
2. Easily switch brands when changing gyms (post-onboarding)

## Data Model & Seeding Logic

The `exercises_seed.json` contains 393 exercises with 13 brands:
- **Brandless (79):** Barbell, dumbbell, bodyweight, cable, other — always seeded
- **Life Fitness (45), Hammer Strength (49), GYM80 (30)** — original brands
- **Precor (15), Matrix Fitness (13), Panatta (14), Atlantis (16), Cybex (49), HOIST (20), TRUE Fitness (25), Legend Fitness (14), Technogym (13), Nautilus (11)** — new brands

### Seeding becomes brand-aware:
- Basics (non-machine exercises) are **always** seeded
- Machines are seeded **only for selected brands**
- Selected brands are persisted in `SettingsRepository` as a `Set<String>`
- Adding a brand later → only that brand's machines are seeded (incremental)
- Removing a brand → machines set to `isActive = false` (not deleted, historical data preserved)

### No DB schema migration needed
- `ExerciseEntity` already has `isActive: Boolean`
- Brand preferences go in SharedPreferences via `SettingsRepository`

## Onboarding Flow (Revised)

SeedingChoiceScreen becomes a 2-step wizard:

### Step 1 — Base Selection (existing, slightly modified)
- **"FULL ARSENAL"** → proceeds to Step 2 (Brand Picker)
- **"BASICS ONLY"** → seeds only free weight exercises, skips Step 2, done
- **"EMPTY SLATE"** → seeds nothing, done
- **"IMPORT FROM V1"** → unchanged

### Step 2 — Brand Picker (new, only after "FULL ARSENAL")
- Heading: "SELECT YOUR GYM'S BRANDS"
- Subtitle: "You can change this later in Settings."
- 2-column grid with 13 brand chips, each toggleable
- Each chip shows: Brand name + machine count (e.g., "Cybex (49)")
- No brand pre-selected — user must actively choose
- **"SKIP"** button (seeds only basics, no machines)
- **"CONTINUE"** button (seeds basics + selected brands)

User can tap 2-3 brands in 10 seconds for a personalized setup.

## Post-Onboarding: "My Gym" Screen

Located at: **Profile → Data section → "MY GYM"** (subtitle: "Manage machine brands")

### MyGymScreen behavior:
- Same 2-column brand grid as onboarding
- Currently active brands are pre-selected
- **Toggle ON** = immediately seeds that brand's machines in background
- **Toggle OFF** = immediately deactivates machines (`isActive = false`), shows brief confirmation: "15 machines hidden. Logged data is preserved."
- Back navigation saves automatically
- Basics (79 brandless exercises) are not shown here — always active, not toggleable

### Shared component:
`BrandPickerComposable` is used by both onboarding Step 2 and MyGymScreen.

## Implementation Plan

| Component | Change | Effort |
|---|---|---|
| `SettingsRepository` | New field `selectedBrands: Set<String>` | Low |
| `SeedDatabase.kt` | `seedWithBrands(brands)`, `addBrand(brand)`, `removeBrand(brand)` | Medium |
| `ExerciseRepository` | `deactivateByBrand(brand)`, `activateByBrand(brand)` | Low |
| `SeedingChoiceScreen` | Add Step 2 with brand picker | Medium |
| `SeedingChoiceViewModel` | Brand selection state + `seedWithBrands()` | Low |
| `BrandPickerComposable` | Shared component (onboarding + MyGym) | Medium |
| `MyGymScreen` + `MyGymViewModel` | New screen under Profile → Data | Medium |
| `ProfileScreen` | "MY GYM" entry in Data section | Low |
| `Navigation` | Route for MyGymScreen | Low |
| `ExercisePickerScreen` | No change — already shows only `isActive = true` | None |
