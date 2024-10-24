package it.fast4x.rimusic.service

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import it.fast4x.innertube.Innertube
import it.fast4x.innertube.models.PlayerResponse
import it.fast4x.innertube.models.bodies.PlayerBody
import it.fast4x.innertube.requests.player
import it.fast4x.rimusic.enums.AudioQualityFormat
import it.fast4x.rimusic.utils.getPipedSession

@OptIn(UnstableApi::class)
internal suspend fun PlayerService.dataSpecProcess(dataSpec: DataSpec, context: Context, metered: Boolean): DataSpec {
    val songUri = dataSpec.uri.toString()
    val videoId = songUri.substringAfter("watch?v=")
    val chunkLength = 512 * 1024L

    if( dataSpec.isLocal ||
        cache.isCached(videoId, dataSpec.position, chunkLength) ||
        downloadCache.isCached(videoId, dataSpec.position, if (dataSpec.length >= 0) dataSpec.length else 1)
    ) {
        println("PlayerService DataSpecProcess Playing song ${videoId} from cached or local file")
        return dataSpec.withUri(Uri.parse(dataSpec.uri.toString()))
    }

    val format = getMediaFormat(videoId, audioQualityFormat)

    println("PlayerService DataSpecProcess Playing song ${videoId} from format $format from url=${format?.url}")
    return dataSpec.withUri(Uri.parse(format?.url))

}

@OptIn(UnstableApi::class)
internal suspend fun MyDownloadHelper.dataSpecProcess(dataSpec: DataSpec, context: Context, metered: Boolean): DataSpec {
    val songUri = dataSpec.uri.toString()
    val videoId = songUri.substringAfter("watch?v=")

    if( dataSpec.isLocal ||
        downloadCache.isCached(videoId, dataSpec.position, if (dataSpec.length >= 0) dataSpec.length else 1)
    ) {
        println("MyDownloadHelper DataSpecProcess Playing song ${videoId} from cached or local file")
        return dataSpec.withUri(Uri.parse(dataSpec.uri.toString()))
    }

    val format = getMediaFormat(videoId, audioQualityFormat)

    println("MyDownloadHelper DataSpecProcess Playing song $videoId from format $format from url=${format?.url}")
    return dataSpec.withUri(Uri.parse(format?.url))

}

suspend fun getMediaFormat(
    videoId: String,
    audioQualityFormat: AudioQualityFormat,
): PlayerResponse.StreamingData.AdaptiveFormat? {
    println("PlayerService MyDownloadHelper DataSpecProcess getMediaFormat Playing song $videoId from format $audioQualityFormat")
    return Innertube.player(
        body = PlayerBody(videoId = videoId),
        pipedSession = getPipedSession().toApiSession()
    )?.fold(
        { playerResponse ->
            when (audioQualityFormat) {
                AudioQualityFormat.Auto -> playerResponse.streamingData?.autoMaxQualityFormat
                AudioQualityFormat.High -> playerResponse.streamingData?.highestQualityFormat
                AudioQualityFormat.Medium -> playerResponse.streamingData?.mediumQualityFormat
                AudioQualityFormat.Low -> playerResponse.streamingData?.lowestQualityFormat
            }
        },
        {
            //return dataSpec.withUri(Uri.parse(dataSpec.uri.toString()))

            println("PlayerService MyDownloadHelper DataSpecProcess Error: ${it.message}")
            throw it
        }
    )
}