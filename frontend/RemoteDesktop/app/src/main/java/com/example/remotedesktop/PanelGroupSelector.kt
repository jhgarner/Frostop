package com.example.remotedesktop

import androidx.compose.runtime.Composable
import com.example.remotedesktop.App.Companion.panelGroups

data class PanelGroupSelector(
    val sessionInfo: SessionInfo,
) : EditableSelector<PanelGroup> {
    override val getList = panelGroups
    override val headingText = "Select a Panel Group"

    override fun default() = PanelGroup()
    override fun String.deserialize(): PanelGroup? = decodeSafely<PanelGroup>(this)

    @Composable
    override fun NextStep(selected: PanelGroup, navigateBack: () -> Unit) {
        SettingsSelector(
            sessionInfo = sessionInfo,
            panelGroup = selected,
        ).ShowSelecter(navigateBack)
    }
}
