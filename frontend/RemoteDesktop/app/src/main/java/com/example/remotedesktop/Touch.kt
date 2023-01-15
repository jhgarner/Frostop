package com.example.remotedesktop

import androidx.compose.ui.input.pointer.PointerInputChange
import kotlin.math.abs
import kotlin.math.roundToInt

sealed interface Touch {
    val x: Int
    val y: Int

    fun processMotion(event: PointerInputChange): Touch

    fun inside(panel: Panel, modes: Modes): Boolean {
        val inBounds = panel.x <= x && panel.y <= y && panel.x+panel.w >= x && panel.y+panel.h >= y
        return panel.showWhen.all(modes::inMode) && inBounds
    }
}

class Still(override val x: Int, override val y: Int): Touch {
    override fun processMotion(event: PointerInputChange): Touch {
        val newX = event.position.x.roundToInt()
        val newY = event.position.y.roundToInt()
        if (overThreshold(x, newX) || overThreshold(y, newY)) {
            return Moving(event, newX, newY)
        }

        return this
    }

    private fun overThreshold(a: Int, newA: Int): Boolean {
        return abs(a - newA) >= 10
    }
}

class Moving(val event: PointerInputChange, override val x: Int, override val y: Int): Touch {
    override fun processMotion(event: PointerInputChange): Touch {
        val newX = event.position.x.roundToInt()
        val newY = event.position.y.roundToInt()
        return Moving(event, newX, newY)
    }
}