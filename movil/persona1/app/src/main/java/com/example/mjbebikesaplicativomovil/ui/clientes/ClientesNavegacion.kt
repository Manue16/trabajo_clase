package com.example.mjbebikesaplicativomovil.ui.clientes

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// Rutas del módulo de clientes (persona 1).
// La persona 2 puede copiar estos tres composable(...) en la navegación principal de la app.
object RutasClientes {
    const val LISTA = "clientes"
    const val NUEVO = "clientes/nuevo"
    const val EDITAR = "clientes/{id}/editar"

    fun editar(id: Int) = "clientes/$id/editar"
}

@Composable
fun ClientesNavegacion() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = RutasClientes.LISTA
    ) {

        // Lista de clientes
        composable(RutasClientes.LISTA) {
            ClientesScreen(
                onNuevoCliente = { navController.navigate(RutasClientes.NUEVO) },
                onEditarCliente = { id -> navController.navigate(RutasClientes.editar(id)) }
            )
        }

        // Crear cliente
        composable(RutasClientes.NUEVO) {
            ClienteFormScreen(
                idCliente = null,
                onVolver = { navController.popBackStack() },
                onGuardado = { navController.popBackStack() }
            )
        }

        // Editar cliente: recibe el id en la ruta
        composable(
            route = RutasClientes.EDITAR,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            ClienteFormScreen(
                idCliente = entrada.arguments?.getInt("id"),
                onVolver = { navController.popBackStack() },
                onGuardado = { navController.popBackStack() }
            )
        }
    }
}
