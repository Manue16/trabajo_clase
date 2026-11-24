package com.example.mjbebikesaplicativomovil.ui.usuarios

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.OutlinedButton
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
import com.example.mjbebikesaplicativomovil.ui.componentes.Selector
import com.example.mjbebikesaplicativomovil.utils.Roles
import com.example.mjbebikesaplicativomovil.utils.UsuarioUtils
import com.example.mjbebikesaplicativomovil.viewmodel.UsuarioFormViewModel
import com.example.mjbebikesaplicativomovil.viewmodel.UsuariosViewModelFactory

// Formulario para crear (idUsuario = null) o editar un usuario.
@Composable
fun UsuarioFormScreen(
    idUsuario: Int?,
    onVolver: () -> Unit = {},
    onGuardado: () -> Unit = {}
) {

    val context = LocalContext.current

    val formViewModel: UsuarioFormViewModel = viewModel(
        key = "usuario_form_${idUsuario ?: "nuevo"}",
        factory = UsuariosViewModelFactory(context, idUsuario)
    )

    val uiState by formViewModel.uiState.collectAsState()
    val f = uiState.formulario
    val errores = uiState.erroresCampos

    LaunchedEffect(uiState.guardado) {
        if (uiState.guardado) onGuardado()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            TextButton(onClick = onVolver) {
                Text("← Volver")
            }

            Text(
                text = if (formViewModel.esEdicion) "Actualizar empleado" else "Registrar empleado",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                uiState.cargando -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.errorCarga != null -> {
                    Text(text = uiState.errorCarga.orEmpty(), color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { formViewModel.cargarDatos() }) {
                        Text("Reintentar")
                    }
                }

                else -> {

                    Campo(f.login, { formViewModel.actualizar(f.copy(login = it)) },
                        "Usuario", errores["login"])

                    Campo(f.email, { formViewModel.actualizar(f.copy(email = it)) },
                        "Correo electrónico", errores["email"], KeyboardType.Email)

                    Campo(
                        valor = f.password,
                        onCambio = { formViewModel.actualizar(f.copy(password = it)) },
                        etiqueta = if (formViewModel.esEdicion) "Nueva contraseña (opcional)" else "Contraseña",
                        error = errores["password"],
                        tipoTeclado = KeyboardType.Password,
                        esClave = true
                    )

                    if (formViewModel.esEdicion) {
                        Text(
                            text = "Déjala vacía para conservar la contraseña actual.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Selector(
                        etiqueta = "Rol",
                        opciones = UsuarioUtils.rolesDisponibles,
                        seleccionada = f.rolId,
                        textoDe = { Roles.nombre(it) },
                        onSeleccionar = { formViewModel.actualizar(f.copy(rolId = it)) },
                        error = errores["rol"]
                    )

                    Selector(
                        etiqueta = "Estado",
                        opciones = listOf("Activo", "Inactivo"),
                        seleccionada = f.estado,
                        textoDe = { it },
                        onSeleccionar = { formViewModel.actualizar(f.copy(estado = it)) },
                        error = errores["estado"]
                    )

                    uiState.errorGuardar?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(onClick = onVolver, modifier = Modifier.weight(1f)) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = { formViewModel.guardar() },
                            enabled = !uiState.guardando,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (uiState.guardando) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            } else {
                                Text(if (formViewModel.esEdicion) "Actualizar" else "Guardar empleado")
                            }
                        }
                    }
                }
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
