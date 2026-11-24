package com.example.mjbebikesaplicativomovil.data.repository

import com.example.mjbebikesaplicativomovil.data.local.TokenManager
import com.example.mjbebikesaplicativomovil.remote.ApiService
import com.example.mjbebikesaplicativomovil.remote.models.LoginRequest
import com.example.mjbebikesaplicativomovil.remote.models.LoginResponse
import com.example.mjbebikesaplicativomovil.remote.models.RegistroRequest
import com.example.mjbebikesaplicativomovil.remote.models.RegistroResponse
import retrofit2.Response

class AuthRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    // Inicia sesión utilizando las credenciales del usuario.
    suspend fun login(
        email: String,
        passwordHash: String
    ): Response<LoginResponse> {

        val response = apiService.login(
            LoginRequest(
                email = email,
                passwordHarsh = passwordHash
            )
        )

        // Si Laravel responde correctamente, guarda el JWT y los datos del usuario
        // (el rol decide qué opciones ve en el menú).
        if (response.isSuccessful) {
            val body = response.body()
            val token: String? = body?.token
            val usuario = body?.usuario

            if (!token.isNullOrBlank() && usuario != null) {
                tokenManager.saveSesion(token, usuario)
            }
        }

        return response
    }

    // Crea la cuenta de un cliente nuevo. No inicia sesión: después se entra con el login.
    suspend fun registrar(request: RegistroRequest): Response<RegistroResponse> {
        return apiService.register(request)
    }

    // Cierra la sesión en Laravel y borra los datos guardados en el teléfono.
    // Aunque la API no responda, la sesión local se borra igual.
    suspend fun logout() {
        try {
            apiService.logout()
        } catch (e: Exception) {
            // Sin conexión: igual se cierra la sesión en el teléfono.
        }
        tokenManager.clearToken()
    }
}
