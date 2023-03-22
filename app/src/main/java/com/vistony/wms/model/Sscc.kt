package com.vistony.wms.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Nullable


data class SsccResponse(
    @Nullable
    @SerializedName("Data")
    val data: DataX=DataX(),
    @Nullable
    @SerializedName("Error")
    val error: String=""
)

data class DataX(
    @SerializedName("Date")
    val date: DateX=DateX(),
    @SerializedName("SsccData")
    val ssccData: SsccData=SsccData()
)

data class DateX(
    @SerializedName("ExpDate")
    val expDate: String=""
)

data class SsccData(
    @SerializedName("Code")
    val code: String="",
    @SerializedName("Name")
    val name: String="",
    @SerializedName("U_BinCode")
    val uBinCode: String="",
    @SerializedName("U_Code")
    val uCode: String="",
    @SerializedName("U_Height")
    val uHeight: Double=0.0,
    @SerializedName("U_Quantity")
    val uQuantity: Double=0.0,
    @SerializedName("U_Weight")
    val uWeight: Double=0.0,
    @SerializedName("U_WhsCode")
    val uWhsCode: String="",
    @SerializedName("VIS_WMS_SCC1Collection")
    val vISWMSSCC1Collection: List<VISWMSSCC1Collection> = emptyList()
)

data class VISWMSSCC1Collection(
    @SerializedName("U_Batch")
    val uBatch: String="",
    @SerializedName("U_BatchCode")
    val uBatchCode: Int=0,
    @SerializedName("U_Dscription")
    val uDscription: String="",
    @SerializedName("U_ItemCode")
    val uItemCode: String="",
    @SerializedName("U_Quantity")
    val uQuantity: Double=0.0
)

////////////////////////
////////////////////////
////////////////////////
////////////////////////

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
    @SerializedName("DetailSSCC")
    val vISWMSSCC1Collection: List<VISWMSSCC1> = emptyList()
)


data class VISWMSSCC1(
    @SerializedName("BatchCode")
    val lineId: Int=0,
    @SerializedName("Batch")
    val uBatch: String="",
    @SerializedName("Dscription")
    val uDscription: String="",
    @SerializedName("InDate")
    val uDate: String="",
    @SerializedName("ExpDate")
    val exDate: String="",
    @SerializedName("ItemCode")
    val uItemCode: String="",
    @SerializedName("Quantity")
    val uQuantity: Int=0
)