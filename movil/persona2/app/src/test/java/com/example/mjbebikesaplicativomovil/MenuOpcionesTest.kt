package com.example.mjbebikesaplicativomovil

import com.example.mjbebikesaplicativomovil.utils.MenuOpciones
import com.example.mjbebikesaplicativomovil.utils.Roles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas de los permisos por rol (tabla de decisión rol / pantalla, igual que en la web).
class MenuOpcionesTest {

    private fun titulos(rol: Int?) = MenuOpciones.opcionesDe(rol).map { it.titulo }

    @Test
    fun menu_administrador() {
        assertEquals(
            listOf("Tienda", "Inventario", "Proveedores", "Compras", "Clientes", "Ventas", "Usuarios", "Panel"),
            titulos(Roles.ADMIN)
        )
    }

    @Test
    fun menu_vendedor() {
        assertEquals(listOf("Tienda", "Clientes", "Nueva venta", "Ventas", "Panel"), titulos(Roles.VENDEDOR))
    }

    @Test
    fun menu_bodeguero() {
        assertEquals(listOf("Inventario", "Panel"), titulos(Roles.BODEGUERO))
    }

    @Test
    fun menu_cliente() {
        assertEquals(listOf("Tienda", "Mi carrito", "Mis compras"), titulos(Roles.CLIENTE))
    }

    @Test
    fun menu_sinSesion_estaVacio() {
        assertTrue(titulos(null).isEmpty())
    }

    @Test
    fun tienda_esPublica() {
        assertTrue(MenuOpciones.puedeEntrar(null, "catalogo"))
        assertTrue(MenuOpciones.puedeEntrar(null, "producto/5"))
        assertTrue(MenuOpciones.puedeEntrar(null, "carrito"))
    }

    @Test
    fun sinSesion_noEntraAPantallasProtegidas() {
        assertFalse(MenuOpciones.puedeEntrar(null, "clientes"))
        assertFalse(MenuOpciones.puedeEntrar(null, "inventario"))
        assertFalse(MenuOpciones.puedeEntrar(null, "mis-compras"))
    }

    @Test
    fun rutasInternas_usanElPermisoDelModulo() {
        assertTrue(MenuOpciones.puedeEntrar(Roles.VENDEDOR, "clientes/nuevo"))
        assertTrue(MenuOpciones.puedeEntrar(Roles.BODEGUERO, "inventario/3/editar"))
        assertFalse(MenuOpciones.puedeEntrar(Roles.BODEGUERO, "compras/3"))
    }

    @Test
    fun nuevaVenta_soloVendedor_aunqueAdminVeaVentas() {
        assertTrue(MenuOpciones.puedeEntrar(Roles.VENDEDOR, "ventas/nueva"))
        assertFalse(MenuOpciones.puedeEntrar(Roles.ADMIN, "ventas/nueva"))
        assertTrue(MenuOpciones.puedeEntrar(Roles.ADMIN, "ventas"))
        assertTrue(MenuOpciones.puedeEntrar(Roles.ADMIN, "ventas/12"))
    }

    @Test
    fun permisosNegados_comoEnLaWeb() {
        assertFalse(MenuOpciones.puedeEntrar(Roles.VENDEDOR, "usuarios"))
        assertFalse(MenuOpciones.puedeEntrar(Roles.BODEGUERO, "clientes"))
        assertFalse(MenuOpciones.puedeEntrar(Roles.CLIENTE, "inventario"))
        assertFalse(MenuOpciones.puedeEntrar(Roles.CLIENTE, "ventas"))
        assertFalse(MenuOpciones.puedeEntrar(Roles.ADMIN, "mis-compras"))
    }

    @Test
    fun rutaDesconocida_noSePermite() {
        assertFalse(MenuOpciones.puedeEntrar(Roles.ADMIN, "otra-cosa"))
    }

    @Test
    fun nombresDeRol() {
        assertEquals("Administrador", Roles.nombre(1))
        assertEquals("Cliente", Roles.nombre(4))
        assertEquals("Sin rol", Roles.nombre(null))
    }
}
