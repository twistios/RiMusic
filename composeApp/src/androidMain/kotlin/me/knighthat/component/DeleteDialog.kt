package me.knighthat.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import it.fast4x.rimusic.R
import it.fast4x.rimusic.ui.components.MenuState
import me.knighthat.component.tab.toolbar.ConfirmDialog
import me.knighthat.component.tab.toolbar.Descriptive
import me.knighthat.component.tab.toolbar.MenuIcon

abstract class DeleteDialog protected constructor(
    protected val activeState: MutableState<Boolean>,
    protected val menuState: MenuState
): ConfirmDialog, MenuIcon, Descriptive {

    override val iconId: Int = R.drawable.trash
    override val messageId: Int = R.string.delete
    override val menuIconTitle: String
        @Composable
        get() = stringResource( messageId )

    override var isActive: Boolean = activeState.value
        set(value) {
            activeState.value = value
            field = value
        }

    override fun onShortClick() = super.onShortClick()

    override fun onDismiss() {
        super.onDismiss()
        menuState.hide()
    }
}