package com.vistony.wms.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.vistony.wms.model.*
import com.vistony.wms.num.TypeCode
import com.vistony.wms.util.APIService
import io.realm.Case
import io.realm.Realm
import io.realm.kotlin.syncSession
import io.realm.mongodb.sync.SyncConfiguration
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class PrintViewModel(): ViewModel() {

    private var realm: Realm = Realm.getInstance(Realm.getDefaultConfiguration())

    private var configPublic =SyncConfiguration
            .Builder(realm.syncSession.user, "public")
            .build()

    private val _print = MutableStateFlow(Print())
    val print: StateFlow<Print> get() = _print

    private val _printList = MutableStateFlow(ListPrint())
    val printList: StateFlow<ListPrint> get() = _printList

    private val _statusPrint = MutableStateFlow("")
    val statusPrint: StateFlow<String> get() = _statusPrint

    private val _terminationReport = MutableStateFlow(TerminationReport())
    val terminationReport: StateFlow<TerminationReport> get() = _terminationReport

    private val _articleList = MutableStateFlow<List<Items?>>(emptyList())
    val articleList: StateFlow<List<Items?>> get() = _articleList

    private val _flagPrint = MutableStateFlow("")
    val flagPrint: StateFlow<String> get() = _flagPrint

    private  val _typeScan = MutableStateFlow("")
    val typeScan: StateFlow<String> get() = _typeScan
    private val _fv = MutableStateFlow("")
    val fv: StateFlow<String> get() = _fv

    class PrintViewModelFactory(): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PrintViewModel() as T
        }
    }

    init {
        listPrinter()
    }

    fun resetItemStatus(){
        _print.value=Print()
    }
    fun setTypeScan(type:String){
        _typeScan.value=type
    }

    fun setPrint(print:Print){
        _print.value=print
    }

    fun resetStatusPrint(){
        _statusPrint.value=""
    }
    fun setStatusPrint(status:String){
        _statusPrint.value=status
    }
    fun resetStatusTerminationReport(){
        _terminationReport.value= TerminationReport(Data="", Status = "")
    }

    fun getArticle(itemCode:String, batch: String, name: String) {
        Log.d("jesusdebug", "ingreso getArticle: $itemCode")
        Log.d("jesusdebug", "print: "+_print.value)
        Realm.getInstanceAsync(configPublic, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {

                val article = r.where(Items::class.java)
                    .equalTo("ItemCode",itemCode)
                    .findFirst()
                Log.d("jesusdebug", "getArticle: $article")

                if (article != null) {
                    _print.value= _print.value.copy(
                        itemUom=article.UoMGroupEntry,
                        itemBatch = batch,
                        itemName = article.ItemName,
                        itemCode = article.ItemCode,
                        status="ok"
                    )
                    viewModelScope.launch(Dispatchers.Default){

                        APIService.getInstance().getPrintData(itemCode = itemCode, lote = batch).enqueue(object :Callback<MyDataPrint> {
                            override fun onResponse(call: Call<MyDataPrint>, response: Response<MyDataPrint>) {
                                Log.e("jesusdebug", "PrintViewModel-sendPrintTerminationReport-call$call")
                                Log.e("jesusdebug",
                                    "PrintViewModel-sendPrintTerminationReport-response$response"
                                )
                                Log.e("jesusdebug", "PrintViewModel-sendPrintTerminationReport-response${response.body()?.Data}")
                                _print.value= _print.value.copy(
                                    itemDate = response.body()?.Data!![0].Fecha,
                                )
                            }
                            override fun onFailure(call: Call<MyDataPrint>, t: Throwable) {
                                TODO("Not yet implemented")
                            }
                        })


                    }
                }else{
                    _print.value= _print.value.copy(
                        status="vacio"
                    )
                }
            }
            override fun onError(exception: Throwable) {
                _print.value= _print.value.copy(
                    status=exception.message.toString())
            }
        }).toString()
    }
    fun getArticleSSCC(itemCode:String, batch: String, date: String) {
        Log.d("jesusdebug", "ingreso getArticle: $itemCode")
        Log.d("jesusdebug", "print: "+_print.value)
        viewModelScope.launch(Dispatchers.Default){

            APIService.getInstance().getPrintData(itemCode = itemCode, lote = batch).enqueue(object :Callback<MyDataPrint> {
                override fun onResponse(call: Call<MyDataPrint>, response: Response<MyDataPrint>) {
                    Log.e("jesusdebug", "PrintViewModel-sendPrintTerminationReport-call$call")
                    Log.e("jesusdebug",
                        "PrintViewModel-sendPrintTerminationReport-response$response"
                    )
                    Log.e("jesusdebug", "PrintViewModel-sendPrintTerminationReport-response${response.body()?.Data}")
                    _print.value= _print.value.copy(
                        itemUom = response.body()?.Data!![0].UM,
                        itemBatch = batch,
                        itemName = response.body()?.Data!![0].ItemName,
                        itemCode = response.body()?.Data!![0].ItemCode,
                        itemDate = date,
                    )
                }
                override fun onFailure(call: Call<MyDataPrint>, t: Throwable) {
                    TODO("Not yet implemented")
                }
            })


        }
    }

    private fun listPrinter(){
        _printList.value = ListPrint(prints=emptyList(), "cargando")

        viewModelScope.launch(Dispatchers.Default){
            APIService.getInstance().listPrint ("http://192.168.254.20:66/vs1.0/printer").enqueue( object :Callback<ListPrint> {
                override fun onResponse(call: Call<ListPrint>, response: Response<ListPrint>) {
                    if(response.isSuccessful){
                        _printList.value=ListPrint(prints = response.body()?.prints!!,status="ok")
                    }else{
                        _printList.value=ListPrint(prints=emptyList(),status="InternalServerError "+response.code())
                    }
                }
                override fun onFailure(call: Call<ListPrint>, error: Throwable) {
                    _printList.value=ListPrint(prints=emptyList(),status=error.message.toString())
                }
            })
        }
    }

    fun sendPrintFinal(print:Print, data:List<ItemDataPrint>){
        Log.d("jesusdebug", "se ingresó a sendPrintFinal")
        Log.d("jesusdebug", "sendPrintFinal print: "+print)
        Log.d("jesusdebug", "sendPrintFinal data: "+data)

        Log.d("jesusdebug", "iniciando calculo de fechas")


        val fecha = print.itemDate.substringBefore("/") // Obtener el número antes del '/'
            .let {
                val numBeforeSlash = if (it.toInt() in 1..12) it else "01" // Validar el primer número
                if (numBeforeSlash.toInt() < 9) "0${numBeforeSlash.toInt()}" else numBeforeSlash // Agregar '0' si es menor que 9
            }
            .padStart(2, '0') // Asegurarse de que el número tenga dos dígitos
            .plus("/") // Agregar el '/'
            .plus(Calendar.getInstance().get(Calendar.YEAR).toString().takeLast(2)) // Obtener los dos últimos dígitos del año actual


        val anio = fecha.split("/")[1].toInt()
        val mes = fecha.split("/")[0].toInt()

        Log.d("jesusdebug", "fecha: "+fecha)
        val tv = data?.get(0)?.Tvida // Reemplaza esto con tu valor de tv

        val anioFinal = anio + (if(Calendar.getInstance().get(Calendar.YEAR).toString().takeLast(2).toInt() < anio) 0 else tv)!!

        Log.d("jesusdebug", "tv: "+tv)

        val formattedResult = if (mes < 10) {
            "${anioFinal}0${mes}01"
        } else {
            "${anioFinal}${mes}01"
        }

        val jsonBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            JSONObject(
                Gson().toJson(
                    PrintData(
                        ipAddress = print.ipAddress ,
                        portNumber = print.portNumber,
                        flag = _flagPrint.value,
                        lineaData = listOf(
                            LineaItem(
                                itemName = print.itemName + " "+ print.itemUom,
                                itemCode = print.itemCode,
                                numero = if(_flagPrint.value=="Zebra_QR") print.quantity else print.quantity,
                                lote = print.itemBatch,
                                fecha = if(_typeScan.value=="BARCODE" && _fv.value!= "")"${_fv.value.substring(2, 4)}/${_fv.value.substring(0, 2)}"
                                else fecha,
                                unidadMedida = print.itemUom,
                                barCode = if(_flagPrint.value=="Zebra_QR"){data?.get(0)?.BarCode.toString()}else{data?.get(0)?.BarCodeUnit.toString()},
                                fv = formattedResult?:"0000",
                            )
                        )
                    )
                )
            ).toString()
        )
        Log.d("jesusdebug", "formattedResult: "+formattedResult)
        Log.d("jesusdebug", "jsonBody ${jsonBody}")
        Log.d("jesusdebug", "sendPrintFinal: ${JSONObject(
            Gson().toJson(
                PrintData(
                    ipAddress = print.ipAddress ,
                    portNumber = print.portNumber,
                    flag = _flagPrint.value,
                    lineaData = listOf(
                        LineaItem(
                            itemName = print.itemName + " "+ print.itemUom,
                            itemCode = print.itemCode,
                            numero = if(_flagPrint.value=="Zebra_QR") print.quantity else print.quantity,
                            lote = print.itemBatch,
                            fecha = if(_typeScan.value=="BARCODE" && _fv.value!= "")"${_fv.value.substring(2, 4)}/${_fv.value.substring(0, 2)}"
                            else fecha,
                            unidadMedida = print.itemUom,
                            barCode = if(_flagPrint.value=="Zebra_QR"){data?.get(0)?.BarCode.toString()}else{data?.get(0)?.BarCodeUnit.toString()},
                            fv = formattedResult?:"0000",
                        )
                    )
                )
            )
        ).toString()}")



        viewModelScope.launch(Dispatchers.Default){
            val retrofit = Retrofit.Builder()
                .baseUrl("http://192.168.254.26:8050/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(APIService::class.java)
            apiService.sendPrint2(jsonBody).enqueue(object :Callback<MyData> {
                override fun onResponse(call: Call<MyData>, response: Response<MyData>) {

                    if(response.isSuccessful){
                        _statusPrint.value="ok"
                        _print.value = Print(status = "vacio")
                    }else{

                        val errorBody = response.errorBody()?.string()
                        val gson = Gson()
                        val errorResponse = gson.fromJson(errorBody, MyData::class.java)

                        if(errorResponse==null){
                            _statusPrint.value = " El servidor respondio ${response.code()} - ${response.message()}"
                        }else{
                            _statusPrint.value = " " + errorResponse.Data
                        }
                    }
                }
                override fun onFailure(call: Call<MyData>, error: Throwable) {
                    _statusPrint.value=error.message.toString()
                    Log.e("jesusdebug","PrintViewModel-sendPrintSSCC-onFailure-error.message.toString()"+error.message.toString())
                }
            })
        }
    }
    fun sendPrint(print:Print){
        Log.d("jesusdebug", "sendPrint: $print")
        _statusPrint.value = "cargando"

        //print.ipAddress=print.printer.ip
        print.ipAddress=print.printer.ip
        Log.d("jesusdebug", "ipAddress: ${print.ipAddress}")
        print.portNumber=print.printer.port.toInt()
        Log.d("jesusdebug", "iniciando la llamada")
        try {

            viewModelScope.launch(Dispatchers.Default){

                APIService.getInstance().getPrintData(itemCode = print.itemCode, lote = print.itemBatch).enqueue(object :Callback<MyDataPrint> {
                    override fun onResponse(call: Call<MyDataPrint>, response: Response<MyDataPrint>) {
                        Log.e("REOS","PrintViewModel-sendPrintTerminationReport-call"+call)
                        Log.e("REOS","PrintViewModel-sendPrintTerminationReport-response"+response)
                        if(response.isSuccessful){
                            sendPrintFinal(print, response.body()?.Data!!)
                        }else{

                        }
                    }
                    override fun onFailure(call: Call<MyDataPrint>, t: Throwable) {
                        TODO("Not yet implemented")
                    }
                })


            }
        } catch (e: Exception) {
            Log.e("jesusdebug", "Error sendPrint: $e")
        }
    }

    fun sendPrintTerminationReport(print:PrintSSCC){
        Log.e("REOS","PrintViewModel-sendPrintTerminationReport-print"+print)
        _terminationReport.value = TerminationReport(Status="cargando",Data="")

        val jsonBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            JSONObject(
                Gson().toJson(
                    print
                )
            ).toString()
        )

        viewModelScope.launch(Dispatchers.Default){
            APIService.getInstance().sendTerminationReportPrint(jsonBody).enqueue(object :Callback<TerminationReport> {
                override fun onResponse(call: Call<TerminationReport>, response: Response<TerminationReport>) {
                    Log.e("REOS","PrintViewModel-sendPrintTerminationReport-call"+call)
                    Log.e("REOS","PrintViewModel-sendPrintTerminationReport-response"+response)
                    if(response.isSuccessful){
                        _terminationReport.value=TerminationReport(Status="ok",Data=response.body()?.Data!!)
                    }else{

                        val errorBody = response.errorBody()?.string()
                        val gson = Gson()
                        val errorResponse = gson.fromJson(errorBody, SsccResponse::class.java)

                        if(errorResponse==null){
                            _terminationReport.value=TerminationReport(Status=" El servidor respondio ${response.code()} - ${response.message()}",Data="")
                        }else{
                            _terminationReport.value=TerminationReport(Status=" " + errorResponse.error,Data="")
                        }
                    }
                }
                override fun onFailure(call: Call<TerminationReport>, error: Throwable) {
                    _terminationReport.value=TerminationReport(Status=error.message.toString(),Data="")
                }
            })
        }

    }
    fun sendPrintSSCC(print:PrintSSCC){
        Log.e("REOS","PrintViewModel-sendPrintSSCC-print"+print)
        _statusPrint.value = "cargando"

        val jsonBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            JSONObject(
                Gson().toJson(
                    print
                )
            ).toString()
        )

        Log.e("Jepicame","Json is "+Gson().toJson(print).toString())

        viewModelScope.launch(Dispatchers.Default){
            APIService.getInstance().sendPrint(jsonBody).enqueue(object :Callback<SsccResponse> {
                override fun onResponse(call: Call<SsccResponse>, response: Response<SsccResponse>) {
                    Log.e("REOS","PrintViewModel-sendPrintSSCC-call"+call)
                    Log.e("REOS","PrintViewModel-sendPrintSSCC-response"+response)
                    if(response.isSuccessful){
                        _statusPrint.value="ok"
                    }else{

                        val errorBody = response.errorBody()?.string()
                        val gson = Gson()
                        val errorResponse = gson.fromJson(errorBody, SsccResponse::class.java)

                        if(errorResponse==null){
                            _statusPrint.value = " El servidor respondio ${response.code()} - ${response.message()}"
                        }else{
                            _statusPrint.value = " " + errorResponse.error
                        }
                    }
                }
                override fun onFailure(call: Call<SsccResponse>, error: Throwable) {
                    _statusPrint.value=error.message.toString()
                    Log.e("REOS","PrintViewModel-sendPrintSSCC-onFailure-error.message.toString()"+error.message.toString())
                }
            })
        }

    }


    fun sendTerminationReport(sendTerminationReport: sendTerminationReport){
        _terminationReport.value = TerminationReport(Status="cargando",Data="")

        val jsonBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            JSONObject(
                Gson().toJson(
                    sendTerminationReport
                )
            ).toString()
        )

        viewModelScope.launch(Dispatchers.Default){
            APIService.getInstance().sendTerminationReportPrint(jsonBody).enqueue(object :
                Callback<TerminationReport> {
                override fun onResponse(call: Call<TerminationReport>, response: Response<TerminationReport>) {

                    if(response.isSuccessful){
                        _terminationReport.value= TerminationReport(Status="ok",Data=response.body()?.Data!!)
                    }else{

                        val errorBody = response.errorBody()?.string()
                        val gson = Gson()
                        val errorResponse = gson.fromJson(errorBody, SsccResponse::class.java)

                        if(errorResponse==null){
                            _terminationReport.value= TerminationReport(Status=" El servidor respondio ${response.code()} - ${response.message()}",Data="")
                        }else{
                            _terminationReport.value=
                                TerminationReport(Status=" " + errorResponse.error,Data="")
                        }
                    }
                }
                override fun onFailure(call: Call<TerminationReport>, error: Throwable) {
                    _terminationReport.value= TerminationReport(Status=error.message.toString(),Data="")
                }
            })
        }
    }

    fun searchArticleByName(name: String) {
        // Buscar artículo en Realm por nombre
        try {
            Log.d("jesusdebug", "searchArticleByName: $name")
            val realm = Realm.getInstance(configPublic) // Obtener la instancia de Realm
            val article = realm.where(Items::class.java)
                .contains("ItemName", name, Case.INSENSITIVE) // Añadir Case.INSENSITIVE para ignorar mayúsculas
                .findAll()

            Log.d("jesusdebug", "getArticles: $article")

            _articleList.value = article
        } catch (e: Exception) {
            Log.d("jesusdebug", "searchArticleByName: $e")
            _print.value = Print(status = e.message ?: "Error desconocido")
        }
    }

    fun setArticleList(articleList: List<Items>?) {
        _articleList.value = articleList?: emptyList()
    }

    fun setFlagPrint(flagPrint: String) {
        _flagPrint.value = flagPrint
    }

    fun setFV(fv: String) {
        _fv.value = fv
    }
}