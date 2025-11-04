package com.example.controltarjetas.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageHelper {

    /**
     * Guarda una imagen del URI en el almacenamiento interno de la app
     * Retorna la ruta del archivo guardado
     */
    fun saveImageToInternalStorage(context: Context, imageUri: Uri): String? {
        return try {
            // Leer la imagen del URI
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Crear directorio para logos si no existe
            val directory = File(context.filesDir, "bank_logos")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            // Generar nombre único para el archivo
            val filename = "logo_${UUID.randomUUID()}.jpg"
            val file = File(directory, filename)

            // Guardar la imagen comprimida
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.flush()
            outputStream.close()

            // Retornar la ruta absoluta
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Elimina una imagen del almacenamiento interno
     */
    fun deleteImage(imagePath: String?): Boolean {
        if (imagePath == null) return false

        return try {
            val file = File(imagePath)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Verifica si un archivo de imagen existe
     */
    fun imageExists(imagePath: String?): Boolean {
        if (imagePath == null) return false
        return File(imagePath).exists()
    }
}