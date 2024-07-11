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
        File(context.filesDir, "personalTraining").apply {
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


    // Función para copiar un archivo a almacenamiento privado
    fun copyFileToPrivateStorage(uri: Uri): File? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = uri.lastPathSegment ?: "temp_file"
            val destinationFile = File(mediaDir, fileName)
            inputStream.use { input ->
                destinationFile.outputStream().use { output ->
                    input?.copyTo(output)
                }
            }
            return destinationFile
        } catch (e: Exception) {
            Log.e(tag, "Error al copiar archivo a almacenamiento privado", e)
            return null
        }
    }

    // Función para eliminar un archivo
    fun deleteFile(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            file.delete()
        }
    }

    // Función para obtener la ruta completa de un archivo en almacenamiento privado
    fun getFilePath(fileName: String): String {
        return File(mediaDir, fileName).absolutePath
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