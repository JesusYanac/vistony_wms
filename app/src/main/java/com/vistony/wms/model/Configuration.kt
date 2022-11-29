package com.vistony.wms.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.bson.types.ObjectId

open class Configuration(
    var BinType: String = "",
    var Code: String = "",
    var FromWarehouse: String = "",
    var GroupCode: String = "",
    var ObjType: Int = 0,
    var SubZona: String = "",
    var ToWarehouse: String = "",
    var Zona: String = "",
    var Realm_Id: String = ""
): RealmObject() {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
}