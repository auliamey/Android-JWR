package com.example.pbd_jwr.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Body

import com.example.pbd_jwr.data.entity.User
import com.example.pbd_jwr.data.model.LoginModel
import okhttp3.MultipartBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.Part

interface ApiService {
    @POST("/api/auth/login")
    suspend fun login(@Body loginRequest: LoginModel.LoginRequest): LoginModel.LoginResponse

    @POST("/api/auth/token")
    suspend fun getToken(@Header("Authorization") token: String): TokenResponse

    @Multipart
    @POST("/api/bill/upload")
    suspend fun uploadBill(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): UploadResponse
}

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)

data class UploadResponse(
    val success: Boolean,
    val message: String
)