package com.trackgod.app.feature.profile

import com.trackgod.app.ui.theme.screenPadding
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
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
import com.trackgod.app.ui.theme.TrackGodTheme

@Composable
fun ProfileScreen(
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToWeightLoss: () -> Unit = {},
    onNavigateToBackup: () -> Unit = {},
    onNavigateToMyGym: () -> Unit = {},
    onNavigateToPrivacyPolicy: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = TrackGodTheme.spacing

    MetalTextureBackground {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = spacing.xs)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.screenPadding),
    ) {
        // ── Header ────────────────────────────────────────────────────────
        TrackGodHeader()

        Spacer(modifier = Modifier.height(spacing.md))

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Blood)
            }
        } else if (state.profile == null && !state.isLoading) {
            // No profile -- show setup prompt
            NoProfileSection(onSetUp = onNavigateToEditProfile)
        } else if (state.profile != null) {
            // Profile header
            ProfileHeader(
                state = state,
            )

            Spacer(modifier = Modifier.height(spacing.xxl))

            // -- ACCOUNT section --
            SectionDivider(text = "ACCOUNT", modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(spacing.lg))
            ProfileMenuItem(label = "EDIT PROFILE", onClick = onNavigateToEditProfile)
            Spacer(modifier = Modifier.height(spacing.sm))
            ProfileMenuItem(label = "PRIVACY POLICY", onClick = onNavigateToPrivacyPolicy)

            Spacer(modifier = Modifier.height(spacing.xl))

            // -- GOALS section --
            SectionDivider(text = "GOALS", modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(spacing.lg))
            ProfileMenuItem(label = "WEIGHT LOSS JOURNEY", onClick = onNavigateToWeightLoss)

            Spacer(modifier = Modifier.height(spacing.xl))

            // -- DATA section --
            SectionDivider(text = "DATA", modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(spacing.lg))
            ProfileMenuItem(label = "BACKUP & RESTORE", onClick = onNavigateToBackup)
            Spacer(modifier = Modifier.height(spacing.sm))
            ProfileMenuItemWithSubtitle(
                label = "MY GYM",
                subtitle = "Manage gym equipment brands",
                onClick = onNavigateToMyGym,
            )

            Spacer(modifier = Modifier.height(spacing.xl))

            // -- APP section --
            SectionDivider(text = "APP", modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(spacing.lg))
            ProfileMenuItem(label = "SETTINGS", onClick = onNavigateToSettings)
        }

        Spacer(modifier = Modifier.height(spacing.xxl))
    }
    } // MetalTextureBackground
}

// -- Profile Header -----------------------------------------------------------

@Composable
private fun ProfileHeader(
    state: ProfileState,
) {
    val spacing = TrackGodTheme.spacing
    val profile = state.profile ?: return

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Avatar
        if (profile.avatarUri != null) {
            val context = LocalContext.current
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(profile.avatarUri)
                    .crossfade(true)
                    .error(android.R.drawable.ic_menu_gallery)
                    .build(),
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

        Spacer(modifier = Modifier.height(spacing.lg))

        // User name
        Text(
            text = profile.name.uppercase(),
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        // Primary objective
        if (!profile.primaryObjective.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(spacing.xs))
            Text(
                text = profile.primaryObjective.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = BloodBright,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(spacing.sm))

        // Member since
        if (state.memberSince.isNotBlank()) {
            Text(
                text = "MEMBER SINCE ${state.memberSince.uppercase()}",
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(spacing.xs))

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
    val spacing = TrackGodTheme.spacing
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

        Spacer(modifier = Modifier.height(spacing.sm))

        Text(
            text = "CREATE YOUR PROFILE TO TRACK YOUR PROGRESS",
            style = MaterialTheme.typography.labelMedium,
            color = TextTertiary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(spacing.xl))

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

@Composable
private fun ProfileMenuItemWithSubtitle(
    label: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    TrackGodCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
            )
        }
    }
}
