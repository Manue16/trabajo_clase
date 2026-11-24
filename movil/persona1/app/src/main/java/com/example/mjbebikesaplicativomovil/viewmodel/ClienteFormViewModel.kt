package com.example.mjbebikesaplicativomovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mjbebikesaplicativomovil.remote.InventarioApi
import com.example.mjbebikesaplicativomovil.remote.models.TipoDocumento
import com.example.mjbebikesaplicativomovil.remote.models.UsuarioResumen
import com.example.mjbebikesaplicativomovil.utils.ApiErrores
import com.example.mjbebikesaplicativomovil.utils.ClienteFormulario
import com.example.mjbebikesaplicativomovil.utils.ClienteValidacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Todo lo que muestra el formulario de cliente.
data class ClienteFormUiState(
    val cargando: Boolean = true,
    val errorCarga: String? = null,
    val usuarios: List<UsuarioResumen> = emptyList(),
    val tiposDocumento: List<TipoDocumento> = emptyList(),
    val formulario: ClienteFormulario = ClienteFormulario(),
    val erroresCampos: Map<String, String> = emptyMap(),
    val guardando: Boolean = false,
    val errorGuardar: String? = null,
    val guardado: Boolean = false
)

// Sirve para crear (idCliente = null) y para editar (idCliente = id del cliente).
class ClienteFormViewModel(
    private val api: InventarioApi,
    private val idCliente: Int?
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClienteFormUiState())
    val uiState: StateFlow<ClienteFormUiState> = _uiState.asStateFlow()

    val esEdicion: Boolean = idCliente != null

    init {
        cargarDatos()
    }

    // Carga las listas del formulario y, si es edición, los datos del cliente.
    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, errorCarga = null)

            try {
                val tipos = api.tiposDocumentos()
                val usuarios = api.usuarios()

                if (!tipos.isSuccessful || !usuarios.isSuccessful) {
                    val fallida = if (!tipos.isSuccessful) tipos else usuarios
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        errorCarga = ApiErrores.mensaje(fallida, "No se pudieron cargar las listas")
                    )
                    return@launch
                }

                var formulario = _uiState.value.formulario

                if (idCliente != null) {
                    val cliente = api.cliente(idCliente)
                    val datos = cliente.body()
                    if (!cliente.isSuccessful || datos == null) {
                        _uiState.value = _uiState.value.copy(
                            cargando = false,
                            errorCarga = ApiErrores.mensaje(cliente, "No se pudo cargar el cliente")
                        )
                        return@launch
                    }
                    formulario = ClienteFormulario.desde(datos)
                }

                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    tiposDocumento = tipos.body().orEmpty(),
                    usuarios = usuarios.body().orEmpty(),
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

    // Cada vez que se escribe en un campo. Si ya se intentó guardar,
    // los errores se recalculan al momento.
    fun actualizar(nuevo: ClienteFormulario) {
        val estado = _uiState.value
        _uiState.value = estado.copy(
            formulario = nuevo,
            erroresCampos = if (estado.erroresCampos.isEmpty()) emptyMap()
            else ClienteValidacion.validar(nuevo),
            errorGuardar = null
        )
    }

    // POST /api/clientes (crear) o PUT /api/clientes/{id} (editar).
    fun guardar() {
        val estado = _uiState.value
        val errores = ClienteValidacion.validar(estado.formulario)

        if (errores.isNotEmpty()) {
            _uiState.value = estado.copy(erroresCampos = errores)
            return
        }

        viewModelScope.launch {
            _uiState.value = estado.copy(guardando = true, erroresCampos = emptyMap(), errorGuardar = null)

            try {
                val cliente = estado.formulario.aCliente()
                val response = if (idCliente == null) {
                    api.crearCliente(cliente)
                } else {
                    api.actualizarCliente(idCliente, cliente)
                }

                _uiState.value = if (response.isSuccessful) {
                    _uiState.value.copy(guardando = false, guardado = true)
                } else {
                    _uiState.value.copy(
                        guardando = false,
                        errorGuardar = ApiErrores.mensaje(response, "No se pudo guardar el cliente")
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
