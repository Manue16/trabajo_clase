package com.example.mjbebikesaplicativomovil.utils

import com.example.mjbebikesaplicativomovil.data.repository.ItemCarrito
import com.example.mjbebikesaplicativomovil.remote.models.Cliente
import com.example.mjbebikesaplicativomovil.remote.models.ItemVentaNueva
import com.example.mjbebikesaplicativomovil.remote.models.Venta
import com.example.mjbebikesaplicativomovil.remote.models.VentaNueva

// Formas de pago simuladas (las mismas de la web).
enum class MetodoPago(val valorApi: String, val texto: String) {
    TARJETA("tarjeta", "Tarjeta"),
    TRANSFERENCIA("transferencia", "Transferencia"),
    CONTRAENTREGA("contraentrega", "Contraentrega");

    // Tarjeta y transferencia quedan pagadas; contraentrega se paga al recibir.
    val estadoVenta: String
        get() = if (this == CONTRAENTREGA) "pendiente" else "completada"
}

// Datos del formulario de finalizar compra.
// Los datos personales solo se piden si el usuario todavía no tiene registro de cliente.
data class CheckoutFormulario(
    val nombres: String = "",
    val apellidos: String = "",
    val tipoDocumentoId: Int? = null,
    val numeroDocumento: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val metodoPago: MetodoPago = MetodoPago.TARJETA
) {
    companion object {
        // Llena teléfono y dirección con los datos del cliente ya registrado.
        fun desde(cliente: Cliente) = CheckoutFormulario(
            nombres = cliente.nombres.orEmpty(),
            apellidos = cliente.apellidos.orEmpty(),
            tipoDocumentoId = cliente.tipoDocumentoId,
            numeroDocumento = cliente.numeroDocumento.orEmpty(),
            telefono = cliente.telefono.orEmpty(),
            direccion = cliente.direccion.orEmpty()
        )
    }
}

object CheckoutUtils {

    // Revisa el formulario. Si es la primera compra (sin registro de cliente),
    // también exige los datos personales que pide la tabla clientes.
    fun validar(f: CheckoutFormulario, esPrimeraCompra: Boolean): Map<String, String> {
        val errores = mutableMapOf<String, String>()

        if (esPrimeraCompra) {
            // Se reutilizan las reglas del formulario de clientes (persona 1).
            val datosCliente = ClienteValidacion.validar(aFormularioCliente(f, usuarioId = 0))
            listOf("tipoDocumento", "numeroDocumento", "nombres", "apellidos").forEach { campo ->
                datosCliente[campo]?.let { errores[campo] = it }
            }
        }

        val telefono = f.telefono.trim()
        when {
            telefono.isEmpty() -> errores["telefono"] = "El teléfono es obligatorio"
            telefono.length > 20 -> errores["telefono"] = "Máximo 20 caracteres"
            !telefono.all { it.isDigit() || it == ' ' || it == '+' || it == '-' } ->
                errores["telefono"] = "El teléfono solo puede tener números, espacios, + o -"
        }

        val direccion = f.direccion.trim()
        when {
            direccion.isEmpty() -> errores["direccion"] = "La dirección de entrega es obligatoria"
            direccion.length > 150 -> errores["direccion"] = "Máximo 150 caracteres"
        }

        return errores
    }

    // Formulario de cliente para crear el registro en la primera compra.
    fun aFormularioCliente(f: CheckoutFormulario, usuarioId: Int) = ClienteFormulario(
        usuarioId = usuarioId,
        tipoDocumentoId = f.tipoDocumentoId,
        numeroDocumento = f.numeroDocumento,
        nombres = f.nombres,
        apellidos = f.apellidos,
        direccion = f.direccion,
        telefono = f.telefono
    )

    fun total(items: List<ItemCarrito>): Double = items.sumOf { it.subtotal }

    // Arma el cuerpo de POST /ventas con los productos del carrito.
    fun armarVenta(
        usuarioId: Int,
        idCliente: Int,
        items: List<ItemCarrito>,
        f: CheckoutFormulario,
        fecha: String
    ): VentaNueva = VentaNueva(
        userId = usuarioId,
        cliente = idCliente,
        productos = items.mapNotNull { item ->
            item.producto.id?.let { id ->
                ItemVentaNueva(
                    id = id,
                    cantidad = item.cantidad,
                    valorUnitario = item.producto.valorUnitario ?: 0.0
                )
            }
        },
        total = total(items),
        direccion = f.direccion.trim(),
        metodoPago = f.metodoPago.valorApi,
        estado = f.metodoPago.estadoVenta,
        fecha = fecha
    )

    // "Pagada" o "Pendiente" para mostrar en Mis compras.
    fun estadoTexto(venta: Venta): String =
        if (venta.pago == true || venta.estado == "completada") "Pagada" else "Pendiente"

    // Número de unidades compradas en una venta.
    fun unidades(venta: Venta): Int = venta.productos.orEmpty().sumOf { it.cantidad ?: 0 }
}
