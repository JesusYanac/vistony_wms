package com.vistony.wms.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import org.bson.types.ObjectId

open class Warehouse(
    var code: String = "",
    var name: String = "",
    var realm_id: String = "",
    var status: String = "",
    var zipCode: String = ""
): RealmObject() {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
}

open class Location(
    var absEntry: Int = 0,
    var binCode: String = "",
    var realm_id: String = "",
    var wareHouse: String = ""
): RealmObject() {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
}

open class LocationResponse(
    var location: Location = Location(),
    var status: String = ""
)

open class WarehouseResponse(
    var numLocation:List<Int> = emptyList(),
    var warehouse: List<Warehouse> = emptyList(),
    var status: String = ""
)