package com.example.mjbebikesaplicativomovil.utils

import com.example.mjbebikesaplicativomovil.remote.models.Usuario
import com.example.mjbebikesaplicativomovil.remote.models.UsuarioGuardar

// Datos del formulario de usuario (crear o editar empleado).
data class UsuarioFormulario(
    val login: String = "",
    val email: String = "",
    // Contraseña escrita (sin cifrar). Al editar, vacía = conservar la actual.
    val password: String = "",
    val rolId: Int? = null,
    val estado: String = "Activo"
) {
    companion object {
        // Llena el formulario con un usuario existente (para editar). La contraseña queda vacía.
        fun desde(u: Usuario) = UsuarioFormulario(
            login = u.login.orEmpty(),
            email = u.email.orEmpty(),
            rolId = u.rolId,
            estado = if (UsuarioUtils.esActivo(u)) "Activo" else "Inactivo"
        )
    }
}

object UsuarioUtils {

    val rolesDisponibles = listOf(Roles.ADMIN, Roles.VENDEDOR, Roles.BODEGUERO, Roles.CLIENTE)

    private val formatoEmail = Regex("""^[^\s@]+@[^\s@]+\.[^\s@]+$""")

    fun esActivo(u: Usuario): Boolean = u.estado.equals("activo", ignoreCase = true)

    // Revisa el formulario. "existentes" sirve para no repetir usuario ni correo
    // (la API no lo impide); al editar se excluye el propio usuario (idActual).
    fun validar(
        f: UsuarioFormulario,
        esNuevo: Boolean,
        existentes: List<Usuario>,
        idActual: Int? = null
    ): Map<String, String> {
        val errores = mutableMapOf<String, String>()
        val otros = existentes.filter { it.id != idActual }

        val login = f.login.trim()
        when {
            login.isEmpty() -> errores["login"] = "El usuario es obligatorio"
            login.length > 255 -> errores["login"] = "Máximo 255 caracteres"
            login.contains(' ') -> errores["login"] = "El usuario no puede tener espacios"
            otros.any { it.login.equals(login, ignoreCase = true) } ->
                errores["login"] = "Ya existe un usuario con ese nombre"
        }

        val email = f.email.trim()
        when {
            email.isEmpty() -> errores["email"] = "El correo es obligatorio"
            !formatoEmail.matches(email) -> errores["email"] = "Escribe un correo válido"
            email.length > 255 -> errores["email"] = "Máximo 255 caracteres"
            otros.any { it.email.equals(email, ignoreCase = true) } ->
                errores["email"] = "Ya existe un usuario con ese correo"
        }

        if (esNuevo && f.password.isEmpty()) errores["password"] = "La contraseña es obligatoria"

        if (f.rolId == null || f.rolId !in rolesDisponibles) errores["rol"] = "Selecciona el rol"

        if (f.estado != "Activo" && f.estado != "Inactivo") errores["estado"] = "Estado no válido"

        return errores
    }

    // Convierte el formulario en lo que se envía a la API.
    // La contraseña se cifra con SHA-256; si está vacía (al editar) no se envía.
    fun aGuardar(f: UsuarioFormulario): UsuarioGuardar = UsuarioGuardar(
        login = f.login.trim(),
        email = f.email.trim().lowercase(),
        passwordHarsh = if (f.password.isEmpty()) null else PasswordUtils.sha256(f.password),
        rolId = f.rolId ?: 0,
        estado = f.estado
    )

    // Mismo usuario con el estado contrario, para activar / desactivar sin tocar la contraseña.
    fun conEstadoContrario(u: Usuario): UsuarioGuardar = UsuarioGuardar(
        login = u.login.orEmpty(),
        email = u.email.orEmpty(),
        passwordHarsh = null,
        rolId = u.rolId ?: 0,
        estado = if (esActivo(u)) "Inactivo" else "Activo"
    )

    // Búsqueda por usuario o correo, filtro por rol (null = todos) y por estado.
    fun filtrar(
        usuarios: List<Usuario>,
        busqueda: String,
        rolId: Int?,
        estado: EstadoFiltro
    ): List<Usuario> {
        val texto = busqueda.trim().lowercase()
        return usuarios.filter { u ->
            val coincideTexto = texto.isEmpty() ||
                    u.login?.lowercase()?.contains(texto) == true ||
                    u.email?.lowercase()?.contains(texto) == true
            val coincideRol = rolId == null || u.rolId == rolId
            val coincideEstado = when (estado) {
                EstadoFiltro.TODOS -> true
                EstadoFiltro.ACTIVOS -> esActivo(u)
                EstadoFiltro.INACTIVOS -> !esActivo(u)
            }
            coincideTexto && coincideRol && coincideEstado
        }
    }
}
