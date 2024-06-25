package com.example.personaltraining.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.sql.Time

@Entity(
    tableName = "Ejercicio",
    foreignKeys = [ForeignKey(
        entity = Rutina::class,
        parentColumns = ["ID"],
        childColumns = ["rutinaId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("rutinaId")]
)
data class Ejercicio (
    @PrimaryKey(autoGenerate = true) val ID : Int,
    val Nombre : String,
    val DEjercicio : String,
    val DDescanso : String,
    val rutinaId: Int
)