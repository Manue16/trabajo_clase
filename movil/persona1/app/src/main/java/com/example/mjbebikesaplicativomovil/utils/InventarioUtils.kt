package com.example.mjbebikesaplicativomovil.utils

import com.example.mjbebikesaplicativomovil.remote.models.Producto

// Filtro por estado en la lista del inventario.
enum class EstadoFiltro { TODOS, ACTIVOS, INACTIVOS }

// Orden por cantidad en inventario.
enum class OrdenCantidad { NINGUNO, MENOR, MAYOR }

// Nivel de stock para mostrar alertas (igual que el panel de bodega de la web).
enum class NivelStock { SIN_STOCK, BAJO, NORMAL }

// Datos que se escriben en el formulario de producto.
// Los números se guardan como texto mientras el usuario escribe.
data class ProductoFormulario(
    val imgProducto: String = "",
    val nombreProducto: String = "",
    val descripcion: String = "",
    val colorProducto: String = "",
    val marcaProducto: String = "",
    val modelo: String = "",
    val cantidad: String = "",
    val valorUnitario: String = "",
    val idTalla: Int? = null,
    val idProveedor: Int? = null,
    val idLocal: Int? = null,
    val estado: String = "activo"
) {
    // Convierte el formulario en el producto que se envía a la API.
    fun aProducto(): Producto = Producto(
        imgProducto = imgProducto.trim(),
        nombreProducto = nombreProducto.trim(),
        descripcion = descripcion.trim(),
        colorProducto = colorProducto.trim(),
        marcaProducto = marcaProducto.trim(),
        modelo = modelo.trim(),
        cantProducto = cantidad.trim().toIntOrNull(),
        valorUnitario = InventarioUtils.aNumero(valorUnitario),
        idTalla = idTalla,
        idProveedor = idProveedor,
        idLocal = idLocal,
        estado = estado
    )

    companion object {
        // Llena el formulario con un producto existente (para editar).
        fun desde(p: Producto) = ProductoFormulario(
            imgProducto = p.imgProducto.orEmpty(),
            nombreProducto = p.nombreProducto.orEmpty(),
            descripcion = p.descripcion.orEmpty(),
            colorProducto = p.colorProducto.orEmpty(),
            marcaProducto = p.marcaProducto.orEmpty(),
            modelo = p.modelo.orEmpty(),
            cantidad = p.cantProducto?.toString().orEmpty(),
            valorUnitario = p.valorUnitario?.let { InventarioUtils.sinDecimalesSobrantes(it) }.orEmpty(),
            idTalla = p.idTalla,
            idProveedor = p.idProveedor,
            idLocal = p.idLocal,
            estado = p.estado?.lowercase() ?: "activo"
        )
    }
}

object InventarioUtils {

    // Revisa el formulario con las mismas reglas de la API.
    // Devuelve un mapa campo → mensaje; vacío si todo está bien.
    fun validar(f: ProductoFormulario): Map<String, String> {
        val errores = mutableMapOf<String, String>()

        texto(f.imgProducto, 255, "imgProducto", "La URL de la imagen es obligatoria", errores)
        texto(f.nombreProducto, 255, "nombreProducto", "El nombre es obligatorio", errores)
        texto(f.descripcion, null, "descripcion", "La descripción es obligatoria", errores)
        texto(f.colorProducto, 100, "colorProducto", "El color es obligatorio", errores)
        texto(f.marcaProducto, 100, "marcaProducto", "La marca es obligatoria", errores)
        texto(f.modelo, 100, "modelo", "El modelo es obligatorio", errores)

        val cantidad = f.cantidad.trim()
        when {
            cantidad.isEmpty() -> errores["cantidad"] = "La cantidad es obligatoria"
            cantidad.toIntOrNull() == null -> errores["cantidad"] = "La cantidad debe ser un número entero"
            cantidad.toInt() < 0 -> errores["cantidad"] = "La cantidad no puede ser negativa"
        }

        val valor = f.valorUnitario.trim()
        when {
            valor.isEmpty() -> errores["valorUnitario"] = "El valor unitario es obligatorio"
            aNumero(valor) == null -> errores["valorUnitario"] = "El valor unitario debe ser un número"
            aNumero(valor)!! < 0 -> errores["valorUnitario"] = "El valor unitario no puede ser negativo"
        }

        if (f.idTalla == null) errores["talla"] = "Selecciona la talla"
        if (f.idProveedor == null) errores["proveedor"] = "Selecciona el proveedor"
        if (f.idLocal == null) errores["local"] = "Selecciona el local"
        if (f.estado != "activo" && f.estado != "inactivo") errores["estado"] = "Estado no válido"

        return errores
    }

    private fun texto(
        valor: String, maximo: Int?, campo: String, mensaje: String,
        errores: MutableMap<String, String>
    ) {
        val t = valor.trim()
        when {
            t.isEmpty() -> errores[campo] = mensaje
            maximo != null && t.length > maximo -> errores[campo] = "Máximo $maximo caracteres"
        }
    }

    // Acepta "150000", "150000.50" o "150000,50".
    fun aNumero(texto: String): Double? = texto.trim().replace(',', '.').toDoubleOrNull()

    // 150000.0 → "150000"; 99.5 → "99.5"
    fun sinDecimalesSobrantes(valor: Double): String =
        if (valor % 1.0 == 0.0) valor.toLong().toString() else valor.toString()

    fun nivelStock(cantidad: Int?): NivelStock {
        val c = cantidad ?: 0
        return when {
            c <= 0 -> NivelStock.SIN_STOCK
            c <= 5 -> NivelStock.BAJO
            else -> NivelStock.NORMAL
        }
    }

    fun esActivo(producto: Producto): Boolean =
        producto.estado.equals("activo", ignoreCase = true)

    // Búsqueda por nombre, modelo, marca o color; filtro por estado y por stock bajo;
    // y orden por cantidad.
    fun filtrar(
        productos: List<Producto>,
        busqueda: String,
        estado: EstadoFiltro,
        soloStockBajo: Boolean,
        orden: OrdenCantidad
    ): List<Producto> {
        val texto = busqueda.trim().lowercase()

        val filtrados = productos.filter { p ->
            val coincideTexto = texto.isEmpty() || listOf(
                p.nombreProducto, p.modelo, p.marcaProducto, p.colorProducto
            ).any { it?.lowercase()?.contains(texto) == true }

            val coincideEstado = when (estado) {
                EstadoFiltro.TODOS -> true
                EstadoFiltro.ACTIVOS -> esActivo(p)
                EstadoFiltro.INACTIVOS -> !esActivo(p)
            }

            val coincideStock = !soloStockBajo || nivelStock(p.cantProducto) != NivelStock.NORMAL

            coincideTexto && coincideEstado && coincideStock
        }

        return when (orden) {
            OrdenCantidad.MENOR -> filtrados.sortedBy { it.cantProducto ?: 0 }
            OrdenCantidad.MAYOR -> filtrados.sortedByDescending { it.cantProducto ?: 0 }
            OrdenCantidad.NINGUNO -> filtrados
        }
    }
}
