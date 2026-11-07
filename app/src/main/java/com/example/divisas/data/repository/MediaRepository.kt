package com.example.divisas.data.repository

import android.content.Context
import android.net.Uri
import com.example.divisas.BuildConfig
import com.example.divisas.data.remote.CloudinaryApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class MediaRepository(private val api: CloudinaryApi) {

    /** Sube la imagen a Cloudinary (unsigned preset) y devuelve la URL https. */
    suspend fun upload(context: Context, uri: Uri): String {
        require(BuildConfig.CLOUDINARY_CLOUD_NAME.isNotBlank() && BuildConfig.CLOUDINARY_UPLOAD_PRESET.isNotBlank()) {
            "Faltan datos de Cloudinary en local.properties"
        }
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("No se pudo leer la imagen")
        val body = bytes.toRequestBody("image/*".toMediaType())
        val part = MultipartBody.Part.createFormData("file", "profile.jpg", body)
        val preset = BuildConfig.CLOUDINARY_UPLOAD_PRESET.toRequestBody("text/plain".toMediaType())
        val response = api.upload(BuildConfig.CLOUDINARY_CLOUD_NAME, part, preset)
        return response.secureUrl ?: error("Cloudinary no devolvio la URL")
    }
}
