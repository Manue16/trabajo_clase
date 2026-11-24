package com.example.mjbebikesaplicativomovil.ui.catalogo

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mjbebikesaplicativomovil.data.repository.CarritoRepository
import com.example.mjbebikesaplicativomovil.remote.models.Producto
import com.example.mjbebikesaplicativomovil.utils.CatalogoFiltro
import com.example.mjbebikesaplicativomovil.viewmodel.DetalleProductoUiState
import com.example.mjbebikesaplicativomovil.viewmodel.DetalleProductoViewModel
import com.example.mjbebikesaplicativomovil.viewmodel.DetalleProductoViewModelFactory

@Composable
fun DetalleProductoScreen(
    idProducto: Int,
    onVolver: () -> Unit = {},
    onVerCarrito: () -> Unit = {}
) {

    val context = LocalContext.current

    // Un ViewModel por producto (la clave evita reutilizar el de otro producto).
    val detalleViewModel: DetalleProductoViewModel = viewModel(
        key = "producto_$idProducto",
        factory = DetalleProductoViewModelFactory(context, idProducto)
    )

    val uiState by detalleViewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        Column(modifier = Modifier.fillMaxSize()) {

            TextButton(
                onClick = onVolver,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
            ) {
                Text("← Volver")
            }

            when (val estado = uiState) {

                is DetalleProductoUiState.Cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is DetalleProductoUiState.Error -> {
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
                        Button(onClick = { detalleViewModel.cargarProducto() }) {
                            Text("Reintentar")
                        }
                    }
                }

                is DetalleProductoUiState.Listo -> {
                    DetalleProducto(
                        producto = estado.producto,
                        onAgregar = {
                            val agregado = CarritoRepository.agregar(estado.producto)
                            val mensaje = if (agregado) "Agregado al carrito"
                            else "No hay más unidades disponibles"
                            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                        },
                        onVerCarrito = onVerCarrito
                    )
                }
            }
        }
    }
}

// Información del producto: imagen, precio, descripción y características.
@Composable
private fun DetalleProducto(
    producto: Producto,
    onAgregar: () -> Unit,
    onVerCarrito: () -> Unit
) {
    val stock = producto.cantProducto ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {

        AsyncImage(
            model = producto.imgProducto,
            contentDescription = producto.nombreProducto,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(android.R.drawable.ic_menu_gallery),
            error = painterResource(android.R.drawable.ic_menu_gallery),
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = producto.marcaProducto.orEmpty(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = producto.nombreProducto.orEmpty(),
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = CatalogoFiltro.formatoPrecio(producto.valorUnitario),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Descripción", style = MaterialTheme.typography.titleMedium)
        Text(text = producto.descripcion.orEmpty())

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Text(text = "Características", style = MaterialTheme.typography.titleMedium)
        Text(text = "• Color: ${producto.colorProducto.orEmpty()}")
        Text(text = "• Modelo: ${producto.modelo.orEmpty()}")
        Text(text = "• Marca: ${producto.marcaProducto.orEmpty()}")
        Text(text = "• Stock: $stock")

        Spacer(modifier = Modifier.height(24.dp))

        // Si no hay stock, el botón se desactiva.
        Button(
            onClick = onAgregar,
            enabled = stock > 0,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (stock > 0) "Agregar al carrito" else "Agotado")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onVerCarrito,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver carrito")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
