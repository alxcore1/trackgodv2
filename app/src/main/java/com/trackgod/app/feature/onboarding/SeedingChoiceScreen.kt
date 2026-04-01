package com.trackgod.app.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import com.trackgod.app.ui.component.BrandPicker
import com.trackgod.app.ui.component.ButtonVariant
import com.trackgod.app.ui.component.MetalTextureBackground
import com.trackgod.app.ui.component.TrackGodButton
import com.trackgod.app.ui.component.TrackGodCard
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.SurfaceHighest
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextSecondary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.TrackGodTheme

@Composable
fun SeedingChoiceScreen(
    viewModel: SeedingChoiceViewModel,
    onComplete: () -> Unit,
    onNavigateToV1Import: () -> Unit,
) {
    val spacing = TrackGodTheme.spacing
    val isSeeding by viewModel.isSeeding.collectAsState()
    val showBrandPicker by viewModel.showBrandPicker.collectAsState()
    val availableBrands by viewModel.availableBrands.collectAsState()
    val selectedBrands by viewModel.selectedBrands.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    BackHandler(enabled = showBrandPicker) {
        viewModel.goBackToStep1()
    }

    MetalTextureBackground {
    AnimatedContent(
        targetState = showBrandPicker,
        transitionSpec = {
            (slideInHorizontally { it } + fadeIn())
                .togetherWith(slideOutHorizontally { -it } + fadeOut())
        },
        label = "seedingStep",
    ) { isBrandStep ->
        if (isBrandStep) {
            // ── Step 2: Brand Picker ────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                // ── Header with Back Button ────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.xs, vertical = spacing.lg),
                ) {
                    IconButton(
                        onClick = { viewModel.goBackToStep1() },
                        modifier = Modifier.align(Alignment.CenterStart),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextSecondary,
                            modifier = Modifier.size(spacing.xl),
                        )
                    }
                    Text(
                        text = "TRACKGOD",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextTertiary,
                        letterSpacing = 4.sp,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                // ── Heading ─────────────────────────────────────────────────
                Column(modifier = Modifier.padding(horizontal = spacing.xl)) {
                    Text(
                        text = buildAnnotatedString {
                            append("SELECT YOUR\n")
                            withStyle(SpanStyle(color = Blood)) {
                                append("GYM'S BRANDS")
                            }
                        },
                        style = MaterialTheme.typography.displaySmall,
                        color = TextPrimary,
                        lineHeight = 34.sp,
                    )

                    Spacer(modifier = Modifier.height(spacing.sm))

                    Text(
                        text = "You can change this later in Profile > My Gym.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary,
                    )
                }

                Spacer(modifier = Modifier.height(spacing.xl))

                // ── Brand Grid (fills remaining space, scrolls internally) ─
                BrandPicker(
                    brands = availableBrands,
                    onToggleBrand = viewModel::toggleBrand,
                    onSelectAll = if (selectedBrands.size < availableBrands.size) viewModel::selectAllBrands else null,
                    onDeselectAll = if (selectedBrands.size == availableBrands.size) viewModel::deselectAllBrands else null,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = spacing.xl),
                )

                // ── Error Message ──────────────────────────────────────────
                if (errorMessage != null) {
                    Text(
                        text = errorMessage.orEmpty(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                        ),
                        color = BloodBright,
                        modifier = Modifier
                            .padding(horizontal = spacing.xl, vertical = spacing.sm),
                    )
                }

                // ── Buttons (fixed at bottom) ──────────────────────────────
                Column(
                    modifier = Modifier.padding(
                        start = spacing.xl,
                        end = spacing.xl,
                        top = spacing.lg,
                        bottom = spacing.xxl,
                    ),
                ) {
                    TrackGodButton(
                        text = if (isSeeding) "LOADING..." else "CONTINUE",
                        onClick = { viewModel.seedWithSelectedBrands(onComplete) },
                        enabled = !isSeeding && selectedBrands.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(spacing.md))

                    TrackGodButton(
                        text = "SKIP",
                        onClick = { viewModel.skipBrandSelection(onComplete) },
                        enabled = !isSeeding,
                        variant = ButtonVariant.Ghost,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        } else {
            // ── Step 1: Original 3-option choice ────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                // ── Header ──────────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.xl, vertical = spacing.lg),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "TRACKGOD",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextTertiary,
                        letterSpacing = 4.sp,
                    )
                }

                // ── Heading ─────────────────────────────────────────────────
                Column(modifier = Modifier.padding(horizontal = spacing.xl)) {
                    Text(
                        text = buildAnnotatedString {
                            append("LOAD\n")
                            withStyle(SpanStyle(color = Blood)) {
                                append("ARSENAL")
                            }
                        },
                        style = MaterialTheme.typography.displaySmall,
                        color = TextPrimary,
                        lineHeight = 34.sp,
                    )

                    Spacer(modifier = Modifier.height(spacing.sm))

                    Text(
                        text = "Choose your starting loadout.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary,
                    )
                }

                Spacer(modifier = Modifier.height(spacing.xxl))

                // ── Option 1: Full Arsenal ──────────────────────────────────
                Column(modifier = Modifier.padding(horizontal = spacing.xl)) {
                    TrackGodCard(
                        accentBorder = true,
                    ) {
                        Text(
                            text = "FULL ARSENAL",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                        )
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Text(
                            text = "390+ exercises across all categories. Pick your gym's brands next.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextTertiary,
                        )
                        Spacer(modifier = Modifier.height(spacing.md))
                        TrackGodButton(
                            text = if (isSeeding) "LOADING..." else "SELECT",
                            onClick = { viewModel.showBrandSelection() },
                            enabled = !isSeeding,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.lg))

                    // ── Option 2: Basics Only ───────────────────────────────
                    TrackGodCard {
                        Text(
                            text = "BASICS ONLY",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                        )
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Text(
                            text = "Common free weight exercises. No machines. Clean start.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextTertiary,
                        )
                        Spacer(modifier = Modifier.height(spacing.md))
                        TrackGodButton(
                            text = "SELECT",
                            onClick = { viewModel.seedBasics(onComplete) },
                            enabled = !isSeeding,
                            variant = ButtonVariant.Secondary,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.lg))

                    // ── Option 3: Empty Slate ───────────────────────────────
                    TrackGodCard {
                        Text(
                            text = "EMPTY SLATE",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                        )
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Text(
                            text = "Add everything yourself. Full control.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextTertiary,
                        )
                        Spacer(modifier = Modifier.height(spacing.md))
                        TrackGodButton(
                            text = "SELECT",
                            onClick = { viewModel.seedEmpty(onComplete) },
                            enabled = !isSeeding,
                            variant = ButtonVariant.Secondary,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.xxl))

                    // ── Error Message ───────────────────────────────────────
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage.orEmpty(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                            ),
                            color = BloodBright,
                            modifier = Modifier.padding(bottom = spacing.lg),
                        )
                    }

                    // ── Divider ─────────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(SurfaceHighest),
                        )
                        Text(
                            text = "  OR  ",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextTertiary,
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(SurfaceHighest),
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.lg))

                    // ── V1 Import ───────────────────────────────────────────
                    TrackGodButton(
                        text = "IMPORT FROM TRACKGOD V1",
                        onClick = onNavigateToV1Import,
                        variant = ButtonVariant.Secondary,
                        enabled = !isSeeding,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(spacing.xxl))
                }
            }
        }
    }
    } // MetalTextureBackground
}
