package com.example.remotedesktop

import android.graphics.Color.parseColor
import android.util.DisplayMetrics
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Panel(
    private var xRaw: Mut<Int> = mutOf(10),
    private var yRaw: Mut<Int> = mutOf(10),
    private var wRaw: Mut<Int> = mutOf(10),
    private var hRaw: Mut<Int> = mutOf(10),
    private var textRaw: Mut<String> = mutOf("Panel"),
    private var nameRaw: Mut<String> = mutOf("Panel Name"),
    private var unpressedRaw: Mut<String> = mutOf("#30888888"),
    private var pressedRaw: Mut<String> = mutOf("#50888888"),
    var actions: MutList<Action> = mutList(),
    var showWhenRaw: Mut<List<String>> = mutOf(listOf()),
) {
    var x: Int by xRaw
    var y: Int by yRaw
    var w: Int by wRaw
    var h: Int by hRaw
    var text: String by textRaw
    var name: String by nameRaw
    var unpressed: String by unpressedRaw
    var pressed: String by pressedRaw
    var showWhen: List<String> by showWhenRaw

    @Transient
    val extraCompose: MutableState<KeyboardHandler> = mutableStateOf(KeyboardHandler())

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun Create(modes: Modes, dm: DisplayMetrics) {
        extraCompose.value.Fire()
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        val active = showWhen.all(modes::inMode)
        val color = if (active) {
            parseColor(if (isPressed) pressed else unpressed)
        } else {
            0
        }

        fun Int.toDp(): Dp {
            return (this / (dm.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).dp
        }

        Box(
            modifier = Modifier
                .padding(start = x.toDp(), top = y.toDp())
                .size(width = w.toDp(), height = h.toDp())
                .background(Color(color))
                .clickable(interactionSource = interactionSource, indication = null) { },
        ) {
            if (active) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = text
                )
            }
        }
    }

    fun handleTouch(touch: Touch?, modes: Modes, server: StableServer.Streaming) {
        for (action in actions) {
            action.handleMotion(
                touch,
                modes,
                server
            ) { extraCompose.component2()(KeyboardHandler()) }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
    @Composable
    fun Configure() {
        var expanded by remember { mutableStateOf(false) }
        OutlinedCard(
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp)
                    .fillMaxWidth(),
            ) {
                Text(
                    name,
                    style = MaterialTheme.typography.headlineMedium,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )

                if (expanded) {
                    FlowRow(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = name,
                            label = { Text(text = "name") },
                            onValueChange = { name = it }
                        )
                        NumberBackingTextField(
                            value = x,
                            label = "x",
                            setter = { x = it }
                        )
                        NumberBackingTextField(
                            value = y,
                            label = "y",
                            setter = { y = it }
                        )
                        NumberBackingTextField(
                            value = w,
                            label = "w",
                            setter = { w = it }
                        )
                        NumberBackingTextField(
                            value = h,
                            label = "h",
                            setter = { h = it }
                        )
                        OutlinedTextField(
                            value = text,
                            label = { Text(text = "text") },
                            onValueChange = { text = it }
                        )
                        OutlinedTextField(
                            value = unpressed,
                            label = { Text(text = "unpressed") },
                            onValueChange = { unpressed = it }
                        )
                        OutlinedTextField(
                            value = pressed,
                            label = { Text(text = "pressed") },
                            onValueChange = { pressed = it }
                        )

                        var showWhenEditing = showWhen.joinToString(" ")
                        OutlinedTextField(
                            value = showWhenEditing,
                            label = { Text(text = "Show When") },
                            onValueChange = {
                                showWhenEditing = it
                                showWhen = it.split(" ")
                            }
                        )

                        actions.NewAction()
                    }
                }

                Column(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Divider()
                    TextButton(
                        onClick = { expanded = !expanded }
                    ) {
                        Text(if (expanded) "Collapse" else "Expand")
                    }
                }
            }
        }
    }
}

//@Preview
//@Composable
//private fun PreviewPanelConfig() {
//    val panel = Panel()
//    panel.actions.add(Action.Trackpad)
//    panel.actions.add(Action.Trackpad)
//    panel.actions.add(Action.Trackpad)
//    panel.actions.add(Action.Trackpad)
//    panel.actions.add(Action.Trackpad)
//    panel.Configure()
//}
//
//@Preview(device = "spec:parent=pixel_5,orientation=landscape")
//@Composable
//private fun PreviewPanelConfigTablet() {
//    val panel = Panel()
//    panel.actions.add(Action.Trackpad)
//    panel.actions.add(Action.Trackpad)
//    panel.actions.add(Action.Trackpad)
//    panel.actions.add(Action.Trackpad)
//    panel.actions.add(Action.Trackpad)
//    panel.Configure()
//}
