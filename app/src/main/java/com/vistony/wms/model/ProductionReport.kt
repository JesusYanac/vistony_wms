package com.vistony.wms.model

import androidx.annotation.NonNull
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.bson.types.ObjectId
import java.util.Date

data class TerminationReport(
    @NonNull @SerializedName("Data")
    val Data: String="",
    val Status: String=""
)

data class sendTerminationReport(
    var ItemCode: String,
    var Batch: String,
    var Quantity: Int
)

open class ProductionReceipt(
    var Batch: String = "",
    var CodeSAP: Int = 0,
    var DateDocument: Date = Date(),
    var ItemCode: String = "",
    var NumSSCC: Int = 0,
    var PrinterIp: String = "",
    var PrinterName: String = "",
    var PrinterPort: String = "",
    var ProductionLine: String = "",
    var ProductionOrder: Int = 0,
    var Quantity: Double = 0.0,
    var Realm_Id: String = "",
    var Response: String = "",
    var Status: String = ""
): RealmObject() {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
}