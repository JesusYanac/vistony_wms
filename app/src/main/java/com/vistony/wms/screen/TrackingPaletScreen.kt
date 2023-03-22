package com.vistony.wms.screen

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.gson.annotations.SerializedName
import com.vistony.wms.R
import com.vistony.wms.component.*
import com.vistony.wms.model.*
import com.vistony.wms.num.TypeCode
import com.vistony.wms.ui.theme.AzulVistony202
import com.vistony.wms.ui.theme.ColorDestine
import com.vistony.wms.viewmodel.ItemsViewModel
import com.vistony.wms.viewmodel.PrintViewModel
import com.vistony.wms.viewmodel.WarehouseViewModel
import com.vistony.wms.viewmodel.ZebraViewModel


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TrackingPaletScreen(navController: NavHostController, context: Context,zebraViewModel:ZebraViewModel){

    val itemsViewModel: ItemsViewModel = viewModel(
        factory = ItemsViewModel.ArticleViewModelFactory("scan")
    )

    val zebraValue = zebraViewModel.data.collectAsState()
    val articleValue = itemsViewModel.article.collectAsState()

    if(zebraValue.value.Payload.isNotEmpty()){
        if(zebraValue.value.Type in listOf("LABEL-TYPE-EAN128")) {
            itemsViewModel.getArticle(value=zebraValue.value.Payload)
        }else{
            Toast.makeText(context, "El rotulado escaneado no corresponde a un código SSCC", Toast.LENGTH_LONG).show()
        }

        zebraViewModel.setData(zebraPayload())
    }

    Scaffold(
        topBar = {
            TopBar(title="Tracking del Palet")
        }
    ){

        Log.e("JEPICAE","ME EJEUOT "+articleValue.value.status)

        when(articleValue.value.status){
            ""->{
                Column(
                    modifier=Modifier.padding(top=20.dp, bottom = 10.dp).fillMaxWidth().fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_palet_on_24),
                        contentDescription = "Favorite Icon",
                        modifier = Modifier.size(150.dp)
                    )

                    Text(
                        text = "ESCANEA EL SSCC DEL PALET",
                        color = Color.Gray,
                        modifier = Modifier.padding(top= 25.dp)
                    )
                }
            }
            "cargando"->{
                CustomProgressDialog("Buscando palet...")
            }
            "locked"->{

                itemsViewModel.resetArticleStatus()
            }
            "ok"->{
                Column(
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp,top=20.dp)
                ){
                    divPrintSSCC(
                        articleValue=articleValue.value,
                        onContinue = {

                            /*printViewModel.sendPrintSSCC(
                                PrintSSCC(
                                    ItemCode = it.itemCode,
                                    Batch = it.itemBatch,
                                    PrinterIP = it.printer.ip,
                                    PortNum = it.printer.port.toInt(),
                                    Warehouse = it.warehouse,
                                    BinCode = it.binCode,
                                    AbsEntry = it.absEntry
                                )
                            )*/
                        },
                        onCancel = {
                            navController.navigateUp()
                        }
                    )
                }

                //itemsViewModel.resetArticleStatus()
            }
            "vacio"->{
                Toast.makeText(context, "El código escaneado no se encuentra en el maestro de articulos", Toast.LENGTH_SHORT).show()
                itemsViewModel.resetArticleStatus()
            }
            else->{
                Toast.makeText(context, "Ocurrio un error:\n ${articleValue.value.status}", Toast.LENGTH_SHORT).show()
                itemsViewModel.resetArticleStatus()
            }
        }
    }
}

@Composable
private fun divPrintSSCC(articleValue:ItemsResponse,onContinue:(Print)->Unit,onCancel:()->Unit){

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = " ${articleValue.items[0].item.ItemName}", color = Color.Gray, textAlign = TextAlign.Center)
    }
    Divider()

    Text("")

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Código")
        Text(text = " ${articleValue.items[0].item.ItemCode}", color = Color.Gray)
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Lote")
        Text(text = " ${articleValue.items[0].lote }", color = Color.Gray)
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "SSCC")
        Text(text = " ${articleValue.nameSscc}", color = Color.Gray)
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Fecha Prod")
        Text(text = " ${articleValue.items[0].inDate}", color = Color.Gray)
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Fecha Vcto")
        Text(text = " ${articleValue.items[0].expireDate}", color = Color.Gray)
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Cantidad")
        Text(text = " ${articleValue.items[0].quantity}", color = Color.Gray)
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Almacén actual")
        Text(text = " X", color = Color.Gray)
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Ubicación actual")
        Text(text = " ${articleValue.defaultLocation}", color = Color.Gray)
    }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Estado")
        Text(text = " X", color = Color.Gray)
    }

}