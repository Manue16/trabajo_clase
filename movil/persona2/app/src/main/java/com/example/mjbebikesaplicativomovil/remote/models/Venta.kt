package com.example.mjbebikesaplicativomovil.remote.models

import com.google.gson.annotations.SerializedName

// Venta tal como la devuelve la API (GET /ventas, GET /ventas/{id} y POST /ventas).
// La usan Mis compras, el historial de ventas y el detalle de venta.
data class Venta(
    val id: Int? = null,

    @SerializedName("id_vendedor")
    val idVendedor: Int? = null,

    @SerializedName("id_cliente")
    val idCliente: Int? = null,

    // "2026-11-24" o con hora, según la BD
    val fecha: String? = null,

    val total: Double? = null,

    // "completada" o "pendiente" (la API lo calcula a partir de "pago")
    val estado: String? = null,

    // true = Pagada, false = Pendiente
    val pago: Boolean? = null,

    val cliente: Cliente? = null,

    val vendedor: UsuarioResumen? = null,

    val productos: List<ProductoVenta>? = null
)

// Un producto dentro de una venta.
data class ProductoVenta(
    @SerializedName("id_producto")
    val idProducto: Int? = null,

    @SerializedName("nombre_producto")
    val nombreProducto: String? = null,

    val cantidad: Int? = null,

    // Precio guardado en el momento de la venta
    @SerializedName("valor_unitario")
    val valorUnitario: Double? = null,

    val subtotal: Double? = null
)

// Lo que se envía en POST /ventas.
data class VentaNueva(
    // Usuario que hace la operación (en la compra del cliente, el mismo cliente)
    @SerializedName("user_id")
    val userId: Int,

    // id de la tabla clientes (no es el id del usuario)
    val cliente: Int,

    val productos: List<ItemVentaNueva>,

    val total: Double,

    val direccion: String,

    // "efectivo", "transferencia", "tarjeta" o "contraentrega"
    @SerializedName("metodo_pago")
    val metodoPago: String,

    // "completada" (pagada) o "pendiente"
    val estado: String,

    // "2026-11-24"
    val fecha: String
)

data class ItemVentaNueva(
    val id: Int,
    val cantidad: Int,

    @SerializedName("valor_unitario")
    val valorUnitario: Double
)

// Respuesta de POST /ventas: { "mensaje": "...", "venta": {...} }
data class VentaRespuesta(
    val mensaje: String? = null,
    val venta: Venta? = null
)
