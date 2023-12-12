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
import java.util.*

class InventoryViewModel(private var wareHouse:String): ViewModel() {

    private var realm: Realm = Realm.getInstance(Realm.getDefaultConfiguration())

    //private val _count = MutableStateFlow(CountingResponse())
    //val counting: StateFlow<CountingResponse> get() = _count

    private val _data = MutableStateFlow(CustomCounting())
    val data: StateFlow<CustomCounting> get() = _data

    private val _inventory = MutableStateFlow(InventoryResponse())
    val inventories: StateFlow<InventoryResponse> get() = _inventory

    private val _idInventoryHeader = MutableStateFlow(DocumentInventory())
    val idInventoryHeader: StateFlow<DocumentInventory> get() = _idInventoryHeader

    class InventoryViewModelFactory(private var wareHouse:String): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InventoryViewModel(wareHouse) as T
        }
    }

    init{
        getData()
    }

    fun resetIdInventoryHeader(){
        _idInventoryHeader.value=DocumentInventory()
    }

    fun writeData(body: CustomCounting){
        _data.value=body
    }

   /* fun resetSendOrClose(){
        _count.value=CountingResponse(counting=  emptyList(),status = "",nameInventory = "",statusEvent="")
    }

    fun resetCountingState(){
        _count.value=CountingResponse()
    }*/

    fun getData(){
        _inventory.value =  InventoryResponse(inventory=emptyList(), status = "cargando")
        Log.e("REOS","InventoryViewModel-getData-_inventory.value "+_inventory.value )
        Realm.getInstanceAsync(realm.configuration, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {
                Log.e("REOS","InventoryViewModel-getData-r"+r.toString())
                val inventory = r.where(Inventory::class.java)
                    .sort("closeAt", Sort.DESCENDING)
                    .findAll()

                inventory?.let { data: RealmResults<Inventory> ->

                    val inventoryTemp:List<Inventory> = data.subList(0, data.size)

                    val customUserData : Document? = r.syncSession.user.customData
                    val firstName= customUserData?.getString("FirstName")?:""
                    val lastName = customUserData?.getString("LastName")?:""

                    if(inventoryTemp.isNotEmpty()){
                        _inventory.value =  InventoryResponse(inventory=inventoryTemp, ownerName = "$firstName $lastName", status = "ok")
                    }else{
                        _inventory.value =  InventoryResponse(inventory=emptyList(), status = "vacio")
                    }


                }
            }

            override fun onError(exception: Throwable) {
                Log.e("REOS","InventoryViewModel-getData-exception"+exception.toString())
                _inventory.value =  InventoryResponse(inventory=emptyList(), status = " ${exception.message}")
            }
        })
        Log.e("REOS","InventoryViewModel-getData-_inventory.value.inventory"+_inventory.value.inventory)
    }


    fun addInventoryHeader(payload: InventoryPayload){
        Log.e("REOS","InventoryViewModel-addInventoryHeader-payload"+payload.toString())
        val customUserData : Document? = realm.syncSession.user.customData
        val employeeId=customUserData?.getInteger("EmployeeId")?:0
        val country=customUserData?.getString("Country")?:""

        if(employeeId!=0){
            realm.executeTransactionAsync { r: Realm ->
                Log.e("REOS","InventoryViewModel-addInventoryHeader-r"+r.toString())
                val obj = r.createObject(Inventory::class.java, ObjectId().toHexString())

                val currentTime = Date() // Tiempo actual
                obj.createAt = currentTime
                obj.arrivalTimeAtlas = currentTime
                obj.arrivalTimeSap = currentTime

                obj.name=payload.inventory.name
                obj.wareHouse=payload.inventory.wareHouse
                obj.Realm_Id=realm.syncSession.user.id
                obj.status=payload.inventory.status
                obj.type=payload.inventory.type
                obj.owner=employeeId
                obj.defaultLocation=payload.defaultLocation
                obj.country=country

                r.insert(obj)
                val recovery=r.copyToRealmOrUpdate(obj)
                if(recovery!=null){
                    _idInventoryHeader.value=DocumentInventory(idInventoryHeader=recovery._id.toHexString(),idWhs=recovery.wareHouse,defaultLocation=payload.defaultLocation,type=payload.inventory.type)
                }else{
                    _idInventoryHeader.value=DocumentInventory("error")
                }
            }
        }else{
            _idInventoryHeader.value=DocumentInventory("error")
        }
        Log.e("REOS","InventoryViewModel-addInventoryHeader-_idInventoryHeader.value: "+_idInventoryHeader.value)
    }

    /*fun deleteData(idItem: ObjectId){

        _count.value=CountingResponse(emptyList(),"cargando")

        realm.executeTransactionAsync { r:Realm->

            val body: Counting? =r.where(Counting::class.java)
                .equalTo("_id", idItem)
                .findFirst()

            body?.deleteFromRealm()

            _count.value=CountingResponse(emptyList(),"ok")
        }
    }*/


    fun updateStatusClose(idInventory:ObjectId ){
        realm.executeTransactionAsync { r:Realm->

            val body: Inventory? =r.where(Inventory::class.java)
                .equalTo("_id",idInventory)
                .findFirst()

            val count = r.where(Counting::class.java)
                .equalTo("inventoryId",idInventory)
                .findFirst()

            if(count !=null && body!=null){
                body.status ="Cerrado"
                body.response =""
                body.closeAt= Date()

                r.insertOrUpdate(body)
            }
        }

        getData()
    }

    fun resendToSap(idInventory:ObjectId ){
        realm.executeTransactionAsync { r:Realm->
            val value:Int=0
            val body: Inventory? =r.where(Inventory::class.java)
                .equalTo("_id",idInventory)
                .equalTo("codeSAP",value)
                .equalTo("status","Cerrado")
                .findFirst()

            val count = r.where(Counting::class.java)
                .equalTo("inventoryId",idInventory)
                .findFirst()

            if(count != null && body!=null){
                val currentTime = Date() // Tiempo actual
                body.response = ""
                body.arrivalTimeSap = currentTime
                body.arrivalTimeAtlas = currentTime
                r.insertOrUpdate(body)
            }

        }

        getData()
    }
}