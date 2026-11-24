package com.example.mjbebikesaplicativomovil.utils

import com.example.mjbebikesaplicativomovil.data.local.SesionUsuario
import com.example.mjbebikesaplicativomovil.remote.models.ItemVentaNueva
import com.example.mjbebikesaplicativomovil.remote.models.Producto
import com.example.mjbebikesaplicativomovil.remote.models.Venta
import com.example.mjbebikesaplicativomovil.remote.models.VentaNueva

// Una fila de productos en el formulario de nueva venta.
data class LineaVenta(
    val idProducto: Int? = null,
    val cantidad: String = "1"
)

// Datos del formulario de nueva venta (venta en tienda del vendedor).
data class NuevaVentaFormulario(
    val idCliente: Int? = null,
    val fecha: String = CompraUtils.hoy(),
    val lineas: List<LineaVenta> = listOf(LineaVenta())
)

object VentaUtils {

    // Subtotal de una fila con el precio actual del producto.
    fun subtotal(linea: LineaVenta, productos: List<Producto>): Double {
        val producto = productos.find { it.id == linea.idProducto } ?: return 0.0
        val cantidad = linea.cantidad.trim().toIntOrNull() ?: 0
        return cantidad * (producto.valorUnitario ?: 0.0)
    }

    fun total(f: NuevaVentaFormulario, productos: List<Producto>): Double =
        f.lineas.sumOf { subtotal(it, productos) }

    // Productos que se pueden vender: activos y con stock.
    fun vendibles(productos: List<Producto>): List<Producto> =
        productos.filter { it.estado.equals("activo", ignoreCase = true) && (it.cantProducto ?: 0) > 0 }

    // Revisa el formulario. Las claves de las filas llevan su posición: "producto_0", "cantidad_1"...
    fun validar(f: NuevaVentaFormulario, productos: List<Producto>): Map<String, String> {
        val errores = mutableMapOf<String, String>()

        if (f.idCliente == null) errores["cliente"] = "Selecciona el cliente"
        if (!CompraUtils.esFechaValida(f.fecha)) errores["fecha"] = "Escribe la fecha como AAAA-MM-DD"
        if (f.lineas.isEmpty()) errores["lineas"] = "Agrega al menos un producto"

        val repetidos = f.lineas.mapNotNull { it.idProducto }
            .groupingBy { it }.eachCount().filter { it.value > 1 }.keys

        f.lineas.forEachIndexed { i, linea ->
            val producto = productos.find { it.id == linea.idProducto }

            when {
                linea.idProducto == null || producto == null -> errores["producto_$i"] = "Selecciona el producto"
                linea.idProducto in repetidos -> errores["producto_$i"] = "Este producto ya está en otra fila"
            }

            val cantidad = linea.cantidad.trim().toIntOrNull()
            val stock = producto?.cantProducto ?: 0
            when {
                cantidad == null || cantidad < 1 -> errores["cantidad_$i"] = "Mínimo 1"
                producto != null && cantidad > stock -> errores["cantidad_$i"] = "Solo hay $stock unidades"
            }
        }

        return errores
    }

    // Cuerpo de POST /ventas para una venta en tienda: pago en efectivo y ya pagada.
    fun armarVenta(vendedorId: Int, f: NuevaVentaFormulario, productos: List<Producto>): VentaNueva {
        val items = f.lineas.mapNotNull { linea ->
            val producto = productos.find { it.id == linea.idProducto } ?: return@mapNotNull null
            ItemVentaNueva(
                id = producto.id ?: return@mapNotNull null,
                cantidad = linea.cantidad.trim().toInt(),
                valorUnitario = producto.valorUnitario ?: 0.0
            )
        }

        return VentaNueva(
            userId = vendedorId,
            cliente = f.idCliente ?: 0,
            productos = items,
            total = items.sumOf { it.cantidad * it.valorUnitario },
            direccion = "Venta en tienda",
            metodoPago = "efectivo",
            estado = "completada",
            fecha = f.fecha.trim()
        )
    }

    // El administrador ve todas las ventas; el vendedor, solo las suyas.
    fun ventasDe(ventas: List<Venta>, sesion: SesionUsuario): List<Venta> =
        if (sesion.rolId == Roles.ADMIN) ventas else ventas.filter { it.idVendedor == sesion.id }

    fun nombreCliente(venta: Venta): String {
        val nombre = listOfNotNull(venta.cliente?.nombres, venta.cliente?.apellidos)
            .joinToString(" ").trim()
        return nombre.ifEmpty { "Cliente #${venta.idCliente ?: ""}" }
    }

    fun nombreVendedor(venta: Venta): String =
        venta.vendedor?.login ?: "Vendedor #${venta.idVendedor ?: ""}"

    // Búsqueda por número de venta, cliente o vendedor.
    fun filtrar(ventas: List<Venta>, busqueda: String): List<Venta> {
        val texto = busqueda.trim().lowercase().removePrefix("#")
        if (texto.isEmpty()) return ventas
        return ventas.filter { v ->
            v.id?.toString() == texto ||
                    nombreCliente(v).lowercase().contains(texto) ||
                    nombreVendedor(v).lowercase().contains(texto)
        }
    }
}
