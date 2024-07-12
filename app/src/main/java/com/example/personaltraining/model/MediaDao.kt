package com.example.personaltraining.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MediaDao {
    @Insert
    suspend fun insertMedia(media: Media): Long

    @Query("SELECT * FROM Media WHERE ejercicioId = :ejercicioId")
    suspend fun getMediaForExercise(ejercicioId: Int): List<Media>

    // Otros métodos según tus necesidades (actualización, eliminación, consultas adicionales, etc.)
}
