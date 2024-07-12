package com.example.personaltraining.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "Media",
    foreignKeys = [ForeignKey(
        entity = Ejercicio::class,
        parentColumns = ["ID"],
        childColumns = ["ejercicioId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("ejercicioId")]
)
data class Media(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ejercicioId: Int, // Id del ejercicio al que pertenece este medio
    val tipo: MediaTipo, // Tipo de medio: Imagen, Secuencia, GIF, Video
    val ruta: String // Ruta relativa al directorio de medios en personalTraining
)
enum class MediaTipo {
    IMAGE,
    IMAGE_SEQUENCE,
    GIF,
    VIDEO
}