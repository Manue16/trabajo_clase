package com.example.mjbebikesaplicativomovil.ui.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mjbebikesaplicativomovil.data.repository.CarritoRepository
import com.example.mjbebikesaplicativomovil.remote.models.Venta
import com.example.mjbebikesaplicativomovil.ui.componentes.Selector
import com.example.mjbebikesaplicativomovil.utils.CatalogoFiltro
import com.example.mjbebikesaplicativomovil.utils.CheckoutUtils
import com.example.mjbebikesaplicativomovil.utils.MetodoPago
import com.example.mjbebikesaplicativomovil.viewmodel.CheckoutViewModel
import com.example.mjbebikesaplicativomovil.viewmodel.VentasViewModelFactory

@Composable
fun CheckoutScreen(
    onVolver: () -> Unit = {},
    onVerProductos: () -> Unit = {},
    onVerMisCompras: () -> Unit = {},
    onIrAlInicio: () -> Unit = {}
) {

    val context = LocalContext.current

    val checkoutViewModel: CheckoutViewModel = viewModel(
        factory = VentasViewModelFactory(context)
    )

    val uiState by checkoutViewModel.uiState.collectAsState()
    val items by CarritoRepository.items.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        // Compra registrada: mostramos la confirmación.
        uiState.ventaCreada?.let { venta ->
            Confirmacion(
                venta = venta,
                metodoPago = uiState.formulario.metodoPago,
                onVerMisCompras = onVerMisCompras,
                onIrAlInicio = onIrAlInicio
            )
            return@Surface
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            TextButton(onClick = onVolver) {
                Text("← Volver al carrito")
            }

            Text(
                text = "Finalizar compra",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            when {
                // Carrito vacío
                items.isEmpty() -> {
                    Text("Tu carrito está vacío.")
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onVerProductos) {
                        Text("Ver productos")
                    }
                }

                uiState.cargando -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.errorCarga != null -> {
                    Text(
                        text = uiState.errorCarga.orEmpty(),
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { checkoutViewModel.cargarDatos() }) {
                        Text("Reintentar")
                    }
                }

                else -> {
                    val f = uiState.formulario
                    val errores = uiState.erroresCampos

                    // ---------- Resumen del pedido ----------
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Resumen del pedido", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            items.forEach { item ->
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "${item.producto.nombreProducto.orEmpty()} x ${item.cantidad}",
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(CatalogoFiltro.formatoPrecio(item.subtotal))
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Total", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text(
                                    text = CatalogoFiltro.formatoPrecio(CheckoutUtils.total(items)),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ---------- Datos de entrega ----------
                    Text("Datos de entrega", style = MaterialTheme.typography.titleMedium)

                    if (uiState.esPrimeraCompra) {
                        // Primera compra: se crean sus datos de cliente.
                        Text(
                            text = "Es tu primera compra: completa tus datos para registrarte como cliente.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Campo(f.nombres, { checkoutViewModel.actualizar(f.copy(nombres = it)) },
                            "Nombres", errores["nombres"])
                        Campo(f.apellidos, { checkoutViewModel.actualizar(f.copy(apellidos = it)) },
                            "Apellidos", errores["apellidos"])

                        Selector(
                            etiqueta = "Tipo de documento",
                            opciones = uiState.tiposDocumento,
                            seleccionada = uiState.tiposDocumento.find { it.id == f.tipoDocumentoId },
                            textoDe = { it.nombreDocumento ?: it.sigla.orEmpty() },
                            onSeleccionar = { checkoutViewModel.actualizar(f.copy(tipoDocumentoId = it.id)) },
                            error = errores["tipoDocumento"]
                        )

                        Campo(f.numeroDocumento, { checkoutViewModel.actualizar(f.copy(numeroDocumento = it)) },
                            "Número de documento", errores["numeroDocumento"])
                    } else {
                        Text("Cliente: ${f.nombres} ${f.apellidos}".trim())
                    }

                    Campo(f.telefono, { checkoutViewModel.actualizar(f.copy(telefono = it)) },
                        "Teléfono", errores["telefono"], KeyboardType.Phone)
                    Campo(f.direccion, { checkoutViewModel.actualizar(f.copy(direccion = it)) },
                        "Dirección de entrega", errores["direccion"])

                    Spacer(modifier = Modifier.height(12.dp))

                    // ---------- Forma de pago ----------
                    Text("Método de pago simulado", style = MaterialTheme.typography.titleMedium)

                    MetodoPago.values().forEach { metodo ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = f.metodoPago == metodo,
                                    onClick = { checkoutViewModel.actualizar(f.copy(metodoPago = metodo)) }
                                )
                        ) {
                            RadioButton(
                                selected = f.metodoPago == metodo,
                                onClick = { checkoutViewModel.actualizar(f.copy(metodoPago = metodo)) }
                            )
                            Text(metodo.texto)
                        }
                    }

                    uiState.errorCompra?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { checkoutViewModel.confirmarCompra() },
                        enabled = !uiState.procesando,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.procesando) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Text("Confirmar compra")
                        }
                    }
                }
            }
        }
    }
}

// Pantalla que aparece cuando la compra se registró.
@Composable
private fun Confirmacion(
    venta: Venta,
    metodoPago: MetodoPago,
    onVerMisCompras: () -> Unit,
    onIrAlInicio: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("✅", style = MaterialTheme.typography.displaySmall)
        Text(
            text = "Compra simulada realizada",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        venta.id?.let { Text("Compra #$it") }
        Text("Total: ${CatalogoFiltro.formatoPrecio(venta.total)}")
        Text("Pago: ${metodoPago.texto} · ${CheckoutUtils.estadoTexto(venta)}")

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onVerMisCompras, modifier = Modifier.fillMaxWidth()) {
            Text("Ver mis compras")
        }
        OutlinedButton(onClick = onIrAlInicio, modifier = Modifier.fillMaxWidth()) {
            Text("Volver al inicio")
        }
    }
}

// Campo de texto con su mensaje de error debajo.
@Composable
private fun Campo(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    error: String?,
    tipoTeclado: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        isError = error != null,
        supportingText = if (error != null) {
            { Text(error) }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
        modifier = Modifier.fillMaxWidth()
    )
}
