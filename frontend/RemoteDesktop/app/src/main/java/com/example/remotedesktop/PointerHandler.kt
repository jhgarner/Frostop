package com.example.remotedesktop

import androidx.compose.ui.input.pointer.PointerType

class PointerHandler(private val fingerHandler: FingerHandler, private val mouseHandler: MouseHandler) {
    fun handlePointer(pointer: PointerOrKeyEvent.Pointer) {
        when(pointer.p.type) {
            PointerType.Touch -> fingerHandler.handleFinger(pointer.p)
            PointerType.Stylus -> mouseHandler.handle(pointer.p, pointer.b)
        }
    }
}