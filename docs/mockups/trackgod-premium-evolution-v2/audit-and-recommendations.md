# TrackGod UX/UI Review — code-backed V2

## What was reviewed

- The current Compose navigation and all first-level product areas.
- State and events in the Altar, Workout Session, Exercise Picker, History, Stats, Profile, Weight Loss, My Gym, Backup and Settings ViewModels/screens.
- A fresh debug build installed on a Pixel 8 / API 35 emulator.
- Runtime flow: Splash, seven-step onboarding, Arsenal choice, Altar, empty workout, Exercise Picker and active Barbell Bench Press session.

This is why V2 contains the real TrackGod details instead of reducing the app to four generic fitness screens.

## Recommended direction

Keep TrackGod's industrial-brutalist identity. The app does not need a rebrand; it needs a stricter information system. Red should signal a live state, selected state, warning or primary action. Dark surface levels should carry grouping. Large display type should introduce a screen, not compete with the task on every section.

## Priority 0 — daily training flow

### 1. Compress the active workout header

The current header, four telemetry values and full-width END action use a large part of the first viewport before the exercise begins. Keep all telemetry, but place it in one compact live header and make END a clearly secondary toolbar action.

Expected benefit: the exercise name, weight/reps controls and Log Set action fit into one task-oriented viewport more reliably.

### 2. Make Log Set a stable bottom action

Weight, reps, set type and Log Set form the core loop. Keep Log Set fixed above the system gesture area while the exercise content scrolls. Completed sets, last session and other detail remain accessible without moving the primary action.

### 3. Group advanced controls without removing them

RPE, RIR, note, Plate Calculator and set type are valuable TrackGod functionality. Put them into a clearly labelled advanced block. Respect the existing Settings toggles for RPE and RIR. Set type remains next to Log Set because it changes logging semantics.

### 4. Keep post-set states in context

Rest Timer, PR feedback and progressive overload should layer over the current exercise instead of feeling like a navigation event. Warm-up sets continue to skip volume/PR/rest behavior as in the current logic.

## Priority 1 — navigation surfaces

### 5. Preserve all three Altar entry paths

The Altar has three distinct jobs: resume an unfinished session, start a Saved Ritual, or start a new workout. They should remain separate and ordered by urgency. Do not replace them with a single generic hero CTA.

### 6. Keep the real Exercise Picker hierarchy

The picker needs search, equipment, category and machine-brand filters, plus recently used, OCR scan, custom creation and long-press management. A two-row filter hierarchy is clearer than visually equivalent chips in one long stream.

### 7. Retain the seven-day History model

The real date picker is useful and should stay. Improve its tap targets and type size, then keep expand, rename, edit and delete inside each workout's detail state. Volume delta is supporting context, not a new primary metric.

### 8. Treat Performance and Personal as equal Stats modes

Performance has volume, exercise progress, PRs, heatmap, consistency, strength balance, muscle groups, frequency and the 1RM calculator. Personal has body, nutrition and training rhythm. Neither should disappear in a redesign.

## Priority 2 — system polish

- Raise critical labels that are currently around 8–9sp to a more comfortable minimum where space allows.
- Enforce at least 44dp interaction targets for arrows, chips and icon-only controls.
- Use one spacing scale and one border-strength scale across cards, filters and dialogs.
- Reserve `BloodBright` for active/important text. Repeated muted metadata should use the existing tertiary text color.
- Keep zero-radius geometry; it is part of TrackGod's identity and does not need soft consumer-app cards.
- Add explicit pressed, disabled, error and saving states to shared controls.
- Keep profile modules discoverable: Weight Loss, progress photos, milestones, My Gym, Backup & Restore, Settings and Privacy.

## Mockup coverage

The interactive prototype contains:

- Altar normal and incomplete-workout states, Saved Rituals and recent workouts.
- Active session with exercise switcher, telemetry, progressive overload, weight/reps, set types, RPE/RIR, note, plates, completed sets, last session, rest timer and PR state.
- Exercise Picker with equipment/category/brand filters, recent items, scan affordance and custom creation.
- Ritual completion with summary, Save as Ritual and discard path.
- History with search, seven-day picker, expanded exercise details, rename/edit/delete.
- Stats with five ranges and Performance/Personal modes.
- Profile hub with interactive Weight Loss, My Gym, Backup and Settings modules.

## Implementation order

1. Refactor only the Workout Session layout using existing ViewModel state and events.
2. Extract shared tokens/components for telemetry, filter chips, section headers and dense rows.
3. Apply the hierarchy to Altar and Exercise Picker.
4. Polish History and both Stats modes.
5. Normalize Profile submodules and accessibility states.

No production app code was changed for this concept.
