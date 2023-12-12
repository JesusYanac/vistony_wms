package com.vistony.wms.util

import java.text.SimpleDateFormat
import java.util.*

fun isNumeric(toCheck: String): Boolean {
    return toCheck.all { char -> char.isDigit() }
}

fun removeLastChar(str: String): String {
    return str.replaceFirst(".$".toRegex(), "")
}

fun parseValue(suggestionValue: String): Pair<String, String> {
    val parts = suggestionValue.split("|")
    val itemCodeNew = parts.getOrElse(0) { "" }
    var lote = parts.getOrElse(1) { "" }
    if(lote.split(" ").isNotEmpty()){
        lote=lote.split(" ")[0]
    }

    return itemCodeNew to lote
}

fun ConvertdatefordateSAP(date: String?): String? {
    var dateFormat = SimpleDateFormat("yyyyMMdd HH:MM", Locale.getDefault())
    var datefecha = Date()
    var fecha = dateFormat.format(date)

    return fecha
}



fun ConvertdatefordateSAP2(date: String?): String? {
    // Verificar si la fecha de entrada es nula o vacía
    if (date.isNullOrEmpty()) {
        return null
    }

    // Formato para parsear la fecha de entrada con información de la zona horaria
    val inputDateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
    val parsedDate: Date
    try {
        parsedDate = inputDateFormat.parse(date)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }

    // Formato para formatear la fecha de salida en el formato deseado
    val outputDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return outputDateFormat.format(parsedDate)
}