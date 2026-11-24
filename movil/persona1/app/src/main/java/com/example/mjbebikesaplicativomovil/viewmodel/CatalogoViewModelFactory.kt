package com.example.mjbebikesaplicativomovil.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mjbebikesaplicativomovil.remote.RetrofitClient

class CatalogoViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(CatalogoViewModel::class.java)) {

            // Crea el servicio de la persona 1 (catálogo, inventario, etc.).
            val api = RetrofitClient.createInventario(context)

            @Suppress("UNCHECKED_CAST")
            return CatalogoViewModel(api) as T
        }

        throw IllegalArgumentException("ViewModel desconocido")
    }
}
