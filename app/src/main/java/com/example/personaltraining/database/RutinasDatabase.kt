package com.example.personaltraining.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.personaltraining.model.Ejercicio
import com.example.personaltraining.model.EjercicioDao
import com.example.personaltraining.model.Rutina
import com.example.personaltraining.model.RutinaDao

@Database(entities = [Ejercicio::class,Rutina::class], version = 2)
abstract class RutinasDatabase : RoomDatabase() {
    abstract fun ejercicioDAO(): EjercicioDao
    abstract fun rutinaDAO(): RutinaDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: RutinasDatabase? = null

        fun getDatabase(context: Context): RutinasDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RutinasDatabase::class.java,
                    "Rutinas_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}