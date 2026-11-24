package com.example.mjbebikesaplicativomovil.ui.proveedores

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mjbebikesaplicativomovil.ui.componentes.Selector
import com.example.mjbebikesaplicativomovil.viewmodel.ProveedorFormViewModel
import com.example.mjbebikesaplicativomovil.viewmodel.ProveedoresViewModelFactory

// Formulario para crear (idProveedor = null) o editar un proveedor.
@Composable
fun ProveedorFormScreen(
    idProveedor: Int?,
    onVolver: () -> Unit = {},
    onGuardado: () -> Unit = {}
) {

    val context = LocalContext.current

    val formViewModel: ProveedorFormViewModel = viewModel(
        key = "proveedor_form_${idProveedor ?: "nuevo"}",
        factory = ProveedoresViewModelFactory(context, idProveedor)
    )

    val uiState by formViewModel.uiState.collectAsState()
    val f = uiState.formulario
    val errores = uiState.erroresCampos

    // Cuando la API confirma que se guardó, volvemos a la lista.
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
                text = if (formViewModel.esEdicion) "Actualizar proveedor" else "Registrar proveedor",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = if (formViewModel.esEdicion) "Modifique la información del proveedor"
                else "Complete la información del proveedor",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    Text(
                        text = uiState.errorCarga.orEmpty(),
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { formViewModel.cargarDatos() }) {
                        Text("Reintentar")
                    }
                }

                else -> {

                    CampoTexto(
                        valor = f.nombreProveedor,
                        onCambio = { formViewModel.actualizar(f.copy(nombreProveedor = it)) },
                        etiqueta = "Nombre del proveedor",
                        error = errores["nombreProveedor"]
                    )

                    Selector(
                        etiqueta = "Tipo de documento",
                        opciones = uiState.tiposDocumento,
                        seleccionada = uiState.tiposDocumento.find { it.id == f.tipoDocumentoId },
                        textoDe = { it.nombreDocumento ?: it.sigla.orEmpty() },
                        onSeleccionar = { formViewModel.actualizar(f.copy(tipoDocumentoId = it.id)) },
                        error = errores["tipoDocumento"]
                    )

                    CampoTexto(
                        valor = f.numeroIdentidad,
                        onCambio = { formViewModel.actualizar(f.copy(numeroIdentidad = it)) },
                        etiqueta = "Número de identidad",
                        error = errores["numeroIdentidad"]
                    )

                    CampoTexto(
                        valor = f.telefono,
                        onCambio = { formViewModel.actualizar(f.copy(telefono = it)) },
                        etiqueta = "Teléfono",
                        error = errores["telefono"],
                        tipoTeclado = KeyboardType.Phone
                    )

                    CampoTexto(
                        valor = f.direccion,
                        onCambio = { formViewModel.actualizar(f.copy(direccion = it)) },
                        etiqueta = "Dirección",
                        error = errores["direccion"]
                    )

                    Selector(
                        etiqueta = "Estado",
                        opciones = listOf("activo", "inactivo"),
                        seleccionada = f.estado,
                        textoDe = { if (it == "activo") "Activo" else "Inactivo" },
                        onSeleccionar = { formViewModel.actualizar(f.copy(estado = it)) },
                        error = errores["estado"]
                    )

                    // Error que devuelve la API (por ejemplo, sesión vencida).
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
                        OutlinedButton(
                            onClick = onVolver,
                            modifier = Modifier.weight(1f)
                        ) {
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
                                Text(if (formViewModel.esEdicion) "Actualizar" else "Guardar proveedor")
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
private fun CampoTexto(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    error: String?,
    tipoTeclado: KeyboardType = KeyboardType.Text
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
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
        modifier = Modifier.fillMaxWidth()
    )
}
