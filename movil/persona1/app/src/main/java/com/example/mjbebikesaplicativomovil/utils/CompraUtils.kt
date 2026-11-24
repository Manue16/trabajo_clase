package com.example.mjbebikesaplicativomovil.utils

import com.example.mjbebikesaplicativomovil.remote.models.Compra
import com.example.mjbebikesaplicativomovil.remote.models.CompraNueva
import com.example.mjbebikesaplicativomovil.remote.models.DetalleCompraNuevo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Una fila de productos dentro del formulario de compra.
data class LineaCompra(
    val idProducto: Int? = null,
    val cantidad: String = "1",
    val precio: String = ""
) {
    // Subtotal de la fila (0 si todavía no hay números válidos).
    val subtotal: Double
        get() = (cantidad.trim().toIntOrNull() ?: 0) * (InventarioUtils.aNumero(precio) ?: 0.0)
}

// Datos del formulario de compra.
data class CompraFormulario(
    val idProveedor: Int? = null,
    val fecha: String = CompraUtils.hoy(),
    val pago: Boolean = false,
    val lineas: List<LineaCompra> = listOf(LineaCompra())
) {
    val total: Double
        get() = lineas.sumOf { it.subtotal }

    // Lo que se envía en POST /api/compras.
    fun aCompraNueva(): CompraNueva = CompraNueva(
        idProveedor = idProveedor ?: 0,
        fecha = fecha.trim(),
        totalCompra = total,
        pago = pago
    )

    // Lo que se envía en POST /api/detalles_compra, una vez por cada fila.
    fun aDetalles(idCompra: Int): List<DetalleCompraNuevo> = lineas.map {
        DetalleCompraNuevo(
            idCompra = idCompra,
            idProducto = it.idProducto ?: 0,
            cantidad = it.cantidad.trim().toInt(),
            precioUnitario = InventarioUtils.aNumero(it.precio) ?: 0.0
        )
    }
}

object CompraUtils {

    // La API usa fechas "yyyy-MM-dd". Se usa SimpleDateFormat porque
    // java.time necesita Android 8 y la app admite desde Android 7 (minSdk 24).
    private fun formatoFecha() = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }

    fun hoy(): String = formatoFecha().format(Date())

    fun esFechaValida(texto: String): Boolean {
        val t = texto.trim()
        if (!Regex("""\d{4}-\d{2}-\d{2}""").matches(t)) return false
        return try {
            formatoFecha().parse(t) != null
        } catch (e: Exception) {
            false
        }
    }

    // Revisa el formulario. Las claves de las filas llevan su posición: "producto_0", "cantidad_1"...
    fun validar(f: CompraFormulario): Map<String, String> {
        val errores = mutableMapOf<String, String>()

        if (f.idProveedor == null) errores["proveedor"] = "Selecciona el proveedor"
        if (!esFechaValida(f.fecha)) errores["fecha"] = "Escribe la fecha como AAAA-MM-DD"

        if (f.lineas.isEmpty()) errores["lineas"] = "Agrega al menos un producto"

        val repetidos = f.lineas.mapNotNull { it.idProducto }
            .groupingBy { it }.eachCount().filter { it.value > 1 }.keys

        f.lineas.forEachIndexed { i, linea ->
            when {
                linea.idProducto == null -> errores["producto_$i"] = "Selecciona el producto"
                linea.idProducto in repetidos -> errores["producto_$i"] = "Este producto ya está en otra fila"
            }

            val cantidad = linea.cantidad.trim().toIntOrNull()
            if (cantidad == null || cantidad < 1) errores["cantidad_$i"] = "Mínimo 1"

            val precio = InventarioUtils.aNumero(linea.precio)
            if (precio == null || precio < 0) errores["precio_$i"] = "Costo no válido"
        }

        return errores
    }

    fun estadoTexto(pago: Boolean?): String = if (pago == true) "Pagada" else "Pendiente"

    // "2026-11-24T00:00:00.000000Z" o "2026-11-24 00:00:00" → "24/11/2026"
    fun fechaCorta(fecha: String?): String {
        val partes = fecha?.take(10)?.split("-") ?: return "Sin fecha"
        return if (partes.size == 3) "${partes[2]}/${partes[1]}/${partes[0]}" else "Sin fecha"
    }

    // Búsqueda por número de compra, proveedor o estado (Pagada / Pendiente).
    fun filtrar(compras: List<Compra>, busqueda: String): List<Compra> {
        val texto = busqueda.trim().lowercase().removePrefix("#")
        if (texto.isEmpty()) return compras
        return compras.filter { c ->
            c.id?.toString() == texto ||
                    c.proveedor?.nombreProveedor?.lowercase()?.contains(texto) == true ||
                    estadoTexto(c.pago).lowercase().contains(texto)
        }
    }
}
