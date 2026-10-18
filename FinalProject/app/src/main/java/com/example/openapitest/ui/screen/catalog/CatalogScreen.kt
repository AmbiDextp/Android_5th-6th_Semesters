package com.example.openapitest.ui.screen.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.compose.AsyncImage
import com.example.openapitest.domain.CatalogStatusFilter
import com.example.openapitest.domain.FruitStatus
import com.example.openapitest.model.Fruit
import com.example.openapitest.ui.viewmodel.CatalogViewModel
import com.example.openapitest.ui.viewmodel.rememberAppContainer
import com.example.openapitest.ui.widget.EmptyState
import com.example.openapitest.ui.widget.FruitCard
import com.example.openapitest.ui.widget.InfoBanner
import com.example.openapitest.ui.widget.NameInputDialog
import com.example.openapitest.ui.widget.formatRelativeTime
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onFruitClick: (Int) -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: CatalogViewModel = viewModel(factory = viewModelFactory {
        initializer {
            CatalogViewModel(
                catalogRepository = container.catalogRepository,
                userdataRepository = container.userdataRepository,
                historyRepository = container.historyRepository,
                settingsRepository = container.settingsRepository,
                ttlPolicy = container.ttlPolicy
            )
        }
    })
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var hintShown by rememberSaveable { mutableStateOf(false) }

    if (!hintShown) {
        hintShown = true
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar(
                "Долгое нажатие на карточку скрывает плод из каталога"
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Devil Fruit Codex")
                        Text(
                            text = "Обновлено: ${formatRelativeTime(state.lastSyncedAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = !state.isRefreshing
                    ) {
                        if (state.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Filled.Refresh,
                                contentDescription = "Обновить каталог"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Поиск по названию или типу…") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Очистить")
                        }
                    }
                }
            )

            Spacer(Modifier.height(8.dp))

            if (state.offline) {
                InfoBanner(
                    icon = Icons.Filled.CloudOff,
                    text = "Нет сети — показаны сохранённые данные"
                )
                Spacer(Modifier.height(8.dp))
            }

            if (state.isStale && !state.offline && state.totalFruits > 0) {
                InfoBanner(
                    icon = Icons.Filled.Schedule,
                    text = "Данные могли устареть (порог из настроек)",
                    actionLabel = "Обновить",
                    onAction = { viewModel.refresh() }
                )
                Spacer(Modifier.height(8.dp))
            }

            if (state.recent.isNotEmpty() && state.filter.isDefault && state.query.isBlank()) {
                Text(
                    "Недавние",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(state.recent, key = { it.id }) { fruit ->
                        RecentFruitCard(fruit = fruit, onClick = { onFruitClick(fruit.id) })
                    }
                }
            }

            if (state.savedFilters.isNotEmpty() ||
                !state.filter.isDefault || state.query.isNotBlank()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(state.savedFilters, key = { it.id }) { preset ->
                            val selected = state.filter == preset.filter &&
                                    state.query == preset.query
                            InputChip(
                                selected = selected,
                                onClick = { viewModel.applyPreset(preset) },
                                label = { Text(preset.name) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Удалить пресет",
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { viewModel.deletePreset(preset.id) }
                                    )
                                }
                            )
                        }
                    }
                    IconButton(onClick = { showSavePresetDialog = true }) {
                        Icon(
                            Icons.Filled.BookmarkAdd,
                            contentDescription = "Сохранить текущий фильтр как пресет"
                        )
                    }
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(CatalogStatusFilter.entries.toList()) { status ->
                    FilterChip(
                        selected = state.filter.status == status,
                        onClick = { viewModel.onStatusFilterChange(status) },
                        label = { Text(status.label) }
                    )
                }
            }

            if (state.availableTypes.size > 1) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    items(state.availableTypes) { type ->
                        FilterChip(
                            selected = type in state.filter.selectedTypes,
                            onClick = { viewModel.onToggleType(type) },
                            label = { Text(type) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            val errorMessage = state.errorMessage
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(16.dp))
                            Text("Загружаем каталог…")
                        }
                    }
                }

                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { viewModel.refresh(initial = true) }) {
                                Text("Повторить")
                            }
                        }
                    }
                }

                state.fruits.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Filled.SearchOff,
                        title = if (state.totalFruits == 0) "Каталог пуст" else "Ничего не найдено",
                        subtitle = if (state.totalFruits == 0) {
                            "Загрузите каталог при наличии сети"
                        } else {
                            "Попробуйте изменить запрос или фильтры"
                        }
                    )
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.fruits, key = { it.id }) { fruit ->
                            val userState = state.userStates[fruit.id]
                            FruitCard(
                                fruit = fruit,
                                status = userState?.status ?: FruitStatus.NONE,
                                isPinned = userState?.isPinned == true,
                                isHidden = userState?.isHidden == true,
                                collectionCount = state.fruitCollectionCounts[fruit.id] ?: 0,
                                onToggleFavourite = { viewModel.toggleFavouriteStatus(fruit.id) },
                                onToggleHidden = {
                                    viewModel.toggleHidden(fruit.id)
                                    scope.launch {
                                        val hidden = userState?.isHidden != true
                                        snackbarHostState.showSnackbar(
                                            if (hidden) {
                                                "«${fruit.name}» скрыт. Фильтр «Скрытые» покажет его."
                                            } else {
                                                "«${fruit.name}» снова виден в каталоге"
                                            }
                                        )
                                    }
                                },
                                onClick = { onFruitClick(fruit.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSavePresetDialog) {
        NameInputDialog(
            title = "Сохранить пресет фильтра",
            placeholder = "Например: только Logia",
            onConfirm = { name ->
                viewModel.saveCurrentFilterAsPreset(name)
                showSavePresetDialog = false
            },
            onDismiss = { showSavePresetDialog = false }
        )
    }
}

@Composable
private fun RecentFruitCard(fruit: Fruit, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(104.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = fruit.img,
            contentDescription = fruit.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 96.dp, height = 64.dp)
                .clip(MaterialTheme.shapes.small)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = fruit.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
