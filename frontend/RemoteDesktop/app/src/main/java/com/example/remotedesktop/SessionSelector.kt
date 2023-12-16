package com.example.remotedesktop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.example.remotedesktop.App.Companion.sessions
import java.util.UUID

data object SessionSelector : EditableSelector<Session> {
    override fun default() = Session()
    override fun String.deserialize(): Session? = decodeSafely<Session>(this)

    override val getList = sessions
    override val headingText = "Select a Session Template"

    @Composable
    override fun NextStep(selected: Session, navigateBack: () -> Unit) {
        val id = remember(selected) { UUID.randomUUID().toString() }
        SessionInfo(id, selected)
        PanelGroupSelector(
            sessionInfo = SessionInfo(id, selected),
        ).ShowSelecter(navigateBack)
    }
}

@Preview(showBackground = true)
@Composable
fun SessionConfigPreview() {
    val envs: MutList<Entry> = mutList()
//    envs.add(Entry())
//    envs.add(Entry())
//    envs.add(Entry())
    val session =
        Session(desktopRaw = mutOf(listOf("run", "this", "", "command")), envs = envs)
    session.RawEdit {}
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
fun SessionConfigPreviewTablet() {
    val envs: MutList<Entry> = mutList()
    envs.add(Entry())
    envs.add(Entry())
    envs.add(Entry())
    val session =
        Session(desktopRaw = mutOf(listOf("run", "this", "", "command")), envs = envs)
    session.RawEdit {}
}
