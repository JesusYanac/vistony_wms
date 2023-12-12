package com.vistony.wms.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vistony.wms.model.*
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.syncSession
import io.realm.mongodb.sync.SyncConfiguration
import io.sentry.Sentry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.bson.Document
import java.util.*

class WarehouseViewModel(flag:String,warehouse:String="",objType:Int=0): ViewModel() {

    private var realm: Realm = Realm.getInstance(Realm.getDefaultConfiguration())
    private val customUserData: Document? = realm.syncSession.user.customData

    private var configBranch = SyncConfiguration
        .Builder(realm.syncSession.user, customUserData?.getString("Branch") ?: "")
        .build()

    private val _almacenes = MutableStateFlow(WarehouseResponse())
    val almacenes: StateFlow<WarehouseResponse> get() = _almacenes

    private val _location = MutableStateFlow(LocationResponse(status=""))
    val location: StateFlow<LocationResponse> get() = _location

    class WarehouseViewModelFactory(private var flag: String,private var wareHouse:String="",private var objType:Int=0) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WarehouseViewModel(flag, wareHouse,objType ) as T
        }
    }

    init{
        if (flag == "init") {
            getMasterDataWarehouse()
        }

        if(warehouse!="" && objType !in setOf(22, 18)){
            getDefaultLocations(warehouse)
        }
    }

    fun resetWarehouseStatus() {
        _almacenes.value = WarehouseResponse()
    }

    fun resetLocationStatus() {
        _location.value = LocationResponse(location= BinLocations(),status="")
    }

    fun getMasterDataWarehouse() {

        Log.e("JEPICAME","Cargando")
        _almacenes.value = WarehouseResponse(warehouse = emptyList(), status = "cargando")
        Log.e("REOS","WareHouseViewModel-getMasterDataWarehouse-configBranch"+configBranch)
        Realm.getInstanceAsync(configBranch, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {
                Log.e("REOS","WarehouseViewModel-getMasterDataWarehouse-onSuccess-r"+r.toString())
                val almacenes = r.where(Warehouses::class.java)
                    .sort("WarehouseName", Sort.ASCENDING).findAll()
                Log.e("REOS","WarehouseViewModel-getMasterDataWarehouse-onSuccess-almacenes"+almacenes)
                almacenes?.let { data: RealmResults<Warehouses> ->
                    Log.e("REOS","WarehouseViewModel-getMasterDataWarehouse-onSuccess-data"+data)
                    val temp: List<Warehouses> = data.subList(0, data.size)

                    if (temp.isNotEmpty()) {

                        val numLocation: MutableList<Int> = mutableListOf()
                        val defaultLocation: MutableList<String> = mutableListOf()

                        temp.forEach{ i->
                            val whsx = r.where(BinLocations::class.java)
                                .equalTo("Warehouse",i.WarehouseCode)
                                .findAll()

                            whsx?.let { datax: RealmResults<BinLocations> ->
                                val temp2: List<BinLocations> = datax.subList(0, datax.size)
                                if(i.WmsLocation == "N" ){
                                    defaultLocation.add("-")
                                    numLocation.add(0)
                                }else{
                                    if(temp2.size==1){
                                        defaultLocation.add(temp2[0].BinCode)
                                    }else if(temp2.size>1){
                                        defaultLocation.add("+")
                                    }else{
                                        defaultLocation.add("-")
                                    }

                                    numLocation.add(temp2.size)
                                }
                            }

                        }
                        _almacenes.value = WarehouseResponse(defaultLocation=defaultLocation,numLocation=numLocation,warehouse = temp, status = "ok",fechaDescarga = Date())
                    } else {
                        _almacenes.value =
                            WarehouseResponse(warehouse = emptyList(), status = "vacio", fechaDescarga = Date())
                    }
                }
            }
            override fun onError(exception: Throwable) {
                Log.e("REOS","WarehouseViewModel-getMasterDataWarehouse-onError-exception"+exception.toString())
                _almacenes.value = WarehouseResponse(warehouse = emptyList(), status = " ${exception.message}",fechaDescarga = Date())
            }
        })
    }

    fun verificationLocation(binCode:String,AbsEntry: String){
        Log.e("JEPICAME","===>"+binCode)
        Log.e("REOS","WarehouseViewModel-verificationLocation-binCode: "+binCode)
        Log.e("REOS","WarehouseViewModel-verificationLocation-AbsEntry: "+AbsEntry)
        _location.value= LocationResponse(location= BinLocations(),status="cargando")

        Realm.getInstanceAsync(configBranch, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {
                if(!binCode.isNullOrEmpty())
                {
                    val location = r.where(BinLocations::class.java)
                        .equalTo("BinCode",binCode)
                        .findFirst()
                    if (location != null) {
                        _location.value= LocationResponse(location=location,status="ok", EnableBinLocations = "tYES")
                    }else{
                        _location.value=LocationResponse(location=BinLocations(),status="La ubicación ${binCode}, no se encuentra como dato maestro.")
                    }
                }else {
                    val location = r.where(BinLocations::class.java)
                        .equalTo("AbsEntry",AbsEntry.toInt())
                        .findFirst()
                    if (location != null) {
                        _location.value= LocationResponse(location=location,status="ok", EnableBinLocations = "tYES")
                    }else{
                        _location.value=LocationResponse(location=BinLocations(),status="La ubicación ${binCode}, no se encuentra como dato maestro.")
                    }
                }
            }
            override fun onError(exception: Throwable) {
                _location.value= LocationResponse(location=BinLocations(),status=" ${exception.message}")
            }
        })
        Log.e("REOS","WarehouseViewModel-verificationLocation-_location.value.location.BinCode: "+_location.value.location.BinCode)
        Log.e("REOS","WarehouseViewModel-verificationLocation-_location.value.location.AbsEntry: "+_location.value.location.AbsEntry)
    }

    fun getLocations(AbsEntry:String,whsOrigin:String,objType: Int){
        Log.e("REOS","WarehouseViewModel-getLocations-AbsEntry: "+AbsEntry)
        Log.e("REOS","WarehouseViewModel-getLocations-whsOrigin: "+whsOrigin)
        _location.value= LocationResponse(location= BinLocations(),status="cargando")

        val convertText=AbsEntry.replace("B", "")
        var valorAbsEntry: Int=0

        try {
            valorAbsEntry = convertText.toIntOrNull()!!

        }catch(e:Exception){
            Sentry.captureMessage(" ${e.message.toString()}")

        }

        Realm.getInstanceAsync(configBranch, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {

                val almacen = r.where(Warehouses::class.java)
                    .equalTo("WarehouseCode",whsOrigin)
                    .findFirst()
                Log.e("REOS","WarehouseViewModel-getLocations-valorAbsEntry.toString()"+valorAbsEntry.toString())
                if(almacen!=null){
                    val location = r.where(BinLocations::class.java)
                        //.equalTo("BinCode",AbsEntry)
                        //.equalTo("BinCode",valorAbsEntry)
                        //.equalTo(if(objType==6701){"BinCode"}else{"AbsEntry"} ,if(objType==6701){valorAbsEntry.toString()}else{valorAbsEntry.toInt()})
                        //.equalTo("AbsEntry",valorAbsEntry)
                        .equalTo("BinCode",if (objType==6701||objType==67||objType==1250000001||objType==-1){AbsEntry}else{valorAbsEntry.toString()})
                        .equalTo("Warehouse",whsOrigin)
                        .findFirst()

                    if (location != null) {
                        _location.value= LocationResponse(location=location,status="ok", EnableBinLocations = almacen.EnableBinLocations)
                    }else{
                        _location.value=LocationResponse(location=BinLocations(),status="La ubicación ${AbsEntry}, no se encuentra como dato maestro o no pertenece al almacén ${whsOrigin}.")
                    }
                }else{

                    _location.value=LocationResponse(location=BinLocations(),status="El almacén ${whsOrigin}, no se encuentra como dato maestro.")
                }
            }
            override fun onError(exception: Throwable) {
                _location.value= LocationResponse(location=BinLocations(),status=" ${exception.message}")
            }
        })
    }

    fun getDefaultLocations(whsOrigin:String){

        Log.e("JEPICAMR","ENTRO A BUSCAR UNICACION +XDD")

        _location.value= LocationResponse(location= BinLocations(),status="cargando")

        Realm.getInstanceAsync(configBranch, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {
                Log.e("REOS","WarehouseViewModel-getDefaultLocations-onSuccess-r"+r.toString())
                val almacen = r.where(Warehouses::class.java)
                    .equalTo("WarehouseCode",whsOrigin)
                    .findFirst()

                if(almacen!=null){
                    val location = r.where(BinLocations::class.java)
                        .equalTo("AbsEntry",almacen.DefaultBin)
                        .equalTo("Warehouse",whsOrigin)
                        .findFirst()

                    if (location != null) {
                        _location.value= LocationResponse(location=location,status="ok", EnableBinLocations = almacen.EnableBinLocations)
                    }else{
                        Log.e("JEPICAMR","ENTRO A AQUI X ESO NO ACTUALIZAA +XDD")
                        _location.value=LocationResponse(location=BinLocations(),status="El almacén ${whsOrigin}, no tiene la ubicaciones por defecto cargada en el maestro.")
                    }
                }else{
                    _location.value=LocationResponse(location=BinLocations(),status="El almacén ${whsOrigin}, no se encuentra en el maestro.")
                }
            }
            override fun onError(exception: Throwable) {
                Log.e("REOS","WarehouseViewModel-getDefaultLocations-onSuccess-exception:"+exception.toString())
                _location.value= LocationResponse(location=BinLocations(),status=" ${exception.message}")
            }
        })
    }



    /*fun resetArticleStatus(){
        _articulo.value=ArticleResponse()
    }

    fun getArticle(itemCode:String){
        _articulo.value=ArticleResponse(article=Article(),status="cargando")

        Realm.getInstanceAsync(configPublic, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {
                val article = r.where(Article::class.java)
                    .equalTo("itemCode",itemCode)
                    .findFirst()

                    if (article != null) {
                        _articulo.value= ArticleResponse(article=article,status="ok")
                    }else{
                        _articulo.value=ArticleResponse(article=Article(),status="vacio")
                    }
            }
            override fun onError(exception: Throwable) {
                _articulo.value= ArticleResponse(article=Article(),status=" ${exception.message}")
            }
        })
    } */

    fun getWarehouse(WarehouseCode: String) {

        Log.e("JEPICAME","Cargando")
        _almacenes.value = WarehouseResponse(warehouse = emptyList(), status = "cargando")
        Log.e("REOS","WareHouseViewModel-getMasterDataWarehouse-configBranch"+configBranch)
        Realm.getInstanceAsync(configBranch, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {
                Log.e("REOS","WarehouseViewModel-getMasterDataWarehouse-onSuccess-r"+r.toString())
                val almacenes = r.where(Warehouses::class.java)
                    .equalTo("WarehouseCode",WarehouseCode)
                    .sort("WarehouseName", Sort.ASCENDING).findAll()
                Log.e("REOS","WarehouseViewModel-getMasterDataWarehouse-onSuccess-almacenes"+almacenes)
                almacenes?.let { data: RealmResults<Warehouses> ->
                    Log.e("REOS","WarehouseViewModel-getMasterDataWarehouse-onSuccess-data"+data)
                    val temp: List<Warehouses> = data.subList(0, data.size)

                    if (temp.isNotEmpty()) {

                        val numLocation: MutableList<Int> = mutableListOf()
                        val defaultLocation: MutableList<String> = mutableListOf()

                        temp.forEach{ i->
                            val whsx = r.where(BinLocations::class.java)
                                .equalTo("Warehouse",i.WarehouseCode)
                                .findAll()

                            whsx?.let { datax: RealmResults<BinLocations> ->
                                val temp2: List<BinLocations> = datax.subList(0, datax.size)

                                if(i.WmsLocation == "N" ){
                                    defaultLocation.add("-")
                                    numLocation.add(0)
                                }else{
                                    if(temp2.size==1){
                                        defaultLocation.add(temp2[0].BinCode)
                                    }else if(temp2.size>1){
                                        defaultLocation.add("+")
                                    }else{
                                        defaultLocation.add("-")
                                    }

                                    numLocation.add(temp2.size)
                                }
                            }

                        }
                        _almacenes.value = WarehouseResponse(defaultLocation=defaultLocation,numLocation=numLocation,warehouse = temp, status = "ok",fechaDescarga = Date())
                    } else {
                        _almacenes.value =
                            WarehouseResponse(warehouse = emptyList(), status = "vacio", fechaDescarga = Date())
                    }
                }
            }
            override fun onError(exception: Throwable) {
                Log.e("REOS","WarehouseViewModel-getMasterDataWarehouse-onError-exception"+exception.toString())
                _almacenes.value = WarehouseResponse(warehouse = emptyList(), status = " ${exception.message}",fechaDescarga = Date())
            }
        })
    }


}