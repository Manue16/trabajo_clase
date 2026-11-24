package com.example.mjbebikesaplicativomovil.ui.componentes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

// Lista desplegable reutilizable para los formularios (usuario, tipo de documento,
// proveedor, talla, local...). Muestra el texto de la opción elegida y,
// al tocarla, abre la lista.
@Composable
fun <T> Selector(
    etiqueta: String,
    opciones: List<T>,
    seleccionada: T?,
    textoDe: (T) -> String,
    onSeleccionar: (T) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    var abierto by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {

        OutlinedTextField(
            value = seleccionada?.let(textoDe) ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            placeholder = { Text("Seleccione…") },
            trailingIcon = { Text(if (abierto) "▲" else "▼") },
            isError = error != null,
            supportingText = if (error != null) {
                { Text(error) }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )

        // Capa invisible encima del campo para abrir la lista al tocarlo.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { abierto = true }
        )

        DropdownMenu(
            expanded = abierto,
            onDismissRequest = { abierto = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(textoDe(opcion)) },
                    onClick = {
                        onSeleccionar(opcion)
                        abierto = false
                    }
                )
            }
        }
    }
}
