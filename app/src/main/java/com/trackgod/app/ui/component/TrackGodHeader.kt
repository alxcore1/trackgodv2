package com.trackgod.app.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trackgod.app.R
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.Void

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
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
    ) {
        // Red scratched texture background
        Image(
            painter = painterResource(R.drawable.topbar_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.25f),
        )

        // Bottom gradient fade into Void
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Void),
                    ),
                ),
        )

        // Header content on top (padded so text doesn't touch edges)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "TRACKGOD",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
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
private fun TrackGodHeaderFullWidthPreview() {
    TrackGodHeader()
}
