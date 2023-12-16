package com.example.remotedesktop

import androidx.compose.ui.input.pointer.PointerInputChange
import kotlin.math.roundToInt

class FingerHandler(private val panels: List<Panel>, private val modes: Modes, private val server: StableServer.Streaming) {
    private val touches = HashMap<Long, Pair<Touch, Set<Panel>>>()

    fun handleFinger(event: PointerInputChange) {
        when(Pair(event.previousPressed, event.pressed)) {
            Pair(false, true) -> {
                handleFingerDown(event)
            }
            Pair(true, false) -> {
                handleFingerUp(event)
            }
            else -> {
                handleFingerMoved(event)
            }
        }
    }

    private fun handleFingerMoved(event: PointerInputChange) {
        val id = event.id.value
        touches[id]?.let { (oldTouch, panelsTouched) ->
            val newTouch = oldTouch.processMotion(event)
            if (newTouch != oldTouch) {
                for (panel in panelsTouched) {
                    panel.handleTouch(newTouch, modes, server)
                }
                touches[id] = Pair(newTouch, panelsTouched)
            }
        }
    }

    private fun handleFingerDown(event: PointerInputChange) {
        val id = event.id.value
        val x = event.position.x.roundToInt()
        val y = event.position.y.roundToInt()
        val touch = Still(x, y)
        val panelsTouched = panels.filter { touch.inside(it, modes) }.toSet()
        touches[id] = Pair(touch, panelsTouched)
        for (panel in panelsTouched) {
            panel.handleTouch(touch, modes, server)
        }
    }

    private fun handleFingerUp(event: PointerInputChange) {
        val id = event.id.value
        touches[id]?.let { (_, panelsTouched) ->
            for (panel in panelsTouched) {
                panel.handleTouch(null, modes, server)
            }
            touches.remove(id)
        }
    }
}