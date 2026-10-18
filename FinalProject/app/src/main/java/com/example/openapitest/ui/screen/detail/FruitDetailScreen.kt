package com.example.openapitest.ui.screen.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.compose.AsyncImage
import com.example.openapitest.domain.FruitStatus
import com.example.openapitest.ui.viewmodel.FruitDetailViewModel
import com.example.openapitest.ui.viewmodel.rememberAppContainer
import com.example.openapitest.ui.widget.EmptyState
import com.example.openapitest.ui.widget.NameInputDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FruitDetailScreen(
    fruitId: Int,
    onBack: () -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: FruitDetailViewModel = viewModel(
        key = "fruit_detail_$fruitId",
        factory = viewModelFactory {
            initializer {
                FruitDetailViewModel(
                    fruitId = fruitId,
                    catalogRepository = container.catalogRepository,
                    userdataRepository = container.userdataRepository,
                    historyRepository = container.historyRepository
                )
            }
        }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateCollectionDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.fruit?.name ?: "Плод") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
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
                ) {
                    CircularProgressIndicator()
                }
            }

            state.notFound -> {
                EmptyState(
                    icon = Icons.Filled.VisibilityOff,
                    title = "Плод не найден",
                    subtitle = "Его нет в локальном кэше каталога",
                    modifier = Modifier.padding(padding)
                )
            }

            else -> {
                val fruit = state.fruit ?: return@Scaffold
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = fruit.img,
                            contentDescription = fruit.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(140.dp)
                                .clip(MaterialTheme.shapes.medium)
                        )
                        Spacer(Modifier.size(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                fruit.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            AssistChip(onClick = {}, label = { Text(fruit.type) })
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        fruit.description,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(Modifier.height(24.dp))
                    SectionTitle("Статус")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FruitStatus.entries.forEach { status ->
                            FilterChip(
                                selected = state.userState?.status == status,
                                onClick = { viewModel.setStatus(status) },
                                label = { Text(status.label) }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    SectionTitle("Видимость в каталоге")
                    ToggleRow(
                        title = "Закрепить вверху каталога",
                        icon = Icons.Filled.PushPin,
                        checked = state.userState?.isPinned == true,
                        onCheckedChange = viewModel::setPinned
                    )
                    ToggleRow(
                        title = "Скрыть из каталога",
                        icon = Icons.Filled.VisibilityOff,
                        checked = state.userState?.isHidden == true,
                        onCheckedChange = viewModel::setHidden
                    )

                    Spacer(Modifier.height(16.dp))
                    SectionTitle("Личная заметка")
                    OutlinedTextField(
                        value = state.note,
                        onValueChange = viewModel::onNoteChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ваши мысли о плоде…") },
                        minLines = 3
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = viewModel::saveNote,
                        enabled = state.noteDirty
                    ) {
                        Text("Сохранить заметку")
                    }

                    Spacer(Modifier.height(16.dp))
                    SectionTitle("Коллекции")
                    if (state.collections.isEmpty()) {
                        Text(
                            "Пока нет ни одной коллекции",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        state.collections.forEach { collection ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = collection.id in state.collectionIds,
                                    onCheckedChange = { viewModel.toggleInCollection(collection.id) }
                                )
                                Text(
                                    "${collection.emoji} ${collection.name} (${collection.itemCount})",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { showCreateCollectionDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.size(4.dp))
                        Text("Создать коллекцию и добавить")
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }

    if (showCreateCollectionDialog) {
        NameInputDialog(
            title = "Новая коллекция",
            placeholder = "Например: Мои любимцы",
            onConfirm = { name ->
                viewModel.createCollectionAndAdd(name)
                showCreateCollectionDialog = false
            },
            onDismiss = { showCreateCollectionDialog = false }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun ToggleRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.size(8.dp))
        Text(title, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
