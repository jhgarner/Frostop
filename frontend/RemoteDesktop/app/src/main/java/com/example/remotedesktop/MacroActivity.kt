package com.example.remotedesktop

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MacroActivity: ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val new = intent.getBooleanExtra("new", false)
        val result = Intent()
        result.putExtra("new", new)

        setContent {
            var json by remember { mutableStateOf(intent.getStringExtra("json") ?: "") }
            var name by remember { mutableStateOf(intent.getStringExtra("name") ?: "") }
            var editingName: String? by remember { mutableStateOf(null) }
            val scroll = rememberScrollState(0)
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = !isSystemInDarkTheme()
            val ctx = LocalContext.current
            val color = if (isSystemInDarkTheme()) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)

            MaterialTheme(colorScheme = color) {
                DisposableEffect(systemUiController, useDarkIcons) {
                    systemUiController.setSystemBarsColor(
                        color = color.surface,
                        darkIcons = useDarkIcons
                    )
                    onDispose {}
                }

                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                if (editingName != null) {
                                    TextField(
                                        modifier = Modifier.padding(bottom = 8.dp),
                                        value = editingName!!,
                                        singleLine = true,
                                        label = { Text("Name") },
                                        onValueChange = { editingName = it },
                                    )
                                } else {
                                    Text(name)
                                }
                            },
                            actions = {
                                if (editingName == null) {
                                    IconButton(onClick = { editingName = name }) {
                                        Icon(Icons.Filled.Edit, "Edit name")
                                    }
                                    if (!new) {
                                        IconButton(onClick = {
                                            result.putExtra("delete", true)
                                            setResult(Activity.RESULT_OK, result)
                                            finish()
                                        }) {
                                            Icon(Icons.Filled.Delete, "Delete entry")
                                        }
                                    }
                                } else {
                                    IconButton(onClick = { name = editingName!!; editingName = null }) {
                                        Icon(Icons.Filled.Save, "Save name edit")
                                    }
                                    IconButton(onClick = { editingName = null }) {
                                        Icon(Icons.Filled.Cancel, "Cancel name edit")
                                    }
                                }
                            }
                        )
                    },
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(
                            16.dp,
                            BiasAlignment.Vertical(-0.25f)
                        ),
                    ) {
                        Spacer(Modifier.size(16.dp))
                        TextField(
                            value = name,
                            singleLine = true,
                            label = { Text("Name") },
                            onValueChange = { name = it },
                        )
                        TextField(
                            // This behavior is pretty bad for a code editor, but I'm not sure how to make it better....
                            modifier = Modifier
                                .horizontalScroll(scroll)
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(start = 16.dp, end = 16.dp),
                            value = json,
                            singleLine = false,
                            label = { Text("Panel JSON") },
                            onValueChange = { json = it},
                        )
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = {
                                    result.putExtra("name", name)
                                    result.putExtra("json", json)
                                    setResult(Activity.RESULT_OK, result)
                                    finish()
                                },
                            ) {
                                Icon(Icons.Filled.Save, "Save")
                                Spacer(Modifier.size(8.dp))
                                Text("Save")
                            }
                        }
                    }
                }
            }
        }
    }
}