package com.vistony.wms.model.finditem

import android.util.Log
import com.vistony.wms.util.APIService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FindItemRepository {
    private val _result = MutableStateFlow(FindItemEntity())
    val result: StateFlow<FindItemEntity> get() = _result

    fun getFindItem(Imei:String,ItemCode: String)
    {
        try {
            Log.e("busquedadebug","Buscando item "+ItemCode)
            APIService.getInstance().getFindItem(ItemCode).enqueue(object : Callback<FindItemEntity> {
                override fun onResponse(
                    call: Call<FindItemEntity?>,
                    response: Response<FindItemEntity?>
                ) {
                    Log.e("busquedadebug", "Se ejecuto la peticion")
                    val findItemEntity = response.body()
                    Log.e("busquedadebug", "Se obtuvo el resultado")
                    if (response.isSuccessful&&findItemEntity?.data?.size!!>0) {
                        Log.e("busquedadebug", "finItemEntity.data: "+findItemEntity.data)
                        _result.value= FindItemEntity(status = "Y",data = findItemEntity.data)
                    } else {
                        Log.e("busquedadebug", "No se encontraron registros")
                        _result.value= FindItemEntity(status = "N", message = "No se encontraron registros")
                    }
                }
                override fun onFailure(call: Call<FindItemEntity?>, t: Throwable) {
                    Log.e("busquedadebug", "No se concluyo la peticion")
                    Log.e("busquedadebug", "onFailure: "+t.message)
                    _result.value= FindItemEntity(status = "N", message = "No se encontraron registros")
                }
            })
        } catch (e: Exception) {

            Log.e(
                "REOS",
                "BankRepository-addBanks-error: " + e.toString()
            )
        }
    }

    fun getFindItembyLote(Imei: String, itemLote: String) {

        try {
            Log.e("busquedadebug","Buscando item "+itemLote)
            APIService.getInstance().getFindItembyLote(itemLote).enqueue(object : Callback<FindItemEntity> {
                override fun onResponse(
                    call: Call<FindItemEntity?>,
                    response: Response<FindItemEntity?>
                ) {
                    Log.e("busquedadebug", "Se ejecuto la peticion")
                    val findItemEntity = response.body()
                    Log.e("busquedadebug", "Se obtuvo el resultado")
                    if (response.isSuccessful&&findItemEntity?.data?.size!!>0) {
                        Log.e("busquedadebug", "finItemEntity.data: "+findItemEntity.data)
                        _result.value= FindItemEntity(status = "Y",data = findItemEntity.data)
                    } else {
                        Log.e("busquedadebug", "No se encontraron registros")
                        _result.value= FindItemEntity(status = "N", message = "No se encontraron registros")
                    }
                }
                override fun onFailure(call: Call<FindItemEntity?>, t: Throwable) {
                    Log.e("busquedadebug", "No se concluyo la peticion")
                    Log.e("busquedadebug", "onFailure: "+t.message)
                    _result.value= FindItemEntity(status = "N", message = "No se encontraron registros")
                }
            })
        } catch (e: Exception) {

            Log.e(
                "REOS",
                "BankRepository-addBanks-error: " + e.toString()
            )
        }
    }
}