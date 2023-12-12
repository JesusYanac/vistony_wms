package com.vistony.wms.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vistony.wms.model.*
import com.vistony.wms.num.TypeCode
import com.vistony.wms.screen.getUIStringTimeStampWithDate
import com.vistony.wms.util.isNumeric
import com.vistony.wms.util.parseValue
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
                        createAt = inventory.CreateAt.getUIStringTimeStampWithDate(),
                        //DocDate = inventory.DocDate.getUIStringTimeStampWithDate(),
                    )
                }
            }
            override fun onError(exception: Throwable) {
                _merchandiseBody.value= StockTransferBodyResponse(emptyList(),"${exception.message}")
            }
        })
    }

    fun addDestine(stockTransferPayloadVal:List<StockTransferPayloadVal>,objType: Int){

        Log.e("REOS","StockTransferBodyViewModel-addDestine-stockTransferPayloadVal: "+stockTransferPayloadVal.toString())

        _destine.value="cargando"

        realm.executeTransactionAsync { r: Realm ->

            stockTransferPayloadVal.forEach {  stockTransferPayloadVal ->

                val subBody =r.where(StockTransferSubBody::class.java)
                    .equalTo("_StockTransferBody", stockTransferPayloadVal.idBody)
                    .equalTo("Batch", stockTransferPayloadVal.batch)
                    //.equalTo("Sscc", stockTransferPayloadVal.sscc)
                    .equalTo("Delete", "N")
                    .findAll()
                Log.e("REOS","StockTransferBodyViewModel-addDestine-stockTransferPayloadVal.idBody: "+stockTransferPayloadVal.idBody)
                Log.e("REOS","StockTransferBodyViewModel-addDestine-stockTransferPayloadVal.batch: "+stockTransferPayloadVal.batch)
                Log.e("REOS","StockTransferBodyViewModel-addDestine-stockTransferPayloadVal.sscc: "+stockTransferPayloadVal.sscc)
                Log.e("REOS","StockTransferBodyViewModel-addDestine-subBody: "+subBody.toString())
                if(subBody!=null){

                    subBody.forEach { sBody ->

                        val secondFilter = stockTransferPayloadVal.origin.firstOrNull { it.locationName == sBody.LocationName }
                        //Log.e("REOS","StockTransferBodyViewModel-addDestine-secondFilter.quantityNow: "+ secondFilter!!.quantityNow)
                        //Log.e("REOS","StockTransferBodyViewModel-addDestine-secondFilter.quantityUsed: "+secondFilter!!.quantityUsed)
                        //Log.e("REOS","StockTransferBodyViewModel-addDestine-secondFilter.quantityAvailable: "+secondFilter!!.quantityAvailable)


                        if(secondFilter!=null){
                            Log.e("JEPICAMR","IS NOT NUL 128")
                            if(sBody.Destine.isEmpty()){
                                Log.e("JEPICAMR","IS NOT NUL 130 "+sBody.Destine.size)
                                /*if(objType==67)
                                {
                                    sBody.Destine.add(StockTransferSubBody_Destine(
                                        LocationName = stockTransferPayloadVal.destine.text,
                                        LocationCode = ""+stockTransferPayloadVal.destine.id,
                                        Quantity = sBody.Quantity //stockTransferPayloadVal.quantity
                                    ))
                                }else{
                                    sBody.Destine.add(StockTransferSubBody_Destine(
                                        LocationName = stockTransferPayloadVal.destine.text,
                                        LocationCode = ""+stockTransferPayloadVal.destine.id,
                                        Quantity = secondFilter.quantityUsed //stockTransferPayloadVal.quantity
                                    ))
                                }*/
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
                                    /*if(objType==67)
                                    {
                                        sBody.Destine.add(StockTransferSubBody_Destine(
                                            LocationName = stockTransferPayloadVal.destine.text,
                                            LocationCode = ""+stockTransferPayloadVal.destine.id,
                                            Quantity = sBody.Quantity//stockTransferPayloadVal.quantity
                                        ))
                                    }else
                                    {
                                        sBody.Destine.add(StockTransferSubBody_Destine(
                                            LocationName = stockTransferPayloadVal.destine.text,
                                            LocationCode = ""+stockTransferPayloadVal.destine.id,
                                            Quantity = secondFilter.quantityUsed//stockTransferPayloadVal.quantity
                                        ))
                                    }*/
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
            Log.e("REOS","StockTransferBodyViewModel-addDestine-_destine.value: "+_destine.value.toString())

        }
    }


    fun getBodyAndSubBody(zebra:zebraPayload,objType: Int){

        _stockTransferBodyAndSubBodyResponse.value= StockTranfBySRspnsList(emptyList(),status="cargando")
        Log.e("REOS","StockTransferBodyViewModel-getBodyAndSubBody-zebra: "+zebra)
        Realm.getInstanceAsync(realm.configuration, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {
                Log.e("REOS","StockTransferBodyViewModel-getBodyAndSubBody-zebra.Payload.length: "+zebra.Payload.length)
                Log.e("REOS","StockTransferBodyViewModel-getBodyAndSubBody-isNumeric(zebra.Payload): "+isNumeric(zebra.Payload))
                if(zebra.Payload.length==20 && isNumeric(zebra.Payload)){

                    val body = r.where(StockTransferBody::class.java)
                        .equalTo("_StockTransferHeader", ObjectId(idMerchandise))
                        .findAll()
                    Log.e("REOS","StockTransferBodyViewModel-getBodyAndSubBody-body.asJSON(): "+body.asJSON())
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
                                tempListResponse.add( StockTransferBodyAndSubBodyResponse(stockTransferBody,countTemp,quantityDestine=countTemp.sumOf{ it.Destine.sum("Quantity").toDouble() }) )
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
                    Log.e("REOS","StockTransferBodyViewModel-getBodyAndSubBody-else.zebra.Payload: "+zebra.Payload)
                    var itemCodeNew=""
                    var lote=""
                    var locationName =""
                    val elements = zebra.Payload.split("|", limit = 3)
                    for (i in 0 until elements.size) {
                        when (i) {
                            0 -> {
                                itemCodeNew = elements.get(i)
                            }
                            1 -> {
                                lote = elements.get(i)
                            }
                            2 -> {
                                locationName = elements.get(i)
                            }
                        }
                    }
                    if(objType in setOf(67,6701,1701)&&itemCodeNew.length!=20)
                    {

                        //val (itemCodeNew, lote,locationName) = parseValue(zebra.Payload)
                        Log.e("REOS","StockTransferBodyViewModel-getBodyAndSubBody-else.itemCodeNew: "+itemCodeNew)
                        Log.e("REOS","StockTransferBodyViewModel-getBodyAndSubBody-else.lote "+lote)
                        Log.e("REOS","StockTransferBodyViewModel-getBodyAndSubBody-else.idMerchandise: "+idMerchandise)
                        val body = r.where(StockTransferBody::class.java)
                            .equalTo("ItemCode", itemCodeNew)
                            /*.or()
                            .equalTo("Sku",itemCodeNew)*/
                            .equalTo("_StockTransferHeader", ObjectId(idMerchandise))
                            .findFirst()
                        Log.e("REOS","StockTransferBodyViewModel-getBodyAndSubBody-else.body: "+body.toString())
                        if(body!=null){

                            val subBody: RealmResults<StockTransferSubBody> = if (lote.isEmpty()) {
                                r.where(StockTransferSubBody::class.java)
                                    .equalTo("Delete", "N")
                                    .equalTo("Sscc", "")
                                    .equalTo("_StockTransferBody", body._id)
                                    .findAll()
                            } else {
                                Log.e("REOS","StockTransferBodyViewModel-getBodyAndSubBody-else-else-lote: "+lote)
                                Log.e("REOS","StockTransferBodyViewModel-getBodyAndSubBody-else-else-body._id: "+body._id)
                                Log.e("REOS","StockTransferBodyViewModel-getBodyAndSubBody-else-else-locationName "+locationName)
                                r.where(StockTransferSubBody::class.java)
                                    .equalTo("Delete", "N")
                                    .equalTo("Batch", lote)
                                    //.equalTo("Sscc", "")
                                    .equalTo("LocationName", locationName)
                                    .equalTo("_StockTransferBody", body._id)
                                    .findAll()
                            }
                            Log.e("REOS","StockTransferBodyViewModel-getBodyAndSubBody-else-else-subBody: "+subBody.toString())
                            if(subBody!=null && subBody.isNotEmpty()){

                                val countTemp:List<StockTransferSubBody> = subBody.subList(0, subBody.size)
                                Log.e("REOS","StockTransferBodyViewModel-getBodyAndSubBody-else-else-if-countTemp: "+countTemp)
                                _stockTransferBodyAndSubBodyResponse.value= StockTranfBySRspnsList(
                                    response=listOf(StockTransferBodyAndSubBodyResponse(body,countTemp,quantityDestine=countTemp.sumOf{ it.Destine.sum("Quantity").toDouble() })),
                                    status="ok",
                                    type=TypeCode.QR
                                )
                                Log.e("REOS","StockTransferBodyViewModel-getBodyAndSubBody-else-else-if-_stockTransferBodyAndSubBodyResponse.value: "+_stockTransferBodyAndSubBodyResponse.value)

                            }else{
                                _stockTransferBodyAndSubBodyResponse.value= StockTranfBySRspnsList(emptyList(),status="El lote del producto escaneado no pertenece a este documento o esta dentro de un palet")
                            }
                        }else{
                            _stockTransferBodyAndSubBodyResponse.value= StockTranfBySRspnsList(emptyList(),status="El producto escaneado no pertenece a este documento")
                        }
                    }else {
                        val (itemCodeNew, lote) = parseValue(zebra.Payload)
                        Log.e("REOS","StockTransferBodyViewModel-getBodyAndSubBody-else.itemCodeNew: "+itemCodeNew)
                        Log.e("REOS","StockTransferBodyViewModel-getBodyAndSubBody-else.lote "+lote)
                        Log.e("REOS","StockTransferBodyViewModel-getBodyAndSubBody-else.idMerchandise: "+idMerchandise)
                        val body = r.where(StockTransferBody::class.java)
                            .equalTo("ItemCode", itemCodeNew)
                            /*.or()
                            .equalTo("Sku",itemCodeNew)*/
                            .equalTo("_StockTransferHeader", ObjectId(idMerchandise))
                            .findFirst()
                        Log.e("REOS","StockTransferBodyViewModel-getBodyAndSubBody-else.body: "+body.toString())
                        if(body!=null){

                            val subBody: RealmResults<StockTransferSubBody> = if (lote.isEmpty()) {
                                r.where(StockTransferSubBody::class.java)
                                    .equalTo("Delete", "N")
                                    .equalTo("Sscc", "")
                                    .equalTo("_StockTransferBody", body._id)
                                    .findAll()
                            } else {
                                Log.e("REOS","StockTransferBodyViewModel-getBodyAndSubBody-else-else-lote: "+lote)
                                Log.e("REOS","StockTransferBodyViewModel-getBodyAndSubBody-else-else-body._id: "+body._id)
                                r.where(StockTransferSubBody::class.java)
                                    .equalTo("Delete", "N")
                                    .equalTo("Batch", lote)
                                    .equalTo("Sscc", "")
                                    .equalTo("_StockTransferBody", body._id)
                                    .findAll()
                            }

                            if(subBody!=null && subBody.isNotEmpty()){

                                val countTemp:List<StockTransferSubBody> = subBody.subList(0, subBody.size)
                                Log.e("REOS","StockTransferBodyViewModel-getBodyAndSubBody-else-else-if-countTemp: "+countTemp)
                                _stockTransferBodyAndSubBodyResponse.value= StockTranfBySRspnsList(
                                    response=listOf(StockTransferBodyAndSubBodyResponse(body,countTemp,quantityDestine=countTemp.sumOf{ it.Destine.sum("Quantity").toDouble() })),
                                    status="ok",
                                    type=TypeCode.QR
                                )
                                Log.e("REOS","StockTransferBodyViewModel-getBodyAndSubBody-else-else-if-_stockTransferBodyAndSubBodyResponse.value: "+_stockTransferBodyAndSubBodyResponse.value)

                            }else{
                                _stockTransferBodyAndSubBodyResponse.value= StockTranfBySRspnsList(emptyList(),status="El lote del producto escaneado no pertenece a este documento o esta dentro de un palet")
                            }
                        }else{
                            _stockTransferBodyAndSubBodyResponse.value= StockTranfBySRspnsList(emptyList(),status="El producto escaneado no pertenece a este documento")
                        }
                    }


                }
                Log.e("REOS","StockTransferBodyViewModel-getBodyAndSubBody-else-_stockTransferBodyAndSubBodyResponse.value: "+_stockTransferBodyAndSubBodyResponse.value.status)
                Log.e("REOS","StockTransferBodyViewModel-getBodyAndSubBody-else-_stockTransferBodyAndSubBodyResponse.value.response.toString(): "+_stockTransferBodyAndSubBodyResponse.value.response.toString())
            }
            override fun onError(exception: Throwable){
                _stockTransferBodyAndSubBodyResponse.value= StockTranfBySRspnsList(emptyList(),status="${exception.message}")
                Log.e("REOS","StockTransferBodyViewModel-getBodyAndSubBody-onError-_stockTransferBodyAndSubBodyResponse.value: "+_stockTransferBodyAndSubBodyResponse.value.status)
            }
        })

    }

    fun getArticleFromBody(value:String){

        _documentBody.value=StockTransferBodyAndSubBody(body= StockTransferBody(),subBody=emptyList(),status="cargando")

        val (itemCodeNew, lote) = parseValue(value)
        /*val count:Int = value.split("|").size

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
        }*/

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

                Log.e("JEPICAME","INSERTAR DATAAA header document "+idMerchandise)

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
                obj.Sku= body.Sku
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
                        val obit = r.createObject(StockTransferSubBody::class.java, ObjectId().toHexString())
                        obit.Quantity=body.Quantity
                        obit.LocationName=body.LocationName
                        obit.LocationCode=body.LocationCode
                        obit.Batch=body.Batch
                        obit.Sscc=body.Sscc
                        obit._StockTransferBody= recoveryArticle._id
                        obit.Realm_Id=realm.syncSession.user.id
                        /*if(objType==67)
                        {
                            obit.Destine.add(StockTransferSubBody_Destine(LocationCode = body.LocationCode, LocationName = body.LocationName, Quantity = body.Quantity))
                        }*/
                        r.insert(obit)

                    }else{
                        ArticleDetail.Quantity=ArticleDetail.Quantity+1
                        ArticleDetail.UpdateAt= Date()
                        /*if(objType==67)
                        {
                            ArticleDetail.Destine.add(StockTransferSubBody_Destine(LocationCode = body.LocationCode, LocationName = body.LocationName, Quantity = body.Quantity))
                        }*/
                        r.insertOrUpdate(ArticleDetail)
                    }
                }else{
                    _merchandiseBody.value= StockTransferBodyResponse(emptyList(),"error")
                }
            }else{
                if(objType ==22
                    &&objType==18&&objType==1701
                ){ //SOLO PARA ORDEN DE COMPRA
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
                            /*if(objType==67)
                            {
                                ArticleDetail.Destine.add(StockTransferSubBody_Destine(LocationCode = body.LocationCode, LocationName = body.LocationName, Quantity = body.Quantity))
                            }*/
                            r.insertOrUpdate(ArticleDetail)
                        }
                    }
                }else{


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
                        /*if(objType==67)
                        {
                            sub.Destine.add(StockTransferSubBody_Destine(LocationCode = body.LocationCode, LocationName = body.LocationName, Quantity = body.Quantity))
                        }*/
                        r.insert(sub)

                    }else{

                        if(body.Quantity!=0.0){
                            ArticleDetail.Quantity=ArticleDetail.Quantity+body.Quantity
                        }else{
                            ArticleDetail.Quantity=ArticleDetail.Quantity+1
                        }

                        ArticleDetail.UpdateAt=Date()
                        /*if(objType==67)
                        {
                            ArticleDetail.Destine.add(StockTransferSubBody_Destine(LocationCode = body.LocationCode, LocationName = body.LocationName, Quantity = body.Quantity))
                        }*/
                        r.insertOrUpdate(ArticleDetail)
                    }

                }
            }
            }

            _merchandiseBody.value= StockTransferBodyResponse(emptyList(),"ok")
        }
    }

    fun delete(idSubBody:ObjectId){
        realm.executeTransactionAsync { r: Realm ->
            val subBody: StockTransferSubBody? = r.where(StockTransferSubBody::class.java)
                .equalTo("_id", idSubBody)
                .findFirst()

            if (subBody != null) {
                Log.e("JEPICAME", "Id es=>LLEG AQUO")
                val body: StockTransferBody? = r.where(StockTransferBody::class.java)
                    .equalTo("_id", subBody._StockTransferBody)
                    .findFirst()

                if (body != null) {

                    body.TotalQuantity = body.TotalQuantity - subBody.Quantity
                    body.UpdateAt = Date()

                    subBody.deleteFromRealm()
                    if (body.TotalQuantity == 0.0) {
                        body.deleteFromRealm()
                    }
                }
            }
        }

        getBodyList()
    }

}