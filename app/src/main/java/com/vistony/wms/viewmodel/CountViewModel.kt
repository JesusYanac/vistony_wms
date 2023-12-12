package com.vistony.wms.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vistony.wms.model.Counting
import com.vistony.wms.model.CountingResponse
import com.vistony.wms.model.CustomCounting
import com.vistony.wms.model.Inventory
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.syncSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.bson.types.ObjectId
import java.util.Date

class CountViewModel(idInventory:String): ViewModel() {

    private var idInventory:String= idInventory

    private var realm: Realm = Realm.getInstance(Realm.getDefaultConfiguration())

    private val _count = MutableStateFlow(CountingResponse())
    val counting: StateFlow<CountingResponse> get() = _count

    private val _data = MutableStateFlow(CustomCounting())
    val data: StateFlow<CustomCounting> get() = _data

    //private val _sendOrClose = MutableStateFlow("")
    //val sendOrClose: StateFlow<String> get() = _sendOrClose

    class CountViewModelFactory(private var idInventory:String): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CountViewModel(idInventory) as T
        }
    }

    init{
        if(idInventory!="flag"){
            getData()
        }
    }

    fun writeData(body: CustomCounting){
        _data.value=body
    }

    fun resetSendOrClose(){
        _count.value=CountingResponse(counting=  emptyList(),status = "",nameInventory = "",statusEvent="")
    }

    fun resetCountingState(){
        _count.value=CountingResponse()
    }

    fun updateStatusClose(){
        _count.value=CountingResponse(counting=  emptyList(),status = "",nameInventory = "",statusEvent="cargando")

        realm.executeTransactionAsync ({ r:Realm->

            val body: Inventory? =r.where(Inventory::class.java)
                .equalTo("_id", ObjectId(idInventory))
                .findFirst()

            val count = r.where(Counting::class.java)
                .equalTo("inventoryId",ObjectId(idInventory))
                .findFirst()

            if(body!=null){
                if(count !=null){
                    body.status ="Cerrado"
                    body.response =""
                    body.closeAt= Date()

                    r.insertOrUpdate(body)
                }else{
                    _count.value=CountingResponse(counting=  emptyList(),status = "",nameInventory = "",statusEvent="La ficha de inventario no tiene conteos registrados.")
                }
            }else{
                _count.value=CountingResponse(counting=  emptyList(),status = "",nameInventory = "",statusEvent="La ficha de inventario no se encontro.")
            }

        },{
            _count.value=CountingResponse(counting=  emptyList(),status = "",nameInventory = "",statusEvent="ok")
        },{
            _count.value=CountingResponse(counting=  emptyList(),status = "",nameInventory = "",statusEvent=it.message.toString())
        })
        Log.e("REOS","CountViewModel-updateStatusClose-_count.value"+_count.value.statusEvent)
    }

    fun resendToSap(){

        _count.value=CountingResponse(counting=  emptyList(),status = "",nameInventory = "",statusEvent="cargando")

        realm.executeTransactionAsync ({ r:Realm->
            Log.e("REOS","CountViewModel-resendToSap-r"+r.toString())
            var value:Int=0
            val body: Inventory? =r.where(Inventory::class.java)
                .equalTo("_id", ObjectId(idInventory))
                .equalTo("codeSAP",value)
                .equalTo("status","Cerrado")
                .findFirst()

            if(body!=null){
                val count = r.where(Counting::class.java)
                    .equalTo("inventoryId",ObjectId(idInventory))
                    .findFirst()

                if(count != null){
                    body.response =""
                    body.arrivalTimeSap= body.createAt
                   // body.arrivalTimeAtlas =Date()
                    body.arrivalTimeAtlas =body.createAt
                    r.insertOrUpdate(body)
                }else{
                    _count.value=CountingResponse(counting=  emptyList(),status = "",nameInventory = "",statusEvent="La ficha de inventario no tiene conteos registrados.")
                }
            }else{
                _count.value=CountingResponse(counting=  emptyList(),status = "",nameInventory = "",statusEvent="La ficha de inventario no se encontro.")
            }
        },{
            _count.value=CountingResponse(counting=  emptyList(),status = "",nameInventory = "",statusEvent="ok")
        },{
            _count.value=CountingResponse(counting=  emptyList(),status = "",nameInventory = "",statusEvent=it.message.toString())
        })
        Log.e("REOS","CountViewModel-resendToSap-_count.value"+_count.value)
    }

    fun getData(){
        _count.value=CountingResponse(emptyList(),"cargando")

        Realm.getInstanceAsync(realm.configuration, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {
                Log.e("REOS","CountViewModel-getData-r"+r.toString())
                val inventory = r.where(Inventory::class.java)
                    .equalTo("_id",ObjectId(idInventory))
                    .findFirst()

                val count = r.where(Counting::class.java)
                    .equalTo("inventoryId",ObjectId(idInventory))
                    .sort("updateAt", Sort.DESCENDING)
                    .findAll()

                count?.let { data: RealmResults<Counting> ->

                    val countTemp:List<Counting> = data.subList(0, data.size)

                    _count.value=CountingResponse(countTemp,"ok-data",inventory?.name?:"#")


                }
            }
            override fun onError(exception: Throwable) {
                Log.e("REOS","CountViewModel-getData-exception"+exception.toString())
                _count.value=CountingResponse(emptyList()," ${exception.message}")
            }
        })
        Log.e("REOS","CountViewModel-getData()-_count.value"+_count.value)
    }

    fun insertData(body:Counting){
        Log.e("REOS","CountViewModel-insertData-body"+body.toString())
        _count.value=CountingResponse(emptyList(),"cargando")

        realm.executeTransactionAsync { r: Realm ->
            Log.e("REOS","CountViewModel-insertData-r"+r.toString())
            //body.forEach { body ->
            val count = r.where(Counting::class.java)
                .equalTo("itemCode",body.itemCode)
                .equalTo("location",body.location)
                .equalTo("lote",body.lote)
                .equalTo("inventoryId",ObjectId(idInventory))
                .findFirst()

            if(count == null){
                val obj = r.createObject(Counting::class.java, ObjectId().toHexString())

                obj.itemCode=body.itemCode
                obj.itemName=body.itemName
                obj.quantity=body.quantity

                obj.location=body.location
                obj.sscc=body.sscc
                obj.interfaz=body.interfaz
                obj.lote=body.lote
                obj.inventoryId=ObjectId(idInventory)
                obj.Realm_Id=realm.syncSession.user.id

                r.insert(obj)

            }else{

                count.quantity=count.quantity+body.quantity

                r.insertOrUpdate(count)
            }
            //}


            _count.value=CountingResponse(emptyList(),"ok")
        }
        Log.e("REOS","CountViewModel-insertData-_count.value"+_count.value)
    }

    //se suma en el rack
    fun updateQuantity(lineUpdate: Counting){

        _count.value=CountingResponse(emptyList(),"cargando")

        realm.executeTransactionAsync { r:Realm->

            val body: Counting? =r.where(Counting::class.java)
                .equalTo("_id", lineUpdate._id)
                //.equalTo("location",lineUpdate.location)
                .findFirst()

            body?.quantity=lineUpdate.quantity
            body?.location=lineUpdate.location
            body?.lote=lineUpdate.lote
            body?.updateAt= Date()

            r.insertOrUpdate(body)

            _count.value=CountingResponse(emptyList(),"ok")
        }
    }

    fun deleteData(idItem: ObjectId){
        Log.e("REOS","CountViewModel-deleteData-idItem"+idItem.toString())
        _count.value= CountingResponse(emptyList(),"cargando")

        realm.executeTransactionAsync { r: Realm ->

            val body: Counting? =r.where(Counting::class.java)
                .equalTo("_id", idItem)
                .findFirst()

            body?.deleteFromRealm()

            _count.value=CountingResponse(emptyList(),"ok")
        }
    }

}
