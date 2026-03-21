# Phase 1: Foundation -- Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Create a buildable Android app that launches a splash screen, then navigates to a 4-tab shell with the full TrackGod Industrial Brutalism design system applied. Room database initialized with all tables ready.

**Architecture:** Single Activity + Compose Navigation. MVVM with Repository pattern. Room for persistence. Hilt for DI. All code in `com.trackgod.app` package.

**Tech Stack:** Kotlin 2.0+, Jetpack Compose (BOM 2024.12+), Room, Hilt, Compose Navigation, Material 3, Coil, Gradle with Version Catalogs.

**Design Reference:** `docs/DESIGN_SPEC.md` is the source of truth for all colors, typography, spacing, and components.

**Parallel Execution:** Tasks 1 must complete first. Tasks 2, 3, 4 can run in parallel. Task 5 depends on 2+4. Task 6 depends on 2+4+5. Task 7 validates everything.

---

### Task 1: Android Project Scaffolding

**Goal:** Buildable empty Compose project with all dependencies declared.

**Files to Create:**
```
TracGod_v2/
  settings.gradle.kts
  build.gradle.kts (root)
  gradle.properties
  gradle/
    libs.versions.toml
  app/
    build.gradle.kts
    proguard-rules.pro
    src/main/
      AndroidManifest.xml
      java/com/trackgod/app/
        TrackGodApplication.kt
        MainActivity.kt
      res/
        values/strings.xml
        values/themes.xml
```

**Step 1: Create `gradle/libs.versions.toml` (Version Catalog)**

```toml
[versions]
agp = "8.7.3"
kotlin = "2.1.0"
ksp = "2.1.0-1.0.29"
compose-bom = "2024.12.01"
compose-compiler = "1.5.15"
room = "2.6.1"
hilt = "2.53.1"
hilt-navigation-compose = "1.2.0"
navigation-compose = "2.8.5"
coil = "2.7.0"
lifecycle = "2.8.7"
coroutines = "1.9.0"
core-ktx = "1.15.0"
activity-compose = "1.9.3"

[libraries]
# Compose
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }

# Navigation
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation-compose" }

# Lifecycle
lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hilt-navigation-compose" }

# Coil
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }

# Core
core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "core-ktx" }
activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activity-compose" }
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }

# Google Fonts
compose-google-fonts = { group = "androidx.compose.ui", name = "ui-text-google-fonts" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
room = { id = "androidx.room", version.ref = "room" }
```

**Step 2: Create `settings.gradle.kts`**

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolution {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TrackGod"
include(":app")
```

**Step 3: Create root `build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.room) apply false
}
```

**Step 4: Create `gradle.properties`**

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

**Step 5: Create `app/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.room)
}

android {
    namespace = "com.trackgod.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.trackgod.v2"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "2.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    // Navigation
    implementation(libs.navigation.compose)

    // Lifecycle
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Coil
    implementation(libs.coil.compose)

    // Core
    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(libs.coroutines.android)

    // Google Fonts
    implementation(libs.compose.google.fonts)
}
```

**Step 6: Create `app/proguard-rules.pro`**

```proguard
# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
```

**Step 7: Create `app/src/main/AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.USE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

    <application
        android:name=".TrackGodApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.TrackGod">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.TrackGod"
            android:launchMode="singleTask"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

**Step 8: Create `app/src/main/res/values/strings.xml`**

```xml
<resources>
    <string name="app_name">TrackGod</string>
</resources>
```

**Step 9: Create `app/src/main/res/values/themes.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.TrackGod" parent="android:Theme.Material.NoActionBar">
        <item name="android:windowBackground">#131313</item>
        <item name="android:statusBarColor">#131313</item>
        <item name="android:navigationBarColor">#131313</item>
        <item name="android:windowLightStatusBar">false</item>
    </style>
</resources>
```

**Step 10: Create `TrackGodApplication.kt`**

```kotlin
package com.trackgod.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TrackGodApplication : Application()
```

**Step 11: Create `MainActivity.kt`**

```kotlin
package com.trackgod.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.trackgod.app.ui.theme.TrackGodTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrackGodTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF131313)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("TRACKGOD v2", color = Color(0xFF8B0000))
                }
            }
        }
    }
}
```

**Step 12: Create Gradle wrapper**

Run: Download gradle wrapper files or create them. The agent should run:
```bash
cd /c/Projects/TracGod_v2 && gradle wrapper --gradle-version 8.9
```

If `gradle` is not available, create the wrapper files manually.

**Step 13: Verify build compiles**

Run:
```bash
cd /c/Projects/TracGod_v2 && ./gradlew assembleDebug
```
Expected: BUILD SUCCESSFUL (may take a while on first run to download dependencies)

**Step 14: Commit**

```bash
git init && git add -A && git commit -m "feat: initial project scaffolding with Compose, Room, Hilt, Navigation"
```

---

### Task 2: Theme System

**Goal:** Complete TrackGod Industrial Brutalism theme: colors, typography (Space Grotesk + Work Sans via Google Fonts), shapes, spacing.

**Depends on:** Task 1

**Files to Create:**
```
app/src/main/java/com/trackgod/app/ui/theme/
  Color.kt
  Type.kt
  Shape.kt
  Spacing.kt
  Theme.kt
```

**Step 1: Create `Color.kt`**

Reference: `docs/DESIGN_SPEC.md` Section 2

```kotlin
package com.trackgod.app.ui.theme

import androidx.compose.ui.graphics.Color

// Background -- The Void
val Void = Color(0xFF131313)
val VoidDeep = Color(0xFF0E0E0E)
val SurfaceLow = Color(0xFF1C1B1B)
val SurfaceMid = Color(0xFF201F1F)
val SurfaceHigh = Color(0xFF2A2A2A)
val SurfaceHighest = Color(0xFF353534)
val SurfaceBright = Color(0xFF3A3939)

// Red -- The Blood
val Blood = Color(0xFF8B0000)
val BloodDeep = Color(0xFF690000)
val BloodBright = Color(0xFFFFB4A8)
val BloodGlow = Color(0xFFFF907F)
val BloodWarm = Color(0xFFE3BEB8)

// Text -- The Signal
val TextPrimary = Color(0xFFE5E2E1)
val TextSecondary = Color(0xFFC8C6C6)
val TextTertiary = Color(0xFFAA8984)
val GhostBorder = Color(0xFF5A403C)

// Semantic
val ErrorColor = Color(0xFFFFB4AB)
```

**Step 2: Create `Type.kt`**

```kotlin
package com.trackgod.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp

val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = com.trackgod.app.R.array.com_google_android_gms_fonts_certs
)

val SpaceGrotesk = FontFamily(
    Font(googleFont = GoogleFont("Space Grotesk"), fontProvider = fontProvider, weight = FontWeight.Light),
    Font(googleFont = GoogleFont("Space Grotesk"), fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Space Grotesk"), fontProvider = fontProvider, weight = FontWeight.Bold),
    Font(googleFont = GoogleFont("Space Grotesk"), fontProvider = fontProvider, weight = FontWeight.Black),
)

val WorkSans = FontFamily(
    Font(googleFont = GoogleFont("Work Sans"), fontProvider = fontProvider, weight = FontWeight.Light),
    Font(googleFont = GoogleFont("Work Sans"), fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Work Sans"), fontProvider = fontProvider, weight = FontWeight.SemiBold),
)

val TrackGodTypography = Typography(
    // Display XL -- hero headings
    displayLarge = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Black,
        fontSize = 48.sp,
        letterSpacing = (-0.5).sp,
    ),
    // Display -- screen titles, large stats
    displayMedium = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Black,
        fontSize = 36.sp,
        letterSpacing = (-0.3).sp,
    ),
    // Display small -- medium stats
    displaySmall = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Black,
        fontSize = 28.sp,
        letterSpacing = (-0.2).sp,
    ),
    // Headline -- section headers, card titles
    headlineLarge = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Black,
        fontSize = 24.sp,
        letterSpacing = (-0.2).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Black,
        fontSize = 20.sp,
        letterSpacing = (-0.1).sp,
    ),
    // Title -- workout names, exercise names
    titleLarge = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
    ),
    // Body -- descriptions, system notes
    bodyLarge = TextStyle(
        fontFamily = WorkSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = WorkSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
    ),
    // Label -- metadata, stat labels, tags
    labelLarge = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 2.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 2.sp,
    ),
    // Micro -- timestamps, version numbers
    labelSmall = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 8.sp,
        letterSpacing = 3.sp,
    ),
)
```

**Step 3: Create Google Fonts certificate resource**

Create `app/src/main/res/values/font_certs.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <array name="com_google_android_gms_fonts_certs">
        <item>@array/com_google_android_gms_fonts_certs_dev</item>
        <item>@array/com_google_android_gms_fonts_certs_prod</item>
    </array>
    <string-array name="com_google_android_gms_fonts_certs_dev">
        <item>MIIEqDCCA5CgAwIBAgIJANWFuGx90071MA0GCSqGSIb3DQEBBAUAMIGUMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNTW91bnRhaW4gVmlldzEQMA4GA1UEChMHQW5kcm9pZDEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDEiMCAGCSqGSIb3DQEJARYTYW5kcm9pZEBhbmRyb2lkLmNvbTAeFw0wODA0MTUyMzM2NTZaFw0zNTA5MDEyMzM2NTZaMIGUMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNTW91bnRhaW4gVmlldzEQMA4GA1UEChMHQW5kcm9pZDEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDEiMCAGCSqGSIb3DQEJARYTYW5kcm9pZEBhbmRyb2lkLmNvbTCCASAwDQYJKoZIhvcNAQEBBQADggENADCCAQgCggEBANbOLggKv+IxTdGNs8/TGFy0PTP6DHThvbbR24kT9ixcOd9W+EaBPWW+wPPKQmsHxajtWjmQwWfna8mZuSeJS48LIgAZlKkpFeVyxW0qMBujb8X8ETrWy550PaFtI6t9+u7hZeTfHwqNvacKhp1RbE6dBRGWynwMVX8XW8N1+UjFaq6GCJukT4qmpN2afb8sCjUigq0GuMwYXrFVee74bQgLHWGJwPmvmLHC69EH6kWr22ijx4OKXlSIx2xT1AsSHee70w5iDBiK4aph27yH3TxkXy9V89TDdexAcKk/cVHYNnDBapcavl7y0RiQ4biu8ymM8Ga/nmzhRKya6G0cGw8CAQOjgfwwgfkwHQYDVR0OBBYEFI0cxb6VBER2NsyfGTx0PSt06ViXMIHJBgNVHSMEgcEwgb6AFI0cxb6VBER2NsyfGTx0PSt06ViXoYGapIGXMIGUMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNTW91bnRhaW4gVmlldzEQMA4GA1UEChMHQW5kcm9pZDEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDEiMCAGCSqGSIb3DQEJARYTYW5kcm9pZEBhbmRyb2lkLmNvbYIJANWFuGx90071MAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQEEBQADggEBABnTDPEF+3iSP0wNfdIjIz1AlnrPzgAIHVvXxunW7SBR/HA8pSq0P5CO7e1EfDojYfnxSSWBMhCEHma7GCOcMY5IBgLCIBXOC0Z02u9gOiMaY0csq1ou8hHKRNGbqVNMHBhAK0JK1FQGmUzKoLOGOiVvb2hMjYLJtkVWuwVhAzVDmHxFMq3MO46ypGSM0mJc/7DqHbMXkjaDBJSM2u7h4MWWBR+m1+9K8JhWu5fMAo3vflBcS7raGNIY5mRUPy0bm6gfFE6EJFn3LS3GXIxH2eEO/SJzHN1F1wV1R/VnNabGSf/cO9IKQG0hQ6OQXW3q7pvn1MOakpTP0dFBw==</item>
    </string-array>
    <string-array name="com_google_android_gms_fonts_certs_prod">
        <item>MIIEQzCCAyugAwIBAgIJAMLgh0ZkSjCNMA0GCSqGSIb3DQEBBAUAMHQxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1Nb3VudGFpbiBWaWV3MRQwEgYDVQQKEwtHb29nbGUgSW5jLjEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDAeFw0wODA4MjEyMzEzMzRaFw0zNjAxMDcyMzEzMzRaMHQxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1Nb3VudGFpbiBWaWV3MRQwEgYDVQQKEwtHb29nbGUgSW5jLjEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDCCASAwDQYJKoZIhvcNAQEBBQADggENADCCAQgCggEBAKtWLgDYO6IIrgqWbxJOKdoR8qtW0I9Y4sypEwPpt1TTcvZApxsdyxMJZ2JORland2qSGT2y5b+3JKkedxiLDmpHpDsz2WCbdxgxRczfey5YZnTJ4VZbH0xqWVW/8lGmPav5xVwnIiJS6HXk+BVKZF+JcWjAsb/GEuq/eFdpuzSqeYTcfi6idkyugwfYwXFU1+5fZKUaRKYCwkkFQVfcAs1fXA5V+++FGfvjJ/CxURaSxaBvGdGDhfXE28LWuT9ozCl5xw4Yq5OGazvV24mZVSoOO0yZ31j7kYvtwYK6NeADwbSxDdJEqO4k//0zOHKrUiGYXtqw/A0LFFtqoZKFjnkCAQOjgdkwgdYwHQYDVR0OBBYEFMd2jMiZLPHqdGSCBFVMhn5HPHJBMIGGBGNVHSMEQDA+oXykejB4MQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNTW91bnRhaW4gVmlldzEUMBIGA1UEChMLR29vZ2xlIEluYy4xEDAOBgNVBAsTB0FuZHJvaWQxEDAOBgNVBAMTB0FuZHJvaWSCCQDC4IdGZEowjTAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBBAUADggBAQBt0lLO74UwLDYKDEj17hcXLbg/EqnRJiAI+FPN2U9NOgD1pWtJ4Ztcihaz7+VVx6Cig8YDfTOOEG0gODNEmKr8jQVAsFmH+oEE4TNqX6B5Z0CC3vlQ7ux+cFN7HPOIPkHjL0qRCthNOyjsifeC2s+Q8OtJNwq82ceiV7VhL5DJpbGfweHmjRkmVNwHb5JVxlvMCXafJSLz+/zCQnlBJJGGVA7UFM2v2vSgalFN2oohGkYkG1TiVI4Fr12ICBVZA8x7Ia+ALoBQXBE0kcBIAFpKfbBqQhiLJPhVyXov/LDN7PoEHDfFvcjFJ4PzgCX9M8sg4/Gp7QcqLj0R/hX8VfE</item>
    </string-array>
</resources>
```

**Step 4: Create `Shape.kt`**

```kotlin
package com.trackgod.app.ui.theme

import androidx.compose.foundation.shape.RectangleShape
import androidx.compose.material3.Shapes

// Industrial Brutalism: ZERO radius everywhere
val TrackGodShapes = Shapes(
    extraSmall = RectangleShape,
    small = RectangleShape,
    medium = RectangleShape,
    large = RectangleShape,
    extraLarge = RectangleShape,
)
```

**Step 5: Create `Spacing.kt`**

```kotlin
package com.trackgod.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Spacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }

val Spacing.screenPadding: Dp get() = lg
```

**Step 6: Create `Theme.kt`**

```kotlin
package com.trackgod.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val TrackGodColorScheme = darkColorScheme(
    primary = Blood,
    onPrimary = TextPrimary,
    primaryContainer = Blood,
    onPrimaryContainer = BloodBright,
    secondary = BloodBright,
    onSecondary = BloodDeep,
    background = Void,
    onBackground = TextPrimary,
    surface = SurfaceLow,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceMid,
    onSurfaceVariant = TextSecondary,
    outline = TextTertiary,
    outlineVariant = GhostBorder,
    error = ErrorColor,
    onError = Void,
    surfaceContainerLowest = VoidDeep,
    surfaceContainerLow = SurfaceLow,
    surfaceContainer = SurfaceMid,
    surfaceContainerHigh = SurfaceHigh,
    surfaceContainerHighest = SurfaceHighest,
)

@Composable
fun TrackGodTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        MaterialTheme(
            colorScheme = TrackGodColorScheme,
            typography = TrackGodTypography,
            shapes = TrackGodShapes,
            content = content,
        )
    }
}
```

**Step 7: Verify build with theme**

Update `MainActivity.kt` to use `TrackGodTheme` and verify it compiles.

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 8: Commit**

```bash
git add -A && git commit -m "feat: TrackGod Industrial Brutalism theme system (colors, typography, shapes, spacing)"
```

---

### Task 3: Room Database

**Goal:** All 8 tables defined as Room entities with DAOs and Hilt-injected database.

**Depends on:** Task 1

**Files to Create:**
```
app/src/main/java/com/trackgod/app/core/database/
  TrackGodDatabase.kt
  entity/
    UserProfileEntity.kt
    ExerciseEntity.kt
    WorkoutEntity.kt
    SetEntity.kt
    BodyMetricEntity.kt
    WeightLossGoalEntity.kt
    WeightLossMilestoneEntity.kt
    BackupMetadataEntity.kt
  dao/
    UserProfileDao.kt
    ExerciseDao.kt
    WorkoutDao.kt
    SetDao.kt
    BodyMetricDao.kt
    WeightLossDao.kt
    BackupDao.kt
  converter/
    Converters.kt
app/src/main/java/com/trackgod/app/core/di/
  DatabaseModule.kt
```

Implement all entities exactly as specified in `docs/ARCHITECTURE.md` Section 3 (Entity Relationship Diagram). All DAOs with the key queries from Section 3.3. DatabaseModule provides the Room database and all DAOs via Hilt `@Singleton`.

Reference the ARCHITECTURE.md for exact column names, types, indexes, and foreign key relationships.

**Verify:** `./gradlew assembleDebug` compiles. Room schema exports to `app/schemas/`.

**Commit:** `git add -A && git commit -m "feat: Room database with 8 entities, 7 DAOs, Hilt DI module"`

---

### Task 4: Core Composables

**Goal:** Reusable UI building blocks matching the Industrial Brutalism design spec.

**Depends on:** Task 2 (theme must exist)

**Files to Create:**
```
app/src/main/java/com/trackgod/app/ui/component/
  TrackGodCard.kt
  TrackGodButton.kt
  TrackGodTextField.kt
  NumberInput.kt
  StatCard.kt
  SectionDivider.kt
  BottomNavBar.kt
  EmptyState.kt
```

Each component follows the specs from `docs/DESIGN_SPEC.md` Section 5 (Component Library). Key implementation notes:

- **TrackGodCard**: Surface with `#1c1b1b` background, 0px radius, optional red left border (4px `#8b0000`), press-to-darken interaction
- **TrackGodButton**: Primary (solid `#8b0000`), Secondary (ghost/outline), both with `scale(0.95)` on press via `Modifier.pointerInput` + `animateFloatAsState`
- **TrackGodTextField**: `#0e0e0e` background, no border except left accent bar (`#8b0000` when focused, transparent when not), label above in uppercase tracked text
- **NumberInput**: `[-]  value  [+]` layout with `#1c1b1b` buttons, tappable center to type directly
- **StatCard**: Vertical stack of icon + value (large) + unit (tiny label), used in dashboard grid
- **SectionDivider**: `--- LABEL TEXT ---` with `#353534` lines and centered `#aa8984` text
- **BottomNavBar**: 4 tabs, active has top red border + glow, inactive is `#aa8984`. Uses `NavigationBar` from Material 3 customized to match spec
- **EmptyState**: Centered icon + title + subtitle + optional action button

All text in components should use `uppercase()` transform where the design spec calls for it. All shapes are `RectangleShape` (0px radius enforced by theme).

**Verify:** Build compiles. Create a simple preview composable showing all components.

**Commit:** `git add -A && git commit -m "feat: core composable components (cards, buttons, inputs, nav bar, stat cards)"`

---

### Task 5: Navigation Shell

**Goal:** 4-tab bottom navigation with placeholder screens. Tapping tabs switches content.

**Depends on:** Tasks 2, 4

**Files to Create:**
```
app/src/main/java/com/trackgod/app/ui/navigation/
  Screen.kt
  TrackGodNavHost.kt
app/src/main/java/com/trackgod/app/feature/
  altar/AltarScreen.kt
  history/HistoryScreen.kt
  stats/StatsScreen.kt
  profile/ProfileScreen.kt
```

**Step 1: Create `Screen.kt`** -- sealed class with all routes (see ARCHITECTURE.md Section 4)

**Step 2: Create placeholder screens** -- each shows the screen name in `displayMedium` typography centered on `#131313` background. Use `TrackGodCard` and `SectionDivider` to show the design system is working.

Example `AltarScreen.kt`:
```kotlin
@Composable
fun AltarScreen(onStartWorkout: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(LocalSpacing.current.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "ALTAR",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(LocalSpacing.current.md))
        Text(
            text = "DASHBOARD COMING SOON",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}
```

**Step 3: Create `TrackGodNavHost.kt`** -- NavHost with bottom nav using `BottomNavBar` component from Task 4. Wire up all 4 tab destinations.

**Step 4: Update `MainActivity.kt`** -- Replace placeholder content with `TrackGodNavHost`.

**Step 5: Verify navigation works**

Run: `./gradlew assembleDebug && ./gradlew installDebug`
Expected: App launches, shows 4 tabs at bottom. Tapping each tab shows that screen's name. The TrackGod theme is visibly applied (dark background, red accents, industrial typography).

**Commit:** `git add -A && git commit -m "feat: 4-tab navigation shell (Altar, Workout, Stats, Profile) with TrackGod theme"`

---

### Task 6: Splash Screen

**Goal:** Branded splash screen with TG logo, "RAGE. RIP. REPEAT.", and "TAP TO ENTER THE ALTAR" CTA. Auto-navigates to main app or shows CTA for first launch.

**Depends on:** Tasks 2, 4, 5

**Files to Create:**
```
app/src/main/java/com/trackgod/app/feature/splash/
  SplashScreen.kt
app/src/main/res/drawable/
  trackgod_logo.png        (copy from v1: Trackgod_no_bg.png)
  screen_bg.png             (copy from v1: screen_bg.png)
```

**Step 1: Copy brand assets from v1**

```bash
mkdir -p app/src/main/res/drawable
cp "/c/Projects/TrackGod/app/assets/images/icons/Trackgod_no_bg.png" app/src/main/res/drawable/trackgod_logo.png
cp "/c/Projects/TrackGod/app/assets/images/backgrounds/screen_bg.png" app/src/main/res/drawable/screen_bg.png
cp "/c/Projects/TrackGod/app/assets/images/icons/Trackgod_app_icon.png" app/src/main/res/mipmap-xxxhdpi/ic_launcher.png
```

Note: may need to resize/rename assets to comply with Android resource naming (lowercase, no special chars).

**Step 2: Create `SplashScreen.kt`**

Layout per `docs/DESIGN_SPEC.md` Section 7.1:
- Full screen `#131313` background with `screen_bg.png` as faint texture overlay (5% opacity via `alpha`)
- "RAGE. RIP. REPEAT." in `labelLarge`, `#c8c6c6` (60% opacity), centered near top
- TG logo centered, large, with subtle red glow (use `drawBehind` with red blur or simply a colored shadow)
- "TRACKGOD" wordmark below logo in `displayLarge`, `#8b0000`
- Red left bar + "SYSTEM_INIT" in `labelMedium` + "LOADING" in `headlineMedium`
- "TAP TO ENTER THE ALTAR >>" primary button at bottom
- "VER: 2.0.0" and "SECURE ACCESS ONLY" in `labelSmall` at very bottom

Animations (simple for now):
- Logo fades in over 500ms (`animateFloatAsState` on alpha)
- Button slides up from bottom over 300ms (`animateDpAsState` on offset)

Navigation: On button tap, navigate to main NavHost (replacing splash in backstack so user can't go back to it).

**Step 3: Add splash as start destination in NavHost**

Update `TrackGodNavHost` to set `startDestination = Screen.Splash.route`. After splash, navigate to the main scaffold with bottom nav.

**Step 4: Verify splash flow**

Run: `./gradlew installDebug`
Expected: App opens to splash screen with TG logo, tagline, and CTA. Tapping the button navigates to the 4-tab main app. Back button from main app does NOT return to splash.

**Commit:** `git add -A && git commit -m "feat: splash screen with TG branding, animations, and navigation to main app"`

---

### Task 7: Integration Verification

**Goal:** Confirm the entire Phase 1 deliverable works end-to-end.

**Depends on:** All previous tasks

**Verification checklist:**

- [ ] App builds without errors (`./gradlew assembleDebug`)
- [ ] App installs on device/emulator (`./gradlew installDebug`)
- [ ] Splash screen appears with TG logo, "RAGE. RIP. REPEAT.", red accents
- [ ] "TAP TO ENTER THE ALTAR" button works
- [ ] Main screen shows 4-tab bottom nav: ALTAR, WORKOUT, STATS, PROFILE
- [ ] Tabs switch correctly, each shows placeholder content
- [ ] Active tab has red top border with glow effect
- [ ] Typography is Space Grotesk (sharp, industrial), not default sans
- [ ] Colors match spec: `#131313` background, `#8b0000` red accents, `#e5e2e1` white text
- [ ] All corners are sharp (0px radius) -- no rounded elements anywhere
- [ ] Room database initializes without crash (check logcat for Room)
- [ ] Back button from main app exits (doesn't go to splash)
- [ ] Edge-to-edge: content extends behind status bar and nav bar

**Fix any issues found, then final commit:**

```bash
git add -A && git commit -m "feat: Phase 1 complete -- foundation with theme, navigation, database, and splash screen"
```

---

## Execution Notes

**Task Parallelism:**
```
Task 1 (scaffolding)
  ├── Task 2 (theme)     ─┐
  ├── Task 3 (database)   ├── Task 5 (navigation) ── Task 6 (splash) ── Task 7 (verify)
  └── Task 4 (components) ─┘
```

Tasks 2, 3, 4 are fully independent and can run as parallel sub-agents after Task 1 completes.

**Testing on Device:** The user wants to see and test the app at each phase. After Task 5 at minimum, the app should be installable and visually demonstrate the TrackGod design system.

**Design Priority:** The user emphasized design and look is the main focus. Typography, colors, and component styling must match `docs/DESIGN_SPEC.md` precisely. A functionally correct but visually wrong component is a failure.
