package com.example.remotedesktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
@Stable
class PanelGroup(
    private val nameRaw: Mut<String> = mutOf("New Panel"),
    val panels: MutList<Panel> = mutList(),
) : Editable {
    var name: String by nameRaw

    override val editHeadingText = "Edit Panel Group"

    override fun serialize(): String = Json.encodeToString(this)

    override fun preview() = name

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun BoxScope.Edit() {
        Column(
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp, top = 8.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = name,
                singleLine = true,
                label = { Text("Name") },
                onValueChange = { name = it },
            )
            for (panel in panels) {
                panel.Configure()
            }
            Spacer(modifier = Modifier.height(64.dp))
        }
        ExtendedFloatingActionButton(
            modifier = Modifier
                .padding(bottom = 16.dp, end = 16.dp)
                .align(Alignment.BottomEnd),
            text = { Text(text = "New Panel") },
            icon = { Icon(imageVector = Icons.Outlined.Add, "Add") },
            onClick = { panels.add(Panel()) }
        )
    }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
private fun PreviewPanelGroupEdit() {
    val panel = Panel()
    panel.actions.add(Action.Trackpad)
    panel.actions.add(Action.Button(10))
    panel.actions.add(Action.Key("k"))
    panel.actions.add(Action.TmpMode("someMode"))
    val panelGroup = PanelGroup()
    panelGroup.panels.add(panel)
    panelGroup.RawEdit {}
}

fun <T> MutableList<T>.move(oldI: Int, newI: Int) {
    val old = this[oldI]
    if (newI < oldI) {
        removeAt(oldI)
        add(newI, old)
    } else {
        removeAt(oldI)
        add(newI, old)
    }
}
