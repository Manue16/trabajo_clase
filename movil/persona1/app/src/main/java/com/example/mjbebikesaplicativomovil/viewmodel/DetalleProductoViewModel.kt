package com.example.mjbebikesaplicativomovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mjbebikesaplicativomovil.remote.InventarioApi
import com.example.mjbebikesaplicativomovil.remote.models.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Las situaciones en las que puede estar la pantalla de detalle.
sealed class DetalleProductoUiState {
    object Cargando : DetalleProductoUiState()
    data class Listo(val producto: Producto) : DetalleProductoUiState()
    data class Error(val mensaje: String) : DetalleProductoUiState()
}

class DetalleProductoViewModel(
    private val api: InventarioApi,
    private val idProducto: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetalleProductoUiState>(DetalleProductoUiState.Cargando)
    val uiState: StateFlow<DetalleProductoUiState> = _uiState.asStateFlow()

    init {
        cargarProducto()
    }

    // Consulta GET /api/productos/{id} (ruta pública, no necesita sesión).
    fun cargarProducto() {
        viewModelScope.launch {
            _uiState.value = DetalleProductoUiState.Cargando

            try {
                val response = api.producto(idProducto)
                val producto = response.body()

                _uiState.value = when {
                    response.isSuccessful && producto != null ->
                        DetalleProductoUiState.Listo(producto)
                    response.code() == 404 ->
                        DetalleProductoUiState.Error("Producto no encontrado")
                    else ->
                        DetalleProductoUiState.Error("No se pudo cargar el producto (${response.code()})")
                }

            } catch (e: Exception) {
                _uiState.value = DetalleProductoUiState.Error(
                    "No se pudo conectar con el servidor. Comprueba tu conexión."
                )
            }
        }
    }
}
