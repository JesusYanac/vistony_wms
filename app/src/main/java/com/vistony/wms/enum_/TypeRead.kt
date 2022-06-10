package com.vistony.wms.enum_

enum class TypeReadSKU {
    CAMERA,
    KEYBOARD,
    HANDHELD
}

open class OptionsInventory(
    var type: TypeReadSKU = TypeReadSKU.KEYBOARD,
    var text: String = "Manual",
    var icon: Int = 0
)