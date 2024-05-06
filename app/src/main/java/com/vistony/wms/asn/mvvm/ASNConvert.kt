package com.vistony.wms.asn.mvvm

import android.util.Log
import com.vistony.wms.util.converter.getDateCurrent

fun getDataBarCode(cadena: String): Map<String, String> {
    // Definir el patrón de la expresión regular
    val patron = Regex("\\((\\d{2})\\)(\\d+)")

    // Buscar todas las coincidencias en la cadena
    val coincidencias = patron.findAll(cadena)

    // Crear un mapa para almacenar los datos
    val datos = mutableMapOf<String, String>()

    // Iterar sobre las coincidencias y almacenar los datos en el mapa
    coincidencias.forEach {
        val (marcador, valor) = it.destructured
        datos[marcador] = valor
    }
    return datos
}


fun dataPreASN():List<PreASN>{
    return listOf(PreASN(itemCode = "1000017",itemName = "MUTURROL DE 55 GAL", plannedQty = "20", manufacturingNumber = "9999999", cmpltQty = "22", batch = "240002739", ugpCode = "CIL", aSNnumber = "0", docNum = "240002740", date = "2025-03-01", qtyPallet = "4"))
}

fun countElements(texto: String): Int {
    var count = 0
    var dentroParentesis = false
    for (caracter in texto) {
        if (caracter == '(' && !dentroParentesis) {
            dentroParentesis = true
        } else if (caracter == ')' && dentroParentesis) {
            dentroParentesis = false
            count++
        } else if (!dentroParentesis && caracter != '(' && caracter != ')') {
            if (count == 0 || texto[count - 1] == ')') {
                count++
            }
        }
    }
    // Si el texto termina sin cerrar el paréntesis, se cuenta como un elemento más
    if (dentroParentesis) {
        count++
    }
    return count
}

fun convertPreASNEntityToASNEntity(preASN: PreASN): List<ASN> {
    val asnEntities = mutableListOf<ASN>()
    val asnEntity = ASN(
        docNum = "0",
        docEntry = "0",
        U_Ref2 = preASN.docNum,
        DateExpected = getDateCurrent().toString(),
        U_WhsCode = "AN001",
        U_ItemCode = preASN.itemCode,
        U_ItemName = preASN.itemName,
        batch = preASN.batch,
        dueDate = preASN.date,
    )
    asnEntities.add(asnEntity)
    return asnEntities
}

fun ConvertaddDetailLpnCode(aSNEntity: ASNEntity,lpnCode: String): List<ASN> {
    //val asnEntities = mutableListOf<ASN>()
    val quantityDetail = aSNEntity.data.last().detail.size + 1
    val detail = ASNDetail(id = quantityDetail.toString(), U_LpnCode = lpnCode, U_ItemCode = aSNEntity.data.last().U_ItemCode, U_FifoDate =aSNEntity.data.last().DateExpected, U_Batch = aSNEntity.data.last().batch, U_ExpirationDate = aSNEntity.data.last().dueDate)
    val currentASN = aSNEntity
    var listASN:List<ASN> = emptyList()
    val updatedDetails = currentASN.data.last().detail.toMutableList().apply {
        add(detail)
    }
    val updatedASN = currentASN.data.last().copy(detail = updatedDetails)
    listASN+=updatedASN
    return listASN
}

fun ConvertUpdateResultQuantityDetail(aSNEntity: ASNEntity,index: Int,quantityReport: String): List<ASN> {
    var result = aSNEntity.data
    for (i in 0 until result.size) {
        for(j in 0  until  result[i].detail.size)
        {
            if (j == index)
            {
                result[i].detail[j].U_Quantity = quantityReport
            }
        }
    }
    return result
}

fun ConvertDeleteLPN(aSNEntity: ASNEntity,indexDelete: Int): List<ASN> {
    val currentASN = aSNEntity
    var listASN:List<ASN> = emptyList()
    val updatedDetails = currentASN.data.last().detail.toMutableList().apply {
        removeAt(indexDelete)
    }
    val updatedASN = currentASN.data.last().copy(detail = updatedDetails)
    listASN+=updatedASN
    for (i in 0 until listASN.size) {
        for (j in 0  until  listASN[i].detail.size){
            listASN[i].detail[j].id = (j+1).toString()
        }
    }
    return listASN
}

fun ConvertValidateStatusASN(aSNEntity: ASNEntity): Boolean {
    var status = false
    for(i in 0 until aSNEntity.data.size) {
        if (aSNEntity.data[i].detail.size > 0) {
            status = true
        }
    }
    return status
}

fun ConvertValidateStatusPrintAssigned(aSNEntity: ASNEntity): Boolean {
    var status = false
        for(i in 0 until aSNEntity.data.size) {
            if (!aSNEntity.ipAddress.isNullOrEmpty()) {
                status = true
            }
        }
    return status
}

fun ConvertValidateStatusHeadASN(aSNEntity: ASNEntity): Boolean {
    var status = false
    if (aSNEntity.data.size > 0) {
        status = true
    }
    return status
}

