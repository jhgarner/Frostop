package com.example.remotedesktop

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

interface Selectable {
    fun preview(): String
}

interface Editable : Selectable {
    fun serialize(): String

    @Composable
    fun BoxScope.Edit()

    val editHeadingText: String

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun RawEdit(navigateBack: () -> Unit) {
        val clipboard = LocalClipboardManager.current
        BackHandler(enabled = true) {
            navigateBack()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            TopAppBar(
                navigationIcon = {
                    Icon(
                        modifier = Modifier
                            .clickable { navigateBack() }
                            .padding(16.dp),
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back")
                },
                title = {
                    Text(editHeadingText)
                },
                actions = {
                    Icon(
                        modifier = Modifier
                            .clickable {
                                clipboard.setText(AnnotatedString(serialize()))
                            }
                            .padding(16.dp),
                        imageVector = Icons.Default.CopyAll,
                        contentDescription = "CopyItem")
                }
            )
            Box {
                Edit()
            }
        }
    }

}