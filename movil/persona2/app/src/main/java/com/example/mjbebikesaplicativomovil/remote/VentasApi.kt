package com.example.mjbebikesaplicativomovil.remote

import com.example.mjbebikesaplicativomovil.remote.models.Cliente
import com.example.mjbebikesaplicativomovil.remote.models.ClienteRespuesta
import com.example.mjbebikesaplicativomovil.remote.models.Dashboard
import com.example.mjbebikesaplicativomovil.remote.models.Producto
import com.example.mjbebikesaplicativomovil.remote.models.TipoDocumento
import com.example.mjbebikesaplicativomovil.remote.models.Venta
import com.example.mjbebikesaplicativomovil.remote.models.VentaNueva
import com.example.mjbebikesaplicativomovil.remote.models.VentaRespuesta
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// Endpoints de la persona 2: ventas, compra del cliente y Mis compras.
// Todas requieren sesión (el token lo agrega AuthInterceptor).
interface VentasApi {

    // ---------- Ventas ----------

    // Sin user_id: todas las ventas. Con user_id: solo las compras de ese usuario (Mis compras).
    @GET("ventas")
    suspend fun ventas(@Query("user_id") userId: Int? = null): Response<List<Venta>>

    @GET("ventas/{id}")
    suspend fun venta(@Path("id") id: Int): Response<Venta>

    // Crea la venta y sus detalles en una sola transacción (la API descuenta el stock).
    @POST("ventas")
    suspend fun crearVenta(@Body venta: VentaNueva): Response<VentaRespuesta>

    // ---------- Cliente del usuario (para finalizar compra) ----------

    // Registro de cliente asociado a un usuario (lista vacía si todavía no tiene).
    @GET("clientes")
    suspend fun clientePorUsuario(@Query("usuario_id") usuarioId: Int): Response<List<Cliente>>

    @POST("clientes")
    suspend fun crearCliente(@Body cliente: Cliente): Response<ClienteRespuesta>

    @GET("tipos_documentos")
    suspend fun tiposDocumentos(): Response<List<TipoDocumento>>

    // ---------- Listas para registrar una venta en tienda ----------

    @GET("clientes")
    suspend fun clientes(): Response<List<Cliente>>

    @GET("productos")
    suspend fun productos(): Response<List<Producto>>

    // ---------- Panel del administrador ----------

    // Resumen general calculado por la API (totales, ventas del mes, stock bajo...).
    @GET("reportes/dashboard")
    suspend fun dashboard(): Response<Dashboard>
}
