package it.fast4x.rimusic.ui.screens.settings

import android.content.Context
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.ui.components.themed.HeaderWithIcon
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.utils.getVersionName
import it.fast4x.rimusic.utils.secondary
import me.knighthat.colorPalette
import me.knighthat.typography
import me.knighthat.ui.screens.settings.about.DevBoard


@ExperimentalAnimationApi
@Composable
fun About() {
    val uriHandler = LocalUriHandler.current
    //val context = LocalContext.current

    Column(
        modifier = Modifier
            .background(colorPalette().background0)
            //.fillMaxSize()
            .fillMaxHeight()
            .fillMaxWidth(
                if (NavigationBarPosition.Right.isCurrent())
                    Dimensions.contentWidthRightBar
                else
                    1f
            )
            .verticalScroll(rememberScrollState())
            /*
            .padding(
                LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                    .asPaddingValues()
            )

             */
    ) {
        HeaderWithIcon(
            title = stringResource(R.string.about),
            iconId = R.drawable.information,
            enabled = false,
            showIcon = true,
            modifier = Modifier,
            onClick = {}
        )
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
        ) {
            BasicText(
                text = "RiMusic v${getVersionName()} by fast4x",
                style = typography().s.secondary,

                )
        }

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = stringResource(R.string.social))

        SettingsEntry(
            title = stringResource(R.string.social_telegram),
            text = stringResource(R.string.social_telegram_info),
            onClick = {
                uriHandler.openUri("https://t.me/rimusic_app")
            }
        )

        SettingsEntry(
            title = stringResource(R.string.social_reddit),
            text = stringResource(R.string.social_reddit_info),
            onClick = {
                uriHandler.openUri("https://www.reddit.com/r/RiMusicApp/")
            }
        )

        SettingsEntry(
            title = stringResource(R.string.social_github),
            text = stringResource(R.string.view_the_source_code),
            onClick = {
                uriHandler.openUri("https://github.com/fast4x/RiMusic")
            }
        )

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = stringResource(R.string.troubleshooting))

        SettingsEntry(
            title = stringResource(R.string.report_an_issue),
            text = stringResource(R.string.you_will_be_redirected_to_github),
            onClick = {
                uriHandler.openUri("https://github.com/fast4x/RiMusic/issues/new?assignees=&labels=bug&template=bug_report.yaml")
            }
        )


        SettingsEntry(
            title = stringResource(R.string.request_a_feature_or_suggest_an_idea),
            text = stringResource(R.string.you_will_be_redirected_to_github),
            onClick = {
                uriHandler.openUri("https://github.com/fast4x/RiMusic/issues/new?assignees=&labels=feature_request&template=feature_request.yaml")
            }
        )

        SettingsGroupSpacer()

        HeaderWithIcon(
            title = stringResource(R.string.contributors),
            iconId = R.drawable.artists,
            enabled = false,
            showIcon = true,
            modifier = Modifier,
            onClick = {}
        )
        SettingsGroupSpacer()

        SettingsEntryGroupText(title = stringResource(R.string.translators))
        SettingsDescription(text = stringResource(R.string.in_alphabetical_order))
        SettingsTopDescription( text =
            "2010furs \n"+
                    "821938089 \n"+
                    "abfreeman \n"+
                    "ABS zarzis \n"+
                    "Adam Kop \n"+
                    "agefcgo \n"+
                    "Ahmad Al Juwaisri \n"+
                    "Alnoer \n"+
                    "Aniol \n" +
                    "Ann Naser Nabil \n"+
                    "AntoniNowak \n" +
                    "beez276 \n"+
                    "benhaotang \n" +
                    "CiccioDerole \n"+
                    "Clyde6790p_PH \n"+
                    "Conk \n"+
                    "Corotyest \n" +
                    "Crayz310 \n"+
                    "cultcats \n"+
                    "CUMOON \n"+
                    "DanielSevillano \n"+
                    "Dženan \n" +
                    "EMC_Translator \n"+
                    "Fabian Urra \n"+
                    "fast4x \n"+
                    "Fausta Ahmad \n"+
                    "Get100percent \n"+
                    "Glich440 \n"+
                    "HelloZebra1133 \n"+
                    "Ikanakova \n"+
                    "iOSStarWorld \n"+
                    "IvanMaksimovic77 \n"+
                    "JZITNIK-github \n"+
                    "Kjev666 \n"+
                    "Kptmx \n"+
                    "koliwan \n"+
                    "Lolozweipunktnull \n" +
                    "ManuelCoimbra) \n" +
                    "Marinkas \n"+
                    "materialred \n"+
                    "Mid_Vur_Shaan \n" +
                    "Muha Aliss \n"+
                    "Ndvok \n"+
                    "Nebula-Mechanica \n"+
                    "NEVARLeVrai \n"+
                    "NikunjKhangwal \n"+
                    "NiXT0y \n"+
                    "Notesuree \n" +
                    "opcitgv \n"+
                    "OlimitLolli \n"+
                    "OrangeZXZ \n"+
                    "RegularWater \n"+
                    "Rikalaj \n" +
                    "Roklc \n"+
                    "sebbe.ekman \n"+
                    "Seryoga1984 \n" +
                    "SharkChan0622 \n"+
                    "Sharunkumar \n" +
                    "Shilave malay \n"+
                    "softinterlingua \n"+
                    "SureshTimma \n"+
                    "Siggi1984 \n"+
                    "Teaminh \n"+
                    "TeddysulaimanGL \n"+
                    "YeeTW \n"+
                    "Th3-C0der \n" +
                    "TheCreeperDuck \n"+
                    "TsyQax \n"+
                    "VINULA2007 \n" +
                    "Vladimir \n" +
                    "xSyntheticWave \n"+
                    "Zan1456 \n" +
                    "ZeroZero00 \n"
        )

        SettingsEntryGroupText(title = "Developers / Designers")
        SettingsDescription(text = stringResource(R.string.in_alphabetical_order))
        DevBoard()

        SettingsGroupSpacer(
            modifier = Modifier.height(Dimensions.bottomSpacer)
        )
    }
}
