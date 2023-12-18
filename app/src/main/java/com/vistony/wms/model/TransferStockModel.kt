package com.vistony.wms.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import org.bson.types.ObjectId
import java.util.Date

open class TransfersLayout(
    var realm_id: String="",
    var response: String="",
    var codeSAP: Int = 0,
    var owner: Int = 0,
    var createAt: Date = Date(),
    var closeAt: Date = Date(),
    var arrivalTimeSap: Date = Date(),
    var detail: RealmList<TransfersLayoutDetail> = RealmList()
): RealmObject(){
    @PrimaryKey
    var _id: ObjectId = ObjectId()
}
@RealmClass(embedded = true)
open class TransfersLayoutDetail(
    var itemCode: String="",
    var itemName: String="",
    var quantity: Double=0.0,
    var batch: String="",
    var sscc: String="",
    var binOrigin: String="",
    var binDestine: String=""
): RealmObject(){}
