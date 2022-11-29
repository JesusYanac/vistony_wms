package com.vistony.wms.enum_

enum class TypeReadSKU {
    CAMERA,
    KEYBOARD,
    HANDHELD,
    CERRAR_ORIGEN,
    CERRAR_FICHA
}


enum class CallFor {
    Article,
    Location
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