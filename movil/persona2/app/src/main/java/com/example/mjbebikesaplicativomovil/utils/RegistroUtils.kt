package com.example.mjbebikesaplicativomovil.utils

import com.example.mjbebikesaplicativomovil.remote.models.RegistroRequest

// Datos del formulario de registro de cliente.
data class RegistroFormulario(
    val login: String = "",
    val email: String = "",
    val password: String = "",
    val confirmar: String = ""
)

object RegistroUtils {

    private val formatoEmail = Regex("""^[^\s@]+@[^\s@]+\.[^\s@]+$""")

    // Mismas reglas de la web: todos los campos obligatorios y las contraseñas iguales.
    // Además se revisa el formato del correo y que el usuario no tenga espacios.
    fun validar(f: RegistroFormulario): Map<String, String> {
        val errores = mutableMapOf<String, String>()

        val login = f.login.trim()
        when {
            login.isEmpty() -> errores["login"] = "El usuario es obligatorio"
            login.length > 255 -> errores["login"] = "Máximo 255 caracteres"
            login.contains(' ') -> errores["login"] = "El usuario no puede tener espacios"
        }

        val email = f.email.trim()
        when {
            email.isEmpty() -> errores["email"] = "El correo es obligatorio"
            !formatoEmail.matches(email) -> errores["email"] = "Escribe un correo válido"
            email.length > 255 -> errores["email"] = "Máximo 255 caracteres"
        }

        if (f.password.isEmpty()) errores["password"] = "La contraseña es obligatoria"

        when {
            f.confirmar.isEmpty() -> errores["confirmar"] = "Confirma la contraseña"
            f.confirmar != f.password -> errores["confirmar"] = "Las contraseñas no coinciden."
        }

        return errores
    }

    // Lo que se envía a la API: la contraseña cifrada y el correo en minúsculas (como la web).
    fun aRequest(f: RegistroFormulario): RegistroRequest = RegistroRequest(
        login = f.login.trim(),
        email = f.email.trim().lowercase(),
        passwordHarsh = PasswordUtils.sha256(f.password)
    )
}
