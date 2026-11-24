package com.example.mjbebikesaplicativomovil

import com.example.mjbebikesaplicativomovil.remote.models.Producto
import com.example.mjbebikesaplicativomovil.utils.EstadoFiltro
import com.example.mjbebikesaplicativomovil.utils.InventarioUtils
import com.example.mjbebikesaplicativomovil.utils.NivelStock
import com.example.mjbebikesaplicativomovil.utils.OrdenCantidad
import com.example.mjbebikesaplicativomovil.utils.ProductoFormulario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas del inventario: validación del formulario, niveles de stock y filtros.
class InventarioUtilsTest {

    private val valido = ProductoFormulario(
        imgProducto = "https://ejemplo.com/casco.jpg",
        nombreProducto = "Casco MTB",
        descripcion = "Casco ventilado",
        colorProducto = "Negro",
        marcaProducto = "Shimano",
        modelo = "2026",
        cantidad = "10",
        valorUnitario = "85000",
        idTalla = 1,
        idProveedor = 1,
        idLocal = 1
    )

    private val casco = Producto(id = 1, nombreProducto = "Casco", modelo = "X1", marcaProducto = "Shimano",
        colorProducto = "Negro", cantProducto = 10, estado = "activo")
    private val guantes = Producto(id = 2, nombreProducto = "Guantes", modelo = "G2", marcaProducto = "GW",
        colorProducto = "Rojo", cantProducto = 3, estado = "Activo")
    private val pedal = Producto(id = 3, nombreProducto = "Pedal", modelo = "P3", marcaProducto = "GW",
        colorProducto = "Gris", cantProducto = 0, estado = "inactivo")
    private val todos = listOf(casco, guantes, pedal)

    @Test
    fun validar_formularioCompleto_sinErrores() {
        assertTrue(InventarioUtils.validar(valido).isEmpty())
    }

    @Test
    fun validar_formularioVacio_marcaLosObligatorios() {
        val errores = InventarioUtils.validar(ProductoFormulario())
        assertEquals(
            setOf("imgProducto", "nombreProducto", "descripcion", "colorProducto", "marcaProducto",
                "modelo", "cantidad", "valorUnitario", "talla", "proveedor", "local"),
            errores.keys
        )
    }

    @Test
    fun validar_cantidadNegativaODecimal_esInvalida() {
        assertEquals("La cantidad no puede ser negativa",
            InventarioUtils.validar(valido.copy(cantidad = "-1"))["cantidad"])
        assertEquals("La cantidad debe ser un número entero",
            InventarioUtils.validar(valido.copy(cantidad = "2.5"))["cantidad"])
    }

    @Test
    fun validar_valorUnitario_aceptaComaYRechazaNegativos() {
        assertTrue(InventarioUtils.validar(valido.copy(valorUnitario = "1500,50")).isEmpty())
        assertEquals("El valor unitario no puede ser negativo",
            InventarioUtils.validar(valido.copy(valorUnitario = "-5"))["valorUnitario"])
        assertEquals("El valor unitario debe ser un número",
            InventarioUtils.validar(valido.copy(valorUnitario = "abc"))["valorUnitario"])
    }

    @Test
    fun validar_longitudMaxima() {
        assertEquals("Máximo 100 caracteres",
            InventarioUtils.validar(valido.copy(colorProducto = "a".repeat(101)))["colorProducto"])
    }

    @Test
    fun aProducto_convierteNumerosYQuitaEspacios() {
        val p = valido.copy(nombreProducto = "  Casco  ", valorUnitario = "1500,5").aProducto()
        assertEquals("Casco", p.nombreProducto)
        assertEquals(10, p.cantProducto)
        assertEquals(1500.5, p.valorUnitario!!, 0.001)
        assertEquals(null, p.id)
    }

    @Test
    fun desde_llenaElFormularioParaEditar() {
        val f = ProductoFormulario.desde(casco.copy(valorUnitario = 85000.0, estado = "Activo"))
        assertEquals("10", f.cantidad)
        assertEquals("85000", f.valorUnitario)
        assertEquals("activo", f.estado)
    }

    @Test
    fun nivelStock_sinStockBajoYNormal() {
        assertEquals(NivelStock.SIN_STOCK, InventarioUtils.nivelStock(0))
        assertEquals(NivelStock.SIN_STOCK, InventarioUtils.nivelStock(null))
        assertEquals(NivelStock.BAJO, InventarioUtils.nivelStock(1))
        assertEquals(NivelStock.BAJO, InventarioUtils.nivelStock(5))
        assertEquals(NivelStock.NORMAL, InventarioUtils.nivelStock(6))
    }

    @Test
    fun filtrar_porEstado() {
        assertEquals(listOf(casco, guantes),
            InventarioUtils.filtrar(todos, "", EstadoFiltro.ACTIVOS, false, OrdenCantidad.NINGUNO))
        assertEquals(listOf(pedal),
            InventarioUtils.filtrar(todos, "", EstadoFiltro.INACTIVOS, false, OrdenCantidad.NINGUNO))
    }

    @Test
    fun filtrar_stockBajoIncluyeSinStock() {
        assertEquals(listOf(guantes, pedal),
            InventarioUtils.filtrar(todos, "", EstadoFiltro.TODOS, true, OrdenCantidad.NINGUNO))
    }

    @Test
    fun filtrar_busquedaYOrden() {
        assertEquals(listOf(guantes, pedal),
            InventarioUtils.filtrar(todos, "gw", EstadoFiltro.TODOS, false, OrdenCantidad.MAYOR))
        assertEquals(listOf(pedal, guantes, casco),
            InventarioUtils.filtrar(todos, "", EstadoFiltro.TODOS, false, OrdenCantidad.MENOR))
        assertEquals(listOf(casco),
            InventarioUtils.filtrar(todos, "negro", EstadoFiltro.TODOS, false, OrdenCantidad.NINGUNO))
    }
}
