package com.example.remotedesktop

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.*
import kotlin.math.abs
import kotlin.math.roundToInt

data class StylusMenu(
    private val openState: MutableState<Boolean>,
    private val selectedState: MutableState<Selected>
) {
    var open by openState
    var selected by selectedState

    enum class Selected(val code: Byte) {
        LEFT(1.toByte()),
        MIDDLE(2.toByte()),
        RIGHT(3.toByte()),
    }
}

class MouseHandler(private val server: StableServer, private val menu: StylusMenu) {
    var lastX: Int = -1
    var lastY: Int = -1
    var pressed: Pair<Int, Int>? = null

    fun handle(event: PointerInputChange, buttons: PointerButtons) {
        if (buttons.isPrimaryPressed) {
            menu.open = true
        }

        val x = event.position.x.roundToInt()
        val y = event.position.y.roundToInt()
        val movedPressed = pressed?.let { (pressedX, pressedY) ->
            abs(pressedX - x) > 10 || abs(pressedY - y) > 10
        }
        if (lastX != x || lastY != y && movedPressed != false) {
            lastX = x
            lastY = y
            server.sendLoc(x, y)
        }

        if (event.changedToDownIgnoreConsumed()) {
            pressed = x to y
            server.sendClick(menu.selected.code)
        } else if (event.changedToUpIgnoreConsumed()) {
            pressed = null
            server.sendRelease(menu.selected.code)
        }
    }
}