package com.example.personaltraining.application

import android.app.Application
import com.example.personaltraining.appFiles.FileManager
import com.example.personaltraining.database.RutinasDatabase
import com.example.personaltraining.repository.RutinasRepository

class RutinasApplication : Application() {
    val database by lazy { RutinasDatabase.getDatabase(this) }
    val fileManager by lazy { FileManager(this) }
    val repository by lazy { RutinasRepository(database.ejercicioDAO(), database.rutinaDAO(), database.mediaDao(), fileManager) }
}
