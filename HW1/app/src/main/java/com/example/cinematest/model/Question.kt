package com.example.cinematest.model

data class Question(
    val id: Int,
    val question: String,
    val responses: List<String>,
    val correct: Int
)