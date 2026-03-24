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

    // Blockbuster word-by-word (v1 timings: 600ms gap, 500ms fade each)
    val word1Alpha = remember { Animatable(0f) }
    val word2Alpha = remember { Animatable(0f) }
    val word3Alpha = remember { Animatable(0f) }

    // Cyberpunk init sequence
    val initAlpha = remember { Animatable(0f) }
    var initPhase by remember { mutableIntStateOf(0) }
    var hexLine1 by remember { mutableStateOf("") }
    var hexLine2 by remember { mutableStateOf("") }
    var initStatus by remember { mutableStateOf("") }

    val ctaAlpha = remember { Animatable(0f) }
    val ctaOffsetY = remember { Animatable(60f) }
    val footerAlpha = remember { Animatable(0f) }

    fun randomHex(len: Int): String {
        val chars = "0123456789ABCDEF"
        return (1..len).map { chars.random() }.joinToString("")
    }

    LaunchedEffect(Unit) {
        // v1 timings: words at 1200ms, 1800ms, 2400ms with 500ms fade
        launch {
            delay(1200)
            word1Alpha.animateTo(1f, tween(500))
        }
        launch {
            delay(1800)
            word2Alpha.animateTo(1f, tween(500))
        }
        launch {
            delay(2400)
            word3Alpha.animateTo(1f, tween(500))
        }

        // Cyberpunk init starts simultaneously with words
        launch {
            delay(1200)
            initAlpha.animateTo(1f, tween(200))

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

            hexLine1 = "0xDEAD BEEF C0DE"
            hexLine2 = ">> ALL SYSTEMS NOMINAL"
            initStatus = "READY"
        }

        // CTA slides up after init
        launch {
            delay(3600)
            launch { ctaAlpha.animateTo(1f, tween(400)) }
            launch { ctaOffsetY.animateTo(0f, tween(400)) }
        }

        launch {
            delay(3800)
            footerAlpha.animateTo(1f, tween(300))
        }
    }

    // Auto-navigate for returning users (skip the button tap)
    LaunchedEffect(isReady, hasProfile, initStatus) {
        if (isReady && hasProfile && initStatus == "READY") {
            delay(400) // Brief pause after "READY" so user sees it
            onEnter()
        }
    }

    // ── Layout ───────────────────────────────────────────────────────────────

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Void),
    ) {
        // Original v1 start screen background -- shifted up to center logo
        Image(
            painter = painterResource(R.drawable.start_screen_bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-60).dp)
                .alpha(0.55f),
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

        // ── Words: single line at top ──────────────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "RAGE.",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                    fontSize = 24.sp,
                ),
                color = TextPrimary.copy(alpha = 0.65f),
                modifier = Modifier.alpha(word1Alpha.value),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "RIP.",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                    fontSize = 24.sp,
                ),
                color = TextPrimary.copy(alpha = 0.65f),
                modifier = Modifier.alpha(word2Alpha.value),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "REPEAT.",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                    fontSize = 24.sp,
                ),
                color = TextPrimary.copy(alpha = 0.65f),
                modifier = Modifier.alpha(word3Alpha.value),
            )
        }

        // ── Init sequence + CTA at bottom ────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Cyberpunk init
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(initAlpha.value),
                horizontalAlignment = Alignment.Start,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
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
                    Text(
                        text = if (initStatus.isNotEmpty()) "[ACTIVE]" else "",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                        ),
                        color = Blood.copy(alpha = 0.5f),
                    )
                }

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
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
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = initStatus,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp,
                    ),
                    color = if (initStatus == "READY") BloodBright else TextPrimary,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CTA - only show button for first-time users (no profile yet)
            if (!hasProfile) {
                TrackGodButton(
                    text = if (isReady && initStatus == "READY") "TAP TO ENTER THE ALTAR" else "INITIALIZING...",
                    onClick = { onEnterOnboarding() },
                    enabled = isReady && initStatus == "READY",
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(ctaAlpha.value)
                        .offset { IntOffset(0, ctaOffsetY.value.dp.roundToPx()) },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer
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
