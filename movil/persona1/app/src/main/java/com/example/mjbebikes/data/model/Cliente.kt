package com.example.mjbebikes.data.model

// Tabla clientes. Todo cliente está asociado a una cuenta de usuario (usuario_id).
data class Cliente(
    val id: Int? = null,
    val usuario_id: Int? = null,
    val tipo_documento_id: Int? = null,
    val numero_documento: String? = null,
    val nombres: String? = null,
    val apellidos: String? = null,
    val direccion: String? = null,
    val telefono_clnt: String? = null
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
    val nombre_documento: String? = null
)

// Solo los datos del usuario que necesita el formulario de clientes
// (el modelo completo de usuarios es de la persona 2)
data class UsuarioResumen(
    val id: Int? = null,
    val login: String? = null,
    val email: String? = null
)
