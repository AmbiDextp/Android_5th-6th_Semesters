package com.example.openapitest.ui.viewmodel

import com.example.openapitest.domain.HistoryType
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryGroupingTest {

    private val zone = ZoneId.of("UTC")
    private val today = LocalDate.of(2026, 10, 18)

    private fun ts(month: Int, day: Int, hour: Int): Long =
        ZonedDateTime.of(2026, month, day, hour, 0, 0, 0, zone)
            .toInstant().toEpochMilli()

    private fun item(timestamp: Long) = HistoryUiItem(
        id = timestamp,
        type = HistoryType.VIEWED,
        fruitId = 1,
        fruitName = "Fruit",
        timestamp = timestamp
    )

    @Test
    fun `записи группируются по дням с человеческими заголовками`() {
        val items = listOf(
            item(ts(10, 18, 10)),
            item(ts(10, 18, 9)),
            item(ts(10, 17, 22)),
            item(ts(10, 1, 8))
        )

        val groups = groupHistoryByDay(items, zone, today)

        assertEquals(listOf("Сегодня", "Вчера", "1 октября 2026"), groups.map { it.title })
        assertEquals(2, groups[0].items.size)
        assertEquals(1, groups[1].items.size)
        assertEquals(1, groups[2].items.size)
    }

    @Test
    fun `пустая история не даёт групп`() {
        assertTrue(groupHistoryByDay(emptyList(), zone, today).isEmpty())
    }

    @Test
    fun `границей месяца формат остаётся корректным`() {
        val items = listOf(item(ts(1, 3, 5)))

        val groups = groupHistoryByDay(items, zone, today)

        assertEquals("3 января 2026", groups.single().title)
    }
}
