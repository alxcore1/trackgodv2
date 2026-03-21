package com.trackgod.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary

/**
 * Standardised TRACKGOD wordmark header used on all main tab screens.
 *
 * Layout: 56dp tall row with the wordmark left-aligned and a small
 * user-avatar placeholder square (initials) on the right.
 *
 * @param initials One or two characters shown inside the avatar square.
 *                 Pass null to hide the avatar.
 * @param modifier Modifier for the root row.
 */
@Composable
fun TrackGodHeader(
    modifier: Modifier = Modifier,
    initials: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "TRACKGOD",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp,
        )

        Spacer(modifier = Modifier.weight(1f))

        // User avatar placeholder
        val avatarText = initials ?: "TG"
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color = Blood, shape = RectangleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = avatarText,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
            )
        }
    }
}

// -- Previews -----------------------------------------------------------------

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun TrackGodHeaderPreview() {
    TrackGodHeader()
}

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun TrackGodHeaderWithInitialsPreview() {
    TrackGodHeader(initials = "JD")
}
