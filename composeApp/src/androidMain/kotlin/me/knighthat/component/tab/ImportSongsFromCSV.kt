package me.knighthat.component.tab

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.transaction
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import me.knighthat.appContext
import me.knighthat.component.tab.toolbar.Descriptive
import me.knighthat.component.tab.toolbar.MenuIcon

class ImportSongsFromCSV private constructor(
    private val launcher: ManagedActivityResultLauncher<Array<String>, Uri?>
): Descriptive, MenuIcon {

    companion object {
        private fun openFile(
            uri: Uri,
            beforeTransaction: (Int, Map<String, String>) -> Unit = { _,_ -> },
            afterTransaction: ( Int, Song ) -> Unit = { _,_ -> }
        ) {
            appContext().applicationContext
                        .contentResolver
                        .openInputStream(uri)
                        ?.use { inputStream ->
                            csvReader().open(inputStream) {
                                readAllWithHeaderAsSequence().forEachIndexed { index, row: Map<String, String> ->
                                    println("mediaItem index song $index")

                                    transaction {
                                        beforeTransaction( index, row )
                                        /**/
                                        val mediaId = row["MediaId"]
                                        val title = row["Title"]

                                        if( mediaId == null || title == null)
                                            return@transaction

                                        val song = Song (
                                            id = mediaId,
                                            title = title,
                                            artistsText = row["Artists"],
                                            durationText = row["Duration"],
                                            thumbnailUrl = row["ThumbnailUrl"],
                                            totalPlayTimeMs = 1L
                                        )
                                        afterTransaction( index, song )
                                    }
                                }
                            }
                        }
        }

        @JvmStatic
        @Composable
        fun init(
            beforeTransaction: (Int, Map<String, String>) -> Unit = { _,_ -> },
            afterTransaction: ( Int, Song ) -> Unit = { _,_ -> }
        ) = ImportSongsFromCSV(
            rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocument()
            ) { uri ->
                if( uri == null ) return@rememberLauncherForActivityResult

                openFile( uri, beforeTransaction, afterTransaction )
            }
        )
    }

    override val messageId: Int = R.string.import_playlist
    override val iconId: Int = R.drawable.resource_import
    override val menuIconTitle: String
        @Composable
        get() = stringResource( messageId )

    override fun onShortClick() {
        try {
            launcher.launch( arrayOf("text/csv", "text/comma-separated-values") )
        } catch (_: ActivityNotFoundException) {
            SmartMessage(
                appContext().resources.getString( R.string.info_not_find_app_open_doc ),
                type = PopupType.Warning, context = appContext()
            )
        }
    }
}