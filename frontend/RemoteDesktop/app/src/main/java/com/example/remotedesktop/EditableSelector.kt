package com.example.remotedesktop

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.Json

data class Backable(val action: @Composable ((() -> Unit) -> Unit))

interface EditableSelector<T : Editable> : Selector<T, T> {
    fun default(): T
    fun String.deserialize(): T?

    override val getList: MutableList<T>

    @Composable
    override fun RowScope.Actions() {
        LocalClipboardManager.current.getText()?.text?.deserialize()?.let {
            Icon(
                modifier = Modifier
                    .clickable { getList.add(it) }
                    .padding(16.dp),
                imageVector = Icons.Default.ContentPaste,
                contentDescription = "Paste")
        }
    }

    override fun onNew(): Backable {
        val newItem = default()
        getList.add(newItem)
        return Backable { newItem.RawEdit(it) }
    }

    override fun T.onEdit(): Backable? = Backable { RawEdit(it) }
    override fun T.onRemove(i: Int) {
        getList.removeAt(i)
    }
}

inline fun <reified T> decodeSafely(json: String): T? {
    return try {
        Json.decodeFromString<T>(json)
    } catch (ex: IllegalArgumentException) {
        null
    }
}

interface Selector<Item : Selectable, New> {
    sealed interface PageState<in Item> {
        data class Editing<Item>(val item: Backable) : PageState<Item>
        data class Selected<Item>(val item: Item) : PageState<Item>
        data object None : PageState<Any?>
    }

    val getList: List<Item>
    val headingText: String

    @Composable
    fun RowScope.Actions() {
    }

    @Composable
    fun NextStep(selected: Item, navigateBack: () -> Unit)

    fun onNew(): Backable
    fun Item.onEdit(): Backable?
    fun Item.onRemove(i: Int)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ShowSelecter(
        navigateBack: () -> Unit,
    ) {
        BackHandler(enabled = true) {
            navigateBack()
        }

        var pageState: PageState<Item> by remember { mutableStateOf(PageState.None) }

        AnimatedContent(targetState = pageState, label = "EditableSelector", transitionSpec = {
            if (targetState == PageState.None) {
                slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
            } else {
                slideInVertically { it } togetherWith slideOutVertically { -it }
            }
        }) {
            val back = { pageState = PageState.None }
            when (it) {
                is PageState.Selected -> NextStep(it.item, back)
                is PageState.Editing -> it.item.action(back)
                is PageState.None -> {
                    App.Save()
                    Column {
                        TopAppBar(
                            title = { Text(text = headingText) },
                            navigationIcon = {
                                Icon(
                                    modifier = Modifier
                                        .clickable { navigateBack() }
                                        .padding(16.dp),
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back")
                            },
                            actions = { Actions() }
                        )
                        Box {
                            Column(
                                Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                if (getList.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1.0f)
                                            .fillMaxWidth(),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            text = "Nothing was found"
                                        )
                                    }
                                }
                                for ((i, t) in getList.withIndex()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            style = MaterialTheme.typography.labelLarge,
                                            modifier = Modifier
                                                .clickable { pageState = PageState.Selected(t) }
                                                .weight(1.0f)
                                                .padding(horizontal = 16.dp, vertical = 8.dp),
                                            text = t.preview()
                                        )
                                        t.onEdit()?.also { editAction ->
                                            IconButton(onClick = {
                                                pageState = PageState.Editing(editAction)
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit"
                                                )
                                            }
                                        }
                                        IconButton(onClick = { t.onRemove(i) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Edit"
                                            )
                                        }
                                    }
                                }
                            }

                            ExtendedFloatingActionButton(
                                modifier = Modifier
                                    .padding(bottom = 16.dp, end = 16.dp)
                                    .align(Alignment.BottomEnd),
                                text = { Text(text = "New") },
                                icon = { Icon(imageVector = Icons.Outlined.Add, "New") },
                                onClick = { pageState = PageState.Editing(onNew()) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewEmptySelector() {
    object : EditableSelector<Nothing> {
        override fun default(): Nothing {
            TODO("Not yet implemented")
        }

        override fun String.deserialize(): Nothing? {
            TODO("Not yet implemented")
        }

        override val getList = mutableListOf<Nothing>()
        override val headingText = "test"

        @Composable
        override fun NextStep(selected: Nothing, navigateBack: () -> Unit) {
        }
    }.ShowSelecter {}
}

@Preview(showBackground = true)
@Composable
private fun PreviewNonemptySelector() {
    object : EditableSelector<PanelGroup> {
        override fun default() = PanelGroup()
        override fun String.deserialize(): PanelGroup? = null

        override val getList = mutableListOf(PanelGroup(), PanelGroup())
        override val headingText = "test"

        @Composable
        override fun NextStep(selected: PanelGroup, navigateBack: () -> Unit) {
        }
    }.ShowSelecter {}
}
