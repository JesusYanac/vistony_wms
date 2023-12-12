package com.vistony.wms.num


enum class TypeReadSKU {
    CAMERA,
    KEYBOARD,
    HANDHELD,
    CERRAR_ORIGEN,
    CERRAR_FICHA,
    REENVIAR_FICHA,
    CANCELAR_FICHA
}

enum class TypeCode {
    SSCC,
    QR
}

open class OptionsInventory(
    var type: TypeReadSKU = TypeReadSKU.KEYBOARD,
    var text: String = "Manual",
    var icon: Int = 0
)

open class OptionsDowns(
    var text: String = "",
    var icon: Int = 0
)