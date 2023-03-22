package com.vistony.wms.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vistony.wms.model.*
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.syncSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.bson.Document
import org.bson.types.ObjectId
import java.text.SimpleDateFormat
import java.util.*

class StockTransferHeaderViewModel(private val ObjType: TaskManagement,private val Flag:String): ViewModel() {

    private var realm: Realm = Realm.getInstance(Realm.getDefaultConfiguration())

    private val _MerchandiseHeader = MutableStateFlow(StockTransferHeaderRI())
    val MerchandiseHeaderValue: StateFlow<StockTransferHeaderRI> get() = _MerchandiseHeader

    private val _merchandise = MutableStateFlow(StockTransferHeaderResponse())
    val merchandise: StateFlow<StockTransferHeaderResponse> get() = _merchandise

    private val _result = MutableStateFlow(StockTransferHeaderRI())
    val result: StateFlow<StockTransferHeaderRI> get() = _result

    private val _form = MutableStateFlow(TaskMngmtDataForm())
    val form: StateFlow<TaskMngmtDataForm> get() = _form

    class StockTransferHeaderViewModelFactory(private var ObjType:TaskManagement,private var Flag:String=""): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StockTransferHeaderViewModel(ObjType,Flag) as T
        }
    }

    init{
        if(Flag!="Task"){
            getMerchandise(ObjType)
        }
    }

    fun onFormChanged(taskMngmtDataForm:TaskMngmtDataForm){

        Log.e("JEPICAME","VAOR NUEVO ES=> "+taskMngmtDataForm.serie+" "+taskMngmtDataForm.correlativo+" "+taskMngmtDataForm.comentario)
        _form.value=taskMngmtDataForm
    }

    fun resetMerchandiseHeader(){
        _MerchandiseHeader.value=StockTransferHeaderRI(request="")
    }

    fun resetResultHeader(){
        _result.value=StockTransferHeaderRI("")
    }

    fun getMerchandise(ObjType:TaskManagement,dataForm:TaskMngmtDataForm=TaskMngmtDataForm()){

        _form.value=dataForm

        _merchandise.value =  StockTransferHeaderResponse(stockTransferHeader =emptyList(), status = "cargando")

        Realm.getInstanceAsync(realm.configuration, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {

                val inventory:RealmResults<StockTransferHeader> = if(ObjType.ObjType==22){

                    Log.e("JEPICAME","TAREA ENTRO AQUI>>"+ObjType.DocNum)

                    r.where(StockTransferHeader::class.java)
                        .equalTo("ObjType",  ObjType.ObjType)
                        .equalTo("_TaskManagement", ObjType._id)
                        .sort("CreateAt", Sort.DESCENDING)
                        .findAll()
                }else{

                    val sdf = SimpleDateFormat("yyyy-MM-dd")
                    val currentDate = sdf.format(Date())

                    r.where(StockTransferHeader::class.java)
                        .equalTo("ObjType",  ObjType.ObjType)
                        //.contains("CreateAt",currentDate, Case.INSENSITIVE)
                        .sort("CreateAt", Sort.DESCENDING)
                        .findAll()
                }

                Log.e("JEPICAME","TAREA "+inventory.isNullOrEmpty())


                inventory.let { data: RealmResults<StockTransferHeader> ->

                    val inventoryTemp:List<StockTransferHeader> = data.subList(0, data.size)

                    val customUserData : Document? = r.syncSession.user.customData
                    val firstName= customUserData?.getString("FirstName")?:""
                    val lastName = customUserData?.getString("LastName")?:""

                    if(inventoryTemp.isNotEmpty()){
                        _merchandise.value =  StockTransferHeaderResponse(stockTransferHeader=inventoryTemp, ownerName = "$firstName $lastName", status = "ok")
                    }else{
                        _merchandise.value =  StockTransferHeaderResponse(stockTransferHeader=emptyList(), status = "vacio")
                    }

                }

                //realm.close()
            }
            override fun onError(exception: Throwable) {
                //realm.close()
                _merchandise.value =  StockTransferHeaderResponse(stockTransferHeader=emptyList(), status = " ${exception.message}")

            }
        })


    }

    fun addMerchandiseHeader(stockTransferHeader: StockTransferHeader){

        val customUserData : Document? = realm.syncSession.user.customData
        val employeeId=customUserData?.getInteger("EmployeeId")?:0

        if(employeeId!=0){
            realm.executeTransactionAsync { r: Realm ->

                val date:Date=Date()

                val task = r.createObject(TaskManagement::class.java, ObjectId().toHexString())
                task.Code=""
                task.CardCode=stockTransferHeader.WarehouseOrigin
                task.CardName=stockTransferHeader.WarehouseDestine
                task.DateAssignment=date
                task.DocDate=date
                task.DocEntry=0
                task.DocNum=""
                task.Documento= when(stockTransferHeader.ObjType){ 22-> {"Pedido de Compra"} 67->{"Transferencia de Stock"} 671 ->{"Slotting"} else->{""}}
                task.EndDate=date
                task.ObjType=stockTransferHeader.ObjType
                task.Realm_Id=realm.syncSession.user.id
                task.ArrivalTimeSap=date
                task.StartDate=date
                task.Status="En curso"

                //A Asignado
                //P Pendiente
                //E En curso
                //T Terminado

                r.insert(task)

                val recoveryTask=r.copyToRealmOrUpdate(task)


                val obj = r.createObject(StockTransferHeader::class.java, ObjectId().toHexString())

                obj.NumReference=stockTransferHeader.NumReference
                obj.Comment=stockTransferHeader.Comment
                obj.PriceList=stockTransferHeader.PriceList
                obj.Motive=stockTransferHeader.Motive
                obj.WarehouseOrigin=stockTransferHeader.WarehouseOrigin
                obj.WarehouseDestine=stockTransferHeader.WarehouseDestine
                obj.Status="Abierto"
                obj.ObjType=stockTransferHeader.ObjType
                obj.ArrivalTimeSap=date
                obj.CreateAt=date
                obj.Realm_Id=realm.syncSession.user.id
                obj._TaskManagement=recoveryTask._id

                r.insert(obj)

                val recovery=r.copyToRealmOrUpdate(obj)

                if(recovery!=null ){
                    _MerchandiseHeader.value= StockTransferHeaderRI(recovery._id.toHexString(),recovery.WarehouseOrigin,recovery.WarehouseDestine,recovery.Status,request="ok")
                }else{
                    _MerchandiseHeader.value= StockTransferHeaderRI("error",request="error")
                }
            }
        }else{
            _MerchandiseHeader.value= StockTransferHeaderRI("error",request="error")
        }
    }

    fun updateStatusClose(idInventory:ObjectId ){
        realm.executeTransactionAsync { r:Realm->

            val body: StockTransferHeader? =r.where(StockTransferHeader ::class.java)
                .equalTo("_id",idInventory)
                .findFirst()

            val count = r.where(StockTransferBody::class.java)
                .equalTo("HeaderId",idInventory)
                .findFirst()

            if(count !=null && body!=null){
                body.Status ="Cerrado"
                body.Response =""
                body.CloseAt= Date()

                r.insertOrUpdate(body)
            }
        }

        getMerchandise(ObjType)
    }

    fun updateHeaderStatus(objType:Int,idInventory:ObjectId,newStatus:String){
        _merchandise.value =  StockTransferHeaderResponse(stockTransferHeader =emptyList(), status = "cargando")

        realm.executeTransactionAsync ({ r:Realm->

            val headerDocument=r.where(StockTransferHeader ::class.java)
                .equalTo("_id",idInventory)
                .findFirst()

            if(headerDocument!=null){
                headerDocument.Status =newStatus
                headerDocument.UpdateAt= Date()

                if(newStatus=="FichaCerrada"){
                    headerDocument.Response=""
                    headerDocument.CloseAt= Date()
                }

                r.insertOrUpdate(headerDocument)

                _merchandise.value =  StockTransferHeaderResponse(stockTransferHeader = listOf(headerDocument), status = "ok")
            }else{
                Log.e("JEPICAME","=>si e snull xdd")
            }
        },{
            _merchandise.value =  StockTransferHeaderResponse(stockTransferHeader = emptyList(), status = "ok")
        },{
            _merchandise.value =  StockTransferHeaderResponse(stockTransferHeader =emptyList(), status = it.message.toString())
        })
    }

    fun updateHeader(taskMngmtDataForm:TaskMngmtDataForm){

        _result.value= StockTransferHeaderRI(request = "cargando")

        Realm.getDefaultInstance().use { realm ->
            realm.executeTransactionAsync ({ bgRealm ->

                val task=bgRealm.where(TaskManagement::class.java)
                    .equalTo("_id",taskMngmtDataForm.documentHeader)
                    .findFirst()

                if(task!=null){
                    if(task?.Status=="Asignado"){
                        task.Status ="En Curso"
                        task.StartDate =Date()
                        bgRealm.insertOrUpdate(task!!)
                    }

                    val body=bgRealm.where(StockTransferHeader::class.java)
                        .equalTo("_TaskManagement",taskMngmtDataForm.documentHeader)
                        .findFirst()

                    body?.Comment=taskMngmtDataForm.comentario
                    body?.SerieDocument=taskMngmtDataForm.serie
                    body?.CorrelativoDocument=taskMngmtDataForm.correlativo
                    body?.NumAtCard=taskMngmtDataForm.serie+"-"+taskMngmtDataForm.correlativo
                    body?.UpdateAt= Date()

                    bgRealm.insertOrUpdate(body!!)

                    val recoveryTask=bgRealm.copyToRealmOrUpdate(body)

                    if(recoveryTask!=null ){

                        _result.value =  StockTransferHeaderRI(request = "ok",
                            id=recoveryTask._id.toHexString(),
                            whs = recoveryTask.WarehouseOrigin,
                            whsDestine = recoveryTask.WarehouseDestine,
                            status =   recoveryTask.Status,
                            objType=recoveryTask.ObjType,
                            cardCode = recoveryTask.CardCode,
                            cardName = recoveryTask.CardName
                        )
                    }else{
                        _result.value =  StockTransferHeaderRI( request = "error")
                    }
                }else{
                    _result.value =  StockTransferHeaderRI( request = "Error al encontrar la tarea.")
                }
            },{
                //_result.value =  StockTransferHeaderResponse(stockTransferHeader=emptyList(), status = "ok")

                //realm.close()
            },{
                Log.e("JEPICAMR","=>"+it.message)
                _result.value =  StockTransferHeaderRI( request = " ${it.message}")
                //realm.close()
            }

            )
        }
    }

    fun resendToSap(idInventory:ObjectId ){
        realm.executeTransactionAsync { r:Realm->
            val value:Int=0
            val body: StockTransferHeader? =r.where(StockTransferHeader::class.java)
                .equalTo("_id",idInventory)
                .equalTo("CodeSAP",value)
                .equalTo("Status","Cerrado")
                .findFirst()

            val count = r.where(StockTransferBody::class.java)
                .equalTo("HeaderId",idInventory)
                .findFirst()

            if(count != null && body!=null){
                body.Response =""
                body.ArrivalTimeSap= body.CreateAt
                body.ArrivalTimeAtlas = Date()

                r.insertOrUpdate(body)
            }
        }

        getMerchandise(ObjType)
    }
}