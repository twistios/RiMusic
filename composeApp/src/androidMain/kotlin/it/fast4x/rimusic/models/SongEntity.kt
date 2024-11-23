package it.fast4x.rimusic.models

import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import kotlinx.serialization.Serializable


@Immutable
data class SongEntity(
    @Embedded val song: Song,
    val contentLength: Long? = null,
    val albumTitle: String? = null,
)
