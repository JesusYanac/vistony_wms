package com.vistony.wms.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.bson.types.ObjectId
import java.util.*

open class Inventory(
    var coment: String = "",
    var owner: Int = 0,
    var createAt: Date = Date(),
    var updateAt: Date = Date(),
    var location: Int = 0,
    var name: String = "",
    var realm_id: String = "",
    var status: String = "Abierto",
    var codeSAP: Int=0,
    var response: String="",
    var type: String = "Simple",
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
    var quantity: Long = 1,
    var location: String = "",
    var realm_id: String = ""
): RealmObject() {
    @PrimaryKey var _id: ObjectId = ObjectId()
}

open class CountingResponse(
    var counting: List<Counting> =  emptyList(),
    var status: String = "",
    var nameInventory: String = ""
)

open class UpdateLine(
    var count: Long =  1,
    var location: String = ""
)

open class DocumentInventory(
    var idInventoryHeader: String =  "",
    var idWhs: String = ""
)