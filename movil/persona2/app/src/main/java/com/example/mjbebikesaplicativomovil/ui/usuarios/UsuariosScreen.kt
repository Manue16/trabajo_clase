package com.example.mjbebikesaplicativomovil.ui.usuarios

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
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
import com.example.mjbebikesaplicativomovil.remote.models.Usuario
import com.example.mjbebikesaplicativomovil.utils.EstadoFiltro
import com.example.mjbebikesaplicativomovil.utils.Roles
import com.example.mjbebikesaplicativomovil.utils.UsuarioUtils
import com.example.mjbebikesaplicativomovil.viewmodel.UsuariosUiState
import com.example.mjbebikesaplicativomovil.viewmodel.UsuariosViewModel
import com.example.mjbebikesaplicativomovil.viewmodel.UsuariosViewModelFactory

// FilterChip es experimental en algunas versiones de Material 3.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuariosScreen(
    onNuevoUsuario: () -> Unit = {},
    onEditarUsuario: (Int) -> Unit = {}
) {

    val context = LocalContext.current

    val usuariosViewModel: UsuariosViewModel = viewModel(
        factory = UsuariosViewModelFactory(context)
    )

    val uiState by usuariosViewModel.uiState.collectAsState()
    val busqueda by usuariosViewModel.busqueda.collectAsState()
    val rol by usuariosViewModel.rol.collectAsState()
    val estado by usuariosViewModel.estado.collectAsState()
    val mensaje by usuariosViewModel.mensaje.collectAsState()

    // Usuario que se va a eliminar (abre la ventana de confirmación).
    var usuarioAEliminar by remember { mutableStateOf<Usuario?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Se recarga cada vez que se entra a la pantalla (también al volver del formulario).
    LaunchedEffect(Unit) {
        usuariosViewModel.cargarUsuarios()
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            usuariosViewModel.mensajeMostrado()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevoUsuario,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Crear empleado") }
            )
        }
    ) { espacio ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacio)
                .padding(16.dp)
        ) {

            Text(text = "Usuarios", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = busqueda,
                onValueChange = { usuariosViewModel.cambiarBusqueda(it) },
                label = { Text("Buscar") },
                placeholder = { Text("Usuario o correo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Filtro por rol.
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UsuarioUtils.rolesDisponibles.forEach { idRol ->
                    FilterChip(
                        selected = rol == idRol,
                        onClick = { usuariosViewModel.cambiarRol(idRol) },
                        label = { Text(Roles.nombre(idRol)) }
                    )
                }
            }

            // Filtro por estado.
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = estado == EstadoFiltro.TODOS,
                    onClick = { usuariosViewModel.cambiarEstado(EstadoFiltro.TODOS) },
                    label = { Text("Todos") }
                )
                FilterChip(
                    selected = estado == EstadoFiltro.ACTIVOS,
                    onClick = { usuariosViewModel.cambiarEstado(EstadoFiltro.ACTIVOS) },
                    label = { Text("Activos") }
                )
                FilterChip(
                    selected = estado == EstadoFiltro.INACTIVOS,
                    onClick = { usuariosViewModel.cambiarEstado(EstadoFiltro.INACTIVOS) },
                    label = { Text("Inactivos") }
                )
            }

            when (val estadoUi = uiState) {

                is UsuariosUiState.Cargando -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is UsuariosUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = estadoUi.mensaje, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { usuariosViewModel.cargarUsuarios() }) {
                            Text("Reintentar")
                        }
                    }
                }

                is UsuariosUiState.Listo -> {
                    val usuarios = remember(estadoUi.usuarios, busqueda, rol, estado) {
                        UsuarioUtils.filtrar(estadoUi.usuarios, busqueda, rol, estado)
                    }

                    if (usuarios.isEmpty()) {
                        Text(
                            text = "No hay usuarios disponibles",
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                        ) {
                            items(usuarios, key = { it.id ?: it.hashCode() }) { usuario ->
                                UsuarioCard(
                                    usuario = usuario,
                                    esPropio = usuario.id == estadoUi.idPropio,
                                    onEditar = { usuario.id?.let(onEditarUsuario) },
                                    onCambiarEstado = { usuariosViewModel.cambiarEstadoUsuario(usuario) },
                                    onEliminar = { usuarioAEliminar = usuario }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Ventana de confirmación antes de eliminar.
    usuarioAEliminar?.let { usuario ->
        AlertDialog(
            onDismissRequest = { usuarioAEliminar = null },
            title = { Text("Eliminar usuario") },
            text = {
                Text(
                    "¿Deseas eliminar a \"${usuario.login.orEmpty()}\"? " +
                            "Si tiene ventas u otros registros no se podrá eliminar; en ese caso, desactívalo."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    usuariosViewModel.eliminarUsuario(usuario)
                    usuarioAEliminar = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { usuarioAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Tarjeta de un usuario con sus acciones. El usuario propio no se puede desactivar ni eliminar.
@Composable
private fun UsuarioCard(
    usuario: Usuario,
    esPropio: Boolean,
    onEditar: () -> Unit,
    onCambiarEstado: () -> Unit,
    onEliminar: () -> Unit
) {
    val activo = UsuarioUtils.esActivo(usuario)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = usuario.login.orEmpty() + if (esPropio) " (tú)" else "",
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

            Text(text = usuario.email.orEmpty())
            Text(
                text = "Rol: ${Roles.nombre(usuario.rolId)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEditar) {
                    Text("Editar")
                }
                if (!esPropio) {
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
}
