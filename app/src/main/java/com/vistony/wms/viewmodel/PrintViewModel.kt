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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    fun getArticle(value:String){
        _print.value=Print(status="cargando")

        Realm.getInstanceAsync(configPublic, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {

                val article = r.where(Items::class.java)
                    .equalTo("ItemCode",value)
                    .findFirst()

                if (article != null) {
                    _print.value= Print(
                        itemName=article.ItemName,
                        itemCode=article.ItemCode,
                        itemUom=article.UoMGroupEntry,
                        status="ok"
                    )
                }else{
                    _print.value=Print(status="vacio")
                }
            }
            override fun onError(exception: Throwable) {
                _print.value=Print(status=exception.message.toString())
            }
        }).toString()
    }

    fun listPrinter(){
        _printList.value = ListPrint(prints=emptyList(), "cargando")

        viewModelScope.launch(Dispatchers.Default){
            APIService.getInstance().listPrint ("http://192.168.254.20:89/vs1.0/printer").enqueue( object :Callback<ListPrint> {
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

    fun sendPrint(print:Print){
        _statusPrint.value = "cargando"

        print.ipAddress=print.printer.ip
        print.portNumber=print.printer.port.toInt()

        val jsonBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            JSONObject(
                Gson().toJson(
                    print
                )
            ).toString()
        )

        viewModelScope.launch(Dispatchers.Default){
            /*APIService.getInstance().sendPrint("http://192.168.254.20:89/vs1.0/printer",jsonBody).enqueue(object :Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if(response.isSuccessful){
                        _statusPrint.value="ok"
                    }else{
                        _statusPrint.value="InternalServerError"
                    }
                }
                override fun onFailure(call: Call<Void>, error: Throwable) {
                    _statusPrint.value=error.message.toString()
                }
            })*/
        }
    }

    fun sendPrintSSCC(print:PrintSSCC){
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
            APIService.getInstance().sendPrint("http://192.168.254.20:93/vs1.0/Sscc/Print",jsonBody).enqueue(object :Callback<SsccResponse> {
                override fun onResponse(call: Call<SsccResponse>, response: Response<SsccResponse>) {

                    Log.e("JEPICAME","ERROR =>"+response.code())
                    Log.e("JEPICAME","ERROR =>"+response.message())

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
                }
            })
        }

    }
}