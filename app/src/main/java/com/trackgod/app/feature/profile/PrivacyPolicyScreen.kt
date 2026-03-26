package com.trackgod.app.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trackgod.app.ui.component.MetalTextureBackground
import com.trackgod.app.ui.component.TrackGodCard
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextSecondary
import com.trackgod.app.ui.theme.TextTertiary

private data class PolicySection(
    val icon: ImageVector,
    val title: String,
    val bullets: List<String>,
)

private val sections = listOf(
    PolicySection(
        icon = Icons.Default.WifiOff,
        title = "OFFLINE FIRST",
        bullets = listOf(
            "All data is stored locally on your device.",
            "No servers, no cloud, no account required.",
            "The app works fully offline -- no internet connection needed.",
        ),
    ),
    PolicySection(
        icon = Icons.Default.DocumentScanner,
        title = "OCR SCANNING",
        bullets = listOf(
            "The camera is used only for scanning gym machine labels.",
            "Images are processed on-device using ML Kit.",
            "No images are uploaded or stored after scanning.",
        ),
    ),
    PolicySection(
        icon = Icons.Default.LocationOff,
        title = "GPS / LOCATION",
        bullets = listOf(
            "TrackGod does not collect GPS or location data.",
            "No geofencing, no location history, no tracking.",
        ),
    ),
    PolicySection(
        icon = Icons.Default.CameraAlt,
        title = "PROGRESS PHOTOS",
        bullets = listOf(
            "Photos are stored only on your device in app-internal storage.",
            "Photos are never uploaded, shared, or accessed by third parties.",
            "You can delete any photo at any time.",
        ),
    ),
    PolicySection(
        icon = Icons.Default.Notifications,
        title = "NOTIFICATIONS",
        bullets = listOf(
            "Notifications are used only for the rest timer alarm.",
            "No push notification servers are involved.",
            "Notifications are entirely local and can be disabled.",
        ),
    ),
    PolicySection(
        icon = Icons.Default.Analytics,
        title = "NO ANALYTICS",
        bullets = listOf(
            "No analytics SDKs, no crash reporting, no usage tracking.",
            "No Firebase, no Mixpanel, no third-party code that collects data.",
            "What happens on your device stays on your device.",
        ),
    ),
    PolicySection(
        icon = Icons.Default.Storage,
        title = "DATA CONTROL",
        bullets = listOf(
            "Export your entire workout history at any time.",
            "Delete all data from the Backup & Restore screen.",
            "Your data belongs to you -- no vendor lock-in.",
        ),
    ),
    PolicySection(
        icon = Icons.Default.Backup,
        title = "BACKUP & RESTORE",
        bullets = listOf(
            "Backups are local database files stored where you choose.",
            "No cloud sync, no automatic uploads.",
            "Daily auto-backups are stored on-device only.",
        ),
    ),
    PolicySection(
        icon = Icons.Default.Security,
        title = "PERMISSIONS",
        bullets = listOf(
            "Camera -- used only for OCR machine scanning.",
            "Notifications -- used only for rest timer alerts.",
            "Storage -- used only for photo storage and database backups.",
            "No permission is used for tracking or data collection.",
        ),
    ),
    PolicySection(
        icon = Icons.Default.Email,
        title = "CONTACT",
        bullets = listOf(
            "https://github.com/alxcore1",
        ),
    ),
)

@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit,
) {
    MetalTextureBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp),
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
                // Last updated
                Text(
                    text = "LAST UPDATED: MARCH 2026",
                    color = BloodBright,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Intro
                Text(
                    text = "TrackGod respects your privacy. All data stays on your device. No accounts, no servers, no tracking.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Section cards
                sections.forEach { section ->
                    PolicySectionCard(section)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Footer
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "YOUR DATA. YOUR DEVICE. YOUR CONTROL.",
                    color = TextTertiary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun PolicySectionCard(section: PolicySection) {
    TrackGodCard {
        // Icon + title row
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = section.icon,
                contentDescription = null,
                tint = Blood,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = section.title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bullets
        section.bullets.forEach { bullet ->
            PolicyBulletItem(bullet)
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}

@Composable
private fun PolicyBulletItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 7.dp)
                .size(5.dp)
                .background(Blood, RectangleShape),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
    }
}
