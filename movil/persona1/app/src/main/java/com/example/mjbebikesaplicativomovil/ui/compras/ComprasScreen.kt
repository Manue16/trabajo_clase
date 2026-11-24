package com.example.mjbebikesaplicativomovil.ui.compras

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
import com.example.mjbebikesaplicativomovil.remote.models.Compra
import com.example.mjbebikesaplicativomovil.utils.CatalogoFiltro
import com.example.mjbebikesaplicativomovil.utils.CompraUtils
import com.example.mjbebikesaplicativomovil.viewmodel.ComprasUiState
import com.example.mjbebikesaplicativomovil.viewmodel.ComprasViewModel
import com.example.mjbebikesaplicativomovil.viewmodel.ComprasViewModelFactory

@Composable
fun ComprasScreen(
    onNuevaCompra: () -> Unit = {},
    onVerDetalle: (Int) -> Unit = {}
) {

    val context = LocalContext.current

    val comprasViewModel: ComprasViewModel = viewModel(
        factory = ComprasViewModelFactory(context)
    )

    val uiState by comprasViewModel.uiState.collectAsState()
    val busqueda by comprasViewModel.busqueda.collectAsState()

    // Se recarga cada vez que se entra a la pantalla (también al volver de crear una compra).
    LaunchedEffect(Unit) {
        comprasViewModel.cargarCompras()
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevaCompra,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Crear compra") }
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
                text = "Compras",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Historial de compras a proveedores",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = busqueda,
                onValueChange = { comprasViewModel.cambiarBusqueda(it) },
                label = { Text("Buscar compra") },
                placeholder = { Text("N° de compra, proveedor o estado") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            when (val estado = uiState) {

                is ComprasUiState.Cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ComprasUiState.Error -> {
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
                        Button(onClick = { comprasViewModel.cargarCompras() }) {
                            Text("Reintentar")
                        }
                    }
                }

                is ComprasUiState.Listo -> {

                    val compras = remember(estado.compras, busqueda) {
                        CompraUtils.filtrar(estado.compras, busqueda)
                    }

                    when {
                        estado.compras.isEmpty() -> Text(
                            text = "No hay compras registradas.",
                            modifier = Modifier.padding(top = 24.dp)
                        )

                        compras.isEmpty() -> Text(
                            text = "No se encontraron compras",
                            modifier = Modifier.padding(top = 24.dp)
                        )

                        else -> LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(compras, key = { it.id ?: it.hashCode() }) { compra ->
                                CompraCard(
                                    compra = compra,
                                    onClick = { compra.id?.let(onVerDetalle) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Tarjeta de una compra: número, proveedor, fecha, total y estado. Al tocarla se abre el detalle.
@Composable
private fun CompraCard(
    compra: Compra,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {

        Column(modifier = Modifier.padding(12.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Compra #${compra.id ?: ""}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = CompraUtils.estadoTexto(compra.pago),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (compra.pago == true) MaterialTheme.colorScheme.primary
                    else Color(0xFFE65100)          // naranja para "Pendiente"
                )
            }

            Text(text = compra.proveedor?.nombreProveedor ?: "Proveedor #${compra.idProveedor ?: ""}")

            Row {
                Text(
                    text = CompraUtils.fechaCorta(compra.fecha),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = CatalogoFiltro.formatoPrecio(compra.totalCompra),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
