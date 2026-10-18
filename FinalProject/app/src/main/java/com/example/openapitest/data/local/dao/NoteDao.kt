package com.example.openapitest.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.openapitest.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE fruitId = :fruitId")
    fun observeByFruit(fruitId: Int): Flow<NoteEntity?>

    @Query("SELECT COUNT(*) FROM notes")
    fun observeCount(): Flow<Int>

    @Upsert
    suspend fun upsert(note: NoteEntity)

    @Query("DELETE FROM notes WHERE fruitId = :fruitId")
    suspend fun deleteByFruit(fruitId: Int)
}
