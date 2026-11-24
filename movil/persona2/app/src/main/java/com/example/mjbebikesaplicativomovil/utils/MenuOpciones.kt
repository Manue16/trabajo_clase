package com.example.mjbebikesaplicativomovil.utils

// Roles de la tabla roles (los mismos de la web).
object Roles {
    const val ADMIN = 1
    const val VENDEDOR = 2
    const val BODEGUERO = 3
    const val CLIENTE = 4

    fun nombre(rolId: Int?): String = when (rolId) {
        ADMIN -> "Administrador"
        VENDEDOR -> "Vendedor"
        BODEGUERO -> "Bodeguero"
        CLIENTE -> "Cliente"
        else -> "Sin rol"
    }
}

// Un botón del menú.
data class OpcionMenu(
    val titulo: String,
    val descripcion: String,
    val ruta: String,
    val roles: Set<Int>,
    // false = el módulo todavía no está hecho (se muestra como "Próximamente").
    val disponible: Boolean = true
)

object MenuOpciones {

    // Todas las opciones de la app y qué roles las ven (igual que los permisos de la web).
    val todas: List<OpcionMenu> = listOf(
        // Tienda (persona 1)
        OpcionMenu("Tienda", "Catálogo de productos", "catalogo",
            setOf(Roles.ADMIN, Roles.VENDEDOR, Roles.CLIENTE)),
        OpcionMenu("Mi carrito", "Productos que vas a comprar", "carrito",
            setOf(Roles.CLIENTE)),

        // Compra del cliente (persona 2)
        OpcionMenu("Mis compras", "Historial de tus compras", "mis-compras",
            setOf(Roles.CLIENTE)),

        // Inventario y abastecimiento (persona 1)
        OpcionMenu("Inventario", "Productos y stock", "inventario",
            setOf(Roles.ADMIN, Roles.BODEGUERO)),
        OpcionMenu("Proveedores", "Gestión de proveedores", "proveedores",
            setOf(Roles.ADMIN)),
        OpcionMenu("Compras", "Compras a proveedores", "compras",
            setOf(Roles.ADMIN)),
        OpcionMenu("Clientes", "Gestión de clientes", "clientes",
            setOf(Roles.ADMIN, Roles.VENDEDOR)),

        // Ventas y administración (persona 2)
        OpcionMenu("Nueva venta", "Registrar una venta en tienda", "ventas/nueva",
            setOf(Roles.VENDEDOR)),
        OpcionMenu("Ventas", "Historial de ventas", "ventas",
            setOf(Roles.ADMIN, Roles.VENDEDOR)),
        OpcionMenu("Usuarios", "Empleados y cuentas", "usuarios",
            setOf(Roles.ADMIN)),
        OpcionMenu("Panel", "Resumen e indicadores", "panel",
            setOf(Roles.ADMIN, Roles.VENDEDOR, Roles.BODEGUERO))
    )

    // Rutas que se pueden ver sin iniciar sesión (igual que la tienda de la web).
    private val publicas = setOf("catalogo", "producto", "carrito")

    // Opciones que ve un rol en el menú.
    fun opcionesDe(rolId: Int?): List<OpcionMenu> =
        if (rolId == null) emptyList() else todas.filter { rolId in it.roles }

    // ¿Puede este rol abrir esta ruta? Sirve también para las rutas internas:
    // "clientes/nuevo" o "inventario/5/editar" usan el permiso de "clientes" o "inventario".
    // Si dos opciones encajan, manda la más específica ("ventas/nueva" antes que "ventas").
    fun puedeEntrar(rolId: Int?, ruta: String): Boolean {
        val base = ruta.substringBefore("/")
        if (base in publicas) return true
        if (rolId == null) return false

        val opcion = todas
            .filter { ruta == it.ruta || ruta.startsWith(it.ruta + "/") }
            .maxByOrNull { it.ruta.length }
            ?: return false

        return rolId in opcion.roles
    }
}
