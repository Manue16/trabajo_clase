package com.example.mjbebikesaplicativomovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mjbebikesaplicativomovil.data.local.TokenManager
import com.example.mjbebikesaplicativomovil.remote.UsuariosApi
import com.example.mjbebikesaplicativomovil.remote.models.Usuario
import com.example.mjbebikesaplicativomovil.utils.ApiErrores
import com.example.mjbebikesaplicativomovil.utils.EstadoFiltro
import com.example.mjbebikesaplicativomovil.utils.UsuarioUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Las situaciones en las que puede estar la lista de usuarios.
sealed class UsuariosUiState {
    object Cargando : UsuariosUiState()
    // idPropio: el administrador que inició sesión (no puede desactivarse ni eliminarse).
    data class Listo(val usuarios: List<Usuario>, val idPropio: Int?) : UsuariosUiState()
    data class Error(val mensaje: String) : UsuariosUiState()
}

class UsuariosViewModel(
    private val api: UsuariosApi,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UsuariosUiState>(UsuariosUiState.Cargando)
    val uiState: StateFlow<UsuariosUiState> = _uiState.asStateFlow()

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    // Rol seleccionado en el filtro (null = todos).
    private val _rol = MutableStateFlow<Int?>(null)
    val rol: StateFlow<Int?> = _rol.asStateFlow()

    private val _estado = MutableStateFlow(EstadoFiltro.TODOS)
    val estado: StateFlow<EstadoFiltro> = _estado.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    // GET /api/usuarios. La pantalla la llama cada vez que se muestra.
    fun cargarUsuarios() {
        viewModelScope.launch {
            if (_uiState.value !is UsuariosUiState.Listo) {
                _uiState.value = UsuariosUiState.Cargando
            }

            try {
                val idPropio = tokenManager.getSesion()?.id
                val response = api.usuarios()

                _uiState.value = if (response.isSuccessful) {
                    UsuariosUiState.Listo(response.body().orEmpty(), idPropio)
                } else {
                    UsuariosUiState.Error(ApiErrores.mensaje(response, "No se pudieron cargar los usuarios"))
                }

            } catch (e: Exception) {
                _uiState.value = UsuariosUiState.Error(
                    "No se pudo conectar con el servidor. Comprueba tu conexión."
                )
            }
        }
    }

    fun cambiarBusqueda(texto: String) {
        _busqueda.value = texto
    }

    // Tocar el rol ya seleccionado lo quita.
    fun cambiarRol(rolId: Int?) {
        _rol.value = if (_rol.value == rolId) null else rolId
    }

    fun cambiarEstado(estado: EstadoFiltro) {
        _estado.value = estado
    }

    // PUT /api/usuarios/{id} con el estado contrario (la contraseña no se toca).
    fun cambiarEstadoUsuario(usuario: Usuario) {
        val id = usuario.id ?: return
        val estado = _uiState.value as? UsuariosUiState.Listo ?: return

        if (id == estado.idPropio) {
            _mensaje.value = "No puedes desactivar tu propio usuario"
            return
        }

        viewModelScope.launch {
            try {
                val cambio = UsuarioUtils.conEstadoContrario(usuario)
                val response = api.actualizarUsuario(id, cambio)

                if (response.isSuccessful) {
                    val actualizado = response.body()?.usuario ?: usuario.copy(estado = cambio.estado)
                    val actual = _uiState.value
                    if (actual is UsuariosUiState.Listo) {
                        _uiState.value = actual.copy(
                            usuarios = actual.usuarios.map { if (it.id == id) actualizado else it }
                        )
                    }
                    _mensaje.value =
                        if (cambio.estado == "Inactivo") "Usuario desactivado" else "Usuario activado"
                } else {
                    _mensaje.value = ApiErrores.mensaje(response, "No se pudo cambiar el estado")
                }

            } catch (e: Exception) {
                _mensaje.value = "No se pudo conectar con el servidor."
            }
        }
    }

    // DELETE /api/usuarios/{id}. Si tiene registros relacionados, la API responde 409 con su mensaje.
    fun eliminarUsuario(usuario: Usuario) {
        val id = usuario.id ?: return
        val estado = _uiState.value as? UsuariosUiState.Listo ?: return

        if (id == estado.idPropio) {
            _mensaje.value = "No puedes eliminar tu propio usuario"
            return
        }

        viewModelScope.launch {
            try {
                val response = api.eliminarUsuario(id)

                if (response.isSuccessful) {
                    val actual = _uiState.value
                    if (actual is UsuariosUiState.Listo) {
                        _uiState.value = actual.copy(usuarios = actual.usuarios.filter { it.id != id })
                    }
                    _mensaje.value = "Usuario eliminado"
                } else {
                    _mensaje.value = ApiErrores.mensaje(response, "No se pudo eliminar el usuario")
                }

            } catch (e: Exception) {
                _mensaje.value = "No se pudo conectar con el servidor."
            }
        }
    }

    fun mensajeMostrado() {
        _mensaje.value = null
    }
}
