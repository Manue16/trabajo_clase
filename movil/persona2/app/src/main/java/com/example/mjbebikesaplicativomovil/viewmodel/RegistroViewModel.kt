package com.example.mjbebikesaplicativomovil.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mjbebikesaplicativomovil.data.local.TokenManager
import com.example.mjbebikesaplicativomovil.data.repository.AuthRepository
import com.example.mjbebikesaplicativomovil.remote.RetrofitClient
import com.example.mjbebikesaplicativomovil.utils.ApiErrores
import com.example.mjbebikesaplicativomovil.utils.RegistroFormulario
import com.example.mjbebikesaplicativomovil.utils.RegistroUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Todo lo que muestra la pantalla de registro.
data class RegistroUiState(
    val formulario: RegistroFormulario = RegistroFormulario(),
    val erroresCampos: Map<String, String> = emptyMap(),
    val enviando: Boolean = false,
    val errorRegistro: String? = null,
    // Correo con el que se registró: cuando no es null se muestra "Registro exitoso".
    val emailRegistrado: String? = null
)

class RegistroViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    fun actualizar(nuevo: RegistroFormulario) {
        val estado = _uiState.value
        _uiState.value = estado.copy(
            formulario = nuevo,
            erroresCampos = if (estado.erroresCampos.isEmpty()) emptyMap() else RegistroUtils.validar(nuevo),
            errorRegistro = null
        )
    }

    // POST /api/register. La API rechaza (422) un usuario o correo que ya exista.
    fun registrar() {
        val estado = _uiState.value
        val errores = RegistroUtils.validar(estado.formulario)

        if (errores.isNotEmpty()) {
            _uiState.value = estado.copy(erroresCampos = errores)
            return
        }

        viewModelScope.launch {
            _uiState.value = estado.copy(enviando = true, erroresCampos = emptyMap(), errorRegistro = null)

            try {
                val request = RegistroUtils.aRequest(estado.formulario)
                val response = authRepository.registrar(request)

                _uiState.value = if (response.isSuccessful) {
                    _uiState.value.copy(enviando = false, emailRegistrado = request.email)
                } else {
                    _uiState.value.copy(
                        enviando = false,
                        errorRegistro = ApiErrores.mensaje(response, "No se pudo completar el registro")
                    )
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    enviando = false,
                    errorRegistro = "No se pudo conectar con el servidor. Comprueba tu conexión."
                )
            }
        }
    }
}

class RegistroViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(RegistroViewModel::class.java)) {

            val repository = AuthRepository(
                RetrofitClient.create(context),
                TokenManager(context.applicationContext)
            )

            @Suppress("UNCHECKED_CAST")
            return RegistroViewModel(repository) as T
        }

        throw IllegalArgumentException("ViewModel desconocido")
    }
}
