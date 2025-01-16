package it.fast4x.innertube

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.compression.brotli
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.userAgent
import io.ktor.serialization.kotlinx.json.json
import it.fast4x.innertube.models.AccountInfo
import it.fast4x.innertube.models.AccountMenuResponse
import it.fast4x.innertube.models.MusicNavigationButtonRenderer
import it.fast4x.innertube.models.NavigationEndpoint
import it.fast4x.innertube.models.Runs
import it.fast4x.innertube.models.Thumbnail
import it.fast4x.innertube.clients.YouTubeClient
import it.fast4x.innertube.clients.YouTubeClient.Companion.IOS
import it.fast4x.innertube.clients.YouTubeClient.Companion.WEB_REMIX
import it.fast4x.innertube.clients.YouTubeLocale
import it.fast4x.innertube.models.BrowseResponse
import it.fast4x.innertube.models.Context
import it.fast4x.innertube.models.Context.Client
import it.fast4x.innertube.models.Context.Companion.DefaultAndroid
import it.fast4x.innertube.models.Context.Companion.DefaultIOS
import it.fast4x.innertube.models.bodies.AccountMenuBody
import it.fast4x.innertube.models.bodies.Action
import it.fast4x.innertube.models.bodies.BrowseBody
import it.fast4x.innertube.models.bodies.CreatePlaylistBody
import it.fast4x.innertube.models.bodies.EditPlaylistBody
import it.fast4x.innertube.models.bodies.PlayerBody
import it.fast4x.innertube.models.bodies.PlaylistDeleteBody
import it.fast4x.innertube.utils.ProxyPreferences
import it.fast4x.innertube.utils.YoutubePreferences
import it.fast4x.innertube.utils.getProxy
import it.fast4x.innertube.utils.parseCookieString
import it.fast4x.innertube.utils.sha1
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.logging.HttpLoggingInterceptor
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.Locale

object Innertube {

    private const val VISITOR_DATA_PREFIX = "Cgt"

    //const val DEFAULT_VISITOR_DATA = "CgtsZG1ySnZiQWtSbyiMjuGSBg%3D%3D"
    const val DEFAULT_VISITOR_DATA = "CgtMN0FkbDFaWERfdyi8t4u7BjIKCgJWThIEGgAgWQ%3D%3D"

    @OptIn(ExperimentalSerializationApi::class)
    val client = HttpClient(OkHttp) {
        //BrowserUserAgent()

        expectSuccess = true

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
            })
        }

        install(ContentEncoding) {
            //brotli(1.0F)
            gzip(0.9F)
            deflate(0.8F)
        }

        engine {
            addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
        }

        ProxyPreferences.preference?.let {
            engine {
                proxy = getProxy(it)
            }
        }

        defaultRequest {
            url(scheme = "https", host ="music.youtube.com") {
                headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                headers.append("X-Goog-Api-Key", "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8")
                //parameters.append("prettyPrint", "false")
            }
        }
    }

    var proxy: Proxy? = null
        set(value) {
            field = value
            client.close()
            client
        }

    var locale = YouTubeLocale(
        gl = Locale.getDefault().country,
        hl = Locale.getDefault().toLanguageTag()
        //gl = LocalePreferences.preference?.gl ?: "US",
        //hl = LocalePreferences.preference?.hl ?: "en"
    )
    var visitorData: String = YoutubePreferences.preference?.visitordata.toString()
    var cookieMap = emptyMap<String, String>()
    //var cookie: String? = YoutubePreferences.preference?.cookie
    var cookie: String? = YoutubePreferences.preference?.cookie
        set(value) {
            field = value
            cookieMap = if (value == null) emptyMap() else parseCookieString(value)
        }

    internal const val browse = "/youtubei/v1/browse"
    internal const val next = "/youtubei/v1/next"
    internal const val player = "/youtubei/v1/player"
    internal const val queue = "/youtubei/v1/music/get_queue"
    internal const val search = "/youtubei/v1/search"
    internal const val searchSuggestions = "/youtubei/v1/music/get_search_suggestions"
    internal const val accountMenu = "/youtubei/v1/account/account_menu"
    internal const val playlistCreate = "/youtubei/v1/playlist/create"
    internal const val playlistDelete = "/youtubei/v1/playlist/delete"
    internal const val playlistEdit = "/youtubei/v1/browse/edit_playlist"


    internal const val musicResponsiveListItemRendererMask = "musicResponsiveListItemRenderer(flexColumns,fixedColumns,thumbnail,navigationEndpoint,badges)"
    internal const val musicTwoRowItemRendererMask = "musicTwoRowItemRenderer(thumbnailRenderer,title,subtitle,navigationEndpoint)"
    const val playlistPanelVideoRendererMask = "playlistPanelVideoRenderer(title,navigationEndpoint,longBylineText,shortBylineText,thumbnail,lengthText)"

    internal fun HttpRequestBuilder.mask(value: String = "*") =
        header("X-Goog-FieldMask", value)


    @Serializable
    data class Info<T : NavigationEndpoint.Endpoint>(
        val name: String?,
        val endpoint: T?
    ) {
        @Suppress("UNCHECKED_CAST")
        constructor(run: Runs.Run) : this(
            name = run.text,
            endpoint = run.navigationEndpoint?.endpoint as T?
        )
    }

    @JvmInline
    value class SearchFilter(val value: String) {
        companion object {
            val Song = SearchFilter("EgWKAQIIAWoKEAkQBRAKEAMQBA%3D%3D")
            val Video = SearchFilter("EgWKAQIQAWoKEAkQChAFEAMQBA%3D%3D")
            val Album = SearchFilter("EgWKAQIYAWoKEAkQChAFEAMQBA%3D%3D")
            val Artist = SearchFilter("EgWKAQIgAWoKEAkQChAFEAMQBA%3D%3D")
            val CommunityPlaylist = SearchFilter("EgeKAQQoAEABagoQAxAEEAoQCRAF")
            val FeaturedPlaylist = SearchFilter("EgeKAQQoADgBagwQDhAKEAMQBRAJEAQ%3D")
            val Podcast = SearchFilter("EgWKAQJQAWoIEBAQERADEBU%3D")
        }
    }

    @Serializable
    sealed class Item {
        abstract val thumbnail: Thumbnail?
        abstract val key: String
        abstract val title: String?
    }

    @Serializable
    data class SongItem(
        val info: Info<NavigationEndpoint.Endpoint.Watch>?,
        val authors: List<Info<NavigationEndpoint.Endpoint.Browse>>?,
        val album: Info<NavigationEndpoint.Endpoint.Browse>?,
        val durationText: String?,
        override val thumbnail: Thumbnail?,
        val explicit: Boolean = false
    ) : Item() {
        //override val key get() = info!!.endpoint!!.videoId!!
        override val key get() = info?.endpoint?.videoId ?: ""
        override val title get() = info?.name

        companion object
    }

    @Serializable
    data class VideoItem(
        val info: Info<NavigationEndpoint.Endpoint.Watch>?,
        val authors: List<Info<NavigationEndpoint.Endpoint.Browse>>?,
        val viewsText: String?,
        val durationText: String?,
        override val thumbnail: Thumbnail?
    ) : Item() {
        override val key get() = info!!.endpoint!!.videoId!!
        override val title get() = info?.name

        val isOfficialMusicVideo: Boolean
            get() = info
                ?.endpoint
                ?.watchEndpointMusicSupportedConfigs
                ?.watchEndpointMusicConfig
                ?.musicVideoType == "MUSIC_VIDEO_TYPE_OMV"

        val isUserGeneratedContent: Boolean
            get() = info
                ?.endpoint
                ?.watchEndpointMusicSupportedConfigs
                ?.watchEndpointMusicConfig
                ?.musicVideoType == "MUSIC_VIDEO_TYPE_UGC"

        companion object
    }

    @Serializable
    data class AlbumItem(
        val info: Info<NavigationEndpoint.Endpoint.Browse>?,
        val authors: List<Info<NavigationEndpoint.Endpoint.Browse>>?,
        val year: String?,
        override val thumbnail: Thumbnail?
    ) : Item() {
        override val key get() = info!!.endpoint!!.browseId!!
        override val title get() = info?.name

        companion object
    }

    @Serializable
    data class ArtistItem(
        val info: Info<NavigationEndpoint.Endpoint.Browse>?,
        val subscribersCountText: String?,
        override val thumbnail: Thumbnail?
    ) : Item() {
        override val key get() = info!!.endpoint!!.browseId!!
        override val title get() = info?.name

        companion object
    }

    @Serializable
    data class PlaylistItem(
        val info: Info<NavigationEndpoint.Endpoint.Browse>?,
        val channel: Info<NavigationEndpoint.Endpoint.Browse>?,
        val songCount: Int?,
        override val thumbnail: Thumbnail?
    ) : Item() {
        override val key get() = info!!.endpoint!!.browseId!!
        override val title get() = info?.name

        companion object
    }

    data class ArtistInfoPage(
        val name: String?,
        val description: String?,
        val subscriberCountText: String?,
        val thumbnail: Thumbnail?,
        val shuffleEndpoint: NavigationEndpoint.Endpoint.Watch?,
        val radioEndpoint: NavigationEndpoint.Endpoint.Watch?,
        val songs: List<SongItem>?,
        val songsEndpoint: NavigationEndpoint.Endpoint.Browse?,
        val albums: List<AlbumItem>?,
        val albumsEndpoint: NavigationEndpoint.Endpoint.Browse?,
        val singles: List<AlbumItem>?,
        val singlesEndpoint: NavigationEndpoint.Endpoint.Browse?,
        val playlists: List<PlaylistItem>?,
    )

    data class PlaylistOrAlbumPage(
        val title: String?,
        val authors: List<Info<NavigationEndpoint.Endpoint.Browse>>?,
        val year: String?,
        val thumbnail: Thumbnail?,
        val url: String?,
        val songsPage: ItemsPage<SongItem>?,
        val otherVersions: List<AlbumItem>?,
        val description: String?,
        val otherInfo: String?
    )

    data class NextPage(
        val itemsPage: ItemsPage<SongItem>?,
        val playlistId: String?,
        val params: String? = null,
        val playlistSetVideoId: String? = null
    )

    @Serializable
    data class RelatedPage(
        val songs: List<SongItem>? = null,
        val playlists: List<PlaylistItem>? = null,
        val albums: List<AlbumItem>? = null,
        val artists: List<ArtistItem>? = null,
    )
    data class RelatedSongs(
        val songs: List<SongItem>? = null
    )

    @Serializable
    data class DiscoverPage(
        val newReleaseAlbums: List<AlbumItem>,
        val moods: List<Mood.Item>
    )

    data class DiscoverPageAlbums(
        val newReleaseAlbums: List<AlbumItem>

    )

    @Serializable
    data class Mood(
        val title: String,
        val items: List<Item>
    ) {
        @Serializable
        data class Item(
            val title: String,
            val stripeColor: Long,
            val endpoint: NavigationEndpoint.Endpoint.Browse
        )
    }

    data class ItemsPage<T : Item>(
        var items: List<T>?,
        val continuation: String?
    )

    @Serializable
    data class ChartsPage(
        val playlists: List<PlaylistItem>? = null,
        val artists: List<ArtistItem>? = null,
        val videos: List<VideoItem>? = null,
        val songs: List<SongItem>? = null,
        val trending: List<SongItem>? = null
    )

    data class Podcast(
        val title: String,
        //val author: ArtistItem,
        val author: String?,
        val authorThumbnail: String?,
        val thumbnail: List<Thumbnail>,
        val description: String?,
        val listEpisode: List<EpisodeItem>
    ) {
        data class EpisodeItem(
            val title: String,
            //val author: ArtistItem,
            val author: String?,
            val description: String?,
            val thumbnail: List<Thumbnail>,
            val createdDay: String?,
            val durationString: String?,
            val videoId: String
        )
    }

    data class SearchSuggestions(
        val queries: List<String>,
        val recommendedSong: SongItem?,
        val recommendedAlbum: AlbumItem?,
        val recommendedArtist: ArtistItem?,
        val recommendedPlaylist: PlaylistItem?,
        val recommendedVideo: VideoItem?,
    )

    fun MusicNavigationButtonRenderer.toMood(): Mood.Item? {
        return Mood.Item(
            title = buttonText.runs.firstOrNull()?.text ?: return null,
            stripeColor = solid?.leftStripeColor ?: return null,
            endpoint = clickCommand.browseEndpoint ?: return null
        )
    }

    fun List<Thumbnail>.getBestQuality() =
        maxByOrNull { (it.width ?: 0) * (it.height ?: 0) }


    suspend fun accountInfo(): Result<AccountInfo> = runCatching {
        accountMenu()
            .body<AccountMenuResponse>()
            .actions?.get(0)?.openPopupAction?.popup?.multiPageMenuRenderer
            ?.header?.activeAccountHeaderRenderer
            ?.toAccountInfo() ?: AccountInfo(
                name = null,
                email = null,
                channelHandle = null,
                thumbnailUrl = null
            )
    }

    suspend fun accountInfoList(): Result<List<AccountInfo>> = runCatching {
        accountMenu()
            .body<AccountMenuResponse>()
            .actions?.get(0)?.openPopupAction?.popup?.multiPageMenuRenderer
            ?.header?.activeAccountHeaderRenderer
            ?.toAccountInfoList() ?: emptyList()
    }

    suspend fun accountMenu(): HttpResponse {
        val response =
            client.post(accountMenu) {
                setLogin(setLogin = true)
                setBody(AccountMenuBody())
            }

        println("YoutubeLogin Innertube accountMenuBody: ${AccountMenuBody()}")
        println("YoutubeLogin Innertube accountMenu RESPONSE: ${response.bodyAsText()}")

        return response
    }

    suspend fun getSwJsData() = client.get("https://music.youtube.com/sw.js_data")

    suspend fun visitorData(): Result<String> = runCatching {
        Json.parseToJsonElement(getSwJsData().bodyAsText().substring(5))
            .jsonArray[0]
            .jsonArray[2]
            .jsonArray.first { (it as? JsonPrimitive)?.content?.startsWith(VISITOR_DATA_PREFIX) == true }
            .jsonPrimitive.content
    }

    fun HttpRequestBuilder.setLogin(clientType: Client = Context.DefaultWeb.client, setLogin: Boolean = false) {
        contentType(ContentType.Application.Json)
        headers {
            append("X-Goog-Api-Format-Version", "1")
            append("X-YouTube-Client-Name", clientType.clientName)
            append("X-YouTube-Client-Version", clientType.clientVersion)
            append("x-origin", "https://music.youtube.com")
            if (clientType.referer != null) {
                append("Referer", clientType.referer)
            }
            if (setLogin) {
                cookie?.let { cookie ->
                    cookieMap = parseCookieString(cookie)
                    append("X-Goog-Authuser", "0")
                    append("X-Goog-Visitor-Id", visitorData)
                    append("Cookie", cookie)
                    if ("SAPISID" !in cookieMap || "__Secure-3PAPISID" !in cookieMap) return@let
                    val currentTime = System.currentTimeMillis() / 1000
                    val sapisidCookie = cookieMap["SAPISID"] ?: cookieMap["__Secure-3PAPISID"]
                    val sapisidHash = sha1("$currentTime $sapisidCookie https://music.youtube.com")
                    append("Authorization", "SAPISIDHASH ${currentTime}_$sapisidHash")
                }
            }
        }
        clientType.userAgent?.let { userAgent(it) }
        parameter("key", clientType.api_key)
        parameter("prettyPrint", false)

    }

    /*******************************************
     * NEW CODE
     */

    suspend fun createPlaylist(
        ytClient: Client,
        title: String,
    ) = client.post(playlistCreate) {
        setLogin(ytClient, true)
        setBody(
            CreatePlaylistBody(
                context = ytClient.toContext(locale, visitorData),
                title = title
            )
        )
    }

    suspend fun deletePlaylist(
        ytClient: Client,
        playlistId: String,
    ) = client.post(playlistDelete) {
        println("deleting $playlistId")
        setLogin(ytClient, setLogin = true)
        setBody(
            PlaylistDeleteBody(
                context = ytClient.toContext(locale, visitorData),
                playlistId = playlistId
            )
        )
    }

    suspend fun renamePlaylist(
        ytClient: Client,
        playlistId: String,
        name: String,
    ) = client.post(playlistEdit) {
        setLogin(ytClient, setLogin = true)
        setBody(
            EditPlaylistBody(
                context = ytClient.toContext(locale, visitorData),
                playlistId = playlistId,
                actions = listOf(
                    Action.RenamePlaylistAction(
                        playlistName = name
                    )
                )
            )
        )
    }

    suspend fun addToPlaylist(
        ytClient: Client,
        playlistId: String,
        videoId: String,
    ) = client.post(playlistEdit) {
        setLogin(ytClient, setLogin = true)
        setBody(
            EditPlaylistBody(
                context = ytClient.toContext(locale, visitorData),
                playlistId = playlistId.removePrefix("VL"),
                actions = listOf(
                    Action.AddVideoAction(addedVideoId = videoId)
                )
            )
        )
    }

    suspend fun removeFromPlaylist(
        ytClient: Client,
        playlistId: String,
        videoId: String,
        setVideoId: String? = null,
    ) = client.post(playlistEdit) {
        setLogin(ytClient, setLogin = true)
        setBody(
            EditPlaylistBody(
                context = ytClient.toContext(locale, visitorData),
                playlistId = playlistId.removePrefix("VL"),
                actions = listOf(
                    Action.RemoveVideoAction(
                        removedVideoId = videoId,
                        setVideoId = setVideoId,
                    )
                )
            )
        )
    }

    suspend fun browse(
        ytClient: Client = Context.DefaultWeb.client,
        browseId: String? = null,
        params: String? = null,
        continuation: String? = null,
        setLogin: Boolean = false,
    ) = client.post(browse) {
        setLogin(ytClient, true)
        setBody(
            BrowseBody(
                context = Context.DefaultWebWithLocale,
                browseId = browseId,
                params = params,
            )
        )
        parameter("continuation", continuation)
        parameter("ctoken", continuation)
        if (continuation != null) {
            parameter("type", "next")
        }
    }

    suspend fun player(
        //ytClient: Client,
        videoId: String,
        playlistId: String?,
    ) = client.post(player) {
        //setLogin(ytClient, setLogin = true)
        setLogin(setLogin = true)
        setBody(
            PlayerBody(
//                context = ytClient.toContext(locale, visitorData).let {
//                    if (ytClient == Context.DefaultRestrictionBypass.client) {
//                        it.copy(
//                            thirdParty =
//                            Context.ThirdParty(
//                                embedUrl = "https://www.youtube.com/watch?v=$videoId",
//                            ),
//                        )
//                    } else {
//                        it
//                    }
//                },
                videoId = videoId,
                playlistId = playlistId,
            ),
        )
    }

    suspend fun noLogInPlayer(videoId: String, withLogin: Boolean = false) =
        client.post(player) {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            header("Host", "music.youtube.com")
            setBody(
                PlayerBody(
                    context = DefaultIOS,
                    playlistId = null,
                    videoId = videoId,
                ),
            )
        }

    suspend fun customBrowse(
        browseId: String? = null,
        params: String? = null,
        continuation: String? = null,
        setLogin: Boolean = true,
    ) = runCatching {
        browse(Context.DefaultWeb.client, browseId, params, continuation, setLogin).body<BrowseResponse>()
    }

}
