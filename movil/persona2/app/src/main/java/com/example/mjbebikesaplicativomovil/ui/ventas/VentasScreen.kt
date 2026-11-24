package com.example.mjbebikesaplicativomovil.ui.ventas

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.mjbebikesaplicativomovil.utils.VentaUtils
import com.example.mjbebikesaplicativomovil.viewmodel.VentasUiState
import com.example.mjbebikesaplicativomovil.viewmodel.VentasViewModel
import com.example.mjbebikesaplicativomovil.viewmodel.VentasViewModelFactory

// Historial de ventas: el vendedor ve las suyas; el administrador, todas y su vendedor.
@Composable
fun VentasScreen(
    onNuevaVenta: () -> Unit = {},
    onVerDetalle: (Int) -> Unit = {}
) {

    val context = LocalContext.current

    val ventasViewModel: VentasViewModel = viewModel(
        factory = VentasViewModelFactory(context)
    )

    val uiState by ventasViewModel.uiState.collectAsState()
    val busqueda by ventasViewModel.busqueda.collectAsState()

    // Se recarga cada vez que se entra (también al volver de registrar una venta).
    LaunchedEffect(Unit) {
        ventasViewModel.cargarVentas()
    }

    val esAdmin = (uiState as? VentasUiState.Listo)?.esAdmin == true

    Scaffold(
        floatingActionButton = {
            // Solo el vendedor registra ventas en tienda (como en la web).
            if (uiState is VentasUiState.Listo && !esAdmin) {
                ExtendedFloatingActionButton(
                    onClick = onNuevaVenta,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Crear venta") }
                )
            }
        }
    ) { espacio ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacio)
                .padding(16.dp)
        ) {

            Text(text = "Ventas", style = MaterialTheme.typography.headlineSmall)

            Text(
                text = if (esAdmin) "Todas las ventas registradas y su vendedor" else "Tus ventas registradas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = busqueda,
                onValueChange = { ventasViewModel.cambiarBusqueda(it) },
                label = { Text("Buscar venta") },
                placeholder = { Text(if (esAdmin) "N° de venta, cliente o vendedor" else "N° de venta o cliente") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            when (val estado = uiState) {

                is VentasUiState.Cargando -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is VentasUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = estado.mensaje, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { ventasViewModel.cargarVentas() }) {
                            Text("Reintentar")
                        }
                    }
                }

                is VentasUiState.Listo -> {
                    val ventas = remember(estado.ventas, busqueda) {
                        VentaUtils.filtrar(estado.ventas, busqueda)
                    }

                    when {
                        estado.ventas.isEmpty() -> Text(
                            text = "No hay ventas registradas.",
                            modifier = Modifier.padding(top = 24.dp)
                        )

                        ventas.isEmpty() -> Text(
                            text = "No se encontraron ventas",
                            modifier = Modifier.padding(top = 24.dp)
                        )

                        else -> LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(ventas, key = { it.id ?: it.hashCode() }) { venta ->
                                VentaCard(
                                    venta = venta,
                                    mostrarVendedor = estado.esAdmin,
                                    onClick = { venta.id?.let(onVerDetalle) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Tarjeta de una venta. Al tocarla se abre el detalle.
@Composable
private fun VentaCard(
    venta: Venta,
    mostrarVendedor: Boolean,
    onClick: () -> Unit
) {
    val estadoTexto = CheckoutUtils.estadoTexto(venta)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Venta #${venta.id ?: ""}",
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

            Text(text = "Cliente: ${VentaUtils.nombreCliente(venta)}")

            if (mostrarVendedor) {
                Text(text = "Vendedor: ${VentaUtils.nombreVendedor(venta)}")
            }

            Row {
                Text(
                    text = CompraUtils.fechaCorta(venta.fecha),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = CatalogoFiltro.formatoPrecio(venta.total),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
