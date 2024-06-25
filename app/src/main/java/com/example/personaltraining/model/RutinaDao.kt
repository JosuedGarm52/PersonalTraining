package com.example.personaltraining.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RutinaDao {
    @Query("SELECT * FROM Rutina")
    fun getAll(): Flow<List<Rutina>>

    @Query("SELECT * FROM Rutina WHERE ID = :id")
    suspend fun getRutinaById(id: Int): Rutina?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rutina: Rutina): Long
}