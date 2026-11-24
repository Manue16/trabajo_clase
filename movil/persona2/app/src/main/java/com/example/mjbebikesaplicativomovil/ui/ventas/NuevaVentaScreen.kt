package com.example.mjbebikesaplicativomovil.ui.ventas

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mjbebikesaplicativomovil.remote.models.Producto
import com.example.mjbebikesaplicativomovil.ui.componentes.Selector
import com.example.mjbebikesaplicativomovil.utils.CatalogoFiltro
import com.example.mjbebikesaplicativomovil.utils.LineaVenta
import com.example.mjbebikesaplicativomovil.utils.VentaUtils
import com.example.mjbebikesaplicativomovil.viewmodel.NuevaVentaViewModel
import com.example.mjbebikesaplicativomovil.viewmodel.VentasViewModelFactory

// Venta en tienda del vendedor: cliente, productos y cantidades. Queda pagada en efectivo.
@Composable
fun NuevaVentaScreen(
    onVolver: () -> Unit = {},
    // Recibe el id de la venta creada (0 si la API no lo devolvió).
    onGuardado: (Int) -> Unit = {}
) {

    val context = LocalContext.current

    val ventaViewModel: NuevaVentaViewModel = viewModel(
        key = "venta_nueva",
        factory = VentasViewModelFactory(context)
    )

    val uiState by ventaViewModel.uiState.collectAsState()
    val f = uiState.formulario
    val errores = uiState.erroresCampos

    // Cuando la API confirma la venta, abrimos su detalle.
    LaunchedEffect(uiState.idVentaCreada) {
        uiState.idVentaCreada?.let(onGuardado)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            TextButton(onClick = onVolver) {
                Text("← Volver")
            }

            Text(text = "Crear venta", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "Complete la información de la venta",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                uiState.cargando -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.errorCarga != null -> {
                    Text(text = uiState.errorCarga.orEmpty(), color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { ventaViewModel.cargarDatos() }) {
                        Text("Reintentar")
                    }
                }

                else -> {

                    Selector(
                        etiqueta = "Cliente",
                        opciones = uiState.clientes,
                        seleccionada = uiState.clientes.find { it.id == f.idCliente },
                        textoDe = { "${it.nombres.orEmpty()} ${it.apellidos.orEmpty()}".trim() },
                        onSeleccionar = { ventaViewModel.cambiarCliente(it.id) },
                        error = errores["cliente"]
                    )

                    OutlinedTextField(
                        value = f.fecha,
                        onValueChange = { ventaViewModel.cambiarFecha(it) },
                        label = { Text("Fecha (AAAA-MM-DD)") },
                        singleLine = true,
                        isError = errores["fecha"] != null,
                        supportingText = if (errores["fecha"] != null) {
                            { Text(errores["fecha"].orEmpty()) }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Productos",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(onClick = { ventaViewModel.agregarLinea() }) {
                            Text("Agregar producto")
                        }
                    }

                    errores["lineas"]?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }

                    if (uiState.productos.isEmpty()) {
                        Text(
                            text = "No hay productos activos con stock para vender.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    f.lineas.forEachIndexed { indice, linea ->
                        LineaVentaCard(
                            numero = indice + 1,
                            linea = linea,
                            productos = uiState.productos,
                            errores = errores,
                            indice = indice,
                            sePuedeQuitar = f.lineas.size > 1,
                            onCambiar = { ventaViewModel.cambiarLinea(indice, it) },
                            onQuitar = { ventaViewModel.quitarLinea(indice) }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = CatalogoFiltro.formatoPrecio(VentaUtils.total(f, uiState.productos)),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Pago en efectivo en tienda (la venta queda pagada).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    uiState.errorGuardar?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(onClick = onVolver, modifier = Modifier.weight(1f)) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = { ventaViewModel.guardar() },
                            enabled = !uiState.guardando,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (uiState.guardando) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            } else {
                                Text("Guardar venta")
                            }
                        }
                    }
                }
            }
        }
    }
}

// Una fila de producto: producto, cantidad (con el stock disponible) y subtotal.
@Composable
private fun LineaVentaCard(
    numero: Int,
    linea: LineaVenta,
    productos: List<Producto>,
    errores: Map<String, String>,
    indice: Int,
    sePuedeQuitar: Boolean,
    onCambiar: (LineaVenta) -> Unit,
    onQuitar: () -> Unit
) {
    val producto = productos.find { it.id == linea.idProducto }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Producto $numero",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                // Con una sola fila no se puede quitar.
                TextButton(onClick = onQuitar, enabled = sePuedeQuitar) {
                    Text("Quitar")
                }
            }

            Selector(
                etiqueta = "Producto",
                opciones = productos,
                seleccionada = producto,
                textoDe = { "${it.nombreProducto.orEmpty()} - ${CatalogoFiltro.formatoPrecio(it.valorUnitario)}" },
                onSeleccionar = { onCambiar(linea.copy(idProducto = it.id)) },
                error = errores["producto_$indice"]
            )

            OutlinedTextField(
                value = linea.cantidad,
                onValueChange = { onCambiar(linea.copy(cantidad = it)) },
                label = { Text(if (producto != null) "Cantidad (máx. ${producto.cantProducto ?: 0})" else "Cantidad") },
                singleLine = true,
                isError = errores["cantidad_$indice"] != null,
                supportingText = if (errores["cantidad_$indice"] != null) {
                    { Text(errores["cantidad_$indice"].orEmpty()) }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Subtotal: ${CatalogoFiltro.formatoPrecio(VentaUtils.subtotal(linea, productos))}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
