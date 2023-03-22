package com.vistony.wms.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vistony.wms.model.*
import com.vistony.wms.num.TypeCode
import com.vistony.wms.screen.getUIStringTimeStampWithDate
import com.vistony.wms.util.isNumeric
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.syncSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.bson.types.ObjectId
import java.util.*

class StockTransferBodyViewModel(idMerchandise:String): ViewModel() {

    private var idMerchandise:String= idMerchandise
    private var realm: Realm = Realm.getInstance(Realm.getDefaultConfiguration())

    class StockTransferBodyViewModelModelFactory(private var idMerchandise:String): ViewModelProvider.Factory {
        @Suppress("UNSCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StockTransferBodyViewModel(idMerchandise) as T
        }
    }

    private val _merchandiseBody = MutableStateFlow(StockTransferBodyResponse())
    val merchandiseBody: StateFlow<StockTransferBodyResponse> get() = _merchandiseBody

    private val _documentBody = MutableStateFlow(StockTransferBodyAndSubBody())
    val documentBody: StateFlow<StockTransferBodyAndSubBody> get() = _documentBody

    private val _stockTransferBodyAndSubBodyResponse =  MutableStateFlow<StockTranfBySRspnsList>(StockTranfBySRspnsList())
    val stockTransferBodyAndSubBodyResponse: StateFlow<StockTranfBySRspnsList> get() = _stockTransferBodyAndSubBodyResponse

    private val _destine = MutableStateFlow(String())
    val destine: StateFlow<String> get() = _destine

    init{
        if(idMerchandise!="flag"){
            getBodyList()
        }
    }

    fun resetBodyState(){
        _merchandiseBody.value= StockTransferBodyResponse()
    }

    fun resetDestineState(){
        _destine.value= ""
    }

    fun resetDocumentBody(){
        _documentBody.value=StockTransferBodyAndSubBody(body= StockTransferBody(),subBody=emptyList(),status="")
    }

    fun resetBodyAndSubBodyState(){
        _stockTransferBodyAndSubBodyResponse.value= StockTranfBySRspnsList()
    }

    fun getBodyList(){
        _merchandiseBody.value= StockTransferBodyResponse(emptyList(),"cargando")

        Realm.getInstanceAsync(realm.configuration, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {

                val inventory = r.where(StockTransferHeader::class.java)
                    .equalTo("_id", ObjectId(idMerchandise))

                    .findFirst()

                val count = r.where(StockTransferBody::class.java)
                    .equalTo("_StockTransferHeader", ObjectId(idMerchandise))
                    .sort("UpdateAt", Sort.DESCENDING)
                    .findAll()

                count?.let { data: RealmResults<StockTransferBody> ->

                    val countTemp:List<StockTransferBody> = data.subList(0, data.size)

                    val list: MutableList<StockTransferBodyAndSubBody> = mutableListOf()

                    countTemp.forEach{ body->
                        val subDetail = r.where(StockTransferSubBody::class.java)
                            .equalTo("_StockTransferBody", body._id)
                            .equalTo("Delete", "N")
                            .sort("UpdateAt", Sort.DESCENDING)
                            .findAll()

                        if(subDetail.size>0){
                            val tempSubDetail:List<StockTransferSubBody> = subDetail.subList(0, subDetail.size)

                            list.add(StockTransferBodyAndSubBody(body,tempSubDetail))
                        }else{
                            list.add(StockTransferBodyAndSubBody(body, emptyList()))
                        }
                    }

                    _merchandiseBody.value= StockTransferBodyResponse(
                        trasnferenceStatus=inventory?.Status!!,
                        stockTransferBody =list,
                        status="ok-data",
                        wareHouseDestine = inventory?.WarehouseDestine!!,
                        wareHouseOrigin = inventory.WarehouseOrigin,
                        createAt = inventory.CreateAt.getUIStringTimeStampWithDate()
                    )
                }
            }
            override fun onError(exception: Throwable) {
                _merchandiseBody.value= StockTransferBodyResponse(emptyList(),"${exception.message}")
            }
        })
    }

    fun addDestine(stockTransferPayloadVal:List<StockTransferPayloadVal>){

        _destine.value="cargando"

        realm.executeTransactionAsync { r: Realm ->

            stockTransferPayloadVal.forEach {  stockTransferPayloadVal ->

                val subBody =r.where(StockTransferSubBody::class.java)
                    .equalTo("_StockTransferBody", stockTransferPayloadVal.idBody)
                    .equalTo("Batch", stockTransferPayloadVal.batch)
                    .equalTo("Sscc", stockTransferPayloadVal.sscc)
                    .equalTo("Delete", "N")
                    .findAll()

                if(subBody!=null){

                    subBody.forEach { sBody ->

                        val secondFilter = stockTransferPayloadVal.origin.firstOrNull { it.locationName == sBody.LocationName }

                        if(secondFilter!=null){
                            Log.e("JEPICAMR","IS NOT NUL 128")
                            if(sBody.Destine.isEmpty()){
                                Log.e("JEPICAMR","IS NOT NUL 130 "+sBody.Destine.size)
                                sBody.Destine.add(StockTransferSubBody_Destine(
                                    LocationName = stockTransferPayloadVal.destine.text,
                                    LocationCode = ""+stockTransferPayloadVal.destine.id,
                                    Quantity = secondFilter.quantityUsed //stockTransferPayloadVal.quantity
                                ))
                            }else{
                                val searchDestine=sBody.Destine.firstOrNull { it.LocationName ==stockTransferPayloadVal.destine.text }

                                if(searchDestine!=null){
                                    Log.e("JEPICAMR","IS NOT NUL 140")
                                    searchDestine.Quantity= searchDestine.Quantity?.plus(secondFilter.quantityUsed) //stockTransferPayloadVal.quantity)
                                }else{

                                    Log.e("JEPICAMR","IS NOT NUL 143")
                                    sBody.Destine.add(StockTransferSubBody_Destine(
                                        LocationName = stockTransferPayloadVal.destine.text,
                                        LocationCode = ""+stockTransferPayloadVal.destine.id,
                                        Quantity = secondFilter.quantityUsed//stockTransferPayloadVal.quantity
                                    ))
                                }
                            }

                            if(sBody.Quantity==sBody.Destine.sum("Quantity")){
                                sBody.Status="Completo"
                            }

                            _destine.value="ok"
                        }else{
                            Log.e("JEPICAME","ESTE VALOR TIENE NULL "+sBody.LocationName)
                        }
                    }
                }else{
                    if(stockTransferPayloadVal.origin.isEmpty()){
                        _destine.value="Es necesario selecionar una ubicación origen."
                    }else{
                        _destine.value="El Lote ${stockTransferPayloadVal.batch}"
                    }
                }

            }




        }
    }

    fun getBodyAndSubBody(zebra:zebraPayload){

        _stockTransferBodyAndSubBodyResponse.value= StockTranfBySRspnsList(emptyList(),status="cargando")

        val count:Int = zebra.Payload.split("|").size

        var lote:String = ""
        var itemCodeNew:String = ""

        when(count){
            0,1->{
                itemCodeNew=zebra.Payload.split("|")[0]
            }
            2->{
                itemCodeNew=zebra.Payload.split("|")[0]
                lote=zebra.Payload.split("|")[1]
            }else->{
                itemCodeNew=zebra.Payload.split("|")[0]
                lote=zebra.Payload.split("|")[1]
            }
        }

        Realm.getInstanceAsync(realm.configuration, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {
                if(zebra.Payload.length==20 && isNumeric(zebra.Payload)){

                    val body = r.where(StockTransferBody::class.java)
                        .equalTo("_StockTransferHeader", ObjectId(idMerchandise))
                        .findAll()

                    if(body!=null && body.isNotEmpty()){

                        val tempListBody:List<StockTransferBody> = body.subList(0, body.size)
                        val tempListResponse = listOf<StockTransferBodyAndSubBodyResponse>().toMutableList()

                        tempListBody.forEach { stockTransferBody ->

                            val subBody = r.where(StockTransferSubBody::class.java)
                                .equalTo("Delete", "N")
                                .equalTo("Sscc", zebra.Payload.substring(2))
                                .equalTo("_StockTransferBody", stockTransferBody._id)
                                .findAll()

                            if(subBody!=null && subBody.isNotEmpty()){
                                val countTemp:List<StockTransferSubBody> = subBody.subList(0, subBody.size)

                                /*if(countTemp.sumOf{ it.Quantity } <= countTemp.sumOf{ it.Destine.sum("Quantity").toDouble() }){
                                    tempListResponse.add(
                                        StockTransferBodyAndSubBodyResponse(
                                            stockTransferBody=stockTransferBody,
                                            stockTransferSubBody=countTemp,
                                            message="El producto ${stockTransferBody.ItemCode} con lote ${stockTransferBandSRpsValue.stockTransferSubBody[0].Batch}, no tiene stock pendiente de ubicar.",
                                            quantityDestine=countTemp.sumOf{ it.Destine.sum("Quantity").toDouble() }
                                        )
                                    )
                                }else{*/
                                    tempListResponse.add( StockTransferBodyAndSubBodyResponse(stockTransferBody,countTemp,quantityDestine=countTemp.sumOf{ it.Destine.sum("Quantity").toDouble() }) )
                                //}
                            }


                        }

                        if(tempListResponse.isEmpty()){
                            _stockTransferBodyAndSubBodyResponse.value= StockTranfBySRspnsList(tempListResponse,status="El SSCC del palet escaneado no pertenece a este documento")
                        }else{
                            _stockTransferBodyAndSubBodyResponse.value= StockTranfBySRspnsList(tempListResponse,status="ok",TypeCode.SSCC)
                        }
                    }else{
                        _stockTransferBodyAndSubBodyResponse.value=StockTranfBySRspnsList(emptyList(),status="Inesperadamente este documento no tiene detalle")
                    }
                }
                else{
                    val body = r.where(StockTransferBody::class.java)
                        .equalTo("ItemCode", itemCodeNew)
                        .equalTo("_StockTransferHeader", ObjectId(idMerchandise))
                        .findFirst()

                    if(body!=null){

                        val subBody = r.where(StockTransferSubBody::class.java)
                            .equalTo("Delete", "N")
                            .equalTo("Batch", lote)
                            .equalTo("Sscc", "")
                            .equalTo("_StockTransferBody", body._id)
                            .findAll()

                        if(subBody!=null && subBody.isNotEmpty()){

                            val countTemp:List<StockTransferSubBody> = subBody.subList(0, subBody.size)
                            _stockTransferBodyAndSubBodyResponse.value= StockTranfBySRspnsList(
                                response=listOf(StockTransferBodyAndSubBodyResponse(body,countTemp,quantityDestine=countTemp.sumOf{ it.Destine.sum("Quantity").toDouble() })),
                                status="ok",
                                type=TypeCode.QR
                            )

                        }else{
                            _stockTransferBodyAndSubBodyResponse.value= StockTranfBySRspnsList(emptyList(),status="El lote del producto escaneado no pertenece a este documento o esta dentro de un palet")
                        }
                    }else{
                        _stockTransferBodyAndSubBodyResponse.value= StockTranfBySRspnsList(emptyList(),status="El producto escaneado no pertenece a este documento")
                    }
                }
            }
            override fun onError(exception: Throwable){
                _stockTransferBodyAndSubBodyResponse.value= StockTranfBySRspnsList(emptyList(),status="${exception.message}")
            }
        })

    }

    fun getArticleFromBody(value:String){

        _documentBody.value=StockTransferBodyAndSubBody(body= StockTransferBody(),subBody=emptyList(),status="cargando")

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

        Realm.getInstanceAsync(realm.configuration, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {

                val documentBody = r.where(StockTransferBody::class.java)
                    .equalTo("_StockTransferHeader", ObjectId(idMerchandise))
                    .equalTo("ItemCode",itemCodeNew)
                    .findFirst()

                if(documentBody!=null){
                    _documentBody.value=StockTransferBodyAndSubBody(body= documentBody,subBody=emptyList(),status="ok")
                }else{
                    _documentBody.value=StockTransferBodyAndSubBody(body= StockTransferBody(),subBody=emptyList(),status="El artículo $itemCodeNew, no existe en el documento actual.")
                }
            }
            override fun onError(exception: Throwable) {
                _documentBody.value=StockTransferBodyAndSubBody(body= StockTransferBody(),subBody=emptyList(),status=" ${exception.message}")
            }
        })
    }

    fun insertData(body: List<StockTransferBodyPayload>,objType:Int){

        Log.e("JEPICAME","INSERTAR DATAAA")

        _merchandiseBody.value= StockTransferBodyResponse(emptyList(),"cargando")

        realm.executeTransactionAsync { r: Realm ->


            body.map { body->

                Log.e("JEPICAME","INSERTAR DATAAA")

            val Article = r.where(StockTransferBody::class.java)
                .equalTo("ItemCode",body.ItemCode)
                .equalTo("_StockTransferHeader", ObjectId(idMerchandise))
                .findFirst()

            if(Article == null){

                val obj = r.createObject(StockTransferBody::class.java, ObjectId().toHexString())

                obj.ItemCode=body.ItemCode
                obj.ItemName=body.ItemName
                obj.TotalQuantity=body.Quantity
                obj._StockTransferHeader= ObjectId(idMerchandise)
                obj.Realm_Id=realm.syncSession.user.id

                r.insert(obj)

                val recoveryArticle=r.copyToRealmOrUpdate(obj)

                if(recoveryArticle!=null ){

                    val ArticleDetail = r.where(StockTransferSubBody::class.java)

                        .equalTo("Batch",body.Batch)
                        .equalTo("LocationName",body.LocationName)
                        .equalTo("Sscc",body.Sscc)
                        .equalTo("Delete","N")
                        .equalTo("_StockTransferBody",recoveryArticle._id)
                        .findFirst()

                    if(ArticleDetail == null){

                        Log.e("JEPICAME","22PERCONA-->ENTRO A CARGAR SUBDETALLE")

                        val obit = r.createObject(StockTransferSubBody::class.java, ObjectId().toHexString())

                        obit.Quantity=body.Quantity
                        obit.LocationName=body.LocationName
                        obit.LocationCode=body.LocationCode
                        obit.Batch=body.Batch
                        obit.Sscc=body.Sscc
                        obit._StockTransferBody= recoveryArticle._id
                        obit.Realm_Id=realm.syncSession.user.id

                        r.insert(obit)

                    }else{
                        ArticleDetail.Quantity=ArticleDetail.Quantity+1
                        ArticleDetail.UpdateAt= Date()
                        r.insertOrUpdate(ArticleDetail)
                    }
                }else{
                    _merchandiseBody.value= StockTransferBodyResponse(emptyList(),"error")
                }
            }else{


                /*
                if(objType ==22){ //SOLO PARA ORDEN DE COMPRA
                    if(Article.TotalQuantity+body.Quantity>Article.Quantity){
                        _merchandiseBody.value= StockTransferBodyResponse(emptyList(),"Ocurrio un error, la cantidad total ingresada no puede superar la asignada en la OC.")
                        Log.e("JEPICAME","OCURRIO UN ERROR YNO SE PINTA tipo 2 mayor AA")
                    }else{
                        Log.e("JEPICAME","OCURRIO UN ERROR YNO SE PINTA tipo 2 menor AA")

                        Article.TotalQuantity= Article.TotalQuantity+body.Quantity
                        Article.UpdateAt= Date()
                        r.insertOrUpdate(Article)

                        val ArticleDetail = r.where(StockTransferSubBody::class.java)
                            .equalTo("Batch",body.Batch)
                            .equalTo("LocationName",body.LocationName)
                            .equalTo("Delete","N")
                            .equalTo("_StockTransferBody",Article._id)
                            .findFirst()

                        if(ArticleDetail == null){

                            Log.e("JEPICAME","2PERCONA-->ENTRO A CARGAR SUBDETALLE")

                            val sub = r.createObject(StockTransferSubBody::class.java, ObjectId().toHexString())

                            sub.Quantity=body.Quantity
                            sub.LocationName=body.LocationName
                            sub.LocationCode=body.LocationCode
                            sub.Batch=body.Batch
                            sub._StockTransferBody= Article._id
                            sub.Realm_Id=realm.syncSession.user.id

                            r.insert(sub)

                        }else{

                            if(body.Quantity!=0.0){
                                ArticleDetail.Quantity=ArticleDetail.Quantity+body.Quantity
                            }else{
                                ArticleDetail.Quantity=ArticleDetail.Quantity+1
                            }

                            ArticleDetail.UpdateAt=Date()

                            r.insertOrUpdate(ArticleDetail)
                        }
                    }
                }else{
                    */

                    Article.TotalQuantity= Article.TotalQuantity+body.Quantity
                    Article.UpdateAt= Date()
                    r.insertOrUpdate(Article)

                    val ArticleDetail = r.where(StockTransferSubBody::class.java)
                        .equalTo("Batch",body.Batch)
                        .equalTo("LocationName",body.LocationName)
                        .equalTo("Delete","N")
                        .equalTo("Sscc",body.Sscc)
                        .equalTo("_StockTransferBody",Article._id)
                        .findFirst()

                    if(ArticleDetail == null){

                        Log.e("JEPICAME","2PERCONA-->ENTRO A CARGAR SUBDETALLE")

                        val sub = r.createObject(StockTransferSubBody::class.java, ObjectId().toHexString())

                        sub.Quantity=body.Quantity
                        sub.LocationName=body.LocationName
                        sub.LocationCode=body.LocationCode
                        sub.Sscc=body.Sscc
                        sub.Batch=body.Batch
                        sub._StockTransferBody= Article._id
                        sub.Realm_Id=realm.syncSession.user.id

                        r.insert(sub)

                    }else{

                        if(body.Quantity!=0.0){
                            ArticleDetail.Quantity=ArticleDetail.Quantity+body.Quantity
                        }else{
                            ArticleDetail.Quantity=ArticleDetail.Quantity+1
                        }

                        ArticleDetail.UpdateAt=Date()

                        r.insertOrUpdate(ArticleDetail)
                    }

               // }
            }
            }

            _merchandiseBody.value= StockTransferBodyResponse(emptyList(),"ok")
        }
    }

    /*fun updateStatus(idBody: ObjectId,newStatus:String){

        _merchandiseBody.value= StockTransferBodyResponse(emptyList(),"cargando")

        realm.executeTransactionAsync { r: Realm ->

            val body: StockTransferBody? =r.where(StockTransferBody::class.java)
                .equalTo("_id", idBody)
                //.equalTo("Status",lineUpdate.location)
                .findFirst()

            body?.Status=newStatus
            body?.UpdateAt= Date()

            r.insertOrUpdate(body)

            _merchandiseBody.value= StockTransferBodyResponse(emptyList(),"ok")
        }
    }*/

    fun deleteData(idItem: ObjectId){

        _merchandiseBody.value= StockTransferBodyResponse(emptyList(),"cargando")

        realm.executeTransactionAsync { r: Realm ->

            val body: StockTransferBody? =r.where(StockTransferBody::class.java)
                .equalTo("_id", idItem)
                .findFirst()

            body?.deleteFromRealm()

            _merchandiseBody.value= StockTransferBodyResponse(emptyList(),"ok")
        }
    }


}