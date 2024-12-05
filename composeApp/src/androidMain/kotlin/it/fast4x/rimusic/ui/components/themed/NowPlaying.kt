package it.fast4x.rimusic.ui.components.themed

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import it.fast4x.rimusic.ui.components.MusicAnimation
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.onOverlay
import me.knighthat.colorPalette
import me.knighthat.thumbnailShape

@OptIn(UnstableApi::class)
@Composable
fun NowPlayingSongIndicator (
    mediaId: String,
    player: ExoPlayer?
) {

    if (player?.currentMediaItem?.mediaId != mediaId) return

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(
                color = Color.Black.copy(alpha = 0.25f),
                shape = thumbnailShape()
            )
            .size(Dimensions.thumbnails.song)
    ) {
            MusicAnimation(
                color = colorPalette().onOverlay,
                modifier = Modifier
                    .height(20.dp)
            )
    }

}