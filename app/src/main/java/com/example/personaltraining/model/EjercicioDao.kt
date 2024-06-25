package com.example.personaltraining.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
@Dao
interface EjercicioDao {
    @Query("SELECT * FROM Ejercicio WHERE rutinaId = :rutinaId")
    fun getEjerciciosByRutinaId(rutinaId: Int): Flow<List<Ejercicio>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ejercicio: Ejercicio)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ejercicios: List<Ejercicio>)
}