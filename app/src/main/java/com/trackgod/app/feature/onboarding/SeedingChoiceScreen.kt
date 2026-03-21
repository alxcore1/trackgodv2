package com.trackgod.app.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trackgod.app.core.database.SeedDatabase
import com.trackgod.app.ui.component.ButtonVariant
import com.trackgod.app.ui.component.TrackGodButton
import com.trackgod.app.ui.component.TrackGodCard
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.SurfaceHighest
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextSecondary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.Void
import kotlinx.coroutines.launch

@Composable
fun SeedingChoiceScreen(
    seedDatabase: SeedDatabase,
    onComplete: () -> Unit,
    onNavigateToV1Import: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var isSeeding by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Void)
            .verticalScroll(rememberScrollState()),
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "TRACKGOD",
                style = MaterialTheme.typography.labelLarge,
                color = TextTertiary,
                letterSpacing = 4.sp,
            )
        }

        // ── Heading ─────────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Choose your starting loadout.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextTertiary,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Option 1: Full Arsenal ──────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            TrackGodCard(
                accentBorder = true,
                onClick = if (!isSeeding) {
                    {
                        isSeeding = true
                        scope.launch {
                            seedDatabase.seedIfNeeded()
                            onComplete()
                        }
                    }
                } else null,
            ) {
                Text(
                    text = "FULL ARSENAL",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "79 exercises across all categories. Everything ready.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                )
                Spacer(modifier = Modifier.height(12.dp))
                TrackGodButton(
                    text = if (isSeeding) "LOADING..." else "SELECT",
                    onClick = {
                        isSeeding = true
                        scope.launch {
                            seedDatabase.seedIfNeeded()
                            onComplete()
                        }
                    },
                    enabled = !isSeeding,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Option 2: Basics Only ───────────────────────────────────────
            TrackGodCard(
                onClick = if (!isSeeding) {
                    {
                        isSeeding = true
                        scope.launch {
                            seedDatabase.seedBasicsOnly()
                            onComplete()
                        }
                    }
                } else null,
            ) {
                Text(
                    text = "BASICS ONLY",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Common free weight exercises. No machines. Clean start.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                )
                Spacer(modifier = Modifier.height(12.dp))
                TrackGodButton(
                    text = "SELECT",
                    onClick = {
                        isSeeding = true
                        scope.launch {
                            seedDatabase.seedBasicsOnly()
                            onComplete()
                        }
                    },
                    enabled = !isSeeding,
                    variant = ButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Option 3: Empty Slate ───────────────────────────────────────
            TrackGodCard(
                onClick = if (!isSeeding) {
                    {
                        isSeeding = true
                        scope.launch {
                            seedDatabase.markAsSeeded()
                            onComplete()
                        }
                    }
                } else null,
            ) {
                Text(
                    text = "EMPTY SLATE",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Add everything yourself. Full control.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                )
                Spacer(modifier = Modifier.height(12.dp))
                TrackGodButton(
                    text = "SELECT",
                    onClick = {
                        isSeeding = true
                        scope.launch {
                            seedDatabase.markAsSeeded()
                            onComplete()
                        }
                    },
                    enabled = !isSeeding,
                    variant = ButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Divider ─────────────────────────────────────────────────────
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

            Spacer(modifier = Modifier.height(16.dp))

            // ── V1 Import ───────────────────────────────────────────────────
            TrackGodButton(
                text = "IMPORT FROM TRACKGOD V1",
                onClick = onNavigateToV1Import,
                variant = ButtonVariant.Secondary,
                enabled = !isSeeding,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
