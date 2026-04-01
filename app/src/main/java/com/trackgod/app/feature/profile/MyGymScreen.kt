package com.trackgod.app.feature.profile

import com.trackgod.app.ui.theme.screenPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trackgod.app.ui.component.BrandPicker
import com.trackgod.app.ui.component.MetalTextureBackground
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.TrackGodTheme

@Composable
fun MyGymScreen(
    viewModel: MyGymViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = TrackGodTheme.spacing

    MetalTextureBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = spacing.xs),
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.xs, vertical = spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate back",
                        tint = TextPrimary,
                    )
                }
                Text(
                    text = "MY GYM",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Subtitle
            Text(
                text = "Select the brands your gym has. Exercises are added or hidden instantly.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextTertiary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.screenPadding),
            )

            Spacer(modifier = Modifier.height(spacing.md))

            // Message bar
            state.message?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.labelMedium,
                    color = BloodBright,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceLow)
                        .padding(horizontal = spacing.screenPadding, vertical = spacing.sm),
                )
            }

            // Brand picker
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Blood)
                }
            } else
            BrandPicker(
                brands = state.brands,
                onToggleBrand = viewModel::toggleBrand,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = spacing.screenPadding),
            )

            Spacer(modifier = Modifier.height(spacing.md))
        }
    }
}
