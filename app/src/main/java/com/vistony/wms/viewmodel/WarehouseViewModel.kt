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
import io.sentry.util.StringUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.bson.Document
import org.bson.types.ObjectId
import java.nio.charset.Charset
import java.util.*

class WarehouseViewModel(flag:String): ViewModel() {

    private var realm: Realm = Realm.getInstance(Realm.getDefaultConfiguration())
    private val customUserData: Document? = realm.syncSession.user.customData

    private var configCountry = SyncConfiguration
        .Builder(realm.syncSession.user, customUserData?.getString("Branch") ?: "")
        .build()

    private val _almacenes = MutableStateFlow(WarehouseResponse())
    val almacenes: StateFlow<WarehouseResponse> get() = _almacenes

    private val _location = MutableStateFlow(LocationResponse())
    val location: StateFlow<LocationResponse> get() = _location


    class WarehouseViewModelFactory(private var flag: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WarehouseViewModel(flag) as T
        }
    }

    init {
        if (flag == "init") {
            getMasterDataWarehouse()
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

        Realm.getInstanceAsync(configCountry, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {
                val almacenes = r.where(Warehouse::class.java)
                    .sort("WarehouseName", Sort.ASCENDING).findAll()

                almacenes?.let { data: RealmResults<Warehouse> ->

                    val temp: List<Warehouse> = data.subList(0, data.size)

                    if (temp.isNotEmpty()) {

                        val numLocation: MutableList<Int> = mutableListOf()

                        temp.forEach{ i->
                            val whsx = r.where(BinLocations::class.java)
                                .equalTo("Warehouse",i.WarehouseCode)
                                .findAll()

                            whsx?.let { datax: RealmResults<BinLocations> ->
                                val temp2: List<BinLocations> = datax.subList(0, datax.size)
                                numLocation.add(temp2.size)
                            }

                        }
                        _almacenes.value = WarehouseResponse(numLocation=numLocation,warehouse = temp, status = "ok",fechaDescarga = Date())
                    } else {
                        _almacenes.value =
                            WarehouseResponse(warehouse = emptyList(), status = "vacio", fechaDescarga = Date())
                    }
                }

                //r.close()
            }

            override fun onError(exception: Throwable) {
                _almacenes.value = WarehouseResponse(warehouse = emptyList(), status = " ${exception.message}",fechaDescarga = Date())
            }
        })
    }

    fun getLocations(AbsEntry:String,whsOrigin:String){

        _location.value= LocationResponse(location= BinLocations(),status="cargando")

        //val convertText=AbsEntry.replace("B", "")

        Realm.getInstanceAsync(configCountry, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {

                val almacen = r.where(Warehouse::class.java)
                    .equalTo("WarehouseCode",whsOrigin)
                    .findFirst()

                val location = r.where(BinLocations::class.java)
                    .equalTo("BinCode",AbsEntry)
                    .equalTo("Warehouse",whsOrigin)
                    .findFirst()


                if (location != null && almacen !=null) {

                    _location.value= LocationResponse(location=location,status="ok", EnableBinLocations = almacen.EnableBinLocations                                                                        )
                }else{
                    _location.value=LocationResponse(location=BinLocations(),status="vacio")
                }

            }
            override fun onError(exception: Throwable) {
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


}