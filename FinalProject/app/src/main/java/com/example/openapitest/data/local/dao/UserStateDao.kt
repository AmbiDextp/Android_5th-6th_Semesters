package com.example.openapitest.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.openapitest.data.local.entity.FruitUserStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStateDao {

    @Query("SELECT * FROM fruit_user_state")
    fun observeAll(): Flow<List<FruitUserStateEntity>>

    @Query("SELECT * FROM fruit_user_state WHERE fruitId = :fruitId")
    fun observeByFruit(fruitId: Int): Flow<FruitUserStateEntity?>

    @Query("SELECT * FROM fruit_user_state WHERE fruitId = :fruitId")
    suspend fun getByFruit(fruitId: Int): FruitUserStateEntity?

    @Upsert
    suspend fun upsert(state: FruitUserStateEntity)

    @Query("DELETE FROM fruit_user_state WHERE fruitId = :fruitId")
    suspend fun deleteByFruit(fruitId: Int)
}
