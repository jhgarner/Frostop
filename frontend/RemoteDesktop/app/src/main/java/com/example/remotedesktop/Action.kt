package com.example.remotedesktop

import android.view.KeyCharacterMap
import android.view.KeyEvent
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Serializable
sealed interface Action {
    fun handleMotion(touch: Touch?, modes: Modes, server: StableServer)

    @Serializable
    @SerialName("Trackpad")
    class Trackpad : Action {
        @kotlinx.serialization.Transient
        private val velocityTracker = VelocityTracker()
        override fun handleMotion(touch: Touch?, modes: Modes, server: StableServer) {
            when (touch) {
                is Moving -> {
                    velocityTracker.addPointerInputChange(touch.event)
                    val vel = velocityTracker.calculateVelocity()
                    val speed = sqrt(vel.x.pow(2) + vel.y.pow(2))
                    val factor = 0.05 * speed.pow(0.6f)
                    val xLoc = (factor * (touch.x - touch.event.previousPosition.x)).roundToInt()
                    val yLoc = (factor * (touch.y - touch.event.previousPosition.y)).roundToInt()
                    server.sendRelLoc(xLoc, yLoc)
                }
                else -> velocityTracker.resetTracking()
            }
        }
    }

    @Serializable
    @SerialName("Button")
    class Button(private val button: Byte): Action {
        override fun handleMotion(touch: Touch?, modes: Modes, server: StableServer) {
            when (touch) {
                null -> server.sendRelease(button)
                else -> server.sendClick(button)
            }
        }
    }

    @Serializable
    @SerialName("Key")
    class Key(private val keys: Array<String>): Action {
        override fun handleMotion(touch: Touch?, modes: Modes, server: StableServer) {
            val keyCodes = keys.map {
                KeyCoder.toCode(when(it) {
                    "Ctrl" -> KeyEvent.KEYCODE_CTRL_LEFT
                    "Alt" -> KeyEvent.KEYCODE_ALT_LEFT
                    "Win" -> KeyEvent.KEYCODE_ALT_RIGHT
                    else -> {
                        KeyCharacterMap.load(KeyCharacterMap.FULL).getEvents(charArrayOf(it[0]))[0].keyCode
                    }
                })
            }
            if (touch == null) {
                keyCodes.forEach {
                    server.sendKeyPress(it.toByte())
                }
                keyCodes.forEach {
                    server.sendKeyRelease(it.toByte())
                }
            }
        }
    }

    @Serializable
    @SerialName("TmpMode")
    class TmpMode(private val mode: String): Action {
        override fun handleMotion(touch: Touch?, modes: Modes, server: StableServer) {
            when (touch) {
                null -> modes.removeMode(mode)
                else -> modes.addMode(mode)
            }
        }
    }
}



