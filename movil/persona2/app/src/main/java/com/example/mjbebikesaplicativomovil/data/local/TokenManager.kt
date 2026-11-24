
package com.example.mjbebikesaplicativomovil.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mjbebikesaplicativomovil.remote.models.UsuarioLogin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Crea el almacenamiento local de preferencias para guardar el token.
private val Context.dataStore by preferencesDataStore(name = "user_prefs")

// Datos del usuario que inició sesión (lo mismo que guarda la web en localStorage).
data class SesionUsuario(
    val id: Int,
    val login: String,
    val email: String,
    val rolId: Int
)

class TokenManager(private val context: Context) {

    companion object {
        // Clave con la que se identifica el token guardado.
        private val TOKEN_KEY = stringPreferencesKey("auth_token")

        // Datos del usuario de la sesión.
        private val USER_ID_KEY = intPreferencesKey("user_id")
        private val USER_LOGIN_KEY = stringPreferencesKey("user_login")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val ROL_ID_KEY = intPreferencesKey("rol_id")
    }

    // Guarda el token recibido desde Laravel.
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    // Guarda el token y los datos del usuario al iniciar sesión.
    suspend fun saveSesion(token: String, usuario: UsuarioLogin) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_ID_KEY] = usuario.id
            preferences[USER_LOGIN_KEY] = usuario.login
            preferences[USER_EMAIL_KEY] = usuario.email
            preferences[ROL_ID_KEY] = usuario.rolid
        }
    }

    // Permite observar el token almacenado.
    val token: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[TOKEN_KEY]
        }

    // Permite observar la sesión: null si no hay token o no se conoce el rol.
    val sesion: Flow<SesionUsuario?> = context.dataStore.data
        .map { preferences ->
            val token = preferences[TOKEN_KEY]
            val rolId = preferences[ROL_ID_KEY]

            if (token.isNullOrBlank() || rolId == null) {
                null
            } else {
                SesionUsuario(
                    id = preferences[USER_ID_KEY] ?: 0,
                    login = preferences[USER_LOGIN_KEY].orEmpty(),
                    email = preferences[USER_EMAIL_KEY].orEmpty(),
                    rolId = rolId
                )
            }
        }

    // Obtiene el token actual una sola vez.
    suspend fun getToken(): String? {
        return token.first()
    }

    // Obtiene la sesión actual una sola vez.
    suspend fun getSesion(): SesionUsuario? {
        return sesion.first()
    }

    // Elimina el token y los datos del usuario cuando cierra sesión.
    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_LOGIN_KEY)
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(ROL_ID_KEY)
        }
    }
}
