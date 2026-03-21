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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.trackgod.app.R
import com.trackgod.app.ui.component.TrackGodButton
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextSecondary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.TrackGodTheme
import com.trackgod.app.ui.theme.Void
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Branded splash / entry screen.
 *
 * Shows the TG logo with "RAGE. RIP. REPEAT." tagline, a loading status
 * block, and a CTA button to enter the main app. All elements animate in
 * sequentially for a premium first-impression.
 *
 * @param onEnter Called when the user taps the CTA button.
 */
@Composable
fun SplashScreen(
    onEnter: () -> Unit = {},
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val isReady by viewModel.isReady.collectAsState()

    // ── Animation states ─────────────────────────────────────────────────────

    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.9f) }
    val taglineAlpha = remember { Animatable(0f) }
    val loadingAlpha = remember { Animatable(0f) }
    val ctaAlpha = remember { Animatable(0f) }
    val ctaOffsetY = remember { Animatable(100f) }
    val footerAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Logo: fade in + scale up
        launch {
            logoAlpha.animateTo(1f, animationSpec = tween(600))
        }
        launch {
            logoScale.animateTo(1f, animationSpec = tween(600))
        }

        // Tagline: fade in after 300ms
        launch {
            delay(300)
            taglineAlpha.animateTo(1f, animationSpec = tween(400))
        }

        // Loading block: fade in after 500ms
        launch {
            delay(500)
            loadingAlpha.animateTo(1f, animationSpec = tween(400))
        }

        // CTA button: slide up + fade after 700ms
        launch {
            delay(700)
            launch { ctaAlpha.animateTo(1f, animationSpec = tween(500)) }
            launch { ctaOffsetY.animateTo(0f, animationSpec = tween(500)) }
        }

        // Footer: fade in with CTA
        launch {
            delay(800)
            footerAlpha.animateTo(1f, animationSpec = tween(400))
        }
    }

    // ── Layout ───────────────────────────────────────────────────────────────

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Void),
    ) {
        // Subtle texture overlay
        Image(
            painter = painterResource(R.drawable.screen_bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.05f),
            contentScale = ContentScale.Crop,
        )

        // Vignette overlay: radial gradient from transparent center to dark edges
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Void.copy(alpha = 0.7f),
                        ),
                        radius = 900f,
                    ),
                ),
        )

        // Main content column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(0.15f))

            // ── Tagline ──────────────────────────────────────────────────
            Text(
                text = "RAGE.  RIP.  REPEAT.",
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 6.sp,
                ),
                color = TextSecondary,
                modifier = Modifier.alpha(taglineAlpha.value),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── TG Logo ──────────────────────────────────────────────────
            Image(
                painter = painterResource(R.drawable.trackgod_logo),
                contentDescription = "TrackGod Logo",
                modifier = Modifier
                    .width(220.dp)
                    .graphicsLayer {
                        alpha = logoAlpha.value
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                    },
                contentScale = ContentScale.FillWidth,
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ── Loading status block ─────────────────────────────────────
            Row(
                modifier = Modifier.alpha(loadingAlpha.value),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Red accent bar
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(40.dp)
                        .background(Blood),
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "SYSTEM_INIT",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isReady) "READY" else "LOADING",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── CTA button ───────────────────────────────────────────────
            TrackGodButton(
                text = if (isReady) "TAP TO ENTER THE ALTAR" else "INITIALIZING...",
                onClick = onEnter,
                enabled = isReady,
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

                // Red dot separator
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(Blood, shape = CircleShape),
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

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun SplashScreenPreview() {
    TrackGodTheme {
        SplashScreen()
    }
}
