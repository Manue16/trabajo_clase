package com.example.mjbebikesaplicativomovil.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mjbebikesaplicativomovil.data.local.TokenManager
import com.example.mjbebikesaplicativomovil.remote.RetrofitClient

// Fábrica de los ViewModels de la persona 2 que usan ventas:
// finalizar compra, Mis compras, historial de ventas, detalle de venta, nueva venta y panel.
class VentasViewModelFactory(
    private val context: Context,
    private val idVenta: Int = 0
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

            modelClass.isAssignableFrom(VentasViewModel::class.java) ->
                VentasViewModel(api, tokenManager) as T

            modelClass.isAssignableFrom(DetalleVentaViewModel::class.java) ->
                DetalleVentaViewModel(api, idVenta) as T

            modelClass.isAssignableFrom(NuevaVentaViewModel::class.java) ->
                NuevaVentaViewModel(api, tokenManager) as T

            modelClass.isAssignableFrom(PanelViewModel::class.java) ->
                PanelViewModel(api, tokenManager) as T

            else -> throw IllegalArgumentException("ViewModel desconocido")
        }
    }
}
