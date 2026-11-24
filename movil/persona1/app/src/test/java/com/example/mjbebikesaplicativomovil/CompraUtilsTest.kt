package com.example.mjbebikesaplicativomovil

import com.example.mjbebikesaplicativomovil.remote.models.Compra
import com.example.mjbebikesaplicativomovil.remote.models.Proveedor
import com.example.mjbebikesaplicativomovil.utils.CompraFormulario
import com.example.mjbebikesaplicativomovil.utils.CompraUtils
import com.example.mjbebikesaplicativomovil.utils.LineaCompra
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas del formulario de compras, el total y los filtros.
class CompraUtilsTest {

    private val valida = CompraFormulario(
        idProveedor = 1,
        fecha = "2026-11-24",
        pago = true,
        lineas = listOf(
            LineaCompra(idProducto = 10, cantidad = "3", precio = "20000"),
            LineaCompra(idProducto = 11, cantidad = "2", precio = "15000,50")
        )
    )

    @Test
    fun total_sumaCantidadPorCosto() {
        assertEquals(3 * 20000.0 + 2 * 15000.5, valida.total, 0.001)
    }

    @Test
    fun total_ignoraFilasIncompletas() {
        val f = valida.copy(lineas = listOf(LineaCompra(idProducto = 10, cantidad = "", precio = "1000")))
        assertEquals(0.0, f.total, 0.001)
    }

    @Test
    fun validar_formularioCompleto_sinErrores() {
        assertTrue(CompraUtils.validar(valida).isEmpty())
    }

    @Test
    fun validar_formularioNuevo_pideProveedorProductoYCosto() {
        val errores = CompraUtils.validar(CompraFormulario())
        assertEquals(setOf("proveedor", "producto_0", "precio_0"), errores.keys)
    }

    @Test
    fun validar_cantidadCeroYCostoNegativo() {
        val f = valida.copy(lineas = listOf(LineaCompra(idProducto = 10, cantidad = "0", precio = "-1")))
        assertEquals(setOf("cantidad_0", "precio_0"), CompraUtils.validar(f).keys)
    }

    @Test
    fun validar_productoRepetido() {
        val f = valida.copy(lineas = listOf(
            LineaCompra(idProducto = 10, cantidad = "1", precio = "100"),
            LineaCompra(idProducto = 10, cantidad = "2", precio = "100")
        ))
        assertEquals(setOf("producto_0", "producto_1"), CompraUtils.validar(f).keys)
    }

    @Test
    fun validar_sinFilas() {
        assertEquals(setOf("lineas"), CompraUtils.validar(valida.copy(lineas = emptyList())).keys)
    }

    @Test
    fun fecha_validaEInvalida() {
        assertTrue(CompraUtils.esFechaValida("2026-02-28"))
        assertFalse(CompraUtils.esFechaValida("2026-02-30"))
        assertFalse(CompraUtils.esFechaValida("24/11/2026"))
        assertTrue(CompraUtils.esFechaValida(CompraUtils.hoy()))
    }

    @Test
    fun aCompraNuevaYDetalles() {
        val compra = valida.aCompraNueva()
        assertEquals(1, compra.idProveedor)
        assertEquals(90001.0, compra.totalCompra, 0.001)
        val detalles = valida.aDetalles(idCompra = 7)
        assertEquals(2, detalles.size)
        assertEquals(7, detalles[0].idCompra)
        assertEquals(15000.5, detalles[1].precioUnitario, 0.001)
    }

    @Test
    fun fechaCortaYEstado() {
        assertEquals("24/11/2026", CompraUtils.fechaCorta("2026-11-24T00:00:00.000000Z"))
        assertEquals("Sin fecha", CompraUtils.fechaCorta(null))
        assertEquals("Pagada", CompraUtils.estadoTexto(true))
        assertEquals("Pendiente", CompraUtils.estadoTexto(false))
    }

    @Test
    fun filtrar_porNumeroProveedorOEstado() {
        val c1 = Compra(id = 5, pago = true, proveedor = Proveedor(nombreProveedor = "Shimano"))
        val c2 = Compra(id = 6, pago = false, proveedor = Proveedor(nombreProveedor = "GW"))
        val todas = listOf(c1, c2)
        assertEquals(listOf(c2), CompraUtils.filtrar(todas, "#6"))
        assertEquals(listOf(c1), CompraUtils.filtrar(todas, "shim"))
        assertEquals(listOf(c2), CompraUtils.filtrar(todas, "pendiente"))
        assertEquals(todas, CompraUtils.filtrar(todas, ""))
    }
}
