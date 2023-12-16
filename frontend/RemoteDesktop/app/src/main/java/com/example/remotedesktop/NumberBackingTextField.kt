package com.example.remotedesktop

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T: Number> NumberBackingTextField(value: T, setter: (T) -> Unit, label: String) {
    val keyboardType = when (value::class) {
        Int::class -> { KeyboardType.Number }
        Float::class -> { KeyboardType.Decimal }
        else -> throw RuntimeException("Bad input type")
    }
    OutlinedTextField(
        value = value.toString(),
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        onValueChange = {
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
