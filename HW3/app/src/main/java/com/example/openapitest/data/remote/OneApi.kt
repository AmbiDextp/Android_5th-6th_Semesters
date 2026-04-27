package com.example.openapitest.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface OneApi {
    @GET("fruits/en")
    suspend fun getFruits(): List<OneFruitDto>
}