package com.example.mjbebikesaplicativomovil.remote.models

// Respuesta de GET /reportes/dashboard (panel del administrador).
data class Dashboard(
    val resumen: ResumenDashboard? = null,
    // Las 5 ventas más recientes.
    val reportes: List<ReporteDashboard>? = null
)

// Los nombres coinciden con el JSON de la API (ya vienen en camelCase).
data class ResumenDashboard(
    val totalProductos: Int? = null,
    val totalClientes: Int? = null,
    val totalVentas: Int? = null,
    val ventasHoy: Int? = null,
    val ventasMes: Int? = null,
    // Productos con 5 unidades o menos (incluye los agotados).
    val stockBajo: Int? = null,
    val ingresosMes: Double? = null
)

// Una venta reciente: { "titulo": "Venta #7", "descripcion": "Cliente: ... - Total: $...", "fecha": "..." }
data class ReporteDashboard(
    val id: Int? = null,
    val titulo: String? = null,
    val descripcion: String? = null,
    val fecha: String? = null
)
