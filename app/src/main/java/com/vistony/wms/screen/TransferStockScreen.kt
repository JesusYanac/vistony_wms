package com.vistony.wms.screen

import TransferStockDialog
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.vistony.wms.R
import com.vistony.wms.component.*
import com.vistony.wms.model.*
import com.vistony.wms.model.finditem.*
import com.vistony.wms.ui.theme.*
import com.vistony.wms.viewmodel.*
import java.math.*
import java.text.SimpleDateFormat
import java.util.*


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TransferStockScreen(navController: NavHostController, context: Context, zebraViewModel: ZebraViewModel) {
    val zebraValue = zebraViewModel.data.collectAsState()

    val transferStockViewModel: TransferStockViewModel = viewModel(
        factory = TransferStockViewModel.TransferStockViewModelFactory( )
    )


    val showPopup : State<Boolean> = transferStockViewModel.showPopup.collectAsState()
    val showFindItem = remember { mutableStateOf<Boolean>(false) }
    if( showFindItem.value ){
        DialogFindItemScreen(bitmapDeliveryPruebaStatus = showFindItem)
    }
    if (zebraValue.value.Payload.isNotEmpty()) {
        LocalFocusManager.current.clearFocus()
        transferStockViewModel.handleScannedData(
            type = zebraValue.value.Type,
            code = zebraValue.value.Payload,
            context = context
        )
        zebraViewModel.setData(zebraPayload())
        zebraValue.value.Payload = ""  // esta linea fue añadida, se puede borrar si genera problemas
    }
    if(showPopup.value){
        TransferStockDialog(transferStockViewModel = transferStockViewModel)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                elevation = 0.dp,
                modifier = Modifier.background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            AzulVistony202,
                            AzulVistony201
                        )
                    )
                ),
                title = {
                    Text(
                        text = "Transferencia de Stock",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                },
                backgroundColor = Color.Transparent,
                actions = {
                    Row {
                        IconButton(
                            onClick = {
                                // Aquí se ejecuta la acción de la lupa
                                      showFindItem.value = true
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "search",
                                tint = Color.White
                            )
                        }
                    }
                }
            )
        },
    ){
        CardTransferenciaLayoutList(
            transferStockViewModel = transferStockViewModel
        )
    }
}

@Composable
fun CardTransferenciaLayoutList(transferStockViewModel: TransferStockViewModel) {

    val transfersLayoutList: State<List<TransfersLayout>?> = transferStockViewModel.transfersLayoutList.collectAsState()
    val milista = remember { mutableStateListOf<TransfersLayout>() }

    val listKey = transfersLayoutList.value.hashCode()
    LaunchedEffect(key1 = listKey) {
        milista.clear()
        transfersLayoutList.value?.let { milista.addAll(it) }
    }
    CardTransferenciaLayoutListContent(transfersLayoutList = milista)
}

@Composable
fun CardTransferenciaLayoutListContent(transfersLayoutList: List<TransfersLayout>) {
    LazyColumn(
        modifier = Modifier
            .padding(top = 20.dp)
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        items(transfersLayoutList) { transfersLayout ->
            CardTransferenciaLayout(item = transfersLayout, fecha = formatDate(transfersLayout.createAt))
        }
    }
}
fun formatDate(date: Date): String {
    val locale = Locale("es", "ES") // Configura el idioma español
    val pattern = "dd MMM yyyy HH:mm:ss" // Usa 'MMM' para el nombre corto del mes
    val formatter = SimpleDateFormat(pattern, locale)
    return formatter.format(date)
}
@Composable
fun CardTransferenciaLayout(item: TransfersLayout, fecha: String) {


    val detail = item.detail.first()!!
    val codeArticulo = detail.itemCode
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
            //.border( 0.dp, Color.Black )
            //.clip(RoundedCornerShape(10.dp))
            .background(Color.White)
        //.shadow(elevation = 3.dp, shape = RoundedCornerShape(10.dp))
    ) {
        Card (
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.White)
                .padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 10.dp),
            elevation = 4.dp,
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    AzulVistony202,
                                    AzulVistony201
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .weight(70f)
                            .padding(start = 10.dp)
                    ) {

                        Spacer(modifier = Modifier.height(10.dp))
                        Row (
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Text(text = fecha, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Row (
                            modifier = Modifier
                                .fillMaxWidth()
                        ){
                            Column(
                                modifier = Modifier
                                    .weight(40f)
                            ) {
                                Text(text = "Artículo", fontWeight = FontWeight.Light, color = Color.White)
                            }
                            Column(
                                modifier = Modifier
                                    .weight(60f)
                            ) {
                                Text(text = codeArticulo, fontWeight = FontWeight.Light, color = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    Column (
                        modifier = Modifier
                            .weight(30f)
                            .align(alignment = Alignment.CenterVertically)
                            .padding(end = 10.dp)
                    ){

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            elevation = 0.dp
                        ) {
                            Column (
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 10.dp, end = 10.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ){
                                Text(
                                    "$cantidad",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }                    }
                }
                val colorBlack = Color.Black
                val colorGray = Color.Gray
                Spacer(modifier = Modifier.height(10.dp))
                Row (
                    modifier = Modifier.padding(start = 10.dp)
                ){
                    Text(text = valueArticulo, fontWeight = FontWeight.Bold, color = Color.Black)
                }
                dataRow("Origen", origen, colorBlack)
                dataRow("Destino", destino, colorBlack)
                dataRow("Lote", lote, colorGray)
                dataRow("N° SAP", "$num_sap", colorBlack)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

    }

}
@Composable
fun dataRow(label: String, text: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp)
    ) {
        Column (
            modifier = Modifier
                .weight(32f)
        ){
            Text(text = label, fontWeight = FontWeight.Light, color = color)
        }
        Column (
            modifier = Modifier
                .weight(68f)
        ){
            Text(text = text, fontWeight = FontWeight.Light, color = color)
        }
    }
}


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DialogFindItemScreen(bitmapDeliveryPruebaStatus: MutableState<Boolean>) {
    if(bitmapDeliveryPruebaStatus.value)
    {
        DialogFindItemView(
            //"Captura de Imagen"
            "Busqueda de Producto"
            ,""
            ,onClickCancel = {
                bitmapDeliveryPruebaStatus.value = false
            }
            ,onClickAccept = {
                bitmapDeliveryPruebaStatus.value = false
            }
            ,statusButtonAccept = false
            ,statusButtonIcon = false
            ,context= LocalContext.current
        ){
            val resultEditext : MutableState<String> = remember {mutableStateOf("") }
            val resultEditextLote : MutableState<String> = remember {mutableStateOf("") }
            val resultItemName :MutableState<String> = remember {mutableStateOf("") }
            val context = LocalContext.current
            val findItemRepository = FindItemRepository()
            val findItemViewModel: FindItemViewModel = viewModel(
                factory = FindItemViewModel.FindItemViewModelFactory(
                    findItemRepository,"0",context
                )
            )
            var findItemResponse = findItemViewModel.result.collectAsState()
            findItemViewModel.reset()

            Scaffold()
            {
                Column {
                    Row() {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Box(modifier = Modifier.weight(0.8f)) {
                                TextField(value = resultEditext.value, placeholder = { Text(text = "Código") }, onValueChange = {
                                    resultEditext.value = it
                                })
                            }
                            Box(modifier = Modifier
                                .weight(0.20f)
                                .align(Alignment.CenterVertically)
                                ) {
                                ButtonCircle(
                                    OnClick = {
                                        findItemViewModel.getFindItem(resultEditext.value)
                                    },
                                    roundedCornerShape = RoundedCornerShape(4.dp),
                                    size = DpSize(55.dp, 55.dp)
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_search_white_24dp),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier
                                    )
                                }
                            }
                        }
                        if (findItemResponse.value.status == "Y"){
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                Column(modifier = Modifier.weight(0.8f)) {
                                    TextField(value = resultEditextLote.value, onValueChange = {
                                        resultEditextLote.value = it
                                    })
                                }
                            }
                        }
                    }


                    when (findItemResponse.value.status)
                    {
                        "Y" -> {

                            val filteredData =  remember (resultEditextLote)
                            {
                                derivedStateOf{
                                    if (resultEditextLote.value == "") {
                                        findItemResponse.value.data
                                    } else {
                                        findItemResponse.value.data.filter {
                                                findItem -> findItem.Batch.contains(resultEditextLote.value) }
                                    }
                                }

                            }

                            Row(modifier = Modifier.background(BlueVistony)) {
                                TableCell(
                                    text = resultItemName.value,
                                    color = Color.White,
                                    title = false,
                                    weight = 1f,
                                    textAlign = TextAlign.Center
                                )
                            }
                            LazyColumn(){
                                itemsIndexed(filteredData.value){
                                        _, item ->
                                    resultItemName.value = item.ItemName
                                    CardView(
                                        cardtTittle ={},
                                        cardContent = {
                                            Column(modifier = Modifier
                                                .padding(4.dp)
                                                .fillMaxWidth())
                                            {
                                                Row {
                                                    Row (
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .weight(1f)
                                                    ){
                                                        Text(text = "Ubi.", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Start)
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(text = item.BinCode, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp, textAlign = TextAlign.Start)

                                                    }
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Row (
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .weight(1f)
                                                    ){
                                                        Text(text = "Cant.", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Start)
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        numberForViewDecimals(item.Quantity,2)?.let { it1 ->
                                                            Text(text = it1, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp, textAlign = TextAlign.Start)
                                                        }
                                                    }
                                                }
                                                Row(
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .weight(1f)) {
                                                        Text(text = "Lote", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Start)
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(text = item.Batch, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp, textAlign = TextAlign.Start)
                                                    }
                                                    Spacer(modifier = Modifier.width(4.dp))

                                                    Row (
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .weight(1f)){
                                                        Text(text = "F.venc.", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Start)
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(text = item.DueDate, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp, textAlign = TextAlign.Start)
                                                    }
                                                }
                                            }
                                        },
                                        cardBottom = {}
                                    )
                                }
                            }
                        }
                        "N" -> {
                            Log.e("busquedadebug", findItemResponse.value.status);
                            resultItemName.value = ""
                            Toast.makeText(context, findItemResponse.value.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}


fun numberForViewDecimals(amount: String, decimals: Int): String? {
    var amount = amount
    if (amount ==
        ""
    ) {
        amount = "0"
    }
    val amountRedonded: BigDecimal = BigDecimal(amount).setScale(
        decimals, RoundingMode.HALF_UP
    )
    return amountRedonded.toString()
}