package com.example.remotedesktop

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.remotedesktop.App.Companion.host
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.net.ConnectException
import java.net.NoRouteToHostException


@Serializable
sealed interface ConnectionState {
    @Serializable
    data object NoConnection : ConnectionState

    @Serializable
    data object Loading : ConnectionState

    @Serializable
    data class Loaded(val sessions: List<SessionInfo>) : ConnectionState

    @Serializable
    data class Removing(val session: SessionInfo) : ConnectionState
}

class MainActivity : ComponentActivity() {
    override fun onStop() {
        super.onStop()
        App.save(this)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App.Load()

            val connectionStateRaw: MutableState<ConnectionState> =
                remember { mutableStateOf(ConnectionState.Loading) }

            val systemUiController = rememberSystemUiController()
            val useDarkIcons = !isSystemInDarkTheme()
            val ctx = LocalContext.current
            val color =
                if (isSystemInDarkTheme()) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(
                    ctx
                )

            MaterialTheme(colorScheme = color) {
                DisposableEffect(systemUiController, useDarkIcons) {
                    systemUiController.setSystemBarsColor(
                        color = color.surface,
                        darkIcons = useDarkIcons
                    )
                    onDispose {}
                }

                Scaffold { padding ->
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(
                            16.dp,
                            BiasAlignment.Vertical(-0.25f)
                        ),
                    ) {
                        when (val connectionState = connectionStateRaw.value) {
                            ConnectionState.Loading -> {
                                LaunchedEffect(host) {
                                    launch(Dispatchers.IO) {
                                        connectionStateRaw.value = try {
                                            ConnectionState.Loaded(StableServer.query())
                                        } catch (ex: ConnectException) {
                                            ConnectionState.NoConnection
                                        } catch (ex: NoRouteToHostException) {
                                            ConnectionState.NoConnection
                                        }
                                    }
                                }

                                CircularProgressIndicator()
                            }

                            ConnectionState.NoConnection -> {
                                OutlinedTextField(
                                    value = host,
                                    label = { Text("Host") },
                                    onValueChange = { host = it },
                                )
                                Button(onClick = {
                                    connectionStateRaw.value = ConnectionState.Loading
                                }) {
                                    Text("Connect")
                                }
                            }

                            is ConnectionState.Loaded -> {
                                val activeSessions = connectionState.sessions
                                ActiveSessionSelector(
                                    connectionStateRaw = connectionStateRaw,
                                    activeSessions = activeSessions,
                                ).ShowSelecter {
                                    connectionStateRaw.value = ConnectionState.NoConnection
                                }
                            }

                            is ConnectionState.Removing -> {
                                val toRemove = connectionState.session
                                LaunchedEffect(toRemove) {
                                    launch(Dispatchers.IO) {
                                        connectionStateRaw.value = try {
                                            StableServer.stop(toRemove);
                                            ConnectionState.Loading
                                        } catch (ex: ConnectException) {
                                            ConnectionState.NoConnection
                                        }
                                    }
                                }
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}
