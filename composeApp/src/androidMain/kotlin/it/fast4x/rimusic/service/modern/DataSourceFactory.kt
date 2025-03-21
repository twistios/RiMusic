package it.fast4x.rimusic.service.modern

import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.R
import it.fast4x.rimusic.utils.asSong
import it.fast4x.rimusic.utils.isConnectionMetered
import it.fast4x.rimusic.utils.okHttpDataSourceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import it.fast4x.rimusic.appContext
import it.fast4x.rimusic.extensions.players.SimplePlayer
import it.fast4x.rimusic.models.Format
import it.fast4x.rimusic.service.isLocal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


@OptIn(UnstableApi::class)
internal fun PlayerServiceModern.createSimpleDataSourceFactory(scope: CoroutineScope): DataSource.Factory {
    val songUrlCache = HashMap<String, Pair<String, Long>>()
    return ResolvingDataSource.Factory(createCacheDataSource()) { dataSpec ->
        val mediaId = dataSpec.key ?: error("No media id")
        val length = if (dataSpec.length >= 0) dataSpec.length else 1

        // Get song from player
        val mediaItem = runBlocking {
            withContext(Dispatchers.Main) {
                player.currentMediaItem
            }
        }
        // Ensure that the song is in database
        Database.asyncTransaction {
            if (mediaItem != null) {
                insert(mediaItem.asSong)
            }
        }

        val isCached = try {
            cache.isCached(mediaId, dataSpec.position, PlayerServiceModern.ChunkLength)
        } catch (e: Exception) {
            false
        }
        val isDownloaded = try {
            downloadCache.isCached(mediaId, dataSpec.position, length)
        } catch (e: Exception) {
            false
        }


        if( dataSpec.isLocal || isCached || isDownloaded ) {
             //scope.launch(Dispatchers.IO) { recoverSong(mediaId) }
            return@Factory dataSpec
        }

        songUrlCache[mediaId]?.takeIf { it.second > System.currentTimeMillis() }?.let {
            //scope.launch(Dispatchers.IO) { recoverSong(mediaId) }
            return@Factory dataSpec.withUri(it.first.toUri())
        }

        // Check whether format exists so that users from older version can view format details
        // There may be inconsistent between the downloaded file and the displayed info if user change audio quality frequently
        val playedFormat = runBlocking(Dispatchers.IO) { Database.format(mediaId).first() }
        val playbackData = runBlocking(Dispatchers.IO) {
            SimplePlayer.playerResponseForPlayback(
                mediaId,
                playedFormat = playedFormat,
                audioQuality = audioQualityFormat,
                //connectivityManager = connectivityManager,
            )
        }.getOrElse { throwable ->
            when (throwable) {
                is PlaybackException -> throw throwable
                is ConnectException, is UnknownHostException -> {
                    throw PlaybackException(
                        getString(R.string.error_no_internet),
                        throwable,
                        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                    )
                }

                is SocketTimeoutException -> {
                    throw PlaybackException(
                        getString(R.string.error_timeout),
                        throwable,
                        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
                    )
                }

                else -> throw PlaybackException(
                    getString(R.string.error_unknown),
                    throwable,
                    PlaybackException.ERROR_CODE_REMOTE_ERROR,
                )
            }
        }

        val format = playbackData.format

        Database.asyncTransaction {
            if (songExist(mediaId) > 0)
                upsert(
                    Format(
                        songId = mediaId,
                        itag = format.itag,
                        mimeType = format.mimeType.split(";")[0],
                        //codecs = format.mimeType.split("codecs=")[1].removeSurrounding("\""),
                        bitrate = format.bitrate.toLong(),
                        //sampleRate = format.audioSampleRate,
                        contentLength = format.contentLength!!,
                        loudnessDb = playbackData.audioConfig?.loudnessDb,
                        //playbackUrl = playbackData.playbackTracking?.videostatsPlaybackUrl?.baseUrl
                    )
                )
        }
        //scope.launch(Dispatchers.IO) { recoverSong(mediaId, playbackData) }

        val streamUrl = playbackData.streamUrl

        songUrlCache[mediaId] = streamUrl to System.currentTimeMillis() + (playbackData.streamExpiresInSeconds * 1000L)
        dataSpec.withUri(streamUrl.toUri()).subrange(dataSpec.uriPositionOffset, PlayerServiceModern.ChunkLength)
    }
}

@OptIn(UnstableApi::class)
internal fun PlayerServiceModern.createDataSourceFactory(): DataSource.Factory {
    return ResolvingDataSource.Factory(
        CacheDataSource.Factory()
            .setCache(downloadCache)
            .setUpstreamDataSourceFactory(
                CacheDataSource.Factory()
                    .setCache(cache)
                    .setUpstreamDataSourceFactory(
                        appContext().okHttpDataSourceFactory
                    )
            )
            .setCacheWriteDataSinkFactory(null)
            .setFlags(FLAG_IGNORE_CACHE_ON_ERROR)
//        ConditionalCacheDataSourceFactory(
//            cacheDataSourceFactory = cache.asDataSource, //.readOnlyWhen { PlayerPreferences.pauseCache }.asDataSource,
//            upstreamDataSourceFactory = appContext().defaultDataSourceFactory,
//            shouldCache = { !it.isLocal }
//        )

    ) { dataSpec: DataSpec ->
        //try {

            // Get song from player
             val mediaItem = runBlocking {
                 withContext(Dispatchers.Main) {
                     player.currentMediaItem
                 }
            }
            // Ensure that the song is in database
            Database.asyncTransaction {
                if (mediaItem != null) {
                    insert(mediaItem.asSong)
                }
            }


            //println("PlayerService DataSourcefactory currentMediaItem: ${mediaItem?.mediaId}")
            //dataSpec.key?.let { player.findNextMediaItemById(it)?.mediaMetadata }

            return@Factory runBlocking {
                try {
                    dataSpecProcess(dataSpec, applicationContext, applicationContext.isConnectionMetered())
                } catch (e: Exception) {
                    Timber.e("PlayerServiceModern DataSourcefactory return@Factory Error: ${e.stackTraceToString()}")
                    println("PlayerServiceModern DataSourcefactory return@Factory Error: ${e.stackTraceToString()}")
                    dataSpec
                }
            }


//        } catch (e: Throwable) {
//            Timber.e("PlayerServiceModern DataSourcefactory Error: ${e.stackTraceToString()}")
//            println("PlayerServiceModern DataSourcefactory Error: ${e.stackTraceToString()}")
//            dataSpec
//        }
    }
//        .retryIf<UnplayableException>(
//        maxRetries = 3,
//        printStackTrace = true
//    )
//    .retryIf(
//        maxRetries = 1,
//        printStackTrace = true
//    ) { ex ->
//        ex.findCause<InvalidResponseCodeException>()?.responseCode == 403 ||
//                ex.findCause<ClientRequestException>()?.response?.status?.value == 403 ||
//                ex.findCause<InvalidHttpCodeException>() != null
//    }.handleRangeErrors()
}


