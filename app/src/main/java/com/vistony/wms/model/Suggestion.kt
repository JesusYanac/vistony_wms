package com.vistony.wms.model

import androidx.annotation.NonNull
import com.google.gson.annotations.SerializedName

data class Suggestions(
    @NonNull @SerializedName("Data") var Data: List<Suggestion> = emptyList(),
    var status: String = ""
)

data class Suggestion(
    @NonNull @SerializedName("AbsEntry") var AbsEntry: Int = 0,
    @NonNull @SerializedName("BinCode") var BinCode: String= "",
)