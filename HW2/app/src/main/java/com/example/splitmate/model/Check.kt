package com.example.splitmate.model

import java.time.LocalDate

data class Check(
    val id: Int,
    val sum: Int,
    val tip: Int,
    val people: Int,
    val date: LocalDate
)
