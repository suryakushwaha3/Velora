package com.example.velora.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

// Response Data Classes
data class ImgBBResponse(
    val data: ImgBBData,
    val success: Boolean
)

data class ImgBBData(
    val url: String
)

// Retrofit Interface
interface ImgBBApi {
    @Multipart
    @POST("1/upload")
    suspend fun uploadImage(
        @Query("key") apiKey: String,
        @Part image: MultipartBody.Part
    ): Response<ImgBBResponse>

    companion object {
        private const val BASE_URL = "https://api.imgbb.com/"

        val instance: ImgBBApi by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ImgBBApi::class.java)
        }
    }
}