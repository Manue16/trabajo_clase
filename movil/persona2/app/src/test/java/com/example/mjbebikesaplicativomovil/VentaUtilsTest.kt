package com.example.mjbebikesaplicativomovil

import com.example.mjbebikesaplicativomovil.data.local.SesionUsuario
import com.example.mjbebikesaplicativomovil.remote.models.Cliente
import com.example.mjbebikesaplicativomovil.remote.models.Producto
import com.example.mjbebikesaplicativomovil.remote.models.UsuarioResumen
import com.example.mjbebikesaplicativomovil.remote.models.Venta
import com.example.mjbebikesaplicativomovil.utils.LineaVenta
import com.example.mjbebikesaplicativomovil.utils.NuevaVentaFormulario
import com.example.mjbebikesaplicativomovil.utils.Roles
import com.example.mjbebikesaplicativomovil.utils.VentaUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas de nueva venta y del historial de ventas.
class VentaUtilsTest {

    private val casco = Producto(id = 1, nombreProducto = "Casco", valorUnitario = 85000.0, cantProducto = 3, estado = "activo")
    private val guantes = Producto(id = 2, nombreProducto = "Guantes", valorUnitario = 30000.0, cantProducto = 10, estado = "Activo")
    private val agotado = Producto(id = 3, nombreProducto = "Pedal", valorUnitario = 50000.0, cantProducto = 0, estado = "activo")
    private val inactivo = Producto(id = 4, nombreProducto = "Luz", valorUnitario = 20000.0, cantProducto = 5, estado = "inactivo")
    private val productos = listOf(casco, guantes, agotado, inactivo)

    private val valida = NuevaVentaFormulario(
        idCliente = 7,
        fecha = "2026-11-24",
        lineas = listOf(LineaVenta(1, "2"), LineaVenta(2, "1"))
    )

    @Test
    fun vendibles_soloActivosConStock() {
        assertEquals(listOf(casco, guantes), VentaUtils.vendibles(productos))
    }

    @Test
    fun total_conElPrecioDeCadaProducto() {
        assertEquals(2 * 85000.0 + 30000.0, VentaUtils.total(valida, productos), 0.001)
    }

    @Test
    fun validar_formularioCompleto_sinErrores() {
        assertTrue(VentaUtils.validar(valida, productos).isEmpty())
    }

    @Test
    fun validar_formularioNuevo_pideClienteYProducto() {
        assertEquals(setOf("cliente", "producto_0"), VentaUtils.validar(NuevaVentaFormulario(), productos).keys)
    }

    @Test
    fun validar_cantidadCeroYMayorQueElStock() {
        val cero = valida.copy(lineas = listOf(LineaVenta(1, "0")))
        assertEquals("Mínimo 1", VentaUtils.validar(cero, productos)["cantidad_0"])
        val demasiado = valida.copy(lineas = listOf(LineaVenta(1, "4")))     // stock = 3
        assertEquals("Solo hay 3 unidades", VentaUtils.validar(demasiado, productos)["cantidad_0"])
    }

    @Test
    fun validar_productoRepetido() {
        val f = valida.copy(lineas = listOf(LineaVenta(1, "1"), LineaVenta(1, "1")))
        assertEquals(setOf("producto_0", "producto_1"), VentaUtils.validar(f, productos).keys)
    }

    @Test
    fun armarVenta_enTiendaQuedaPagadaEnEfectivo() {
        val venta = VentaUtils.armarVenta(vendedorId = 2, f = valida, productos = productos)
        assertEquals(2, venta.userId)
        assertEquals(7, venta.cliente)
        assertEquals("efectivo", venta.metodoPago)
        assertEquals("completada", venta.estado)
        assertEquals("Venta en tienda", venta.direccion)
        assertEquals(200000.0, venta.total, 0.001)
        assertEquals(85000.0, venta.productos[0].valorUnitario, 0.001)
    }

    @Test
    fun ventasDe_adminVeTodasYVendedorSoloLasSuyas() {
        val v1 = Venta(id = 1, idVendedor = 2)
        val v2 = Venta(id = 2, idVendedor = 5)
        val todas = listOf(v1, v2)
        assertEquals(todas, VentaUtils.ventasDe(todas, SesionUsuario(1, "admin1", "", Roles.ADMIN)))
        assertEquals(listOf(v1), VentaUtils.ventasDe(todas, SesionUsuario(2, "vend1", "", Roles.VENDEDOR)))
    }

    @Test
    fun nombres_yFiltrado() {
        val v = Venta(id = 9, idCliente = 4, cliente = Cliente(nombres = "Ana", apellidos = "Gómez"),
            vendedor = UsuarioResumen(login = "vend1"))
        val sinDatos = Venta(id = 10, idCliente = 5, idVendedor = 3)
        assertEquals("Ana Gómez", VentaUtils.nombreCliente(v))
        assertEquals("Cliente #5", VentaUtils.nombreCliente(sinDatos))
        assertEquals("Vendedor #3", VentaUtils.nombreVendedor(sinDatos))
        val todas = listOf(v, sinDatos)
        assertEquals(listOf(v), VentaUtils.filtrar(todas, "#9"))
        assertEquals(listOf(v), VentaUtils.filtrar(todas, "ana"))
        assertEquals(listOf(v), VentaUtils.filtrar(todas, "VEND1"))
        assertEquals(todas, VentaUtils.filtrar(todas, ""))
    }
}
