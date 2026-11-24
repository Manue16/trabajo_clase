package com.example.mjbebikesaplicativomovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mjbebikesaplicativomovil.data.local.TokenManager
import com.example.mjbebikesaplicativomovil.remote.VentasApi
import com.example.mjbebikesaplicativomovil.remote.models.Cliente
import com.example.mjbebikesaplicativomovil.remote.models.Producto
import com.example.mjbebikesaplicativomovil.utils.ApiErrores
import com.example.mjbebikesaplicativomovil.utils.LineaVenta
import com.example.mjbebikesaplicativomovil.utils.NuevaVentaFormulario
import com.example.mjbebikesaplicativomovil.utils.VentaUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Todo lo que muestra el formulario de nueva venta.
data class NuevaVentaUiState(
    val cargando: Boolean = true,
    val errorCarga: String? = null,
    val clientes: List<Cliente> = emptyList(),
    // Solo productos activos con stock.
    val productos: List<Producto> = emptyList(),
    val formulario: NuevaVentaFormulario = NuevaVentaFormulario(),
    val erroresCampos: Map<String, String> = emptyMap(),
    val guardando: Boolean = false,
    val errorGuardar: String? = null,
    // id de la venta creada: cuando no es null se abre su detalle.
    val idVentaCreada: Int? = null
)

class NuevaVentaViewModel(
    private val api: VentasApi,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NuevaVentaUiState())
    val uiState: StateFlow<NuevaVentaUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    // Carga los clientes y los productos que se pueden vender.
    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, errorCarga = null)

            try {
                val clientes = api.clientes()
                val productos = api.productos()

                val fallida = listOf(clientes, productos).firstOrNull { !it.isSuccessful }
                if (fallida != null) {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        errorCarga = ApiErrores.mensaje(fallida, "No se pudieron cargar las listas")
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    clientes = clientes.body().orEmpty(),
                    productos = VentaUtils.vendibles(productos.body().orEmpty())
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    errorCarga = "No se pudo conectar con el servidor. Comprueba tu conexión."
                )
            }
        }
    }

    // ---------- Cambios en el formulario ----------

    private fun actualizar(nuevo: NuevaVentaFormulario) {
        val estado = _uiState.value
        _uiState.value = estado.copy(
            formulario = nuevo,
            erroresCampos = if (estado.erroresCampos.isEmpty()) emptyMap()
            else VentaUtils.validar(nuevo, estado.productos),
            errorGuardar = null
        )
    }

    fun cambiarCliente(id: Int?) = actualizar(_uiState.value.formulario.copy(idCliente = id))

    fun cambiarFecha(fecha: String) = actualizar(_uiState.value.formulario.copy(fecha = fecha))

    fun cambiarLinea(indice: Int, linea: LineaVenta) {
        val lineas = _uiState.value.formulario.lineas.toMutableList()
        if (indice in lineas.indices) {
            lineas[indice] = linea
            actualizar(_uiState.value.formulario.copy(lineas = lineas))
        }
    }

    fun agregarLinea() {
        val f = _uiState.value.formulario
        actualizar(f.copy(lineas = f.lineas + LineaVenta()))
    }

    // Siempre queda al menos una fila.
    fun quitarLinea(indice: Int) {
        val f = _uiState.value.formulario
        if (f.lineas.size > 1 && indice in f.lineas.indices) {
            actualizar(f.copy(lineas = f.lineas.filterIndexed { i, _ -> i != indice }))
        }
    }

    // POST /api/ventas: la API crea la venta y sus detalles y descuenta el stock.
    fun guardar() {
        val estado = _uiState.value
        val errores = VentaUtils.validar(estado.formulario, estado.productos)

        if (errores.isNotEmpty()) {
            _uiState.value = estado.copy(erroresCampos = errores)
            return
        }

        viewModelScope.launch {
            _uiState.value = estado.copy(guardando = true, erroresCampos = emptyMap(), errorGuardar = null)

            try {
                val sesion = tokenManager.getSesion()
                if (sesion == null) {
                    _uiState.value = _uiState.value.copy(guardando = false, errorGuardar = ApiErrores.SESION_VENCIDA)
                    return@launch
                }

                val venta = VentaUtils.armarVenta(sesion.id, estado.formulario, estado.productos)
                val response = api.crearVenta(venta)

                _uiState.value = if (response.isSuccessful) {
                    _uiState.value.copy(
                        guardando = false,
                        idVentaCreada = response.body()?.venta?.id ?: 0
                    )
                } else {
                    _uiState.value.copy(
                        guardando = false,
                        errorGuardar = ApiErrores.mensaje(response, "No se pudo registrar la venta")
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
