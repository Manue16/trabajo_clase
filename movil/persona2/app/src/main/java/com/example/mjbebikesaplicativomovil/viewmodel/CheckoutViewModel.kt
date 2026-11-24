package com.example.mjbebikesaplicativomovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mjbebikesaplicativomovil.data.local.SesionUsuario
import com.example.mjbebikesaplicativomovil.data.local.TokenManager
import com.example.mjbebikesaplicativomovil.data.repository.CarritoRepository
import com.example.mjbebikesaplicativomovil.remote.VentasApi
import com.example.mjbebikesaplicativomovil.remote.models.Cliente
import com.example.mjbebikesaplicativomovil.remote.models.TipoDocumento
import com.example.mjbebikesaplicativomovil.remote.models.Venta
import com.example.mjbebikesaplicativomovil.utils.ApiErrores
import com.example.mjbebikesaplicativomovil.utils.CheckoutFormulario
import com.example.mjbebikesaplicativomovil.utils.CheckoutUtils
import com.example.mjbebikesaplicativomovil.utils.CompraUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Todo lo que muestra la pantalla de finalizar compra.
data class CheckoutUiState(
    val cargando: Boolean = true,
    val errorCarga: String? = null,
    // Registro de cliente del usuario (null = primera compra: hay que pedir sus datos).
    val cliente: Cliente? = null,
    val tiposDocumento: List<TipoDocumento> = emptyList(),
    val formulario: CheckoutFormulario = CheckoutFormulario(),
    val erroresCampos: Map<String, String> = emptyMap(),
    val procesando: Boolean = false,
    val errorCompra: String? = null,
    // Venta creada: cuando no es null se muestra la confirmación.
    val ventaCreada: Venta? = null
) {
    val esPrimeraCompra: Boolean get() = cliente == null
}

class CheckoutViewModel(
    private val api: VentasApi,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private var sesion: SesionUsuario? = null

    init {
        cargarDatos()
    }

    // Busca el registro de cliente del usuario y los tipos de documento.
    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, errorCarga = null)

            try {
                val usuario = tokenManager.getSesion()
                if (usuario == null) {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        errorCarga = ApiErrores.SESION_VENCIDA
                    )
                    return@launch
                }
                sesion = usuario

                val clientes = api.clientePorUsuario(usuario.id)
                val tipos = api.tiposDocumentos()

                val fallida = listOf(clientes, tipos).firstOrNull { !it.isSuccessful }
                if (fallida != null) {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        errorCarga = ApiErrores.mensaje(fallida, "No se pudieron cargar tus datos")
                    )
                    return@launch
                }

                val cliente = clientes.body().orEmpty().firstOrNull()

                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    cliente = cliente,
                    tiposDocumento = tipos.body().orEmpty(),
                    formulario = cliente?.let { CheckoutFormulario.desde(it) } ?: CheckoutFormulario()
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    errorCarga = "No se pudo conectar con el servidor. Comprueba tu conexión."
                )
            }
        }
    }

    fun actualizar(nuevo: CheckoutFormulario) {
        val estado = _uiState.value
        _uiState.value = estado.copy(
            formulario = nuevo,
            erroresCampos = if (estado.erroresCampos.isEmpty()) emptyMap()
            else CheckoutUtils.validar(nuevo, estado.esPrimeraCompra),
            errorCompra = null
        )
    }

    // 1) Si es la primera compra, crea el registro de cliente (POST /clientes).
    // 2) Crea la venta con los productos del carrito (POST /ventas).
    // 3) Vacía el carrito y muestra la confirmación.
    fun confirmarCompra() {
        val estado = _uiState.value
        val usuario = sesion ?: return
        val items = CarritoRepository.items.value

        if (items.isEmpty()) {
            _uiState.value = estado.copy(errorCompra = "Tu carrito está vacío.")
            return
        }

        val errores = CheckoutUtils.validar(estado.formulario, estado.esPrimeraCompra)
        if (errores.isNotEmpty()) {
            _uiState.value = estado.copy(erroresCampos = errores)
            return
        }

        viewModelScope.launch {
            _uiState.value = estado.copy(procesando = true, erroresCampos = emptyMap(), errorCompra = null)

            try {
                // 1) Registro de cliente
                var cliente = estado.cliente
                if (cliente == null) {
                    val nuevo = CheckoutUtils.aFormularioCliente(estado.formulario, usuario.id).aCliente()
                    val respuesta = api.crearCliente(nuevo)
                    cliente = respuesta.body()?.cliente

                    if (!respuesta.isSuccessful || cliente?.id == null) {
                        _uiState.value = _uiState.value.copy(
                            procesando = false,
                            errorCompra = ApiErrores.mensaje(respuesta, "No se pudieron guardar tus datos")
                        )
                        return@launch
                    }
                    // Si después falla la venta, ya no se vuelve a crear el cliente.
                    _uiState.value = _uiState.value.copy(cliente = cliente)
                }

                // 2) Venta
                val venta = CheckoutUtils.armarVenta(
                    usuarioId = usuario.id,
                    idCliente = cliente?.id ?: 0,
                    items = items,
                    f = estado.formulario,
                    fecha = CompraUtils.hoy()
                )
                val respuesta = api.crearVenta(venta)

                if (respuesta.isSuccessful) {
                    // 3) Carrito vacío y confirmación
                    CarritoRepository.vaciar()
                    _uiState.value = _uiState.value.copy(
                        procesando = false,
                        ventaCreada = respuesta.body()?.venta ?: Venta(total = venta.total)
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        procesando = false,
                        errorCompra = ApiErrores.mensaje(respuesta, "No se pudo registrar la compra")
                    )
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    procesando = false,
                    errorCompra = "No se pudo conectar con el servidor. Intenta nuevamente."
                )
            }
        }
    }
}
