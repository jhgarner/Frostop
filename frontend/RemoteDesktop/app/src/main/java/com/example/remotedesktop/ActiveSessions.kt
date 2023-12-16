package com.example.remotedesktop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview

data class ActiveSessionSelector(
    val connectionStateRaw: MutableState<ConnectionState>,
    val activeSessions: List<SessionInfo>,
) : Selector<SessionInfo, SessionSelector> {
    private var connectionState: ConnectionState by connectionStateRaw

    override val getList = activeSessions
    override val headingText = "Select an Active Session"

    override fun onNew(): Backable = Backable {
        SessionSelector.ShowSelecter(it)
    }

    override fun SessionInfo.onEdit() = null
    override fun SessionInfo.onRemove(i: Int) {
        connectionState = ConnectionState.Removing(this)
    }

    @Composable
    override fun NextStep(selected: SessionInfo, navigateBack: () -> Unit) {
        PanelGroupSelector(
            sessionInfo = selected,
        ).ShowSelecter(navigateBack)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewActiveSessions() {
    ActiveSessionSelector(
        connectionStateRaw = remember { mutableStateOf(ConnectionState.NoConnection) },
        activeSessions = listOf(),
    ).ShowSelecter {}
}