package com.example.mjbebikesaplicativomovil.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mjbebikesaplicativomovil.data.local.TokenManager
import com.example.mjbebikesaplicativomovil.remote.RetrofitClient

// Fábrica de los ViewModels de la persona 2 que usan ventas (finalizar compra y Mis compras).
class VentasViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        val api = RetrofitClient.createVentas(context)
        val tokenManager = TokenManager(context.applicationContext)

        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(CheckoutViewModel::class.java) ->
                CheckoutViewModel(api, tokenManager) as T

            modelClass.isAssignableFrom(MisComprasViewModel::class.java) ->
                MisComprasViewModel(api, tokenManager) as T

            else -> throw IllegalArgumentException("ViewModel desconocido")
        }
    }
}
