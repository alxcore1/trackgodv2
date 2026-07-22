package com.trackgod.app.feature.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trackgod.app.core.util.restartAppProcess
import com.trackgod.app.ui.component.ButtonVariant
import com.trackgod.app.ui.component.MetalTextureBackground
import com.trackgod.app.ui.component.SectionDivider
import com.trackgod.app.ui.component.TrackGodButton
import com.trackgod.app.ui.component.TrackGodCard
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextSecondary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.TrackGodTheme
import com.trackgod.app.ui.theme.Void

@Composable
fun OnboardingRestoreScreen(
    onNavigateBack: () -> Unit,
    viewModel: OnboardingRestoreViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let(viewModel::importBackup)
    }

    OnboardingRestoreScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onPickFile = { picker.launch(arrayOf("*/*")) },
        onRestart = {
            restartAppProcess(context)
        },
    )
}

@Composable
fun OnboardingRestoreScreenContent(
    uiState: OnboardingRestoreUiState,
    onNavigateBack: () -> Unit,
    onPickFile: () -> Unit,
    onRestart: () -> Unit,
) {
    val spacing = TrackGodTheme.spacing

    if (uiState.showRestartRequired) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    text = "RESTART REQUIRED",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
            },
            text = {
                Text(
                    text = "DATABASE RESTORED SUCCESSFULLY. PLEASE RESTART TRACKGOD FOR CHANGES TO TAKE EFFECT.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = onRestart) {
                    Text("RESTART", color = Blood)
                }
            },
            containerColor = Void,
        )
    }

    MetalTextureBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.xs, vertical = spacing.lg),
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.align(Alignment.CenterStart),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextSecondary,
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

            if (uiState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Blood,
                    trackColor = SurfaceLow,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = spacing.xl),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Spacer(modifier = Modifier.height(spacing.lg))

                    Text(
                        text = buildAnnotatedString {
                            append("RESTORE\n")
                            withStyle(SpanStyle(color = BloodBright)) {
                                append("FROM BACKUP")
                            }
                        },
                        style = MaterialTheme.typography.displaySmall,
                        color = TextPrimary,
                        lineHeight = 34.sp,
                    )

                    Spacer(modifier = Modifier.height(spacing.sm))

                    Text(
                        text = "FULL DATABASE RESTORE :: .DB FILE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                        ),
                        color = TextTertiary,
                    )

                    Spacer(modifier = Modifier.height(spacing.xxl))

                    TrackGodCard(accentBorder = true) {
                        SectionDivider(
                            text = "TRACKGOD V2 BACKUP",
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(modifier = Modifier.height(spacing.lg))

                        Text(
                            text = "Select a TrackGod v2 full database backup. This replaces the current database and requires a restart before use.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )

                        Spacer(modifier = Modifier.height(spacing.xl))

                        TrackGodButton(
                            text = if (uiState.isLoading) "RESTORING..." else "SELECT BACKUP FILE",
                            onClick = onPickFile,
                            enabled = !uiState.isLoading,
                            icon = Icons.Default.Restore,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    uiState.message?.let { message ->
                        Spacer(modifier = Modifier.height(spacing.lg))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                            ),
                            color = BloodBright,
                        )
                    }
                }

                Column {
                    Spacer(modifier = Modifier.height(spacing.xxl))
                    TrackGodButton(
                        text = "BACK",
                        onClick = onNavigateBack,
                        enabled = !uiState.isLoading,
                        variant = ButtonVariant.Ghost,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(spacing.xxl))
                }
            }
        }
    }
}
