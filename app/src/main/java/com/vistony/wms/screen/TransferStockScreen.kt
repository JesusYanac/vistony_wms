package com.vistony.wms.screen

import TransferStockDialog
import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.vistony.wms.R
import com.vistony.wms.component.ButtonCircle
import com.vistony.wms.component.CardView
import com.vistony.wms.component.DialogFindItemView
import com.vistony.wms.component.Editext
import com.vistony.wms.model.TransfersLayout
import com.vistony.wms.model.finditem.FindItemRepository
import com.vistony.wms.model.finditem.FindItemViewModel
import com.vistony.wms.model.zebraPayload
import com.vistony.wms.ui.theme.AzulVistony201
import com.vistony.wms.ui.theme.AzulVistony202
import com.vistony.wms.ui.theme.BlueVistony
import com.vistony.wms.viewmodel.TransferStockViewModel
import com.vistony.wms.viewmodel.ZebraViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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

fun DialogFindItemView(findItemViewModel: FindItemViewModel) {

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
                            Text(text = "$fecha", fontWeight = FontWeight.Bold, color = Color.White)
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
                                Text(text = "$codeArticulo", fontWeight = FontWeight.Light, color = Color.White)
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
                    Text(text = "$valueArticulo", fontWeight = FontWeight.Bold, color = Color.Black)
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
            ,"Digitar el código de producto"
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
            /*val cameraImageViewModel: CameraImageViewModel = viewModel()
            CamaraScreen(cameraImageViewModel)*/
            val resultEditext : MutableState<String> = remember {mutableStateOf("") }
            val resultItemName :MutableState<String> = remember {mutableStateOf("") }
            val context = LocalContext.current
            val findItemRepository: FindItemRepository = FindItemRepository()
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
                    Column() {
                        Row() {
                            Column(modifier = Modifier.weight(0.8f)) {
                                Editext(
                                    status = true,
                                    text= resultEditext,
                                    "Ingrese el código",
                                    "Código de Producto ",
                                    painterResource(id = R.drawable.ic_baseline_numbers_24),
                                    KeyboardType.Text,
                                    statusMaxCharacter = false
                                )
                            }
                            //Spacer(modifier = Modifier.width(5.dp))
                            Column(modifier = Modifier
                                .weight(0.20f)
                                .padding(0.dp, 18.dp, 0.dp, 0.dp)) {
                                ButtonCircle(
                                    OnClick = {
                                        /*collectionDetailViewModel.getCollectionDetailPendingDeposit(
                                                DateApp.value
                                        )*/
                                        //headerDispatchSheetViewModel.getMasterDispatchSheetDB(DateApp.value)
                                        findItemViewModel.getFindItem(resultEditext.value)
                                    }, roundedCornerShape = RoundedCornerShape(4.dp)
                                    , size = DpSize(55.dp, 55.dp)
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_search_white_24dp),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier
                                        //tint = if ( stepsStatus.get(index) == "Y") BlueVistony else Color.Gray
                                    )
                                }
                            }
                        }
                    }


                    when (findItemResponse.value.status)
                    {
                        "Y" -> {
                            Row(modifier = Modifier.background(BlueVistony)) {
                                TableCell(
                                    text = resultItemName.value,
                                    color = Color.White,
                                    title = false,
                                    weight = 1f,
                                    textAlign = TextAlign.Center
                                )
                            }
                            LazyColumn(
                                //modifier = Modifier.fillMaxWidth()
                            )
                            {
                                itemsIndexed(findItemResponse.value.data){
                                        _, item ->
                                    resultItemName.value = item.ItemName
                                    CardView(
                                        cardtTittle ={},
                                        cardContent = {
                                            Column(modifier = Modifier
                                                .padding(10.dp)
                                                .fillMaxWidth())
                                            {
                                                Row(
                                                ) {
                                                    Column(
                                                        horizontalAlignment = Alignment.Start,
                                                        modifier=Modifier.weight(0.5f)
                                                    ) {
                                                        Row() {
                                                            TableCell(
                                                                text = "Ubicación",
                                                                color = Color.Gray,
                                                                title = false,
                                                                weight = 1f,
                                                                textAlign = TextAlign.End
                                                            )
                                                        }
                                                        Row() {
                                                            TableCell(
                                                                text = item.BinCode,
                                                                //color = textColor,
                                                                title = true,
                                                                weight = 1f,
                                                                textAlign = TextAlign.End,
                                                            )
                                                        }
                                                    }
                                                    Column(
                                                        horizontalAlignment = Alignment.Start,
                                                        modifier=Modifier.weight(0.5f)
                                                    ) {
                                                        Row() {
                                                            TableCell(
                                                                //text = "${Convert.currencyForView(invoices?.)}",
                                                                text = "Cantidad" ,
                                                                color = Color.Gray,
                                                                title = false,
                                                                weight = 1f,
                                                                textAlign = TextAlign.End
                                                            )
                                                        }
                                                        //TextLabel(text ="Saldo" , textAlign = TextAlign.Center )
                                                        Row() {
                                                            numberForViewDecimals(item.Quantity,2)?.let { it1 ->
                                                                TableCell(
                                                                    text = it1,
                                                                    title = true,
                                                                    weight = 1f,
                                                                    textAlign = TextAlign.End
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                                Row(
                                                ) {
                                                    Column(
                                                        horizontalAlignment = Alignment.Start,
                                                        modifier=Modifier.weight(0.5f)
                                                    ) {
                                                        Row() {
                                                            TableCell(
                                                                text = "Lote",
                                                                color = Color.Gray,
                                                                title = false,
                                                                weight = 1f,
                                                                textAlign = TextAlign.End
                                                            )
                                                        }
                                                        Row() {
                                                            TableCell(
                                                                text = item.Batch,
                                                                title = true,
                                                                weight = 1f,
                                                                textAlign = TextAlign.End
                                                            )
                                                        }
                                                    }
                                                    Column(
                                                        horizontalAlignment = Alignment.Start,
                                                        modifier=Modifier.weight(0.5f)
                                                    ) {
                                                        Row() {
                                                            TableCell(
                                                                //text = "${Convert.currencyForView(invoices?.)}",
                                                                text = "F. Vencimiento" ,
                                                                color = Color.Gray,
                                                                title = false,
                                                                weight = 1f,
                                                                textAlign = TextAlign.End
                                                            )
                                                        }
                                                        Row() {
                                                            TableCell(
                                                                text = item.DueDate,
                                                                title = true,
                                                                weight = 1f,
                                                                textAlign = TextAlign.End
                                                            )
                                                        }
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
    val locale: Locale? = null
    val amountRedonded: BigDecimal = BigDecimal(amount).setScale(
        decimals, RoundingMode.HALF_UP
    )
    return amountRedonded.toString()
}