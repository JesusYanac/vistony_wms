package com.vistony.wms.util

import com.vistony.wms.BuildConfig
import com.vistony.wms.model.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*
import java.net.Proxy
import java.util.concurrent.TimeUnit

interface APIService {
    @POST("Warehouse/suggestionPut")
    fun suggestion(@Query("type")  type: String,@Query("warehouse") warehouse:String): Call<Suggestions>

    @GET
    fun listPrint(@Url url:String): Call<ListPrint>

    @POST
    fun sendPrint(@Url url:String,@Body request:RequestBody): Call<SsccResponse>

    @GET
    fun getSscc(@Url url:String, @Query("code") codeVal:String, @Header("Authorization") jwt: String): Call<Sscc>

    @GET
    fun getArticleFromBatchQrEspecial(@Url url:String, @Query("itemCode") itemCode:String): Call<ProductFromBatch>

    companion object {
        private var apiService: APIService? = null

        private var client: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
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