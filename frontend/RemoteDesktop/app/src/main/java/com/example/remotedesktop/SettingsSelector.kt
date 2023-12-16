package com.example.remotedesktop

import androidx.compose.runtime.Composable
import com.example.remotedesktop.App.Companion.videoParams

data class SettingsSelector(
    val sessionInfo: SessionInfo,
    val panelGroup: PanelGroup,
) : EditableSelector<Settings> {
    override val getList = videoParams
    override val headingText = "Select Video Settings"

    override fun default() = Settings()
    override fun String.deserialize(): Settings? = decodeSafely<Settings>(this)

    @Composable
    override fun NextStep(selected: Settings, navigateBack: () -> Unit) {
        Remote(panelGroup, sessionInfo, selected, navigateBack)
    }
}