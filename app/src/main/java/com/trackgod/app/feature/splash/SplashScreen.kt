package com.trackgod.app.feature.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.trackgod.app.R
import com.trackgod.app.ui.component.TrackGodButton
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextSecondary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.TrackGodTheme
import com.trackgod.app.ui.theme.Void
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onEnter: () -> Unit = {},
    onEnterOnboarding: () -> Unit = {},
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val isReady by viewModel.isReady.collectAsState()
    val hasProfile by viewModel.hasProfile.collectAsState()

    // ── Animation states ─────────────────────────────────────────────────────

    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.85f) }

    // Blockbuster word-by-word tagline
    var showRage by remember { mutableStateOf(false) }
    var showRip by remember { mutableStateOf(false) }
    var showRepeat by remember { mutableStateOf(false) }
    val rageAlpha = remember { Animatable(0f) }
    val ripAlpha = remember { Animatable(0f) }
    val repeatAlpha = remember { Animatable(0f) }
    val rageScale = remember { Animatable(1.4f) }
    val ripScale = remember { Animatable(1.4f) }
    val repeatScale = remember { Animatable(1.4f) }

    // Cyberpunk init sequence
    val initAlpha = remember { Animatable(0f) }
    var initPhase by remember { mutableIntStateOf(0) }
    var hexLine1 by remember { mutableStateOf("") }
    var hexLine2 by remember { mutableStateOf("") }
    var initStatus by remember { mutableStateOf("") }

    val ctaAlpha = remember { Animatable(0f) }
    val ctaOffsetY = remember { Animatable(60f) }
    val footerAlpha = remember { Animatable(0f) }

    // Cyberpunk hex generator
    fun randomHex(len: Int): String {
        val chars = "0123456789ABCDEF"
        return (1..len).map { chars.random() }.joinToString("")
    }

    LaunchedEffect(Unit) {
        // Phase 1: Logo fades in (0-600ms)
        launch {
            logoAlpha.animateTo(1f, animationSpec = tween(600))
        }
        launch {
            logoScale.animateTo(1f, animationSpec = tween(700))
        }

        // Phase 2: Blockbuster word drops (600ms, 900ms, 1200ms)
        launch {
            delay(500)
            showRage = true
            launch { rageAlpha.animateTo(1f, tween(200)) }
            launch { rageScale.animateTo(1f, tween(250)) }
        }
        launch {
            delay(800)
            showRip = true
            launch { ripAlpha.animateTo(1f, tween(200)) }
            launch { ripScale.animateTo(1f, tween(250)) }
        }
        launch {
            delay(1100)
            showRepeat = true
            launch { repeatAlpha.animateTo(1f, tween(200)) }
            launch { repeatScale.animateTo(1f, tween(250)) }
        }

        // Phase 3: Cyberpunk init sequence (1400ms+)
        launch {
            delay(1300)
            initAlpha.animateTo(1f, tween(200))

            // Rapid hex cycling
            val phases = listOf(
                "LOADING KERNEL" to 6,
                "MOUNTING /dev/altar" to 5,
                "SYNCING NEURAL_LINK" to 5,
                "CHECKING INTEGRITY" to 4,
                "DECRYPTING PROTOCOL" to 5,
                "INJECTING PAYLOAD" to 4,
                "CALIBRATING SENSORS" to 3,
                "SYSTEM ARMED" to 2,
            )

            for ((status, ticks) in phases) {
                initPhase++
                initStatus = status
                for (t in 0 until ticks) {
                    hexLine1 = "0x${randomHex(4)} ${randomHex(8)} ${randomHex(4)}"
                    hexLine2 = ">> ${randomHex(2)}:${randomHex(2)}:${randomHex(2)} [${randomHex(6)}]"
                    delay(60)
                }
            }

            // Final state
            hexLine1 = "0xDEAD BEEF C0DE"
            hexLine2 = ">> ALL SYSTEMS NOMINAL"
            initStatus = "READY"
        }

        // Phase 4: CTA slides up
        launch {
            delay(2800)
            launch { ctaAlpha.animateTo(1f, tween(400)) }
            launch { ctaOffsetY.animateTo(0f, tween(400)) }
        }

        // Footer
        launch {
            delay(3000)
            footerAlpha.animateTo(1f, tween(300))
        }
    }

    // ── Layout ───────────────────────────────────────────────────────────────

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Void),
    ) {
        // Original v1 start screen background (has TG logo baked in)
        Image(
            painter = painterResource(R.drawable.start_screen_bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.35f),
            contentScale = ContentScale.Crop,
        )

        // Vignette overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Void.copy(alpha = 0.75f),
                        ),
                        radius = 900f,
                    ),
                ),
        )

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(0.08f))

            // ── Blockbuster tagline: ABOVE the logo ────────────────────
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (showRage) {
                    Text(
                        text = "RAGE.",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp,
                        ),
                        color = TextPrimary,
                        modifier = Modifier.graphicsLayer {
                            alpha = rageAlpha.value
                            scaleX = rageScale.value
                            scaleY = rageScale.value
                        },
                    )
                }
                if (showRage) Spacer(Modifier.width(16.dp))
                if (showRip) {
                    Text(
                        text = "RIP.",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp,
                        ),
                        color = BloodBright,
                        modifier = Modifier.graphicsLayer {
                            alpha = ripAlpha.value
                            scaleX = ripScale.value
                            scaleY = ripScale.value
                        },
                    )
                }
                if (showRip) Spacer(Modifier.width(16.dp))
                if (showRepeat) {
                    Text(
                        text = "REPEAT.",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp,
                        ),
                        color = TextPrimary,
                        modifier = Modifier.graphicsLayer {
                            alpha = repeatAlpha.value
                            scaleX = repeatScale.value
                            scaleY = repeatScale.value
                        },
                    )
                }
            }

            // Logo area -- the background image has the TG logo baked in
            Spacer(modifier = Modifier.weight(1f))

            // ── Cyberpunk init sequence: BELOW the logo ────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(initAlpha.value),
                horizontalAlignment = Alignment.Start,
            ) {
                // Status label
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(14.dp)
                            .background(Blood),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "SYSTEM_INIT // PHASE $initPhase",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 2.sp,
                        ),
                        color = TextTertiary,
                    )
                }

                Spacer(Modifier.height(6.dp))

                // Hex lines (monospace cyberpunk feel)
                Text(
                    text = hexLine1,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                    ),
                    color = Blood.copy(alpha = 0.6f),
                )
                Text(
                    text = hexLine2,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                    ),
                    color = TextTertiary.copy(alpha = 0.5f),
                )

                Spacer(Modifier.height(8.dp))

                // Current status
                Text(
                    text = initStatus,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp,
                    ),
                    color = if (initStatus == "READY") BloodBright else TextPrimary,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── CTA button ───────────────────────────────────────────────
            TrackGodButton(
                text = if (isReady && initStatus == "READY") "TAP TO ENTER THE ALTAR" else "INITIALIZING...",
                onClick = {
                    if (hasProfile) onEnter() else onEnterOnboarding()
                },
                enabled = isReady && initStatus == "READY",
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(ctaAlpha.value)
                    .offset { IntOffset(0, ctaOffsetY.value.dp.roundToPx()) },
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Footer ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
                    .alpha(footerAlpha.value),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "VER: 2.0.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(Blood, shape = RectangleShape),
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "SECURE ACCESS ONLY",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun SplashScreenPreview() {
    TrackGodTheme {
        SplashScreen()
    }
}
