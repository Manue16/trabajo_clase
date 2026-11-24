package com.example.mjbebikesaplicativomovil.ui.registro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mjbebikesaplicativomovil.viewmodel.RegistroViewModel
import com.example.mjbebikesaplicativomovil.viewmodel.RegistroViewModelFactory
import kotlinx.coroutines.delay

// Registro de cliente: la cuenta se crea siempre con rol Cliente (lo decide la API).
@Composable
fun RegistroScreen(
    // Después de registrarse vuelve al login (como en la web).
    onRegistrado: () -> Unit = {},
    onIrALogin: () -> Unit = {}
) {

    val context = LocalContext.current

    val registroViewModel: RegistroViewModel = viewModel(
        factory = RegistroViewModelFactory(context)
    )

    val uiState by registroViewModel.uiState.collectAsState()
    val f = uiState.formulario
    val errores = uiState.erroresCampos

    // Igual que la web: muestra "Registro exitoso" y a los 1,5 s pasa al login.
    LaunchedEffect(uiState.emailRegistrado) {
        if (uiState.emailRegistrado != null) {
            delay(1500)
            onRegistrado()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(text = "🚲 MJBE Bikes", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "Registro de cliente",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Rol asignado: Cliente",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Registro hecho: solo se muestra el mensaje mientras pasa al login.
            if (uiState.emailRegistrado != null) {
                Text(
                    text = "Registro exitoso. Ya puedes iniciar sesión con ${uiState.emailRegistrado}.",
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
                return@Column
            }

            Campo(f.login, { registroViewModel.actualizar(f.copy(login = it)) },
                "Usuario", errores["login"])

            Campo(f.email, { registroViewModel.actualizar(f.copy(email = it)) },
                "Correo electrónico", errores["email"], KeyboardType.Email)

            Campo(f.password, { registroViewModel.actualizar(f.copy(password = it)) },
                "Contraseña", errores["password"], KeyboardType.Password, esClave = true)

            Campo(f.confirmar, { registroViewModel.actualizar(f.copy(confirmar = it)) },
                "Confirmar contraseña", errores["confirmar"], KeyboardType.Password, esClave = true)

            // Error de la API (por ejemplo, usuario o correo ya registrados).
            uiState.errorRegistro?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { registroViewModel.registrar() },
                enabled = !uiState.enviando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.enviando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Registrarme")
                }
            }

            TextButton(onClick = onIrALogin) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }
        }
    }
}

// Campo de texto con su mensaje de error debajo.
@Composable
private fun Campo(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    error: String?,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    esClave: Boolean = false
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        isError = error != null,
        supportingText = if (error != null) {
            { Text(error) }
        } else null,
        visualTransformation = if (esClave) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
        modifier = Modifier.fillMaxWidth()
    )
}
