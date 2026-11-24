# Persona 1 – Fase 1: base de conexión con la API

Cliente HTTP y modelos de datos para la app móvil de MJBE Bikes (Kotlin + Views),
conectada a la API Laravel. Corresponde a los issues 1 y 2 de la persona 1.

## Archivos

```
app/src/main/java/com/example/mjbebikes
├── MjbeApp.kt                  ← inicia la conexión al abrir la app
└── data
    ├── api
    │   ├── ApiClient.kt        ← Retrofit + OkHttp (URL base, JSON, logs)
    │   ├── SesionCookieJar.kt  ← guarda la cookie mjbe_token (JWT) del login
    │   └── InventarioApi.kt    ← endpoints de la persona 1
    └── model
        ├── Producto.kt         ← Producto, ProductoRespuesta
        ├── Cliente.kt          ← Cliente, ClienteRespuesta, TipoDocumento, UsuarioResumen
        ├── Proveedor.kt        ← Proveedor, ProveedorRespuesta
        ├── Compra.kt           ← Compra, CompraNueva, DetalleCompra, DetalleCompraNuevo
        └── Catalogos.kt        ← Medida, Talla, Local, Mensaje
```

Si el paquete de tu proyecto no es `com.example.mjbebikes`, cambia la primera línea
(`package ...`) y los `import` de cada archivo por el tuyo.

## 1. Dependencias (`app/build.gradle.kts`)

Dentro de `dependencies { ... }`, además de las de la guía, agrega y pulsa **Sync Now**:

```kotlin
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
```

## 2. `AndroidManifest.xml`

Encima de `<application>`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

Y en la etiqueta `<application ...>` agrega estos dos atributos:

```xml
android:name=".MjbeApp"
android:usesCleartextTraffic="true"
```

`usesCleartextTraffic` es necesario porque la API va por `http` (sin `s`).

## 3. Arrancar la API

En la carpeta de la API Laravel:

```
php artisan serve --host=0.0.0.0 --port=8000
```

- Emulador: la app usa `http://10.0.2.2:8000/api/` (ya configurado en `ApiClient.kt`).
- Celular real: cambia `BASE_URL` por la IP de tu PC en el wifi (`ipconfig` en el cmd).

## 4. Probar la conexión

`GET /productos` es pública, así que se puede probar sin login. Pega esto
temporalmente al final de `onCreate` de cualquier pantalla:

```kotlin
lifecycleScope.launch {
    try {
        val respuesta = ApiClient.inventario.productos()
        Log.d("PRUEBA_API", "Código ${respuesta.code()} – ${respuesta.body()?.size} productos")
    } catch (e: Exception) {
        Log.e("PRUEBA_API", "No se pudo conectar: ${e.message}")
    }
}
```

Imports: `androidx.lifecycle.lifecycleScope`, `kotlinx.coroutines.launch`, `android.util.Log`
y `com.example.mjbebikes.data.api.ApiClient`.

En **Logcat** filtra por `PRUEBA_API`: debe decir `Código 200 – N productos`.
Si sale `No se pudo conectar`, la API no está encendida o la URL no es la correcta.

## Notas para los dos

- Casi todas las rutas exigen sesión: sin login responden **401**. El login lo hace la
  persona 2 con `POST /login`; la cookie queda guardada sola en `SesionCookieJar`, sin
  código extra. Hasta entonces, la persona 1 puede trabajar el catálogo (rutas públicas).
- Para cerrar sesión o ante un 401: `ApiClient.sesion.limpiar()`.
- La persona 2 crea sus propios archivos de endpoints (por ejemplo `AuthApi.kt` y
  `VentasApi.kt`) y los inicia en `ApiClient.iniciar()` con `retrofit.create(...)`,
  igual que `InventarioApi`. Así no editan el mismo archivo.
