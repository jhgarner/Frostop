package com.example.remotedesktop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun <T> RelaunchableIO(t: T, f: suspend CoroutineScope.(T) -> Unit) {
    LaunchedEffect(t) {
        withContext(Dispatchers.IO) {
            f(t)
        }
    }
}