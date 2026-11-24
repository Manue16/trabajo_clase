package com.example.mjbebikesaplicativomovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mjbebikesaplicativomovil.remote.InventarioApi
import com.example.mjbebikesaplicativomovil.remote.models.Producto
import com.example.mjbebikesaplicativomovil.remote.models.Proveedor
import com.example.mjbebikesaplicativomovil.utils.ApiErrores
import com.example.mjbebikesaplicativomovil.utils.CompraFormulario
import com.example.mjbebikesaplicativomovil.utils.CompraUtils
import com.example.mjbebikesaplicativomovil.utils.LineaCompra
import com.example.mjbebikesaplicativomovil.utils.ProveedorValidacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Todo lo que muestra el formulario de compra.
data class CompraFormUiState(
    val cargando: Boolean = true,
    val errorCarga: String? = null,
    val proveedores: List<Proveedor> = emptyList(),
    val productos: List<Producto> = emptyList(),
    val formulario: CompraFormulario = CompraFormulario(),
    val erroresCampos: Map<String, String> = emptyMap(),
    val guardando: Boolean = false,
    val errorGuardar: String? = null,
    val guardado: Boolean = false
)

class CompraFormViewModel(
    private val api: InventarioApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompraFormUiState())
    val uiState: StateFlow<CompraFormUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    // Carga los proveedores activos y los productos para las listas.
    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, errorCarga = null)

            try {
                val proveedores = api.proveedores()
                val productos = api.productos()

                val fallida = listOf(proveedores, productos).firstOrNull { !it.isSuccessful }
                if (fallida != null) {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        errorCarga = ApiErrores.mensaje(fallida, "No se pudieron cargar las listas")
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    proveedores = proveedores.body().orEmpty().filter { ProveedorValidacion.esActivo(it) },
                    productos = productos.body().orEmpty()
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

    private fun actualizar(nuevo: CompraFormulario) {
        val estado = _uiState.value
        _uiState.value = estado.copy(
            formulario = nuevo,
            erroresCampos = if (estado.erroresCampos.isEmpty()) emptyMap() else CompraUtils.validar(nuevo),
            errorGuardar = null
        )
    }

    fun cambiarProveedor(id: Int?) = actualizar(_uiState.value.formulario.copy(idProveedor = id))

    fun cambiarFecha(fecha: String) = actualizar(_uiState.value.formulario.copy(fecha = fecha))

    fun cambiarPago(pago: Boolean) = actualizar(_uiState.value.formulario.copy(pago = pago))

    fun cambiarLinea(indice: Int, linea: LineaCompra) {
        val lineas = _uiState.value.formulario.lineas.toMutableList()
        if (indice in lineas.indices) {
            lineas[indice] = linea
            actualizar(_uiState.value.formulario.copy(lineas = lineas))
        }
    }

    fun agregarLinea() {
        val f = _uiState.value.formulario
        actualizar(f.copy(lineas = f.lineas + LineaCompra()))
    }

    // Siempre queda al menos una fila.
    fun quitarLinea(indice: Int) {
        val f = _uiState.value.formulario
        if (f.lineas.size > 1 && indice in f.lineas.indices) {
            actualizar(f.copy(lineas = f.lineas.filterIndexed { i, _ -> i != indice }))
        }
    }

    // ---------- Guardar ----------

    // 1) POST /api/compras con el proveedor, la fecha, el total y el estado de pago.
    // 2) POST /api/detalles_compra por cada producto (la API suma esa cantidad al stock).
    fun guardar() {
        val estado = _uiState.value
        val errores = CompraUtils.validar(estado.formulario)

        if (errores.isNotEmpty()) {
            _uiState.value = estado.copy(erroresCampos = errores)
            return
        }

        viewModelScope.launch {
            _uiState.value = estado.copy(guardando = true, erroresCampos = emptyMap(), errorGuardar = null)

            try {
                val respuestaCompra = api.crearCompra(estado.formulario.aCompraNueva())
                val idCompra = respuestaCompra.body()?.id

                if (!respuestaCompra.isSuccessful || idCompra == null) {
                    _uiState.value = _uiState.value.copy(
                        guardando = false,
                        errorGuardar = ApiErrores.mensaje(respuestaCompra, "No se pudo crear la compra")
                    )
                    return@launch
                }

                // Se crean los detalles uno por uno para saber cuál falló, si alguno falla.
                for (detalle in estado.formulario.aDetalles(idCompra)) {
                    val respuestaDetalle = api.crearDetalleCompra(detalle)
                    if (!respuestaDetalle.isSuccessful) {
                        val producto = estado.productos.find { it.id == detalle.idProducto }
                        _uiState.value = _uiState.value.copy(
                            guardando = false,
                            errorGuardar = "La compra #$idCompra se creó, pero no se pudo registrar " +
                                    "\"${producto?.nombreProducto ?: "un producto"}\": " +
                                    ApiErrores.mensaje(respuestaDetalle, "error en el detalle")
                        )
                        return@launch
                    }
                }

                _uiState.value = _uiState.value.copy(guardando = false, guardado = true)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    errorGuardar = "No se pudo conectar con el servidor."
                )
            }
        }
    }
}
