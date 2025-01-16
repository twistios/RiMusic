package it.fast4x.innertube.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SectionListRenderer(
    val contents: List<Content>?,
    val continuations: List<Continuation>?
) {
    @Serializable
    data class Content(
        @JsonNames("musicImmersiveCarouselShelfRenderer")
        val musicCarouselShelfRenderer: MusicCarouselShelfRenderer?,
        @JsonNames("musicPlaylistShelfRenderer")
        val musicShelfRenderer: MusicShelfRenderer?,
        val gridRenderer: GridRenderer?,
        val musicDescriptionShelfRenderer: MusicDescriptionShelfRenderer?,
        val musicResponsiveHeaderRenderer: MusicResponsiveHeaderRenderer?,
        val musicPlaylistShelfRenderer: MusicPlaylistShelfRenderer?,

    ) {

        @Serializable
        data class MusicDescriptionShelfRenderer(
            val description: Runs?,
        )

        @Serializable
        data class MusicResponsiveHeaderRenderer(
            val title: Runs?,
            //val description: MusicDescriptionShelfRenderer?,
            val description: Description?,
            val subtitle: Runs?,
            val secondSubtitle: Runs?,
            val thumbnail: ThumbnailRenderer?,
            val straplineTextOne: Runs?,
        ) {
            @Serializable
            data class Description(
                val musicDescriptionShelfRenderer: MusicDescriptionShelfRenderer?,
            )
        }
    }

}
