package com.example.mjbebikesaplicativomovil

import com.example.mjbebikesaplicativomovil.remote.models.Usuario
import com.example.mjbebikesaplicativomovil.utils.EstadoFiltro
import com.example.mjbebikesaplicativomovil.utils.PasswordUtils
import com.example.mjbebikesaplicativomovil.utils.Roles
import com.example.mjbebikesaplicativomovil.utils.UsuarioFormulario
import com.example.mjbebikesaplicativomovil.utils.UsuarioUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas de la administración de usuarios.
class UsuarioUtilsTest {

    private val admin = Usuario(id = 1, login = "admin1", email = "admin1@mjbe.com", rolId = 1, estado = "Activo")
    private val vend = Usuario(id = 2, login = "vend1", email = "vend1@mjbe.com", rolId = 2, estado = "activo")
    private val bod = Usuario(id = 4, login = "bod1", email = "bod1@mjbe.com", rolId = 3, estado = "Inactivo")
    private val existentes = listOf(admin, vend, bod)

    private val nuevo = UsuarioFormulario(login = "vend2", email = "vend2@mjbe.com", password = "clave123", rolId = 2)

    @Test
    fun validar_nuevoCompleto_sinErrores() {
        assertTrue(UsuarioUtils.validar(nuevo, esNuevo = true, existentes = existentes).isEmpty())
    }

    @Test
    fun validar_nuevoVacio_pideTodo() {
        assertEquals(setOf("login", "email", "password", "rol"),
            UsuarioUtils.validar(UsuarioFormulario(), esNuevo = true, existentes = existentes).keys)
    }

    @Test
    fun validar_edicion_permiteContrasenaVacia() {
        val f = UsuarioFormulario.desde(vend)
        assertTrue(UsuarioUtils.validar(f, esNuevo = false, existentes = existentes, idActual = 2).isEmpty())
    }

    @Test
    fun validar_usuarioYCorreoRepetidos_sinImportarMayusculas() {
        val f = nuevo.copy(login = "VEND1", email = "Admin1@MJBE.com")
        val errores = UsuarioUtils.validar(f, esNuevo = true, existentes = existentes)
        assertEquals("Ya existe un usuario con ese nombre", errores["login"])
        assertEquals("Ya existe un usuario con ese correo", errores["email"])
    }

    @Test
    fun validar_correoInvalidoYUsuarioConEspacios() {
        val errores = UsuarioUtils.validar(nuevo.copy(login = "vend 2", email = "correo-invalido"),
            esNuevo = true, existentes = existentes)
        assertEquals(setOf("login", "email"), errores.keys)
    }

    @Test
    fun aGuardar_cifraLaContrasenaYNormalizaElCorreo() {
        val g = UsuarioUtils.aGuardar(nuevo.copy(email = "  Vend2@MJBE.com "))
        assertEquals(PasswordUtils.sha256("clave123"), g.passwordHarsh)
        assertEquals("vend2@mjbe.com", g.email)
        assertEquals(2, g.rolId)
    }

    @Test
    fun aGuardar_contrasenaVacia_noSeEnvia() {
        assertNull(UsuarioUtils.aGuardar(UsuarioFormulario.desde(vend)).passwordHarsh)
    }

    @Test
    fun conEstadoContrario_activaYDesactiva() {
        assertEquals("Inactivo", UsuarioUtils.conEstadoContrario(vend).estado)
        assertEquals("Activo", UsuarioUtils.conEstadoContrario(bod).estado)
        assertNull(UsuarioUtils.conEstadoContrario(vend).passwordHarsh)
    }

    @Test
    fun filtrar_porTextoRolYEstado() {
        assertEquals(listOf(vend), UsuarioUtils.filtrar(existentes, "vend", null, EstadoFiltro.TODOS))
        assertEquals(listOf(bod), UsuarioUtils.filtrar(existentes, "", Roles.BODEGUERO, EstadoFiltro.TODOS))
        assertEquals(listOf(admin, vend), UsuarioUtils.filtrar(existentes, "", null, EstadoFiltro.ACTIVOS))
        assertEquals(listOf(bod), UsuarioUtils.filtrar(existentes, "mjbe", null, EstadoFiltro.INACTIVOS))
    }
}
