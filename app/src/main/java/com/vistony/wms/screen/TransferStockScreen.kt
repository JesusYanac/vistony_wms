package com.vistony.wms.screen

import LabelForRow
import LabelRow
import TransferStockDialog
import android.annotation.*
import android.content.*
import android.util.*
import android.widget.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.*
import androidx.navigation.*
import com.vistony.wms.component.*
import com.vistony.wms.model.*
import com.vistony.wms.viewmodel.*


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TransferStockScreen(navController: NavHostController, context: Context, zebraViewModel: ZebraViewModel) {
    val isDialogVisible: MutableState<Boolean> = remember { mutableStateOf(false) }

    val zebraValue = zebraViewModel.data.collectAsState()

    val scannedArticleCode: MutableState<String> = remember { mutableStateOf("") }
    val scannedWarehouseCode: MutableState<String> = remember { mutableStateOf("") }
    val scannedWarehouseCode1: MutableState<String> = remember { mutableStateOf("") }
    val scannedWarehouseCode2: MutableState<String> = remember { mutableStateOf("") }

    val cantidad: MutableState<String> = remember { mutableStateOf("0") }
    val transferStockViewModel: TransferStockViewModel = viewModel(
        factory = TransferStockViewModel.TransferStockViewModelFactory()
    )


    fun handleQRCodeScan() {
        scannedArticleCode.value = zebraValue.value.Payload
        isDialogVisible.value = true
    }


    fun handleWarehouseCodeScan() {
        val codigo: String = scannedWarehouseCode.value

        when {
            scannedWarehouseCode1.value == "" -> scannedWarehouseCode1.value = codigo
            scannedWarehouseCode2.value == "" && scannedWarehouseCode1.value != codigo -> scannedWarehouseCode2.value = codigo
            scannedWarehouseCode1.value == codigo -> scannedWarehouseCode1.value = ""
            scannedWarehouseCode2.value == codigo -> scannedWarehouseCode2.value = ""
        }
    }
    fun handleCode39Scan() {
        if (zebraValue.value.Payload.contains("|")) {
            scannedArticleCode.value = zebraValue.value.Payload
        } else {
            scannedWarehouseCode.value = zebraValue.value.Payload
            handleWarehouseCodeScan()
        }
    }
    fun showQRCodeMismatchError() {
        Toast.makeText(context, "El rotulado escaneado no corresponde a un código QR", Toast.LENGTH_LONG).show()
    }
    fun handleScannedData() {
        when (zebraValue.value.Type) {
            "LABEL-TYPE-QRCODE" -> handleQRCodeScan()
            "LABEL-TYPE-CODE39" -> handleCode39Scan()
            else -> showQRCodeMismatchError()
        }

        zebraViewModel.setData(zebraPayload())
    }

    fun insertTransferLayout() {
        // Lógica para insertar la transferencia en el ViewModel
        transferStockViewModel.insertTransfersLayout(
            scannedArticleCode.value,
            scannedWarehouseCode1.value,
            scannedWarehouseCode2.value,
            cantidad.value
        )
    }
    if (zebraValue.value.Payload.isNotEmpty()) {
        handleScannedData()
    }

    TransferStockDialog(
        isDialogVisible = isDialogVisible.value,
        scannedArticleCode = scannedArticleCode,
        scannedWarehouseCode1 = scannedWarehouseCode1,
        scannedWarehouseCode2 = scannedWarehouseCode2,
        cantidad = cantidad,
        onDialogDismiss = {
            isDialogVisible.value = false
        },
        onConfirm = { valid ->
            if (valid) {
                insertTransferLayout()
            }
        }
    )

    Scaffold(
        topBar = { TopBarWithBackPress(title = "Transferencia de Stock", onButtonClicked = (navController::popBackStack)) }
    ) {
        TransferenciaPaletList(transferStockViewModel.transfersLayoutList.value)
    }
}


@Composable
fun TransferenciaPaletList(transfersLayoutList: List<TransfersLayout>?) {
    LazyColumn(
        modifier = Modifier
            .padding(top = 20.dp)
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        transfersLayoutList?.forEach { transfersLayout ->
            item {
                TransferenciaPalet(item = transfersLayout)
            }
        }
    }
}
@Composable
fun buildBody(transferStockViewModel: TransferStockViewModel) {
    LazyColumn(
        modifier = Modifier
            .padding(top = 20.dp)
            .fillMaxWidth()
            .fillMaxHeight()
    ) {

        if(transferStockViewModel.transfersLayoutList.value != null){
            val dataSize = transferStockViewModel.transfersLayoutList.value!!.size
            Log.e("jesusdebug", "dataSize: $dataSize")
            for (i in 0..dataSize) {
                item {
//                    TransferenciaPalet(item = transferStockViewModel.transfersLayoutList.value!![i])
                }
            }
        }
    }
}

@Composable
fun TransferenciaPalet(item: TransfersLayout) {


    val detail = item.detail.first()!!
    val codeArticulo = detail.sscc
    val valueArticulo = detail.itemName
    val cantidad = detail.quantity
    val origen = detail.binOrigin
    val destino = detail.binDestine
    val lote = detail.batch
    val num_sap = item.codeSAP



    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .border( 1.dp, Color.Black )
            .clip(RoundedCornerShape(5.dp))
            .background(Color.White)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(5.dp))
    ) {

        Box (
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .weight(80f)
                    ) {
                        Row {
                            LabelRow(text = "Articulo")
                            LabelForRow(text = "$codeArticulo")
                        }
                        Row {
                            LabelForRow(text = "$valueArticulo")
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .weight(20f)
                            .border(1.dp, Color.Black)
                            .height(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "$cantidad",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
               Spacer(modifier = Modifier.height(12.dp))
               Row(
                   modifier = Modifier
                       .fillMaxWidth()
               ) {
                   Column (
                       modifier = Modifier
                           .weight(60f)
                   ){
                       Row(
                           modifier = Modifier
                               .fillMaxWidth()
                       ) {
                           LabelRow(text = "Origen")
                           LabelForRow(text = "$origen")
                       }
                       Spacer(modifier = Modifier.height(10.dp))
                       Row(
                           modifier = Modifier
                               .fillMaxWidth()
                       ) {
                           LabelRow(text = "Destino")
                           LabelForRow(text = "$destino")
                       }
                   }

                   Column (
                       modifier = Modifier
                           .weight(40f)
                   ){
                       Row(
                           modifier = Modifier
                               .fillMaxWidth()
                       ) {
                           LabelRow(text = "Lote")
                           LabelForRow(text = lote)
                       }
                       Spacer(modifier = Modifier.height(10.dp))
                       Row(
                           modifier = Modifier
                               .fillMaxWidth()
                       ) {
                           LabelRow(text = "N° SAP")
                           LabelForRow(text = "$num_sap")
                       }
                   }
               }
            }
        }

    }

}




