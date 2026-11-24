package com.example.mjbebikesaplicativomovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mjbebikesaplicativomovil.remote.InventarioApi
import com.example.mjbebikesaplicativomovil.remote.models.Local
import com.example.mjbebikesaplicativomovil.remote.models.Proveedor
import com.example.mjbebikesaplicativomovil.remote.models.Talla
import com.example.mjbebikesaplicativomovil.utils.ApiErrores
import com.example.mjbebikesaplicativomovil.utils.InventarioUtils
import com.example.mjbebikesaplicativomovil.utils.ProductoFormulario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Todo lo que muestra el formulario de producto.
data class ProductoFormUiState(
    val cargando: Boolean = true,
    val errorCarga: String? = null,
    val tallas: List<Talla> = emptyList(),
    val proveedores: List<Proveedor> = emptyList(),
    val locales: List<Local> = emptyList(),
    val formulario: ProductoFormulario = ProductoFormulario(),
    val erroresCampos: Map<String, String> = emptyMap(),
    val guardando: Boolean = false,
    val errorGuardar: String? = null,
    val guardado: Boolean = false
)

// Sirve para crear (idProducto = null) y para editar (idProducto = id del producto).
class ProductoFormViewModel(
    private val api: InventarioApi,
    private val idProducto: Int?
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductoFormUiState())
    val uiState: StateFlow<ProductoFormUiState> = _uiState.asStateFlow()

    val esEdicion: Boolean = idProducto != null

    init {
        cargarDatos()
    }

    // Carga las listas (tallas, proveedores, locales) y, si es edición, el producto.
    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, errorCarga = null)

            try {
                val tallas = api.tallas()
                val proveedores = api.proveedores()
                val locales = api.locales()

                val fallida = listOf(tallas, proveedores, locales).firstOrNull { !it.isSuccessful }
                if (fallida != null) {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        errorCarga = ApiErrores.mensaje(fallida, "No se pudieron cargar las listas")
                    )
                    return@launch
                }

                var formulario = _uiState.value.formulario

                if (idProducto != null) {
                    val producto = api.producto(idProducto)
                    val datos = producto.body()
                    if (!producto.isSuccessful || datos == null) {
                        _uiState.value = _uiState.value.copy(
                            cargando = false,
                            errorCarga = ApiErrores.mensaje(producto, "No se pudo cargar el producto")
                        )
                        return@launch
                    }
                    formulario = ProductoFormulario.desde(datos)
                }

                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    tallas = tallas.body().orEmpty(),
                    proveedores = proveedores.body().orEmpty(),
                    locales = locales.body().orEmpty(),
                    formulario = formulario
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    errorCarga = "No se pudo conectar con el servidor. Comprueba tu conexión."
                )
            }
        }
    }

    fun actualizar(nuevo: ProductoFormulario) {
        val estado = _uiState.value
        _uiState.value = estado.copy(
            formulario = nuevo,
            erroresCampos = if (estado.erroresCampos.isEmpty()) emptyMap()
            else InventarioUtils.validar(nuevo),
            errorGuardar = null
        )
    }

    // POST /api/productos (crear) o PUT /api/productos/{id} (editar).
    fun guardar() {
        val estado = _uiState.value
        val errores = InventarioUtils.validar(estado.formulario)

        if (errores.isNotEmpty()) {
            _uiState.value = estado.copy(erroresCampos = errores)
            return
        }

        viewModelScope.launch {
            _uiState.value = estado.copy(guardando = true, erroresCampos = emptyMap(), errorGuardar = null)

            try {
                val producto = estado.formulario.aProducto()
                val response = if (idProducto == null) {
                    api.crearProducto(producto)
                } else {
                    api.actualizarProducto(idProducto, producto)
                }

                _uiState.value = if (response.isSuccessful) {
                    _uiState.value.copy(guardando = false, guardado = true)
                } else {
                    _uiState.value.copy(
                        guardando = false,
                        errorGuardar = ApiErrores.mensaje(response, "No se pudo guardar el producto")
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
