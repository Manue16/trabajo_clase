
package com.example.mjbebikesaplicativomovil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.mjbebikesaplicativomovil.ui.navegacion.AppNavegacion
import com.example.mjbebikesaplicativomovil.ui.theme.MjbebikesAplicativoMovilTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MjbebikesAplicativoMovilTheme {
                // La navegación decide si abrir el login o el menú
                // según haya o no una sesión guardada.
                AppNavegacion()
            }
        }
    }
}
