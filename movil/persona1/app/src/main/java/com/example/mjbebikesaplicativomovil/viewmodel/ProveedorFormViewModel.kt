package com.example.mjbebikesaplicativomovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mjbebikesaplicativomovil.remote.InventarioApi
import com.example.mjbebikesaplicativomovil.remote.models.TipoDocumento
import com.example.mjbebikesaplicativomovil.utils.ApiErrores
import com.example.mjbebikesaplicativomovil.utils.ProveedorFormulario
import com.example.mjbebikesaplicativomovil.utils.ProveedorValidacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Todo lo que muestra el formulario de proveedor.
data class ProveedorFormUiState(
    val cargando: Boolean = true,
    val errorCarga: String? = null,
    val tiposDocumento: List<TipoDocumento> = emptyList(),
    val formulario: ProveedorFormulario = ProveedorFormulario(),
    val erroresCampos: Map<String, String> = emptyMap(),
    val guardando: Boolean = false,
    val errorGuardar: String? = null,
    val guardado: Boolean = false
)

// Sirve para crear (idProveedor = null) y para editar (idProveedor = id del proveedor).
class ProveedorFormViewModel(
    private val api: InventarioApi,
    private val idProveedor: Int?
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProveedorFormUiState())
    val uiState: StateFlow<ProveedorFormUiState> = _uiState.asStateFlow()

    val esEdicion: Boolean = idProveedor != null

    init {
        cargarDatos()
    }

    // Carga los tipos de documento y, si es edición, los datos del proveedor.
    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, errorCarga = null)

            try {
                val tipos = api.tiposDocumentos()
                if (!tipos.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        errorCarga = ApiErrores.mensaje(tipos, "No se pudieron cargar los tipos de documento")
                    )
                    return@launch
                }

                var formulario = _uiState.value.formulario

                if (idProveedor != null) {
                    val proveedor = api.proveedor(idProveedor)
                    val datos = proveedor.body()
                    if (!proveedor.isSuccessful || datos == null) {
                        _uiState.value = _uiState.value.copy(
                            cargando = false,
                            errorCarga = ApiErrores.mensaje(proveedor, "No se pudo cargar el proveedor")
                        )
                        return@launch
                    }
                    formulario = ProveedorFormulario.desde(datos)
                }

                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    tiposDocumento = tipos.body().orEmpty(),
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

    fun actualizar(nuevo: ProveedorFormulario) {
        val estado = _uiState.value
        _uiState.value = estado.copy(
            formulario = nuevo,
            erroresCampos = if (estado.erroresCampos.isEmpty()) emptyMap()
            else ProveedorValidacion.validar(nuevo),
            errorGuardar = null
        )
    }

    // POST /api/proveedores (crear) o PUT /api/proveedores/{id} (editar).
    fun guardar() {
        val estado = _uiState.value
        val errores = ProveedorValidacion.validar(estado.formulario)

        if (errores.isNotEmpty()) {
            _uiState.value = estado.copy(erroresCampos = errores)
            return
        }

        viewModelScope.launch {
            _uiState.value = estado.copy(guardando = true, erroresCampos = emptyMap(), errorGuardar = null)

            try {
                val proveedor = estado.formulario.aProveedor()
                val response = if (idProveedor == null) {
                    api.crearProveedor(proveedor)
                } else {
                    api.actualizarProveedor(idProveedor, proveedor)
                }

                _uiState.value = if (response.isSuccessful) {
                    _uiState.value.copy(guardando = false, guardado = true)
                } else {
                    _uiState.value.copy(
                        guardando = false,
                        errorGuardar = ApiErrores.mensaje(response, "No se pudo guardar el proveedor")
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
