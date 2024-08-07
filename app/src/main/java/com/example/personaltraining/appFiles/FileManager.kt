package com.example.personaltraining.appFiles

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AlertDialog
import java.io.File
import java.io.IOException

class FileManager(private val context: Context) {

    private val tag = "FileManager"

    // Directorio para almacenar media
    private val mediaDir: File by lazy {
        File(context.filesDir, "personalTraining/datos").apply {
            if (!exists()) {
                try {
                    mkdirs()
                } catch (e: IOException) {
                    Log.e(tag, "Error al crear el directorio: $path", e)
                    showDirectoryCreationErrorDialog()
                }
            }
        }
    }

    // Método para obtener la ruta del directorio de media
    fun getMediaDirectory(): File {
        return mediaDir
    }


    fun copyRawResourceToPrivateStorage(resourceId: Int, fileName: String): File? {
        return try {
            val inputStream = context.resources.openRawResource(resourceId)
            val destinationFile = File(mediaDir, fileName)
            inputStream.use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            destinationFile
        } catch (e: Exception) {
            Log.e(tag, "Error al copiar archivo desde recursos a almacenamiento privado", e)
            null
        }
    }

    // Función para copiar un archivo a almacenamiento privado
    fun copyFileToPrivateStorage(uri: Uri, customFileName: String? = null): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = customFileName ?: uri.lastPathSegment ?: "temp_file"
            val destinationFile = File(mediaDir, fileName)
            inputStream.use { input ->
                destinationFile.outputStream().use { output ->
                    input?.copyTo(output)
                }
            }
            destinationFile
        } catch (e: Exception) {
            Log.e(tag, "Error al copiar archivo a almacenamiento privado", e)
            null
        }
    }

    // Función para eliminar un archivo
    fun deleteFile(filePath: String): Boolean {
        val file = File(filePath)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }


    // Función para obtener la ruta completa de un archivo en almacenamiento privado
    fun getFilePath(fileName: String): String {
        return File(mediaDir, fileName).absolutePath
    }

    fun fileExistsInPrivateStorage(fileName: String): Boolean {
        val file = File(mediaDir, fileName)
        return file.exists()
    }


    // Función para mostrar un diálogo en caso de error al crear el directorio
    private fun showDirectoryCreationErrorDialog() {
        AlertDialog.Builder(context)
            .setTitle("Error")
            .setMessage("No se pudo crear el directorio de almacenamiento privado.")
            .setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}