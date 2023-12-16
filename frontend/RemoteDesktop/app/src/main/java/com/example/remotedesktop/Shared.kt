package com.example.remotedesktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ControlInput {
    @Serializable
    @SerialName("Cursor")
    data class Cursor(val x: Int, val y: Int, val relative: Boolean) :
        ControlInput
    @Serializable
    @SerialName("MouseButton")
    data class MouseButton(val detail: Int, val event: Int) : ControlInput
    @Serializable
    @SerialName("Key")
    data class Key(val detail: Int, val event: Int) : ControlInput
}

@Serializable
sealed interface Params {
    @Serializable
    @SerialName("Connect")
    data class Connect(val video_params: VideoParams, val session_info: SessionInfo) : Params
    @Serializable
    @SerialName("Stop")
    data class Stop(val session_info: SessionInfo) : Params
    @Serializable
    @SerialName("Query")
    object Query : Params
}

@Serializable
data class VideoParams(
    val bitrate: Int,
    val fps: Int,
    val width: Int,
    val height: Int
)

@Serializable
@Stable
data class SessionInfo(val id: String, val session: Session): Selectable {
    // This is a bad preview but it'll work for now
    override fun preview() = id
}

@Serializable
data class Entry(
    @SerialName("key")
    val keyRaw: Mut<String> = mutOf("key"),
    @SerialName("value")
    val valueRaw: Mut<String> = mutOf("value"),
) {
    var key : String by keyRaw
    var value : String by valueRaw
}
