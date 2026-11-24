package com.example.mjbebikes.data.model

// Tabla compras. GET /compras y GET /compras/{id} incluyen el proveedor.
data class Compra(
    val id: Int? = null,
    val id_proveedor: Int? = null,
    val fecha: String? = null,          // fecha y hora en texto, ej. "2026-11-24T00:00:00.000000Z"
    val total_compra: Double? = null,
    val pago: Boolean? = null,          // true = Pagada, false = Pendiente
    val proveedor: Proveedor? = null
)

// Lo que se envía en POST /compras
data class CompraNueva(
    val id_proveedor: Int,
    val fecha: String?,                 // "2026-11-24" (puede ir en null)
    val total_compra: Double,
    val pago: Boolean
)

// Tabla detalles_compra. GET /detalles_compra?id_compra=X incluye el producto.
data class DetalleCompra(
    val id: Int? = null,
    val id_compra: Int? = null,
    val id_producto: Int? = null,
    val cantidad: Int? = null,
    val precio_unitario_momento: Double? = null,
    val producto: Producto? = null
)

// Lo que se envía en POST /detalles_compra (un registro por cada producto de la compra)
data class DetalleCompraNuevo(
    val id_compra: Int,
    val id_producto: Int,
    val cantidad: Int,
    val precio_unitario_momento: Double
)
