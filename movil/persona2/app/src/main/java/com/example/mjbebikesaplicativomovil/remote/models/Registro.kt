package com.example.mjbebikesaplicativomovil.remote.models

import com.google.gson.annotations.SerializedName

// Lo que se envía en POST /api/register.
data class RegistroRequest(
    val login: String,

    val email: String,

    // SHA-256 de la contraseña (igual que en el login)
    @SerializedName("password_harsh")
    val passwordHarsh: String
)

// Respuesta de POST /api/register: { "mensaje": "Registro exitoso", "usuario": {...} }
data class RegistroResponse(
    val mensaje: String? = null,
    val usuario: UsuarioLogin? = null
)
