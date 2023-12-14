package com.vistony.wms.model

import com.vistony.wms.num.TypeCode
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
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
    var Sku: String? = "",
    var QtyPallet: Double? = 0.0,
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
    var inDate:String="",
    var defaultBinLocation:String=""
)

open class ItemsResponse(
    var items:List<ItemResponse> = emptyList(),
    var type:TypeCode=TypeCode.QR,
    var status: String = "",
    var nameSscc:String="",
    var warehouse:String="",
    var defaultLocation:String="",
    var statusSscc:String="",
    //var tracking:List<Tranck> = emptyList()
)

open class ItemGroup(
    var GroupName: String? = null,
    var Number: Int? = null,
    var Realm_Id: String = ""
): RealmObject() {
    @PrimaryKey
    var _id: ObjectId? = null
}



open class TransfersLayout(
    var realm_id: String="",
    var codeSAP: Int = 0,
    var owner: Int = 0,
    var createAt: Date = Date(),
    var closeAt: Date = Date(),
    var arrivalTimeSap: Date = Date(),
    var detail: RealmList<TransfersLayoutDetail> = RealmList()
):RealmObject(){
    @PrimaryKey
    var _id:ObjectId = ObjectId()
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
):RealmObject(){}
