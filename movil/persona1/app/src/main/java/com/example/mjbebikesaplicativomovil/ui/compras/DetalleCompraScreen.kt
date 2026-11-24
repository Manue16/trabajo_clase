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
import com.example.mjbebikesaplicativomovil.utils.CompraUtils
import com.example.mjbebikesaplicativomovil.viewmodel.ComprasViewModelFactory
import com.example.mjbebikesaplicativomovil.viewmodel.DetalleCompraUiState
import com.example.mjbebikesaplicativomovil.viewmodel.DetalleCompraViewModel

@Composable
fun DetalleCompraScreen(
    idCompra: Int,
    onVolver: () -> Unit = {}
) {

    val context = LocalContext.current

    val detalleViewModel: DetalleCompraViewModel = viewModel(
        key = "compra_$idCompra",
        factory = ComprasViewModelFactory(context, idCompra)
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
                text = "Detalle de compra #$idCompra",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (val estado = uiState) {

                is DetalleCompraUiState.Cargando -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is DetalleCompraUiState.Error -> {
                    Text(
                        text = estado.mensaje,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { detalleViewModel.cargarDetalle() }) {
                        Text("Reintentar")
                    }
                }

                is DetalleCompraUiState.Listo -> {
                    val compra = estado.compra

                    // Datos generales de la compra.
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Dato("Proveedor", compra.proveedor?.nombreProveedor ?: "Proveedor #${compra.idProveedor ?: ""}")
                            Dato("Fecha", CompraUtils.fechaCorta(compra.fecha))
                            Dato("Total", CatalogoFiltro.formatoPrecio(compra.totalCompra))
                            Dato("Estado", CompraUtils.estadoTexto(compra.pago))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Productos de la compra",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (estado.detalles.isEmpty()) {
                        Text("No hay productos asociados.")
                    } else {
                        estado.detalles.forEach { detalle ->
                            val cantidad = detalle.cantidad ?: 0
                            val precio = detalle.precioUnitario ?: 0.0

                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Text(
                                    text = detalle.producto?.nombreProducto
                                        ?: "Producto #${detalle.idProducto ?: ""}",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "$cantidad × ${CatalogoFiltro.formatoPrecio(precio)}",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = CatalogoFiltro.formatoPrecio(cantidad * precio),
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
