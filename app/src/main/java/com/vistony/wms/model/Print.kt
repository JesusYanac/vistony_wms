package com.vistony.wms.model

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.bson.types.ObjectId
import java.util.*

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

data class PrintSSCC(
    @SerializedName("AbsEntry")
    var AbsEntry : Int = 0,
    @SerializedName("BinCode")
    var BinCode : String = "",
    @SerializedName("Warehouse")
    var Warehouse : String = "",
    @SerializedName("ItemCode")
    var ItemCode: String = "",
    @SerializedName("Batch")
    var Batch: String = "",
    @SerializedName("PrinterIP")
    var PrinterIP: String = "",
    @SerializedName("PortNum")
    var PortNum: Int = 0,
    @SerializedName("Transfer")
    var Transfer: String = "N"
)

data class Print(
    @SerializedName("ItemCode")
    var itemCode: String = "",
    @SerializedName("Batch")
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

    var warehouse: String = "",
    var binCode: String = "",
    var absEntry: Int = 0,

    var ipAddress: String = "",
    var portNumber: Int = 0,
    var flagTransfer: String = "N",
)


data class PrintMachines(
    var name: String = "",
    var ip: String = "",
    var port: String = ""
)

open class Printer(
    var Batch: String = "",
    var BinCode: String = "",
    var CreateAt: Date = Date(),
    var ItemCode: String = "",
    var PrinterIp: String = "",
    var Realm_Id: String = "",
    var Response: String = "",
    var Transfer: String = ""
): RealmObject() {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
}