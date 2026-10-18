package com.example.openapitest.ui.screen.collections

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.openapitest.ui.viewmodel.CollectionDetailViewModel
import com.example.openapitest.ui.viewmodel.rememberAppContainer
import com.example.openapitest.ui.widget.ConfirmDialog
import com.example.openapitest.ui.widget.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    collectionId: Long,
    onBack: () -> Unit,
    onFruitClick: (Int) -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: CollectionDetailViewModel = viewModel(
        key = "collection_detail_$collectionId",
        factory = viewModelFactory {
            initializer {
                CollectionDetailViewModel(
                    collectionId = collectionId,
                    userdataRepository = container.userdataRepository
                )
            }
        }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.notFound) {
        if (state.notFound) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("${state.collection?.emoji.orEmpty()} ${state.collection?.name.orEmpty()}")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.setPinned(!(state.collection?.isPinned == true)) }) {
                        Icon(
                            Icons.Filled.PushPin,
                            contentDescription = "Закрепить",
                            tint = if (state.collection?.isPinned == true) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    if (state.collection?.isSystem != true) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Удалить коллекцию",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            state.collection?.let { collection ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Плодов в коллекции: ${collection.itemCount}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (collection.isPinned) {
                            Spacer(Modifier.height(4.dp))
                            AssistChip(
                                onClick = { viewModel.setPinned(false) },
                                label = { Text("Закреплена — показывается первой") }
                            )
                        }
                    }
                }
            }

            when {
                state.fruits.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Filled.CollectionsBookmark,
                        title = if (state.notFound) "Коллекция не найдена" else "Пустая коллекция",
                        subtitle = if (state.notFound) null else "Добавьте плоды из их детальных экранов"
                    )
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.fruits, key = { it.id }) { fruit ->
                            ListItem(
                                headlineContent = {
                                    Text(fruit.name, fontWeight = FontWeight.Bold)
                                },
                                supportingContent = { Text(fruit.type) },
                                trailingContent = {
                                    IconButton(onClick = { viewModel.removeFruit(fruit.id) }) {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Убрать из коллекции",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Удалить коллекцию?",
            text = "«${state.collection?.name.orEmpty()}» будет удалена. Сами плоды останутся в каталоге.",
            onConfirm = { viewModel.delete() },
            onDismiss = { showDeleteDialog = false }
        )
    }
}
