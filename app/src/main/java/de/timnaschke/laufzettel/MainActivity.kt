package de.timnaschke.laufzettel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

data class Item(val name: String, val description: String = "desc")

class ItemViewModel : ViewModel() {
    private var _order = mutableSetOf(
        "b",
        "a",
        "c",
        "d",
        "e",
        "x"
    )
    private val _items = MutableStateFlow(listOf(
        Item("a"),
        Item("b"),
        Item("c"),
        Item("x")
    ).sortedBy { item ->
        _order.indexOf(item.name)
    })
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    fun add(newItem: Item) {
        if (! _order.contains(newItem.name)) {
            _order.add(newItem.name)
        }

        _items.update { itemList ->
            itemList.toMutableList().apply {
                add(newItem)
            }.sortedBy { item ->
                _order.indexOf(item.name)
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
        _order = _order.toMutableList().apply{
            val orderFrom = indexOf(_items.value[from].name)
            val orderTo = indexOf(_items.value[to].name)
            add(orderTo, removeAt(orderFrom))
        }.toMutableSet()

        _items.update { itemList -> itemList.sortedBy { item -> _order.indexOf(item.name) } }
    }
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            LaufzettelTheme {
                val itemViewModel: ItemViewModel = viewModel()
                var showBottomSheet by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        FloatingActionButton(
                            //onClick = {itemViewModel.add(Item(Random.nextInt().toString()))}
                            onClick = { showBottomSheet = true }
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add")
                        }
                    }
                ) { innerPadding ->
                    ReorderableList(itemViewModel, Modifier.padding(innerPadding))

                    if (showBottomSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showBottomSheet = false }
                        ) {
                            ItemSearch()
                        }
                    }
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
        items(items, key = { item -> item.name }) { item ->
            ReorderableItem(reorderableLazyListState, key = item.name) { isDragging ->
                val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

                Surface(
                    shape = shapes.small,
                    color = colorScheme.primary,
                    shadowElevation = elevation,
                    onClick = { itemViewModel.remove(item) }
                ) {
                    Row {
                        Column(Modifier.weight(1f)) {
                            Text(item.name, Modifier.padding(horizontal = 8.dp))
                            Text(item.description, Modifier.padding(horizontal = 8.dp))
                        }
                        IconButton(
                            modifier = Modifier.draggableHandle(),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemSearch() {
    Box {
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = "Query",
                    onQueryChange = {},
                    onSearch = {},
                    expanded = true,
                    onExpandedChange = {}
                )
            },
            expanded = true,
            onExpandedChange = {}
        ) {}
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