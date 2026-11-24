package com.example.mjbebikesaplicativomovil.remote.models

import com.google.gson.annotations.SerializedName

// Tabla productos. Sirve para leer (GET) y para enviar al crear o editar (POST / PUT):
// al crear, el id va en null y Gson no lo envía.
data class Producto(
    val id: Int? = null,

    @SerializedName("img_producto")
    val imgProducto: String? = null,

    @SerializedName("nombre_producto")
    val nombreProducto: String? = null,

    val descripcion: String? = null,

    @SerializedName("color_producto")
    val colorProducto: String? = null,

    @SerializedName("marca_producto")
    val marcaProducto: String? = null,

    @SerializedName("cant_producto")
    val cantProducto: Int? = null,

    val modelo: String? = null,

    @SerializedName("id_talla")
    val idTalla: Int? = null,

    @SerializedName("id_proveedor")
    val idProveedor: Int? = null,

    @SerializedName("id_local")
    val idLocal: Int? = null,

    @SerializedName("valor_unitario")
    val valorUnitario: Double? = null,

    // "activo" o "inactivo"
    val estado: String? = null
)

// Respuesta de POST / PUT /productos: { "mensaje": "...", "producto": {...} }
data class ProductoRespuesta(
    val mensaje: String? = null,
    val producto: Producto? = null
)
