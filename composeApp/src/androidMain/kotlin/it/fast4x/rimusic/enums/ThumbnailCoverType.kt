package it.fast4x.rimusic.enums

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import it.fast4x.rimusic.R

enum class ThumbnailCoverType {
    Vinyl,
    CD;

    val textName: String
        @Composable
        get() = when (this) {
            Vinyl -> stringResource( R.string.thumbnail_cover_vinyl )
            CD -> stringResource( R.string.thumbnail_cover_cd )
        }
}