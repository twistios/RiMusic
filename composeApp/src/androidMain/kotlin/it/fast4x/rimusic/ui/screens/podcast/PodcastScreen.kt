package it.fast4x.rimusic.ui.screens.podcast

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import it.fast4x.compose.persist.PersistMapCleanup
import it.fast4x.compose.routing.RouteHandler
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.ui.components.Scaffold
import it.fast4x.rimusic.ui.screens.globalRoutes
import it.fast4x.rimusic.utils.UiTypeKey
import it.fast4x.rimusic.utils.rememberPreference

@ExperimentalMaterialApi
@ExperimentalTextApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun PodcastScreen(
    navController: NavController,
    browseId: String,
    params: String?,
    maxDepth: Int? = null,
    playerEssential: @Composable () -> Unit = {},
) {
    val saveableStateHolder = rememberSaveableStateHolder()
    PersistMapCleanup(tagPrefix = "podcast/$browseId")


    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()
        val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)
        host {
            Scaffold(
                navController = navController,
                playerEssential = playerEssential,
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                showButton1 = uiType != UiType.RiMusic,
                topIconButton2Id = R.drawable.chevron_back,
                onTopIconButton2Click = pop,
                showButton2 = false,
                tabIndex = 0,
                onTabChanged = { },
                onHomeClick = {
                    //homeRoute()
                    navController.navigate(NavRoutes.home.name)
                },
                tabColumnContent = { Item ->
                    Item(0, stringResource(R.string.podcast_episodes), R.drawable.podcast)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> Podcast(
                            navController = navController,
                            browseId = browseId,
                            params = params,
                            maxDepth = maxDepth
                        )
                    }
                }
            }
        }
    }
}
