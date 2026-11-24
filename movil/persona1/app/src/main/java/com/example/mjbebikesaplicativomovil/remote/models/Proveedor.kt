package com.example.mjbebikesaplicativomovil.remote.models

import com.google.gson.annotations.SerializedName

// Tabla proveedores
data class Proveedor(
    val id: Int? = null,

    @SerializedName("nombre_proveedor")
    val nombreProveedor: String? = null,

    @SerializedName("tipo_documento_id")
    val tipoDocumentoId: Int? = null,

    @SerializedName("numero_identidad")
    val numeroIdentidad: String? = null,

    val direccion: String? = null,

    @SerializedName("telefono_prv")
    val telefono: String? = null,

    // "activo" o "inactivo"
    val estado: String? = null
)

// Respuesta de POST / PUT /proveedores: { "mensaje": "...", "proveedor": {...} }
data class ProveedorRespuesta(
    val mensaje: String? = null,
    val proveedor: Proveedor? = null
)
