package com.vistony.wms.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.bson.types.ObjectId
import java.util.*

data class ListItems(
    val listArticle:List<Items> = emptyList(),
    val status: String="",
    val fechaDescarga: Date =Date()
)

open class Items(
    var CorpLine: String = "",
    var InventoryWeight: Double = 0.0,
    var ItemCode: String = "",
    var ItemName: String = "",
    var ItemsGroupCode: String = "",
    var PurchaseUnitHeight: String = "",
    var PurchaseUnitWidth: String = "",
    var PurchaseUnitLength: String = "",
    var Realm_Id: String = "",
    var UoMGroupEntry: String = ""
): RealmObject() {
    @PrimaryKey var _id: ObjectId = ObjectId()
}

open class ItemsResponse(
    var article: Items = Items(),
    var status: String = "",
    var lote: String ="",
    var quantity: Double=1.0
)

open class ItemGroup(
    var GroupName: String? = null,
    var Number: Int? = null,
    var Realm_Id: String = ""
): RealmObject() {
    @PrimaryKey
    var _id: ObjectId? = null
}
