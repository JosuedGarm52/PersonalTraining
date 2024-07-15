package com.example.personaltraining.repository

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.room.Transaction
import com.example.personaltraining.model.Ejercicio
import com.example.personaltraining.model.EjercicioDao
import com.example.personaltraining.model.Media
import com.example.personaltraining.model.MediaDao
import com.example.personaltraining.model.Rutina
import com.example.personaltraining.model.RutinaDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.forEach

class RutinasRepository (
    private val ejercicioDao: EjercicioDao,
    private val rutinaDao: RutinaDao,
    private val mediaDao: MediaDao
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

    fun getAllEjercicios(): Flow<List<Ejercicio>> = ejercicioDao.getAll()

    fun getEjerciciosByRutinaId(rutinaId: Int): Flow<List<Ejercicio>> {
        return ejercicioDao.getEjerciciosByRutinaId(rutinaId)
    }

    @WorkerThread
    suspend fun insertEjercicio(ejercicio: Ejercicio) : Long {
        return ejercicioDao.insert(ejercicio)
    }

    @WorkerThread
    suspend fun soloAdd(ejercicio: Ejercicio) {
        ejercicioDao.onlyAdd(ejercicio)
    }

    @WorkerThread
    suspend fun insertEjercicios(ejercicios: List<Ejercicio>) {
        ejercicioDao.insertAll(ejercicios)
    }
    @WorkerThread
    suspend fun updateEjercicio(ejercicio: Ejercicio) {
        ejercicioDao.insert(ejercicio)
    }

    @Transaction
    suspend fun deleteRutina(rutinaId: Int) {
        rutinaDao.deleteRutinaById(rutinaId)
    }


    suspend fun deleteEjercicioById(ejercicioId: Int) {
        deleteMediaByEjercicioId(ejercicioId)
        ejercicioDao.deleteEjercicioById(ejercicioId)
    }
    suspend fun deleteEjercicioById(ejercicio: Ejercicio) {
        deleteMediaByEjercicioId(ejercicio)
        ejercicioDao.deleteEjercicioById(ejercicio.ID)
    }

    @WorkerThread
    @Transaction
    suspend fun deleteRutinaWithExercises(rutinaId: Int) {
        try {
            // Recolecta los datos del Flow de ejercicios asociados a la rutina
            val ejercicios = ejercicioDao.getEjerciciosByRutinaId(rutinaId).firstOrNull() ?: return
            Log.d("RutinasRepository", "Ejercicios: $ejercicios")

            // Itera sobre cada ejercicio para obtener y eliminar sus medios asociados
            ejercicios.forEach { ejercicio ->
                val medios = mediaDao.getMediaForExercise(ejercicio.ID)
                medios.forEach { medio ->
                    deleteMedia(medio) // Elimina cada medio asociado al ejercicio
                }
            }

            // Eliminar todos los ejercicios asociados a la rutina
            ejercicioDao.deleteEjerciciosByRutinaId(rutinaId)

            // Finalmente, eliminar la rutina
            rutinaDao.deleteRutinaById(rutinaId)
        } catch (e: Exception) {
            Log.e("RutinasRepository", "Error deleting rutina and exercises: ${e.message}", e)
        }
    }

    suspend fun getMediaForExercise(ejercicioId: Int): List<Media> {
        return mediaDao.getMediaForExercise(ejercicioId)
    }
    suspend fun insertMedia(media: Media): Long {
        return mediaDao.insertMedia(media)
    }
    suspend fun updateMedia(media: Media) {
        mediaDao.updateMedia(media)
    }
    suspend fun deleteMedia(media: Media) {
        mediaDao.deleteMedia(media)
    }
    suspend fun deleteMediaByEjercicioId(ejercicio: Ejercicio) {
        mediaDao.deleteMediaByEjercicioId(ejercicio.ID)
    }
    suspend fun deleteMediaByEjercicioId(ejercicioId: Int) {
        mediaDao.deleteMediaByEjercicioId(ejercicioId)
    }
}