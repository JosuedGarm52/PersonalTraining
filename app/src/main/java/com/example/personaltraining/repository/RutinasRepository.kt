package com.example.personaltraining.repository

import android.util.Log
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

    suspend fun getRutinaById(rutinaId: Int): Rutina? {
        return rutinaDao.getRutinaById(rutinaId)
    }

    @WorkerThread
    suspend fun insertRutina(rutina: Rutina): Long {
        return rutinaDao.insert(rutina)
    }
    @WorkerThread
    suspend fun updateRutina(rutina: Rutina) {
        rutinaDao.updateRutina(rutina)
    }

    fun getEjerciciosByRutinaId(rutinaId: Int): Flow<List<Ejercicio>> {
        return ejercicioDao.getEjerciciosByRutinaId(rutinaId)
    }

    @WorkerThread
    suspend fun insertEjercicio(ejercicio: Ejercicio) {
        ejercicioDao.insert(ejercicio)
    }

    @WorkerThread
    suspend fun soloAdd(ejercicio: Ejercicio) {
        ejercicioDao.onlyAdd(ejercicio)
    }

    @WorkerThread
    suspend fun insertEjercicios(ejercicios: List<Ejercicio>) {
        ejercicioDao.insertAll(ejercicios)
    }

    @Transaction
    suspend fun deleteRutina(rutinaId: Int) {
        rutinaDao.deleteRutinaById(rutinaId)
    }

    suspend fun deleteEjercicioById(ejercicioId: Int) {
        ejercicioDao.deleteEjercicioById(ejercicioId)
    }

    @WorkerThread
    @Transaction
    suspend fun deleteRutinaWithExercises(rutinaId: Int) {
        try {
            //Log.d("RutinasRepository", "Deleting exercises with rutinaId: $rutinaId")
            ejercicioDao.deleteEjerciciosByRutinaId(rutinaId)
            //Log.d("RutinasRepository", "Exercises deleted for rutinaId: $rutinaId")

            //Log.d("RutinasRepository", "Deleting rutina with id: $rutinaId")
            rutinaDao.deleteRutinaById(rutinaId)
            //Log.d("RutinasRepository", "Rutina and exercises deleted successfully")
        } catch (e: Exception) {
            Log.e("RutinasRepository", "Error deleting rutina and exercises: ${e.message}", e)
        }
    }
}