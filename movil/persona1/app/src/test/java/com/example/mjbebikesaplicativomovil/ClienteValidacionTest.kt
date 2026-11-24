package com.example.mjbebikesaplicativomovil

import com.example.mjbebikesaplicativomovil.remote.models.Cliente
import com.example.mjbebikesaplicativomovil.utils.ApiErrores
import com.example.mjbebikesaplicativomovil.utils.ClienteFormulario
import com.example.mjbebikesaplicativomovil.utils.ClienteValidacion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas del formulario de clientes y de la lectura de errores de la API.
class ClienteValidacionTest {

    private val valido = ClienteFormulario(
        usuarioId = 6,
        tipoDocumentoId = 1,
        numeroDocumento = "1020304050",
        nombres = "Ana",
        apellidos = "Gómez",
        direccion = "Calle 10 # 20-30",
        telefono = "300 123 4567"
    )

    @Test
    fun validar_formularioCompleto_sinErrores() {
        assertTrue(ClienteValidacion.validar(valido).isEmpty())
    }

    @Test
    fun validar_formularioVacio_marcaTodosLosCampos() {
        val errores = ClienteValidacion.validar(ClienteFormulario())
        assertEquals(
            setOf("usuario", "tipoDocumento", "numeroDocumento", "nombres", "apellidos", "direccion", "telefono"),
            errores.keys
        )
    }

    @Test
    fun validar_soloEspacios_cuentaComoVacio() {
        val errores = ClienteValidacion.validar(valido.copy(nombres = "   "))
        assertEquals(setOf("nombres"), errores.keys)
    }

    @Test
    fun validar_respetaLongitudesMaximas() {
        val errores = ClienteValidacion.validar(
            valido.copy(numeroDocumento = "1".repeat(31), direccion = "a".repeat(151))
        )
        assertEquals(setOf("numeroDocumento", "direccion"), errores.keys)
    }

    @Test
    fun validar_telefonoConLetras_esInvalido() {
        val errores = ClienteValidacion.validar(valido.copy(telefono = "300abc"))
        assertEquals(setOf("telefono"), errores.keys)
    }

    @Test
    fun aCliente_quitaEspaciosSobrantes() {
        val cliente = valido.copy(nombres = "  Ana  ").aCliente()
        assertEquals("Ana", cliente.nombres)
        assertEquals(null, cliente.id)
    }

    @Test
    fun filtrar_porNombreDocumentoOTelefono() {
        val ana = Cliente(id = 1, nombres = "Ana", apellidos = "Gómez", numeroDocumento = "111", telefono = "300")
        val luis = Cliente(id = 2, nombres = "Luis", apellidos = "Pérez", numeroDocumento = "222", telefono = "310")
        val todos = listOf(ana, luis)
        assertEquals(listOf(ana), ClienteValidacion.filtrar(todos, "ana"))
        assertEquals(listOf(luis), ClienteValidacion.filtrar(todos, "222"))
        assertEquals(todos, ClienteValidacion.filtrar(todos, " "))
    }

    @Test
    fun apiErrores_leeLosErroresDeLaravel() {
        assertEquals(ApiErrores.SESION_VENCIDA,
            ApiErrores.mensaje(401, """{"message":"Unauthenticated."}""", "x"))
        assertEquals("El campo nombres es obligatorio.",
            ApiErrores.mensaje(422, """{"mensaje":"Error de validación","errores":{"nombres":["El campo nombres es obligatorio."]}}""", "x"))
        assertEquals("Cliente no encontrado",
            ApiErrores.mensaje(404, """{"mensaje":"Cliente no encontrado"}""", "x"))
        assertEquals("No se pudo guardar (500)",
            ApiErrores.mensaje(500, "<html>error</html>", "No se pudo guardar"))
    }
}
