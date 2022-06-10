package com.vistony.wms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

                val ubicacion= when(inventory.wareHouse){
                    "AN001"->{
                        13341
                    }
                    "AN002"->{
                        13342
                    }
                    "AN005"->{
                        13342
                    }
                    "AN021"->{
                        48035
                    }
                    else -> {
                        0
                    }
               }

                obj.name=inventory.name
                obj.wareHouse=inventory.wareHouse
                obj.location=ubicacion
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
                    .sort("updateAt", Sort.DESCENDING)
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

            //SI TODO ESTA OK FRENTE A SAP EL TRIGGER DE ATLAS CAMBIA EL ESTADO A CERRADO
            //body?.status ="Cerrado"
            body?.response =""
            body?.updateAt= Date()

            r.insertOrUpdate(body)

            //INFORMAR EL ESTATUS A LA VISTA

            /*val recovery=r.copyToRealmOrUpdate(body)

            if(recovery!=null ){
                _idInventoryHeader.value=recovery._id.toHexString()
            }else{
                _idInventoryHeader.value="error"
            }*/
        }

        getData()
    }

    /*r.beginTransaction()
    r.commitTransaction()
    r.cancelTransaction()*/

}