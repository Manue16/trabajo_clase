package com.example.mjbebikesaplicativomovil.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mjbebikesaplicativomovil.remote.RetrofitClient

// Fábrica para la lista de clientes y para el formulario (crear / editar).
class ClientesViewModelFactory(
    private val context: Context,
    private val idCliente: Int? = null
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        val api = RetrofitClient.createInventario(context)

        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(ClientesViewModel::class.java) ->
                ClientesViewModel(api) as T

            modelClass.isAssignableFrom(ClienteFormViewModel::class.java) ->
                ClienteFormViewModel(api, idCliente) as T

            else -> throw IllegalArgumentException("ViewModel desconocido")
        }
    }
}
