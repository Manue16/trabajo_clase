package com.example.mjbebikesaplicativomovil.ui.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mjbebikesaplicativomovil.data.local.SesionUsuario
import com.example.mjbebikesaplicativomovil.utils.MenuOpciones
import com.example.mjbebikesaplicativomovil.utils.OpcionMenu
import com.example.mjbebikesaplicativomovil.utils.Roles

// Menú principal: muestra solo las opciones del rol del usuario.
@Composable
fun MenuScreen(
    usuario: SesionUsuario?,
    onAbrir: (OpcionMenu) -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {

    // Ventana de confirmación para cerrar sesión.
    var confirmarSalida by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        // Mientras se leen los datos guardados.
        if (usuario == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Surface
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // Título del menú (como el "Menú SARA" de la guía).
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1976D2)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Menú MJBE Bikes",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White
                        )
                        Text(
                            text = "Hola, ${usuario.login} · ${Roles.nombre(usuario.rolId)}",
                            color = Color.White
                        )
                    }
                }
            }

            val opciones = MenuOpciones.opcionesDe(usuario.rolId)

            if (opciones.isEmpty()) {
                Text("Tu rol no tiene opciones disponibles en la app.")
            }

            // Botones del menú en dos columnas.
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(opciones, key = { it.ruta }) { opcion ->
                    OpcionCard(opcion = opcion, onClick = { onAbrir(opcion) })
                }
            }

            OutlinedButton(
                onClick = { confirmarSalida = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión")
            }
        }
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Deseas cerrar tu sesión?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarSalida = false
                    onCerrarSesion()
                }) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalida = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Un botón del menú: título, descripción y aviso si el módulo todavía no está listo.
@Composable
private fun OpcionCard(
    opcion: OpcionMenu,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 110.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = opcion.titulo,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = opcion.descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!opcion.disponible) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Próximamente",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}
