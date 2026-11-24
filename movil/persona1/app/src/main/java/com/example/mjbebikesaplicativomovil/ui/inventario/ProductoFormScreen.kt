package com.example.mjbebikesaplicativomovil.ui.inventario

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
import com.example.mjbebikesaplicativomovil.viewmodel.InventarioViewModelFactory
import com.example.mjbebikesaplicativomovil.viewmodel.ProductoFormViewModel

// Formulario para crear (idProducto = null) o editar un producto.
@Composable
fun ProductoFormScreen(
    idProducto: Int?,
    onVolver: () -> Unit = {},
    onGuardado: () -> Unit = {}
) {

    val context = LocalContext.current

    val formViewModel: ProductoFormViewModel = viewModel(
        key = "producto_form_${idProducto ?: "nuevo"}",
        factory = InventarioViewModelFactory(context, idProducto)
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
                text = if (formViewModel.esEdicion) "Actualizar producto" else "Crear producto",
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

                    CampoTexto(f.imgProducto, { formViewModel.actualizar(f.copy(imgProducto = it)) },
                        "URL de la imagen", errores["imgProducto"], KeyboardType.Uri)

                    CampoTexto(f.nombreProducto, { formViewModel.actualizar(f.copy(nombreProducto = it)) },
                        "Nombre del producto", errores["nombreProducto"])

                    CampoTexto(f.descripcion, { formViewModel.actualizar(f.copy(descripcion = it)) },
                        "Descripción", errores["descripcion"], unaLinea = false)

                    CampoTexto(f.colorProducto, { formViewModel.actualizar(f.copy(colorProducto = it)) },
                        "Color", errores["colorProducto"])

                    CampoTexto(f.marcaProducto, { formViewModel.actualizar(f.copy(marcaProducto = it)) },
                        "Marca", errores["marcaProducto"])

                    CampoTexto(f.modelo, { formViewModel.actualizar(f.copy(modelo = it)) },
                        "Modelo", errores["modelo"])

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CampoTexto(f.cantidad, { formViewModel.actualizar(f.copy(cantidad = it)) },
                            "Cantidad", errores["cantidad"], KeyboardType.Number,
                            modifier = Modifier.weight(1f))

                        CampoTexto(f.valorUnitario, { formViewModel.actualizar(f.copy(valorUnitario = it)) },
                            "Valor unitario", errores["valorUnitario"], KeyboardType.Decimal,
                            modifier = Modifier.weight(1f))
                    }

                    Selector(
                        etiqueta = "Talla / Medida",
                        opciones = uiState.tallas,
                        seleccionada = uiState.tallas.find { it.id == f.idTalla },
                        textoDe = { talla ->
                            val medida = talla.medida?.tipoMedida
                            if (medida != null) "${talla.talla.orEmpty()} ($medida)" else talla.talla.orEmpty()
                        },
                        onSeleccionar = { formViewModel.actualizar(f.copy(idTalla = it.id)) },
                        error = errores["talla"]
                    )

                    Selector(
                        etiqueta = "Proveedor",
                        opciones = uiState.proveedores,
                        seleccionada = uiState.proveedores.find { it.id == f.idProveedor },
                        textoDe = { it.nombreProveedor.orEmpty() },
                        onSeleccionar = { formViewModel.actualizar(f.copy(idProveedor = it.id)) },
                        error = errores["proveedor"]
                    )

                    Selector(
                        etiqueta = "Local",
                        opciones = uiState.locales,
                        seleccionada = uiState.locales.find { it.id == f.idLocal },
                        textoDe = { it.nombreLocal.orEmpty() },
                        onSeleccionar = { formViewModel.actualizar(f.copy(idLocal = it.id)) },
                        error = errores["local"]
                    )

                    Selector(
                        etiqueta = "Estado",
                        opciones = listOf("activo", "inactivo"),
                        seleccionada = f.estado,
                        textoDe = { if (it == "activo") "Activo" else "Inactivo" },
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
                                Text(if (formViewModel.esEdicion) "Actualizar" else "Crear producto")
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
    tipoTeclado: KeyboardType = KeyboardType.Text,
    unaLinea: Boolean = true,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = unaLinea,
        minLines = if (unaLinea) 1 else 3,
        isError = error != null,
        supportingText = if (error != null) {
            { Text(error) }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
        modifier = modifier.fillMaxWidth()
    )
}
