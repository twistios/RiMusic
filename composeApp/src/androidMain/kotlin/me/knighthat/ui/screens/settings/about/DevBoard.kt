package me.knighthat.ui.screens.settings.about

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.JsonArray
import it.fast4x.rimusic.R
import me.knighthat.colorPalette
import timber.log.Timber
import java.io.InputStream

private val GSON = Gson()
private lateinit var DEVS: List<Developer>

private fun init( context: Context ) {
    try {
        // Open contributors.json file
        val fileStream: InputStream =
            context.resources.openRawResource(R.raw.contributors)
        // Parse content to JSON array
        val json: JsonArray =
            GSON.fromJson(fileStream.bufferedReader(), JsonArray::class.java)

        // Convert each object of JSON array to Developer instance
        // then sort it based on their username
        DEVS = json.map { GSON.fromJson(it, Developer::class.java) }
            .sortedByDescending { it.username }
    } catch ( e: Exception ) {
        Timber.e( e.stackTraceToString() )
        DEVS = emptyList()
    }
}

@Composable
fun DevBoard() {
    val context = LocalContext.current

    if( !::DEVS.isInitialized )
        init( context )

    if ( DEVS.isEmpty() ) return

    Box( Modifier.fillMaxWidth().height( 300.dp ) ) {
        // For decoration purposes only
        val dividerColor = colorPalette().textSecondary.copy( alpha = .5f )
        HorizontalDivider(
            modifier = Modifier.padding( start = 35.dp ).width( 50.dp ),
            color = dividerColor
        )
        VerticalDivider(
            modifier = Modifier.padding( start = 35.dp ).size( 80.dp ),
            color = dividerColor
        )
        // End of decoration

        LazyColumn( Modifier.fillMaxWidth().padding( top = 15.dp ) ) {
            items( DEVS ) { it.Draw() }
        }
    }
}