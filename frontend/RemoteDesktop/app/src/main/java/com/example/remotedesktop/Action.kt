package com.example.remotedesktop

import android.view.KeyCharacterMap
import android.view.KeyEvent.KEYCODE_ALT_LEFT
import android.view.KeyEvent.KEYCODE_ALT_RIGHT
import android.view.KeyEvent.KEYCODE_CTRL_LEFT
import android.view.KeyEvent.KEYCODE_DPAD_DOWN
import android.view.KeyEvent.KEYCODE_DPAD_LEFT
import android.view.KeyEvent.KEYCODE_DPAD_RIGHT
import android.view.KeyEvent.KEYCODE_DPAD_UP
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Serializable
sealed interface Action {
    val name: String
    fun handleMotion(
        touch: Touch?,
        modes: Modes,
        server: StableServer.Streaming,
        showKeyboard: () -> Unit
    )

    @Serializable
    @SerialName("Trackpad")
    data object Trackpad : Action {
        override val name: String = "Trackpad"

        @kotlinx.serialization.Transient
        private val velocityTracker = VelocityTracker()
        override fun handleMotion(
            touch: Touch?,
            modes: Modes,
            server: StableServer.Streaming,
            showKeyboard: () -> Unit
        ) {
            when (touch) {
                is Moving -> {
                    velocityTracker.addPointerInputChange(touch.event)
                    val vel = velocityTracker.calculateVelocity()
                    val speed = sqrt(vel.x.pow(2) + vel.y.pow(2))
                    val factor = 0.25 * speed.pow(0.32f)
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
    class Button(private val button: Int) : Action {
        override val name: String = "Button-$button"
        override fun handleMotion(
            touch: Touch?,
            modes: Modes,
            server: StableServer.Streaming,
            showKeyboard: () -> Unit
        ) {
            when (touch) {
                null -> server.sendRelease(button)
                else -> server.sendClick(button)
            }
        }
    }

    @Serializable
    @SerialName("Key")
    class Key(private val key: String) : Action {
        override val name: String = "Key-$key"
        override fun handleMotion(
            touch: Touch?,
            modes: Modes,
            server: StableServer.Streaming,
            showKeyboard: () -> Unit
        ) {
            val keyCodes =
                when (key) {
                    "Ctrl" -> listOf(KEYCODE_CTRL_LEFT)
                    "Alt" -> listOf(KEYCODE_ALT_LEFT)
                    "Win" -> listOf(KEYCODE_ALT_RIGHT)
                    "Up" -> listOf(KEYCODE_DPAD_UP)
                    "Down" -> listOf(KEYCODE_DPAD_DOWN)
                    "Left" -> listOf(KEYCODE_DPAD_LEFT)
                    "Right" -> listOf(KEYCODE_DPAD_RIGHT)
                    else -> {
                        KeyCharacterMap.load(KeyCharacterMap.FULL)
                            .getEvents(key.toCharArray())
                            .map(android.view.KeyEvent::getKeyCode)
                    }
                }.map(KeyCoder::toCode)
            when (touch) {
                is Still ->
                    keyCodes.forEach(server::sendKeyPress)

                null ->
                    keyCodes.forEach(server::sendKeyRelease)

                else -> {}
            }
        }
    }

    @Serializable
    @SerialName("TmpMode")
    class TmpMode(private val mode: String) : Action {
        override val name: String = "Mode-$mode"
        override fun handleMotion(
            touch: Touch?,
            modes: Modes,
            server: StableServer.Streaming,
            showKeyboard: () -> Unit
        ) {
            when (touch) {
                null -> modes.removeMode(mode)
                else -> modes.addMode(mode)
            }
        }
    }

    @Serializable
    @SerialName("Keyboard")
    data object Keyboard : Action {
        override val name: String = "Keyboard"

        @OptIn(ExperimentalComposeUiApi::class)
        override fun handleMotion(
            touch: Touch?,
            modes: Modes,
            server: StableServer.Streaming,
            showKeyboard: () -> Unit
        ) {
            if (touch == null) {
                showKeyboard()
            }
        }
    }
}



