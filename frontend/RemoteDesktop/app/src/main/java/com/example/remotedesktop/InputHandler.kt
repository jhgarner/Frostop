package com.example.remotedesktop

import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.type

class InputHandler(private val pointerHandler: PointerHandler, private val server: StableServer) {
    fun handleInput(event: PointerOrKeyEvent) {
        when (event) {
            is PointerOrKeyEvent.Pointer -> pointerHandler.handlePointer(event)
            is PointerOrKeyEvent.Key -> {
                val agnosticCode =
                    KeyCoder.toCode(event.k.key.nativeKeyCode).toByte()
                if (agnosticCode != (-1).toByte()) {
                    when (event.k.type) {
                        KeyEventType.KeyDown -> server.sendKeyPress(
                            agnosticCode
                        )
                        else -> server.sendKeyRelease(agnosticCode)
                    }
                }
            }
        }
    }
}