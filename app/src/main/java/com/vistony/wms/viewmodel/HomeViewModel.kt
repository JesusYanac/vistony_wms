package com.vistony.wms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vistony.wms.model.Counting
import com.vistony.wms.model.CountingResponse
import com.vistony.wms.model.Inventory
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.syncSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.bson.types.ObjectId
import java.util.*

class HomeViewModel(idInventory:String): ViewModel() {

    private var idInventory:String= idInventory

    private var realm: Realm = Realm.getInstance(Realm.getDefaultConfiguration())

    private val _count = MutableStateFlow(CountingResponse())
    val counting: StateFlow<CountingResponse> get() = _count

    class HomeViewModelFactory(private var idInventory:String): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(idInventory) as T
        }
    }

    init{
        if(idInventory!="flag"){
            getData()
        }
    }

    fun resetCountingState(){
        _count.value=CountingResponse()
    }

    fun getData(){
        _count.value=CountingResponse(emptyList(),"cargando")

        Realm.getInstanceAsync(realm.configuration, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {

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
                _count.value=CountingResponse(emptyList()," ${exception.message}")
            }
        })
    }

    fun insertData(body:Counting){

        _count.value=CountingResponse(emptyList(),"cargando")

        realm.executeTransactionAsync { r: Realm ->

            val count = r.where(Counting::class.java)

                .equalTo("itemCode",body.itemCode)
                .equalTo("location",body.location)
                .equalTo("inventoryId",ObjectId(idInventory))
                .findFirst()

            if(count == null){
                val obj = r.createObject(Counting::class.java, ObjectId().toHexString())

                obj.itemCode=body.itemCode
                obj.itemName=body.itemName
                obj.quantity=body.quantity
                obj.inventoryId=ObjectId(idInventory)
                obj.realm_id=realm.syncSession.user.id

                r.insert(obj)

            }else{

                count.quantity=count.quantity+1

                r.insertOrUpdate(count)
            }

            _count.value=CountingResponse(emptyList(),"ok")
        }
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
            body?.updateAt= Date()

            r.insertOrUpdate(body)

            _count.value=CountingResponse(emptyList(),"ok")
        }
    }

    fun deleteData(idItem: ObjectId){

        _count.value=CountingResponse(emptyList(),"cargando")

        realm.executeTransactionAsync { r:Realm->

            val body: Counting? =r.where(Counting::class.java)
                .equalTo("_id", idItem)
                .findFirst()

            body?.deleteFromRealm()

            _count.value=CountingResponse(emptyList(),"ok")
        }
    }

}