package it.fast4x.innertube.requests

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import it.fast4x.innertube.Innertube
import it.fast4x.innertube.YtMusic
import it.fast4x.innertube.models.Context
import it.fast4x.innertube.models.PlayerResponse
import it.fast4x.innertube.clients.YouTubeClient
import it.fast4x.innertube.clients.YouTubeClient.Companion.ANDROID_MUSIC
import it.fast4x.innertube.clients.YouTubeClient.Companion.IOS
import it.fast4x.innertube.models.BrowseResponse
import it.fast4x.innertube.models.bodies.PlayerBody
/*
suspend fun Innertube.newPlayer(body: PlayerBody, withLogin: Boolean = false): Result<PlayerResponse> = runCatching {

    val playerResponse = when (withLogin) {
        true -> try {
            println("Innertube.newPlayer Player Response Try Android")
            player(if (cookie != null) ANDROID_MUSIC else IOS, body.videoId, body.playlistId).body<PlayerResponse>()
        } catch (e: Exception) {
            println("Innertube.newPlayer Player Response Error $e")
            println("Innertube.newPlayer Player Response Try IOS")
            noLogInPlayer(body.videoId).body<PlayerResponse>()
        }
        false -> {
            println("Innertube.newPlayer Player Response without login")
            println("Innertube.newPlayer Player Response Try IOS")
            noLogInPlayer(body.videoId).body<PlayerResponse>()
        }
    }

    println("Innertube.newPlayer Player Response $playerResponse")
    println("Innertube.newPlayer Player Response status: ${playerResponse.playabilityStatus?.status}")

    return@runCatching playerResponse

}
*/
suspend fun Innertube.player(body: PlayerBody, withLogin: Boolean = false): Result<PlayerResponse> = runCatching {
    val response = when (withLogin) {
        true -> try {
            println("Innertube newPlayer Player Response Try Android")
            //with login
            //player(if (cookie != null) Context.DefaultAndroid.client else Context.DefaultIOS.client, body.videoId, body.playlistId).body<PlayerResponse>()
            //whitout login
            player(body.videoId, body.playlistId).body<PlayerResponse>()
        } catch (e: Exception) {
            println("Innertube newPlayer Player Response Error $e")
            println("Innertube newPlayer Player Response Try IOS")
            noLogInPlayer(body.videoId, withLogin).body<PlayerResponse>()
        }
        false -> {
            println("Innertube newPlayer Player Response without login")
            println("Innertube newPlayer Player Response Try IOS")
            noLogInPlayer(body.videoId, withLogin).body<PlayerResponse>()
        }
    }

    println("Innertube newPlayer withLogin $withLogin response adaptiveFormats ${response.streamingData?.adaptiveFormats}")
    println("Innertube newPlayer withLogin $withLogin response Formats ${response.streamingData?.formats}")
    println("Innertube newPlayer withLogin $withLogin response expire ${response.streamingData?.expiresInSeconds}")

    return@runCatching response
}

/*
suspend fun Innertube.player(body: PlayerBody, withLogin: Boolean = false): Result<PlayerResponse> = runCatching {

    val clientHttp = if (withLogin) ytHttpClient else client

    val response = clientHttp.post(player) {
        if (withLogin) {
            ytClient(YouTubeClient.ANDROID_MUSIC, setLogin = true)
            setBody(body.copy(
                context = YouTubeClient.ANDROID_MUSIC.toContext(locale, visitorData)
            ))
        } else {
            ytClient(YouTubeClient.IOS, setLogin = false)
            //setBody(body.copy(context = Context.DefaultIOS))
            setBody(body.copy(
                context = YouTubeClient.IOS.toContext(locale, visitorData)
            ))
        }
        //mask("playabilityStatus.status,playerConfig.audioConfig,streamingData.adaptiveFormats,videoDetails.videoId")
    }.body<PlayerResponse>()

    println("PlayerService DownloadHelper Innertube.player withLogin $withLogin response $response")

    if (response.playabilityStatus?.status == "OK") {
        return@runCatching response
    }

    // Try again with android music client DefaultRestrictionBypass
    val context = Context.DefaultRestrictionBypass
    val safePlayerResponse = clientHttp.post(player) {
        setBody(
            body.copy(
                context = context.copy(
                    thirdParty = Context.ThirdParty(
                        embedUrl = "https://www.youtube.com/watch?v=${body.videoId}"
                    )
                ),
            )
        )
        mask("playabilityStatus.status,playerConfig.audioConfig,streamingData.adaptiveFormats,videoDetails.videoId")
    }.body<PlayerResponse>()

    println("PlayerServiceModern DownloadHelper Innertube.player withLogin $withLogin safePlayerResponse $safePlayerResponse")

    if (safePlayerResponse.playabilityStatus?.status == "OK") {
        return@runCatching safePlayerResponse
    }

    //response
    safePlayerResponse

}.onFailure {
    println("YoutubeLogin PlayerServiceModern NEW Innertube.player error ${it.stackTraceToString()}")
}
*/
/*
suspend fun Innertube.player(
    body: PlayerBody,
    //pipedApiInstance: String = "pipedapi.adminforge.de",
    pipedSession: Session
    ) = runCatchingNonCancellable {

        val response = client.post(player) {
            setBody(body)
            mask("playabilityStatus.status,playerConfig.audioConfig,streamingData.adaptiveFormats,videoDetails.videoId")
        }.body<PlayerResponse>()

        println("PlayerService Innertube.player response $response")


        if (response.playabilityStatus?.status == "OK") {
            response
        } else {
            val safePlayerResponse = client.post(player) {
                setBody(
                    body.copy(
                        context = Context.DefaultAndroid.copy(
                            thirdParty = Context.ThirdParty(
                                embedUrl = "https://www.youtube.com/watch?v=${body.videoId}"
                            )
                        ),
                    )
                )
                mask("playabilityStatus.status,playerConfig.audioConfig,streamingData.adaptiveFormats,videoDetails.videoId")
            }.body<PlayerResponse>()

            println("PlayerService Innertube.player response safePlayerResponse $safePlayerResponse")

            if (safePlayerResponse.playabilityStatus?.status == "OK") {
                safePlayerResponse
            } else {


                /**** INVIDIOUS TEST AS BACKUP LINE ****/
                println("PlayerService Innertube.player Invidious.api.videos ${body.videoId}")
                val safeResponse = Invidious.api.videos(body.videoId)?.getOrNull()

                if (safeResponse != null) {
                    safePlayerResponse.copy(
                        streamingData = safePlayerResponse.streamingData?.copy(
                            adaptiveFormats = safePlayerResponse.streamingData.adaptiveFormats?.map { adaptiveFormat ->
                                adaptiveFormat.copy(
                                    url = safeResponse.find { it.bitrate == adaptiveFormat.bitrate }?.url
                                )
                            }

                        )
                    )
                } else safePlayerResponse
                /**** INVIDIOUS ****/


                /*
                @Serializable
                data class AudioStream(
                    val url: String,
                    val bitrate: Long
                )

                @Serializable
                data class PipedResponse(
                    val audioStreams: List<AudioStream>
                )

                val audioStreams = client.get("https://$pipedApiInstance/streams/${body.videoId}") {
                    contentType(ContentType.Application.Json)
                }.body<PipedResponse>().audioStreams
                 */

                //safePlayerResponse // temporaly used

                /*
                    // TODO() Piped api streams not working, improve with other service
                    val audioStreams = Piped.media.audioStreams(
                        session = pipedSession,
                        videoId = body.videoId
                    )?.getOrNull()

                println("PlayerService Innertube.player PIPED audiostreams $audioStreams")

                    safePlayerResponse.copy(
                        streamingData = safePlayerResponse.streamingData?.copy(
                            adaptiveFormats = safePlayerResponse.streamingData.adaptiveFormats?.map { adaptiveFormat ->
                                adaptiveFormat.copy(
                                    url = audioStreams?.find { it.bitrate == adaptiveFormat.bitrate }?.url
                                )
                            }
                        )
                    )

                 */


            }

        }



    }

*/