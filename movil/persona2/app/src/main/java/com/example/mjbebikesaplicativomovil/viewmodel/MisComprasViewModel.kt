package com.example.mjbebikesaplicativomovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mjbebikesaplicativomovil.data.local.TokenManager
import com.example.mjbebikesaplicativomovil.remote.VentasApi
import com.example.mjbebikesaplicativomovil.remote.models.Venta
import com.example.mjbebikesaplicativomovil.utils.ApiErrores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Las situaciones en las que puede estar Mis compras.
sealed class MisComprasUiState {
    object Cargando : MisComprasUiState()
    data class Listo(val compras: List<Venta>) : MisComprasUiState()
    data class Error(val mensaje: String) : MisComprasUiState()
}

class MisComprasViewModel(
    private val api: VentasApi,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<MisComprasUiState>(MisComprasUiState.Cargando)
    val uiState: StateFlow<MisComprasUiState> = _uiState.asStateFlow()

    // GET /api/ventas?user_id={id del usuario}: solo las compras de quien inició sesión.
    // La pantalla la llama cada vez que se muestra.
    fun cargarCompras() {
        viewModelScope.launch {
            if (_uiState.value !is MisComprasUiState.Listo) {
                _uiState.value = MisComprasUiState.Cargando
            }

            try {
                val usuario = tokenManager.getSesion()
                if (usuario == null) {
                    _uiState.value = MisComprasUiState.Error(ApiErrores.SESION_VENCIDA)
                    return@launch
                }

                val response = api.ventas(userId = usuario.id)

                _uiState.value = if (response.isSuccessful) {
                    MisComprasUiState.Listo(response.body().orEmpty())
                } else {
                    MisComprasUiState.Error(
                        ApiErrores.mensaje(response, "No se pudieron cargar tus compras")
                    )
                }

            } catch (e: Exception) {
                _uiState.value = MisComprasUiState.Error(
                    "No se pudo conectar con el servidor. Comprueba tu conexión."
                )
            }
        }
    }
}
