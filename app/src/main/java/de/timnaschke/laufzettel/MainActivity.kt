package de.timnaschke.laufzettel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.timnaschke.laufzettel.ui.theme.LaufzettelTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.random.Random

data class Item(val name: String, val description: String = "desc")

class ItemViewModel : ViewModel() {
    private val _items = MutableStateFlow(listOf(
        Item("a"),
        Item("b"),
        Item("c"),
        Item("x")
    ))
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    fun add(newItem: Item) {
        _items.update { itemList ->
            itemList.toMutableList().apply {
                add(newItem)
            }
        }
    }

    fun remove(toRemoveItem: Item) {
        _items.update { itemList ->
            itemList.toMutableList().apply {
                remove(toRemoveItem)
            }
        }
    }

    fun move(from: Int, to: Int) {
        _items.update { itemList ->
            itemList.toMutableList().apply {
                add(to, removeAt(from))
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            LaufzettelTheme {
                val itemViewModel: ItemViewModel = viewModel()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {itemViewModel.add(Item(Random.nextInt().toString()))}
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add")
                        }
                    }
                ) { innerPadding ->
                    ReorderableList(itemViewModel, Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ReorderableList(itemViewModel: ItemViewModel, modifier: Modifier) {
    val hapticFeedback = LocalHapticFeedback.current

    val items by itemViewModel.items.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        itemViewModel.move(from.index, to.index)
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = lazyListState,
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items, key = { item -> item.name }) {
            ReorderableItem(reorderableLazyListState, key = it.name) { isDragging ->
                val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

                Surface(shadowElevation = elevation) {
                    Row(Modifier.background(colorScheme.primary, shapes.small)) {
                        Column(Modifier.weight(1f)) {
                            Text(it.name, Modifier.padding(horizontal = 8.dp))
                            Text(it.description, Modifier.padding(horizontal = 8.dp))
                        }
                        IconButton(
                            modifier = Modifier.draggableHandle(
                                onDragStarted = {
                                    //hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                },
                                onDragStopped = {
                                    //hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                },
                            ),
                            onClick = {},
                        ) {
                            Icon(Icons.Rounded.DragHandle, contentDescription = "Reorder")
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun Preview() {
    LaufzettelTheme {
        val itemViewModel: ItemViewModel = viewModel()
        ReorderableList(itemViewModel, Modifier)
    }
}