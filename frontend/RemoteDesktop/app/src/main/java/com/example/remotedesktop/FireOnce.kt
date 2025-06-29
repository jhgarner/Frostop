package com.example.remotedesktop

import android.view.KeyCharacterMap
import android.view.KeyEvent.KEYCODE_DEL
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.text.input.CommitTextCommand
import androidx.compose.ui.text.input.DeleteSurroundingTextCommand
import androidx.compose.ui.text.input.EditCommand
import androidx.compose.ui.text.input.ImeOptions
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TextInputSession
import com.example.remotedesktop.KeyCoder.click
import com.example.remotedesktop.KeyCoder.shifted

class KeyboardHandler {
    var inputSession: TextInputSession? by mutableStateOf(null)

    fun isKeyboardVisible(): Boolean = inputSession?.isOpen ?: false

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun Fire() {
        val inputService = LocalTextInputService.current!!
        val keyboardController = LocalSoftwareKeyboardController.current!!
        LaunchedEffect(this) {
            inputSession = inputService.startInput(
                TextFieldValue(),
                ImeOptions.Default,
                this@KeyboardHandler::inputHandler
            ) {}
            keyboardController.show()
        }
    }

    private fun inputHandler(commands: List<EditCommand>) {
        for (command in commands) {
            when (command) {
                is CommitTextCommand -> {
                    KeyCharacterMap.load(KeyCharacterMap.FULL)
                        .getEvents(command.text.toCharArray())?.toList()
                        ?: listOf()
                }

                is DeleteSurroundingTextCommand -> {
                    (1..command.lengthBeforeCursor).flatMap {
                        KEYCODE_DEL.click().shifted()
                    }
                }

                else -> listOf()
            }.map(::KeyEvent).map(PointerOrKeyEvent::Key)
                .forEach(App.inputChannel::send)
        }
    }
}