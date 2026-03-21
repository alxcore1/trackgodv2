package com.trackgod.app.feature.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trackgod.app.ui.component.MetalTextureBackground
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextSecondary

@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit,
) {
    MetalTextureBackground {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                )
            }
            Text(
                text = "PRIVACY POLICY",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            PolicyParagraph(
                "TrackGod v2 stores all data locally on your device. No data is sent to any server. " +
                "No account is required. No analytics or tracking is performed."
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "DATA STORED:",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )

            Spacer(modifier = Modifier.height(12.dp))

            val dataItems = listOf(
                "Workout history",
                "Exercise database",
                "Body measurements",
                "Progress photos (on device)",
                "User settings",
            )
            dataItems.forEach { item ->
                PolicyBullet(item)
                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            PolicyParagraph(
                "All data can be exported and deleted at any time from the Backup & Restore section."
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    } // MetalTextureBackground
}

@Composable
private fun PolicyParagraph(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = TextSecondary,
    )
}

@Composable
private fun PolicyBullet(text: String) {
    Row {
        Text(
            text = "  -  ",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
        )
    }
}
