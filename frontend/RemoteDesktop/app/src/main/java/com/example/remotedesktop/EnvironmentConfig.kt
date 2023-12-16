package com.example.remotedesktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MutList<Entry>.EnvironmentConfig() {
    OutlinedCard {

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
            Text("Environment Variables", style = MaterialTheme.typography.headlineMedium)
            if (isEmpty()) {
                Text(
                    text = "No environment variables added yet",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            for ((i, entry) in withIndex()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        enabled = i < size - 1,
                        onClick = { move(i, i + 1) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Move Down"
                        )
                    }
                    IconButton(enabled = i > 0, onClick = { move(i, i - 1) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Move Up"
                        )
                    }
                    OutlinedTextField(
                        modifier = Modifier.weight(1.0f),
                        value = entry.key,
                        singleLine = true,
                        label = { Text("Key") },
                        onValueChange = { entry.key = it }
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowRight,
                        contentDescription = "Is equal to"
                    )
                    OutlinedTextField(
                        modifier = Modifier.weight(1.0f),
                        value = entry.value,
                        singleLine = true,
                        label = { Text("Value") },
                        onValueChange = { entry.value = it }
                    )
                    IconButton(onClick = { removeAt(i) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete"
                        )
                    }
                }
            }
            Button(
                modifier = Modifier
                    .padding(bottom = 16.dp, end = 16.dp)
                    .align(Alignment.End),
                onClick = { add(Entry()) }
            ) {
                Text("Add Variable")
            }
        }
    }
}
