package com.example.remotedesktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
@Stable
data class Session(
    @SerialName("name")
    private val nameRaw: Mut<String> = mutOf("New Session"),
    @SerialName("desktop")
    var desktopRaw: Mut<List<String>> = mutOf(listOf()),
    var envs: MutList<Entry> = mutList(),
) : Editable {
    var name: String by nameRaw
    private var desktop: List<String> by desktopRaw

    override val editHeadingText = "Edit Session"
    override fun serialize(): String = Json.encodeToString(this)
    override fun preview() = name

    @OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun BoxScope.Edit() {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    singleLine = true,
                    label = { Text("Name") },
                    onValueChange = { name = it },
                )
                var desktopRaw by rememberSaveable { mutableStateOf(desktop.joinToString(" ")) }
                OutlinedTextField(
                    value = desktopRaw,
                    singleLine = true,
                    label = { Text("Start Command") },
                    onValueChange = {
                        desktopRaw = it
                        desktop = parseCommand(it)
                    }
                )
            }
            envs.EnvironmentConfig()
        }
    }

    companion object {
        private fun parseCommand(cmd: String): List<String> {
            var inQuote = false
            var inEscape = false

            val result: MutableList<String> = mutableListOf()
            var arg = ""

            for (token in cmd) {
                if (inEscape) {
                    inEscape = false
                    arg += token
                } else if (token == '\\') {
                    inEscape = true
                } else if (inQuote) {
                    if (token == '"') {
                        inQuote = false
                    } else {
                        arg += token
                    }
                } else if (token == '"') {
                    inQuote = true
                } else if (token == ' ') {
                    result.add(arg)
                    arg = ""
                } else {
                    arg += token
                }
            }

            if (arg != "") {
                result.add(arg)
            }

            return result
        }
    }
}
