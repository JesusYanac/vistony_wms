package com.vistony.wms.model

import androidx.annotation.NonNull
import com.google.gson.annotations.SerializedName

data class Suggestions(
    @NonNull @SerializedName("Data") var Data: List<Suggestion> = emptyList(),
    var status: String = ""
)

data class Suggestion(
    @NonNull @SerializedName("CodUbicacion") var AbsEntry: Int = 0,
    @NonNull @SerializedName("Ubicacion") var BinCode: String= "",
    @NonNull @SerializedName("Cantidad") var Quantity: Double= 0.0,
    @NonNull @SerializedName("ItemCode") var ItemCode: String= "",
    @NonNull @SerializedName("ItemName") var ItemName: String= "",
    @NonNull @SerializedName("BatchCode") var BatchCode: Int= 0,
    @NonNull @SerializedName("BatchNumber") var BatchNumber: String= "",
    @NonNull @SerializedName("Quantity") var Quantity2: Double= 0.0,
    @NonNull @SerializedName("WarehouseCode") var WarehouseCode: String= "",
    @NonNull @SerializedName("BinEntry") var BinEntry: Int= 0,
    @NonNull @SerializedName("BinCode") var BinCode2: String= "",
)

data class SuggestionPut(
    var ItemCode:String="",
    var WareHouse:String="",
    var Document:String="",
    var BatchNumber:String="",
    var WareHouseCode:String="",
)