package com.vistony.wms.viewmodel

import android.util.Log
import androidx.compose.ui.graphics.vector.EmptyPath
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.vistony.wms.model.StockTransferBodyResponse
import com.vistony.wms.model.Suggestions
import com.vistony.wms.util.APIService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class SuggestionViewModel(/* type:String,warehouse:String*/): ViewModel() {

    class SuggestionViewModelFactory(/*private var type:String,private var warehouse:String*/): ViewModelProvider.Factory {
        @Suppress("UNSCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SuggestionViewModel(/*type,warehouse*/) as T
        }
    }

    private val _suggtn = MutableStateFlow(Suggestions())
    val suggtn: StateFlow<Suggestions> get() = _suggtn

    init{
        //getSuggestionList(type,warehouse)
    }

    fun resetSuggestionState(){
        _suggtn.value= Suggestions(Data= emptyList(), status = "")
    }

    fun getSuggestionList(type:String,warehouse:String) {

        _suggtn.value = Suggestions(emptyList(), "cargando")

        viewModelScope.launch(Dispatchers.Default){
            APIService.getInstance().suggestion(type,warehouse).enqueue( object : Callback<Suggestions> {
                override fun onResponse(call: Call<Suggestions>, response: Response<Suggestions>) {
                    if(response.isSuccessful){
                        _suggtn.value=Suggestions(status="OK", Data = response.body()?.Data!!)

                    }else{
                        _suggtn.value=Suggestions(status="InternalServerError")
                    }
                }
                override fun onFailure(call: Call<Suggestions>, error: Throwable) {
                    Log.e("JEPICAME","=>"+error.message.toString())
                    when (error) {
                        is SocketTimeoutException -> {
                            _suggtn.value=Suggestions(status="TimeOut")
                        }
                        is UnknownHostException -> {
                            _suggtn.value=Suggestions(status="NotInternet")
                        }
                        is ConnectException -> {
                            _suggtn.value= Suggestions(status="InternalServerError")
                        }
                        is JSONException, is JsonSyntaxException -> {
                            _suggtn.value=Suggestions(status="BadRequest")
                        }
                        is IOException -> {
                            _suggtn.value=Suggestions(status="BadRequest")
                        }
                    }
                }
            })
        }

    }

}