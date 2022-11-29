package com.vistony.wms.model

import java.util.*

open class ListDataMasterResponse(
    var values:List<DataMasterItem> = listOf(
        DataMasterItem(nombre = "quality"),
        DataMasterItem(nombre = "itemgroup"),
    )
)

open class DataMasterItem(
    var nombre:String="",
    var status:String="",
    var errorMessage:String="",
    //var data: Dynamic = null,
    var size:Int=0
)



