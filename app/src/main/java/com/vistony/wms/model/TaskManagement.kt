package com.vistony.wms.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.bson.types.ObjectId
import java.util.*

open class TaskManagement(
    var ArrivalTimeSap: Date = Date(),
    var CardCode: String = "",
    var CardName: String = "",
    var Code: String = "",
    var Type: String = "Libre",
    var DateAssignment: Date = Date(),
    var DocDate: Date = Date(),
    var DocEntry: Int = 0,
    var DocNum: String = "",
    var Documento: String = "",
    var EndDate: Date = Date(),
    var ObjType: Int = 0,
    var Realm_Id: String = "",
    var Response: String = "",
    var StartDate: Date = Date(),
    var Status: String = "",
    var ScheduledTime: Date = Date(),
    var EstimatedTime: Int = 0
): RealmObject() {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
}


open class TaskMngmtAndHeaderDoc(
    var Task: TaskManagement = TaskManagement(),
    var Document: StockTransferHeader = StockTransferHeader(),
)

open class TaskManagementResponse(
    var data:List<TaskMngmtAndHeaderDoc> = emptyList(),
    var status:String=""
)

open class TaskMngmtDataForm(
    var serie:String = "",
    var correlativo:String="",
    var comentario:String="",
    var documentHeader:ObjectId=ObjectId()
)