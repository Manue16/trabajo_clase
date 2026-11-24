package com.example.mjbebikesaplicativomovil.ui.catalogo

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mjbebikesaplicativomovil.data.repository.CarritoRepository
import com.example.mjbebikesaplicativomovil.remote.models.Producto
import com.example.mjbebikesaplicativomovil.utils.CatalogoFiltro
import com.example.mjbebikesaplicativomovil.utils.OrdenPrecio
import com.example.mjbebikesaplicativomovil.viewmodel.CatalogoUiState
import com.example.mjbebikesaplicativomovil.viewmodel.CatalogoViewModel
import com.example.mjbebikesaplicativomovil.viewmodel.CatalogoViewModelFactory

// FilterChip y BadgedBox son experimentales en algunas versiones de Material 3.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    // Qué hacer al tocar un producto (abre su detalle).
    onProductoClick: (Producto) -> Unit = {},
    // Qué hacer al tocar el ícono del carrito.
    onCarritoClick: () -> Unit = {}
) {

    // Obtenemos el contexto de Android.
    val context = LocalContext.current

    // Creamos el ViewModel con su fábrica.
    val catalogoViewModel: CatalogoViewModel = viewModel(
        factory = CatalogoViewModelFactory(context)
    )

    // Observamos el estado de la carga y los filtros.
    val uiState by catalogoViewModel.uiState.collectAsState()
    val busqueda by catalogoViewModel.busqueda.collectAsState()
    val marca by catalogoViewModel.marca.collectAsState()
    val orden by catalogoViewModel.orden.collectAsState()

    // Unidades en el carrito (el número que se ve sobre el ícono).
    val itemsCarrito by CarritoRepository.items.collectAsState()
    val unidadesCarrito = itemsCarrito.sumOf { it.cantidad }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Nuestros productos",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Text(
                        text = "Encuentra el producto que necesitas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Ícono del carrito con el número de unidades.
                IconButton(onClick = onCarritoClick) {
                    BadgedBox(
                        badge = {
                            if (unidadesCarrito > 0) {
                                Badge { Text(unidadesCarrito.toString()) }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = "Carrito"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Buscador por nombre, descripción o marca.
            OutlinedTextField(
                value = busqueda,
                onValueChange = { catalogoViewModel.cambiarBusqueda(it) },
                label = { Text("Buscar") },
                placeholder = { Text("Nombre, descripción o marca") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            when (val estado = uiState) {

                // Mientras se cargan los productos.
                is CatalogoUiState.Cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                // Si la API falla, mostramos el error y un botón para reintentar.
                is CatalogoUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = estado.mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { catalogoViewModel.cargarProductos() }) {
                            Text("Reintentar")
                        }
                    }
                }

                // Productos cargados: filtros y lista.
                is CatalogoUiState.Listo -> {

                    // Filtro por marca.
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = marca == null,
                                onClick = { catalogoViewModel.cambiarMarca(null) },
                                label = { Text("Todas las marcas") }
                            )
                        }
                        items(estado.marcas) { nombreMarca ->
                            FilterChip(
                                selected = marca == nombreMarca,
                                onClick = { catalogoViewModel.cambiarMarca(nombreMarca) },
                                label = { Text(nombreMarca) }
                            )
                        }
                    }

                    // Orden por precio y limpiar filtros.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = orden == OrdenPrecio.MENOR,
                            onClick = { catalogoViewModel.cambiarOrden(OrdenPrecio.MENOR) },
                            label = { Text("Menor precio") }
                        )
                        FilterChip(
                            selected = orden == OrdenPrecio.MAYOR,
                            onClick = { catalogoViewModel.cambiarOrden(OrdenPrecio.MAYOR) },
                            label = { Text("Mayor precio") }
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { catalogoViewModel.limpiarFiltros() }) {
                            Text("Limpiar")
                        }
                    }

                    // Aplicamos búsqueda, marca y orden.
                    val productos = remember(estado.productos, busqueda, marca, orden) {
                        CatalogoFiltro.filtrar(estado.productos, busqueda, marca, orden)
                    }

                    if (productos.isEmpty()) {
                        Text(
                            text = "No encontramos productos con esos filtros.",
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(productos, key = { it.id ?: it.hashCode() }) { producto ->
                                ProductoCard(
                                    producto = producto,
                                    onClick = { onProductoClick(producto) },
                                    onAgregar = {
                                        val agregado = CarritoRepository.agregar(producto)
                                        val mensaje = if (agregado) "Agregado al carrito"
                                        else "No hay más unidades disponibles"
                                        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Tarjeta de un producto: imagen, marca, nombre y precio.
@Composable
fun ProductoCard(
    producto: Producto,
    onClick: () -> Unit,
    onAgregar: () -> Unit
) {
    val stock = producto.cantProducto ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {

        // La imagen se descarga desde la URL guardada en img_producto.
        AsyncImage(
            model = producto.imgProducto,
            contentDescription = producto.nombreProducto,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(android.R.drawable.ic_menu_gallery),
            error = painterResource(android.R.drawable.ic_menu_gallery),
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        Column(modifier = Modifier.padding(8.dp)) {

            Text(
                text = producto.marcaProducto.orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = producto.nombreProducto.orEmpty(),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = CatalogoFiltro.formatoPrecio(producto.valorUnitario),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Si no hay stock, el botón se desactiva.
            Button(
                onClick = onAgregar,
                enabled = stock > 0,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (stock > 0) "Agregar" else "Agotado",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
