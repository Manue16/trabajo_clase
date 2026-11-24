package com.example.mjbebikesaplicativomovil.ui.inventario

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mjbebikesaplicativomovil.remote.models.Producto
import com.example.mjbebikesaplicativomovil.utils.EstadoFiltro
import com.example.mjbebikesaplicativomovil.utils.InventarioUtils
import com.example.mjbebikesaplicativomovil.utils.NivelStock
import com.example.mjbebikesaplicativomovil.utils.OrdenCantidad
import com.example.mjbebikesaplicativomovil.viewmodel.InventarioUiState
import com.example.mjbebikesaplicativomovil.viewmodel.InventarioViewModel
import com.example.mjbebikesaplicativomovil.viewmodel.InventarioViewModelFactory

// FilterChip es experimental en algunas versiones de Material 3.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioScreen(
    onNuevoProducto: () -> Unit = {},
    onEditarProducto: (Int) -> Unit = {}
) {

    val context = LocalContext.current

    val inventarioViewModel: InventarioViewModel = viewModel(
        factory = InventarioViewModelFactory(context)
    )

    val uiState by inventarioViewModel.uiState.collectAsState()
    val busqueda by inventarioViewModel.busqueda.collectAsState()
    val estado by inventarioViewModel.estado.collectAsState()
    val soloStockBajo by inventarioViewModel.soloStockBajo.collectAsState()
    val orden by inventarioViewModel.orden.collectAsState()
    val mensaje by inventarioViewModel.mensaje.collectAsState()

    // Producto que se va a eliminar (abre la ventana de confirmación).
    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Se recarga cada vez que se entra a la pantalla (también al volver del formulario).
    LaunchedEffect(Unit) {
        inventarioViewModel.cargarProductos()
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            inventarioViewModel.mensajeMostrado()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevoProducto,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nuevo producto") }
            )
        }
    ) { espacio ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacio)
                .padding(16.dp)
        ) {

            Text(
                text = "Inventario",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = busqueda,
                onValueChange = { inventarioViewModel.cambiarBusqueda(it) },
                label = { Text("Buscar") },
                placeholder = { Text("Producto, modelo, marca o color") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Filtros: estado, stock bajo y orden por cantidad.
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = estado == EstadoFiltro.TODOS,
                    onClick = { inventarioViewModel.cambiarEstado(EstadoFiltro.TODOS) },
                    label = { Text("Todos") }
                )
                FilterChip(
                    selected = estado == EstadoFiltro.ACTIVOS,
                    onClick = { inventarioViewModel.cambiarEstado(EstadoFiltro.ACTIVOS) },
                    label = { Text("Activos") }
                )
                FilterChip(
                    selected = estado == EstadoFiltro.INACTIVOS,
                    onClick = { inventarioViewModel.cambiarEstado(EstadoFiltro.INACTIVOS) },
                    label = { Text("Inactivos") }
                )
                FilterChip(
                    selected = soloStockBajo,
                    onClick = { inventarioViewModel.cambiarSoloStockBajo() },
                    label = { Text("Stock bajo (≤ 5)") }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = orden == OrdenCantidad.MENOR,
                    onClick = { inventarioViewModel.cambiarOrden(OrdenCantidad.MENOR) },
                    label = { Text("Menor cantidad") }
                )
                FilterChip(
                    selected = orden == OrdenCantidad.MAYOR,
                    onClick = { inventarioViewModel.cambiarOrden(OrdenCantidad.MAYOR) },
                    label = { Text("Mayor cantidad") }
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { inventarioViewModel.limpiarFiltros() }) {
                    Text("Limpiar")
                }
            }

            when (val estadoUi = uiState) {

                is InventarioUiState.Cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is InventarioUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = estadoUi.mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { inventarioViewModel.cargarProductos() }) {
                            Text("Reintentar")
                        }
                    }
                }

                is InventarioUiState.Listo -> {

                    val productos = remember(estadoUi.productos, busqueda, estado, soloStockBajo, orden) {
                        InventarioUtils.filtrar(estadoUi.productos, busqueda, estado, soloStockBajo, orden)
                    }

                    if (productos.isEmpty()) {
                        Text(
                            text = "No hay productos disponibles",
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                        ) {
                            items(productos, key = { it.id ?: it.hashCode() }) { producto ->
                                ProductoInventarioCard(
                                    producto = producto,
                                    onEditar = { producto.id?.let(onEditarProducto) },
                                    onCambiarEstado = { inventarioViewModel.cambiarEstadoProducto(producto) },
                                    onEliminar = { productoAEliminar = producto }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Ventana de confirmación antes de eliminar.
    productoAEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            title = { Text("Eliminar producto") },
            text = {
                Text(
                    "¿Deseas eliminar \"${producto.nombreProducto.orEmpty()}\"? " +
                            "Si solo quieres ocultarlo de la tienda, usa Desactivar."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    inventarioViewModel.eliminarProducto(producto)
                    productoAEliminar = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { productoAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Tarjeta de un producto del inventario con su alerta de stock y sus acciones.
@Composable
private fun ProductoInventarioCard(
    producto: Producto,
    onEditar: () -> Unit,
    onCambiarEstado: () -> Unit,
    onEliminar: () -> Unit
) {
    val activo = InventarioUtils.esActivo(producto)
    val nivel = InventarioUtils.nivelStock(producto.cantProducto)

    // Color de la cantidad según el nivel de stock.
    val colorStock = when (nivel) {
        NivelStock.SIN_STOCK -> MaterialTheme.colorScheme.error
        NivelStock.BAJO -> Color(0xFFE65100)       // naranja
        NivelStock.NORMAL -> MaterialTheme.colorScheme.onSurface
    }
    val textoStock = when (nivel) {
        NivelStock.SIN_STOCK -> " · Sin stock"
        NivelStock.BAJO -> " · Stock bajo"
        NivelStock.NORMAL -> ""
    }

    Card(modifier = Modifier.fillMaxWidth()) {

        Column(modifier = Modifier.padding(12.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = producto.nombreProducto.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (activo) "Activo" else "Inactivo",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (activo) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = listOfNotNull(producto.modelo, producto.marcaProducto, producto.colorProducto)
                    .joinToString(" · "),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Cantidad: ${producto.cantProducto ?: 0}$textoStock",
                color = colorStock,
                fontWeight = if (nivel == NivelStock.NORMAL) FontWeight.Normal else FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEditar) {
                    Text("Editar")
                }
                TextButton(onClick = onCambiarEstado) {
                    Text(if (activo) "Desactivar" else "Activar")
                }
                TextButton(onClick = onEliminar) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
