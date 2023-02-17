package com.vistony.wms.model

import com.vistony.wms.num.TypeCode
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.bson.types.ObjectId
import java.util.*

/*Información  del articulo obtenido por lote*/

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

open class ItemResponse(
    var item: Items = Items(),
    var status: String = "",
    var lote: String ="",
    var quantity: Double=1.0,
    var expireDate:String="",
    var defaultBinLocation:String=""
)

open class ItemsResponse(
    var items:List<ItemResponse> = emptyList(),
    var type:TypeCode=TypeCode.QR,
    var status: String = "",
    var nameSscc:String="",
    var defaultLocation:String=""
)

open class ItemGroup(
    var GroupName: String? = null,
    var Number: Int? = null,
    var Realm_Id: String = ""
): RealmObject() {
    @PrimaryKey
    var _id: ObjectId? = null
}
