package com.example.mjbebikesaplicativomovil.ui.proveedores

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mjbebikesaplicativomovil.remote.models.Proveedor
import com.example.mjbebikesaplicativomovil.utils.EstadoFiltro
import com.example.mjbebikesaplicativomovil.utils.ProveedorValidacion
import com.example.mjbebikesaplicativomovil.viewmodel.ProveedoresUiState
import com.example.mjbebikesaplicativomovil.viewmodel.ProveedoresViewModel
import com.example.mjbebikesaplicativomovil.viewmodel.ProveedoresViewModelFactory

// FilterChip es experimental en algunas versiones de Material 3.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProveedoresScreen(
    onNuevoProveedor: () -> Unit = {},
    onEditarProveedor: (Int) -> Unit = {}
) {

    val context = LocalContext.current

    val proveedoresViewModel: ProveedoresViewModel = viewModel(
        factory = ProveedoresViewModelFactory(context)
    )

    val uiState by proveedoresViewModel.uiState.collectAsState()
    val busqueda by proveedoresViewModel.busqueda.collectAsState()
    val estado by proveedoresViewModel.estado.collectAsState()
    val mensaje by proveedoresViewModel.mensaje.collectAsState()

    // Proveedor que se va a eliminar (abre la ventana de confirmación).
    var proveedorAEliminar by remember { mutableStateOf<Proveedor?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Se recarga cada vez que se entra a la pantalla (también al volver del formulario).
    LaunchedEffect(Unit) {
        proveedoresViewModel.cargarProveedores()
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            proveedoresViewModel.mensajeMostrado()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevoProveedor,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nuevo proveedor") }
            )
        }
    ) { espacio ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacio)
                .padding(16.dp)
        ) {

            Text(
                text = "Proveedores",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Listado de proveedores registrados",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = busqueda,
                onValueChange = { proveedoresViewModel.cambiarBusqueda(it) },
                label = { Text("Buscar") },
                placeholder = { Text("Nombre, N° de identidad o teléfono") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Filtro por estado.
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = estado == EstadoFiltro.TODOS,
                    onClick = { proveedoresViewModel.cambiarEstado(EstadoFiltro.TODOS) },
                    label = { Text("Todos") }
                )
                FilterChip(
                    selected = estado == EstadoFiltro.ACTIVOS,
                    onClick = { proveedoresViewModel.cambiarEstado(EstadoFiltro.ACTIVOS) },
                    label = { Text("Activos") }
                )
                FilterChip(
                    selected = estado == EstadoFiltro.INACTIVOS,
                    onClick = { proveedoresViewModel.cambiarEstado(EstadoFiltro.INACTIVOS) },
                    label = { Text("Inactivos") }
                )
            }

            when (val estadoUi = uiState) {

                is ProveedoresUiState.Cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ProveedoresUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = estadoUi.mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { proveedoresViewModel.cargarProveedores() }) {
                            Text("Reintentar")
                        }
                    }
                }

                is ProveedoresUiState.Listo -> {

                    val proveedores = remember(estadoUi.proveedores, busqueda, estado) {
                        ProveedorValidacion.filtrar(estadoUi.proveedores, busqueda, estado)
                    }

                    if (proveedores.isEmpty()) {
                        Text(
                            text = "No hay proveedores disponibles",
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                        ) {
                            items(proveedores, key = { it.id ?: it.hashCode() }) { proveedor ->
                                ProveedorCard(
                                    proveedor = proveedor,
                                    onEditar = { proveedor.id?.let(onEditarProveedor) },
                                    onCambiarEstado = { proveedoresViewModel.cambiarEstadoProveedor(proveedor) },
                                    onEliminar = { proveedorAEliminar = proveedor }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Ventana de confirmación antes de eliminar.
    proveedorAEliminar?.let { proveedor ->
        AlertDialog(
            onDismissRequest = { proveedorAEliminar = null },
            title = { Text("Eliminar proveedor") },
            text = {
                Text(
                    "¿Deseas eliminar a \"${proveedor.nombreProveedor.orEmpty()}\"? " +
                            "Si solo quieres dejar de usarlo, usa Desactivar."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    proveedoresViewModel.eliminarProveedor(proveedor)
                    proveedorAEliminar = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { proveedorAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Tarjeta de un proveedor con sus acciones.
@Composable
private fun ProveedorCard(
    proveedor: Proveedor,
    onEditar: () -> Unit,
    onCambiarEstado: () -> Unit,
    onEliminar: () -> Unit
) {
    val activo = ProveedorValidacion.esActivo(proveedor)

    Card(modifier = Modifier.fillMaxWidth()) {

        Column(modifier = Modifier.padding(12.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = proveedor.nombreProveedor.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (activo) "Activo" else "Inactivo",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (activo) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(text = "N° identidad: ${proveedor.numeroIdentidad.orEmpty()}")
            Text(text = "Teléfono: ${proveedor.telefono.orEmpty()}")
            Text(
                text = "Dirección: ${proveedor.direccion.orEmpty()}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEditar) {
                    Text("Editar")
                }
                TextButton(onClick = onCambiarEstado) {
                    Text(if (activo) "Desactivar" else "Activar")
                }
                TextButton(onClick = onEliminar) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
