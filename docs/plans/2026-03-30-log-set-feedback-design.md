# Log Set Button — Visual Feedback Design

**Date:** 2026-03-30
**Origin:** Gym session feedback from training partner

## Problem

The "LOG SET" button gives too little visual feedback after tapping. Especially after multiple sets, it's unclear whether the set was actually logged. The button has only a subtle 0.95x scale animation (60ms) — no color change, no confirmation state.

## Solution: Checkmark Morph + List Slide-In

Two complementary signals — one on the button, one in the list.

### 1. Button: Full Checkmark Morph

**Flow:**
1. User taps "LOG SET"
2. Text "LOG SET" crossfades to a single checkmark icon (centered, same size area)
3. Button background briefly brightens/saturates (~300ms)
4. After ~400-500ms total, checkmark crossfades back to "LOG SET"

**Details:**
- Checkmark completely replaces the text (no text visible during confirmation)
- Use `AnimatedContent` with crossfade transition
- Button remains enabled during animation (no blocking rapid logging)
- Timing: ~400-500ms total for the morph cycle
- No haptic feedback (explicit requirement)

### 2. Completed Sets List: Smooth Slide-In with Highlight

**Flow:**
1. New set entry slides into the completed sets list (from bottom or with vertical expand)
2. Entry has a subtle highlight (slightly lighter background or faint border glow)
3. Highlight fades out over ~500ms

**Details:**
- Use `AnimatedVisibility` with `slideInVertically` + `fadeIn`
- Highlight: temporary lighter `SurfaceLow` background or faint `Blood.copy(alpha=0.15f)` overlay
- Should feel like the set "lands" in the list naturally

## Constraints

- No haptic feedback
- No popups, toasts, or snackbars
- Must not block rapid set logging (set-set-set rhythm)
- Must feel natural, not gamified or annoying

## Affected Files

- `TrackGodButton.kt` — add confirmation state variant or new composable
- `WorkoutSessionScreen.kt` — wire up animation trigger, animate list items
