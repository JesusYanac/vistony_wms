package com.vistony.wms.asn.mvvm

import com.google.gson.annotations.SerializedName

data class PreASN(
    @SerializedName("DocNum")
    val docNum: String="",
    @SerializedName("ItemCode")
    val itemCode: String="",
    @SerializedName("ProdName")
    val itemName: String="",
    val manufacturingNumber: String="",
    @SerializedName("PlannedQty")
    val plannedQty: String="",
    @SerializedName("CmpltQty")
    var cmpltQty: String="",
    @SerializedName("Lote")
    val batch: String="",
    var aSNnumber: String="",
    @SerializedName("UgpCode")
    val ugpCode: String="",
    @SerializedName("Date")
    val date:String="",
    @SerializedName("QtyPallet")
    val qtyPallet:String=""
)

data class PreASNEntity(
    val status: String="N",
    val lpnCode: String="",
    @SerializedName("Data")
    val data: List<PreASN> = emptyList(),
    val message:String=""
)

data class ASNEntity(
    val status: String="N",
    @SerializedName("asn")
    val data: List<ASN> = emptyList(),
    val message:String="",
    val ipAddress: String="",
)

data class ASN(
    val docNum: String="",
    val docEntry:String="",
    val U_Ref2:String="",
    val DateExpected: String="",
    val U_WhsCode: String="",
    @SerializedName("VIS_WMS_ASN1Collection")
    var detail: List<ASNDetail> = emptyList(),
    val U_ItemCode: String="",
    val U_ItemName: String="",
    var U_Quantity:String="",
    var batch: String="",
    var dueDate:String="",

    )

data class ASNDetail(
    var id: String="",
    val U_ItemCode: String="",
    var U_Quantity:String="0",
    val U_FifoDate:String="",
    val U_LpnCode: String="",
    val U_ExpirationDate: String="",
    val U_Batch: String="",
)

data class ASNEntity2(
    val status: String="N",
    @SerializedName("asn")
    val asn: ASN = ASN(),
    val message:String="",
    val ipAddress: String="",
)
data class ASNHeaderResponseEntity(
    val status: String="N",
    @SerializedName("Data")
    val data: ASNHeaderResponse? = ASNHeaderResponse(),
    val message:String="",
)

data class ASNHeaderResponse(
    val hasIssues: String = "",
    val whsCode: String = "",
    val ownCode: String = "",
    @SerializedName("Number")
    val number: String = "",
    val inboundTypeCode: String = "",
    val orderComment: String = "",
    val vendorCode: String = "",
    val dateExpected: String = "",
    val emissionDate: String = "",
    val expirationDate: String = "",
    val status: String = "",
    val outboundNumberSource: String = "",
    val isAsn: String = "",
    val percentLpnInspection: String = "",
    val percentQA: String = "",
    val shiftNumber: String = "",
    val specialField1: String = "",
    val specialField2: String = "",
    val specialField3: String = "",
    val specialField4: String = "",
    @SerializedName("InboundDetailsIfz")
    val aSNDetailResponse: List<ASNDetailResponse> = emptyList()
)

data class ASNDetailResponse(
    val lineNumber: String = "",
    val lineCode: String = "",
    val itemCode: String = "",
    val ctgCode: String = "",
    val itemQty: String = "",
    val status: String = "",
    val lineComment: String = "",
    val fifoDate: String? = null,
    val expirationDate: String? = null,
    val fabricationDate: String? = null,
    val lotNumber: String = "",
    val lpnCode: String = "",
    val price: String = "",
    val specialField1: String = "",
    val specialField2: String = "",
    val specialField3: String = "",
    val specialField4: String = ""
)

