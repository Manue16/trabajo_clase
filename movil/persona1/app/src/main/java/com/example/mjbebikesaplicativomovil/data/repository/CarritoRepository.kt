package com.example.mjbebikesaplicativomovil.data.repository

import com.example.mjbebikesaplicativomovil.remote.models.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Un producto dentro del carrito con la cantidad elegida.
data class ItemCarrito(
    val producto: Producto,
    val cantidad: Int
) {
    val subtotal: Double
        get() = (producto.valorUnitario ?: 0.0) * cantidad
}

// Lógica del carrito: se guarda en memoria mientras la app está abierta,
// igual que en la versión web.
open class Carrito {

    private val _items = MutableStateFlow<List<ItemCarrito>>(emptyList())
    val items: StateFlow<List<ItemCarrito>> = _items.asStateFlow()

    // Agrega una unidad. Si el producto ya está, suma 1 a su cantidad.
    // Devuelve false si no se pudo agregar porque no hay más stock.
    fun agregar(producto: Producto): Boolean {
        val id = producto.id ?: return false
        val actuales = _items.value
        val existente = actuales.find { it.producto.id == id }
        val cantidadNueva = (existente?.cantidad ?: 0) + 1

        if (!hayStock(producto, cantidadNueva)) return false

        _items.value = if (existente == null) {
            actuales + ItemCarrito(producto, 1)
        } else {
            actuales.map { if (it.producto.id == id) it.copy(cantidad = cantidadNueva) else it }
        }
        return true
    }

    // Cambia la cantidad de un producto. Con menos de 1 lo quita del carrito.
    // Devuelve false si la cantidad pedida supera el stock.
    fun cambiarCantidad(idProducto: Int, cantidad: Int): Boolean {
        if (cantidad < 1) {
            quitar(idProducto)
            return true
        }

        val item = _items.value.find { it.producto.id == idProducto } ?: return false
        if (!hayStock(item.producto, cantidad)) return false

        _items.value = _items.value.map {
            if (it.producto.id == idProducto) it.copy(cantidad = cantidad) else it
        }
        return true
    }

    // Quita un producto del carrito.
    fun quitar(idProducto: Int) {
        _items.value = _items.value.filter { it.producto.id != idProducto }
    }

    // Vacía el carrito (por ejemplo, después de finalizar la compra).
    fun vaciar() {
        _items.value = emptyList()
    }

    // Número de unidades en total (el número que se ve en el ícono del carrito).
    fun cantidadTotal(): Int = _items.value.sumOf { it.cantidad }

    // Valor total a pagar.
    fun precioTotal(): Double = _items.value.sumOf { it.subtotal }

    // No se puede pedir más de lo que hay en inventario (cant_producto).
    private fun hayStock(producto: Producto, cantidad: Int): Boolean {
        val stock = producto.cantProducto ?: return true
        return cantidad <= stock
    }
}

// Carrito único compartido por todas las pantallas
// (catálogo, detalle, carrito y, después, finalizar compra de la persona 2).
object CarritoRepository : Carrito()
