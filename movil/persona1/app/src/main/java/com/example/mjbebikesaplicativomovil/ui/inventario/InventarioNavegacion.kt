package com.example.mjbebikesaplicativomovil.ui.inventario

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// Rutas del módulo de inventario (persona 1).
// La persona 2 puede copiar estos tres composable(...) en la navegación principal de la app.
object RutasInventario {
    const val LISTA = "inventario"
    const val NUEVO = "inventario/nuevo"
    const val EDITAR = "inventario/{id}/editar"

    fun editar(id: Int) = "inventario/$id/editar"
}

@Composable
fun InventarioNavegacion() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = RutasInventario.LISTA
    ) {

        // Lista del inventario
        composable(RutasInventario.LISTA) {
            InventarioScreen(
                onNuevoProducto = { navController.navigate(RutasInventario.NUEVO) },
                onEditarProducto = { id -> navController.navigate(RutasInventario.editar(id)) }
            )
        }

        // Crear producto
        composable(RutasInventario.NUEVO) {
            ProductoFormScreen(
                idProducto = null,
                onVolver = { navController.popBackStack() },
                onGuardado = { navController.popBackStack() }
            )
        }

        // Editar producto: recibe el id en la ruta
        composable(
            route = RutasInventario.EDITAR,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            ProductoFormScreen(
                idProducto = entrada.arguments?.getInt("id"),
                onVolver = { navController.popBackStack() },
                onGuardado = { navController.popBackStack() }
            )
        }
    }
}
