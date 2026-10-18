package com.example.openapitest.ui.screen.collections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.openapitest.ui.viewmodel.CollectionsViewModel
import com.example.openapitest.ui.viewmodel.rememberAppContainer
import com.example.openapitest.ui.widget.ConfirmDialog
import com.example.openapitest.ui.widget.EmptyState
import com.example.openapitest.ui.widget.NameInputDialog

private val COLLECTION_EMOJIS = listOf(
    "\uD83C\uDF4D",
    "\u2694\uFE0F",
    "\uD83C\uDFF4\u200D\u2620\uFE0F",
    "\uD83D\uDC32",
    "\uD83C\uDF72",
    "\uD83D\uDC52",
    "\uD83E\uDDED",
    "\u26F5"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    onCollectionClick: (Long) -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: CollectionsViewModel = viewModel(factory = viewModelFactory {
        initializer {
            CollectionsViewModel(container.userdataRepository)
        }
    })
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<Long?>(null) }
    var deleteTarget by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Коллекции") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Новая коллекция") }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) { CircularProgressIndicator() }
            }

            state.collections.isEmpty() -> {
                EmptyState(
                    icon = Icons.Filled.CollectionsBookmark,
                    title = "Коллекций пока нет",
                    subtitle = "Создайте первую коллекцию и собирайте плоды по своему вкусу",
                    modifier = Modifier.padding(padding)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.collections, key = { it.id }) { collection ->
                        CollectionRow(
                            name = collection.name,
                            emoji = collection.emoji,
                            itemCount = collection.itemCount,
                            isPinned = collection.isPinned,
                            isSystem = collection.isSystem,
                            onClick = { onCollectionClick(collection.id) },
                            onRename = { renameTarget = collection.id },
                            onTogglePinned = { viewModel.togglePinned(collection.id, !collection.isPinned) },
                            onDelete = { deleteTarget = collection.id }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateCollectionDialog(
            onConfirm = { name, emoji ->
                viewModel.createCollection(name, emoji)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    renameTarget?.let { id ->
        val current = state.collections.firstOrNull { it.id == id }
        NameInputDialog(
            title = "Переименовать коллекцию",
            placeholder = "Название",
            initialValue = current?.name.orEmpty(),
            onConfirm = { name ->
                viewModel.renameCollection(id, name)
                renameTarget = null
            },
            onDismiss = { renameTarget = null }
        )
    }

    deleteTarget?.let { id ->
        val current = state.collections.firstOrNull { it.id == id }
        ConfirmDialog(
            title = "Удалить коллекцию?",
            text = "«${current?.name.orEmpty()}» будет удалена вместе со списком её плодов. Сами плоды останутся в каталоге.",
            onConfirm = { viewModel.deleteCollection(id) },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
private fun CollectionRow(
    name: String,
    emoji: String,
    itemCount: Int,
    isPinned: Boolean,
    isSystem: Boolean,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onTogglePinned: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isPinned) {
                    Icon(
                        Icons.Filled.PushPin,
                        contentDescription = "Закреплена",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.size(4.dp))
                }
                Text(name, fontWeight = FontWeight.Bold)
            }
        },
        supportingContent = {
            Text("Плодов: $itemCount" + if (isSystem) " · системная" else if (isPinned) " · закреплена" else "")
        },
        leadingContent = { Text(emoji, style = MaterialTheme.typography.headlineMedium) },
        trailingContent = {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Меню")
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                if (!isSystem) {
                    DropdownMenuItem(
                        text = { Text("Переименовать") },
                        leadingIcon = { Icon(Icons.Filled.DriveFileRenameOutline, contentDescription = null) },
                        onClick = { menuExpanded = false; onRename() }
                    )
                }
                DropdownMenuItem(
                    text = { Text(if (isPinned) "Открепить" else "Закрепить") },
                    leadingIcon = { Icon(Icons.Filled.PushPin, contentDescription = null) },
                    onClick = { menuExpanded = false; onTogglePinned() }
                )
                if (!isSystem) {
                    DropdownMenuItem(
                        text = { Text("Удалить", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = { menuExpanded = false; onDelete() }
                    )
                }
            }
        }
    )
}

@Composable
private fun CreateCollectionDialog(
    onConfirm: (name: String, emoji: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf(COLLECTION_EMOJIS.first()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая коллекция") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Text("Иконка", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    COLLECTION_EMOJIS.forEach { candidate ->
                        FilterChip(
                            selected = emoji == candidate,
                            onClick = { emoji = candidate },
                            label = { Text(candidate) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), emoji) },
                enabled = name.isNotBlank()
            ) { Text("Создать") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
