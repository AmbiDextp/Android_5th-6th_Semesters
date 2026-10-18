package com.example.openapitest.domain

import com.example.openapitest.model.Fruit
import org.junit.Assert.assertEquals
import org.junit.Test

class FilteringTest {

    private fun fruit(id: Int, name: String, type: String = "Paramecia") =
        Fruit(id, name, type, "description")

    private val fruits = listOf(
        fruit(1, "Gomu Gomu no Mi", "Paramecia"),
        fruit(2, "Mera Mera no Mi", "Logia"),
        fruit(3, "Hito Hito no Mi", "Zoan"),
        fruit(4, "Gura Gura no Mi", "Paramecia")
    )

    @Test
    fun `фильтр ALL показывает всё кроме скрытых в алфавитном порядке`() {
        val states = mapOf(2 to FruitUserState(2, isHidden = true))
        val result = filterAndSortFruits(fruits, states, "", FruitFilterState())
        assertEquals(listOf(1, 4, 3), result.map { it.id })
    }

    @Test
    fun `фильтр HIDDEN показывает только скрытые`() {
        val states = mapOf(2 to FruitUserState(2, isHidden = true))
        val result = filterAndSortFruits(
            fruits, states, "", FruitFilterState(status = CatalogStatusFilter.HIDDEN)
        )
        assertEquals(listOf(2), result.map { it.id })
    }

    @Test
    fun `фильтр FAVOURITES показывает только избранные`() {
        val states = mapOf(
            1 to FruitUserState(1, status = FruitStatus.FAVOURITE),
            3 to FruitUserState(3, status = FruitStatus.WANT)
        )
        val result = filterAndSortFruits(
            fruits, states, "", FruitFilterState(status = CatalogStatusFilter.FAVOURITES)
        )
        assertEquals(listOf(1), result.map { it.id })
    }

    @Test
    fun `фильтры WANT и STUDIED работают независимо`() {
        val states = mapOf(
            2 to FruitUserState(2, status = FruitStatus.WANT),
            4 to FruitUserState(4, status = FruitStatus.STUDIED)
        )
        val want = filterAndSortFruits(
            fruits, states, "", FruitFilterState(status = CatalogStatusFilter.WANT)
        )
        val studied = filterAndSortFruits(
            fruits, states, "", FruitFilterState(status = CatalogStatusFilter.STUDIED)
        )
        assertEquals(listOf(2), want.map { it.id })
        assertEquals(listOf(4), studied.map { it.id })
    }

    @Test
    fun `фильтр по типу работает вместе со статусом`() {
        val states = mapOf(
            1 to FruitUserState(1, status = FruitStatus.STUDIED),
            4 to FruitUserState(4, status = FruitStatus.STUDIED)
        )
        val result = filterAndSortFruits(
            fruits,
            states,
            "",
            FruitFilterState(status = CatalogStatusFilter.STUDIED, selectedTypes = setOf("Logia", "Zoan"))
        )
        assertEquals(emptyList<Int>(), result.map { it.id })
    }

    @Test
    fun `пустой набор типов означает все типы`() {
        val result = filterAndSortFruits(
            fruits, emptyMap(), "", FruitFilterState(selectedTypes = emptySet())
        )
        assertEquals(4, result.size)
    }

    @Test
    fun `поиск идёт по имени без учёта регистра`() {
        val result = filterAndSortFruits(fruits, emptyMap(), "GOMU", FruitFilterState())
        assertEquals(listOf(1), result.map { it.id })
    }

    @Test
    fun `поиск идёт и по типу`() {
        val result = filterAndSortFruits(fruits, emptyMap(), "logia", FruitFilterState())
        assertEquals(listOf(2), result.map { it.id })
    }

    @Test
    fun `закреплённые плоды поднимаются наверх`() {
        val states = mapOf(3 to FruitUserState(3, isPinned = true))
        val result = filterAndSortFruits(fruits, states, "", FruitFilterState())
        assertEquals(3, result.first().id)
    }
}
