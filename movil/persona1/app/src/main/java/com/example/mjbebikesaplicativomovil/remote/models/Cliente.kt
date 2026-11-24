package com.example.mjbebikesaplicativomovil.remote.models

import com.google.gson.annotations.SerializedName

// Tabla clientes. Todo cliente está asociado a una cuenta de usuario.
data class Cliente(
    val id: Int? = null,

    @SerializedName("usuario_id")
    val usuarioId: Int? = null,

    @SerializedName("tipo_documento_id")
    val tipoDocumentoId: Int? = null,

    @SerializedName("numero_documento")
    val numeroDocumento: String? = null,

    val nombres: String? = null,

    val apellidos: String? = null,

    val direccion: String? = null,

    @SerializedName("telefono_clnt")
    val telefono: String? = null
)

// Respuesta de POST / PUT /clientes: { "mensaje": "...", "cliente": {...} }
data class ClienteRespuesta(
    val mensaje: String? = null,
    val cliente: Cliente? = null
)

// Tabla tipos_documentos (lista del formulario de clientes y proveedores)
data class TipoDocumento(
    val id: Int? = null,

    val sigla: String? = null,

    @SerializedName("nombre_documento")
    val nombreDocumento: String? = null
)

// Solo los datos del usuario que necesita el formulario de clientes
data class UsuarioResumen(
    val id: Int? = null,
    val login: String? = null,
    val email: String? = null
)
