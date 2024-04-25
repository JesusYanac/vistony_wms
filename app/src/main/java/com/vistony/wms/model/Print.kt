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
    var Transfer: String = "N",
    @SerializedName("Quantity")
    var QuantityPallet: Double = 0.0
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
    @SerializedName("Quantity")
    var quantityPallet: Double = 0.00,
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

data class LineaItem(
    var itemName: String = "",
    var ssccName: String = "",
    var itemCode: String = "",
    var numero: Int = 0,
    var lote: String = "",
    var fecha: String = "",
    var unidadMedida: String = "",
    var barCode: String = "",
    var fv: String = ""
)

data class PrintData(
    var ipAddress: String = "",
    var portNumber: Int = 0,
    var flag: String = "",
    var lineaData: List<LineaItem> = emptyList()
)

data class ItemDataPrint(
    var ItemCode: String = "",
    var Tvida: Int = 0,
    var QtyUnit: Int = 0, // Asumo que QtyUnit es la cantidad en unidades
    var QtyPallet: Int = 0, // Asumo que QtyPallet es la cantidad en pallets
    var ItemName: String = "",
    var ItemUnit: String = "",
    var BarCode: String = "",
    var BarCodeUnit: String = "",
    var UM: String = "",
    var Fecha: String = ""
)

data class MyDataPrint(
    var Data: List<ItemDataPrint> = emptyList()
)

data class MyData(
    var Data: String = ""
)
