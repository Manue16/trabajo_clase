package com.example.mjbebikes.data.model

// Tabla proveedores
data class Proveedor(
    val id: Int? = null,
    val nombre_proveedor: String? = null,
    val tipo_documento_id: Int? = null,
    val numero_identidad: String? = null,
    val direccion: String? = null,
    val telefono_prv: String? = null,
    val estado: String? = null          // "activo" o "inactivo"
)

// Respuesta de POST / PUT /proveedores: { "mensaje": "...", "proveedor": {...} }
data class ProveedorRespuesta(
    val mensaje: String? = null,
    val proveedor: Proveedor? = null
)
