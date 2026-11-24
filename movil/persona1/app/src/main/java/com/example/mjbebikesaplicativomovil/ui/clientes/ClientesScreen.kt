package com.example.mjbebikesaplicativomovil.ui.clientes

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
import androidx.compose.material3.ExtendedFloatingActionButton
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
import com.example.mjbebikesaplicativomovil.remote.models.Cliente
import com.example.mjbebikesaplicativomovil.utils.ClienteValidacion
import com.example.mjbebikesaplicativomovil.viewmodel.ClientesUiState
import com.example.mjbebikesaplicativomovil.viewmodel.ClientesViewModel
import com.example.mjbebikesaplicativomovil.viewmodel.ClientesViewModelFactory

@Composable
fun ClientesScreen(
    onNuevoCliente: () -> Unit = {},
    onEditarCliente: (Int) -> Unit = {}
) {

    val context = LocalContext.current

    val clientesViewModel: ClientesViewModel = viewModel(
        factory = ClientesViewModelFactory(context)
    )

    val uiState by clientesViewModel.uiState.collectAsState()
    val busqueda by clientesViewModel.busqueda.collectAsState()
    val mensaje by clientesViewModel.mensaje.collectAsState()

    // Cliente que se va a eliminar (abre la ventana de confirmación).
    var clienteAEliminar by remember { mutableStateOf<Cliente?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Se recarga cada vez que se entra a la pantalla (también al volver del formulario).
    LaunchedEffect(Unit) {
        clientesViewModel.cargarClientes()
    }

    // Muestra los mensajes cortos ("Cliente eliminado", errores...).
    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            clientesViewModel.mensajeMostrado()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevoCliente,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nuevo cliente") }
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
                text = "Clientes",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Listado de clientes registrados",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = busqueda,
                onValueChange = { clientesViewModel.cambiarBusqueda(it) },
                label = { Text("Buscar") },
                placeholder = { Text("Nombre, documento o teléfono") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            when (val estado = uiState) {

                is ClientesUiState.Cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ClientesUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = estado.mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { clientesViewModel.cargarClientes() }) {
                            Text("Reintentar")
                        }
                    }
                }

                is ClientesUiState.Listo -> {

                    val clientes = remember(estado.clientes, busqueda) {
                        ClienteValidacion.filtrar(estado.clientes, busqueda)
                    }

                    if (clientes.isEmpty()) {
                        Text(
                            text = "No hay clientes disponibles",
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            // Espacio abajo para que el botón flotante no tape el último cliente.
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(clientes, key = { it.id ?: it.hashCode() }) { cliente ->
                                ClienteCard(
                                    cliente = cliente,
                                    onEditar = { cliente.id?.let(onEditarCliente) },
                                    onEliminar = { clienteAEliminar = cliente }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Ventana de confirmación antes de eliminar.
    clienteAEliminar?.let { cliente ->
        AlertDialog(
            onDismissRequest = { clienteAEliminar = null },
            title = { Text("Eliminar cliente") },
            text = { Text("¿Deseas eliminar a ${ClienteValidacion.nombreCompleto(cliente)}?") },
            confirmButton = {
                TextButton(onClick = {
                    clientesViewModel.eliminarCliente(cliente)
                    clienteAEliminar = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { clienteAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Tarjeta de un cliente con sus botones de acción.
@Composable
private fun ClienteCard(
    cliente: Cliente,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {

        Column(modifier = Modifier.padding(12.dp)) {

            Text(
                text = ClienteValidacion.nombreCompleto(cliente),
                style = MaterialTheme.typography.titleMedium
            )
            Text(text = "Documento: ${cliente.numeroDocumento.orEmpty()}")
            Text(text = "Teléfono: ${cliente.telefono.orEmpty()}")
            Text(
                text = "Dirección: ${cliente.direccion.orEmpty()}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEditar) {
                    Text("Editar")
                }
                TextButton(onClick = onEliminar) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
