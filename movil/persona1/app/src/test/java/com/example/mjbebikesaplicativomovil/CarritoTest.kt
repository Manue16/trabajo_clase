package com.example.mjbebikesaplicativomovil

import com.example.mjbebikesaplicativomovil.data.repository.Carrito
import com.example.mjbebikesaplicativomovil.remote.models.Producto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas de la lógica del carrito (no necesitan emulador ni API).
class CarritoTest {

    private val casco = Producto(id = 1, nombreProducto = "Casco", valorUnitario = 85000.0, cantProducto = 2)
    private val guantes = Producto(id = 2, nombreProducto = "Guantes", valorUnitario = 30000.0, cantProducto = 10)
    private val sinId = Producto(nombreProducto = "Sin id", valorUnitario = 1000.0)

    @Test
    fun agregar_productoNuevo_quedaConCantidad1() {
        val carrito = Carrito()
        assertTrue(carrito.agregar(casco))
        assertEquals(1, carrito.items.value.size)
        assertEquals(1, carrito.items.value[0].cantidad)
    }

    @Test
    fun agregar_productoRepetido_sumaLaCantidad() {
        val carrito = Carrito()
        carrito.agregar(guantes)
        carrito.agregar(guantes)
        assertEquals(1, carrito.items.value.size)
        assertEquals(2, carrito.items.value[0].cantidad)
    }

    @Test
    fun agregar_noPermiteSuperarElStock() {
        val carrito = Carrito()
        assertTrue(carrito.agregar(casco))
        assertTrue(carrito.agregar(casco))
        assertFalse(carrito.agregar(casco))      // stock = 2
        assertEquals(2, carrito.items.value[0].cantidad)
    }

    @Test
    fun agregar_productoSinId_noSeAgrega() {
        val carrito = Carrito()
        assertFalse(carrito.agregar(sinId))
        assertTrue(carrito.items.value.isEmpty())
    }

    @Test
    fun cambiarCantidad_actualizaYRespetaElStock() {
        val carrito = Carrito()
        carrito.agregar(guantes)
        assertTrue(carrito.cambiarCantidad(2, 5))
        assertEquals(5, carrito.items.value[0].cantidad)
        assertFalse(carrito.cambiarCantidad(2, 11))  // stock = 10
        assertEquals(5, carrito.items.value[0].cantidad)
    }

    @Test
    fun cambiarCantidad_menorQue1_quitaElProducto() {
        val carrito = Carrito()
        carrito.agregar(guantes)
        carrito.cambiarCantidad(2, 0)
        assertTrue(carrito.items.value.isEmpty())
    }

    @Test
    fun quitar_yVaciar() {
        val carrito = Carrito()
        carrito.agregar(casco)
        carrito.agregar(guantes)
        carrito.quitar(1)
        assertEquals(listOf(2), carrito.items.value.map { it.producto.id })
        carrito.vaciar()
        assertTrue(carrito.items.value.isEmpty())
    }

    @Test
    fun totales_sumanUnidadesYPrecio() {
        val carrito = Carrito()
        carrito.agregar(casco)
        carrito.agregar(casco)
        carrito.agregar(guantes)
        assertEquals(3, carrito.cantidadTotal())
        assertEquals(85000.0 * 2 + 30000.0, carrito.precioTotal(), 0.001)
    }
}
