package com.vistony.wms.util

object DatasourceSingleton {
    var apiUrl: String = "http://192.168.254.20:8082/pe/" // URL por defecto para MongoDB
    var port: Int = 8082  // Puerto por defecto para MongoDB
    //var databaseKey: String = "appwms-bckdu" // Clave por defecto para MongoDB
    var databaseKey: String = "appwms-bckdu" // Clave por defecto para MongoDB

        set(value) {
            field = value
            // Puedes agregar lógica adicional aquí, como notificar a las instancias existentes de APIService sobre el cambio.
        }

    fun updateApiUrl(newUrl: String) {
        apiUrl = newUrl
    }

    fun updatePort(newPort: Int) {
        port = newPort
    }

    fun updateDatabaseKey(newKey: String) {
        databaseKey = newKey
    }

    // Puedes agregar métodos adicionales según sea necesario
}
