package com.example.remotedesktop

import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp

@Composable
fun SegmentedButton(
    items: List<String>,
    onClick: (Int) -> Unit
) {
    Row {
        var selected: Int? by rememberSaveable { mutableStateOf(null) }
        for ((index, item) in items.withIndex()) {
            val color = if (index == selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            val colors = ButtonDefaults.outlinedButtonColors(containerColor = color)
            val shape = when (index) {
                0 -> { RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp) }
                items.size-1 -> { RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp) }
                else -> { RoundedCornerShape(0.dp) }
            }

            OutlinedButton(
                modifier = Modifier.offset((-3*index).dp, 0.dp),
                colors = colors,
                shape = shape,
                onClick = { onClick(index); selected = index },
            ) {
                Text(text = item)
            }
        }
    }
}

@Composable
@Preview(wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE)
fun PreviewSegmentedButton() {
    SegmentedButton(items = listOf("Enabled", "Enabled", "Enabled", "Enabled")) {}
}