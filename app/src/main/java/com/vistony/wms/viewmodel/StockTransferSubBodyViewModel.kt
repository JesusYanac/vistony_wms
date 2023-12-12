package com.vistony.wms.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vistony.wms.model.StockTransferBody
import com.vistony.wms.model.StockTransferSubBody
import com.vistony.wms.model.StockTransferSubBodyRI
import io.realm.Realm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.bson.types.ObjectId
import java.util.*

class StockTransferSubBodyViewModel(): ViewModel() {
    private var realm: Realm = Realm.getInstance(Realm.getDefaultConfiguration())

    private val _stockTransferSubBody = MutableStateFlow(StockTransferSubBodyRI())
    val stockTransferSubBody: StateFlow<StockTransferSubBodyRI> get() = _stockTransferSubBody

    class StockTransferSubBodyViewModelModelFactory(): ViewModelProvider.Factory {
        @Suppress("UNSCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StockTransferSubBodyViewModel() as T
        }
    }

    fun resetSubBodyState(){
        _stockTransferSubBody.value= StockTransferSubBodyRI()
    }

    fun getSubData(idSubBody: ObjectId){
        _stockTransferSubBody.value= StockTransferSubBodyRI(status = "cargando",data=StockTransferSubBody())

        Realm.getInstanceAsync(realm.configuration, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {

                val subCount = r.where(StockTransferSubBody::class.java)
                    .equalTo("_id", idSubBody)
                    .equalTo("Delete", "N")
                    .findFirst()

                if(subCount!=null){
                    _stockTransferSubBody.value= StockTransferSubBodyRI(
                        data =subCount,
                        status="ok"
                    )

                }else{
                    _stockTransferSubBody.value= StockTransferSubBodyRI(
                        data=StockTransferSubBody(),
                        status="vacio"
                    )
                }

            }
            override fun onError(exception: Throwable) {
                _stockTransferSubBody.value= StockTransferSubBodyRI(data=StockTransferSubBody(),message="${exception.message}", status = "error")
            }
        })
    }

}