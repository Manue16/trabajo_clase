package com.example.mjbebikesaplicativomovil.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Response

// Convierte las respuestas de error de Laravel en un mensaje para mostrar en pantalla.
object ApiErrores {

    const val SESION_VENCIDA = "Tu sesión no es válida o expiró. Inicia sesión de nuevo."

    // Lee el código y el cuerpo de una respuesta que no fue exitosa.
    fun mensaje(response: Response<*>, porDefecto: String): String {
        return mensaje(response.code(), response.errorBody()?.string(), porDefecto)
    }

    // Laravel responde, por ejemplo:
    //   401 → { "message": "Unauthenticated." }
    //   422 → { "mensaje": "Error de validación", "errores": { "campo": ["El campo ... es obligatorio."] } }
    //   404 / 500 → { "mensaje": "Cliente no encontrado" }
    fun mensaje(codigo: Int, cuerpo: String?, porDefecto: String): String {
        if (codigo == 401) return SESION_VENCIDA

        try {
            // Gson().fromJson funciona con la versión de Gson que trae Retrofit 2.9.0.
            val json = Gson().fromJson(cuerpo.orEmpty(), JsonObject::class.java) ?: return "$porDefecto ($codigo)"

            // Si hay errores de validación, mostramos el primero.
            val errores = json.getAsJsonObject("errores") ?: json.getAsJsonObject("errors")
            val primerError = errores?.entrySet()?.firstOrNull()?.value
            if (primerError != null && primerError.isJsonArray && primerError.asJsonArray.size() > 0) {
                return primerError.asJsonArray[0].asString
            }

            val mensaje = json.get("mensaje") ?: json.get("message")
            if (mensaje != null && mensaje.isJsonPrimitive) {
                return mensaje.asString
            }
        } catch (e: Exception) {
            // El cuerpo no era JSON: usamos el mensaje por defecto.
        }

        return "$porDefecto ($codigo)"
    }
}
