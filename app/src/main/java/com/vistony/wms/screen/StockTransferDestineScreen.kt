package com.vistony.wms.screen

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.vistony.wms.component.TopBar
import com.vistony.wms.viewmodel.StockTransferSubBodyViewModel
import org.bson.types.ObjectId

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun StockTransferDestineScreen(navController: NavHostController, context: Context,subBody:String,producto:String,objType:Int) {

    val stockTransferSubBodyViewModel: StockTransferSubBodyViewModel = viewModel(
        factory = StockTransferSubBodyViewModel.StockTransferSubBodyViewModelModelFactory()
    )

    stockTransferSubBodyViewModel.getSubData(ObjectId(subBody))

    Scaffold(
        topBar = {
           TopBar(if(objType==67){"Transferencia de Stock - Destino"}else if(objType==1701){"Hoja de Alistado - Destino"}else{"Slotting - Destino"})
        }
    ){
        resumen(stockTransferSubBodyViewModel,producto=producto,objType=objType)
    }
}

@Composable
private fun resumen(stockTransferSubBodyViewModel:StockTransferSubBodyViewModel,producto:String,objType:Int){

    val stockTransferSubBodyValue = stockTransferSubBodyViewModel.stockTransferSubBody.collectAsState()


    Log.e("JEPICAME","=HH "+stockTransferSubBodyValue.value.status)

    when(stockTransferSubBodyValue.value.status){
        ""->{
            Text("VACIO...")
        }
        "cargando"->{
            Text("CARGANDO....")
        }
        "ok"->{
            Column(
                modifier= Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
            ){
                Text(producto.replace("+"," "), fontWeight = FontWeight.Bold)
                Text("")
                Column{
                    Text( buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold) ) {
                            append("Lote")
                        }
                        append(" ${stockTransferSubBodyValue.value.data.Batch}")
                    })
                    Text( buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold) ) {
                            append("Ubicación Orig.")
                        }
                        append(" ${stockTransferSubBodyValue.value.data.LocationName}")
                    })
                    Text( buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold) ) {
                            append("Estado")
                        }
                        append(" ${stockTransferSubBodyValue.value.data.Status}")
                    })
                    Text( buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold) ) {
                            append("Transferido")
                        }
                        append(" ${stockTransferSubBodyValue.value.data.Destine.sum("Quantity")} de ${stockTransferSubBodyValue.value.data.Quantity}")
                    })
                    Text("")
                    Divider()
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(5.dp)
                ) {
                    item{

                        Text("")
                    }
                    itemsIndexed(stockTransferSubBodyValue.value.data.Destine) { _, line ->
                        Row(modifier=Modifier.fillMaxWidth().background(Color.LightGray).padding(5.dp),horizontalArrangement = Arrangement.SpaceBetween){
                            Column(modifier=Modifier.weight(0.5f)){
                                Text("Fecha ${line.CreateAt?.getUIStringTimeStampWithDate()}")

                                if(objType!=1701){
                                    Text("Ubicación ${line.LocationName}")
                                }

                                Text("Cantidad ${line.Quantity}")
                            }
                        }
                        Text("")
                    }
                }


                Text("")
            }

            //stockTransferSubBodyViewModel.resetSubBodyState()
        }
        else->{
            Text(" ${stockTransferSubBodyValue.value.message}")
            stockTransferSubBodyViewModel.resetSubBodyState()
        }
    }
}