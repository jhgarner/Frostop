package com.example.remotedesktop

import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.pointer.PointerButtons
import androidx.compose.ui.input.pointer.PointerInputChange

sealed interface PointerOrKeyEvent {
    data class Pointer(val p: PointerInputChange, val b: PointerButtons): PointerOrKeyEvent
    data class Key(val k: KeyEvent): PointerOrKeyEvent
}