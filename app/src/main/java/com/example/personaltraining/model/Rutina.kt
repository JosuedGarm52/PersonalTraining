package com.example.personaltraining.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Rutina")
data class Rutina(
    @PrimaryKey(autoGenerate = true) val ID: Int = 0,
    val nombre: String
)
