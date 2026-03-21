package com.trackgod.app.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trackgod.app.ui.component.TrackGodButton
import com.trackgod.app.ui.component.ButtonVariant
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.Void

@Composable
fun V1ImportScreen(
    onNavigateBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Void)
            .padding(horizontal = 24.dp),
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // ── Header ──────────────────────────────────────────────────────────
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "TRACKGOD",
                style = MaterialTheme.typography.labelLarge,
                color = TextTertiary,
                letterSpacing = 4.sp,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Heading ─────────────────────────────────────────────────────────
        Text(
            text = buildAnnotatedString {
                append("IMPORT FROM\n")
                withStyle(SpanStyle(color = Blood)) {
                    append("V1")
                }
            },
            style = MaterialTheme.typography.displaySmall,
            color = TextPrimary,
            lineHeight = 34.sp,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "This feature will be available soon.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Export your data from TrackGod v1, then import it here.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextTertiary,
        )

        Spacer(modifier = Modifier.weight(1f))

        // ── Back button ─────────────────────────────────────────────────────
        TrackGodButton(
            text = "BACK",
            onClick = onNavigateBack,
            variant = ButtonVariant.Secondary,
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
