package com.example.remotedesktop

import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.remotedesktop.LifecycleObserver.observeAsState

object LifecycleObserver {
    enum class SystemState {
        Running,
        Paused,
        ;

        companion object {
            fun fromEvent(event: Lifecycle.Event): SystemState {
                return when (event) {
                    Lifecycle.Event.ON_CREATE, Lifecycle.Event.ON_START, Lifecycle.Event.ON_RESUME -> Running
                    else -> Paused
                }
            }
        }
    }
    @Composable
    fun Lifecycle.observeAsState(): State<SystemState> {
        val state = remember { mutableStateOf(SystemState.Running) }
        DisposableEffect(this) {
            val observer = LifecycleEventObserver { _, event ->
                state.value = SystemState.fromEvent(event)
            }
            this@observeAsState.addObserver(observer)
            onDispose {
                this@observeAsState.removeObserver(observer)
            }
        }
        return state
    }

    @Composable
    fun Lifecycle.onPause(f: () -> Unit) {
        DisposableEffect(this) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_PAUSE) {
                    f()
                }
            }
            this@onPause.addObserver(observer)
            onDispose {
                this@onPause.removeObserver(observer)
            }
        }
    }
}