package com.vistony.wms.util

import com.vistony.wms.BuildConfig
import com.vistony.wms.model.LoginResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*
import java.net.Proxy
import java.util.concurrent.TimeUnit


interface APIService {
   @POST("login")
    fun login(@Body  request: RequestBody): Call<LoginResponse>

    /*@POST("Inventory")
    fun inventory(@Body  request: RequestBody): Call<InventoryResponse>*/

    companion object {
        private var apiService: APIService? = null

        private var client: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .proxy(Proxy.NO_PROXY)
            .build()

        fun getInstance() : APIService {
            if (apiService == null) {
                apiService = Retrofit.Builder()
                    .baseUrl(BuildConfig.API_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build().create(APIService::class.java)
            }

            return apiService!!
        }
    }
}