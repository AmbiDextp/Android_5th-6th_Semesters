package com.example.openapitest.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import com.example.openapitest.data.settings.ThemeMode
import com.example.openapitest.data.settings.TtlOption
import com.example.openapitest.ui.viewmodel.SettingsViewModel
import com.example.openapitest.ui.viewmodel.rememberAppContainer
import com.example.openapitest.ui.widget.ConfirmDialog
import com.example.openapitest.ui.widget.formatRelativeTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val container = rememberAppContainer()
    val viewModel: SettingsViewModel = viewModel(factory = viewModelFactory {
        initializer {
            SettingsViewModel(
                settingsRepository = container.settingsRepository,
                catalogRepository = container.catalogRepository,
                userdataRepository = container.userdataRepository,
                historyRepository = container.historyRepository,
                ttlPolicy = container.ttlPolicy
            )
        }
    })
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.message.collect { message ->
            if (message != null) {
                snackbarHostState.showSnackbar(message)
                viewModel.consumeMessage()
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Настройки") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            SectionTitle("Внешний вид")
            Text(
                "Тема применяется сразу и сохраняется между запусками",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            listOf(
                ThemeMode.SYSTEM to "Системная",
                ThemeMode.LIGHT to "Светлая",
                ThemeMode.DARK to "Тёмная"
            ).forEach { (mode, label) ->
                RadioRow(
                    title = label,
                    selected = state.settings.theme == mode,
                    onClick = { viewModel.setTheme(mode) }
                )
            }

            Spacer(Modifier.height(24.dp))
            SectionTitle("Актуальность данных")
            Text(
                "Как быстро кэш каталога считается устаревшим. " +
                        "Устаревший кэш обновляется при открытии приложения " +
                        "и помечается баннером в каталоге.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            TtlOption.entries.forEach { option ->
                RadioRow(
                    title = option.label,
                    subtitle = option.description,
                    selected = state.settings.ttl == option,
                    onClick = { viewModel.setTtl(option) }
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.padding(4.dp))
                Column(Modifier.weight(1f)) {
                    Text("Фоновое обновление")
                    Text(
                        "Обновляет кэш каталога каждые 6 часов при наличии сети (WorkManager)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.settings.autoSync,
                    onCheckedChange = { viewModel.setAutoSync(it) }
                )
            }

            Spacer(Modifier.height(24.dp))
            SectionTitle("Локальные данные")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    StatRow("Плодов в кэше", "${state.cachedFruits}")
                    StatRow(
                        "Последнее обновление",
                        formatRelativeTime(state.lastSyncedAt) +
                                if (state.isStale) " · устарел" else ""
                    )
                    HorizontalDivider()
                    StatRow("Коллекций", "${state.stats.collectionsCount}")
                    StatRow("Заметок", "${state.stats.notesCount}")
                    StatRow("Пресетов фильтров", "${state.stats.presetsCount}")
                    StatRow("Событий истории", "${state.stats.historyCount}")
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = { viewModel.clearUnreferencedCache() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text("Удалить неиспользуемый кэш каталога")
            }
            Text(
                "Удаляются только плоды, которых нет ни в коллекциях, заметках, статусах и истории",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { showClearHistoryDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text("Очистить историю")
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "Devil Fruit Codex · данные: api-onepiece.com",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }

    if (showClearHistoryDialog) {
        ConfirmDialog(
            title = "Очистить историю?",
            text = "Записи об истории действий будут удалены безвозвратно.",
            confirmLabel = "Очистить",
            onConfirm = { viewModel.clearHistory() },
            onDismiss = { showClearHistoryDialog = false }
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
private fun RadioRow(
    title: String,
    subtitle: String? = null,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column {
            Text(title)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Bold)
    }
}
