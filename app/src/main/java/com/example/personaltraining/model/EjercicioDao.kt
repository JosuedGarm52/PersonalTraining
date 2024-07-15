package com.example.personaltraining.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EjercicioDao{
    @Query("SELECT * FROM Ejercicio")
    fun getAll(): Flow<List<Ejercicio>>

    @Query("SELECT * FROM Ejercicio WHERE rutinaId = :rutinaId")
    fun getEjerciciosByRutinaId(rutinaId: Int): Flow<List<Ejercicio>>

    @Insert
    suspend fun onlyAdd(ejercicio: Ejercicio)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ejercicio: Ejercicio): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ejercicios: List<Ejercicio>)

    @Update
    suspend fun updateEjercicio(ejercicio: Ejercicio)

    @Query("DELETE FROM Ejercicio WHERE ID = :ejercicioId")
    suspend fun deleteEjercicioById(ejercicioId: Int)

    @Query("DELETE FROM Ejercicio WHERE rutinaId = :rutinaId")
    suspend fun deleteEjerciciosByRutinaId(rutinaId: Int)
}