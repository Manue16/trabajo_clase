package com.example.mjbebikesaplicativomovil.ui.compras

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// Rutas del módulo de compras (persona 1).
// La persona 2 puede copiar estos tres composable(...) en la navegación principal de la app.
object RutasCompras {
    const val LISTA = "compras"
    const val NUEVA = "compras/nueva"
    const val DETALLE = "compras/{id}"

    fun detalle(id: Int) = "compras/$id"
}

@Composable
fun ComprasNavegacion() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = RutasCompras.LISTA
    ) {

        // Historial de compras
        composable(RutasCompras.LISTA) {
            ComprasScreen(
                onNuevaCompra = { navController.navigate(RutasCompras.NUEVA) },
                onVerDetalle = { id -> navController.navigate(RutasCompras.detalle(id)) }
            )
        }

        // Crear compra (va antes que el detalle para que "nueva" no se lea como un id)
        composable(RutasCompras.NUEVA) {
            CompraFormScreen(
                onVolver = { navController.popBackStack() },
                onGuardado = { navController.popBackStack() }
            )
        }

        // Detalle de una compra: recibe el id en la ruta
        composable(
            route = RutasCompras.DETALLE,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            DetalleCompraScreen(
                idCompra = entrada.arguments?.getInt("id") ?: 0,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
