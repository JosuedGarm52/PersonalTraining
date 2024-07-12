package com.example.personaltraining.model

import androidx.room.Embedded
import androidx.room.Relation

data class EjercicioConMedios(
    @Embedded val ejercicio: Ejercicio,
    @Relation(
        parentColumn = "ID",
        entityColumn = "ejercicioId"
    )
    val medios: List<Media>
)