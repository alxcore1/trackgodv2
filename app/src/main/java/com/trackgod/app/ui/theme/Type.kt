package com.trackgod.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.trackgod.app.R

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

// === Space Grotesk — headlines, UI, stats ===
private val spaceGrotesk = GoogleFont("Space Grotesk")

val SpaceGroteskFamily = FontFamily(
    Font(googleFont = spaceGrotesk, fontProvider = fontProvider, weight = FontWeight.Light),
    Font(googleFont = spaceGrotesk, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = spaceGrotesk, fontProvider = fontProvider, weight = FontWeight.Bold),
    Font(googleFont = spaceGrotesk, fontProvider = fontProvider, weight = FontWeight.Black),
)

// === Work Sans — body text ===
private val workSans = GoogleFont("Work Sans")

val WorkSansFamily = FontFamily(
    Font(googleFont = workSans, fontProvider = fontProvider, weight = FontWeight.Light),
    Font(googleFont = workSans, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = workSans, fontProvider = fontProvider, weight = FontWeight.SemiBold),
)

// === Typography Scale ===
val TrackGodTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Black,
        fontSize = 48.sp,
        letterSpacing = (-0.5).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Black,
        fontSize = 36.sp,
        letterSpacing = (-0.3).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Black,
        fontSize = 28.sp,
        letterSpacing = (-0.2).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Black,
        fontSize = 24.sp,
        letterSpacing = (-0.2).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Black,
        fontSize = 20.sp,
        letterSpacing = (-0.1).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = WorkSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = WorkSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 2.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 2.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 8.sp,
        letterSpacing = 3.sp,
    ),
)
