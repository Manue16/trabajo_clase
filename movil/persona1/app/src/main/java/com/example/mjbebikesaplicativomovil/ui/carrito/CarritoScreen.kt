package com.example.mjbebikesaplicativomovil.ui.carrito

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mjbebikesaplicativomovil.data.repository.CarritoRepository
import com.example.mjbebikesaplicativomovil.data.repository.ItemCarrito
import com.example.mjbebikesaplicativomovil.utils.CatalogoFiltro

@Composable
fun CarritoScreen(
    onSeguirComprando: () -> Unit = {},
    // Pantalla de la persona 2 (finalizar compra). Mientras no exista, avisa.
    onFinalizarCompra: (() -> Unit)? = null
) {

    val context = LocalContext.current

    // El carrito es compartido: cualquier cambio se ve aquí al instante.
    val items by CarritoRepository.items.collectAsState()
    val total = items.sumOf { it.subtotal }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            TextButton(onClick = onSeguirComprando) {
                Text("← Seguir comprando")
            }

            Text(
                text = "Tu carrito",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (items.isEmpty()) {

                Text("No tienes productos agregados.")
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onSeguirComprando) {
                    Text("Ver productos")
                }

            } else {

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(items, key = { it.producto.id ?: it.hashCode() }) { item ->
                        ItemCarritoCard(
                            item = item,
                            onCambiarCantidad = { nueva ->
                                val idProducto = item.producto.id
                                if (idProducto != null &&
                                    !CarritoRepository.cambiarCantidad(idProducto, nueva)
                                ) {
                                    Toast.makeText(
                                        context,
                                        "No hay más unidades disponibles",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onQuitar = {
                                item.producto.id?.let { CarritoRepository.quitar(it) }
                            }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = CatalogoFiltro.formatoPrecio(total),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (onFinalizarCompra != null) {
                            onFinalizarCompra()
                        } else {
                            Toast.makeText(
                                context,
                                "Finalizar compra todavía no está disponible",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Finalizar compra")
                }

                TextButton(
                    onClick = { CarritoRepository.vaciar() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Vaciar carrito")
                }
            }
        }
    }
}

// Un producto del carrito con sus botones "-", "+" y "Quitar".
@Composable
private fun ItemCarritoCard(
    item: ItemCarrito,
    onCambiarCantidad: (Int) -> Unit,
    onQuitar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {

        Column(modifier = Modifier.padding(12.dp)) {

            Text(
                text = item.producto.nombreProducto.orEmpty(),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = CatalogoFiltro.formatoPrecio(item.producto.valorUnitario) + " c/u",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {

                OutlinedButton(onClick = { onCambiarCantidad(item.cantidad - 1) }) {
                    Text("-")
                }

                Text(
                    text = item.cantidad.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                OutlinedButton(onClick = { onCambiarCantidad(item.cantidad + 1) }) {
                    Text("+")
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = CatalogoFiltro.formatoPrecio(item.subtotal),
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(onClick = onQuitar) {
                Text("Quitar", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
