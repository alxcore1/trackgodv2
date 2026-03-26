# TrackGod

**Hardcore gym tracker. Offline. No ads. No tracking. Just iron.**

TrackGod is a privacy-first workout tracker for Android built with Jetpack Compose, Room, and Hilt. All data stays on your device — no accounts, no cloud, no analytics.

## Features

**Workout Tracking**
- Log sets with weight, reps, RPE and RIR
- PR detection during workouts
- Progressive overload suggestions based on session history
- Set types: working, warmup, drop sets, failure sets
- Superset support
- Rest timer with exact alarm notifications
- OCR scanner — scan gym machine labels to auto-select exercises
- Plate calculator for quick barbell loading
- Save workouts as reusable templates (Rituals)

**Stats & Analytics**
- Volume progression charts
- GitHub-style workout heatmap
- Per-exercise progression tracking
- 1RM calculator (Epley, Brzycki, Lander)
- Strength balance analysis by muscle group
- Personal records overview

**Weight Loss Tracking**
- Weight goals with target dates
- Daily weigh-ins with progress photos
- BMR/TDEE calculator
- Milestone tracking
- Before/after photo comparison

**Privacy**
- 100% offline — no internet permission
- No Firebase, no analytics SDKs, no crash reporting
- Local database + SharedPreferences only
- Full data export (database backup + CSV)
- Delete all data anytime

## Tech Stack

- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM with Hilt DI
- **Database**: Room (SQLite) with migrations
- **Navigation**: Compose Navigation with typed routes
- **Camera**: CameraX + ML Kit Text Recognition (OCR)
- **Background**: WorkManager (auto-backup), Foreground Service (workout timer)
- **Charts**: Vico + custom Canvas composables
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 35 (Android 15)

## Building

```bash
# Debug APK
./gradlew assembleDebug

# Release AAB (requires keystore in local.properties)
./gradlew bundleRelease
```



## Privacy Policy

https://alxcore1.github.io/trackgodv2/privacy-policy.html

## License

All rights reserved. © 2026 Alexander Michor
