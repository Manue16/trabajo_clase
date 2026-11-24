package com.example.mjbebikesaplicativomovil.utils

import com.example.mjbebikesaplicativomovil.remote.models.Proveedor

// Datos que se escriben en el formulario de proveedor.
data class ProveedorFormulario(
    val nombreProveedor: String = "",
    val tipoDocumentoId: Int? = null,
    val numeroIdentidad: String = "",
    val direccion: String = "",
    val telefono: String = "",
    val estado: String = "activo"
) {
    // Convierte el formulario en el proveedor que se envía a la API.
    fun aProveedor(): Proveedor = Proveedor(
        nombreProveedor = nombreProveedor.trim(),
        tipoDocumentoId = tipoDocumentoId,
        numeroIdentidad = numeroIdentidad.trim(),
        direccion = direccion.trim(),
        telefono = telefono.trim(),
        estado = estado
    )

    companion object {
        // Llena el formulario con un proveedor existente (para editar).
        fun desde(p: Proveedor) = ProveedorFormulario(
            nombreProveedor = p.nombreProveedor.orEmpty(),
            tipoDocumentoId = p.tipoDocumentoId,
            numeroIdentidad = p.numeroIdentidad.orEmpty(),
            direccion = p.direccion.orEmpty(),
            telefono = p.telefono.orEmpty(),
            estado = p.estado?.lowercase() ?: "activo"
        )
    }
}

object ProveedorValidacion {

    // Revisa el formulario con las mismas reglas de la API.
    // Devuelve un mapa campo → mensaje; vacío si todo está bien.
    fun validar(f: ProveedorFormulario): Map<String, String> {
        val errores = mutableMapOf<String, String>()

        texto(f.nombreProveedor, 100, "nombreProveedor", "El nombre del proveedor es obligatorio", errores)
        if (f.tipoDocumentoId == null) errores["tipoDocumento"] = "Selecciona el tipo de documento"
        texto(f.numeroIdentidad, 30, "numeroIdentidad", "El número de identidad es obligatorio", errores)
        texto(f.direccion, 150, "direccion", "La dirección es obligatoria", errores)

        val telefono = f.telefono.trim()
        when {
            telefono.isEmpty() -> errores["telefono"] = "El teléfono es obligatorio"
            telefono.length > 20 -> errores["telefono"] = "Máximo 20 caracteres"
            !telefono.all { it.isDigit() || it == ' ' || it == '+' || it == '-' } ->
                errores["telefono"] = "El teléfono solo puede tener números, espacios, + o -"
        }

        if (f.estado != "activo" && f.estado != "inactivo") errores["estado"] = "Estado no válido"

        return errores
    }

    private fun texto(
        valor: String, maximo: Int, campo: String, mensaje: String,
        errores: MutableMap<String, String>
    ) {
        val t = valor.trim()
        when {
            t.isEmpty() -> errores[campo] = mensaje
            t.length > maximo -> errores[campo] = "Máximo $maximo caracteres"
        }
    }

    fun esActivo(proveedor: Proveedor): Boolean =
        proveedor.estado.equals("activo", ignoreCase = true)

    // Búsqueda por nombre, número de identidad o teléfono, y filtro por estado.
    fun filtrar(proveedores: List<Proveedor>, busqueda: String, estado: EstadoFiltro): List<Proveedor> {
        val texto = busqueda.trim().lowercase()

        return proveedores.filter { p ->
            val coincideTexto = texto.isEmpty() || listOf(p.nombreProveedor, p.numeroIdentidad, p.telefono)
                .any { it?.lowercase()?.contains(texto) == true }

            val coincideEstado = when (estado) {
                EstadoFiltro.TODOS -> true
                EstadoFiltro.ACTIVOS -> esActivo(p)
                EstadoFiltro.INACTIVOS -> !esActivo(p)
            }

            coincideTexto && coincideEstado
        }
    }
}
