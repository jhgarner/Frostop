package com.example.remotedesktop

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@Serializable
data class App(
    private val hostRaw: Mut<String> = mutOf(""),
    val panelGroups: MutList<PanelGroup> = mutList(),
    val sessions: MutList<Session> = mutList(),
    val videoParams: MutList<Settings> = mutList(),
) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app")
        private val key = stringPreferencesKey("AppV1")
        private var hasLoaded = false
        val inputChannel = ResetableChannel<PointerOrKeyEvent>()
        var app = App()
        var host: String
            get() = app.hostRaw.value
            set(newHost) {
                app.hostRaw.value = newHost
            }
        val panelGroups
            get() = app.panelGroups
        val sessions
            get() = app.sessions
        val videoParams
            get() = app.videoParams

        @Composable
        fun Load() {
            val context = LocalContext.current
            runBlocking {
                context.dataStore.data.first()[key]?.let {
                    try {
                        app = Json.decodeFromString(it)
                    } catch (_: IllegalArgumentException) {
                    }
                }
            }
            hasLoaded = true
        }

        @Composable
        fun Save() {
            save(LocalContext.current)
        }

        fun save(activity: Activity) {
            save(activity.baseContext)
        }

        private fun save(context: Context) {
            if (hasLoaded) {
                runBlocking {
                    context.dataStore.edit {
                        it[key] = Json.encodeToString(app)
                    }
                }
            }
        }
    }
}
