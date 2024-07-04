package com.example.personaltraining.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.personaltraining.model.Ejercicio
import com.example.personaltraining.model.EjercicioDao
import com.example.personaltraining.model.Rutina
import com.example.personaltraining.model.RutinaDao

@Database(entities = [Ejercicio::class,Rutina::class], version = 4)
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
                )
                    //.addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()//quitar cuando se pase a produccion
                    .build()
                INSTANCE = instance
                instance
            }
        }
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregar una nueva columna 'Objetivo' con un valor predeterminado
                database.execSQL("ALTER TABLE Ejercicio ADD COLUMN Objetivo TEXT DEFAULT ''")

                // Agregar una nueva columna 'isObjetivo' con un valor predeterminado
                database.execSQL("ALTER TABLE Ejercicio ADD COLUMN isObjetivo INTEGER DEFAULT 0")
            }
        }
        //bruh
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // No se hace nada, migración vacía
            }
        }

    }
}