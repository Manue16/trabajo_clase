package com.example.mjbebikes.data.model

// Tabla productos. Los nombres son iguales a los campos del JSON de la API.
// Se usa para leer (GET) y para enviar al crear o editar (POST / PUT):
// al crear, el id va en null y Gson no lo envía.
data class Producto(
    val id: Int? = null,
    val img_producto: String? = null,
    val nombre_producto: String? = null,
    val descripcion: String? = null,
    val color_producto: String? = null,
    val marca_producto: String? = null,
    val cant_producto: Int? = null,
    val modelo: String? = null,
    val id_talla: Int? = null,
    val id_proveedor: Int? = null,
    val id_local: Int? = null,
    val valor_unitario: Double? = null,
    val estado: String? = null          // "activo" o "inactivo"
)

// Respuesta de POST / PUT /productos: { "mensaje": "...", "producto": {...} }
data class ProductoRespuesta(
    val mensaje: String? = null,
    val producto: Producto? = null
)
