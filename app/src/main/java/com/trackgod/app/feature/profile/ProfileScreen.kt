package com.trackgod.app.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.trackgod.app.ui.component.ButtonVariant
import com.trackgod.app.ui.component.MetalTextureBackground
import com.trackgod.app.ui.component.SectionDivider
import com.trackgod.app.ui.component.TrackGodButton
import com.trackgod.app.ui.component.TrackGodCard
import com.trackgod.app.ui.component.TrackGodHeader
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary

@Composable
fun ProfileScreen(
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToWeightLoss: () -> Unit = {},
    onNavigateToBackup: () -> Unit = {},
    onNavigateToPrivacyPolicy: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MetalTextureBackground {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 4.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        // ── Header ────────────────────────────────────────────────────────
        TrackGodHeader()

        Spacer(modifier = Modifier.height(12.dp))

        if (state.profile == null && !state.isLoading) {
            // No profile -- show setup prompt
            NoProfileSection(onSetUp = onNavigateToEditProfile)
        } else if (state.profile != null) {
            // Profile header
            ProfileHeader(
                state = state,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // -- ACCOUNT section --
            SectionDivider(text = "ACCOUNT", modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            ProfileMenuItem(label = "EDIT PROFILE", onClick = onNavigateToEditProfile)
            Spacer(modifier = Modifier.height(8.dp))
            ProfileMenuItem(label = "PRIVACY POLICY", onClick = onNavigateToPrivacyPolicy)

            Spacer(modifier = Modifier.height(24.dp))

            // -- GOALS section --
            SectionDivider(text = "GOALS", modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            ProfileMenuItem(label = "WEIGHT LOSS JOURNEY", onClick = onNavigateToWeightLoss)

            Spacer(modifier = Modifier.height(24.dp))

            // -- DATA section --
            SectionDivider(text = "DATA", modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            ProfileMenuItem(label = "BACKUP & RESTORE", onClick = onNavigateToBackup)
            Spacer(modifier = Modifier.height(8.dp))
            ProfileMenuItem(label = "EXPORT DATABASE", onClick = onNavigateToBackup)

            Spacer(modifier = Modifier.height(24.dp))

            // -- APP section --
            SectionDivider(text = "APP", modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            ProfileMenuItem(label = "SETTINGS", onClick = onNavigateToSettings)
            Spacer(modifier = Modifier.height(8.dp))
            ProfileMenuItem(label = "ABOUT", onClick = { /* TODO */ })
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
    } // MetalTextureBackground
}

// -- Profile Header -----------------------------------------------------------

@Composable
private fun ProfileHeader(
    state: ProfileState,
) {
    val profile = state.profile ?: return

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Avatar
        if (profile.avatarUri != null) {
            AsyncImage(
                model = profile.avatarUri,
                contentDescription = "Profile avatar",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RectangleShape),
                contentScale = ContentScale.Crop,
            )
        } else {
            // Initials avatar
            val initials = profile.name
                .split(" ")
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .take(2)
                .joinToString("")
                .ifEmpty { "?" }

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RectangleShape)
                    .background(Blood),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User name
        Text(
            text = profile.name.uppercase(),
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )

        // Primary objective
        if (!profile.primaryObjective.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = profile.primaryObjective.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = BloodBright,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Member since
        if (state.memberSince.isNotBlank()) {
            Text(
                text = "MEMBER SINCE ${state.memberSince.uppercase()}",
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Workout count
        Text(
            text = "${state.totalWorkouts} WORKOUTS COMPLETED",
            style = MaterialTheme.typography.labelMedium,
            color = TextTertiary,
            textAlign = TextAlign.Center,
        )
    }
}

// -- No Profile Prompt --------------------------------------------------------

@Composable
private fun NoProfileSection(onSetUp: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "NO PROFILE SET UP",
            style = MaterialTheme.typography.headlineMedium,
            color = TextTertiary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "CREATE YOUR PROFILE TO TRACK YOUR PROGRESS",
            style = MaterialTheme.typography.labelMedium,
            color = TextTertiary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        TrackGodButton(
            text = "SET UP PROFILE",
            onClick = onSetUp,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// -- Menu Item ----------------------------------------------------------------

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
