package com.vistony.wms.util

import com.vistony.wms.BuildConfig
import com.vistony.wms.model.*
import com.vistony.wms.model.finditem.FindItemEntity
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*
import java.net.Proxy
import java.util.concurrent.TimeUnit

interface APIService {
    @POST("Warehouse/suggestionPick")
    fun suggestionPick(@Body request:RequestBody): Call<Suggestions>

    @POST("Warehouse/suggestionPut")
    fun suggestion(@Body request:RequestBody): Call<Suggestions>

    @GET
    fun listPrint(@Url url:String): Call<ListPrint>

    @POST("Sscc/Print")
    fun sendPrint(@Body request:RequestBody): Call<SsccResponse>

    @POST("Production/TerminacionReport")
    fun sendTerminationReportPrint(@Body request:RequestBody): Call<TerminationReport>

    @GET("sscc")
    fun getSscc(@Query("code") codeVal:String, @Header("Authorization") jwt: String): Call<Sscc>

    @GET("Inventory/getItem")
    fun getArticleFromBatchQrEspecial(@Query("itemCode") itemCode:String): Call<ProductFromBatch>

    @GET("/pe/vs1.0/Inventory/GetItemLayout")
    fun getFindItem(@Query("itemCode") itemCode: String?): Call<FindItemEntity>

    companion object {
        private var apiService: APIService? = null

        private var client: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(50, TimeUnit.SECONDS)
            .writeTimeout(50, TimeUnit.SECONDS)
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