package com.example.divisas.data.remote

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

data class CloudinaryResponse(
    @SerializedName("secure_url") val secureUrl: String?
)

interface CloudinaryApi {
    @Multipart
    @POST("v1_1/{cloud}/image/upload")
    suspend fun upload(
        @Path("cloud") cloud: String,
        @Part file: MultipartBody.Part,
        @Part("upload_preset") preset: RequestBody
    ): CloudinaryResponse
}
