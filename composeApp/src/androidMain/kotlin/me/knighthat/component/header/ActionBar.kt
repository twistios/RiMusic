package me.knighthat.component.header

import androidx.compose.foundation.background
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.extensions.pip.isPipSupported
import it.fast4x.rimusic.extensions.pip.rememberPipHandler
import it.fast4x.rimusic.utils.enablePictureInPictureKey
import it.fast4x.rimusic.utils.rememberPreference
import me.knighthat.colorPalette
import me.knighthat.menu.DropdownMenu

@Composable
private fun HamburgerMenu(
    expanded: Boolean,
    onItemClick: (NavRoutes) -> Unit,
    onDismissRequest: () -> Unit
) {
    val enablePictureInPicture by rememberPreference(enablePictureInPictureKey, false)
    val pipHandler = rememberPipHandler()

    val menu = DropdownMenu(
        expanded = expanded,
        modifier = Modifier.background( colorPalette().background0.copy(0.90f) ),
        onDismissRequest = onDismissRequest
    )
    // History button
    menu.add(
        DropdownMenu.Item(
            R.drawable.history,
            R.string.history
        ) { onItemClick( NavRoutes.history ) }
    )
    // Statistics button
    menu.add(
        DropdownMenu.Item(
            R.drawable.stats_chart,
            R.string.statistics
        ) { onItemClick( NavRoutes.statistics ) }
    )
    // Picture in picture button
    if (isPipSupported && enablePictureInPicture)
        menu.add(
            DropdownMenu.Item(
                R.drawable.picture,
                R.string.menu_go_to_picture_in_picture
            ) { pipHandler.enterPictureInPictureMode() }
        )
    menu.add { HorizontalDivider() }
    // Settings button
    menu.add(
        DropdownMenu.Item(
            R.drawable.settings,
            R.string.settings
        ) { onItemClick( NavRoutes.settings ) }
    )

    menu.Draw()
}

// START
@Composable
fun ActionBar(
    navController: NavController,
) {
    var expanded by remember { mutableStateOf(false) }

    // Search Icon
    HeaderIcon( R.drawable.search) { navController.navigate(NavRoutes.search.name) }
    // Hamburger Icon
    HeaderIcon( R.drawable.burger ) { expanded = !expanded }

    // Define actions for when item inside menu clicked,
    // and when user clicks on places other than the menu (dismiss)
    val onItemClick: (NavRoutes) -> Unit = {
        expanded = false
        navController.navigate(it.name)
    }
    val onDismissRequest: () -> Unit = { expanded = false }

    // Hamburger menu
    HamburgerMenu(
        expanded = expanded,
        onItemClick = onItemClick,
        onDismissRequest = onDismissRequest
    )
// END
}