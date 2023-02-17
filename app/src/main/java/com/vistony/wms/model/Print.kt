package com.vistony.wms.model

import com.google.gson.annotations.SerializedName

data class ListPrint(
    @SerializedName("prints")
    val prints: List<PrintX> = emptyList(),
    val status: String = ""
)

data class PrintX(
    @SerializedName("name")
    val name: String ="",
    @SerializedName("u_Enabled")
    val uEnabled: String ="",
    @SerializedName("u_IPAdress")
    val uIPAdress: String ="",
    @SerializedName("u_Port")
    val uPort: String=""
)

data class Print(
    @SerializedName("ItemCode")
    var itemCode: String = "",
    @SerializedName("ItemName")
    var itemName: String = "",
    @SerializedName("lote")
    var itemBatch: String = "",
    @SerializedName("fecha")
    var itemDate: String = "",
    @SerializedName("unidadMedida")
    var itemUom: String = "",
    var printer: PrintMachines = PrintMachines(),
    @SerializedName("numero")
    var quantity: Int = 1,
    var quantityString: String = "1",
    var status: String = "",

    var ipAddress: String = "",
    var portNumber: Int = 0,
)

data class PrintMachines(
    var name: String = "",
    var ip: String = "",
    var port: String = ""
)