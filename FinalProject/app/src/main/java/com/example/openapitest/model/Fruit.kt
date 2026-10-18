package com.example.openapitest.model

data class Fruit (
    val id: Int,
    val name: String,
    val type: String,
    val description: String,
    val img: String? = null
)