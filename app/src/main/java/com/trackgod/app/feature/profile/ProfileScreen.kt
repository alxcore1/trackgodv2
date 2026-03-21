package com.trackgod.app.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trackgod.app.ui.component.SectionDivider
import com.trackgod.app.ui.component.TrackGodCard
import com.trackgod.app.ui.theme.TrackGodTheme

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Screen title
        Text(
            text = "PROFILE",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionDivider(
            text = "ACCOUNT",
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Menu items as cards
        ProfileMenuItem(label = "EDIT PROFILE", onClick = { /* TODO */ })
        Spacer(modifier = Modifier.height(8.dp))
        ProfileMenuItem(label = "SETTINGS", onClick = { /* TODO */ })
        Spacer(modifier = Modifier.height(8.dp))
        ProfileMenuItem(label = "WEIGHT TRACKING", onClick = { /* TODO */ })
        Spacer(modifier = Modifier.height(8.dp))
        ProfileMenuItem(label = "PHOTO COMPARISON", onClick = { /* TODO */ })

        Spacer(modifier = Modifier.height(24.dp))

        SectionDivider(
            text = "DATA",
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        ProfileMenuItem(label = "BACKUP & RESTORE", onClick = { /* TODO */ })
        Spacer(modifier = Modifier.height(8.dp))
        ProfileMenuItem(label = "PRIVACY POLICY", onClick = { /* TODO */ })
    }
}

@Composable
private fun ProfileMenuItem(
    label: String,
    onClick: () -> Unit,
) {
    TrackGodCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

// -- Preview ------------------------------------------------------------------

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun ProfileScreenPreview() {
    TrackGodTheme {
        ProfileScreen()
    }
}
