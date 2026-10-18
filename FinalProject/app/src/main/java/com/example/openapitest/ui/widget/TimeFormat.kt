package com.example.openapitest.ui.widget

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun formatTimeOfDay(timestamp: Long): String =
    Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalTime()
        .format(DateTimeFormatter.ofPattern("HH:mm"))

fun formatDayTitle(timestamp: Long): String {
    val today = LocalDate.now()
    val date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
    return when (date) {
        today -> "Сегодня"
        today.minusDays(1) -> "Вчера"
        else -> date.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
    }
}

fun formatRelativeTime(
    timestamp: Long?,
    nowMillis: Long = System.currentTimeMillis()
): String {
    if (timestamp == null) return "никогда"
    val diff = nowMillis - timestamp
    val minutes = diff / 60_000
    val hours = diff / 3_600_000
    val days = diff / 86_400_000
    return when {
        minutes < 1 -> "только что"
        minutes < 60 -> "$minutes мин назад"
        hours < 24 -> "$hours ч назад"
        else -> "$days дн назад"
    }
}
