package com.example.mjbebikesaplicativomovil

import com.example.mjbebikesaplicativomovil.remote.models.Proveedor
import com.example.mjbebikesaplicativomovil.utils.EstadoFiltro
import com.example.mjbebikesaplicativomovil.utils.ProveedorFormulario
import com.example.mjbebikesaplicativomovil.utils.ProveedorValidacion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas del formulario y de los filtros de proveedores.
class ProveedorValidacionTest {

    private val valido = ProveedorFormulario(
        nombreProveedor = "Shimano Colombia",
        tipoDocumentoId = 4,
        numeroIdentidad = "900123456-7",
        direccion = "Calle 80 # 10-20",
        telefono = "+57 601 555 1234"
    )

    private val shimano = Proveedor(id = 1, nombreProveedor = "Shimano", numeroIdentidad = "900111",
        telefono = "3001112233", estado = "activo")
    private val gw = Proveedor(id = 2, nombreProveedor = "GW Bikes", numeroIdentidad = "900222",
        telefono = "3104445566", estado = "inactivo")
    private val todos = listOf(shimano, gw)

    @Test
    fun validar_formularioCompleto_sinErrores() {
        assertTrue(ProveedorValidacion.validar(valido).isEmpty())
    }

    @Test
    fun validar_formularioVacio_marcaLosObligatorios() {
        val errores = ProveedorValidacion.validar(ProveedorFormulario())
        assertEquals(
            setOf("nombreProveedor", "tipoDocumento", "numeroIdentidad", "direccion", "telefono"),
            errores.keys
        )
    }

    @Test
    fun validar_longitudesMaximas() {
        val errores = ProveedorValidacion.validar(
            valido.copy(nombreProveedor = "a".repeat(101), numeroIdentidad = "1".repeat(31))
        )
        assertEquals(setOf("nombreProveedor", "numeroIdentidad"), errores.keys)
    }

    @Test
    fun validar_telefonoConLetras_esInvalido() {
        assertEquals(setOf("telefono"), ProveedorValidacion.validar(valido.copy(telefono = "abc")).keys)
    }

    @Test
    fun aProveedor_quitaEspaciosYConservaEstado() {
        val p = valido.copy(nombreProveedor = "  Shimano  ", estado = "inactivo").aProveedor()
        assertEquals("Shimano", p.nombreProveedor)
        assertEquals("inactivo", p.estado)
        assertEquals(null, p.id)
    }

    @Test
    fun desde_normalizaElEstadoAMinuscula() {
        assertEquals("activo", ProveedorFormulario.desde(shimano.copy(estado = "Activo")).estado)
    }

    @Test
    fun filtrar_porTextoYEstado() {
        assertEquals(listOf(gw), ProveedorValidacion.filtrar(todos, "gw", EstadoFiltro.TODOS))
        assertEquals(listOf(shimano), ProveedorValidacion.filtrar(todos, "", EstadoFiltro.ACTIVOS))
        assertEquals(listOf(gw), ProveedorValidacion.filtrar(todos, "900", EstadoFiltro.INACTIVOS))
        assertEquals(listOf(shimano), ProveedorValidacion.filtrar(todos, "300111", EstadoFiltro.TODOS))
    }
}
