package com.example.mjbebikesaplicativomovil.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mjbebikesaplicativomovil.remote.RetrofitClient

class DetalleProductoViewModelFactory(
    private val context: Context,
    private val idProducto: Int
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(DetalleProductoViewModel::class.java)) {

            val api = RetrofitClient.createInventario(context)

            @Suppress("UNCHECKED_CAST")
            return DetalleProductoViewModel(api, idProducto) as T
        }

        throw IllegalArgumentException("ViewModel desconocido")
    }
}
