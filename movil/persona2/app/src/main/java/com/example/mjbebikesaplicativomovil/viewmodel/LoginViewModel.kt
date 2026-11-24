
package com.example.mjbebikesaplicativomovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mjbebikesaplicativomovil.data.repository.AuthRepository
import com.example.mjbebikesaplicativomovil.utils.ApiErrores
import com.example.mjbebikesaplicativomovil.utils.PasswordUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Indica si el inicio de sesión está en proceso.
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Almacena los mensajes de error.
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Indica si el inicio de sesión fue exitoso.
    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()

    // Recibe la contraseña original y calcula su SHA-256.
    fun login(email: String, password: String) {

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _loginSuccess.value = false

            try {
                // Convierte la contraseña al formato que espera Laravel.
                val passwordHash = PasswordUtils.sha256(password)

                // Envía el correo y el hash al backend.
                val response = authRepository.login(email.lowercase(), passwordHash)

                if (response.isSuccessful && response.body()?.token?.isNotBlank() == true) {
                    _loginSuccess.value = true
                } else {
                    _errorMessage.value = when {
                        response.code() == 401 -> "Correo o contraseña incorrectos"
                        response.code() == 403 -> "El usuario no está activo"
                        // La API respondió bien pero sin token: falta 'token' => $token en AuthController.
                        response.isSuccessful -> "La API no devolvió el token de sesión"
                        else -> ApiErrores.mensaje(response, "Error del servidor")
                    }
                }

            } catch (e: Exception) {
                _errorMessage.value =
                    "No se pudo conectar con el servidor. Comprueba tu conexión."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
