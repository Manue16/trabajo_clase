package com.example.mjbebikesaplicativomovil

import com.example.mjbebikesaplicativomovil.remote.models.Producto
import com.example.mjbebikesaplicativomovil.remote.models.Venta
import com.example.mjbebikesaplicativomovil.utils.PanelUtils
import org.junit.Assert.assertEquals
import org.junit.Test

// Pruebas de los indicadores de los paneles de bodega y del vendedor.
class PanelUtilsTest {

    @Test
    fun resumenBodega_cuentaStockBajoSinStockYActivos() {
        val productos = listOf(
            Producto(id = 1, cantProducto = 10, estado = "activo"),
            Producto(id = 2, cantProducto = 3, estado = "Activo"),
            Producto(id = 3, cantProducto = 0, estado = "activo"),
            Producto(id = 4, cantProducto = 5, estado = "inactivo")
        )
        val r = PanelUtils.resumenBodega(productos)
        assertEquals(4, r.totalProductos)
        assertEquals(3, r.activos)
        assertEquals(2, r.stockBajo)     // 3 y 5 unidades
        assertEquals(1, r.sinStock)      // 0 unidades
        assertEquals(listOf(3, 2, 4), r.alertas.map { it.id })   // de menos a más stock
    }

    @Test
    fun resumenBodega_sinProductos() {
        val r = PanelUtils.resumenBodega(emptyList())
        assertEquals(0, r.totalProductos)
        assertEquals(0, r.alertas.size)
    }

    @Test
    fun resumenVendedor_soloSusVentas() {
        val ventas = listOf(
            Venta(id = 9, idVendedor = 2, total = 100000.0, pago = true, fecha = "2026-11-24"),
            Venta(id = 8, idVendedor = 2, total = 50000.0, pago = false, fecha = "2026-11-23 10:00:00"),
            Venta(id = 7, idVendedor = 5, total = 999999.0, pago = true, fecha = "2026-11-24"),
            Venta(id = 6, idVendedor = 2, total = 25000.0, estado = "completada", fecha = "2026-11-24T15:00:00Z")
        )
        val r = PanelUtils.resumenVendedor(ventas, vendedorId = 2, hoy = "2026-11-24")
        assertEquals(3, r.realizadas)
        assertEquals(2, r.completadas)
        assertEquals(175000.0, r.ingresos, 0.001)
        assertEquals(2, r.deHoy)
        assertEquals(listOf(9, 8, 6), r.recientes.map { it.id })
    }

    @Test
    fun resumenVendedor_recientesComoMaximo5() {
        val ventas = (1..8).map { Venta(id = it, idVendedor = 2, total = 1.0) }
        assertEquals(5, PanelUtils.resumenVendedor(ventas, 2, "2026-11-24").recientes.size)
    }
}
