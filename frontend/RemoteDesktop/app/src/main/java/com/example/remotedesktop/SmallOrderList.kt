package com.example.remotedesktop

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MutList<Action>.SmallOrderList() {
    var selectedItem: Int? by rememberSaveable { mutableStateOf(null) }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Spacer(modifier = Modifier.width(8.dp))
        }
        for ((index, item) in withIndex()) {
            item {
                InputChip(
                    selected = index == selectedItem,
                    onClick = { selectedItem = index },
                    label = {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    },
                    leadingIcon = {
                        if (index == selectedItem && index != 0) {
                            IconButton(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .height(18.dp)
                                    .width(18.dp),
                                onClick = {
                                    move(index, index - 1)
                                    selectedItem = selectedItem!! - 1
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Move back"
                                )
                            }
                        }
                    },
                    trailingIcon = {
                        if (index != selectedItem) {
                            IconButton(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .height(18.dp)
                                    .width(18.dp),
                                onClick = {
                                    removeAt(index)
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete"
                                )
                            }
                        } else if (index < size - 1) {
                            IconButton(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .height(18.dp)
                                    .width(18.dp),
                                onClick = {
                                    move(index, index + 1)
                                    selectedItem = selectedItem!! + 1
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Move forward"
                                )
                            }
                        }
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewList() {
    val actions: MutList<Action> = mutList()
    actions.add(Action.Trackpad)
    actions.add(Action.Trackpad)
    actions.add(Action.Trackpad)
    actions.add(Action.Trackpad)
    actions.SmallOrderList()
}