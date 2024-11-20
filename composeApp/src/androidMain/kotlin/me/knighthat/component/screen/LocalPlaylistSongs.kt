package me.knighthat.component.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.PINNED_PREFIX
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.MenuStyle
import it.fast4x.rimusic.enums.PlaylistSongSortBy
import it.fast4x.rimusic.enums.SortOrder
import it.fast4x.rimusic.models.PlaylistPreview
import it.fast4x.rimusic.transaction
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.MenuState
import it.fast4x.rimusic.ui.components.themed.MenuEntry
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.utils.autosyncKey
import it.fast4x.rimusic.utils.menuStyleKey
import it.fast4x.rimusic.utils.playlistSongSortByKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.reorderInQueueEnabledKey
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.songSortOrderKey
import me.knighthat.appContext
import me.knighthat.component.tab.toolbar.Descriptive
import me.knighthat.component.tab.toolbar.DualIcon
import me.knighthat.component.tab.toolbar.DynamicColor
import me.knighthat.component.tab.toolbar.MenuIcon
import me.knighthat.component.tab.Sort
import me.knighthat.typography


@Composable
fun pin(
    playlistPreview: PlaylistPreview?,
    playlistId: Long
): MenuIcon = object: MenuIcon, DynamicColor, Descriptive {

    override val iconId: Int = R.drawable.pin
    override val messageId: Int = R.string.info_pin_unpin_playlist
    override val menuIconTitle: String
        @Composable
        get() = stringResource( messageId )

    override var isFirstColor: Boolean by rememberSaveable( playlistPreview ) { mutableStateOf( isPinned() ) }

    fun isPinned(): Boolean {
        return playlistPreview?.playlist
                              ?.name
                              ?.startsWith( PINNED_PREFIX, true ) == true
    }

    override fun onShortClick() {
        transaction {
            val playlistName = playlistPreview?.playlist?.name ?: return@transaction
            if( playlistName.startsWith( PINNED_PREFIX ) )
                Database.unPinPlaylist( playlistId )
            else
                Database.pinPlaylist( playlistId )

            isFirstColor = isPinned()
        }
    }
}

@Composable
fun positionLock(
    sortOrder: SortOrder
): MenuIcon = object: MenuIcon, DualIcon, DynamicColor, Descriptive {

    override val secondIconId: Int = R.drawable.unlocked
    override val iconId: Int = R.drawable.locked
    override val messageId: Int = R.string.info_lock_unlock_reorder_songs
    override val menuIconTitle: String
        @Composable
        get() = stringResource( messageId )

    /**
     * If [isFirstIcon] equals `true` then user CANNOT change songs' positions
     */
    override var isFirstIcon: Boolean by rememberPreference( reorderInQueueEnabledKey, true )
    override var isFirstColor: Boolean = rememberSaveable( sortOrder ) { sortOrder == SortOrder.Ascending }

    override fun onShortClick() {
        if( !isFirstColor )
            SmartMessage(
                message = appContext().resources.getString( R.string.info_reorder_is_possible_only_in_ascending_sort ),
                context = appContext()
            )
        else
            isFirstIcon = !isFirstIcon
    }
}

@Composable
fun playlistSync(): MenuIcon = object : MenuIcon, DynamicColor, Descriptive {

    override var isFirstColor: Boolean by rememberPreference( autosyncKey, false )
    override val iconId: Int = R.drawable.sync
    override val messageId: Int = R.string.autosync
    override val menuIconTitle: String
        @Composable
        get() = stringResource( messageId )

    override fun onShortClick() { isFirstColor = !isFirstColor }
}

class PlaylistSongsSort private constructor(
    sortOrderState: MutableState<SortOrder>,
    sortByState: MutableState<PlaylistSongSortBy>,
    menuState: MenuState,
    styleState: MutableState<MenuStyle>
): Sort<PlaylistSongSortBy>( sortOrderState, PlaylistSongSortBy.entries, sortByState, menuState, styleState) {

    companion object {
        @JvmStatic
        @Composable
        fun init() = PlaylistSongsSort(
            rememberPreference( songSortOrderKey, SortOrder.Descending ),
            rememberPreference ( playlistSongSortByKey, PlaylistSongSortBy.Title ),
            LocalMenuState.current,
            rememberPreference( menuStyleKey, MenuStyle.List )
        )
    }

    @Composable
    private fun sortTitle( sortBy: PlaylistSongSortBy ): String =
        when( sortBy ) {
            PlaylistSongSortBy.ArtistAndAlbum ->
                "${stringResource(R.string.sort_artist)}, ${stringResource(R.string.sort_album)}"

            else -> stringResource( sortBy.titleId )
        }

    @Composable
    override fun MenuComponent() {
        super.Menu( sortByEntries ) {
            val icon = it.icon

            MenuEntry(
                painter = icon,
                text = sortTitle( it ),
                onClick = {
                    // Don't pass menuState::hide, it won't work
                    menuState.hide()
                    sortByState.value = it
                }
            )
        }
    }

    @Composable
    override fun ToolBarButton() {
        super.ToolBarButton()

        BasicText(
            text = sortTitle( this.sortBy ),
            style = typography().xs.semiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable { super.onLongClick() }
        )
    }

    override fun onLongClick() { /* Does nothing */ }
}