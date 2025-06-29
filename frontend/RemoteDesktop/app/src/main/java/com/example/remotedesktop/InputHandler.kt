package com.example.remotedesktop

import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.type
import com.example.remotedesktop.App.Companion.inputChannel

class InputHandler(
    private val pointerHandler: PointerHandler,
    private val server: StableServer.Streaming,
) {
    suspend fun runInputHandler(): Result<Unit> {
        return runCatching {
            inputChannel.reset()
            for (event in inputChannel) {
                when (event) {
                    is PointerOrKeyEvent.Pointer -> pointerHandler.handlePointer(event)
                    is PointerOrKeyEvent.Key -> {
                        val agnosticCode =
                            KeyCoder.toCode(event.k.key.nativeKeyCode)
                        if (agnosticCode != -1) {
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
    }
}