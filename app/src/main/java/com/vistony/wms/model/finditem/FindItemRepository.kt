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
            APIService.getInstance().getFindItem(ItemCode).enqueue(object : Callback<FindItemEntity> {
                override fun onResponse(
                    call: Call<FindItemEntity?>,
                    response: Response<FindItemEntity?>
                ) {
                    val findItemEntity = response.body()
                    if (response.isSuccessful&&findItemEntity?.data?.size!!>0) {
                        _result.value= FindItemEntity(status = "Y",data = findItemEntity.data)
                    } else {
                        _result.value= FindItemEntity(status = "N", message = "No se encontraron registros")
                    }
                }
                override fun onFailure(call: Call<FindItemEntity?>, t: Throwable) {
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