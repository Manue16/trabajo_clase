package com.example.mjbebikes

import android.app.Application
import com.example.mjbebikes.data.api.ApiClient

// Se ejecuta una sola vez, antes que cualquier pantalla.
// Deja lista la conexión con la API para toda la app.
// Se registra en AndroidManifest.xml con android:name=".MjbeApp".
class MjbeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.iniciar(this)
    }
}
