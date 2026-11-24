package com.example.mjbebikesaplicativomovil.remote

import com.example.mjbebikesaplicativomovil.remote.models.Mensaje
import com.example.mjbebikesaplicativomovil.remote.models.Usuario
import com.example.mjbebikesaplicativomovil.remote.models.UsuarioGuardar
import com.example.mjbebikesaplicativomovil.remote.models.UsuarioRespuesta
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Endpoints de la persona 2: administración de usuarios (requieren sesión).
interface UsuariosApi {

    @GET("usuarios")
    suspend fun usuarios(): Response<List<Usuario>>

    @GET("usuarios/{id}")
    suspend fun usuario(@Path("id") id: Int): Response<Usuario>

    @POST("usuarios")
    suspend fun crearUsuario(@Body usuario: UsuarioGuardar): Response<UsuarioRespuesta>

    @PUT("usuarios/{id}")
    suspend fun actualizarUsuario(@Path("id") id: Int, @Body usuario: UsuarioGuardar): Response<UsuarioRespuesta>

    // Responde 409 si el usuario tiene registros relacionados (ventas, cliente...).
    @DELETE("usuarios/{id}")
    suspend fun eliminarUsuario(@Path("id") id: Int): Response<Mensaje>
}
