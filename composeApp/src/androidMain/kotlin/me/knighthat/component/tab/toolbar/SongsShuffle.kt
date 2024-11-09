package me.knighthat.component.tab.toolbar

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.media3.common.util.UnstableApi
import it.fast4x.rimusic.R
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.service.modern.PlayerServiceModern
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.utils.PlayShuffledSongs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import me.knighthat.component.header.TabToolBar
import kotlin.coroutines.CoroutineContext

interface SongsShuffle: Button {

    @get:UnstableApi
    val binder: PlayerServiceModern.Binder?
    val context: Context
    val dispatcher: CoroutineContext
        /**
         * ATTENTION: If query() returns song list from
         * data base, Dispatchers.IO must be indicated.
         * <p>
         * If query() returns a pre-defined list,
         * Dispatchers.Main is the choice.
         */
        get() = Dispatchers.IO

    fun query(): Flow<List<Song>?>

    @UnstableApi
    @Composable
    override fun ToolBarButton() {

        TabToolBar.Icon(
            iconId = R.drawable.shuffle,
            onShortClick = {
                CoroutineScope( dispatcher ).launch {
                    query().collect {
                        PlayShuffledSongs( it, context, binder )
                    }
                }
            },
            onLongClick = {
                SmartMessage(
                    context.resources.getString( R.string.shuffle ),
                    context = context
                )
            }
        )
    }
}