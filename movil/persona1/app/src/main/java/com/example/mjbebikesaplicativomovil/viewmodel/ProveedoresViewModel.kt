package com.example.mjbebikesaplicativomovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mjbebikesaplicativomovil.remote.InventarioApi
import com.example.mjbebikesaplicativomovil.remote.models.Proveedor
import com.example.mjbebikesaplicativomovil.utils.ApiErrores
import com.example.mjbebikesaplicativomovil.utils.EstadoFiltro
import com.example.mjbebikesaplicativomovil.utils.ProveedorValidacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Las situaciones en las que puede estar la lista de proveedores.
sealed class ProveedoresUiState {
    object Cargando : ProveedoresUiState()
    data class Listo(val proveedores: List<Proveedor>) : ProveedoresUiState()
    data class Error(val mensaje: String) : ProveedoresUiState()
}

class ProveedoresViewModel(
    private val api: InventarioApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProveedoresUiState>(ProveedoresUiState.Cargando)
    val uiState: StateFlow<ProveedoresUiState> = _uiState.asStateFlow()

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    private val _estado = MutableStateFlow(EstadoFiltro.TODOS)
    val estado: StateFlow<EstadoFiltro> = _estado.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    // GET /api/proveedores (requiere sesión).
    // La pantalla la llama cada vez que se muestra (también al volver del formulario).
    fun cargarProveedores() {
        viewModelScope.launch {
            if (_uiState.value !is ProveedoresUiState.Listo) {
                _uiState.value = ProveedoresUiState.Cargando
            }

            try {
                val response = api.proveedores()

                _uiState.value = if (response.isSuccessful) {
                    ProveedoresUiState.Listo(response.body().orEmpty())
                } else {
                    ProveedoresUiState.Error(
                        ApiErrores.mensaje(response, "No se pudieron cargar los proveedores")
                    )
                }

            } catch (e: Exception) {
                _uiState.value = ProveedoresUiState.Error(
                    "No se pudo conectar con el servidor. Comprueba tu conexión."
                )
            }
        }
    }

    fun cambiarBusqueda(texto: String) {
        _busqueda.value = texto
    }

    fun cambiarEstado(estado: EstadoFiltro) {
        _estado.value = estado
    }

    // PUT /api/proveedores/{id} con el estado contrario (activo ↔ inactivo).
    fun cambiarEstadoProveedor(proveedor: Proveedor) {
        val id = proveedor.id ?: return
        val nuevoEstado = if (ProveedorValidacion.esActivo(proveedor)) "inactivo" else "activo"

        viewModelScope.launch {
            try {
                val response = api.actualizarProveedor(id, proveedor.copy(estado = nuevoEstado))

                if (response.isSuccessful) {
                    val actualizado = response.body()?.proveedor ?: proveedor.copy(estado = nuevoEstado)
                    val estado = _uiState.value
                    if (estado is ProveedoresUiState.Listo) {
                        _uiState.value = ProveedoresUiState.Listo(
                            estado.proveedores.map { if (it.id == id) actualizado else it }
                        )
                    }
                    _mensaje.value =
                        if (nuevoEstado == "inactivo") "Proveedor desactivado" else "Proveedor activado"
                } else {
                    _mensaje.value = ApiErrores.mensaje(response, "No se pudo cambiar el estado")
                }

            } catch (e: Exception) {
                _mensaje.value = "No se pudo conectar con el servidor."
            }
        }
    }

    // DELETE /api/proveedores/{id}
    fun eliminarProveedor(proveedor: Proveedor) {
        val id = proveedor.id ?: return

        viewModelScope.launch {
            try {
                val response = api.eliminarProveedor(id)

                if (response.isSuccessful) {
                    val estado = _uiState.value
                    if (estado is ProveedoresUiState.Listo) {
                        _uiState.value = ProveedoresUiState.Listo(estado.proveedores.filter { it.id != id })
                    }
                    _mensaje.value = "Proveedor eliminado"
                } else {
                    _mensaje.value = ApiErrores.mensaje(response, "No se pudo eliminar el proveedor")
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
