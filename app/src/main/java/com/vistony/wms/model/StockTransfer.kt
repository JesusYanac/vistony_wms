package com.vistony.wms.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.LinkingObjects
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import org.bson.types.ObjectId
import java.util.*

open class StockTransferHeader(
    var ArrivalTimeAtlas: Date = Date(),
    var ArrivalTimeSap: Date = Date(),
    var CardCode: String = "",
    var CardName: String = "",
    var CloseAt: Date = Date(),
    var CodeSAP: Int = 0,
    var Comment: String = "",
    var CorrelativoDocument: String = "",
    var CreateAt: Date = Date(),
    var DocNum: String = "",
    var Motive: String = "",
    var NumAtCard: String = "",
    var NumReference: String = "",
    var ObjType: Int = 0,
    var PriceList: Int = 0,
    var Realm_Id: String = "",
    var Response: String = "",
    var SerieDocument: String = "",
    var Status: String = "",
    var Sucursal: String = "",
    var TaxDate: Date = Date(),
    var TypeDocument: String = "",
    var TypePurchase: String = "",
    var UpdateAt: Date = Date(),
    var WarehouseDestine: String = "",
    var WarehouseOrigin: String = "",
    var _TaskManagement: ObjectId = ObjectId()
): RealmObject(){
    @PrimaryKey
    var _id: ObjectId = ObjectId()
}

open class StockTransferHeaderRI(
    var id: String =  "",
    var whs: String =  "",
    var whsDestine: String =  "",
    var status:String="",
    var objType:Int=0,
    var cardCode:String="",
    var cardName:String="",
    var request:String=""
)

open class StockTransferHeaderResponse(
    var stockTransferHeader: List<StockTransferHeader> =  emptyList(),
    var ownerName: String = "",
    var status: String = ""
)

open class DocumentLongPress(
    var _id: ObjectId = ObjectId(),
    var batch:String="",
    var itemCode:String="",
    var whsDestine:String=""
)

open class StockTransferBody(
    var CreateAt: Date = Date(),
    var Code: String = "",
    var ItemCode: String = "",
    var ItemName: String = "",
    var Realm_Id: String = "",
    var Status: String = "Abierto",
    var Warehouse: String = "",
    var TotalQuantity: Double = 0.0,
    var Quantity: Double=0.0,
    var LineNum: Int = 0,
    var UpdateAt: Date = Date(),
    var _StockTransferHeader: ObjectId = ObjectId(),
): RealmObject() {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
}

open class StockTransferBodyPayload(
    var ItemCode: String = "",
    var ItemName: String = "",
    var Batch: String = "",
    var LocationCode: String = "",
    var LocationName: String = "",
    var Quality: QualityControl_Collection?=QualityControl_Collection(),
    var Quantity: Double = 0.0
)

open class StockTransferBodyAndSubBody(
    var body:StockTransferBody = StockTransferBody(),
    var subBody:List<StockTransferSubBody> = emptyList()
)

open class StockTransferBodyResponse(
    var stockTransferBody: List<StockTransferBodyAndSubBody> =  emptyList(),
    var status: String = "",
    var trasnferenceStatus:String="",
    var wareHouseOrigin:String="",
    var wareHouseDestine:String="",
    var createAt:String="",
    var ownerName: String = ""
)

open class StockTransferSubBodyRI(
    var status: String =  "",
    var message:String="",
    var data:StockTransferSubBody = StockTransferSubBody()
)
open class ManyToOne(
    var id: ObjectId=ObjectId(),
    var locationCode:String="",
    var locationName:String="",
    var quantityNow: Double=0.0,
    var quantityUsed: Double=0.0,
    var quantityAvailable: Double=0.0
)

open class StockTransferSubBody(
    var Batch: String = "",
    var CreateAt: Date = Date(),
    var LocationCode: String = "",
    var LocationName: String = "",
    var Delete: String = "N",
    var Quantity: Double = 0.0,
    var Quality: String = "",
    var Realm_Id: String = "",
    var Status: String = "Pendiente",
    var UpdateAt: Date = Date(),
    var Destine: RealmList<StockTransferSubBody_Destine> = RealmList(),
    var _StockTransferBody: ObjectId = ObjectId()
): RealmObject() {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
}


@RealmClass(embedded = true)
open class StockTransferSubBody_Destine(
    var CreateAt: Date? = Date(),
    var LocationCode: String? = "",
    var LocationName: String? = "",
    var Quantity: Double? = 0.0
): RealmObject() {}

open class StockTransferBodyAndSubBodyResponse(
    var stockTransferBody: StockTransferBody=StockTransferBody(),
    var stockTransferSubBody: List<StockTransferSubBody> =  emptyList(),
    var quantityDestine:Double=0.0,
    var status: String = ""
)

open class BinLocation(
    var id:String="",
    var text:String=""
)

open class StockTransferPayloadVal(
    var batch:String="",
    //var origin:BinLocation=BinLocation(),
    var origin:List<ManyToOne> = emptyList(),
    var idBody:ObjectId= ObjectId(),
    var destine:BinLocation=BinLocation(),
    var quantity:Double=0.0
)