package com.example.mjbebikes.data.model

// Tablas pequeñas que se usan como listas desplegables en los formularios.

// Tabla medidas
data class Medida(
    val id: Int? = null,
    val tipo_medida: String? = null
)

// Tabla tallas. GET /tallas incluye la medida.
data class Talla(
    val id: Int? = null,
    val medida_id: Int? = null,
    val talla: String? = null,
    val medida: Medida? = null
)

// Tabla locales
data class Local(
    val id: Int? = null,
    val nombre_local: String? = null,
    val direccion_local: String? = null,
    val correo: String? = null,
    val telefono: String? = null,
    val estado: String? = null
)

// Respuesta de DELETE y de los errores: { "mensaje": "..." }
data class Mensaje(
    val mensaje: String? = null
)
