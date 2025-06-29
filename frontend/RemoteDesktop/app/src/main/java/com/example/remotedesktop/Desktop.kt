package com.example.remotedesktop

import android.util.Log
import android.view.SurfaceView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.example.remotedesktop.LifecycleObserver.observeAsState
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class Desktop(
    private val settings: ServerSettings,
    private val sessionInfo: SessionInfo,
    private val panels: List<Panel>
) {
    @Composable
    fun Create(modes: Modes) {
        var view: SurfaceView? by remember { mutableStateOf(null, referentialEqualityPolicy()) }
        val lifeState by LocalLifecycleOwner.current.lifecycle.observeAsState()
        val stylusMenu = remember {
            StylusMenu(mutableStateOf(false), mutableStateOf(StylusMenu.Selected.LEFT))
        }

        DropdownMenu(
            expanded = stylusMenu.open,
            onDismissRequest = { stylusMenu.open = false }
        ) {
            for (button in StylusMenu.Selected.entries) {
                DropdownMenuItem(
                    text = { Text(button.name) },
                    onClick = {
                        stylusMenu.selected = button
                        stylusMenu.open = false
                    }
                )
            }
        }

        AndroidView(
            modifier = Modifier
                .fillMaxSize(),
            factory = {
                view = SurfaceView(it)
                view!!
            },
        )

        RelaunchableIO(view to lifeState) { (view, lifeState) ->
            if (lifeState == LifecycleObserver.SystemState.Running) {
                if (view != null) {
                    val server =
                        StableServer.connect(settings, view.width, view.height, sessionInfo)
                    val fingerHandler = FingerHandler(panels, modes, server)
                    val mouseHandler = MouseHandler(server, stylusMenu)
                    val pointerHandler = PointerHandler(fingerHandler, mouseHandler)
                    val inputHandler = InputHandler(pointerHandler, server)
                    val streamer = launch {
                        HevcReceiver(settings.scale, server, view).runReceiver().logOnError()
                    }
                    val inputter = launch {
                        inputHandler.runInputHandler().logOnError()
                    }
                    joinAll(streamer, inputter)
                }
            }
        }
    }

    private fun <T> Result<T>.logOnError() {
        recoverCatching {
            Log.e("Frostop", it.message, it)
        }
    }
}