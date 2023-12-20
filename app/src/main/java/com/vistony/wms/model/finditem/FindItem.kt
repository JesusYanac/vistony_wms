package com.vistony.wms.model.finditem

import com.google.gson.annotations.SerializedName

data class FindItem(
    val ItemCode: String="",
    val ItemName: String="",
    val Quantity: String="",
    val BinCode: String="",
    val DueDate: String="",
    val Batch: String=""
)

data class FindItemEntity(
    var status:String="",
    @SerializedName("Items")
    val data: List<FindItem> = (emptyList()),
    val message: String? = null
)




