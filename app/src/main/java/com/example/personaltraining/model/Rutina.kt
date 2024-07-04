package com.example.personaltraining.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "Rutina")
data class Rutina(
    @PrimaryKey(autoGenerate = true) val ID: Int = 0,
    var nombre: String,
    var fechaCreacion: String = getCurrentDate()
)
fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    return sdf.format(Date())
}
