package com.vistony.wms.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import org.bson.types.ObjectId

open class QualityControlResponse(
    var status:String="",
    //var data:List<QualityControl> = emptyList()
    var data:QualityControl = QualityControl()
)

open class QualityControl(
    var Code: String? = null,
    var Collection: RealmList<QualityControl_Collection> = RealmList(),
    var Name: String? = null,
    var ObjType: Int? = null,
    var Order: Int? = null,
    var Realm_Id: String = ""
): RealmObject() {
    @PrimaryKey
    var _id: ObjectId? = null
}

@RealmClass(embedded = true)
open class QualityControl_Collection(
    var Code: String? = null,
    var Dscription: String? = null,
    var LineId: Int? = null,
    var Value: String? = null
): RealmObject() {}