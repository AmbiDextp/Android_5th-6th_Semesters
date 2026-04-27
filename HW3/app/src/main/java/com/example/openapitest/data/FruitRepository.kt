package com.example.openapitest.data

import com.example.openapitest.NetworkModule
import com.example.openapitest.data.remote.OneApi
import com.example.openapitest.data.remote.toDomainOrNull
import com.example.openapitest.model.Fruit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FruitRepository(private val api: OneApi = NetworkModule.api) {
    suspend fun fruits(): List<Fruit> = withContext(Dispatchers.IO) {
        api.getFruits().mapNotNull { it.toDomainOrNull() }
    }
}