package it.fast4x.innertube.models

import it.fast4x.invidious.models.AdaptiveFormat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.knighthat.common.response.AudioFormat
import me.knighthat.common.response.MediaFormatContainer
import java.util.SortedSet

@Serializable
data class PlayerResponse(
    val playabilityStatus: PlayabilityStatus?,
    val playerConfig: PlayerConfig?,
    val streamingData: StreamingData?,
    val videoDetails: VideoDetails?,
) {
    @Serializable
    data class PlayabilityStatus(
        val status: String?,
        val reason: String?
    )

    @Serializable
    data class PlayerConfig(
        val audioConfig: AudioConfig?
    ) {
        @Serializable
        data class AudioConfig(
            val loudnessDb: Float?
        ) {
            // For music clients only
            val normalizedLoudnessDb: Float?
                get() = loudnessDb?.plus(7)
        }
    }

    @Serializable
    data class StreamingData(
        val adaptiveFormats: List<AdaptiveFormat>?,
        val formats: List<Format>?,
        val expiresInSeconds: Int,
    ) {

        val autoMaxQualityFormat: AdaptiveFormat?
            get() = adaptiveFormats?.sortedBy { it.bitrate }?.findLast {
                it.itag == 251 || it.itag == 141 ||
                        it.itag == 250 || it.itag == 140 ||
                        it.itag == 249 || it.itag == 139 || it.itag == 171
            }

        val highestQualityFormat: AdaptiveFormat?
            get() = adaptiveFormats?.sortedBy { it.bitrate }?.findLast { it.itag == 251 || it.itag == 141 }

        val mediumQualityFormat: AdaptiveFormat?
            get() = adaptiveFormats?.sortedBy { it.bitrate }?.findLast { it.itag == 250 || it.itag == 140 }

        val lowestQualityFormat: AdaptiveFormat?
            get() = adaptiveFormats?.sortedBy { it.bitrate }?.findLast { it.itag == 249 || it.itag == 139 }



        @Serializable
        data class AdaptiveFormat(
            val itag: Int,
            val mimeType: String,
            val bitrate: Long?,
            val averageBitrate: Long?,
            val contentLength: Long?,
            val audioQuality: String?,
            val approxDurationMs: Long?,
            val lastModified: Long?,
            val loudnessDb: Double?,
            val audioSampleRate: Int?,
            val url: String?,
            val width: Int?
        ) {
            val isAudio: Boolean
                get() = width == null

            val isVideo: Boolean
                get() = width != null
        }

        @Serializable
        data class Format(
            val itag: Int,
            val url: String?,
            val mimeType: String,
            val bitrate: Int,
            val width: Int?,
            val height: Int?,
            val contentLength: Long?,
            val quality: String,
            val fps: Int?,
            val qualityLabel: String?,
            val averageBitrate: Int?,
            val audioQuality: String?,
            val approxDurationMs: String?,
            val audioSampleRate: Int?,
            val audioChannels: Int?,
            val loudnessDb: Double?,
            val lastModified: Long?,
        ) {
            val isAudio: Boolean
                get() = width == null
        }
    }

//    @Serializable
//    data class StreamingData(
//        val expiresInSeconds: Long?,
//        val adaptiveFormats: List<AdaptiveFormat>,
//    ): MediaFormatContainer<StreamingData.AdaptiveFormat> {
//
//        override val formats: SortedSet<AdaptiveFormat> =
//            sortedSetOf<AdaptiveFormat>().apply {
//                // Should filter format starts with "audio" as in "audio/webm"
//                addAll( adaptiveFormats.filter { it.mimeType.startsWith("audio") } )
//            }
//
//        @Serializable
//        data class AdaptiveFormat(
//            val averageBitrate: Long?,
//            val contentLength: Long?,
//            val approxDurationMs: Long?,
//            val lastModified: Long?,
//            val loudnessDb: Double?,
//            val width: Int?,
//            val fps: Int?,
//            val quality: String?,
//            val qualityLabel: String?,
//            val audioQuality: String?,
//            val audioSampleRate: Int?,
//            val audioChannels: Int?,
//            @SerialName("mimeType")
//            val mimeTypeCodec: String,
//            override val itag: UShort,
//            override val url: String?,
//            override val bitrate: UInt
//        ): AudioFormat {
//
//            override val mimeType: String
//                get() = mimeTypeCodec.split( ";" )[0].trim()
//            override val codec: String
//                get() = mimeTypeCodec.split( ";" )[1].trim()
//
//            val isAudio: Boolean
//                get() = width == null
//
//            val isVideo: Boolean
//                get() = width != null
//        }
//    }

    @Serializable
    data class VideoDetails(
        val videoId: String?,
        val title: String?,
        val author: String?,
        val channelId: String?,
        val authorAvatar: String?,
        val authorSubCount: String?,
        val lengthSeconds: String?,
        val musicVideoType: String?,
        val viewCount: String?,
        val thumbnail: Thumbnails?,
        val description: String?,
    )
}
