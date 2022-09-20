package com.vistony.wms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vistony.wms.model.Counting
import com.vistony.wms.model.DocumentInventory
import com.vistony.wms.model.Inventory
import com.vistony.wms.model.InventoryResponse
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.syncSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.bson.Document
import org.bson.types.ObjectId
import java.util.*


class InventoryViewModel(): ViewModel() {

    private var realm: Realm = Realm.getInstance(Realm.getDefaultConfiguration())

    private val _inventory = MutableStateFlow(InventoryResponse())
    val inventories: StateFlow<InventoryResponse> get() = _inventory

    private val _idInventoryHeader = MutableStateFlow(DocumentInventory())
    val idInventoryHeader: StateFlow<DocumentInventory> get() = _idInventoryHeader

    fun resetIdInventoryHeader(){
        _idInventoryHeader.value=DocumentInventory()
    }

    init{
        getData()
    }

    class InventoryViewModelFactory(): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InventoryViewModel() as T
        }
    }

    fun addInventoryHeader(inventory:Inventory){

        val customUserData : Document? = realm.syncSession.user.customData
        val employeeId=customUserData?.getInteger("employeeId")?:0
        val country=customUserData?.getString("country")?:""

        if(employeeId!=0){
            realm.executeTransactionAsync { r: Realm ->

                val obj = r.createObject(Inventory::class.java, ObjectId().toHexString())

                obj.name=inventory.name
                obj.wareHouse=inventory.wareHouse
                obj.realm_id=realm.syncSession.user.id
                obj.status=inventory.status
                obj.type=inventory.type
                obj.owner=employeeId
                obj.country=country

                r.insert(obj)

                val recovery=r.copyToRealmOrUpdate(obj)

                if(recovery!=null ){
                    _idInventoryHeader.value=DocumentInventory(recovery._id.toHexString(),recovery.wareHouse)
                }else{
                    _idInventoryHeader.value=DocumentInventory("error")
                }
            }
        }else{
            _idInventoryHeader.value=DocumentInventory("error")
        }


    }

    fun resetArticleStatus(){
        _inventory.value=InventoryResponse()
    }

    fun getData(){
        _inventory.value =  InventoryResponse(inventory=emptyList(), status = "cargando")

        Realm.getInstanceAsync(realm.configuration, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {

                val inventory = r.where(Inventory::class.java)
                    .sort("closeAt", Sort.DESCENDING)
                    .findAll()

                inventory?.let { data: RealmResults<Inventory> ->

                    val inventoryTemp:List<Inventory> = data.subList(0, data.size)

                    val customUserData : Document? = r.syncSession.user.customData
                    val firstName= customUserData?.getString("firstName")?:""
                    val lastName = customUserData?.getString("lastName")?:""

                    if(inventoryTemp.isNotEmpty()){
                        _inventory.value =  InventoryResponse(inventory=inventoryTemp, ownerName = "$firstName $lastName", status = "ok")
                    }else{
                        _inventory.value =  InventoryResponse(inventory=emptyList(), status = "vacio")
                    }


                }
            }

            override fun onError(exception: Throwable) {
                _inventory.value =  InventoryResponse(inventory=emptyList(), status = " ${exception.message}")
            }
        })
    }


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
                body.response =""
                body.arrivalTimeSap= body.createAt
                body.arrivalTimeAtlas =Date()

                r.insertOrUpdate(body)
            }

        }

        getData()
    }

    /*r.beginTransaction()
    r.commitTransaction()
    r.cancelTransaction()*/

}