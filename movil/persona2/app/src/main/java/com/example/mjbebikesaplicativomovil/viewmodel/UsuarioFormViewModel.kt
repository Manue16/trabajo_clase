package com.example.mjbebikesaplicativomovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mjbebikesaplicativomovil.remote.UsuariosApi
import com.example.mjbebikesaplicativomovil.remote.models.Usuario
import com.example.mjbebikesaplicativomovil.utils.ApiErrores
import com.example.mjbebikesaplicativomovil.utils.UsuarioFormulario
import com.example.mjbebikesaplicativomovil.utils.UsuarioUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Todo lo que muestra el formulario de usuario.
data class UsuarioFormUiState(
    val cargando: Boolean = true,
    val errorCarga: String? = null,
    // Todos los usuarios, para no repetir usuario ni correo.
    val existentes: List<Usuario> = emptyList(),
    val formulario: UsuarioFormulario = UsuarioFormulario(),
    val erroresCampos: Map<String, String> = emptyMap(),
    val guardando: Boolean = false,
    val errorGuardar: String? = null,
    val guardado: Boolean = false
)

// Sirve para crear (idUsuario = null) y para editar (idUsuario = id del usuario).
class UsuarioFormViewModel(
    private val api: UsuariosApi,
    private val idUsuario: Int?
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsuarioFormUiState())
    val uiState: StateFlow<UsuarioFormUiState> = _uiState.asStateFlow()

    val esEdicion: Boolean = idUsuario != null

    init {
        cargarDatos()
    }

    // Carga la lista de usuarios y, si es edición, los datos del usuario.
    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, errorCarga = null)

            try {
                val usuarios = api.usuarios()
                if (!usuarios.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        errorCarga = ApiErrores.mensaje(usuarios, "No se pudieron cargar los usuarios")
                    )
                    return@launch
                }

                var formulario = _uiState.value.formulario

                if (idUsuario != null) {
                    val usuario = api.usuario(idUsuario)
                    val datos = usuario.body()
                    if (!usuario.isSuccessful || datos == null) {
                        _uiState.value = _uiState.value.copy(
                            cargando = false,
                            errorCarga = ApiErrores.mensaje(usuario, "No se pudo cargar el usuario")
                        )
                        return@launch
                    }
                    formulario = UsuarioFormulario.desde(datos)
                }

                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    existentes = usuarios.body().orEmpty(),
                    formulario = formulario
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    errorCarga = "No se pudo conectar con el servidor. Comprueba tu conexión."
                )
            }
        }
    }

    private fun validar(f: UsuarioFormulario): Map<String, String> =
        UsuarioUtils.validar(f, esNuevo = !esEdicion, existentes = _uiState.value.existentes, idActual = idUsuario)

    fun actualizar(nuevo: UsuarioFormulario) {
        val estado = _uiState.value
        _uiState.value = estado.copy(
            formulario = nuevo,
            erroresCampos = if (estado.erroresCampos.isEmpty()) emptyMap() else validar(nuevo),
            errorGuardar = null
        )
    }

    // POST /api/usuarios (crear) o PUT /api/usuarios/{id} (editar).
    fun guardar() {
        val estado = _uiState.value
        val errores = validar(estado.formulario)

        if (errores.isNotEmpty()) {
            _uiState.value = estado.copy(erroresCampos = errores)
            return
        }

        viewModelScope.launch {
            _uiState.value = estado.copy(guardando = true, erroresCampos = emptyMap(), errorGuardar = null)

            try {
                val datos = UsuarioUtils.aGuardar(estado.formulario)
                val response = if (idUsuario == null) {
                    api.crearUsuario(datos)
                } else {
                    api.actualizarUsuario(idUsuario, datos)
                }

                _uiState.value = if (response.isSuccessful) {
                    _uiState.value.copy(guardando = false, guardado = true)
                } else {
                    _uiState.value.copy(
                        guardando = false,
                        errorGuardar = ApiErrores.mensaje(response, "No se pudo guardar el usuario")
                    )
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    errorGuardar = "No se pudo conectar con el servidor."
                )
            }
        }
    }
}
