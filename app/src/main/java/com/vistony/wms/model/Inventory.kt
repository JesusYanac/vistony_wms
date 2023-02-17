package com.vistony.wms.model

import com.vistony.wms.num.TypeCode
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.bson.types.ObjectId
import java.util.*

open class InventoryPayload(
    var inventory:Inventory=Inventory(),
    var defaultLocation:String=""
)

open class Inventory(
    var coment: String = "",
    var owner: Int = 0,
    var createAt: Date = Date(),
    var closeAt: Date = Date(),
    var arrivalTimeAtlas: Date = Date(),
    var arrivalTimeSap: Date = Date(),
    var defaultLocation: String = "",
    var name: String = "",
    var Realm_Id: String = "",
    var status: String = "Abierto",
    var codeSAP: Int=0,
    var response: String="",
    var type: String = "Otros",
    var wareHouse: String = "",
    var country:String=""
): RealmObject() {
    @PrimaryKey var _id: ObjectId = ObjectId()
}

open class InventoryResponse(
    var inventory: List<Inventory> =  emptyList(),
    var ownerName: String = "",
    var status: String = ""
)

open class Counting(
    var createAt: Date = Date(),
    var updateAt: Date = Date(),
    var inventoryId: ObjectId = ObjectId(),
    var itemCode: String = "",
    var itemName: String = "",
    var interfaz: String = "",
    var sscc: String = "",
    var quantity: Double  = 0.0,
    var location: String = "",
    var lote: String = "",
    var Realm_Id: String = ""
): RealmObject() {
    @PrimaryKey var _id: ObjectId = ObjectId()
}

open class CustomCounting(
    var counting: List<Counting> =  emptyList(),
    var defaultLocationSSCC:String="",
    var typeCode: TypeCode = TypeCode.QR
)

open class CountingResponse(
    var counting: List<Counting> =  emptyList(),
    var status: String = "",
    var nameInventory: String = "",
    var statusEvent:String=""
)

open class UpdateLine(
    var count: Double  =  1.0,
    var locationName: String = "",
    var cancelar:String="N"
)

open class DocumentInventory(
    var idInventoryHeader: String =  "",
    var type: String =  "",
    var idWhs: String = "",
    var defaultLocation: String=""
)

open class TypeInventario(
    var value:String="",
    var text:String=""
)