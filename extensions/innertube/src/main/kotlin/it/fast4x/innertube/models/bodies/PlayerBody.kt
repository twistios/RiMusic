package it.fast4x.innertube.models.bodies

import it.fast4x.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class PlayerBody(
    val context: Context = Context.DefaultAndroid,
    val videoId: String,
    val playlistId: String? = null,
    val contentCheckOk: Boolean = true,
)
