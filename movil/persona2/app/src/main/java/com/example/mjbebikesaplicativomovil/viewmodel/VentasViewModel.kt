package com.example.mjbebikesaplicativomovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mjbebikesaplicativomovil.data.local.TokenManager
import com.example.mjbebikesaplicativomovil.remote.VentasApi
import com.example.mjbebikesaplicativomovil.remote.models.Venta
import com.example.mjbebikesaplicativomovil.utils.ApiErrores
import com.example.mjbebikesaplicativomovil.utils.Roles
import com.example.mjbebikesaplicativomovil.utils.VentaUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ---------------------------------------------------------------- Historial de ventas

sealed class VentasUiState {
    object Cargando : VentasUiState()
    data class Listo(val ventas: List<Venta>, val esAdmin: Boolean) : VentasUiState()
    data class Error(val mensaje: String) : VentasUiState()
}

class VentasViewModel(
    private val api: VentasApi,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<VentasUiState>(VentasUiState.Cargando)
    val uiState: StateFlow<VentasUiState> = _uiState.asStateFlow()

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    // GET /api/ventas. El administrador ve todas; el vendedor, solo las que registró él.
    // La pantalla la llama cada vez que se muestra (también al volver de registrar una venta).
    fun cargarVentas() {
        viewModelScope.launch {
            if (_uiState.value !is VentasUiState.Listo) {
                _uiState.value = VentasUiState.Cargando
            }

            try {
                val sesion = tokenManager.getSesion()
                if (sesion == null) {
                    _uiState.value = VentasUiState.Error(ApiErrores.SESION_VENCIDA)
                    return@launch
                }

                val response = api.ventas()

                _uiState.value = if (response.isSuccessful) {
                    VentasUiState.Listo(
                        ventas = VentaUtils.ventasDe(response.body().orEmpty(), sesion),
                        esAdmin = sesion.rolId == Roles.ADMIN
                    )
                } else {
                    VentasUiState.Error(ApiErrores.mensaje(response, "No se pudieron cargar las ventas"))
                }

            } catch (e: Exception) {
                _uiState.value = VentasUiState.Error(
                    "No se pudo conectar con el servidor. Comprueba tu conexión."
                )
            }
        }
    }

    fun cambiarBusqueda(texto: String) {
        _busqueda.value = texto
    }
}

// ---------------------------------------------------------------- Detalle de una venta

sealed class DetalleVentaUiState {
    object Cargando : DetalleVentaUiState()
    data class Listo(val venta: Venta) : DetalleVentaUiState()
    data class Error(val mensaje: String) : DetalleVentaUiState()
}

class DetalleVentaViewModel(
    private val api: VentasApi,
    private val idVenta: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetalleVentaUiState>(DetalleVentaUiState.Cargando)
    val uiState: StateFlow<DetalleVentaUiState> = _uiState.asStateFlow()

    init {
        cargarDetalle()
    }

    // GET /api/ventas/{id} (incluye cliente, vendedor y productos).
    fun cargarDetalle() {
        viewModelScope.launch {
            _uiState.value = DetalleVentaUiState.Cargando

            try {
                val response = api.venta(idVenta)
                val venta = response.body()

                _uiState.value = if (response.isSuccessful && venta != null) {
                    DetalleVentaUiState.Listo(venta)
                } else {
                    DetalleVentaUiState.Error(ApiErrores.mensaje(response, "No se pudo cargar la venta"))
                }

            } catch (e: Exception) {
                _uiState.value = DetalleVentaUiState.Error(
                    "No se pudo conectar con el servidor. Comprueba tu conexión."
                )
            }
        }
    }
}
