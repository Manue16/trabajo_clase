package com.example.mjbebikesaplicativomovil.utils

import com.example.mjbebikesaplicativomovil.remote.models.Cliente

// Datos que se escriben en el formulario de cliente.
data class ClienteFormulario(
    val usuarioId: Int? = null,
    val tipoDocumentoId: Int? = null,
    val numeroDocumento: String = "",
    val nombres: String = "",
    val apellidos: String = "",
    val direccion: String = "",
    val telefono: String = ""
) {
    // Convierte el formulario en el objeto que se envía a la API (sin espacios sobrantes).
    fun aCliente(): Cliente = Cliente(
        usuarioId = usuarioId,
        tipoDocumentoId = tipoDocumentoId,
        numeroDocumento = numeroDocumento.trim(),
        nombres = nombres.trim(),
        apellidos = apellidos.trim(),
        direccion = direccion.trim(),
        telefono = telefono.trim()
    )

    companion object {
        // Llena el formulario con un cliente existente (para editar).
        fun desde(cliente: Cliente) = ClienteFormulario(
            usuarioId = cliente.usuarioId,
            tipoDocumentoId = cliente.tipoDocumentoId,
            numeroDocumento = cliente.numeroDocumento.orEmpty(),
            nombres = cliente.nombres.orEmpty(),
            apellidos = cliente.apellidos.orEmpty(),
            direccion = cliente.direccion.orEmpty(),
            telefono = cliente.telefono.orEmpty()
        )
    }
}

object ClienteValidacion {

    // Revisa el formulario con las mismas reglas de la API (campos obligatorios y
    // longitudes máximas). Devuelve un mapa campo → mensaje; vacío si todo está bien.
    fun validar(f: ClienteFormulario): Map<String, String> {
        val errores = mutableMapOf<String, String>()

        if (f.usuarioId == null) errores["usuario"] = "Selecciona el usuario asociado al cliente"
        if (f.tipoDocumentoId == null) errores["tipoDocumento"] = "Selecciona el tipo de documento"

        val documento = f.numeroDocumento.trim()
        when {
            documento.isEmpty() -> errores["numeroDocumento"] = "El número de documento es obligatorio"
            documento.length > 30 -> errores["numeroDocumento"] = "Máximo 30 caracteres"
        }

        obligatorio(f.nombres, 100, "nombres", "Los nombres son obligatorios", errores)
        obligatorio(f.apellidos, 100, "apellidos", "Los apellidos son obligatorios", errores)
        obligatorio(f.direccion, 150, "direccion", "La dirección es obligatoria", errores)

        val telefono = f.telefono.trim()
        when {
            telefono.isEmpty() -> errores["telefono"] = "El teléfono es obligatorio"
            telefono.length > 20 -> errores["telefono"] = "Máximo 20 caracteres"
            !telefono.all { it.isDigit() || it == ' ' || it == '+' || it == '-' } ->
                errores["telefono"] = "El teléfono solo puede tener números, espacios, + o -"
        }

        return errores
    }

    private fun obligatorio(
        valor: String, maximo: Int, campo: String, mensaje: String,
        errores: MutableMap<String, String>
    ) {
        val texto = valor.trim()
        when {
            texto.isEmpty() -> errores[campo] = mensaje
            texto.length > maximo -> errores[campo] = "Máximo $maximo caracteres"
        }
    }

    // Nombre completo para mostrar en la lista.
    fun nombreCompleto(cliente: Cliente): String =
        listOfNotNull(cliente.nombres, cliente.apellidos).joinToString(" ").trim()

    // Búsqueda por nombre, apellido, documento o teléfono.
    fun filtrar(clientes: List<Cliente>, busqueda: String): List<Cliente> {
        val texto = busqueda.trim().lowercase()
        if (texto.isEmpty()) return clientes
        return clientes.filter { c ->
            listOf(c.nombres, c.apellidos, c.numeroDocumento, c.telefono)
                .any { it?.lowercase()?.contains(texto) == true }
        }
    }
}
