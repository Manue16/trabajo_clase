package com.example.mjbebikesaplicativomovil.ui.proveedores

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// Rutas del módulo de proveedores (persona 1).
// La persona 2 puede copiar estos tres composable(...) en la navegación principal de la app.
object RutasProveedores {
    const val LISTA = "proveedores"
    const val NUEVO = "proveedores/nuevo"
    const val EDITAR = "proveedores/{id}/editar"

    fun editar(id: Int) = "proveedores/$id/editar"
}

@Composable
fun ProveedoresNavegacion() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = RutasProveedores.LISTA
    ) {

        // Lista de proveedores
        composable(RutasProveedores.LISTA) {
            ProveedoresScreen(
                onNuevoProveedor = { navController.navigate(RutasProveedores.NUEVO) },
                onEditarProveedor = { id -> navController.navigate(RutasProveedores.editar(id)) }
            )
        }

        // Crear proveedor
        composable(RutasProveedores.NUEVO) {
            ProveedorFormScreen(
                idProveedor = null,
                onVolver = { navController.popBackStack() },
                onGuardado = { navController.popBackStack() }
            )
        }

        // Editar proveedor: recibe el id en la ruta
        composable(
            route = RutasProveedores.EDITAR,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            ProveedorFormScreen(
                idProveedor = entrada.arguments?.getInt("id"),
                onVolver = { navController.popBackStack() },
                onGuardado = { navController.popBackStack() }
            )
        }
    }
}
