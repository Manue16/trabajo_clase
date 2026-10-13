# Comparación: Requerimientos funcionales vs. proyecto MJBE BIKES

Documento revisado: `guia-Requerimientos_funcionales_MJBE_BIKES.docx` (RF01–RF12, RNF01–RNF18).
Proyecto revisado: `MJBE_Bikes.zip` → `backend/` (Node + Express + MySQL) y `MJBE_bikes_frontend/` (React + Vite + Bootstrap).

Leyenda:
- ✅ **Coincide**: lo documentado está en el proyecto.
- 🟡 **Parcial**: existe, pero la documentación describe más o algo distinto.
- ❌ **No está en el proyecto**: hay que eliminarlo de la documentación (o implementarlo).
- ➕ **Falta en la documentación**: el proyecto lo hace y el documento no lo menciona.

---

## 1. Resumen rápido

| RF | Nombre en el documento | Estado | Qué hacer en la documentación |
|----|------------------------|--------|-------------------------------|
| RF01 | Consultar cliente | 🟡 Parcial | Ajustar entradas (quitar Email, agregar tipo de documento), agregar *Eliminar cliente* y *Administrador* como actor |
| RF02 | Registro pedido (caja) | 🟡 Parcial | Renombrar a "Registro de venta presencial", quitar *Hora pedido*, agregar *Cantidad* y *Fecha* |
| RF03 | Forma de pago (caja) | ❌ Casi no existe | Reducir a "estado de pago (pagada/pendiente)" o quitar *plataformas, crédito, débito y confirmación del banco* |
| RF04 | Confirmación de venta | 🟡 Parcial | Mantener *Actualiza inventario*; quitar *Confirmación banco*; cambiar salida *Factura* por *Detalle de venta* |
| RF05 | Registro de cliente | 🟡 Parcial | Entradas reales: usuario, correo, contraseña y confirmar contraseña; quitar *Fecha de nacimiento* |
| RF06 | Registro de pedidos cliente (web) | ✅ Coincide | Agregar *cambiar cantidad*, *filtro por marca*, *Mis compras*; corregir entradas y salidas |
| RF07 | Forma de pago (cliente) | ❌ Duplicado / simulado | Unir con RF03 en un solo RF; dejar solo lo que hay (pago simulado: tarjeta o transferencia) |
| RF08 | Administración (usuarios) | ✅ Coincide | Agregar *Desactivar usuario*; "permisos" = asignar rol; corregir entradas |
| RF09 | Control de inventario | ✅ Coincide | Agregar los campos reales del producto, *Desactivar producto*, *Imprimir*; actor *Administrador* |
| RF10 | Faltantes | 🟡 Parcial | No hay "registro de faltantes": es una alerta automática de stock bajo o agotado. Actor: Bodega/Admin, no "Compras" |
| RF11 | Módulo de compras | ✅ Coincide | Agregar proveedor, estado de pago, detalle y suma de stock; quitar *ID local* y *Factura de compra*; actor: Administrador |
| RF12 | Módulo de proveedores | ✅ Coincide | Agregar *Eliminar proveedor*, *Imprimir*; cambiar *Tipo de proveedor* por *Tipo de documento*; actor: Administrador |

Funcionalidades del proyecto que **no están documentadas** (➕): inicio de sesión con redirección por rol, panel de control del administrador, panel de bodega, panel del vendedor, consulta y detalle de ventas, historial "Mis compras" y catálogo público con búsqueda. En la sección 3 se proponen como RF13–RF18.

---

## 2. Detalle por requerimiento

### RF01 – Consultar cliente (Caja)
**En el proyecto:** `VerClientes.jsx`, `CrearClientes.jsx`, `ActualizarClientes.jsx` + `backend/routes/clientes.js`.

- ✅ Registrar cliente, editar cliente y listar clientes.
- ✅ "Reporte del cliente": el listado tiene botón **Imprimir**.
- ➕ **Eliminar cliente**: existe (botón eliminar → `DELETE /api/clientes/:id`) y no está documentado.
- ➕ **Actor Administrador**: la ruta permite rol 1 (Administrador) y rol 2 (Vendedor/Caja).
- ➕ Cada cliente **debe tener un usuario asociado** (el formulario pide "Usuario asociado"). Conviene mencionarlo.
- ➕ No se puede repetir el documento (tipo + número).
- 🟡 La "consulta" es un **listado**; la pantalla no tiene buscador. El backend sí acepta `?search=`. O se documenta como "listado de clientes" o se agrega un buscador en la pantalla.

**Entradas:**
| Documento actual | Proyecto real | Acción |
|---|---|---|
| Documento | Tipo de documento + Número de documento | Agregar *Tipo de documento* |
| Nombre, Apellido | Nombres, Apellidos | ✅ |
| Email | — (la tabla `clientes` no tiene email; el email está en el usuario) | ❌ Eliminar |
| Teléfono, Dirección | Teléfono, Dirección | ✅ |
| ID usuario | Usuario asociado | ✅ |

**Salidas:** "Registro de Cliente, Consulta del Cliente, Reporte del Cliente" → ✅ (el reporte es el listado imprimible).

---

### RF02 – Registro pedido (Caja)
**En el proyecto:** `CrearVenta.jsx` (rol Vendedor).

- ✅ Registrar, **agregar producto** y **quitar producto**.
- ➕ Se elige **cantidad** por producto (limitada al stock) y el **total se calcula automáticamente**.
- ➕ Solo aparecen productos **activos y con stock > 0**.
- 🟡 En el sistema no hay una entidad "pedido": lo que se registra es una **venta** (tabla `ventas` + `detalles_venta`). Se recomienda renombrar el RF a **"Registro de venta presencial"**.

**Entradas:**
| Documento actual | Proyecto real | Acción |
|---|---|---|
| Documento / Nombre cliente | Cliente (se selecciona de la lista) | Cambiar a "Cliente" |
| ID usuario | Vendedor (usuario en sesión) | ✅ |
| ID producto | Producto | ✅ |
| Fecha pedido | Fecha | ✅ |
| Hora pedido | — | ❌ Eliminar |
| Agregar / Eliminar productos | Agregar / Quitar producto + **Cantidad** | Agregar *Cantidad* |

**Salida:** "Lista de pedido" → cambiar por "Venta registrada (con su detalle y total)".

---

### RF03 – Forma de pago (Caja y Cliente) ❌
**En el proyecto:**
- La venta solo guarda un campo `pago` (verdadero/falso), que se muestra como **"Pagada" / "Pendiente"**.
- En caja (`CrearVenta.jsx`) el método va fijo como `"efectivo"` y **no se guarda** en la base de datos.
- **No existen** pagos por plataformas (Nequi, Daviplata, Bancolombia), tarjeta de crédito a cuotas, tarjeta débito ni **confirmación del banco**.

**Qué hacer:** eliminar 1.2, 1.3, 1.4 y "2. Confirmación banco". Quitar también las salidas "Confirmación de banco" y "Reporte de pago". Si se quiere conservar el RF, redactarlo así: *"El sistema registra si la venta está pagada o pendiente; en la tienda web el método de pago es simulado (tarjeta o transferencia) y no se hace ningún cobro real."*
Otra opción es dejarlo como **alcance futuro**, indicándolo así en el documento.

---

### RF04 – Confirmación de venta
**En el proyecto:** `backend/routes/detallesVenta.js`.

- ✅ **Actualiza inventario**: cada producto vendido **descuenta el stock** dentro de una transacción. La operación se rechaza si no hay stock suficiente ("Stock insuficiente…").
- ➕ Al **editar o eliminar** un detalle de venta, el stock se **repone** automáticamente.
- ❌ "Confirmación de venta: esperar la confirmación del banco" → no existe. Eliminar.
- 🟡 Salida **"Factura"**: no se genera factura. Lo que existe es la pantalla **Detalle de venta** (`DetallesVenta.jsx`): cliente, fecha, total y una tabla con producto, cantidad, precio unitario y subtotal. Cambiar a "Detalle de venta / comprobante", o implementar la impresión de la factura.
- Entradas: quitar *Confirmación banco*, *Monto del pago* e *ID factura*. Dejar *ID venta, productos, cantidades, ID usuario*.
- Corregir el error de escritura: en "Requerimiento Funcional" dice **"RN03"** y debería decir **RF03**.

---

### RF05 – Registro de cliente (web)
**En el proyecto:** `Register.jsx` y `Login.jsx`.

- ✅ **Registrarse** e **Iniciar sesión**.
- El formulario de registro pide: **Usuario (login), Correo electrónico, Contraseña y Confirmar contraseña**. El rol asignado es *Cliente*.
- Los datos personales (nombre, teléfono, dirección) se piden después, **en el checkout**, la primera vez que el cliente compra (`CompraCliente.jsx` crea el registro en `clientes`).

**Entradas:**
| Documento actual | Proyecto real | Acción |
|---|---|---|
| Documento | — (en el checkout se genera automáticamente como `USR-<id>`) | ❌ Eliminar o aclarar |
| Nombres y Apellidos | Se piden en el checkout ("Nombre completo") | Mover a RF06 o aclarar |
| Fecha de nacimiento | — | ❌ Eliminar |
| Dirección | Se pide en el checkout | Mover a RF06 o aclarar |
| Contraseña | Contraseña + Confirmar contraseña | ✅ |
| — | **Usuario (login)** y **Correo electrónico** | ➕ Agregar |

---

### RF06 – Registro de pedidos cliente (web) ✅
**En el proyecto:** `Inicio.jsx`, `ProductosCliente.jsx`, `ProductoDetalle.jsx`, `Carrito.jsx`, `NavarCliente.jsx`, `CompraCliente.jsx`.

- ✅ Búsqueda de producto, agregar producto, quitar producto y carrito de compras.
- ➕ **Filtro por marca** y botón "Limpiar filtros".
- ➕ **Ver detalle del producto**.
- ➕ En el carrito: **aumentar o disminuir la cantidad**, ver la cantidad total y el precio total.
- ➕ **Finalizar compra (checkout)**: solo para usuarios con rol Cliente que hayan iniciado sesión.
- ➕ **Mis compras**: historial de compras del cliente (`MisCompras.jsx`). Ver RF17 propuesto.

**Entradas reales del checkout:** Nombre completo, Correo, Teléfono, Dirección de entrega, Método de pago (simulado), Productos del carrito (con cantidad).
→ Quitar "Documento" e "ID productos / Nom. productos" como entradas que escribe el usuario (se toman del carrito).

**Salidas:** "Reporte de búsqueda de usuario" no corresponde → cambiar por "Listado de productos filtrado" y "Compra registrada (mensaje de confirmación)".

---

### RF07 – Forma de pago (Cliente) ❌ duplicado
- Es casi igual a RF03 (la numeración también está mal: "1.1, 1.., 1.4").
- En la web solo existe el **"Método de pago simulado"**: *Tarjeta de crédito o débito* o *Transferencia bancaria*. La pantalla dice "No se realizará ningún cobro real". Ese dato **no se guarda** en la base de datos.
- **Qué hacer:** eliminar RF07 y dejar una sola "Forma de pago" (RF03) con actores Caja y Cliente, describiendo lo que realmente hay.

---

### RF08 – Administración (usuarios) ✅
**En el proyecto:** `VerUsuarios.jsx`, `CrearEmpleados.jsx`, `ActualizarEmpleados.jsx` + `backend/routes/usuarios.js`.

- ✅ Consultar usuario: listado con filtro por usuario, email, estado o rol, y botón **Imprimir**.
- ✅ Agregar, editar y eliminar usuario.
- ✅ "Agrega permisos": en el sistema esto es **asignar el rol** (Administrador, Empleado, Bodeguero, Cliente). Conviene redactarlo como "Asignar rol" para que coincida.
- ➕ **Desactivar usuario** (borrado lógico: cambia el estado del usuario) además de la eliminación definitiva.
- ➕ No permite repetir login ni correo.

**Entradas:**
| Documento actual | Proyecto real | Acción |
|---|---|---|
| Documento | — (los usuarios no tienen documento) | ❌ Eliminar |
| Nombre | Usuario (login) | Cambiar |
| Tipo de permiso | Rol | Cambiar |
| — | Correo, Contraseña, Estado | ➕ Agregar |

En la descripción dice "usuarios administrativos (bodega, caja)", pero desde aquí también se crean administradores y clientes.

---

### RF09 – Control de inventario ✅
**En el proyecto:** `vistaBodega/*` y `vistaAdmi/gestionDeProducto/*` + `backend/routes/productos.js`.

- ✅ Crear, consultar, editar y eliminar producto, y actualizar inventario.
- ✅ "Consulta proveedores": en el formulario de producto se elige el proveedor de una lista. Ojo: Bodega **no** tiene acceso a la pantalla de proveedores.
- ➕ **Desactivar producto** (estado *inactivo*) además de eliminarlo.
- ➕ Filtros por producto, modelo, marca, color, cantidad (de mayor a menor o al revés) y estado. Botón **Imprimir**.
- ➕ El stock también cambia **automáticamente** con las ventas (resta) y las compras (suma).
- ➕ **Actor Administrador**: también gestiona productos, no solo Bodega.

**Entradas** (agregar las que faltan): Imagen (URL), Nombre, Descripción, Color, Marca, Modelo, Cantidad, Valor unitario, Talla/medida, Proveedor, Local, Estado (activo/inactivo).

---

### RF10 – Módulo de faltantes 🟡
**En el proyecto:** `InicioBodega.jsx` y `InicioAdmin.jsx` (con `GET /api/reportes/dashboard`).

- No hay un "registro de faltantes" (no existe una tabla ni un formulario para eso).
- Lo que existe es una **alerta automática**: productos con **stock bajo (≤ 5 unidades)** y **sin stock (0)**, que se muestran en el panel de Bodega (con las 3 primeras alertas) y en el del Administrador (conteo). También se puede ordenar el inventario por cantidad e imprimirlo.
- **Qué hacer:**
  - Cambiar la descripción a *"El sistema identifica automáticamente los productos con stock bajo (≤5) o agotados y los muestra como alertas"*.
  - Quitar las entradas *ID local, Cantidad* (se calculan solas).
  - Cambiar el actor **"compras" → Bodega y Administrador**.

---

### RF11 – Módulo de compras ✅
**En el proyecto:** `VerCompra.jsx`, `CrearCompra.jsx`, `DetallesCompra.jsx` + `backend/routes/compras.js` y `detallesCompra.js`.

- ✅ Registro de compras.
- ➕ **Consultar compras** (con buscador) y **ver el detalle de una compra**.
- ➕ Cada producto comprado **suma stock** automáticamente (re-stock). Si se edita o elimina el detalle, se revierte.
- ➕ El total se calcula a partir de cantidad × precio unitario.

**Entradas:**
| Documento actual | Proyecto real | Acción |
|---|---|---|
| ID local | — | ❌ Eliminar |
| ID productos | Productos + Cantidad + Precio unitario | Agregar cantidad y precio |
| Factura de compra | — | ❌ Eliminar |
| — | **Proveedor**, **Fecha**, **Estado de pago** | ➕ Agregar |

**Actor:** "compras" → **Administrador** (solo el rol 1 entra a `/compras`).

---

### RF12 – Módulo de proveedores ✅
**En el proyecto:** `VerProveedores.jsx`, `CrearProveedores.jsx`, `ActualizarProveedores.jsx` + `backend/routes/proveedores.js`.

- ✅ Consultar, agregar, editar e **inactivar** proveedor.
- ➕ **Eliminar proveedor** (definitivo).
- ➕ Filtro por nombre, número de identidad, teléfono o estado, y botón **Imprimir**.
- ➕ No permite repetir la identidad (tipo de documento + número).

**Entradas:**
| Documento actual | Proyecto real | Acción |
|---|---|---|
| ID proveedor | ID | ✅ |
| ID N. activo | Estado (activo/inactivo) | Cambiar nombre |
| Nom. Prove. | Nombre del proveedor | ✅ |
| Tipo de proveedor | — | ❌ Eliminar (cambiar por *Tipo de documento*) |
| — | Tipo de documento, Número de identidad, Teléfono, Dirección | ➕ Agregar |

**Actor:** "compras" → **Administrador**.

---

## 3. Funcionalidades del proyecto que faltan en la documentación (RF nuevos propuestos)

| Nuevo RF | Nombre | Dónde está en el proyecto | Actor |
|---|---|---|---|
| RF13 | **Inicio de sesión y control de acceso por rol** | `Login.jsx`, `auth.js`, `App.jsx` (las rutas se protegen por rol y, al iniciar sesión, cada rol va a su propio inicio; cerrar sesión) | Todos |
| RF14 | **Panel de control del administrador (reportes)** | `InicioAdmin.jsx` + `GET /api/reportes/dashboard`: total de productos, clientes y ventas; ventas de hoy y del mes; stock bajo; ingresos del mes; últimas 5 ventas | Administrador |
| RF15 | **Panel del vendedor** | `InicioVentas.jsx`: ventas realizadas, ventas completadas, ingresos generados, ventas de hoy y ventas recientes | Caja/Vendedor |
| RF16 | **Consulta de ventas y detalle de venta** | `VerVenta.jsx` (vendedor, con buscador), `VentasAdmin.jsx` (todas las ventas, con estado Pagada/Pendiente), `DetallesVenta.jsx` | Administrador, Caja |
| RF17 | **Historial "Mis compras"** | `MisCompras.jsx`: lista de compras del cliente con fecha, total y estado | Cliente |
| RF18 | **Catálogo público de productos** | `Inicio.jsx`, `ProductosCliente.jsx`, `ProductoDetalle.jsx` (se ve sin iniciar sesión) | Visitante / Cliente |
| (opcional) | Panel de bodega | `InicioBodega.jsx`: total de productos, productos activos, stock bajo, sin stock, movimientos recientes y alertas de inventario | Bodega |

**Solo en el backend (todavía sin pantalla):** gestión de **locales**, **tallas**, **medidas**, **tipos de documento**, **roles** y **roles secundarios** (`/api/locales`, `/api/tallas`, `/api/medidas`, `/api/tipos_documentos`, `/api/roles`, `/api/roles_secundarios`). Se pueden documentar como "tablas maestras administradas por API" o dejarlas fuera hasta que tengan interfaz.

---

## 4. Otras correcciones al documento

1. **Tipos de usuario:** el documento tiene 5 (Administrador, Caja, Bodega, **Compras**, Cliente). El sistema tiene **4 roles**: 1 Administrador, 2 Empleado/Vendedor (= Caja), 3 Bodeguero, 4 Cliente. **Eliminar "Compras"** y pasar sus actividades (compras y proveedores) al **Administrador**. Si se mantiene, hay que crear ese rol en el sistema.
2. **Restricciones → tecnologías:** dice "HTML, CSS, react, **angular**, javascript, node.js, github, **java**". El proyecto **no usa Angular ni Java**. Usa React 19 + Vite + Bootstrap 5 (frontend), Node.js + Express 5 (backend), **MySQL** (base de datos) y Jest (pruebas). Corregir la lista.
3. **"Funcionalidad del producto"** (sección 2.2) está vacía: llenarla con la lista de módulos (Clientes, Ventas, Tienda web/Carrito, Usuarios, Inventario, Compras, Proveedores, Reportes).
4. **Índice:** dice "Requisito funcional 1…9", pero el documento tiene RF01–RF12 (y tendría más con los nuevos). Actualizar la tabla de contenido.
5. **Plantilla sin completar:** quitar el bloque "Instrucciones para el uso de este formato" y actualizar la fecha "**Abril del 2012**" y la ficha del documento ("12/04/2012 – Carlos Herrera").
6. **Errores de escritura:** "Confinación" → **Confirmación** (RF03, RF04, RF07); "RN03" → **RF03** (RF04); "ususario", "invenario", "ciente", "dados"; correo "brayandiaz19242007@**gmil**.com".
7. **Personal involucrado:** `backend/package.json` tiene como autores a *Jharol Alfonso Meza Tique, Manuel Felipe Orduz Pérez y Jhair Julian Sarmiento Ramos*. El documento tiene a *Manuel, Brayan, Eduard y Jhair*. Verificar que coincidan.
8. **Interfaces de comunicación:** menciona FTP para transferir archivos; el sistema se comunica por **HTTP/REST (JSON)** entre React y Express. Corregir.
9. **Requisitos funcionales (texto, sección final):** la numeración está desordenada (RF05 aparece como "3.", RF06 como "4.", etc.). Rehacerla cuando se actualicen las tablas.

---

## 5. Requerimientos no funcionales: estado rápido

| RNF | Requisito | Estado en el proyecto |
|---|---|---|
| RNF01 | Interfaz intuitiva y responsiva | ✅ Bootstrap 5 responsivo |
| RNF02 | Log de intentos fallidos de inicio de sesión | ❌ No existe (`/api/sessions` guarda sesiones en memoria y el frontend no lo usa) |
| RNF03 | Acceso por HTTPS | ❌ Todo es `http://localhost` (aplica al desplegar) |
| RNF04 | Correo de confirmación al usuario nuevo | ❌ No existe (la columna `token_activacion` no se usa) |
| RNF05 | Restablecer contraseña por correo o SMS | ❌ No existe (las columnas `reset_key`/`reset_base` no se usan) |
| RNF06 | Contraseñas cifradas | 🟡 Se aplica un **hash SHA-256 en el navegador**, sin *salt*. Además, `GET /api/usuarios` devuelve `password_harsh`. Recomendado: `bcrypt` en el backend y no devolver el hash. En el documento decir "hash", no "encriptación" |
| RNF07 | Control de acceso por roles | 🟡 Solo en el frontend (las rutas se protegen con `localStorage`). La API no valida rol ni sesión |
| RNF08–RNF12, RNF17 | Rendimiento, concurrencia, disponibilidad, escalabilidad | Sin evidencia (no hay pruebas de carga ni despliegue) |
| RNF13 | Backup diario de la base de datos | ❌ No existe |
| RNF14 | Manual de usuario | ❌ No está en el proyecto |
| RNF15 | Arquitectura modular | ✅ Una ruta por módulo en el backend, una vista por módulo en el frontend |
| RNF16 | Chrome, Firefox, Edge | ✅ Probable (React + Vite) |
| RNF18 | Pruebas unitarias ≥ 70 % | ❌ Jest y Supertest están configurados, pero `routes/__tests__/` está **vacío** |

> Los RNF que no se cumplen deben **implementarse** o pasarse a "alcance futuro" en el documento, para no prometer algo que el sistema no hace.

---

## 6. Problemas del código que impiden que algunos RF funcionen

Estos no son de la documentación, pero conviene revisarlos antes de la entrega, porque afectan RF02, RF05, RF06, RF09 y RF17:

1. **Login y registro (RF05, RF13):** el frontend llama a `http://localhost:8000/api/login` y `/api/register`, pero el backend Express del zip **no tiene esas rutas** y corre en el puerto **5000** (`index.js`, `PORT || 5000`). Varios comentarios del frontend mencionan **Laravel** (`VentaController::store`). Si usan otro backend para eso, hay que documentarlo; si no, falta implementarlo.
2. **Crear venta (RF02, RF06):** `CrearVenta.jsx` y `CompraCliente.jsx` envían `{ user_id, cliente, productos: [...] }`, pero `POST /api/ventas` exige `id_vendedor` e `id_cliente` y no crea los detalles. Con el backend Express responde **400**. Habría que enviar `id_vendedor`/`id_cliente` y luego llamar a `POST /api/detalles_venta` por cada producto, como ya hace `CrearCompra.jsx`.
3. **Crear producto (RF09):** el botón lleva a `/CrearProducto`, pero esa ruta **no está en `App.jsx`**: el componente existe y no está enlazado, así que la página redirige al inicio.
4. **`/Historialventas`** (botones "Volver" y "Ver historial" de ventas) tampoco existe en `App.jsx`.
5. **Mis compras (RF17):** `MisCompras.jsx` consulta `?user_id=`, que el backend interpreta como **vendedor**. Para el cliente debe ser `?comprador=<id>`.
6. **Desactivar producto:** `VerProductos.jsx` espera `{ producto }` en la respuesta, pero `PUT /api/productos/:id` devuelve el producto directamente.
