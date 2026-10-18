package com.example.openapitest.ui.screen.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.openapitest.domain.HistoryType
import com.example.openapitest.ui.viewmodel.HistoryViewModel
import com.example.openapitest.ui.viewmodel.rememberAppContainer
import com.example.openapitest.ui.widget.ConfirmDialog
import com.example.openapitest.ui.widget.EmptyState
import com.example.openapitest.ui.widget.formatTimeOfDay

private fun historyIcon(type: HistoryType): ImageVector = when (type) {
    HistoryType.VIEWED -> Icons.Filled.Visibility
    HistoryType.COLLECTION_ADD -> Icons.Filled.AddCircle
    HistoryType.COLLECTION_REMOVE -> Icons.Filled.RemoveCircle
    HistoryType.NOTE_SAVED -> Icons.Filled.Edit
    HistoryType.STATUS_SET -> Icons.Filled.Star
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onFruitClick: (Int) -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: HistoryViewModel = viewModel(factory = viewModelFactory {
        initializer {
            HistoryViewModel(container.historyRepository)
        }
    })
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История") },
                actions = {
                    if (!state.isEmpty) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Очистить историю",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
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

            state.isEmpty -> {
                EmptyState(
                    icon = Icons.Filled.History,
                    title = "История пуста",
                    subtitle = "Просмотры плодов и действия появятся здесь",
                    modifier = Modifier.padding(padding)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    state.groups.forEach { group ->
                        item(key = "header_${group.title}") {
                            Text(
                                group.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(
                                    start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp
                                )
                            )
                        }
                        items(group.items, key = { it.id }) { item ->
                            ListItem(
                                leadingContent = {
                                    Icon(
                                        historyIcon(item.type),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                },
                                headlineContent = {
                                    Text(
                                        item.fruitName ?: "Плод #${item.fruitId}",
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                supportingContent = { Text(item.type.label) },
                                trailingContent = {
                                    Text(
                                        formatTimeOfDay(item.timestamp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onFruitClick(item.fruitId) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        ConfirmDialog(
            title = "Очистить историю?",
            text = "Все записи об истории действий будут удалены. Коллекции, заметки и статусы не затрагиваются.",
            confirmLabel = "Очистить",
            onConfirm = { viewModel.clearHistory() },
            onDismiss = { showClearDialog = false }
        )
    }
}
