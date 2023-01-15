package com.example.remotedesktop

import android.graphics.Color.parseColor
import android.util.DisplayMetrics
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable

@Serializable
class Panel(
    val x: Int,
    val y: Int,
    val w: Int,
    val h: Int,
    private val text: String,
    private val unpressed: String,
    private val pressed: String,
    private val actions: Array<Action>,
    val showWhen: Array<String>
) {
    @Composable
    fun Create(modes: Modes, dm: DisplayMetrics) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        val active  = showWhen.all(modes::inMode)
        val color = if (active) {
            parseColor(if (isPressed) pressed else unpressed)
        } else {
            0
        }

        fun Int.toDp(): Dp {
            return (this / (dm.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).dp
        }

        Box(
            modifier = Modifier
                .padding(start = x.toDp(), top = y.toDp())
                .size(width = w.toDp(), height = h.toDp())
                .background(Color(color))
                .clickable(interactionSource = interactionSource, indication = null) { },
        ) {
            if (active) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = text
                )
            }
        }
    }

    fun handleTouch(touch: Touch?, modes: Modes, server: StableServer) {
        for (action in actions) {
            action.handleMotion(touch, modes, server)
        }
    }
}