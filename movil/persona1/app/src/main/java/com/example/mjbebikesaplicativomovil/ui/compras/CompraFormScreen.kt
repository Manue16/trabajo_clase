package com.example.mjbebikesaplicativomovil.ui.compras

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
import androidx.compose.material3.Checkbox
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
import com.example.mjbebikesaplicativomovil.utils.LineaCompra
import com.example.mjbebikesaplicativomovil.viewmodel.CompraFormViewModel
import com.example.mjbebikesaplicativomovil.viewmodel.ComprasViewModelFactory

// Formulario para registrar una compra a un proveedor con uno o varios productos.
@Composable
fun CompraFormScreen(
    onVolver: () -> Unit = {},
    onGuardado: () -> Unit = {}
) {

    val context = LocalContext.current

    val formViewModel: CompraFormViewModel = viewModel(
        key = "compra_nueva",
        factory = ComprasViewModelFactory(context)
    )

    val uiState by formViewModel.uiState.collectAsState()
    val f = uiState.formulario
    val errores = uiState.erroresCampos

    // Cuando la API confirma que se guardó, volvemos a la lista.
    LaunchedEffect(uiState.guardado) {
        if (uiState.guardado) onGuardado()
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

            Text(
                text = "Crear compra",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Complete la información de la compra",
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
                    Text(
                        text = uiState.errorCarga.orEmpty(),
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { formViewModel.cargarDatos() }) {
                        Text("Reintentar")
                    }
                }

                else -> {

                    Selector(
                        etiqueta = "Proveedor",
                        opciones = uiState.proveedores,
                        seleccionada = uiState.proveedores.find { it.id == f.idProveedor },
                        textoDe = { it.nombreProveedor.orEmpty() },
                        onSeleccionar = { formViewModel.cambiarProveedor(it.id) },
                        error = errores["proveedor"]
                    )

                    OutlinedTextField(
                        value = f.fecha,
                        onValueChange = { formViewModel.cambiarFecha(it) },
                        label = { Text("Fecha (AAAA-MM-DD)") },
                        singleLine = true,
                        isError = errores["fecha"] != null,
                        supportingText = if (errores["fecha"] != null) {
                            { Text(errores["fecha"].orEmpty()) }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = f.pago,
                            onCheckedChange = { formViewModel.cambiarPago(it) }
                        )
                        Text("Pagada")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Productos",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(onClick = { formViewModel.agregarLinea() }) {
                            Text("Agregar producto")
                        }
                    }

                    errores["lineas"]?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }

                    // Una tarjeta por cada producto de la compra.
                    f.lineas.forEachIndexed { indice, linea ->
                        LineaCompraCard(
                            numero = indice + 1,
                            linea = linea,
                            productos = uiState.productos,
                            errores = errores,
                            indice = indice,
                            sePuedeQuitar = f.lineas.size > 1,
                            onCambiar = { formViewModel.cambiarLinea(indice, it) },
                            onQuitar = { formViewModel.quitarLinea(indice) }
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
                            text = CatalogoFiltro.formatoPrecio(f.total),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Error que devuelve la API.
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
                        OutlinedButton(
                            onClick = onVolver,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = { formViewModel.guardar() },
                            enabled = !uiState.guardando,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (uiState.guardando) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            } else {
                                Text("Guardar compra")
                            }
                        }
                    }
                }
            }
        }
    }
}

// Una fila de producto: producto, cantidad, costo unitario y subtotal.
@Composable
private fun LineaCompraCard(
    numero: Int,
    linea: LineaCompra,
    productos: List<Producto>,
    errores: Map<String, String>,
    indice: Int,
    sePuedeQuitar: Boolean,
    onCambiar: (LineaCompra) -> Unit,
    onQuitar: () -> Unit
) {
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
                seleccionada = productos.find { it.id == linea.idProducto },
                textoDe = { "${it.nombreProducto.orEmpty()} (stock ${it.cantProducto ?: 0})" },
                onSeleccionar = { onCambiar(linea.copy(idProducto = it.id)) },
                error = errores["producto_$indice"]
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = linea.cantidad,
                    onValueChange = { onCambiar(linea.copy(cantidad = it)) },
                    label = { Text("Cant.") },
                    singleLine = true,
                    isError = errores["cantidad_$indice"] != null,
                    supportingText = if (errores["cantidad_$indice"] != null) {
                        { Text(errores["cantidad_$indice"].orEmpty()) }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = linea.precio,
                    onValueChange = { onCambiar(linea.copy(precio = it)) },
                    label = { Text("Costo unitario") },
                    singleLine = true,
                    isError = errores["precio_$indice"] != null,
                    supportingText = if (errores["precio_$indice"] != null) {
                        { Text(errores["precio_$indice"].orEmpty()) }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(2f)
                )
            }

            Text(
                text = "Subtotal: ${CatalogoFiltro.formatoPrecio(linea.subtotal)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
