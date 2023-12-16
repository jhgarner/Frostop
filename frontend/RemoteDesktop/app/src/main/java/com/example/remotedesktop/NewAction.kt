package com.example.remotedesktop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MutList<Action>.NewAction() {
    OutlinedCard {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val actions = listOf("Trackpad", "Button", "Key", "Mode")
            var selectedAction: Int? by remember { mutableStateOf(null) }
            var addAction: (() -> Unit)? by remember { mutableStateOf(null) }
            Column(
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    for ((i, action) in actions.withIndex()) {
                        if (i != 0) {
                            Divider(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(1.dp)
                            )
                        }
                        val (color, textColor) = if (i == selectedAction) {
                            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            Color.Transparent to MaterialTheme.colorScheme.onSurface
                        }
                        Box(
                            modifier = Modifier
                                .weight(1.0f)
                                .background(color = color)
                                .clickable {
                                    selectedAction = i
                                    addAction = null
                                },
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .align(Alignment.Center),
                                text = action,
                                color = textColor,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
                Divider()
            }
            when (selectedAction) {
                null -> {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = "Select an action type",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }

                0 -> {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = "Nothing else to configure",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    addAction = { add(Action.Trackpad) }
                }

                1 -> {
                    val buttons = listOf("Left", "Middle", "Right")
                    var selectedButton: Int? by remember { mutableStateOf(null) }
                    SegmentedButton(
                        items = buttons,
                        onClick = {
                            addAction = {
                                when (it) {
                                    0 -> add(Action.Button(1))
                                    1 -> add(Action.Button(2))
                                    2 -> add(Action.Button(3))
                                    else -> Unit
                                }
                            }
                        }
                    )
                }

                2 -> {
                    var key: String by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = key,
                        onValueChange = { key = it },
                        label = { Text(text = "Key") },
                    )
                    addAction = if (key != "") {
                        { add(Action.Key(key)) }
                    } else {
                        null
                    }
                }

                3 -> {
                    var mode: String by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = mode,
                        onValueChange = { mode = it },
                        label = { Text(text = "Mode") },
                    )
                    addAction = if (mode != "") {
                        { add(Action.TmpMode(mode)) }
                    } else {
                        null
                    }
                }
            }

            Button(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 8.dp),
                enabled = addAction != null,
                onClick = { addAction!!() },
            ) {
                Text(text = "Add")
            }

            SmallOrderList()
        }
    }
}

@Composable
@Preview
fun PreviewNewAction() {
    val actions: MutList<Action> = mutList()
    actions.add(Action.Trackpad)
    actions.add(Action.Trackpad)
    actions.add(Action.Trackpad)
    actions.add(Action.Trackpad)
    actions.NewAction()
}
