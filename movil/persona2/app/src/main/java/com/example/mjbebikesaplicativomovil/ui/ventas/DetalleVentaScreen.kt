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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mjbebikesaplicativomovil.utils.CatalogoFiltro
import com.example.mjbebikesaplicativomovil.utils.CheckoutUtils
import com.example.mjbebikesaplicativomovil.utils.CompraUtils
import com.example.mjbebikesaplicativomovil.utils.VentaUtils
import com.example.mjbebikesaplicativomovil.viewmodel.DetalleVentaUiState
import com.example.mjbebikesaplicativomovil.viewmodel.DetalleVentaViewModel
import com.example.mjbebikesaplicativomovil.viewmodel.VentasViewModelFactory

@Composable
fun DetalleVentaScreen(
    idVenta: Int,
    onVolver: () -> Unit = {}
) {

    val context = LocalContext.current

    val detalleViewModel: DetalleVentaViewModel = viewModel(
        key = "venta_$idVenta",
        factory = VentasViewModelFactory(context, idVenta)
    )

    val uiState by detalleViewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            TextButton(onClick = onVolver) {
                Text("← Volver")
            }

            Text(
                text = "Detalle de venta #$idVenta",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (val estado = uiState) {

                is DetalleVentaUiState.Cargando -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is DetalleVentaUiState.Error -> {
                    Text(text = estado.mensaje, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { detalleViewModel.cargarDetalle() }) {
                        Text("Reintentar")
                    }
                }

                is DetalleVentaUiState.Listo -> {
                    val venta = estado.venta

                    // Datos generales de la venta.
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Dato("Cliente", VentaUtils.nombreCliente(venta))
                            Dato("Vendedor", VentaUtils.nombreVendedor(venta))
                            Dato("Fecha", CompraUtils.fechaCorta(venta.fecha))
                            Dato("Estado", CheckoutUtils.estadoTexto(venta))
                            Dato("Total", CatalogoFiltro.formatoPrecio(venta.total))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Productos de la venta", style = MaterialTheme.typography.titleMedium)

                    Spacer(modifier = Modifier.height(8.dp))

                    val productos = venta.productos.orEmpty()
                    if (productos.isEmpty()) {
                        Text("No hay productos asociados.")
                    } else {
                        productos.forEach { producto ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Text(
                                    text = producto.nombreProducto ?: "Producto #${producto.idProducto ?: ""}",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "${producto.cantidad ?: 0} × ${CatalogoFiltro.formatoPrecio(producto.valorUnitario)}",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = CatalogoFiltro.formatoPrecio(producto.subtotal),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

// Una fila "Etiqueta: valor" en los datos generales.
@Composable
private fun Dato(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = etiqueta, fontWeight = FontWeight.Bold)
        Text(text = valor)
    }
}
