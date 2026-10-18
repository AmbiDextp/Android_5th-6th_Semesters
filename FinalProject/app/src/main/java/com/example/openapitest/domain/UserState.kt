package com.example.openapitest.domain

enum class FruitStatus(val label: String) {
    NONE("Без статуса"),
    WANT("Хочу"),
    STUDIED("Изучен"),
    FAVOURITE("Избранное");

    companion object {
        fun fromName(name: String?): FruitStatus =
            entries.firstOrNull { it.name == name } ?: NONE
    }
}

data class FruitUserState(
    val fruitId: Int,
    val status: FruitStatus = FruitStatus.NONE,
    val isHidden: Boolean = false,
    val isPinned: Boolean = false
) {
    val isDefault: Boolean
        get() = status == FruitStatus.NONE && !isHidden && !isPinned
}

enum class HistoryType(val label: String) {
    VIEWED("Просмотр"),
    COLLECTION_ADD("Добавлен в коллекцию"),
    COLLECTION_REMOVE("Удалён из коллекции"),
    NOTE_SAVED("Сохранена заметка"),
    STATUS_SET("Изменён статус");

    companion object {
        fun fromName(name: String?): HistoryType =
            entries.firstOrNull { it.name == name } ?: VIEWED
    }
}
