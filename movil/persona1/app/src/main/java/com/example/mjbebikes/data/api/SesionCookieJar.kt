package com.example.mjbebikes.data.api

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

// La API guarda el JWT en la cookie "mjbe_token" al hacer login.
// Esta clase la guarda en el teléfono y la envía en cada petición,
// igual que hace el navegador en la versión web.
class SesionCookieJar(context: Context) : CookieJar {

    private val prefs = context.getSharedPreferences("mjbe_sesion", Context.MODE_PRIVATE)

    // Se llama cuando la API responde: si trae la cookie, la guardamos
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val cookie = cookies.firstOrNull { it.name == NOMBRE } ?: return

        // Al cerrar sesión la API manda la cookie vencida: la borramos
        if (cookie.value.isEmpty() || cookie.expiresAt < System.currentTimeMillis()) {
            limpiar()
        } else {
            prefs.edit().putString(NOMBRE, cookie.value).apply()
        }
    }

    // Se llama antes de cada petición: adjuntamos la cookie guardada
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val token = prefs.getString(NOMBRE, null) ?: return emptyList()
        val cookie = Cookie.Builder()
            .name(NOMBRE)
            .value(token)
            .hostOnlyDomain(url.host)
            .path("/")
            .build()
        return listOf(cookie)
    }

    // ¿Hay una sesión guardada?
    fun haySesion(): Boolean = prefs.getString(NOMBRE, null) != null

    // Borra la sesión (por ejemplo, al cerrar sesión o si la API responde 401)
    fun limpiar() {
        prefs.edit().remove(NOMBRE).apply()
    }

    companion object {
        private const val NOMBRE = "mjbe_token"
    }
}
