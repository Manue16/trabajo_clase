package com.example.mjbebikesaplicativomovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mjbebikesaplicativomovil.remote.InventarioApi
import com.example.mjbebikesaplicativomovil.remote.models.Cliente
import com.example.mjbebikesaplicativomovil.utils.ApiErrores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Las situaciones en las que puede estar la lista de clientes.
sealed class ClientesUiState {
    object Cargando : ClientesUiState()
    data class Listo(val clientes: List<Cliente>) : ClientesUiState()
    data class Error(val mensaje: String) : ClientesUiState()
}

class ClientesViewModel(
    private val api: InventarioApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<ClientesUiState>(ClientesUiState.Cargando)
    val uiState: StateFlow<ClientesUiState> = _uiState.asStateFlow()

    // Texto del buscador.
    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    // Mensaje corto para mostrar una vez (por ejemplo, "Cliente eliminado").
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    // Consulta GET /api/clientes (requiere sesión).
    // La pantalla la llama cada vez que se muestra, así la lista queda al día
    // al volver del formulario.
    fun cargarClientes() {
        viewModelScope.launch {
            // Solo mostramos "cargando" la primera vez; después se actualiza sin parpadeo.
            if (_uiState.value !is ClientesUiState.Listo) {
                _uiState.value = ClientesUiState.Cargando
            }

            try {
                val response = api.clientes()

                _uiState.value = if (response.isSuccessful) {
                    ClientesUiState.Listo(response.body().orEmpty())
                } else {
                    ClientesUiState.Error(
                        ApiErrores.mensaje(response, "No se pudieron cargar los clientes")
                    )
                }

            } catch (e: Exception) {
                _uiState.value = ClientesUiState.Error(
                    "No se pudo conectar con el servidor. Comprueba tu conexión."
                )
            }
        }
    }

    fun cambiarBusqueda(texto: String) {
        _busqueda.value = texto
    }

    // DELETE /api/clientes/{id}. Si sale bien, lo quita de la lista sin volver a consultar.
    fun eliminarCliente(cliente: Cliente) {
        val id = cliente.id ?: return

        viewModelScope.launch {
            try {
                val response = api.eliminarCliente(id)

                if (response.isSuccessful) {
                    val estado = _uiState.value
                    if (estado is ClientesUiState.Listo) {
                        _uiState.value = ClientesUiState.Listo(estado.clientes.filter { it.id != id })
                    }
                    _mensaje.value = "Cliente eliminado"
                } else {
                    _mensaje.value = ApiErrores.mensaje(response, "No se pudo eliminar el cliente")
                }

            } catch (e: Exception) {
                _mensaje.value = "No se pudo conectar con el servidor."
            }
        }
    }

    // La pantalla llama a esto después de mostrar el mensaje.
    fun mensajeMostrado() {
        _mensaje.value = null
    }
}
