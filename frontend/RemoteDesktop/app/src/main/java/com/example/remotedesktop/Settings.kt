package com.example.remotedesktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Settings(
    private val nameRaw: Mut<String> = mutOf("Video Settings"),
    private val fpsRaw: Mut<Int> = mutOf(60),
    private val bitrateMbRaw: Mut<Float> = mutOf(1.0f),
    private val scaleRaw: Mut<Float> = mutOf(1.0f),
) : Editable {
    var name: String by nameRaw
    var fps: Int by fpsRaw
    var bitrateMb: Float by bitrateMbRaw
    var scale: Float by scaleRaw

    override val editHeadingText = "Edit Video Settings"

    override fun serialize(): String = Json.encodeToString(this)
    override fun preview() = name

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun BoxScope.Edit() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .width(IntrinsicSize.Max),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                16.dp,
                BiasAlignment.Vertical(-0.25f)
            ),
        ) {
            OutlinedTextField(
                value = name,
                label = { Text("Name") },
                singleLine = true,
                onValueChange = { name = it },
            )
            NumberBackingTextField(
                value = fps,
                label = "Framerate",
                setter = { fps = it },
            )
            NumberBackingTextField(
                value = bitrateMb,
                label = "Biterate",
                setter = { bitrateMb = it },
            )
            NumberBackingTextField(
                value = scale,
                label = "Scale",
                setter = { scale = it },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsTest() {
    Settings().RawEdit {}
}

