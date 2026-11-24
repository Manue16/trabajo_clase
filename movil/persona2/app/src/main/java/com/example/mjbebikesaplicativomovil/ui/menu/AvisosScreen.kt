package com.example.mjbebikesaplicativomovil.ui.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// Pantalla para los módulos que todavía no están hechos.
@Composable
fun ProximamenteScreen(
    titulo: String,
    onVolver: () -> Unit = {}
) {
    Aviso(
        titulo = titulo,
        mensaje = "Este módulo todavía está en desarrollo.",
        textoBoton = "Volver al menú",
        onBoton = onVolver
    )
}

// Pantalla cuando el rol del usuario no puede abrir una ruta.
@Composable
fun SinPermisoScreen(
    haySesion: Boolean,
    onVolver: () -> Unit = {},
    onIniciarSesion: () -> Unit = {}
) {
    if (haySesion) {
        Aviso(
            titulo = "Sin permiso",
            mensaje = "Tu rol no tiene acceso a esta pantalla.",
            textoBoton = "Volver al menú",
            onBoton = onVolver
        )
    } else {
        Aviso(
            titulo = "Inicia sesión",
            mensaje = "Debes iniciar sesión para ver esta pantalla.",
            textoBoton = "Iniciar sesión",
            onBoton = onIniciarSesion
        )
    }
}

@Composable
private fun Aviso(
    titulo: String,
    mensaje: String,
    textoBoton: String,
    onBoton: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = mensaje,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onBoton) {
                Text(textoBoton)
            }
        }
    }
}
