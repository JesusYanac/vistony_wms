package com.vistony.wms.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vistony.wms.num.TypeCode
import com.vistony.wms.model.*
import com.vistony.wms.util.APIService
import com.vistony.wms.util.isNumeric
import com.vistony.wms.util.parseValue
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
        Log.e("Jepicame idJehdaer","=>"+code)

        viewModelScope.launch(Dispatchers.Default){
            APIService.getInstance()
            .getSscc(code, "NaN")
            .enqueue(object:Callback<Sscc> {
                override fun onResponse(call: Call<Sscc>, response: Response<Sscc>) {
                    Log.e("REOS","ItemsViewModel-getSSCC-call: "+call)
                    Log.e("REOS","ItemsViewModel-getSSCC-response: "+response)
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
                            statusSscc=response.body()?.data!!.status,
                            warehouse = response.body()?.data!!.uWhsCode,
                            //tracking = response.body()?.data!!.TrackingCollection
                        )

                    }else{
                        if(response.code()==424){
                            _articulo.value=ItemsResponse(items= emptyList(),status="El código SSCC $code, no se encontro en SAP",type=TypeCode.SSCC)
                        }else{
                            _articulo.value=ItemsResponse(items= emptyList(),status="El servidor respondio código ${response.code()}",type=TypeCode.SSCC)
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
                .getArticleFromBatchQrEspecial(itemCode)
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
        Log.e("JEPICAME","==> se obtendra artiuclo")
        _articulo.value=ItemsResponse(items= emptyList(),status="cargando",type=TypeCode.QR)
        Log.e("REOS","ItemsViewModel-getArticle-value: "+value)
        Log.e("REOS","ItemsViewModel-getArticle-idHeader: "+idHeader)
        Log.e("REOS","ItemsViewModel-getArticle-typeInventario: "+typeInventario)

        /*if(value.length==20 && isNumeric(value)){
            Log.e("REOS","ItemsViewModel-getArticle-ENTROIFvalue.length==20 && isNumeric(value): ")
            realmVerificationSSCC(
                idHeader=idHeader,
                sscc=value,
                document=typeInventario
            )
        }else{
            Log.e("JEPICAME","==> longitud no es igual a 20")

            var (itemCodeNew, lote) = parseValue(value)
            Log.e("REOS","ItemsViewModel-getArticle-itemCodeNew.length: "+itemCodeNew.length)
            if(itemCodeNew.length in listOf(7,13)){
                Log.e("JEPICAME","==> aqui va entrar 7 or 13")
                realmGetItem(idHeader,itemCodeNew,lote,1.0)
            }else{
                //QR ESPECIAL - bloqueo de presentaciones
                itemCodeNew=removeLastChar(itemCodeNew)
                if(typeInventario.contains("Picking") || typeInventario.contains("Despacho")){
                   getInformationQR(itemCodeNew,lote)
                }else{
                    realmGetItem(idHeader,itemCodeNew,lote,1.0)
                }
            }
        }*/

        var itemCode:String=""
        var batch:String=""
        var itemName:String=""
        val elements = value.split("|", limit = 3)
        for (j in 0 until elements.size) {
            when (j) {
                0 -> {
                    itemCode= elements[j]
                }
                1 -> {
                    batch = elements[j]
                }
            }
        }
        Log.e("jesusdebug3","ItemsViewModel-getArticle-itemCode: "+itemCode)
        Log.e("jesusdebug3","ItemsViewModel-getArticle-batch: "+batch)

        if(itemCode.length==20 && isNumeric(itemCode))
        {
            realmVerificationSSCC(
                idHeader=idHeader,
                sscc=value,
                document=typeInventario
            )
        }
        else {
            realmGetItem(idHeader,itemCode,batch,1.0)
        }


    }

    private fun realmVerificationSSCC(idHeader:String="",sscc:String="",document:String=""){
        Log.e("REOS","ItemsViewModel-realmVerificationSSCC-idHeader: "+idHeader)
        Log.e("REOS","ItemsViewModel-realmVerificationSSCC-sscc: "+sscc)
        Log.e("REOS","ItemsViewModel-realmVerificationSSCC-document: "+document)
        _articulo.value=ItemsResponse(items=emptyList(),status="cargando",type=TypeCode.SSCC)
        val _code:String = sscc.substring(2)

        if(idHeader.isEmpty()){
            getSSCC(_code)
        }else{
            if(document.isNotEmpty()){ // solo cuandoe s inventario devuelve el tipo de inventario otros, logistica inversa, picking sucursales, etc etc et.
                Realm.getInstanceAsync(realm.configuration, object : Realm.Callback() {
                    override fun onSuccess(r: Realm) {

                        val documentCounting = r.where(Counting::class.java)
                            .equalTo("inventoryId", ObjectId(idHeader) )
                            .equalTo("sscc", _code )
                            .findAll()

                        Log.e("JEPICAME","CODE===>"+_code)
                        Log.e("JEPICAME","CODE===>"+idHeader)

                        if(documentCounting.isEmpty()){
                            getSSCC(_code)
                        }else{
                            Log.e("JEPICAME","==>AQUI ENTRO CODIGO EXISTE")
                            _articulo.value= ItemsResponse(
                                items= emptyList(),
                                type=TypeCode.SSCC,
                                status="El palet con SSCC $_code, ya existe en esta ficha."
                            )
                        }
                    }
                    override fun onError(exception: Throwable) {
                        Log.e("JEPICAME","=>"+exception.message)
                        _articulo.value= ItemsResponse(items=emptyList(),status=" Ocurrio un error al intentar verificar si ya existe el palet en el documento:\n${exception.message}",type=TypeCode.QR)
                    }
                })
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
                                Log.e("JEPICAME","==>AQUI ENTRO CODIGO EXISTE VS2")
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
    }

    private fun realmGetItem(idHeader:String="",itemCodeNew:String="",lote:String="",quantity:Double=0.0){
        Log.e("REOS","ItemsViewModel-realmGetItem-idHeader: "+idHeader)
        Log.e("REOS","ItemsViewModel-realmGetItem-itemCodeNew: "+itemCodeNew)
        Log.e("REOS","ItemsViewModel-realmGetItem-lote: "+lote)
        Log.e("REOS","ItemsViewModel-realmGetItem-quantity: "+quantity)


        Realm.getInstanceAsync(configPublic, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {
                val article = r.where(Items::class.java)
                    .equalTo("ItemCode",itemCodeNew)
                    .or()
                    .equalTo("Sku",itemCodeNew)
                    .findFirst()

                if (article != null) {

                    if(idHeader.isNotEmpty()){

                        _articulo.value= ItemsResponse(
                            items= listOf(ItemResponse(item=article,lote=lote, quantity = quantity)),
                            type=TypeCode.QR,
                            status="ok"
                        )
                    }else{
                        _articulo.value= ItemsResponse(
                            items= listOf(ItemResponse(item=article,lote=lote, quantity = quantity)),
                            type=TypeCode.QR,
                            status="ok"
                        )
                    }

                }else{
                    _articulo.value= ItemsResponse(
                        items= emptyList(),
                        type=TypeCode.QR,
                        status="El artículo $itemCodeNew, no se encuentra en el maestro de artículos."
                    )
                }
            }
            override fun onError(exception: Throwable) {
                _articulo.value= ItemsResponse(items=emptyList(),status=" ${exception.message}",type=TypeCode.QR)
            }
        })
    }
    fun realmGetItem2( ): List<Items> {
        val lista = mutableListOf<Items>()
        Realm.getInstanceAsync(configPublic, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {
                val articulos = r.where(Items::class.java).sort("ItemCode", Sort.ASCENDING).findAll()

                articulos?.let { data:RealmResults<Items> ->
                    Log.d("jesusdebug01", "itemResponse: "+data.size)
                    val noteTemp:List<Items> = data.subList(0, data.size)
                    _articulos.value =  ListItems(listArticle=noteTemp,status="ok", fechaDescarga = Date())
                    Log.d("jesusdebug01", "itemResponse: "+noteTemp.size)

                }

                //r.close()
            }
            override fun onError(exception: Throwable) {
                _articulos.value = ListItems(listArticle= emptyList(),status="error",fechaDescarga = Date())
            }
        })
        return lista
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
}