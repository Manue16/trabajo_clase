package com.example.mjbebikesaplicativomovil.remote

import com.example.mjbebikesaplicativomovil.remote.models.LoginRequest
import com.example.mjbebikesaplicativomovil.remote.models.LoginResponse
import com.example.mjbebikesaplicativomovil.remote.models.Mensaje
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    // Envía las credenciales al endpoint POST /api/login.
    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    // Invalida el JWT en Laravel (POST /api/logout, requiere el token).
    @POST("logout")
    suspend fun logout(): Response<Mensaje>
}
