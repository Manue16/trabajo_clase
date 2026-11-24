package com.example.mjbebikesaplicativomovil.remote

import com.example.mjbebikesaplicativomovil.remote.models.Cliente
import com.example.mjbebikesaplicativomovil.remote.models.ClienteRespuesta
import com.example.mjbebikesaplicativomovil.remote.models.Compra
import com.example.mjbebikesaplicativomovil.remote.models.CompraNueva
import com.example.mjbebikesaplicativomovil.remote.models.DetalleCompra
import com.example.mjbebikesaplicativomovil.remote.models.DetalleCompraNuevo
import com.example.mjbebikesaplicativomovil.remote.models.Local
import com.example.mjbebikesaplicativomovil.remote.models.Mensaje
import com.example.mjbebikesaplicativomovil.remote.models.Producto
import com.example.mjbebikesaplicativomovil.remote.models.ProductoRespuesta
import com.example.mjbebikesaplicativomovil.remote.models.Proveedor
import com.example.mjbebikesaplicativomovil.remote.models.ProveedorRespuesta
import com.example.mjbebikesaplicativomovil.remote.models.Talla
import com.example.mjbebikesaplicativomovil.remote.models.TipoDocumento
import com.example.mjbebikesaplicativomovil.remote.models.UsuarioResumen
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Endpoints de la API que usa la persona 1.
// Todas devuelven Response<...> para poder revisar el código (200, 201, 401, 422...).
// Solo GET /productos y GET /productos/{id} son públicas; el resto exige haber iniciado sesión.
interface InventarioApi {

    // ---------- Productos (catálogo e inventario) ----------
    @GET("productos")
    suspend fun productos(): Response<List<Producto>>

    @GET("productos/{id}")
    suspend fun producto(@Path("id") id: Int): Response<Producto>

    @POST("productos")
    suspend fun crearProducto(@Body producto: Producto): Response<ProductoRespuesta>

    @PUT("productos/{id}")
    suspend fun actualizarProducto(@Path("id") id: Int, @Body producto: Producto): Response<ProductoRespuesta>

    @DELETE("productos/{id}")
    suspend fun eliminarProducto(@Path("id") id: Int): Response<Mensaje>

    // ---------- Listas para los formularios de producto ----------
    @GET("tallas")
    suspend fun tallas(): Response<List<Talla>>

    @GET("locales")
    suspend fun locales(): Response<List<Local>>

    // ---------- Clientes ----------
    @GET("clientes")
    suspend fun clientes(): Response<List<Cliente>>

    @GET("clientes/{id}")
    suspend fun cliente(@Path("id") id: Int): Response<Cliente>

    @POST("clientes")
    suspend fun crearCliente(@Body cliente: Cliente): Response<ClienteRespuesta>

    @PUT("clientes/{id}")
    suspend fun actualizarCliente(@Path("id") id: Int, @Body cliente: Cliente): Response<ClienteRespuesta>

    @DELETE("clientes/{id}")
    suspend fun eliminarCliente(@Path("id") id: Int): Response<Mensaje>

    // Listas para el formulario de cliente
    @GET("tipos_documentos")
    suspend fun tiposDocumentos(): Response<List<TipoDocumento>>

    @GET("usuarios")
    suspend fun usuarios(): Response<List<UsuarioResumen>>

    // ---------- Proveedores ----------
    @GET("proveedores")
    suspend fun proveedores(): Response<List<Proveedor>>

    @GET("proveedores/{id}")
    suspend fun proveedor(@Path("id") id: Int): Response<Proveedor>

    @POST("proveedores")
    suspend fun crearProveedor(@Body proveedor: Proveedor): Response<ProveedorRespuesta>

    @PUT("proveedores/{id}")
    suspend fun actualizarProveedor(@Path("id") id: Int, @Body proveedor: Proveedor): Response<ProveedorRespuesta>

    @DELETE("proveedores/{id}")
    suspend fun eliminarProveedor(@Path("id") id: Int): Response<Mensaje>

    // ---------- Compras ----------
    @GET("compras")
    suspend fun compras(): Response<List<Compra>>

    @GET("compras/{id}")
    suspend fun compra(@Path("id") id: Int): Response<Compra>

    // La API responde la compra creada directamente (sin "mensaje")
    @POST("compras")
    suspend fun crearCompra(@Body compra: CompraNueva): Response<Compra>

    @GET("detalles_compra")
    suspend fun detallesCompra(@Query("id_compra") idCompra: Int): Response<List<DetalleCompra>>

    @POST("detalles_compra")
    suspend fun crearDetalleCompra(@Body detalle: DetalleCompraNuevo): Response<DetalleCompra>
}
