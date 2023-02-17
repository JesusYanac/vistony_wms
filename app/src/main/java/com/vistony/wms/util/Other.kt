package com.vistony.wms.util

fun isNumeric(toCheck: String): Boolean {
    return toCheck.all { char -> char.isDigit() }
}

fun removeLastChar(str: String): String {
    return str.replaceFirst(".$".toRegex(), "")
}