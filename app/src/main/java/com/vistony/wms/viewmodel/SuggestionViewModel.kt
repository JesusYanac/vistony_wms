package com.vistony.wms.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.vistony.wms.model.SuggestionPut
import com.vistony.wms.model.Suggestions
import com.vistony.wms.util.APIService
import com.vistony.wms.util.isNumeric
import com.vistony.wms.util.parseValue
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

class SuggestionViewModel(): ViewModel() {

    class SuggestionViewModelFactory(): ViewModelProvider.Factory {
        @Suppress("UNSCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SuggestionViewModel() as T
        }
    }

    private val _suggtn = MutableStateFlow(Suggestions())
    val suggtn: StateFlow<Suggestions> get() = _suggtn

    init{
       // getSuggestionList(suggestionPut)
    }

    fun resetSuggestionState(){
        _suggtn.value= Suggestions(Data= emptyList(), status = "")
    }

    fun getSuggestionList(suggestionPut: SuggestionPut,objType:String) {
        Log.e("REOS","SuggestionViewModel-getSuggestionList-suggestionPut: "+suggestionPut)
        Log.e("REOS","SuggestionViewModel-getSuggestionList-objType: "+objType)
        if(suggestionPut.ItemCode.length==20 && isNumeric(suggestionPut.ItemCode)){
            suggestionPut.ItemCode=suggestionPut.ItemCode.substring(2)
        }else{
            val (itemCodeNew, lote) = parseValue(suggestionPut.ItemCode)
            suggestionPut.ItemCode=itemCodeNew
        }
        Log.e("REOS","SuggestionViewModel-getSuggestionList-suggestionPut: "+suggestionPut.toString())
        _suggtn.value = Suggestions(emptyList(), "cargando")

        val jsonBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            JSONObject(
                Gson().toJson(
                    suggestionPut
                )
            ).toString()
        )

        Log.e("REOS","Suggestion-SuggestionViewModel-jsonBody: "+jsonBody)

        viewModelScope.launch(Dispatchers.Default){

            if(objType.equals("0"))
            {
                APIService.getInstance().suggestionPick(jsonBody)
                    .enqueue(object : Callback<Suggestions> {
                        override fun onResponse(
                            call: Call<Suggestions>,
                            response: Response<Suggestions>
                        ) {
                            if (response.isSuccessful) {
                                _suggtn.value =
                                    Suggestions(status = "OK", Data = response.body()?.Data!!)

                            } else {
                                _suggtn.value = Suggestions(status = "InternalServerError")
                            }
                        }

                        override fun onFailure(call: Call<Suggestions>, error: Throwable) {
                            when (error) {
                                is SocketTimeoutException -> {
                                    _suggtn.value = Suggestions(status = "TimeOut")
                                }
                                is UnknownHostException -> {
                                    _suggtn.value = Suggestions(status = "NotInternet")
                                }
                                is ConnectException -> {
                                    _suggtn.value = Suggestions(status = "InternalServerError")
                                }
                                is JSONException, is JsonSyntaxException -> {
                                    _suggtn.value = Suggestions(status = "BadRequest")
                                }
                                is IOException -> {
                                    _suggtn.value = Suggestions(status = "BadRequest")
                                }
                            }
                        }
                    })
            }
            else{
                APIService.getInstance().suggestion(jsonBody)
                    .enqueue(object : Callback<Suggestions> {
                        override fun onResponse(
                            call: Call<Suggestions>,
                            response: Response<Suggestions>
                        ) {
                            if (response.isSuccessful) {
                                _suggtn.value =
                                    Suggestions(status = "OK", Data = response.body()?.Data!!)

                            } else {
                                _suggtn.value = Suggestions(status = "InternalServerError")
                            }
                        }

                        override fun onFailure(call: Call<Suggestions>, error: Throwable) {
                            when (error) {
                                is SocketTimeoutException -> {
                                    _suggtn.value = Suggestions(status = "TimeOut")
                                }
                                is UnknownHostException -> {
                                    _suggtn.value = Suggestions(status = "NotInternet")
                                }
                                is ConnectException -> {
                                    _suggtn.value = Suggestions(status = "InternalServerError")
                                }
                                is JSONException, is JsonSyntaxException -> {
                                    _suggtn.value = Suggestions(status = "BadRequest")
                                }
                                is IOException -> {
                                    _suggtn.value = Suggestions(status = "BadRequest")
                                }
                            }
                        }
                    })
            }

        }

    }

}