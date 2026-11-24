
package com.example.mjbebikesaplicativomovil.remote

import android.content.Context
import com.example.mjbebikesaplicativomovil.data.local.TokenManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Dirección de Laravel desde el emulador Android.
    private const val BASE_URL = "http://10.0.2.2:8000/api/"

    // Configura la conexión con Laravel (la usan todos los servicios).
    private fun retrofit(context: Context): Retrofit {

        // Permite acceder al almacenamiento del token.
        val tokenManager = TokenManager(context.applicationContext)

        // Configura OkHttp para agregar el JWT a las peticiones.
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Servicio de autenticación (login).
    fun create(context: Context): ApiService {
        return retrofit(context).create(ApiService::class.java)
    }

    // Servicio de la persona 1: catálogo, inventario, clientes, proveedores y compras.
    fun createInventario(context: Context): InventarioApi {
        return retrofit(context).create(InventarioApi::class.java)
    }

    // Servicio de la persona 2: ventas, finalizar compra y Mis compras.
    fun createVentas(context: Context): VentasApi {
        return retrofit(context).create(VentasApi::class.java)
    }
}
