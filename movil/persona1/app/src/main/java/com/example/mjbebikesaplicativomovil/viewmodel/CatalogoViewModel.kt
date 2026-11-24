package com.example.mjbebikesaplicativomovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mjbebikesaplicativomovil.remote.InventarioApi
import com.example.mjbebikesaplicativomovil.remote.models.Producto
import com.example.mjbebikesaplicativomovil.utils.CatalogoFiltro
import com.example.mjbebikesaplicativomovil.utils.OrdenPrecio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Las situaciones en las que puede estar la pantalla del catálogo.
sealed class CatalogoUiState {
    object Cargando : CatalogoUiState()
    data class Listo(
        val productos: List<Producto>,
        val marcas: List<String>
    ) : CatalogoUiState()
    data class Error(val mensaje: String) : CatalogoUiState()
}

class CatalogoViewModel(
    private val api: InventarioApi
) : ViewModel() {

    // Estado de la carga de productos.
    private val _uiState = MutableStateFlow<CatalogoUiState>(CatalogoUiState.Cargando)
    val uiState: StateFlow<CatalogoUiState> = _uiState.asStateFlow()

    // Texto escrito en el buscador.
    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    // Marca seleccionada (null = todas las marcas).
    private val _marca = MutableStateFlow<String?>(null)
    val marca: StateFlow<String?> = _marca.asStateFlow()

    // Orden por precio seleccionado.
    private val _orden = MutableStateFlow(OrdenPrecio.NINGUNO)
    val orden: StateFlow<OrdenPrecio> = _orden.asStateFlow()

    init {
        cargarProductos()
    }

    // Consulta GET /api/productos (ruta pública, no necesita sesión).
    fun cargarProductos() {
        viewModelScope.launch {
            _uiState.value = CatalogoUiState.Cargando

            try {
                val response = api.productos()

                if (response.isSuccessful) {
                    val activos = CatalogoFiltro.soloActivos(response.body().orEmpty())
                    _uiState.value = CatalogoUiState.Listo(
                        productos = activos,
                        marcas = CatalogoFiltro.marcas(activos)
                    )
                } else {
                    _uiState.value = CatalogoUiState.Error(
                        "No se pudieron cargar los productos (${response.code()})"
                    )
                }

            } catch (e: Exception) {
                _uiState.value = CatalogoUiState.Error(
                    "No se pudo conectar con el servidor. Comprueba tu conexión."
                )
            }
        }
    }

    fun cambiarBusqueda(texto: String) {
        _busqueda.value = texto
    }

    // Tocar la marca ya seleccionada la quita.
    fun cambiarMarca(marca: String?) {
        _marca.value = if (_marca.value == marca) null else marca
    }

    // Tocar el orden ya seleccionado lo quita.
    fun cambiarOrden(orden: OrdenPrecio) {
        _orden.value = if (_orden.value == orden) OrdenPrecio.NINGUNO else orden
    }

    fun limpiarFiltros() {
        _busqueda.value = ""
        _marca.value = null
        _orden.value = OrdenPrecio.NINGUNO
    }
}
