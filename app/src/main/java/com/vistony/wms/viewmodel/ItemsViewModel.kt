package com.vistony.wms.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vistony.wms.num.TypeCode
import com.vistony.wms.model.*
import com.vistony.wms.util.APIService
import com.vistony.wms.util.isNumeric
import com.vistony.wms.util.removeLastChar
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.syncSession
import io.realm.mongodb.sync.SyncConfiguration
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.bson.types.ObjectId
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.Date

class ItemsViewModel(flag:String): ViewModel() {

    private var realm: Realm = Realm.getInstance(Realm.getDefaultConfiguration())

    private var configPublic =SyncConfiguration
            .Builder(realm.syncSession.user, "public")
            .build()

   /* private var configPrivate= SyncConfiguration
        .Builder(realm.syncSession.user,realm.syncSession.user.id.toString())
        .build()*/

    private val _articulos = MutableStateFlow(ListItems())
    val articles: StateFlow<ListItems> get() = _articulos

    private val _articulo = MutableStateFlow(ItemsResponse())
    val article: StateFlow<ItemsResponse> get() = _articulo

    class ArticleViewModelFactory(private var flag:String): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ItemsViewModel(flag) as T
        }
    }

    init {
        if(flag=="init"){
            getMasterDataArticle()
        }
    }

    fun resetArticleStatus(){
        _articulo.value=ItemsResponse()
    }

    private fun getSSCC(code:String){
        //  Cargando esta en la funcion padre donde valida primero en Realm

        Log.e("Jepicame idJehdaer","=>ENTRO A SSC 67 API")

        viewModelScope.launch(Dispatchers.Default){
            APIService.getInstance()
            .getSscc("http://192.168.254.20:93/vs1.0/sscc",code, "NaN")
            .enqueue(object:Callback<Sscc> {
                override fun onResponse(call: Call<Sscc>, response: Response<Sscc>) {
                    if(response.isSuccessful){

                        val items:List<ItemResponse> = response.body()?.data!!.vISWMSSCC1Collection.map {
                            ItemResponse(
                                item=Items(
                                    ItemName = it.uDscription,
                                    ItemCode = it.uItemCode
                                ),
                                status="ok",
                                lote=it.uBatch,
                                quantity=it.uQuantity.toDouble(),
                                expireDate = it.exDate,
                                inDate=it.uDate
                            )
                        }

                        _articulo.value=ItemsResponse(
                            items=items,
                            type=TypeCode.SSCC,
                            nameSscc=response.body()?.data!!.code,
                            defaultLocation=response.body()?.data!!.uBtringinCode,
                            status="ok",
                            warehouse = response.body()?.data!!.uWhsCode
                        )

                    }else{
                        if(response.code()==424){
                            _articulo.value=ItemsResponse(items= emptyList(),status="El código SSCC $code, no se encontro en SAP",type=TypeCode.SSCC)
                        }else{
                            _articulo.value=ItemsResponse(items= emptyList(),status="Ocurrio un error, el servidor respondio código ${response.code()}",type=TypeCode.SSCC)
                        }
                    }
                }
                override fun onFailure(call: Call<Sscc>, error: Throwable) {
                    Log.e("JEPICAME","ERRRP =>"+error.message)
                    _articulo.value= ItemsResponse(items=emptyList(),status=" ${error.message}",type=TypeCode.SSCC)
                }
            })
        }
    }

    private fun getInformationQR(itemCode:String,batch:String){
        _articulo.value=ItemsResponse(items=emptyList(),status="cargando",type=TypeCode.QR)

        viewModelScope.launch(Dispatchers.Default){
            APIService.getInstance()
                .getArticleFromBatchQrEspecial("http://192.168.254.20:93/vs1.0/Inventory/getItem",itemCode)
                .enqueue(object:Callback<ProductFromBatch> {
                    override fun onResponse(call: Call<ProductFromBatch>, response: Response<ProductFromBatch>) {
                        if(response.isSuccessful){

                            val items:List<ItemResponse> = listOf(
                                ItemResponse(
                                    item=Items(
                                        ItemName =  response.body()?.data!!.itemName,
                                        ItemCode =  response.body()?.data!!.itemCode
                                    ),
                                    status="ok",
                                    lote=batch,
                                    quantity=0.0
                                )
                            )

                            if(response.body()?.data!!.locked=="tYES"){
                                _articulo.value=ItemsResponse(items=items,type=TypeCode.QR,status="locked")
                            }else{
                                _articulo.value=ItemsResponse(items=items,type=TypeCode.QR,status="ok")
                            }
                        }else{
                            _articulo.value=ItemsResponse(items= emptyList(),status="El código $itemCode, no se encuentra ${response.code()}.",type=TypeCode.QR)
                        }
                    }
                    override fun onFailure(call: Call<ProductFromBatch>, error: Throwable) {
                        _articulo.value= ItemsResponse(items=emptyList(),status=" ${error.message}",type=TypeCode.QR)
                    }
                })
        }
    }

    fun getArticle(value:String, idHeader:String="",typeInventario:String=""){

        _articulo.value=ItemsResponse(items= emptyList(),status="cargando",type=TypeCode.QR)

        if(value.length==20 && isNumeric(value)){
            realmVerificationSSCC(
                idHeader=idHeader,
                sscc=value
            )
        }else{
            val count:Int = value.split("|").size

            var lote:String = ""
            var itemCodeNew:String = ""
            var quantity:Double = 1.0

            when(count){
                0->{}
                1->{
                    itemCodeNew=value.split("|")[0]
                }
                2->{

                    itemCodeNew=value.split("|")[0]
                    lote=value.split("|")[1]

                }else->{
                    itemCodeNew=value.split("|")[0]
                    lote=value.split("|")[1]
                    quantity = try{
                        value.split("|")[2].toDouble()
                    }catch(e:Exception){
                        1.0
                    }
                }
            }

            if(itemCodeNew.length in listOf(7,13)){
                realmGetItem(idHeader,itemCodeNew,lote,quantity)
            }else{
                //QR ESPECIAL - bloqueo de presentaciones
                itemCodeNew=removeLastChar(itemCodeNew)
                if(typeInventario.contains("Picking") || typeInventario.contains("Despacho")){
                   getInformationQR(itemCodeNew,lote)
                }else{
                    realmGetItem(idHeader,itemCodeNew,lote,quantity)
                }
            }
        }
    }

    private fun realmVerificationSSCC(idHeader:String="",sscc:String=""){

        _articulo.value=ItemsResponse(items=emptyList(),status="cargando",type=TypeCode.SSCC)
        val _code:String = sscc.substring(2)

        if(idHeader.isEmpty()){
            getSSCC(_code)
        }else{
            Realm.getInstanceAsync(realm.configuration, object : Realm.Callback() {
                override fun onSuccess(r: Realm) {
                    Log.e("Jepicame idJehdaer","=>"+idHeader)
                    Log.e("Jepicame idJehdaer sscc","=>"+_code)

                    val documentBody = r.where(StockTransferBody::class.java)
                        .equalTo("_StockTransferHeader", ObjectId(idHeader) )
                        .findAll()

                    documentBody?.let { data: RealmResults<StockTransferBody> ->

                        val listBody: List<StockTransferBody> = data.subList(0, data.size)
                        val listCount: MutableList<Int> = mutableListOf()


                        listBody.forEach{ body->
                            val subDetail = r.where(StockTransferSubBody::class.java)
                                .equalTo("_StockTransferBody", body._id)
                                .equalTo("Delete", "N")
                                .equalTo("Sscc", _code)
                                .findAll()

                            Log.e("Jepicame idJehdaer ${body._id} size es","=>"+subDetail.size)

                            if(subDetail.size>0){
                                listCount.add(subDetail.size)
                                return@forEach
                            }
                        }

                        if(listCount.size>0){
                            _articulo.value= ItemsResponse(
                                items= emptyList(),
                                type=TypeCode.SSCC,
                                status="El palet con SSCC $_code, ya existe en esta ficha."
                            )
                        }else{
                            getSSCC(_code)
                        }
                    }
                }
                override fun onError(exception: Throwable) {
                    Log.e("JEPICAME","=>"+exception.message)
                    _articulo.value= ItemsResponse(items=emptyList(),status=" Ocurrio un error al intentar verificar si ya existe el palet en el documento:\n${exception.message}",type=TypeCode.QR)
                }
            })
        }
    }

    private fun realmGetItem(idHeader:String="",itemCodeNew:String="",lote:String="",quantity:Double=0.0){
        Realm.getInstanceAsync(configPublic, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {
                if(idHeader.isNotEmpty()){
                    val documentBody = r.where(StockTransferBody::class.java)
                        .equalTo("_StockTransferHeader", ObjectId(idHeader))
                        .equalTo("ItemCode",itemCodeNew)

                    if(documentBody!=null){
                        val article = r.where(Items::class.java)
                            .equalTo("ItemCode",itemCodeNew)
                            .findFirst()

                        if (article != null) {
                            _articulo.value= ItemsResponse(
                                items= listOf(ItemResponse(item=article,lote=lote, quantity = quantity)),
                                type=TypeCode.QR,
                                status="ok"
                            )
                        }else{
                            _articulo.value= ItemsResponse(
                                items= emptyList(),
                                type=TypeCode.QR,
                                status="El artículo $itemCodeNew, no se encuentra en el maestro de artículos."
                            )
                        }
                    }else{
                        _articulo.value= ItemsResponse(
                            items= emptyList(),
                            type=TypeCode.QR,
                            status="El artículo $itemCodeNew, no existe en el documento actual."
                        )
                    }
                }
                else{
                    val article = r.where(Items::class.java)
                        .equalTo("ItemCode",itemCodeNew)
                        .or()
                        .equalTo("Sku",itemCodeNew)
                        .findFirst()

                    if (article != null) {
                        _articulo.value= ItemsResponse(
                            items= listOf(ItemResponse(item=article,lote=lote, quantity = quantity)),
                            type=TypeCode.QR,
                            status="ok"
                        )
                    }else{
                        _articulo.value=ItemsResponse(items= emptyList(),status="vacio",type=TypeCode.QR)
                    }
                }
            }
            override fun onError(exception: Throwable) {
                _articulo.value= ItemsResponse(items=emptyList(),status=" ${exception.message}",type=TypeCode.QR)
            }
        })
    }

     fun getMasterDataArticle(){

         Realm.getInstanceAsync(configPublic, object : Realm.Callback() {
             override fun onSuccess(r: Realm) {
                 val articulos = r.where(Items::class.java).sort("ItemCode", Sort.ASCENDING).findAll()

                 articulos?.let { data:RealmResults<Items> ->

                     val noteTemp:List<Items> = data.subList(0, data.size)
                     _articulos.value =  ListItems(listArticle=noteTemp,status="ok", fechaDescarga = Date())

                 }

                 //r.close()
             }
             override fun onError(exception: Throwable) {
                 _articulos.value = ListItems(listArticle= emptyList(),status="error",fechaDescarga = Date())
             }
         })
     }

/*
    fun updateNote(id: String, noteTitle: String, noteDesc: String) {
        val target = realm.where(Note::class.java)
            .equalTo("id", id)
            .findFirst()

        realm.executeTransaction {
          //  target?.title = noteTitle
            //target?.description = noteDesc
            realm.insertOrUpdate(target)
        }
    }

    fun deleteNote(id: String) {
        val notes = realm.where(Note::class.java)
            .equalTo("id", id)
            .findFirst()

        realm.executeTransaction {
            notes!!.deleteFromRealm()
        }
    }

    fun deleteAllNotes() {
        realm.executeTransaction { r: Realm ->
            r.delete(Note::class.java)
        }
    }
*/
}