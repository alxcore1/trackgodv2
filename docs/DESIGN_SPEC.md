# TrackGod v2 -- Design Specification

> Version: 1.0 | Date: 2026-03-21
> Design System: Industrial Brutalism
> References: docs/Design/*, docs/PRD.md

---

## 1. Design Philosophy

**Industrial Brutalism** -- cold, heavy, visceral. The aesthetic of distressed metal and deathcore culture applied to interface design. This is not a friendly pastel fitness app. This is a digital altar for lifters who treat the gym like a battlefield.

### Core Principles

1. **Aggressive Precision** -- Sharp corners, tight spacing, extreme typographic scale contrasts
2. **Zero Radius** -- No rounded corners anywhere. Every element has hard, uncompromising edges (0px border-radius)
3. **Depth Through Darkness** -- Layering via surface color shifts, not shadows. 5 tiers of black/charcoal
4. **Red Is Sacred** -- Red (#8b0000) is used sparingly for active states, accents, and the brand. Never decorative
5. **Texture Over Flatness** -- Distressed metal noise overlay (5-8% opacity) on surfaces. Nothing is pristine
6. **No Scroll on Daily Screens** -- Dashboard (Altar) must fit on screen without scrolling. Data-dense screens (Stats, History) can scroll

---

## 2. Color System

### 2.1 Primary Palette

```
BACKGROUND (The Void)
#131313  ............  Base canvas. Used on all screen backgrounds
#0e0e0e  ............  Recessed areas (search bars, input fields, deepest containers)
#1c1b1b  ............  Surface container low (cards, list backgrounds)
#201f1f  ............  Surface container (mid-level elements)
#2a2a2a  ............  Surface container high (hover states, elevated cards)
#353534  ............  Surface container highest (active states, prominent elements)
#3a3939  ............  Surface bright (maximum elevation for non-primary elements)

RED (The Blood)
#8b0000  ............  Primary container. Active borders, CTAs, selected states, brand accent
#690000  ............  Text on primary backgrounds
#ffb4a8  ............  Primary bright. Accent text, labels, highlighted values
#ff907f  ............  Glow bleed (10% opacity for ambient glow effects)
#e3beb8  ............  On-surface variant (warm muted text)

TEXT (The Signal)
#e5e2e1  ............  Primary text. High contrast on dark backgrounds
#c8c6c6  ............  Secondary text (60-70% prominence)
#aa8984  ............  Outline/tertiary text. Labels, metadata, disabled
#5a403c  ............  Ghost borders (15% opacity outlines)
```

### 2.2 Semantic Colors

```
STATES
Error:    #ffb4ab  (reserved for actual errors only)
Success:  #8b0000  (red IS the success color in this system -- sets completed, PRs hit)
Warning:  #aa8984  (muted, not attention-grabbing)

NEVER USE
Blue, green, purple, or any color outside the red/black/gray palette.
No gradients. No color mixing. Flat, industrial surfaces only.
```

### 2.3 Application Rules

| Element | Color | Notes |
|---------|-------|-------|
| Screen background | #131313 | Always |
| Card background | #1c1b1b | Default surface |
| Card hover | #2a2a2a | Shift, not shadow |
| Input field background | #0e0e0e | Recessed, deepest |
| Input focus indicator | #8b0000 | Left border bar, 2-4px |
| Primary button background | #8b0000 | Solid fill |
| Primary button text | #e5e2e1 | High contrast |
| Secondary button background | transparent | Ghost/outline |
| Secondary button text | #aa8984 | Muted |
| Active nav tab | #8b0000 | Top border + glow |
| Inactive nav tab | #aa8984 | Muted icon + label |
| Selected card | #8b0000 bg | Full fill with glow |
| Left accent bar | #8b0000 | 4px wide, on active/featured items |
| Stat values | #e5e2e1 | font-black, large |
| Stat labels | #aa8984 | tiny, uppercase, tracked |
| Section headers | #ffb4a8 | Primary bright accent |

---

## 3. Typography

### 3.1 Font Stack

```
Headlines & UI:    Space Grotesk (weights: 300, 400, 700, 900)
Body & Secondary:  Work Sans (weights: 300, 400, 600)
```

Both are Google Fonts, free for commercial use, available on Android.

### 3.2 Type Scale

| Token | Size | Weight | Tracking | Case | Usage |
|-------|------|--------|----------|------|-------|
| display-xl | 48-56sp | 900 (Black) | -0.05em | UPPERCASE | Hero headings ("ARSENAL ANALYTICS") |
| display | 32-40sp | 900 (Black) | -0.03em | UPPERCASE | Screen titles, large stats |
| headline | 20-24sp | 900 (Black) | -0.02em | UPPERCASE | Section headers, card titles |
| title | 16-18sp | 700 (Bold) | 0em | UPPERCASE | Workout names, exercise names |
| body | 14sp | 400 (Regular) | 0em | Sentence | Descriptions, system notes |
| label | 10-12sp | 700 (Bold) | 0.2em | UPPERCASE | Metadata, stat labels, tags |
| micro | 8-10sp | 700 (Bold) | 0.3em | UPPERCASE | Timestamps, version numbers |

### 3.3 Typography Rules

- **Headlines** are always Space Grotesk Black (900) Italic for phase headings (e.g., "PHASE 03 // MISSION")
- **Labels** are always uppercase with wide letter-spacing (0.2-0.3em)
- **Body text** in Work Sans is the only place sentence case appears
- **Numbers** (stats, weights, reps) use Space Grotesk Black at display or headline size
- **Units** (kg, TONS, MIN, DAYS) are label-sized next to large stat numbers
- Extreme scale contrast creates tension: tiny 8px labels next to 48px stat values

---

## 4. Spacing & Layout

### 4.1 Spacing Scale

```
4px   ............  xs     (micro gaps, inline spacing)
8px   ............  sm     (tight element spacing)
12px  ............  md     (default gap between components)
16px  ............  lg     (section padding, card padding)
24px  ............  xl     (section gaps)
32px  ............  2xl    (major section separation)
```

### 4.2 Layout Rules

- **Screen padding**: 16px horizontal (px-4)
- **Card padding**: 12-16px internal
- **Grid**: 12-column system for bento layouts (Stats screen)
- **Bottom nav height**: 64-80px with safe area inset
- **Header height**: 56-64px
- **No rounded corners**: 0px border-radius on everything
- **No divider lines**: Separation via spacing or surface color shifts
- **No drop shadows**: Depth via glow bleeds (red, 10-30% opacity, 20-40px blur)

### 4.3 Content Density

- **Altar (Dashboard)**: Maximum density. Everything fits without scrolling. Compact stat cards, tight spacing.
- **Workout Session**: Medium density. Focus on the active input area. Completed sets scroll below.
- **History**: Scrollable list. Each entry is compact but expandable.
- **Stats**: Full scroll page. Bento grid of charts and metrics.
- **Profile**: Menu list. Minimal, clean, functional.

---

## 5. Component Library

### 5.1 Cards

**Standard Card**
```
Background: #1c1b1b
Border: none (or 1px #5a403c at 15% opacity ghost border)
Border-radius: 0px
Padding: 12-16px
Hover: background shifts to #2a2a2a
```

**Featured Card** (with accent)
```
Same as standard, plus:
Border-left: 4px solid #8b0000
```

**Selected Card**
```
Background: #8b0000
Text: #e5e2e1
Box-shadow: 0 0 25px rgba(139, 0, 0, 0.4) (red glow)
```

### 5.2 Buttons

**Primary Button**
```
Background: #8b0000
Text: #e5e2e1, Space Grotesk Black, uppercase, tracking-widest
Height: 48-56px
Border-radius: 0px
Active: scale(0.95), background shifts to #a30000
Icon: optional, right-aligned (e.g., arrow_forward)
```

**Secondary Button**
```
Background: #1c1b1b
Text: #aa8984
Border: 1px solid #353534
Active: scale(0.95), background shifts to #2a2a2a
```

**Ghost Button**
```
Background: transparent
Text: #aa8984
Hover: text shifts to #e5e2e1
Active: scale(0.95)
```

### 5.3 Input Fields

```
Background: #0e0e0e
Border: none
Border-left: 2px solid transparent (inactive), 2px solid #8b0000 (focused)
Text: #e5e2e1, Space Grotesk Bold
Placeholder: #aa8984, uppercase, tracked
Label: above field, #aa8984, 10px uppercase, 0.2em tracking
Padding: 12-16px
Height: 48-56px
```

### 5.4 Number Input (Weight/Reps)

```
Layout: [- button] [number display] [+ button]
Number: Space Grotesk Black, 24-32sp, centered
Buttons: 40x40px, #1c1b1b background, #ffb4a8 icon
Button active: scale(0.90), background #2a2a2a
Tap on number: opens direct keyboard input
```

### 5.5 Stat Card (Dashboard)

```
Layout: vertical stack
  [icon]  [label: 10px, #aa8984, uppercase]
  [value: 24-32sp, #e5e2e1, Space Grotesk Black]
  [unit: 10px, #aa8984]
Background: #1c1b1b
Size: flexible, typically 1/4 of grid width
Padding: 12px
```

### 5.6 Bottom Navigation

```
Background: #131313 (or #0e0e0e)
Height: 64px + safe area
Border-top: shadow glow 0 -4px 20px rgba(139, 0, 0, 0.1)
Items: 4 tabs equally spaced

Tab (inactive):
  Icon: 20px, #aa8984
  Label: 8px, #aa8984, uppercase, tracked

Tab (active):
  Icon: 20px, #ffb4a8
  Label: 8px, #ffb4a8
  Top border: 2px solid #8b0000
  Glow: 0 0 30px -10px rgba(139, 0, 0, 0.5)
```

### 5.7 Rest Timer Display

```
Layout: centered overlay or inline card
Countdown: Space Grotesk Black, 48-64sp, #ffb4a8
Label: "REST" in micro text above
Progress: linear bar underneath, #8b0000 fill on #353534 track
Skip button: ghost button, "SKIP" text
```

### 5.8 Confirmation Dialog

```
Background overlay: #000000 at 70% opacity
Dialog background: #1c1b1b
Border: 1px solid #353534
Title: headline size, #e5e2e1
Body: body size, #aa8984
Buttons: Primary (destructive/confirm) right, Secondary (cancel) left
Border-radius: 0px (sharp corners)
```

### 5.9 Section Divider

```
Layout: --- [LABEL TEXT] ---
Lines: 1px solid #353534, flex-grow on each side
Text: #aa8984, 10px uppercase, 0.3em tracking, centered
```

### 5.10 List Item (Exercise/Workout)

```
Background: #1c1b1b (alternating with #131313 for depth)
Padding: 16px
Border-left: 2px transparent → 2px #8b0000 on hover/selected
Hover: background shifts to #353534

Content:
  Title: Space Grotesk Bold, 16sp, uppercase, #e5e2e1
  Subtitle: Work Sans, 10px, uppercase, #aa8984, 0.2em tracking
  Right side: chevron or stat value
```

### 5.11 Search Bar

```
Background: #0e0e0e
Border-left: 2px solid #8b0000 (always visible)
Icon: search icon, #aa8984, left-aligned
Placeholder: "SEARCH 220+ WEAPONS...", #aa8984, uppercase, tracked
Text input: #e5e2e1, Space Grotesk Bold
Height: 48px
```

### 5.12 Filter Chips / Toggle Buttons

```
Layout: horizontal row, gap-8px

Inactive chip:
  Background: #353534
  Text: #aa8984, 10px, uppercase, tracked

Active chip:
  Background: #8b0000
  Text: #e5e2e1

Press: active:scale(0.95)
```

---

## 6. Texture & Effects

### 6.1 Distressed Metal Overlay

Applied globally at 5-8% opacity on all screens. Uses the `screen_bg.png` asset from v1 as a repeating texture, or generated via SVG fractal noise filter:

```xml
<svg>
  <filter id="metal-noise">
    <feTurbulence type="fractalNoise" baseFrequency="0.9" numOctaves="4"/>
  </filter>
</svg>
```

### 6.2 Logo Watermark

The "GOD" or "TG" text appears as a massive watermark (3% opacity) in the bottom-right corner of the Altar dashboard. Reinforces brand without interfering with content.

### 6.3 Red Glow Bleed

Instead of traditional elevation shadows:
```
Subtle:  box-shadow: 0 0 20px rgba(139, 0, 0, 0.1)
Medium:  box-shadow: 0 0 30px rgba(139, 0, 0, 0.2)
Strong:  box-shadow: 0 0 40px rgba(139, 0, 0, 0.4)
```

Used on: active nav tabs, selected cards, primary buttons on press, header bar.

### 6.4 Live Indicator Pulse

For active workout sessions:
```
Dot: 8px circle, #8b0000
Animation: pulse (scale 1.0 → 1.5, opacity 1.0 → 0.0) repeating every 1.5s
Label: "LIVE" in micro text next to dot
```

---

## 7. Screen Specifications

### 7.1 Splash Screen

```
┌──────────────────────────────┐
│                              │
│    RAGE. RIP. REPEAT.        │  ← micro text, 60% opacity, tracked wide
│                              │
│           ┌────┐             │
│           │ TG │             │  ← TG logo asset (Trackgod.png), large
│           │LOGO│             │     with subtle red glow
│           └────┘             │
│        TRACKGOD              │  ← wordmark, display-xl, red
│                              │
│     ┃ SYSTEM_INIT            │  ← red left bar, micro label
│     ┃ LOADING                │  ← headline, white, bold
│                              │
│  ┌────────────────────────┐  │
│  │ TAP TO ENTER THE ALTAR │  │  ← primary button, full-width
│  │            >>           │  │     with arrow icon
│  └────────────────────────┘  │
│                              │
│  VER: 2.0.0 · SECURE ACCESS │  ← micro text, 40% opacity
└──────────────────────────────┘

Background: #131313 with vignette (radial gradient darkening edges)
Texture: screen_bg.png at 5% opacity
Animation: Logo fades in (300ms), tagline types in, button slides up
```

### 7.2 Onboarding (Multi-Phase)

```
PHASE 01 // INTAKE -- "FORGE YOUR PROFILE"
┌──────────────────────────────┐
│  ✕   TRACKGOD          ⚙    │  ← header with close, logo, settings
├──────────────────────────────┤
│  PHASE 01 // INTAKE          │  ← micro label, #ffb4a8
│  FORGE YOUR                  │  ← display-xl, white
│  PROFILE                     │  ← display-xl, #ffb4a8 (red word)
│                              │
│        [avatar]              │  ← circular placeholder, camera icon
│     UPLOAD AVATAR            │  ← label text
│                              │
│  ┃ NAME                      │  ← input with red left bar
│  ┃ [________________]        │
│                              │
│  ┃ AGE                       │
│  ┃ [________________]        │
│                              │
│  ┃ WEIGHT (KG)               │
│  ┃ [________________]        │
│                              │
│  PRIMARY OBJECTIVE           │
│   ↓ LOSE WEIGHT              │  ← selectable cards
│   ⚡ GET FIT                  │
│   💪 GAIN MUSCLE  [selected] │  ← red bg + glow when selected
│                              │
│  ┌────────────────────────┐  │
│  │   INITIATE PROTOCOL >> │  │  ← primary button
│  └────────────────────────┘  │
└──────────────────────────────┘

Progress: Phase indicator bar (filled sections / total sections)
Each phase: separate screen with slide transition
Phases: Avatar+Name, Gender, Birthday, Height+Weight, Units, Objective, Experience, Weekly Target
Database seeding choice: after profile complete ("LOAD ARSENAL" screen)
```

### 7.3 Altar (Dashboard) -- NO SCROLL

```
┌──────────────────────────────┐
│  ≡   TRACKGOD           [av]│  ← menu icon, wordmark, user avatar
├──────────────────────────────┤
│ ┌─────────────────┐ ┌─────┐ │
│ │ WEEKLY RITUAL    │ │START│ │  ← 8-col + 4-col grid
│ │ GOAL       75%   │ │ NEW │ │     goal card + CTA button
│ │ M T W ● T F S S │ │  +  │ │     day dots (● = completed)
│ └─────────────────┘ └─────┘ │
│                              │
│ ┌──────┐ ┌──────┐           │
│ │⚡STRK │ │📊 VOL│           │  ← 2x2 stat grid
│ │ 12   │ │ 14.2 │           │     compact cards
│ │ DAYS │ │ TONS │           │
│ ├──────┤ ├──────┤           │
│ │🔥SETS│ │⏱ DUR │           │
│ │ 24   │ │ 72   │           │
│ │ REPS │ │ MIN  │           │
│ └──────┘ └──────┘           │
│                              │
│  ── PAST TRANSMISSIONS ──   │  ← section divider
│                              │
│ ┃ HEAVY BACK & TRAPS    >  │  ← last 3 workouts
│ ┃ 12.04.2024 · 14,500KG    │     with left accent bar
│                              │
│   DEADLIFT RITUAL        >  │
│   10.04.2024 · 12,200KG    │
│                              │
│   CHEST OBLITERATION     >  │
│   08.04.2024 · 11,800KG    │
│                              │
├──────────────────────────────┤
│  ALTAR  WORKOUT  STATS  PROF│  ← bottom nav, ALTAR active
└──────────────────────────────┘

KEY CONSTRAINT: Everything above fits on screen without scrolling.
Past Transmissions shows max 3 items (scrollable only within that section if needed).
Stat values are large (24-32sp), labels are tiny (10px).
"GOD" watermark at 3% opacity in bottom-right background.
```

### 7.4 Workout Session (Active)

```
┌──────────────────────────────┐
│  🏋 WORKOUT          LIVE ● │  ← header with pulsing live dot
├──────────────────────────────┤
│ EXERCISES│ SETS │VOLUME│TIME │  ← compact 4-col stats panel
│    08    │  24  │12.4k │48:12│     bordered grid, #0e0e0e bg
├──────────────────────────────┤
│ ┌──────────┐ ┌──────────┐   │
│ │ ▌▌ PAUSE │ │ ■  END   │   │  ← session controls
│ └──────────┘ └──────────┘   │     PAUSE = red, END = dark
├──────────────────────────────┤
│                              │
│  When no exercise selected:  │
│                              │
│  ── SELECT AN EXERCISE ──   │
│                              │
│  ┌────────────────────────┐  │
│  │  + CHOOSE EXERCISE     │  │  ← large tile buttons
│  │    BROWSE MANUALLY     │  │
│  └────────────────────────┘  │
│  ┌────────────────────────┐  │
│  │  + CHOOSE MACHINE      │  │
│  │    EQUIPMENT LOOKUP    │  │
│  └────────────────────────┘  │
│                              │
│  When exercise IS selected:  │
│                              │
│  BARBELL BENCH PRESS         │  ← exercise name, headline
│  Chest · Free Weight         │  ← category, label text
│                              │
│  ┌──────────────────────┐    │
│  │ [-]   80.0 kg   [+]  │    │  ← weight input with inc/dec
│  │ [-]    10       [+]  │    │  ← reps input with inc/dec
│  │ Note: ______________ │    │  ← optional note field
│  │                      │    │
│  │  ┌────────────────┐  │    │
│  │  │   LOG SET  ✓   │  │    │  ← primary button
│  │  └────────────────┘  │    │
│  └──────────────────────┘    │
│                              │
│  COMPLETED SETS              │  ← scrollable list below
│  Set 1: 80kg × 10           │     each row tappable to edit
│  Set 2: 80kg × 10           │
│  Set 3: 82.5kg × 8          │
│  Set 4: 82.5kg × 7 ← typo? │     tap to fix any set
│                              │
│  [NEXT EXERCISE]             │  ← go to picker
│                              │
│  ── REST TIMER ──            │  ← appears after set logged
│  01:23                       │     large countdown
│  [SKIP]                      │     ghost button to skip
│                              │
├──────────────────────────────┤
│  TRAIN   LOG   STATS   GEAR │  ← bottom nav
└──────────────────────────────┘

Smart defaults: weight/reps pre-filled from last session (subtle "LAST: 80kg × 10" hint).
RPE/RIR fields appear only if enabled in settings (below reps input).
Rest timer auto-starts after LOG SET (if enabled). Countdown with notification.
```

### 7.5 Exercise Picker

```
┌──────────────────────────────┐
│  🏋 WORKOUT              ✕  │  ← header with close button
├──────────────────────────────┤
│  ┃ SEARCH 220+ WEAPONS... 🔍│  ← search bar with red left border
│                              │
│  [MACHINE] [FREE WEIGHT]     │  ← filter toggle chips
│   active     inactive        │     (MACHINE = red bg when active)
│                              │
│  BARBELL BENCH PRESS      >  │  ← exercise list items
│  CHEST · FREE WEIGHT         │     title + category metadata
│                              │
│  INCLINE DUMBBELL PRESS   >  │
│  CHEST · FREE WEIGHT         │
│                              │
│  CHEST PRESS MACHINE      >  │
│  CHEST · MACHINE             │
│                              │
│  LAT PULLDOWN             >  │
│  BACK · MACHINE              │
│                              │
│  BARBELL ROW              >  │
│  BACK · FREE WEIGHT          │
│                              │
│  ... (scrollable)            │
│                              │
│  ┌────────────────────────┐  │
│  │  + ADD CUSTOM          │  │  ← bottom floating action
│  └────────────────────────┘  │
│                              │
│  [📷 SCAN MACHINE]           │  ← OCR secondary action (subtle)
│                              │
├──────────────────────────────┤
│  TRAIN   LOG   STATS   GEAR │
└──────────────────────────────┘

List sorted by usage frequency (most-used first).
Hover/focus: left border transitions from transparent to #8b0000.
Scrollbar thumb: #8b0000 (custom styled, 4px wide).
Category filter chips: All, Chest, Back, Shoulders, Arms, Legs, Core (horizontal scroll).
```

### 7.6 History (WORKOUT Tab)

```
┌──────────────────────────────┐
│  ≡   TRACKGOD           [av]│
├──────────────────────────────┤
│  ┃ FIND PAST TRANSMISSIONS 🔍│  ← search bar
│                              │
│  [SUN] [MON] [TUE] [WED]... │  ← horizontal date picker
│   22    22    23    24       │     selected day = red bg
│               ↑ selected     │     scrollable
│                         [📅] │  ← calendar picker icon
│                              │
│  ── VERIFIED HISTORY ──     │
│                              │
│ ┃ CHEST & TRICEPS    12,450 │  ← workout card (featured)
│ ┃ JULY 22, 2024      KG VOL │     left red border on first
│ ┃ ⏱ 74 MIN  🔥 24 SETS     │     metadata row with icons
│ ┃  01 BARBELL BENCH   100x8 │     expandable exercise list
│ ┃  02 SKULL CRUSHERS   40x12│
│ ┃  03 CABLE FLYES      25x15│
│                              │
│   BACK & BICEPS      15,800 │  ← second workout card
│   JULY 20, 2024             │     no left border (unfeatured)
│   ⏱ 82 MIN  🔥 28 SETS     │
│    01 CONVENTIONAL DL  140x5│
│    02 LAT PULLDOWN      80x10│
│    03 DB CURLS          18x12│
│                              │
│   LEG DESTRUCTION    22,100 │
│   JULY 18, 2024             │
│   ...                       │
│                              │
│  (scrollable, paginated)     │
│                              │
├──────────────────────────────┤
│  ALTAR  WORKOUT  STATS  PROF│  ← WORKOUT tab active
└──────────────────────────────┘

Date picker: horizontal scroll of dates. Today highlighted.
Tap a date to filter workouts for that day.
Calendar icon opens month view picker.
Workouts show exercise list collapsed by default, tap to expand.
Long-press workout for actions (edit name, delete).
```

### 7.7 Stats (STATS Tab) -- Full Scroll

```
┌──────────────────────────────┐
│  ≡   TRACKGOD           [av]│
├──────────────────────────────┤
│  ARSENAL                     │
│  ANALYTICS         142.8K    │  ← hero heading, total volume
│  Performance Protocol Active │     "ANALYTICS" in #ffb4a8
│  ┃                           │     left red border
│                              │
│  [MONTHLY ▾] [ALL-TIME]      │  ← time range filter
│                              │
│ ┌────────────────────────────┐
│ │ VOLUME PROGRESSION         │  ← bar chart (7 bars)
│ │ ████ ████ ████ ████        │     showing weekly volume
│ │ ██   ████ ██   ████ ████   │     red bars (#8b0000)
│ │ MON  TUE  WED  THU  FRI   │     labels below
│ └────────────────────────────┘
│                              │
│ ┌──────────┐ ┌──────────────┐│
│ │CONSISTENCY│ │ HEATMAP     ││  ← bento grid: 2 cards
│ │ 90-DAY   │ │ ■■■■■■■     ││     consistency = streak
│ │ ■■□■■■■  │ │ ■■□■■■■     ││     heatmap = 90-day grid
│ │ ■■■■□■■  │ │ ■■■■□■■     ││     colored cells by intensity
│ └──────────┘ └──────────────┘│
│                              │
│ ┌────────┐┌────────┐┌──────┐│
│ │ SQUAT  ││ BENCH  ││DEAD- ││  ← PR cards (3 columns)
│ │        ││        ││ LIFT ││
│ │  405   ││  315   ││      ││     large stat number
│ │  LBS   ││  LBS   ││ 495  ││     unit label below
│ └────────┘└────────┘│ LBS  ││
│                     └──────┘│
│                              │
│ ┌────────────────────────────┐
│ │ STRENGTH BALANCE           │  ← horizontal bar chart
│ │ UPPER █████████░░ 65%      │     comparing upper/lower/core
│ │ LOWER ████░░░░░░░ 25%      │
│ │ CORE  ██░░░░░░░░░ 10%      │
│ └────────────────────────────┘
│                              │
│ ┌────────────────────────────┐
│ │ MUSCLE LOAD DISTRIBUTION   │  ← category percentage grid
│ │ CHEST 32% │ BACK  28%     │
│ │ LEGS  20% │ ARMS  12%     │
│ │ SHOULDERS 5% │ CORE 3%    │
│ └────────────────────────────┘
│                              │
│ ┌────────────────────────────┐
│ │ MOST EXECUTED RITES        │  ← horizontal bar ranking
│ │ DEADLIFT    ████████████   │     top exercises by frequency
│ │ SQUAT       ██████████     │
│ │ BENCH       ████████       │
│ │ PULL-UPS    ██████         │
│ └────────────────────────────┘
│                              │
├──────────────────────────────┤
│  ALTAR  WORKOUT  STATS  PROF│
└──────────────────────────────┘

This is the one screen that is designed to scroll.
Bento grid layout -- asymmetric card sizes create visual interest.
All charts use #8b0000 (red) as the data color.
Chart backgrounds are #1c1b1b or #0e0e0e.
Time range filter affects all charts simultaneously.
```

### 7.8 Profile (PROFILE Tab)

```
┌──────────────────────────────┐
│  ≡   TRACKGOD           [av]│
├──────────────────────────────┤
│                              │
│     [AVATAR]                 │  ← large avatar or initials
│     USER NAME                │  ← headline
│     GAIN MUSCLE              │  ← label, #ffb4a8
│     Member since Jan 2024    │  ← micro text, #aa8984
│                              │
│  ── ACCOUNT ──              │
│   Edit Profile            >  │
│   Privacy Policy          >  │
│                              │
│  ── GOALS ──                │
│   Weight Loss Journey     >  │
│                              │
│  ── DATA ──                 │
│   Backup & Restore        >  │
│   Export Database          >  │
│                              │
│  ── APP ──                  │
│   Settings                >  │
│   About                   >  │
│                              │
├──────────────────────────────┤
│  ALTAR  WORKOUT  STATS  PROF│  ← PROFILE tab active
└──────────────────────────────┘

Clean, functional menu. No bloat.
Each item navigates to its own dedicated screen.
Section headers use the standard divider component.
Menu items use list item component with chevron.
```

---

## 8. Brand Assets to Preserve

### 8.1 Must-Keep Assets (copy from v1 to v2)

| Asset | V1 Path | Usage in V2 |
|-------|---------|-------------|
| TG Logo (full, on black) | `assets/images/icons/Trackgod.png` | Splash screen, about screen |
| TG Logo (no background) | `assets/images/icons/Trackgod_no_bg.png` | Watermark overlay (3-5% opacity) |
| TG Icon (compact) | `assets/images/icons/Trackgod_icon.png` | App icon base, nav header |
| App Icon | `assets/images/icons/Trackgod_app_icon.png` | Play Store, launcher icon |
| Screen Background | `assets/images/backgrounds/screen_bg.png` | Global texture overlay (5-8% opacity) |
| Top Bar Background | `assets/images/backgrounds/topbar_bg.png` | Accent texture for CTA buttons |
| Viking Skull | `assets/images/icons/viking_skull.png` | Secondary brand element, empty states |
| Notification Icon | `android/res/drawable-*/notification_icon.png` | Notification small icon |

### 8.2 Assets to Retire

| Asset | Reason |
|-------|--------|
| Achievement images (01-04) | Achievement system removed |
| Cardio SVG icons (swimming, running, etc.) | No cardio in v2 |
| Celebration plate images | Over-the-top celebration removed |
| Onboarding background | Replaced by new onboarding design |
| Body camera overlay | Progress photo feature kept, but redesign overlay |

### 8.3 New Assets Needed

| Asset | Purpose |
|-------|---------|
| Weight lifting SVG icon | For exercise picker, used as weight_lifting icon |
| Dumbbell icon | For bottom nav WORKOUT tab |
| Altar icon | For bottom nav ALTAR tab (custom, ritual-themed) |
| Chart icon | For bottom nav STATS tab |
| User icon | For bottom nav PROFILE tab |
| PR crown/badge | Subtle indicator when a personal record is hit |
| Adaptive icon foreground | Android adaptive icon (TG mark on transparent) |
| Adaptive icon background | Solid #131313 or #8b0000 |

---

## 9. Interaction Patterns

### 9.1 Transitions

| Action | Transition | Duration |
|--------|-----------|----------|
| Screen push (forward) | Slide in from right | 200ms |
| Screen pop (back) | Slide out to right | 200ms |
| Tab switch | Crossfade | 150ms |
| Modal open | Slide up from bottom | 250ms |
| Modal close | Slide down | 200ms |
| Dialog appear | Fade in + scale(0.95 → 1.0) | 150ms |

### 9.2 Touch Feedback

| Element | Feedback |
|---------|----------|
| Buttons | scale(0.95) on press, 100ms |
| Cards | background color shift to next surface tier |
| List items | left border appears (#8b0000, 2px) |
| Icons | scale(0.90) on press |
| Tabs | instant color change, no delay |

### 9.3 Haptic Feedback

| Event | Haptic |
|-------|--------|
| Set logged | Medium impact |
| PR achieved | Heavy impact |
| Rest timer complete | Heavy impact + vibration pattern |
| Button press | Light impact |
| Workout finished | Heavy impact |

### 9.4 PR Celebration

When a personal record is detected (new estimated 1RM higher than previous):
- Stat value briefly flashes #ffb4a8 (bright red)
- Small "PR" badge appears next to the set
- Medium haptic impact
- No full-screen animation, no confetti, no modal. Subtle and respectful.

---

## 10. Responsive Considerations

### Screen Size Targets

| Device Class | Width | Adaptation |
|-------------|-------|------------|
| Compact | 360-400dp | Default layout. All wireframes target this. |
| Medium | 400-600dp | Stat cards can be slightly larger. Same layout. |
| Large / Tablet | 600dp+ | Not optimized for v2.0. Same phone layout centered. |

### Safe Areas

- Top: respect system status bar (use `WindowInsets.statusBars`)
- Bottom: respect navigation bar + bottom nav height
- Edge-to-edge: extend background color behind system bars

---

## 11. Dark Mode

There is no light mode. TrackGod is permanently dark. The #131313 background is the identity. Any "theme toggle" setting is intentionally excluded.
