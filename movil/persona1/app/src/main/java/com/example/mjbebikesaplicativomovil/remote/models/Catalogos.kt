package com.example.mjbebikesaplicativomovil.remote.models

import com.google.gson.annotations.SerializedName

// Tablas pequeñas que se usan como listas desplegables en los formularios.

// Tabla medidas
data class Medida(
    val id: Int? = null,

    @SerializedName("tipo_medida")
    val tipoMedida: String? = null
)

// Tabla tallas. GET /tallas incluye la medida.
data class Talla(
    val id: Int? = null,

    @SerializedName("medida_id")
    val medidaId: Int? = null,

    val talla: String? = null,

    val medida: Medida? = null
)

// Tabla locales
data class Local(
    val id: Int? = null,

    @SerializedName("nombre_local")
    val nombreLocal: String? = null,

    @SerializedName("direccion_local")
    val direccionLocal: String? = null,

    val correo: String? = null,

    val telefono: String? = null,

    val estado: String? = null
)

// Respuesta de DELETE y de los errores: { "mensaje": "..." }
data class Mensaje(
    val mensaje: String? = null
)
