package com.example.mjbebikesaplicativomovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mjbebikesaplicativomovil.data.local.TokenManager
import com.example.mjbebikesaplicativomovil.remote.VentasApi
import com.example.mjbebikesaplicativomovil.remote.models.Dashboard
import com.example.mjbebikesaplicativomovil.utils.ApiErrores
import com.example.mjbebikesaplicativomovil.utils.CompraUtils
import com.example.mjbebikesaplicativomovil.utils.PanelUtils
import com.example.mjbebikesaplicativomovil.utils.ResumenBodega
import com.example.mjbebikesaplicativomovil.utils.ResumenVendedor
import com.example.mjbebikesaplicativomovil.utils.Roles
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Cada rol tiene su propio panel.
sealed class PanelUiState {
    object Cargando : PanelUiState()
    data class Admin(val dashboard: Dashboard) : PanelUiState()
    data class Bodega(val resumen: ResumenBodega) : PanelUiState()
    data class Vendedor(val resumen: ResumenVendedor) : PanelUiState()
    data class Error(val mensaje: String) : PanelUiState()
}

class PanelViewModel(
    private val api: VentasApi,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<PanelUiState>(PanelUiState.Cargando)
    val uiState: StateFlow<PanelUiState> = _uiState.asStateFlow()

    // Carga los datos del panel según el rol de quien inició sesión:
    //   administrador → GET /reportes/dashboard
    //   bodeguero     → GET /productos (alertas de stock)
    //   vendedor      → GET /ventas (solo las suyas)
    // La pantalla la llama cada vez que se muestra, así los números quedan al día.
    fun cargarPanel() {
        viewModelScope.launch {
            val estado = _uiState.value
            if (estado is PanelUiState.Cargando || estado is PanelUiState.Error) {
                _uiState.value = PanelUiState.Cargando
            }

            try {
                val sesion = tokenManager.getSesion()
                if (sesion == null) {
                    _uiState.value = PanelUiState.Error(ApiErrores.SESION_VENCIDA)
                    return@launch
                }

                _uiState.value = when (sesion.rolId) {

                    Roles.ADMIN -> {
                        val response = api.dashboard()
                        val datos = response.body()
                        if (response.isSuccessful && datos != null) PanelUiState.Admin(datos)
                        else PanelUiState.Error(ApiErrores.mensaje(response, "No se pudo cargar el resumen"))
                    }

                    Roles.BODEGUERO -> {
                        val response = api.productos()
                        if (response.isSuccessful) {
                            PanelUiState.Bodega(PanelUtils.resumenBodega(response.body().orEmpty()))
                        } else {
                            PanelUiState.Error(ApiErrores.mensaje(response, "No se pudo cargar el inventario"))
                        }
                    }

                    Roles.VENDEDOR -> {
                        val response = api.ventas()
                        if (response.isSuccessful) {
                            PanelUiState.Vendedor(
                                PanelUtils.resumenVendedor(response.body().orEmpty(), sesion.id, CompraUtils.hoy())
                            )
                        } else {
                            PanelUiState.Error(ApiErrores.mensaje(response, "No se pudieron cargar tus ventas"))
                        }
                    }

                    else -> PanelUiState.Error("Tu rol no tiene panel.")
                }

            } catch (e: Exception) {
                _uiState.value = PanelUiState.Error(
                    "No se pudo conectar con el servidor. Comprueba tu conexión."
                )
            }
        }
    }
}
