package com.example.mjbebikesaplicativomovil.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mjbebikesaplicativomovil.remote.RetrofitClient

// Fábrica para la lista de proveedores y para el formulario (crear / editar).
class ProveedoresViewModelFactory(
    private val context: Context,
    private val idProveedor: Int? = null
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        val api = RetrofitClient.createInventario(context)

        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(ProveedoresViewModel::class.java) ->
                ProveedoresViewModel(api) as T

            modelClass.isAssignableFrom(ProveedorFormViewModel::class.java) ->
                ProveedorFormViewModel(api, idProveedor) as T

            else -> throw IllegalArgumentException("ViewModel desconocido")
        }
    }
}
