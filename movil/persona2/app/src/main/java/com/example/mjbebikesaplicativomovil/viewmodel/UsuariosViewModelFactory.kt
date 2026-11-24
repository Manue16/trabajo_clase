package com.example.mjbebikesaplicativomovil.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mjbebikesaplicativomovil.data.local.TokenManager
import com.example.mjbebikesaplicativomovil.remote.RetrofitClient

// Fábrica para la lista de usuarios y para el formulario (crear / editar).
class UsuariosViewModelFactory(
    private val context: Context,
    private val idUsuario: Int? = null
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        val api = RetrofitClient.createUsuarios(context)

        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(UsuariosViewModel::class.java) ->
                UsuariosViewModel(api, TokenManager(context.applicationContext)) as T

            modelClass.isAssignableFrom(UsuarioFormViewModel::class.java) ->
                UsuarioFormViewModel(api, idUsuario) as T

            else -> throw IllegalArgumentException("ViewModel desconocido")
        }
    }
}
