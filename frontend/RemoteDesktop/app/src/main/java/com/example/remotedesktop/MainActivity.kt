package com.example.remotedesktop

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dev.ahmedmourad.bundlizer.bundle
import net.edwardday.serialization.preferences.Preferences
import net.edwardday.serialization.preferences.decodeOrDefault
import net.edwardday.serialization.preferences.encode


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val self = this

        val prefs = Preferences(this.getPreferences(MODE_PRIVATE))
        val current = prefs
            .decodeOrDefault(SETTINGS, ServerSettings("", 60, 1.0f, 1.0f))
        val activePanel = prefs.decodeOrDefault(ACTIVE_PANEL, "default")
        val initialPanels = prefs
            .decodeOrDefault(PANELS, mapOf("default" to "[]"))

        setContent {
            var settings by remember { mutableStateOf(current) }
            var panels by remember { mutableStateOf(initialPanels) }
            var selectedPanel by remember { mutableStateOf(activePanel) }
            var choosingPanel by remember { mutableStateOf(false) }
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = !isSystemInDarkTheme()
            val ctx = LocalContext.current
            val color = if (isSystemInDarkTheme()) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    val data = it.data!!
                    if (!data.getBooleanExtra("new", false)) {
                        panels = panels - selectedPanel
                    }
                    if (!data.getBooleanExtra("deleted", false)) {
                        val name = data.getStringExtra("name")!!
                        val json = data.getStringExtra("json")!!
                        panels = panels + (name to json)
                        selectedPanel = name
                    }
                    if (panels.isEmpty()) {
                        panels = initialPanels
                        selectedPanel = activePanel
                    }
                    if (selectedPanel !in panels) {
                        selectedPanel = panels.keys.first()
                    }
                }
            }

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
                        Column(
                            modifier = Modifier.width(IntrinsicSize.Max),
                            verticalArrangement = Arrangement.spacedBy(
                                16.dp,
                                BiasAlignment.Vertical(-0.25f)
                            ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            TextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = settings.host,
                                label = { Text("Host") },
                                onValueChange = { settings = settings.copy(host = it) },
                            )
                            NumberBackingTextField(
                                value = settings.fps,
                                keyboardType = KeyboardType.Number,
                                label = "Framerate",
                                setter = { settings = settings.copy(fps = it) },
                            )
                            NumberBackingTextField(
                                value = settings.bitrate_mb,
                                keyboardType = KeyboardType.Decimal,
                                label = "Biterate",
                                setter = { settings = settings.copy(bitrate_mb = it) },
                            )
                            NumberBackingTextField(
                                value = settings.scale,
                                keyboardType = KeyboardType.Decimal,
                                label = "Scale",
                                setter = { settings = settings.copy(scale = it) },
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box {
                                    TextField(
                                        value = selectedPanel,
                                        modifier = Modifier.clickable { choosingPanel = true },
                                        label = { Text("Panels") },
                                        readOnly = true,
                                        enabled = false,
                                        colors = TextFieldDefaults.textFieldColors(
                                            disabledTextColor = color.onSurface,
                                            disabledPlaceholderColor = color.onSurfaceVariant,
                                            //disabledBorderColor = color.outline,
                                            disabledLabelColor = color.onSurfaceVariant,
                                            disabledLeadingIconColor = color.onSurfaceVariant,
                                            disabledTrailingIconColor = color.onSurfaceVariant),
                                        trailingIcon = {
                                            if (choosingPanel) {
                                                Icon(Icons.Filled.ArrowDropUp, "Drop up arrow")
                                            } else {
                                                Icon(Icons.Filled.ArrowDropDown, "Drop down arrow")
                                            }
                                        },
                                        onValueChange = { }
                                    )
                                    DropdownMenu(
                                        expanded = choosingPanel,
                                        onDismissRequest = { choosingPanel = false }
                                    ) {
                                        for (entry in panels) {
                                            DropdownMenuItem(
                                                text = { Text(entry.key) },
                                                onClick = { selectedPanel = entry.key; choosingPanel = false }
                                            )
                                        }

                                    }
                                }
                                IconButton(
                                    modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                                    onClick = {
                                        prefs.encode(SETTINGS, settings)
                                        val intent = Intent(self, MacroActivity::class.java)
                                            .putExtra("json", panels[selectedPanel])
                                            .putExtra("name", selectedPanel)
                                            .putExtra("new", false)
                                        launcher.launch(intent)
                                    },
                                ) {
                                    Icon(Icons.Filled.Edit, "edit")
                                }
                                IconButton(
                                    onClick = {
                                        prefs.encode(SETTINGS, settings)
                                        val intent = Intent(self, MacroActivity::class.java)
                                            .putExtra("json", "[]")
                                            .putExtra("name", "Panel Name")
                                            .putExtra("new", true)
                                        launcher.launch(intent)
                                    },
                                ) {
                                    Icon(Icons.Filled.Add, "edit")
                                }
                            }
                            Button(
                                onClick = {
                                    prefs.encode(SETTINGS, settings)
                                    prefs.encode(PANELS, panels)
                                    prefs.encode(ACTIVE_PANEL, selectedPanel)
                                    val intent = Intent(self, Remote::class.java)
                                        .putExtras(settings.bundle(ServerSettings.serializer()))
                                        .putExtra("panels", panels[selectedPanel])
                                    startActivity(intent)
                                },
                            ) {
                                Text("Connect")
                            }
                        }
                    }
                }
            }
        }
    }
    companion object {
        const val SETTINGS = "serverSettings"
        const val PANELS = "panels"
        const val ACTIVE_PANEL = "activePanel"
    }
}

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T: Number> NumberBackingTextField(value: T, setter: (T) -> Unit, label: String, keyboardType: KeyboardType) {
    var backing by rememberSaveable { mutableStateOf(value.toString()) }

    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = backing,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        onValueChange = {
            backing = it
            val t =
                if (value::class == Int::class) {
                    it.toIntOrNull() ?: 0
                } else if (value::class == Float::class) {
                    it.toFloatOrNull() ?: 0f
                } else {
                    throw RuntimeException("Bad type")
                }
            setter(t as T)
        },
    )
}