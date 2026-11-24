package com.example.mjbebikesaplicativomovil.remote.models

import com.google.gson.annotations.SerializedName

// Tabla compras. GET /compras y GET /compras/{id} incluyen el proveedor.
data class Compra(
    val id: Int? = null,

    @SerializedName("id_proveedor")
    val idProveedor: Int? = null,

    // Fecha en texto, ej. "2026-11-24T00:00:00.000000Z"
    val fecha: String? = null,

    @SerializedName("total_compra")
    val totalCompra: Double? = null,

    // true = Pagada, false = Pendiente
    val pago: Boolean? = null,

    val proveedor: Proveedor? = null
)

// Lo que se envía en POST /compras
data class CompraNueva(
    @SerializedName("id_proveedor")
    val idProveedor: Int,

    // "2026-11-24" (puede ir en null)
    val fecha: String?,

    @SerializedName("total_compra")
    val totalCompra: Double,

    val pago: Boolean
)

// Tabla detalles_compra. GET /detalles_compra?id_compra=X incluye el producto.
data class DetalleCompra(
    val id: Int? = null,

    @SerializedName("id_compra")
    val idCompra: Int? = null,

    @SerializedName("id_producto")
    val idProducto: Int? = null,

    val cantidad: Int? = null,

    @SerializedName("precio_unitario_momento")
    val precioUnitario: Double? = null,

    val producto: Producto? = null
)

// Lo que se envía en POST /detalles_compra (un registro por cada producto de la compra)
data class DetalleCompraNuevo(
    @SerializedName("id_compra")
    val idCompra: Int,

    @SerializedName("id_producto")
    val idProducto: Int,

    val cantidad: Int,

    @SerializedName("precio_unitario_momento")
    val precioUnitario: Double
)
