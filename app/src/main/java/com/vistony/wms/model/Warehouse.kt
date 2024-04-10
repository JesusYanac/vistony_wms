package com.vistony.wms.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.bson.types.ObjectId
import java.util.*

open class Warehouses(
    var Sucursal: String = "",
    var WarehouseCode: String = "",
    var WarehouseName: String = "",
    var EnableBinLocations: String = "",
    var DefaultBin: Int = 0,
    var Realm_Id: String = "",
    var WmsLocation: String = "",
): RealmObject() {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
}

open class WarehouseBinLocation(
    var warehouse: Warehouses = Warehouses(),
    var defaultLocation: String = ""
)

open class BinLocations(
    var AbsEntry: Int = 0,
    var BinCode: String = "",
    var LockPick: String = "",
    var Realm_Id: String = "",
    var SubZona: String? = null,
    var Tipo: String? = null,
    var Warehouse: String = "",
    var Zona: String? = null,
    //var IsFull: String = ""
): RealmObject() {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
}

open class ResponseLocationAndItem(
    var locationResponse:LocationResponse= LocationResponse(),
    var itemResponse:ItemResponse= ItemResponse()
)

open class LocationResponse(
    var location: BinLocations = BinLocations(),
    var status: String = "",
    var EnableBinLocations:String="tNO"
)

open class WarehouseResponse(
    var numLocation:List<Int> = emptyList(),
    var defaultLocation:List<String> = emptyList(),
    var warehouse: List<Warehouses> = emptyList(),
    var status: String = "",
    var fechaDescarga: Date =Date()
)