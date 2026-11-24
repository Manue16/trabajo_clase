package com.example.mjbebikesaplicativomovil.utils

import com.example.mjbebikesaplicativomovil.remote.models.Producto
import com.example.mjbebikesaplicativomovil.remote.models.Venta

// Indicadores del panel de bodega (igual que InicioBodega de la web).
data class ResumenBodega(
    val totalProductos: Int,
    val activos: Int,
    // Entre 1 y 5 unidades.
    val stockBajo: Int,
    // 0 unidades.
    val sinStock: Int,
    // Productos con stock bajo o agotados, del que menos tiene al que más.
    val alertas: List<Producto>
)

// Indicadores del panel del vendedor (igual que InicioVentas de la web).
data class ResumenVendedor(
    val realizadas: Int,
    val completadas: Int,
    val ingresos: Double,
    val deHoy: Int,
    // Sus 5 ventas más recientes.
    val recientes: List<Venta>
)

object PanelUtils {

    fun resumenBodega(productos: List<Producto>): ResumenBodega {
        val niveles = productos.map { it to InventarioUtils.nivelStock(it.cantProducto) }

        return ResumenBodega(
            totalProductos = productos.size,
            activos = productos.count { InventarioUtils.esActivo(it) },
            stockBajo = niveles.count { it.second == NivelStock.BAJO },
            sinStock = niveles.count { it.second == NivelStock.SIN_STOCK },
            alertas = niveles
                .filter { it.second != NivelStock.NORMAL }
                .map { it.first }
                .sortedBy { it.cantProducto ?: 0 }
        )
    }

    // hoy: fecha de hoy en formato "yyyy-MM-dd" (se recibe como parámetro para poder probarlo).
    fun resumenVendedor(ventas: List<Venta>, vendedorId: Int, hoy: String): ResumenVendedor {
        val propias = ventas.filter { it.idVendedor == vendedorId }

        return ResumenVendedor(
            realizadas = propias.size,
            completadas = propias.count { CheckoutUtils.estadoTexto(it) == "Pagada" },
            ingresos = propias.sumOf { it.total ?: 0.0 },
            deHoy = propias.count { it.fecha?.take(10) == hoy },
            // La API ya las envía de la más reciente a la más antigua.
            recientes = propias.take(5)
        )
    }
}
