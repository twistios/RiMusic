package it.fast4x.innertube.requests

import io.ktor.http.Url
import it.fast4x.innertube.Innertube
import it.fast4x.innertube.models.NavigationEndpoint
import it.fast4x.innertube.models.bodies.BrowseBody

suspend fun Innertube.albumPage(body: BrowseBody) = playlistPage(body)?.map { album ->
    album.url?.let { Url(it).parameters["list"] }?.let { playlistId ->
        playlistPage(BrowseBody(browseId = "VL$playlistId"))?.getOrNull()?.let { playlist ->
            println("mediaItem albumPage pre songsPage ${playlist.songsPage?.items?.size}")
            album.copy(songsPage = playlist.songsPage)
        }
    } ?: album
    }

    ?.map { album ->
        //println("mediaItem albumPage post songsPage ${album?.songsPage?.items?.size} des ${album?.description} browseId ${body.browseId}")
        /*
        println("mediaItem albumPage post songsPage songs id ${album?.songsPage?.items?.size}")
        album?.songsPage?.items?.forEach {
            println("mediaItem albumPage post songsPage song id ${it.info?.endpoint?.videoId} playlistId ")
        }
         */

        val albumInfo = Innertube.Info(
            name = album.title,
            endpoint = NavigationEndpoint.Endpoint.Browse(
                browseId = body.browseId,
                params = body.params
            )
        )

        album.copy(
            songsPage = album.songsPage?.copy(
                items = album.songsPage.items?.map { song ->
                    song.copy(
                        authors = song.authors ?: album.authors,
                        album = albumInfo,
                        thumbnail = album.thumbnail
                    )
                }
            )
        )

    }?.onFailure {
        println("ERROR IN Innertube albumPage " + it.message)
    }
