# Hyperframes Composition Brief: TrackGod

## Objective
Create a short launch-style brag video for TrackGod, a hardcore offline Android gym tracker.

## Output
- Composition directory: `brag-output/composition/`
- Rendered video: `brag-output/brag.mp4`
- Format: vertical — 1080x1920
- Duration: 20 seconds

## Source Material
- Project root: `C:\Projects\TracGod_v2`
- Primary files read: `README.md`, `ui/theme/Color.kt`, `ui/theme/Type.kt`, `feature/workout/session/WorkoutSessionScreen.kt`, `feature/altar/AltarScreen.kt`, `strings.xml`
- Product name: TrackGod
- Tagline / strongest claim: "Hardcore gym tracker. 100% offline." / "NO INTERNET PERMISSION" / "Just iron."
- Key UI or visual moment to recreate: the live workout-session card (weight × reps, RPE/RIR chips, LIVE timer, NEW PR badge) and the Altar home (THE ALTAR AWAITS, a weekly Ritual card, a streak counter).
- Copy that must appear verbatim:
  - RAGE. RIP. REPEAT.
  - TrackGod
  - Hardcore gym tracker. 100% offline.
  - THE ALTAR AWAITS
  - WEEKLY RITUAL
  - NEW PR
  - LIVE
  - NO INTERNET PERMISSION
  - Just iron.

## Creative Direction
- Tone preset: app-store
- Creative direction: premium feature spot for a brutalist, occult-flavored iron cult — corporate-clean structure wearing blood-and-void warpaint.
- Interpretation: clean title-case feature cards and smooth slide/wipe transitions, but every surface uses the real Void/Blood palette and Space Grotesk Black. Confident pacing; each feature gets a readable hold; nothing chaotic.
- Angle: Every other fitness app wants your email, your location, and a subscription. TrackGod wants none of it. Feature-forward launch spot wearing the app's real brutalist face — near-black void, blood red, all-caps display type, mantra RAGE. RIP. REPEAT.
- Hook: black void → a blood-red line ignites with RAGE. RIP. REPEAT. → the TrackGod wordmark drops.
- Outro / punchline: NO INTERNET PERMISSION → TrackGod wordmark, blood underline, "Just iron."
- Avoid:
  - Generic SaaS language
  - Abstract filler visuals
  - Unrelated visual redesign (stay on the real palette + type)

## Visual Identity
- Background: #131313 (Void); recessed #0E0E0E; cards #1C1B1B
- Text: #E5E2E1 primary; #C8C6C6 secondary; #BF9D97 tertiary/labels
- Accent: #8B0000 (Blood); #FFB4A8 bright accent text; #FF907F glow
- Display font: Space Grotesk (700/900) via Google Fonts; fallback `system-ui, sans-serif`
- Body font: Work Sans (400/600) via Google Fonts; fallback `system-ui, sans-serif`
- Visual references from the project: all-caps labels with +2px letter-spacing, blood underline under the wordmark, workout-session card with weight×reps row + RPE/RIR chips + LIVE timer, GitHub-style blood-red heatmap grid.

## Storyboard
Use the storyboard in `brag-output/brag-plan.md` as the creative contract.

Scene summary (vertical, 20s total):
1. Hook / Ignite — 3s — blood line ignites; "RAGE. RIP. REPEAT." (hold ~1s)
2. Wordmark reveal — 3s — "TrackGod" drops + blood underline; subtitle "Hardcore gym tracker. 100% offline." (hold ~1.4s)
3. The Altar — 3.5s — "THE ALTAR AWAITS", WEEKLY RITUAL card slides in, STREAK counts 0→12 DAYS
4. Live set logging — 4s — session card: 100kg × 5, RPE 8 / RIR 2 chips, LIVE 12:34 timer; set row appears, then "NEW PR" badge slams in (hold ~1s)
5. Feature cards — 4s — three cards arrive one by one (each holds ≥0.8s): OCR Machine Scan · Live PR Detection · Workout Heatmap (blood-red grid)
6. Privacy + outro — 2.5s — "NO INTERNET PERMISSION" (hold ~1s) → "TrackGod" wordmark + blood underline + "Just iron."

## Audio
- Audio role: steady, clean, controlled bed + heavy iron-flavored SFX accents that carry the gym weight.
- Audio arc: bed enters under the ignite, holds steady through Altar and live logging, lands a heavy plate-slam on NEW PR, ticks cleanly through feature cards, fades on the final wordmark.
- Music: `happy-beats-business-moves-vol-12-by-ende-dot-app.mp3` (steadiest/cleanest bundled track; the upbeat library tracks are the only bundled option — vol-12 is least bouncy and reads as a controlled bed under the dark visuals). Volume 0.30.
- Music treatment: start 0s, steady ~0.30, fade out over the final ~1.5s under the outro wordmark.
- Music cue guidance: bundled preset `assets/music/cues/happy-beats-business-moves-vol-12-by-ende-dot-app.music-cues.json` (tempo ~110 BPM). Strong-cue locks: NEW PR slam → 13.11s; final wordmark → 18.56s. Beat grid for feature cards: card 1 ~14.20s, card 2 ~15.29s, card 3 ~16.38s (every-other-beat spacing so text stays readable). Wordmark drop aligns to beat ~3.27s.
- Audio-reactive treatment: subtle — a gentle blood-red glow breathe on the wordmark and NEW PR badge. NOTE: the hyperframes audio-reactive extraction helper is not installed in this environment (`hyperframes skills update` failed on a CLI version mismatch), so true per-frame RMS extraction is unavailable; implement the breathe as a tasteful low-amplitude CSS glow pulse instead. Documented per step-3 ("if extraction is unavailable… note it and skip — do not block the render"). No waveform/equalizer visuals.
- Audio-coupled moments:
  - Scene 1 ignite — heavy metal clang as the blood line draws
  - Scene 2 wordmark drop — deep bell on the drop
  - Scene 3 — soft drop as the Ritual card slides in
  - Scene 4 — crisp click on set-log; heavy plate-slam on NEW PR (strong cue)
  - Scene 5 — one soft drop per feature card (beat-grid)
  - Scene 6 — deep bell on final wordmark (strong cue), then music fade
- SFX selection guidance: heavy but clean. Iron/metal impacts for ignite and PR (the brand is literally iron plates); deep bell for logo moments; soft drops for cards. No comedic SFX, no whooshes on every element.
- SFX analysis guidance: `~/.claude/plugins/cache/brag/brag/0.1.0/skills/brag/assets/sfx/sfx-analysis.md` — prefer low/medium high-frequency-risk files for repeated/polished moments.
- Exact SFX choice: Hyperframes picks final filenames/timestamps/volumes against the implemented animation.
- Audio files: copy the chosen music + SFX into `brag-output/composition/assets/`.

## Hyperframes Instructions
Use the current hyperframes CLI workflow and conventions (root `data-composition-id`, `class="clip"` on timed elements, single paused GSAP timeline on `window.__timelines`, no Date.now/Math.random/fetch).

Requirements:
- Show at least one real UI element from the project (the live session card + Altar card both qualify).
- Keep all text readable — fast-in then hold; hook line and feature cards hold ≥0.8s.
- 20s total, vertical 1080x1920.
- Include the music bed + SFX layer; music on track 10, SFX on ascending track indices 11+.
- 2 strong-cue locks (NEW PR 13.11s, final wordmark 18.56s); feature cards on the beat grid; mark with `// beat-locked` / `// beat-grid`.
- Music fade-out under the final wordmark.
- Audio-reactive: subtle CSS glow breathe (extraction helper unavailable, documented above).
- Run `npm run check` (lint + validate + inspect) and fix errors before render.
