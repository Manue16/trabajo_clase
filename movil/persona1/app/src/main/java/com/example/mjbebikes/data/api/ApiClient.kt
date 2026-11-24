package com.example.mjbebikes.data.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Punto único de conexión con la API Laravel.
// Se inicia una sola vez desde MjbeApp (ver MjbeApp.kt).
object ApiClient {

    // Emulador de Android: 10.0.2.2 es "localhost" de tu PC.
    // Celular real: cambia por la IP de tu PC en el wifi (ej. http://192.168.1.10:8000/api/).
    // La API se arranca con: php artisan serve --host=0.0.0.0 --port=8000
    private const val BASE_URL = "http://10.0.2.2:8000/api/"

    lateinit var sesion: SesionCookieJar
        private set

    // Endpoints de la persona 1 (catálogo, inventario, clientes, proveedores y compras)
    lateinit var inventario: InventarioApi
        private set

    lateinit var retrofit: Retrofit
        private set

    fun iniciar(context: Context) {
        if (::retrofit.isInitialized) return

        sesion = SesionCookieJar(context.applicationContext)

        // Muestra en Logcat cada petición y su respuesta (útil para depurar)
        val logs = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val cliente = OkHttpClient.Builder()
            .cookieJar(sesion)
            .addInterceptor { chain ->
                // Pedimos siempre JSON para que Laravel responda errores en JSON
                val peticion = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .build()
                chain.proceed(peticion)
            }
            .addInterceptor(logs)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        inventario = retrofit.create(InventarioApi::class.java)
    }
}
