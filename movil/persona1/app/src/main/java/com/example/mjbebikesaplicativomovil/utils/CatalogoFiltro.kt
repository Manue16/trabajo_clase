package com.example.mjbebikesaplicativomovil.utils

import com.example.mjbebikesaplicativomovil.remote.models.Producto
import java.text.NumberFormat
import java.util.Locale

// Orden de los productos por precio en el catálogo.
enum class OrdenPrecio { NINGUNO, MENOR, MAYOR }

object CatalogoFiltro {

    // Solo se muestran los productos activos (sin importar mayúsculas: "activo" o "Activo").
    fun soloActivos(productos: List<Producto>): List<Producto> {
        return productos.filter { it.estado.equals("activo", ignoreCase = true) }
    }

    // Marcas distintas, en orden alfabético, para los filtros.
    fun marcas(productos: List<Producto>): List<String> {
        return productos
            .mapNotNull { it.marcaProducto?.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }

    // Aplica la búsqueda, el filtro por marca y el orden por precio.
    fun filtrar(
        productos: List<Producto>,
        busqueda: String,
        marca: String?,
        orden: OrdenPrecio
    ): List<Producto> {

        val texto = busqueda.trim().lowercase()

        val filtrados = productos.filter { producto ->
            // La búsqueda revisa el nombre, la descripción y la marca.
            val coincideTexto = texto.isEmpty() || listOf(
                producto.nombreProducto,
                producto.descripcion,
                producto.marcaProducto
            ).any { campo -> campo?.lowercase()?.contains(texto) == true }

            val coincideMarca = marca == null ||
                    producto.marcaProducto.equals(marca, ignoreCase = true)

            coincideTexto && coincideMarca
        }

        return when (orden) {
            OrdenPrecio.MENOR -> filtrados.sortedBy { it.valorUnitario ?: 0.0 }
            OrdenPrecio.MAYOR -> filtrados.sortedByDescending { it.valorUnitario ?: 0.0 }
            OrdenPrecio.NINGUNO -> filtrados
        }
    }

    // Formato de precio colombiano: 85000.0 → "$ 85.000"
    fun formatoPrecio(valor: Double?): String {
        val formato = NumberFormat.getNumberInstance(Locale("es", "CO"))
        formato.maximumFractionDigits = 0
        return "$ " + formato.format(valor ?: 0.0)
    }
}
