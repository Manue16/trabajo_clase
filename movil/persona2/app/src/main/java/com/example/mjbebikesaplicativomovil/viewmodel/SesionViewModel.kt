package com.example.mjbebikesaplicativomovil.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mjbebikesaplicativomovil.data.local.SesionUsuario
import com.example.mjbebikesaplicativomovil.data.local.TokenManager
import com.example.mjbebikesaplicativomovil.data.repository.AuthRepository
import com.example.mjbebikesaplicativomovil.data.repository.CarritoRepository
import com.example.mjbebikesaplicativomovil.remote.RetrofitClient
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Situaciones de la sesión en toda la app.
sealed class SesionUiState {
    object Cargando : SesionUiState()                       // leyendo lo guardado en el teléfono
    object SinSesion : SesionUiState()
    data class ConSesion(val usuario: SesionUsuario) : SesionUiState()
}

// Lo comparten la navegación y el menú: sabe quién inició sesión y con qué rol.
class SesionViewModel(
    tokenManager: TokenManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    // Se actualiza solo cuando se inicia o se cierra sesión.
    val estado: StateFlow<SesionUiState> = tokenManager.sesion
        .map { usuario ->
            if (usuario == null) SesionUiState.SinSesion else SesionUiState.ConSesion(usuario)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SesionUiState.Cargando)

    // Cierra la sesión en la API y en el teléfono, vacía el carrito y avisa al terminar.
    fun cerrarSesion(alTerminar: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            CarritoRepository.vaciar()
            alTerminar()
        }
    }
}

class SesionViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(SesionViewModel::class.java)) {

            val tokenManager = TokenManager(context.applicationContext)
            val repository = AuthRepository(RetrofitClient.create(context), tokenManager)

            @Suppress("UNCHECKED_CAST")
            return SesionViewModel(tokenManager, repository) as T
        }

        throw IllegalArgumentException("ViewModel desconocido")
    }
}
