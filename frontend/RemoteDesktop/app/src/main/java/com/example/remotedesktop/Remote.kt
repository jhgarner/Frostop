package com.example.remotedesktop

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.core.view.WindowCompat
import dev.ahmedmourad.bundlizer.unbundle
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


class Remote : ComponentActivity() {

    @SuppressLint("ClickableViewAccessibility", "InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val panelsJson = intent.getStringExtra("panels") ?: "[]"
        val panels = Json.decodeFromString<Array<Panel>>(panelsJson)
//        val panels = arrayOf(
//            Panel(0, 0, 1400, 1752, "#2200FF00", "#22FF0000", arrayOf(Action.TmpMode("left"),
//                Action.Trackpad()
//            ), arrayOf("!right")),
//            Panel(1400, 0, 1400, 1752, "#00000000", "#00000000", arrayOf(Action.TmpMode("right"),
//                Action.Trackpad()
//            ), arrayOf("!left")),
//            Panel(2600, 0, 200, 1752, "#30888888", "#50888888", arrayOf(Action.Button(1)), arrayOf("left")),
//            Panel(2390, 0, 200, 1752, "#30888888", "#50888888",  arrayOf(Action.Button(2)), arrayOf("left")),
//            Panel(2180, 0, 200, 1752, "#30888888", "#50888888", arrayOf(Action.Button(3)), arrayOf("left")),
//        )
        val dm = resources.displayMetrics
        val channel = Channel<PointerOrKeyEvent>(Channel.Factory.UNLIMITED)
        val settings = intent.extras!!.unbundle(ServerSettings.serializer())
        val desktop = Desktop(settings, channel, panels)
        setContent {
            val modes = Modes(remember { mutableStateOf(emptySet()) })
            val requester = remember { FocusRequester() }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(requester)
                    .focusable()
                    .onKeyEvent {
                        runBlocking {
                            channel.send(PointerOrKeyEvent.Key(it))
                        }
                        true
                    }
                    .pointerInput(modes) {
                        try {
                            awaitPointerEventScope {
                                while (true) {
                                    val pointerEvents = awaitPointerEvent(PointerEventPass.Main)
                                    for (event in pointerEvents.changes) {
                                        runBlocking {
                                            channel.send(
                                                PointerOrKeyEvent.Pointer(
                                                    event,
                                                    pointerEvents.buttons
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        } catch (ex: RuntimeException) {
                            println(ex)
                        }
                    },
            ) {
                desktop.Create(modes = modes)
                for (panel in panels) {
                    panel.Create(modes = modes, dm = dm)
                }
            }
            LaunchedEffect(Unit) {
                requester.requestFocus()
            }
        }
        hideSystemBars()
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            window.insetsController?.apply {
                hide(WindowInsets.Type.statusBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }
}
