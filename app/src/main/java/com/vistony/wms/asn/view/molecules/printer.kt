package com.vistony.wms.asn.view.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vistony.wms.component.FlagDialog
import com.vistony.wms.component.lockScreen
import com.vistony.wms.model.PrintMachines
import com.vistony.wms.ui.theme.AzulVistony202
import com.vistony.wms.ui.theme.ColorDestine
import com.vistony.wms.viewmodel.ItemsViewModel
import com.vistony.wms.viewmodel.PrintViewModel


@Composable
 fun statusPrinter(printViewModel: PrintViewModel, itemsViewModel: ItemsViewModel, onResponse:(FlagDialog)->Unit){
    val statusPrint = printViewModel.terminationReport.collectAsState()

    when(statusPrint.value.Status){
        ""->{}
        "cargando"->{
            lockScreen("Procesando...")
        }
        "ok"->{
            itemsViewModel.resetArticleStatus()
            printViewModel.resetStatusTerminationReport()
            printViewModel.resetItemStatus()
            //warehouseViewModel.resetLocationStatus()

            onResponse(FlagDialog(true,statusPrint.value.Data))
        }
        else->{
            printViewModel.resetStatusTerminationReport()
            onResponse(FlagDialog(true,statusPrint.value.Status))
        }
    }
}


@Composable
fun listPrinterSection(viewModel: PrintViewModel, value: PrintMachines, onSelect:(PrintMachines)->Unit){

    val listPrint = viewModel.printList.collectAsState()
    val listState = rememberLazyListState()

    LazyRow(modifier= Modifier.background(ColorDestine), state = listState){
        when(listPrint.value.status) {
            "" -> {}
            "cargando" -> {
                item{
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        CircularProgressIndicator(color= AzulVistony202)
                        Text(
                            modifier = Modifier.padding(top=10.dp),
                            text = "Cargando...",
                            textAlign = TextAlign.Center,
                            color= AzulVistony202
                        )
                    }
                }
            }
            "ok" -> {
                itemsIndexed(listPrint.value.prints) { index, line ->
                    Card(
                        elevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .selectable(
                                selected = true,
                                onClick = {
                                    onSelect(
                                        PrintMachines(
                                            name = line.name,
                                            ip = line.uIPAdress,
                                            port = line.uPort
                                        )
                                    )
                                }
                            )
                    ){
                        Box(modifier= Modifier.background(if(value.ip == line.uIPAdress && value.port == line.uPort){
                            Color.LightGray}else{
                            Color.Unspecified})){
                            Column{
                                Text(line.name,modifier= Modifier.padding(5.dp))
                                Text(line.uIPAdress,modifier= Modifier.padding(bottom = 5.dp,start=5.dp,end=5.dp),color= Color.Gray)
                            }

                        }
                    }
                }
            }
            else->{
                item{
                    Column(
                        modifier = Modifier
                            //.fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        Text(
                            modifier = Modifier.padding(top=10.dp),
                            text = "Ocurrio un error "+listPrint.value.status,
                            textAlign = TextAlign.Center,
                            color= Color.Red
                        )
                    }
                }
            }
        }
    }
}