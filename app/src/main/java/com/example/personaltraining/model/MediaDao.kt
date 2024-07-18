package com.example.personaltraining.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MediaDao {
    @Insert
    suspend fun insertMedia(media: Media): Long

    @Update
    suspend fun updateMedia(media: Media)

    @Delete
    suspend fun deleteMedia(media: Media)

    @Query("DELETE FROM Media WHERE ejercicioId = :ejercicioId")
    suspend fun deleteMediaByEjercicioId(ejercicioId: Int)

    @Query("SELECT * FROM Media WHERE ejercicioId = :ejercicioId")
    suspend fun getMediaForExercise(ejercicioId: Int): List<Media>

    // Método para obtener un Media por su ID
    @Query("SELECT * FROM Media WHERE id = :mediaId LIMIT 1")
    suspend fun getMediaById(mediaId: Long): Media?
}
