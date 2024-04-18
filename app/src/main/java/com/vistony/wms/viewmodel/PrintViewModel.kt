package com.vistony.wms.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.vistony.wms.model.*
import com.vistony.wms.util.APIService
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

    fun setPrint(print:Print){
        _print.value=print
    }

    fun resetStatusPrint(){
        _statusPrint.value=""
    }
    fun resetStatusTerminationReport(){
        _terminationReport.value= TerminationReport(Data="", Status = "")
    }

    fun getArticle(itemCode:String, batch: String, name: String) {
        Log.d("jesusdebug", "ingreso getArticle: "+itemCode)
        Log.d("jesusdebug", "print: "+_print.value)
        Realm.getInstanceAsync(configPublic, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {

                val article = r.where(Items::class.java)
                    .equalTo("ItemCode",itemCode)
                    .findFirst()
                Log.d("jesusdebug", "getArticle: "+article)

                if (article != null) {
                    _print.value= _print.value.copy(
                        itemUom=article.UoMGroupEntry,
                        itemBatch = batch,
                        itemName = name,
                        itemCode = article.ItemCode,
                        status="ok"
                    )
                    viewModelScope.launch(Dispatchers.Default){

                        APIService.getInstance().getPrintData(itemCode = itemCode, lote = batch).enqueue(object :Callback<MyDataPrint> {
                            override fun onResponse(call: Call<MyDataPrint>, response: Response<MyDataPrint>) {
                                Log.e("REOS","PrintViewModel-sendPrintTerminationReport-call"+call)
                                Log.e("REOS","PrintViewModel-sendPrintTerminationReport-response"+response)
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
                    _print.value=Print(status="vacio")
                }
            }
            override fun onError(exception: Throwable) {
                _print.value=Print(status=exception.message.toString())
            }
        }).toString()
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
        val jsonBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            JSONObject(
                Gson().toJson(
                    PrintData(
                        ipAddress = print.ipAddress ,
                        portNumber = print.portNumber,
                        flag = "Zebra_QR",
                        lineaData = listOf(
                            LineaItem(
                                itemName = print.itemName + " "+ print.itemUom,
                                itemCode = print.itemCode,
                                numero = print.quantity*2,
                                lote = print.itemBatch,
                                fecha = print.itemDate,
                                unidadMedida = print.itemUom,
                                barCode = data?.get(0)?.BarCode.toString(),
                                fv = data?.get(0)?.Tvida.toString(),
                            )
                        )
                    )
                )
            ).toString()
        )
        Log.d("jesusdebug", "sendPrintFinal jsonBody: "+jsonBody)
        Log.d("jesusdebug", "iniciando calculo de fechas")

        val fecha = print.itemDate // Reemplaza esto con tu fecha
        val anio = fecha.split("/")[1].toInt()
        val mes = fecha.split("/")[0].toInt()

        Log.d("jesusdebug", "fecha: "+fecha)
        val tv = data?.get(0)?.Tvida // Reemplaza esto con tu valor de tv

        val anioFinal = anio + tv!!.toInt()

        Log.d("jesusdebug", "tv: "+tv)

        val formattedResult = if (mes < 10) {
            "20${anioFinal}0${mes}01"
        } else {
            "20${anioFinal}${mes}01"
        }

        Log.d("jesusdebug", "formattedResult: "+formattedResult)
        Log.d("jesusdebug", "jsonBody $jsonBody")
        Log.d("jesusdebug", "sendPrintFinal: ${JSONObject(
            Gson().toJson(
                PrintData(
                    ipAddress = print.ipAddress ,
                    portNumber = print.portNumber,
                    flag = "Zebra_QR",
                    lineaData = listOf(
                        LineaItem(
                            itemName = print.itemName + " "+ print.itemUom,
                            itemCode = print.itemCode,
                            numero = print.quantity*2,
                            lote = print.itemBatch,
                            fecha = print.itemDate,
                            unidadMedida = print.itemUom,
                            barCode = data?.get(0)?.BarCode.toString(),
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
                    Log.e("jesusdebug","PrintViewModel-sendPrintSSCC-call"+call)
                    Log.e("jesusdebug","PrintViewModel-sendPrintSSCC-response"+response)
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
}