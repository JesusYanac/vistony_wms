package com.vistony.wms.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import org.bson.types.ObjectId
import java.util.*

open class Warehouse(
    var Sucursal: String = "",
    var WarehouseCode: String = "",
    var WarehouseName: String = "",
    var EnableBinLocations: String = "",
    var Realm_Id: String = "",
): RealmObject() {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
}

open class BinLocations(
    var AbsEntry: Int = 0,
    var BinCode: String = "",
    var LockPick: String = "",
    var Realm_Id: String = "",
    var SubZona: String? = null,
    var Tipo: String? = null,
    var Warehouse: String = "",
    var Zona: String? = null
): RealmObject() {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
}

open class ResponseLocationAndItem(
    var locationResponse:LocationResponse= LocationResponse(),
    var itemResponse:ItemsResponse= ItemsResponse()
)

open class LocationResponse(
    var location: BinLocations = BinLocations(),
    var status: String = "",
    var EnableBinLocations:String="tNO"
)

open class WarehouseResponse(
    var numLocation:List<Int> = emptyList(),
    var warehouse: List<Warehouse> = emptyList(),
    var status: String = "",
    var fechaDescarga: Date =Date()
)