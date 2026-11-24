package com.example.mjbebikesaplicativomovil.ui.panel

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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mjbebikesaplicativomovil.remote.models.Dashboard
import com.example.mjbebikesaplicativomovil.utils.CatalogoFiltro
import com.example.mjbebikesaplicativomovil.utils.CheckoutUtils
import com.example.mjbebikesaplicativomovil.utils.CompraUtils
import com.example.mjbebikesaplicativomovil.utils.ResumenBodega
import com.example.mjbebikesaplicativomovil.utils.ResumenVendedor
import com.example.mjbebikesaplicativomovil.utils.VentaUtils
import com.example.mjbebikesaplicativomovil.viewmodel.PanelUiState
import com.example.mjbebikesaplicativomovil.viewmodel.PanelViewModel
import com.example.mjbebikesaplicativomovil.viewmodel.VentasViewModelFactory

// Colores de las alertas.
private val Naranja = Color(0xFFE65100)

// Panel de inicio: cada rol ve sus indicadores.
@Composable
fun PanelScreen(
    // Abre otra pantalla de la app por su ruta ("ventas", "inventario"...).
    onIrA: (String) -> Unit = {}
) {

    val context = LocalContext.current

    val panelViewModel: PanelViewModel = viewModel(
        factory = VentasViewModelFactory(context)
    )

    val uiState by panelViewModel.uiState.collectAsState()

    // Se recarga cada vez que se entra, así los números quedan al día.
    LaunchedEffect(Unit) {
        panelViewModel.cargarPanel()
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            when (val estado = uiState) {

                is PanelUiState.Cargando -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is PanelUiState.Error -> {
                    Text(text = estado.mensaje, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { panelViewModel.cargarPanel() }) {
                        Text("Reintentar")
                    }
                }

                is PanelUiState.Admin -> PanelAdmin(estado.dashboard, onIrA)

                is PanelUiState.Bodega -> PanelBodega(estado.resumen, onIrA)

                is PanelUiState.Vendedor -> PanelVendedor(estado.resumen, onIrA)
            }
        }
    }
}

// ---------------------------------------------------------------- Administrador

@Composable
private fun PanelAdmin(dashboard: Dashboard, onIrA: (String) -> Unit) {
    val r = dashboard.resumen

    Titulo("📊 Resumen General", "Indicadores de toda la tienda")

    FilaIndicadores(
        Indicador("Total productos", (r?.totalProductos ?: 0).toString()),
        Indicador("Total clientes", (r?.totalClientes ?: 0).toString())
    )
    FilaIndicadores(
        Indicador("Total ventas", (r?.totalVentas ?: 0).toString()),
        Indicador("Ventas hoy", (r?.ventasHoy ?: 0).toString())
    )
    FilaIndicadores(
        Indicador("Ventas del mes", (r?.ventasMes ?: 0).toString()),
        Indicador(
            "Stock bajo", (r?.stockBajo ?: 0).toString(),
            color = if ((r?.stockBajo ?: 0) > 0) Naranja else null
        )
    )
    IndicadorAncho("Ingresos del mes", CatalogoFiltro.formatoPrecio(r?.ingresosMes))

    Seccion("📑 Últimas ventas")
    val reportes = dashboard.reportes.orEmpty()
    if (reportes.isEmpty()) {
        Text("Todavía no hay ventas registradas.")
    } else {
        reportes.forEach { reporte ->
            Column {
                Text(text = reporte.titulo.orEmpty(), fontWeight = FontWeight.Bold)
                Text(text = reporte.descripcion.orEmpty())
                Text(
                    text = CompraUtils.fechaCorta(reporte.fecha),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            HorizontalDivider()
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = { onIrA("ventas") }, modifier = Modifier.weight(1f)) {
            Text("Ver ventas")
        }
        OutlinedButton(onClick = { onIrA("inventario") }, modifier = Modifier.weight(1f)) {
            Text("Inventario")
        }
    }
}

// ---------------------------------------------------------------- Bodeguero

@Composable
private fun PanelBodega(r: ResumenBodega, onIrA: (String) -> Unit) {

    Titulo("📦 Resumen de Bodega", "Estado del inventario")

    FilaIndicadores(
        Indicador("Total productos", r.totalProductos.toString()),
        Indicador("Productos activos", r.activos.toString())
    )
    FilaIndicadores(
        Indicador("Stock bajo (1-5)", r.stockBajo.toString(), color = if (r.stockBajo > 0) Naranja else null),
        Indicador(
            "Sin stock", r.sinStock.toString(),
            color = if (r.sinStock > 0) MaterialTheme.colorScheme.error else null
        )
    )

    Seccion("⚠️ Alertas de inventario")
    if (r.alertas.isEmpty()) {
        Text("No hay productos con stock bajo. ✅")
    } else {
        r.alertas.forEach { producto ->
            val cantidad = producto.cantProducto ?: 0
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = producto.nombreProducto.orEmpty(),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (cantidad <= 0) "Sin stock" else "$cantidad unidades",
                    fontWeight = FontWeight.Bold,
                    color = if (cantidad <= 0) MaterialTheme.colorScheme.error else Naranja
                )
            }
            HorizontalDivider()
        }
    }

    Button(onClick = { onIrA("inventario") }, modifier = Modifier.fillMaxWidth()) {
        Text("Ir al inventario")
    }
}

// ---------------------------------------------------------------- Vendedor

@Composable
private fun PanelVendedor(r: ResumenVendedor, onIrA: (String) -> Unit) {

    Titulo("Inicio de ventas", "Panel del vendedor")

    FilaIndicadores(
        Indicador("Ventas realizadas", r.realizadas.toString()),
        Indicador("Ventas completadas", r.completadas.toString())
    )
    FilaIndicadores(
        Indicador("Ventas de hoy", r.deHoy.toString()),
        Indicador("Ingresos generados", CatalogoFiltro.formatoPrecio(r.ingresos))
    )

    Seccion("Ventas recientes")
    if (r.recientes.isEmpty()) {
        Text("Todavía no has registrado ventas.")
    } else {
        r.recientes.forEach { venta ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Venta #${venta.id ?: ""}", fontWeight = FontWeight.Bold)
                    Text(
                        text = "${VentaUtils.nombreCliente(venta)} · ${CompraUtils.fechaCorta(venta.fecha)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(CatalogoFiltro.formatoPrecio(venta.total))
                    Text(
                        text = CheckoutUtils.estadoTexto(venta),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            HorizontalDivider()
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = { onIrA("ventas/nueva") }, modifier = Modifier.weight(1f)) {
            Text("Nueva venta")
        }
        OutlinedButton(onClick = { onIrA("ventas") }, modifier = Modifier.weight(1f)) {
            Text("Ver historial")
        }
    }
}

// ---------------------------------------------------------------- Piezas comunes

private data class Indicador(val titulo: String, val valor: String, val color: Color? = null)

@Composable
private fun Titulo(titulo: String, subtitulo: String) {
    Column {
        Text(text = titulo, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = subtitulo,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Seccion(titulo: String) {
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = titulo, style = MaterialTheme.typography.titleMedium)
}

// Dos tarjetas de indicador lado a lado.
@Composable
private fun FilaIndicadores(izquierda: Indicador, derecha: Indicador) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TarjetaIndicador(izquierda, Modifier.weight(1f))
        TarjetaIndicador(derecha, Modifier.weight(1f))
    }
}

@Composable
private fun IndicadorAncho(titulo: String, valor: String) {
    TarjetaIndicador(Indicador(titulo, valor), Modifier.fillMaxWidth())
}

@Composable
private fun TarjetaIndicador(indicador: Indicador, modifier: Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = indicador.titulo,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = indicador.valor,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = indicador.color ?: MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
