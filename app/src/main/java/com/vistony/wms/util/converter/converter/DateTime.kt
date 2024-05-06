package com.vistony.wms.util.converter

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun getDateCurrent(): String? {
    val format = "yyyyMMdd"
    val formatter = DateTimeFormatter.ofPattern(format)
    val now = LocalDateTime.now()
    return formatter.format(now)
}