package com.example.openapitest.ui.viewmodel

import com.example.openapitest.AppContainer
import com.example.openapitest.CodexApplication
import com.example.openapitest.data.local.CollectionWithItems
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

data class CollectionUiModel(
    val id: Long,
    val name: String,
    val emoji: String,
    val itemCount: Int,
    val isPinned: Boolean,
    val isSystem: Boolean = false
)

fun CollectionWithItems.toUiModel(): CollectionUiModel = CollectionUiModel(
    id = collection.id,
    name = collection.name,
    emoji = collection.emoji,
    itemCount = items.size,
    isPinned = collection.isPinned,
    isSystem = collection.isSystem
)

@Composable
fun rememberAppContainer(): AppContainer {
    val app = LocalContext.current.applicationContext as? CodexApplication
        ?: error("Application must be CodexApplication")
    return app.container
}
