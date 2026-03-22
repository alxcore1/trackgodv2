package com.trackgod.app.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.trackgod.app.R
import com.trackgod.app.ui.theme.Void

/**
 * Wraps content with the TrackGod distressed metal plate background texture.
 *
 * Every screen in the app should use this as its root container to maintain
 * the Industrial Brutalism aesthetic. The texture is rendered at 5-8% opacity
 * over the #131313 void background.
 */
@Composable
fun MetalTextureBackground(
    modifier: Modifier = Modifier,
    textureAlpha: Float = 0.12f,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Void),
    ) {
        // Distressed metal plate texture overlay
        Image(
            painter = painterResource(R.drawable.screen_bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(textureAlpha),
            contentScale = ContentScale.Crop,
        )

        // Screen content on top
        content()
    }
}
