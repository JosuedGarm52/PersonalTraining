package com.example.personaltraining.repository

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.room.Transaction
import com.example.personaltraining.appFiles.FileManager
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
    private val mediaDao: MediaDao,
    private val fileManager: FileManager
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
    @Transaction
    suspend fun deleteRutina(rutinaId: Int) {
        rutinaDao.deleteRutinaById(rutinaId)
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
    suspend fun getMediaById(mediaId: Long): Media? {
        return mediaDao.getMediaById(mediaId)
    }
    suspend fun getMediaForExercise(ejercicioId: Int): List<Media> {
        return mediaDao.getMediaForExercise(ejercicioId)
    }
    suspend fun insertMedia(media: Media): Long {
        return mediaDao.insertMedia(media)
    }

    suspend fun updateMedia(media: Media): Boolean {
        val oldMedia = getMediaById(media.id)
        val oldFilePath = oldMedia?.let { fileManager.getFilePath(it.ruta) }

        // Actualiza el objeto Media
        mediaDao.updateMedia(media)

        // Verifica si la actualización fue exitosa recuperando el objeto Media nuevamente
        val updatedMedia = getMediaById(media.id)
        val updateSuccessful = updatedMedia == media

        if (updateSuccessful) {
            oldFilePath?.let { fileManager.deleteFile(it) }
        }

        return updateSuccessful
    }


    suspend fun deleteMedia(media: Media): Boolean {
        mediaDao.deleteMedia(media)
        val filePath = fileManager.getFilePath(media.ruta)
        return fileManager.deleteFile(filePath)
    }

    suspend fun deleteMediaByEjercicioId(ejercicio: Ejercicio) {
        val mediaList = mediaDao.getMediaForExercise(ejercicio.ID)
        for (media in mediaList) {
            deleteMedia(media)
        }
    }
    suspend fun deleteMediaByEjercicioId(ejercicioId: Int) {
        val mediaList = mediaDao.getMediaForExercise(ejercicioId)
        for (media in mediaList) {
            deleteMedia(media)
        }
    }
}