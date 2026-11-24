package com.example.mjbebikesaplicativomovil

import com.example.mjbebikesaplicativomovil.data.repository.ItemCarrito
import com.example.mjbebikesaplicativomovil.remote.models.Cliente
import com.example.mjbebikesaplicativomovil.remote.models.Producto
import com.example.mjbebikesaplicativomovil.remote.models.ProductoVenta
import com.example.mjbebikesaplicativomovil.remote.models.Venta
import com.example.mjbebikesaplicativomovil.utils.CheckoutFormulario
import com.example.mjbebikesaplicativomovil.utils.CheckoutUtils
import com.example.mjbebikesaplicativomovil.utils.MetodoPago
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas de finalizar compra y Mis compras.
class CheckoutUtilsTest {

    private val casco = Producto(id = 1, nombreProducto = "Casco", valorUnitario = 85000.0, cantProducto = 5)
    private val guantes = Producto(id = 2, nombreProducto = "Guantes", valorUnitario = 30000.0, cantProducto = 5)
    private val items = listOf(ItemCarrito(casco, 2), ItemCarrito(guantes, 1))

    private val entrega = CheckoutFormulario(telefono = "3001234567", direccion = "Calle 10 # 20-30")
    private val primeraCompra = entrega.copy(
        nombres = "Ana", apellidos = "Gómez", tipoDocumentoId = 1, numeroDocumento = "1020304050"
    )

    @Test
    fun metodoPago_estadoDeLaVenta() {
        assertEquals("completada", MetodoPago.TARJETA.estadoVenta)
        assertEquals("completada", MetodoPago.TRANSFERENCIA.estadoVenta)
        assertEquals("pendiente", MetodoPago.CONTRAENTREGA.estadoVenta)
        assertEquals("contraentrega", MetodoPago.CONTRAENTREGA.valorApi)
    }

    @Test
    fun validar_clienteRegistrado_soloPideEntrega() {
        assertTrue(CheckoutUtils.validar(entrega, esPrimeraCompra = false).isEmpty())
        assertEquals(setOf("telefono", "direccion"),
            CheckoutUtils.validar(CheckoutFormulario(), esPrimeraCompra = false).keys)
    }

    @Test
    fun validar_primeraCompra_pideDatosPersonales() {
        assertEquals(setOf("nombres", "apellidos", "tipoDocumento", "numeroDocumento"),
            CheckoutUtils.validar(entrega, esPrimeraCompra = true).keys)
        assertTrue(CheckoutUtils.validar(primeraCompra, esPrimeraCompra = true).isEmpty())
    }

    @Test
    fun validar_telefonoInvalidoYDireccionLarga() {
        val errores = CheckoutUtils.validar(
            entrega.copy(telefono = "abc", direccion = "a".repeat(151)), esPrimeraCompra = false
        )
        assertEquals(setOf("telefono", "direccion"), errores.keys)
    }

    @Test
    fun armarVenta_conLosProductosDelCarrito() {
        val venta = CheckoutUtils.armarVenta(
            usuarioId = 6, idCliente = 3, items = items,
            f = entrega.copy(metodoPago = MetodoPago.CONTRAENTREGA), fecha = "2026-11-24"
        )
        assertEquals(6, venta.userId)
        assertEquals(3, venta.cliente)
        assertEquals(2 * 85000.0 + 30000.0, venta.total, 0.001)
        assertEquals(listOf(1, 2), venta.productos.map { it.id })
        assertEquals(2, venta.productos[0].cantidad)
        assertEquals("contraentrega", venta.metodoPago)
        assertEquals("pendiente", venta.estado)
        assertEquals("Calle 10 # 20-30", venta.direccion)
    }

    @Test
    fun aFormularioCliente_creaElRegistroDelUsuario() {
        val cliente = CheckoutUtils.aFormularioCliente(primeraCompra, usuarioId = 6).aCliente()
        assertEquals(6, cliente.usuarioId)
        assertEquals("Ana", cliente.nombres)
        assertEquals("Calle 10 # 20-30", cliente.direccion)
    }

    @Test
    fun desde_llenaLosDatosDelClienteRegistrado() {
        val f = CheckoutFormulario.desde(Cliente(nombres = "Ana", telefono = "300", direccion = "Calle 1"))
        assertEquals("300", f.telefono)
        assertEquals("Calle 1", f.direccion)
        assertEquals(MetodoPago.TARJETA, f.metodoPago)
    }

    @Test
    fun misCompras_estadoYUnidades() {
        val pagada = Venta(pago = true, productos = listOf(ProductoVenta(cantidad = 2), ProductoVenta(cantidad = 1)))
        val pendiente = Venta(pago = false, estado = "pendiente")
        assertEquals("Pagada", CheckoutUtils.estadoTexto(pagada))
        assertEquals("Pendiente", CheckoutUtils.estadoTexto(pendiente))
        assertEquals(3, CheckoutUtils.unidades(pagada))
        assertEquals(0, CheckoutUtils.unidades(pendiente))
    }
}
