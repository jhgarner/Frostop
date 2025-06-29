package com.example.remotedesktop

import android.view.KeyEvent.KEYCODE_BACK
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsControllerCompat
import com.example.remotedesktop.App.Companion.inputChannel
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.runBlocking

@Composable
fun Remote(
    panelGroup: PanelGroup,
    sessionInfo: SessionInfo,
    settings: Settings,
    navigateBack: () -> Unit,
) {
    val systemUiController: SystemUiController = rememberSystemUiController()
    systemUiController.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    systemUiController.isSystemBarsVisible = false

    BackHandler(enabled = true) {
        systemUiController.isSystemBarsVisible = true
        systemUiController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        navigateBack()
    }
    val panels = panelGroup.panels

    val dm = LocalContext.current.resources.displayMetrics
    val desktop = remember {
        Desktop(
            ServerSettings(settings.fps, settings.bitrateMb, settings.scale),
            sessionInfo,
            panels
        )
    }
    val modes = Modes(remember { mutableStateOf(emptySet()) })
    val requester = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(requester)
            .focusable()
            .onKeyEvent {
                if (it.nativeKeyEvent.keyCode == KEYCODE_BACK) {
                    systemUiController.isSystemBarsVisible = true
                    systemUiController.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
                    navigateBack();
                } else {
                    runBlocking {
                        inputChannel.send(PointerOrKeyEvent.Key(it))
                    }
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
                                    inputChannel.send(
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
        requester.captureFocus()
    }
}
