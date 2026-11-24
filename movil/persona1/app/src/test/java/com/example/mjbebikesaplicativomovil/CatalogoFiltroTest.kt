package com.example.mjbebikesaplicativomovil

import com.example.mjbebikesaplicativomovil.remote.models.Producto
import com.example.mjbebikesaplicativomovil.utils.CatalogoFiltro
import com.example.mjbebikesaplicativomovil.utils.OrdenPrecio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas de la lógica del catálogo (no necesitan emulador ni API).
class CatalogoFiltroTest {

    private val casco = Producto(id = 1, nombreProducto = "Casco MTB", descripcion = "Casco ventilado",
        marcaProducto = "Shimano", valorUnitario = 85000.0, estado = "activo")
    private val guantes = Producto(id = 2, nombreProducto = "Guantes", descripcion = "Guantes de gel",
        marcaProducto = "GW", valorUnitario = 30000.0, estado = "Activo")
    private val cadena = Producto(id = 3, nombreProducto = "Cadena", descripcion = "Cadena 11v",
        marcaProducto = "Shimano", valorUnitario = 120000.0, estado = "activo")
    private val inactivo = Producto(id = 4, nombreProducto = "Pedal", marcaProducto = "GW",
        valorUnitario = 50000.0, estado = "inactivo")

    private val todos = listOf(casco, guantes, cadena, inactivo)

    @Test
    fun soloActivos_quitaLosInactivos_sinImportarMayusculas() {
        val activos = CatalogoFiltro.soloActivos(todos)
        assertEquals(listOf(casco, guantes, cadena), activos)
    }

    @Test
    fun marcas_devuelveMarcasSinRepetirYOrdenadas() {
        assertEquals(listOf("GW", "Shimano"), CatalogoFiltro.marcas(todos))
    }

    @Test
    fun filtrar_sinFiltros_devuelveTodoEnElMismoOrden() {
        val activos = CatalogoFiltro.soloActivos(todos)
        assertEquals(activos, CatalogoFiltro.filtrar(activos, "", null, OrdenPrecio.NINGUNO))
    }

    @Test
    fun filtrar_buscaEnNombreDescripcionYMarca() {
        val activos = CatalogoFiltro.soloActivos(todos)
        assertEquals(listOf(casco), CatalogoFiltro.filtrar(activos, "casco", null, OrdenPrecio.NINGUNO))
        assertEquals(listOf(guantes), CatalogoFiltro.filtrar(activos, "GEL", null, OrdenPrecio.NINGUNO))
        assertEquals(listOf(casco, cadena), CatalogoFiltro.filtrar(activos, " shimano ", null, OrdenPrecio.NINGUNO))
    }

    @Test
    fun filtrar_porMarca() {
        val activos = CatalogoFiltro.soloActivos(todos)
        assertEquals(listOf(guantes), CatalogoFiltro.filtrar(activos, "", "GW", OrdenPrecio.NINGUNO))
    }

    @Test
    fun filtrar_ordenaPorPrecio() {
        val activos = CatalogoFiltro.soloActivos(todos)
        assertEquals(listOf(guantes, casco, cadena), CatalogoFiltro.filtrar(activos, "", null, OrdenPrecio.MENOR))
        assertEquals(listOf(cadena, casco, guantes), CatalogoFiltro.filtrar(activos, "", null, OrdenPrecio.MAYOR))
    }

    @Test
    fun filtrar_sinCoincidencias_devuelveListaVacia() {
        val activos = CatalogoFiltro.soloActivos(todos)
        assertTrue(CatalogoFiltro.filtrar(activos, "noexiste", null, OrdenPrecio.NINGUNO).isEmpty())
    }

    @Test
    fun formatoPrecio_usaPuntosDeMiles() {
        assertEquals("$ 85.000", CatalogoFiltro.formatoPrecio(85000.0))
        assertEquals("$ 0", CatalogoFiltro.formatoPrecio(null))
    }
}
