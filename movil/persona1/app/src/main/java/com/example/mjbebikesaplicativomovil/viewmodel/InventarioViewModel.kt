package com.example.mjbebikesaplicativomovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mjbebikesaplicativomovil.remote.InventarioApi
import com.example.mjbebikesaplicativomovil.remote.models.Producto
import com.example.mjbebikesaplicativomovil.utils.ApiErrores
import com.example.mjbebikesaplicativomovil.utils.EstadoFiltro
import com.example.mjbebikesaplicativomovil.utils.InventarioUtils
import com.example.mjbebikesaplicativomovil.utils.OrdenCantidad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Las situaciones en las que puede estar la lista del inventario.
sealed class InventarioUiState {
    object Cargando : InventarioUiState()
    data class Listo(val productos: List<Producto>) : InventarioUiState()
    data class Error(val mensaje: String) : InventarioUiState()
}

class InventarioViewModel(
    private val api: InventarioApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<InventarioUiState>(InventarioUiState.Cargando)
    val uiState: StateFlow<InventarioUiState> = _uiState.asStateFlow()

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    private val _estado = MutableStateFlow(EstadoFiltro.TODOS)
    val estado: StateFlow<EstadoFiltro> = _estado.asStateFlow()

    private val _soloStockBajo = MutableStateFlow(false)
    val soloStockBajo: StateFlow<Boolean> = _soloStockBajo.asStateFlow()

    private val _orden = MutableStateFlow(OrdenCantidad.NINGUNO)
    val orden: StateFlow<OrdenCantidad> = _orden.asStateFlow()

    // Mensaje corto para mostrar una vez ("Producto desactivado", errores...).
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    // GET /api/productos (incluye activos e inactivos).
    // La pantalla la llama cada vez que se muestra (también al volver del formulario).
    fun cargarProductos() {
        viewModelScope.launch {
            if (_uiState.value !is InventarioUiState.Listo) {
                _uiState.value = InventarioUiState.Cargando
            }

            try {
                val response = api.productos()

                _uiState.value = if (response.isSuccessful) {
                    InventarioUiState.Listo(response.body().orEmpty())
                } else {
                    InventarioUiState.Error(
                        ApiErrores.mensaje(response, "No se pudo cargar el inventario")
                    )
                }

            } catch (e: Exception) {
                _uiState.value = InventarioUiState.Error(
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

    fun cambiarSoloStockBajo() {
        _soloStockBajo.value = !_soloStockBajo.value
    }

    // Tocar el orden ya seleccionado lo quita.
    fun cambiarOrden(orden: OrdenCantidad) {
        _orden.value = if (_orden.value == orden) OrdenCantidad.NINGUNO else orden
    }

    fun limpiarFiltros() {
        _busqueda.value = ""
        _estado.value = EstadoFiltro.TODOS
        _soloStockBajo.value = false
        _orden.value = OrdenCantidad.NINGUNO
    }

    // PUT /api/productos/{id} con el estado contrario (activo ↔ inactivo).
    fun cambiarEstadoProducto(producto: Producto) {
        val id = producto.id ?: return
        val nuevoEstado = if (InventarioUtils.esActivo(producto)) "inactivo" else "activo"

        viewModelScope.launch {
            try {
                val response = api.actualizarProducto(id, producto.copy(estado = nuevoEstado))

                if (response.isSuccessful) {
                    val actualizado = response.body()?.producto ?: producto.copy(estado = nuevoEstado)
                    reemplazarEnLista(actualizado)
                    _mensaje.value =
                        if (nuevoEstado == "inactivo") "Producto desactivado" else "Producto activado"
                } else {
                    _mensaje.value = ApiErrores.mensaje(response, "No se pudo cambiar el estado")
                }

            } catch (e: Exception) {
                _mensaje.value = "No se pudo conectar con el servidor."
            }
        }
    }

    // DELETE /api/productos/{id}
    fun eliminarProducto(producto: Producto) {
        val id = producto.id ?: return

        viewModelScope.launch {
            try {
                val response = api.eliminarProducto(id)

                if (response.isSuccessful) {
                    val estado = _uiState.value
                    if (estado is InventarioUiState.Listo) {
                        _uiState.value = InventarioUiState.Listo(estado.productos.filter { it.id != id })
                    }
                    _mensaje.value = "Producto eliminado"
                } else {
                    _mensaje.value = ApiErrores.mensaje(response, "No se pudo eliminar el producto")
                }

            } catch (e: Exception) {
                _mensaje.value = "No se pudo conectar con el servidor."
            }
        }
    }

    fun mensajeMostrado() {
        _mensaje.value = null
    }

    private fun reemplazarEnLista(producto: Producto) {
        val estado = _uiState.value
        if (estado is InventarioUiState.Listo) {
            _uiState.value = InventarioUiState.Listo(
                estado.productos.map { if (it.id == producto.id) producto else it }
            )
        }
    }
}
