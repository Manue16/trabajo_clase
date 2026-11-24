package com.example.mjbebikesaplicativomovil.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mjbebikesaplicativomovil.remote.RetrofitClient

// Fábrica para la lista del inventario y para el formulario de producto (crear / editar).
class InventarioViewModelFactory(
    private val context: Context,
    private val idProducto: Int? = null
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        val api = RetrofitClient.createInventario(context)

        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(InventarioViewModel::class.java) ->
                InventarioViewModel(api) as T

            modelClass.isAssignableFrom(ProductoFormViewModel::class.java) ->
                ProductoFormViewModel(api, idProducto) as T

            else -> throw IllegalArgumentException("ViewModel desconocido")
        }
    }
}
