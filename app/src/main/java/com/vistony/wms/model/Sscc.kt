package com.vistony.wms.model


import com.google.gson.annotations.SerializedName

data class Sscc(
    @SerializedName("Data")
    val `data`: Data =Data(),
    var status: String = ""
)

data class Data(
    @SerializedName("Code")
    val code: String ="",
    @SerializedName("Name")
    val name: String="",
    @SerializedName("DefaultLocation")
    val uBtringinCode: String="",
    @SerializedName("WhsCode")
    val uWhsCode: String="",
    @SerializedName("Detail")
    val vISWMSSCC1Collection: List<VISWMSSCC1Collection> = emptyList()
)


data class VISWMSSCC1Collection(
    @SerializedName("BatchCode")
    val lineId: Int=0,
    @SerializedName("Batch")
    val uBatch: String="",
    @SerializedName("Dscription")
    val uDscription: String="",
    @SerializedName("InDate")
    val uDate: String="",
    @SerializedName("ItemCode")
    val uItemCode: String="",
    @SerializedName("Quantity")
    val uQuantity: Int=0
)