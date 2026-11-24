package com.example.mjbebikesaplicativomovil.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mjbebikesaplicativomovil.remote.RetrofitClient

// Fábrica para la lista de compras, el detalle de una compra y el formulario de nueva compra.
class ComprasViewModelFactory(
    private val context: Context,
    private val idCompra: Int = 0
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        val api = RetrofitClient.createInventario(context)

        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(ComprasViewModel::class.java) ->
                ComprasViewModel(api) as T

            modelClass.isAssignableFrom(DetalleCompraViewModel::class.java) ->
                DetalleCompraViewModel(api, idCompra) as T

            modelClass.isAssignableFrom(CompraFormViewModel::class.java) ->
                CompraFormViewModel(api) as T

            else -> throw IllegalArgumentException("ViewModel desconocido")
        }
    }
}
