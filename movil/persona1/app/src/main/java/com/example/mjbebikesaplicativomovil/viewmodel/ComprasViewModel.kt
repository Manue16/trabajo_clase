package com.example.mjbebikesaplicativomovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mjbebikesaplicativomovil.remote.InventarioApi
import com.example.mjbebikesaplicativomovil.remote.models.Compra
import com.example.mjbebikesaplicativomovil.remote.models.DetalleCompra
import com.example.mjbebikesaplicativomovil.utils.ApiErrores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ---------------------------------------------------------------- Lista de compras

sealed class ComprasUiState {
    object Cargando : ComprasUiState()
    data class Listo(val compras: List<Compra>) : ComprasUiState()
    data class Error(val mensaje: String) : ComprasUiState()
}

class ComprasViewModel(
    private val api: InventarioApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<ComprasUiState>(ComprasUiState.Cargando)
    val uiState: StateFlow<ComprasUiState> = _uiState.asStateFlow()

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    // GET /api/compras (incluye el proveedor; la API las devuelve de la más nueva a la más antigua).
    // La pantalla la llama cada vez que se muestra (también al volver de crear una compra).
    fun cargarCompras() {
        viewModelScope.launch {
            if (_uiState.value !is ComprasUiState.Listo) {
                _uiState.value = ComprasUiState.Cargando
            }

            try {
                val response = api.compras()

                _uiState.value = if (response.isSuccessful) {
                    ComprasUiState.Listo(response.body().orEmpty())
                } else {
                    ComprasUiState.Error(
                        ApiErrores.mensaje(response, "No fue posible cargar el historial de compras")
                    )
                }

            } catch (e: Exception) {
                _uiState.value = ComprasUiState.Error(
                    "No se pudo conectar con el servidor. Comprueba tu conexión."
                )
            }
        }
    }

    fun cambiarBusqueda(texto: String) {
        _busqueda.value = texto
    }
}

// ---------------------------------------------------------------- Detalle de una compra

sealed class DetalleCompraUiState {
    object Cargando : DetalleCompraUiState()
    data class Listo(val compra: Compra, val detalles: List<DetalleCompra>) : DetalleCompraUiState()
    data class Error(val mensaje: String) : DetalleCompraUiState()
}

class DetalleCompraViewModel(
    private val api: InventarioApi,
    private val idCompra: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetalleCompraUiState>(DetalleCompraUiState.Cargando)
    val uiState: StateFlow<DetalleCompraUiState> = _uiState.asStateFlow()

    init {
        cargarDetalle()
    }

    // GET /api/compras/{id} y GET /api/detalles_compra?id_compra={id}
    fun cargarDetalle() {
        viewModelScope.launch {
            _uiState.value = DetalleCompraUiState.Cargando

            try {
                val compra = api.compra(idCompra)
                val datosCompra = compra.body()
                if (!compra.isSuccessful || datosCompra == null) {
                    _uiState.value = DetalleCompraUiState.Error(
                        ApiErrores.mensaje(compra, "No se pudo cargar la compra")
                    )
                    return@launch
                }

                val detalles = api.detallesCompra(idCompra)
                if (!detalles.isSuccessful) {
                    _uiState.value = DetalleCompraUiState.Error(
                        ApiErrores.mensaje(detalles, "No se pudieron cargar los productos de la compra")
                    )
                    return@launch
                }

                _uiState.value = DetalleCompraUiState.Listo(datosCompra, detalles.body().orEmpty())

            } catch (e: Exception) {
                _uiState.value = DetalleCompraUiState.Error(
                    "No se pudo conectar con el servidor. Comprueba tu conexión."
                )
            }
        }
    }
}
