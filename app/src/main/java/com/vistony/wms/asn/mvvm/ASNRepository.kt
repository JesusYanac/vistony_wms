package com.vistony.wms.asn.mvvm

import android.util.Log
import com.google.android.gms.common.util.VisibleForTesting
import com.google.gson.Gson
import com.vistony.wms.util.APIService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ASNRepository(
    //private val apiService: APIService
) {
    private val _resultPreASN = MutableStateFlow(PreASNEntity())
    val resultPreASN: StateFlow<PreASNEntity> get() = _resultPreASN

    private val _resultASN = MutableStateFlow(ASNEntity())
    val resultASN: StateFlow<ASNEntity> get() = _resultASN

    private val _resultASNresponse = MutableStateFlow(ASNHeaderResponseEntity())
    val resultASNresponse: StateFlow<ASNHeaderResponseEntity> get() = _resultASNresponse

    @VisibleForTesting
    fun getDataPreASN(code:String,batch:String,)
    {
        try {
            _resultPreASN.value=PreASNEntity()
            APIService.getInstance().getDataPreASN(code, batch).enqueue  (object :
                Callback<PreASNEntity> {
                override fun onResponse(
                    call: Call<PreASNEntity?>,
                    response: Response<PreASNEntity?>
                ) {
                    val preASNEntity = response.body()
                    if (response.isSuccessful&&preASNEntity?.data?.size!!>0) {
                        Log.e("REOS", "ASNRepository-getDataPreASN.preASNEntity.data: "+preASNEntity.data)
                        _resultPreASN.value= PreASNEntity(status = "Y",data = preASNEntity.data)
                        Log.e("REOS", "ASNRepository-getDataPreASN._resultPreASN.value: "+_resultPreASN.value)
                    } else {
                        Log.e("REOS", "ASNRepository-getDataPreASN.preASNEntity.No se encontraron registros: ")
                        //_result.value= PreASNEntity(status = "N", message = "No se encontraron registros")
                        _resultPreASN.value= PreASNEntity(status = "N", message = "No se encontraron registros")
                    }
                }
                override fun onFailure(call: Call<PreASNEntity?>, t: Throwable) {
                    Log.e("REOS", "No se concluyo la peticion")
                    Log.e("REOS", "ASNRepository-getDataPreASN.onFailure: "+t.message)
                    //_result.value= PreASNEntity(status = "N", message = "Fallo la Petición")
                    _resultPreASN.value= PreASNEntity(status = "N", message = t.message.toString())
                }
            })
        } catch (e: Exception) {
            _resultPreASN.value= PreASNEntity(status = "N", message = e.toString())
        }
    }

    fun sendDataASNPrint(ASN: ASNEntity)
    {
        try {
            val ASN2:ASNEntity2=ASNEntity2(asn = ASN.data.last(), ipAddress = ASN.ipAddress)
            val jsonBody: RequestBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                JSONObject(
                    Gson().toJson(
                        ASN2
                    )
                ).toString()
            )
            Log.e("REOS", "ASNRepository-sendDataASNPrint-jsonBody: "+jsonBody)
            APIService.getInstance().sendDataASNPrint(jsonBody).enqueue(object :
                Callback<ASNHeaderResponseEntity> {
                override fun onResponse(
                    call: Call<ASNHeaderResponseEntity?>,
                    response: Response<ASNHeaderResponseEntity?>
                ) {
                    Log.e("REOS", "ASNRepository-sendDataASNPrint-call: "+call)
                    Log.e("REOS", "ASNRepository-sendDataASNPrint-response: "+response)
                    val aSNHeaderResponseEntity = response.body()
                    if (response.isSuccessful&&aSNHeaderResponseEntity!=null) {
                        Log.e("REOS", "ASNRepository-sendDataASNPrint-aSNHeaderResponseEntity: "+aSNHeaderResponseEntity)
                        _resultASNresponse.value= ASNHeaderResponseEntity(status = "Y", data =  aSNHeaderResponseEntity.data)
                    } else {
                        Log.e("REOS", "ASNRepository-sendDataASNPrint-aSNHeaderResponseEntity-No se encontraron registros: ")
                        //_result.value= PreASNEntity(status = "N", message = "No se encontraron registros")
                        _resultASNresponse.value= ASNHeaderResponseEntity(status = "N", message = "No se encontraron registros")
                    }
                }
                override fun onFailure(call: Call<ASNHeaderResponseEntity?>, t: Throwable) {
                    Log.e("busquedadebug", "No se concluyo la peticion")
                    Log.e("busquedadebug", "onFailure: "+t.message)
                    //_result.value= PreASNEntity(status = "N", message = "Fallo la Petición")
                    _resultASNresponse.value= ASNHeaderResponseEntity(status = "N", message = t.message.toString())
                }
            })
        } catch (e: Exception) {
            Log.e("REOS", "aSNEntity-sendDataASNPrint-error: "+e.toString())
            _resultASNresponse.value= ASNHeaderResponseEntity(status = "N", message = e.toString())
        }
    }
}