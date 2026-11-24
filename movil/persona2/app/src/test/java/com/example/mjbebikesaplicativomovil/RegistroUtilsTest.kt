package com.example.mjbebikesaplicativomovil

import com.example.mjbebikesaplicativomovil.utils.PasswordUtils
import com.example.mjbebikesaplicativomovil.utils.RegistroFormulario
import com.example.mjbebikesaplicativomovil.utils.RegistroUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas del registro de clientes (mismos casos que la caja negra de la web).
class RegistroUtilsTest {

    private val valido = RegistroFormulario(
        login = "cliente1",
        email = "cliente1@test.com",
        password = "Clave123",
        confirmar = "Clave123"
    )

    @Test
    fun validar_formularioCompleto_sinErrores() {
        assertTrue(RegistroUtils.validar(valido).isEmpty())
    }

    @Test
    fun validar_camposVacios() {
        assertEquals(setOf("login", "email", "password", "confirmar"),
            RegistroUtils.validar(RegistroFormulario()).keys)
    }

    @Test
    fun validar_faltaConfirmarLaContrasena() {
        assertEquals(setOf("confirmar"), RegistroUtils.validar(valido.copy(confirmar = "")).keys)
    }

    @Test
    fun validar_contrasenasDistintas() {
        assertEquals("Las contraseñas no coinciden.",
            RegistroUtils.validar(valido.copy(confirmar = "Clave124"))["confirmar"])
    }

    @Test
    fun validar_correoInvalidoYUsuarioConEspacios() {
        assertEquals(setOf("login", "email"),
            RegistroUtils.validar(valido.copy(login = "cliente 1", email = "correo-invalido")).keys)
    }

    @Test
    fun aRequest_laContrasenaNoViajaEnTextoPlano() {
        val request = RegistroUtils.aRequest(valido.copy(email = "  Cliente1@TEST.com "))
        assertNotEquals("Clave123", request.passwordHarsh)
        assertEquals(PasswordUtils.sha256("Clave123"), request.passwordHarsh)
        assertTrue(request.passwordHarsh.matches(Regex("^[0-9a-f]{64}$")))
        assertEquals("cliente1@test.com", request.email)
    }
}
