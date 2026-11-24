package com.example.mjbebikesaplicativomovil.ui.navegacion

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mjbebikesaplicativomovil.data.local.SesionUsuario
import com.example.mjbebikesaplicativomovil.ui.carrito.CarritoScreen
import com.example.mjbebikesaplicativomovil.ui.catalogo.CatalogoScreen
import com.example.mjbebikesaplicativomovil.ui.catalogo.DetalleProductoScreen
import com.example.mjbebikesaplicativomovil.ui.clientes.ClienteFormScreen
import com.example.mjbebikesaplicativomovil.ui.clientes.ClientesScreen
import com.example.mjbebikesaplicativomovil.ui.clientes.RutasClientes
import com.example.mjbebikesaplicativomovil.ui.compras.CompraFormScreen
import com.example.mjbebikesaplicativomovil.ui.compras.ComprasScreen
import com.example.mjbebikesaplicativomovil.ui.compras.DetalleCompraScreen
import com.example.mjbebikesaplicativomovil.ui.compras.RutasCompras
import com.example.mjbebikesaplicativomovil.ui.inventario.InventarioScreen
import com.example.mjbebikesaplicativomovil.ui.inventario.ProductoFormScreen
import com.example.mjbebikesaplicativomovil.ui.inventario.RutasInventario
import com.example.mjbebikesaplicativomovil.ui.login.LoginScreen
import com.example.mjbebikesaplicativomovil.ui.menu.MenuScreen
import com.example.mjbebikesaplicativomovil.ui.menu.ProximamenteScreen
import com.example.mjbebikesaplicativomovil.ui.menu.SinPermisoScreen
import com.example.mjbebikesaplicativomovil.ui.proveedores.ProveedorFormScreen
import com.example.mjbebikesaplicativomovil.ui.proveedores.ProveedoresScreen
import com.example.mjbebikesaplicativomovil.ui.proveedores.RutasProveedores
import com.example.mjbebikesaplicativomovil.ui.tienda.RutasTienda
import com.example.mjbebikesaplicativomovil.utils.MenuOpciones
import com.example.mjbebikesaplicativomovil.utils.Roles
import com.example.mjbebikesaplicativomovil.viewmodel.SesionUiState
import com.example.mjbebikesaplicativomovil.viewmodel.SesionViewModel
import com.example.mjbebikesaplicativomovil.viewmodel.SesionViewModelFactory

// Rutas propias de la persona 2.
object RutasApp {
    const val LOGIN = "login"
    const val MENU = "menu"
    const val CHECKOUT = "checkout"
    const val MIS_COMPRAS = "mis-compras"
    const val NUEVA_VENTA = "ventas/nueva"
    const val VENTAS = "ventas"
    const val USUARIOS = "usuarios"
    const val PANEL = "panel"
}

// Navegación principal de toda la app.
@Composable
fun AppNavegacion() {

    val context = LocalContext.current

    val sesionViewModel: SesionViewModel = viewModel(
        factory = SesionViewModelFactory(context)
    )

    val estado by sesionViewModel.estado.collectAsState()

    // Mientras se lee la sesión guardada en el teléfono.
    if (estado is SesionUiState.Cargando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val navController = rememberNavController()

    // Pantalla inicial: el menú si ya había sesión, el login si no.
    val inicio = remember { if (estado is SesionUiState.ConSesion) RutasApp.MENU else RutasApp.LOGIN }

    NavHost(navController = navController, startDestination = inicio) {

        // ---------------- Acceso ----------------

        composable(RutasApp.LOGIN) {
            LoginScreen(
                onLoginExitoso = {
                    navController.navigate(RutasApp.MENU) {
                        popUpTo(RutasApp.LOGIN) { inclusive = true }
                    }
                },
                onVerTienda = { navController.navigate(RutasTienda.CATALOGO) }
            )
        }

        composable(RutasApp.MENU) {
            MenuScreen(
                usuario = usuarioActual(sesionViewModel),
                onAbrir = { opcion -> navController.navigate(opcion.ruta) },
                onCerrarSesion = {
                    sesionViewModel.cerrarSesion {
                        navController.navigate(RutasApp.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                }
            )
        }

        // ---------------- Tienda (persona 1, pública) ----------------

        composable(RutasTienda.CATALOGO) {
            CatalogoScreen(
                onProductoClick = { producto ->
                    producto.id?.let { navController.navigate(RutasTienda.detalle(it)) }
                },
                onCarritoClick = { navController.navigate(RutasTienda.CARRITO) }
            )
        }

        composable(
            route = RutasTienda.DETALLE,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            DetalleProductoScreen(
                idProducto = entrada.arguments?.getInt("id") ?: 0,
                onVolver = { navController.popBackStack() },
                onVerCarrito = { navController.navigate(RutasTienda.CARRITO) }
            )
        }

        composable(RutasTienda.CARRITO) {
            CarritoScreen(
                onSeguirComprando = { navController.popBackStack() },
                onFinalizarCompra = { navController.navigate(RutasApp.CHECKOUT) }
            )
        }

        // ---------------- Clientes (persona 1) ----------------

        composable(RutasClientes.LISTA) {
            Protegida(sesionViewModel, RutasClientes.LISTA, navController) {
                ClientesScreen(
                    onNuevoCliente = { navController.navigate(RutasClientes.NUEVO) },
                    onEditarCliente = { id -> navController.navigate(RutasClientes.editar(id)) }
                )
            }
        }

        composable(RutasClientes.NUEVO) {
            Protegida(sesionViewModel, RutasClientes.NUEVO, navController) {
                ClienteFormScreen(
                    idCliente = null,
                    onVolver = { navController.popBackStack() },
                    onGuardado = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = RutasClientes.EDITAR,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            Protegida(sesionViewModel, RutasClientes.LISTA, navController) {
                ClienteFormScreen(
                    idCliente = entrada.arguments?.getInt("id"),
                    onVolver = { navController.popBackStack() },
                    onGuardado = { navController.popBackStack() }
                )
            }
        }

        // ---------------- Inventario (persona 1) ----------------

        composable(RutasInventario.LISTA) {
            Protegida(sesionViewModel, RutasInventario.LISTA, navController) {
                InventarioScreen(
                    onNuevoProducto = { navController.navigate(RutasInventario.NUEVO) },
                    onEditarProducto = { id -> navController.navigate(RutasInventario.editar(id)) }
                )
            }
        }

        composable(RutasInventario.NUEVO) {
            Protegida(sesionViewModel, RutasInventario.NUEVO, navController) {
                ProductoFormScreen(
                    idProducto = null,
                    onVolver = { navController.popBackStack() },
                    onGuardado = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = RutasInventario.EDITAR,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            Protegida(sesionViewModel, RutasInventario.LISTA, navController) {
                ProductoFormScreen(
                    idProducto = entrada.arguments?.getInt("id"),
                    onVolver = { navController.popBackStack() },
                    onGuardado = { navController.popBackStack() }
                )
            }
        }

        // ---------------- Proveedores (persona 1) ----------------

        composable(RutasProveedores.LISTA) {
            Protegida(sesionViewModel, RutasProveedores.LISTA, navController) {
                ProveedoresScreen(
                    onNuevoProveedor = { navController.navigate(RutasProveedores.NUEVO) },
                    onEditarProveedor = { id -> navController.navigate(RutasProveedores.editar(id)) }
                )
            }
        }

        composable(RutasProveedores.NUEVO) {
            Protegida(sesionViewModel, RutasProveedores.NUEVO, navController) {
                ProveedorFormScreen(
                    idProveedor = null,
                    onVolver = { navController.popBackStack() },
                    onGuardado = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = RutasProveedores.EDITAR,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            Protegida(sesionViewModel, RutasProveedores.LISTA, navController) {
                ProveedorFormScreen(
                    idProveedor = entrada.arguments?.getInt("id"),
                    onVolver = { navController.popBackStack() },
                    onGuardado = { navController.popBackStack() }
                )
            }
        }

        // ---------------- Compras (persona 1) ----------------

        composable(RutasCompras.LISTA) {
            Protegida(sesionViewModel, RutasCompras.LISTA, navController) {
                ComprasScreen(
                    onNuevaCompra = { navController.navigate(RutasCompras.NUEVA) },
                    onVerDetalle = { id -> navController.navigate(RutasCompras.detalle(id)) }
                )
            }
        }

        composable(RutasCompras.NUEVA) {
            Protegida(sesionViewModel, RutasCompras.NUEVA, navController) {
                CompraFormScreen(
                    onVolver = { navController.popBackStack() },
                    onGuardado = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = RutasCompras.DETALLE,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            Protegida(sesionViewModel, RutasCompras.LISTA, navController) {
                DetalleCompraScreen(
                    idCompra = entrada.arguments?.getInt("id") ?: 0,
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        // ---------------- Módulos de la persona 2 (por hacer) ----------------
        // Cuando estén listos, se reemplaza ProximamenteScreen por la pantalla real.

        composable(RutasApp.CHECKOUT) {
            // Finalizar compra: solo el cliente con sesión (como en la web).
            val rolId = rolActual(sesionViewModel)
            if (rolId == Roles.CLIENTE) {
                ProximamenteScreen("Finalizar compra", onVolver = { navController.popBackStack() })
            } else {
                SinPermisoScreen(
                    haySesion = rolId != null,
                    onVolver = { navController.popBackStack() },
                    onIniciarSesion = { navController.navigate(RutasApp.LOGIN) }
                )
            }
        }

        listOf(
            RutasApp.MIS_COMPRAS to "Mis compras",
            RutasApp.NUEVA_VENTA to "Nueva venta",
            RutasApp.VENTAS to "Ventas",
            RutasApp.USUARIOS to "Usuarios",
            RutasApp.PANEL to "Panel"
        ).forEach { (ruta, titulo) ->
            composable(ruta) {
                Protegida(sesionViewModel, ruta, navController) {
                    ProximamenteScreen(titulo, onVolver = { navController.popBackStack() })
                }
            }
        }
    }
}

// Usuario de la sesión actual (null si no hay sesión).
// Cada pantalla lo lee en el momento, así no hay que rehacer la navegación al entrar o salir.
@Composable
private fun usuarioActual(sesionViewModel: SesionViewModel): SesionUsuario? {
    val estado by sesionViewModel.estado.collectAsState()
    return (estado as? SesionUiState.ConSesion)?.usuario
}

@Composable
private fun rolActual(sesionViewModel: SesionViewModel): Int? = usuarioActual(sesionViewModel)?.rolId

// Muestra la pantalla solo si el rol tiene permiso para esa ruta (igual que la web).
@Composable
private fun Protegida(
    sesionViewModel: SesionViewModel,
    ruta: String,
    navController: NavHostController,
    contenido: @Composable () -> Unit
) {
    val rolId = rolActual(sesionViewModel)

    if (MenuOpciones.puedeEntrar(rolId, ruta)) {
        contenido()
    } else {
        SinPermisoScreen(
            haySesion = rolId != null,
            onVolver = { navController.popBackStack() },
            onIniciarSesion = {
                navController.navigate(RutasApp.LOGIN) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            }
        )
    }
}
