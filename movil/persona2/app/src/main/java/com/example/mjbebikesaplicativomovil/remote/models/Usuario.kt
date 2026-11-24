package com.example.mjbebikesaplicativomovil.remote.models

import com.google.gson.annotations.SerializedName

// Usuario tal como lo devuelve la API (la contraseña nunca viene en la respuesta).
data class Usuario(
    val id: Int? = null,

    val login: String? = null,

    val email: String? = null,

    @SerializedName("rol_id")
    val rolId: Int? = null,

    // "Activo" / "Inactivo" (en la BD puede venir en minúscula)
    val estado: String? = null
)

// Lo que se envía en POST /usuarios (crear) y PUT /usuarios/{id} (editar).
// Si passwordHarsh va en null, Gson no lo envía y la API conserva la contraseña actual.
data class UsuarioGuardar(
    val login: String,

    val email: String,

    // SHA-256 de la contraseña (igual que en el login)
    @SerializedName("password_harsh")
    val passwordHarsh: String?,

    @SerializedName("rol_id")
    val rolId: Int,

    val estado: String
)

// Respuesta de POST / PUT /usuarios: { "mensaje": "...", "usuario": {...} }
data class UsuarioRespuesta(
    val mensaje: String? = null,
    val usuario: Usuario? = null
)
