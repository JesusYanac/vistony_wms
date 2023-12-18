package com.vistony.wms.screen

import TransferStockDialog
import android.content.Context
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.vistony.wms.component.TopBarWithBackPress
import com.vistony.wms.model.TransfersLayout
import com.vistony.wms.model.zebraPayload
import com.vistony.wms.ui.theme.AzulVistony201
import com.vistony.wms.ui.theme.AzulVistony202
import com.vistony.wms.viewmodel.TransferStockViewModel
import com.vistony.wms.viewmodel.ZebraViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun TransferStockScreen(navController: NavHostController, context: Context, zebraViewModel: ZebraViewModel) {
    val zebraValue = zebraViewModel.data.collectAsState()

    val transferStockViewModel: TransferStockViewModel = viewModel(
        factory = TransferStockViewModel.TransferStockViewModelFactory( )
    )


    val showPopup : State<Boolean> = transferStockViewModel.showPopup.collectAsState()

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
        topBar = { TopBarWithBackPress(title = "Transferencia de Stock", onButtonClicked = (navController::popBackStack)) },
    ) {

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




