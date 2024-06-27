package com.example.personaltraining.repository

import androidx.annotation.WorkerThread
import androidx.room.Transaction
import com.example.personaltraining.model.Ejercicio
import com.example.personaltraining.model.EjercicioDao
import com.example.personaltraining.model.Rutina
import com.example.personaltraining.model.RutinaDao
import kotlinx.coroutines.flow.Flow

class RutinasRepository (
    private val ejercicioDao: EjercicioDao,
    private val rutinaDao: RutinaDao
) {

    // Inserta la rutina y retorna el ID generado
    @WorkerThread
    suspend fun insertRutinaAndGetId(rutina: Rutina): Long {
        return rutinaDao.insert(rutina)
    }

    fun getAllRutinas(): Flow<List<Rutina>> = rutinaDao.getAll()

    @WorkerThread
    suspend fun insertRutina(rutina: Rutina): Long {
        return rutinaDao.insert(rutina)
    }

    fun getEjerciciosByRutinaId(rutinaId: Int): Flow<List<Ejercicio>> {
        return ejercicioDao.getEjerciciosByRutinaId(rutinaId)
    }

    @WorkerThread
    suspend fun insertEjercicio(ejercicio: Ejercicio) {
        ejercicioDao.insert(ejercicio)
    }

    @WorkerThread
    suspend fun insertEjercicios(ejercicios: List<Ejercicio>) {
        ejercicioDao.insertAll(ejercicios)
    }

    @WorkerThread
    @Transaction
    suspend fun deleteRutinaWithExercises(rutinaId: Int) {
        ejercicioDao.deleteEjerciciosByRutinaId(rutinaId)
        rutinaDao.deleteRutinaById(rutinaId)
    }
}