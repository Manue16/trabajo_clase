package com.example.mjbebikesaplicativomovil.ui.miscompras

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.mjbebikesaplicativomovil.remote.models.Venta
import com.example.mjbebikesaplicativomovil.utils.CatalogoFiltro
import com.example.mjbebikesaplicativomovil.utils.CheckoutUtils
import com.example.mjbebikesaplicativomovil.utils.CompraUtils
import com.example.mjbebikesaplicativomovil.viewmodel.MisComprasUiState
import com.example.mjbebikesaplicativomovil.viewmodel.MisComprasViewModel
import com.example.mjbebikesaplicativomovil.viewmodel.VentasViewModelFactory

@Composable
fun MisComprasScreen(
    onIrATienda: () -> Unit = {}
) {

    val context = LocalContext.current

    val misComprasViewModel: MisComprasViewModel = viewModel(
        factory = VentasViewModelFactory(context)
    )

    val uiState by misComprasViewModel.uiState.collectAsState()

    // Se recarga cada vez que se entra (por ejemplo, justo después de comprar).
    LaunchedEffect(Unit) {
        misComprasViewModel.cargarCompras()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Text(
                text = "Mis compras",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (val estado = uiState) {

                is MisComprasUiState.Cargando -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is MisComprasUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = estado.mensaje, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { misComprasViewModel.cargarCompras() }) {
                            Text("Reintentar")
                        }
                    }
                }

                is MisComprasUiState.Listo -> {
                    if (estado.compras.isEmpty()) {
                        Text("Aún no tienes compras registradas.")
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onIrATienda) {
                            Text("Ir a la tienda")
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(estado.compras, key = { it.id ?: it.hashCode() }) { compra ->
                                CompraClienteCard(compra)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Tarjeta de una compra. Al tocarla muestra u oculta los productos.
@Composable
private fun CompraClienteCard(compra: Venta) {

    var abierta by remember { mutableStateOf(false) }
    val estadoTexto = CheckoutUtils.estadoTexto(compra)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { abierta = !abierta }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Compra #${compra.id ?: ""}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = estadoTexto,
                    fontWeight = FontWeight.Bold,
                    color = if (estadoTexto == "Pagada") MaterialTheme.colorScheme.primary
                    else Color(0xFFE65100)          // naranja para "Pendiente"
                )
            }

            Row {
                Text(
                    text = "${CompraUtils.fechaCorta(compra.fecha)} · ${CheckoutUtils.unidades(compra)} productos",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = CatalogoFiltro.formatoPrecio(compra.total),
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = if (abierta) "▲ Ocultar productos" else "▼ Ver productos",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (abierta) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                compra.productos.orEmpty().forEach { producto ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Text(
                            text = "${producto.nombreProducto.orEmpty()} x ${producto.cantidad ?: 0}",
                            modifier = Modifier.weight(1f)
                        )
                        Text(CatalogoFiltro.formatoPrecio(producto.subtotal))
                    }
                }
            }
        }
    }
}
