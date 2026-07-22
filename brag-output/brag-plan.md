# Brag Plan: TrackGod

## What is this app?
TrackGod is a hardcore, 100%-offline Android gym tracker — no accounts, no cloud, no ads, no analytics. It logs sets with weight/reps/RPE/RIR, detects PRs live mid-workout, scans gym-machine labels with OCR, and visualizes everything in a brutalist "blood on the void" interface where workouts are Rituals and your history is a wall of Past Transmissions.

## The angle
Every other fitness app wants your email, your location, and a $9.99/mo subscription. TrackGod wants none of it. It's a feature-forward "app store" launch spot — clean feature cards, smooth reveals — but wearing the app's real face: near-black void, blood red, all-caps Space Grotesk, and the mantra **RAGE. RIP. REPEAT.** The contrast (corporate-clean structure + occult-gym brand) is the whole identity. Specific because no generic tracker looks or talks like this.

## Hook (first 2-3 seconds)
Black screen. A single red line ignites: **RAGE. RIP. REPEAT.** then the wordmark **TrackGod** drops in. Subtitle settles: *Hardcore gym tracker. 100% offline.* Sets the tone before a single feature.

## Key moments (the middle)
- **The Altar (home):** "THE ALTAR AWAITS" with a weekly Ritual card + workout streak counting up.
- **Live set logging:** the working-app centerpiece — `100kg × 5`, RPE/RIR chips, a **LIVE** timer ticking, then a **NEW PR** badge slams in. The product *doing* its thing.
- **Feature cards (app-store beat):** OCR machine scan · PR detection · GitHub-style heatmap — each a clean card arriving one by one.
- **The privacy flex:** "NO INTERNET PERMISSION" — the claim no competitor can make.

## Outro / punchline
Wordmark **TrackGod** centered on the void, blood underline, tagline *Just iron.* → Play Store framing implied.

## User flow worth showing
Entry → key action → result: **The Altar** (start a Ritual) → **log a set live** (weight × reps, RPE/RIR, LIVE timer) → **NEW PR detected** + heatmap fills. The centerpiece scenes (3 & 4) are the working app, not marketing.

## Tone
- Preset: app-store
- Creative direction: "premium feature spot for a brutalist, occult-flavored iron cult — corporate-clean structure wearing blood-and-void warpaint"
- Interpretation: clean title-case feature cards and smooth slide/wipe transitions, but every surface uses the real Void/Blood palette and Space Grotesk Black. Confident pacing, no chaos, each feature gets a readable hold.

## Format: vertical — 1080x1920
## Duration: 20s

## Visual identity (from the project)
- Background: #131313 (Void); recessed #0E0E0E (VoidDeep); cards #1C1B1B (SurfaceLow)
- Accent: #8B0000 (Blood); bright accent text #FFB4A8 (BloodBright); glow #FF907F (BloodGlow)
- Text: #E5E2E1 (TextPrimary); secondary #C8C6C6; tertiary/labels #BF9D97
- Display font: Space Grotesk (Black weight, all-caps labels, negative tracking on headlines; labels at +2sp tracking)
- Body font: Work Sans (Normal)
- Strongest visual element: the all-caps blood-on-void wordmark + live workout-session card with weight×reps, RPE/RIR chips, LIVE timer, and the NEW PR badge

## Share copy (draft)
TrackGod: a hardcore gym tracker with no accounts, no cloud, and literally no internet permission. Just iron. 🩸

## Audio direction
- Role: dark percussive bed — low, driving, gym-warmup energy. Not EDM hype; controlled and heavy.
- Music: dark/heavy electronic or industrial percussion bed; pick the closest bundled track. None only if disabled.
- Music treatment: start ~0s under the hook ignite, hold steady mid volume through features, light fade-out on the final wordmark hold.
- Music cue guidance: bundled track + preset — read cue preset from `assets/music/cues/` if present; otherwise cues detected at composition time. Target a strong cue at the hook wordmark drop (~1.5s), the NEW PR slam (~12s), and the final wordmark (~18s). Feature cards (scene 5) can ride a beat-grid window but each card text holds ≥0.8s.
- Audio-reactive treatment: subtle — let bass energy give the blood-red glow a faint breathe on the wordmark and PR badge; no waveform bars.
- SFX posture: moderate, motion-matched, professional restraint — UI tick when a set logs, soft chip taps on RPE/RIR, one heavier impact on NEW PR and on the logo drop.
- Audio-coupled moments: hook wordmark drop, set-log tick, NEW PR slam, feature cards arriving one by one, final logo.
- Restraint rule: no comedic SFX, no whooshes on every element, no music spikes. Heavy but clean.

## Storyboard

### Scene 1 — Hook / Ignite — 3s
Pure #131313 void. A thin blood-red line draws across; **RAGE. RIP. REPEAT.** types/fades in (all-caps, +2sp tracking, BloodBright). Settle ~1s.
Sequential/interaction: none.
Audio intent: low bed enters; a single low impact as the line ignites.
Audio-coupled idea: impact on the red line ignite.
Music: dark percussive bed begins.
Transition mood: smooth wipe → Scene 2

### Scene 2 — Wordmark reveal — 3s
**TrackGod** drops to center (Space Grotesk Black, TextPrimary) with a blood underline; subtitle settles below: *Hardcore gym tracker. 100% offline.* Hold subtitle ~1.4s.
Sequential/interaction: none.
Audio intent: a heavier hit lands with the wordmark drop.
Audio-coupled idea: impact on wordmark drop; subtle glow breathe on the underline.
Music: bed steady.
Transition mood: smooth slide → Scene 3

### Scene 3 — The Altar (home) — 3.5s
Recreate the home: label **THE ALTAR AWAITS**, a **WEEKLY RITUAL** card (e.g. "Heavy Back & Traps", "6 EXERCISES"), and a **STREAK** stat counting up (e.g. 0→12 DAYS). Card slides in, streak number ticks.
Sequential/interaction: yes — Ritual card slides in, then STREAK number counts up.
Audio intent: card arrival tick; soft rising ticks as the streak counts.
Audio-coupled idea: counter ticks on the streak count-up.
Music: bed steady.
Transition mood: smooth slide → Scene 4

### Scene 4 — Live set logging (centerpiece) — 4s
Recreate the workout session card: exercise name, a completed-sets row showing **100kg × 5**, **RPE 8** / **RIR 2** chips, a **LIVE** timer reading e.g. 12:34. A set logs (row appears with a tick), then a **NEW PR** badge slams in (BloodBright, faint glow). Hold the PR badge ~1s.
Sequential/interaction: yes — set row appears on a tick, then NEW PR badge slams in.
Audio intent: crisp UI tick on the set log, soft chip taps, one heavier impact on NEW PR.
Audio-coupled idea: set-log tick + NEW PR slam impact.
Music: bed steady; PR slam aligns to a strong cue.
Transition mood: smooth wipe → Scene 5

### Scene 5 — Feature cards — 4s
Three clean app-store feature cards arrive one by one (each holds ≥0.8s):
1. **OCR Machine Scan** — *Scan a gym machine label, auto-pick the exercise.*
2. **Live PR Detection** — *Know the moment you beat your best.*
3. **Workout Heatmap** — a small GitHub-style blood-red contribution grid.
Sequential/interaction: yes — 3 feature cards arrive one by one, each with a soft card tick.
Audio intent: one soft tick per card; the heatmap cells can lightly populate to the beat grid.
Audio-coupled idea: card-by-card arrival ticks; heatmap fill on beat-grid.
Music: bed steady; cards ride a beat-grid window but text holds for reading.
Transition mood: smooth slide → Scene 6

### Scene 6 — Privacy flex + outro — 2.5s
Full-bleed void: **NO INTERNET PERMISSION** (BloodBright) holds ~1s, then resolves to the centered **TrackGod** wordmark with blood underline and tagline *Just iron.* Light fade.
Sequential/interaction: none.
Audio intent: bed resolves; a final clean impact on the wordmark, then fade.
Audio-coupled idea: impact on final logo; subtle glow breathe.
Music: light fade-out on the wordmark hold.
Transition mood: soft fade → end

**Music mood for this video:** dark / heavy / driving percussion bed (controlled, not chaotic).
**Audio summary:** A low industrial bed opens on the blood-red ignite, drives steadily through the Altar and live logging, lands a heavy hit on NEW PR, ticks cleanly through the feature cards, and fades on the final "Just iron." wordmark.
