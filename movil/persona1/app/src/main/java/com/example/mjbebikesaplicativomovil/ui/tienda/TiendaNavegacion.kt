package com.example.mjbebikesaplicativomovil.ui.tienda

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mjbebikesaplicativomovil.ui.carrito.CarritoScreen
import com.example.mjbebikesaplicativomovil.ui.catalogo.CatalogoScreen
import com.example.mjbebikesaplicativomovil.ui.catalogo.DetalleProductoScreen

// Rutas de la tienda (persona 1).
// La persona 2 puede copiar estos tres composable(...) en la navegación principal de la app.
object RutasTienda {
    const val CATALOGO = "catalogo"
    const val DETALLE = "producto/{id}"
    const val CARRITO = "carrito"

    fun detalle(id: Int) = "producto/$id"
}

@Composable
fun TiendaNavegacion(
    // Pantalla de finalizar compra de la persona 2 (mientras no exista, queda en null).
    onFinalizarCompra: (() -> Unit)? = null
) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = RutasTienda.CATALOGO
    ) {

        // Catálogo de productos
        composable(RutasTienda.CATALOGO) {
            CatalogoScreen(
                onProductoClick = { producto ->
                    producto.id?.let { navController.navigate(RutasTienda.detalle(it)) }
                },
                onCarritoClick = { navController.navigate(RutasTienda.CARRITO) }
            )
        }

        // Detalle de un producto: recibe el id en la ruta
        composable(
            route = RutasTienda.DETALLE,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            val id = entrada.arguments?.getInt("id") ?: 0
            DetalleProductoScreen(
                idProducto = id,
                onVolver = { navController.popBackStack() },
                onVerCarrito = { navController.navigate(RutasTienda.CARRITO) }
            )
        }

        // Carrito
        composable(RutasTienda.CARRITO) {
            CarritoScreen(
                onSeguirComprando = {
                    // Vuelve al catálogo sin apilar pantallas repetidas.
                    navController.popBackStack(RutasTienda.CATALOGO, inclusive = false)
                },
                onFinalizarCompra = onFinalizarCompra
            )
        }
    }
}
