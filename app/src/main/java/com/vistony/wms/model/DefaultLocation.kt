package com.vistony.wms.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import org.bson.types.ObjectId

open class DefaultLocation(
    @PrimaryKey
    var _id: ObjectId = ObjectId(),
    var Bins: RealmList<DefaultLocation_Bins> = RealmList(),
    var Realm_Id: String = "",
    var WarehouseCode: String? = null
): RealmObject() {}

@RealmClass(embedded = true)
open class DefaultLocation_Bins(
    var AbsEntry: Int? = null,
    var BinCode: String? = null
): RealmObject() {}