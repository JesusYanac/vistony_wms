package com.vistony.wms.model

import com.google.gson.annotations.SerializedName

data class ProductFromBatch(
    @SerializedName("Data")
    val data: Detail
)

data class Detail(
    @SerializedName("ItemCode")
    val itemCode: String,
    @SerializedName("ItemName")
    val itemName: String,
    @SerializedName("Properties8")
    val locked: String
)