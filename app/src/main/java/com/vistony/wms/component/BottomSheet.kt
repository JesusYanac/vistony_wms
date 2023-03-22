package com.vistony.wms.component

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vistony.wms.R
import com.vistony.wms.num.TypeReadSKU
import com.vistony.wms.model.*
import com.vistony.wms.num.TypeCode
import com.vistony.wms.ui.theme.AzulVistony202
import com.vistony.wms.viewmodel.*

@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@Composable
fun SheetLayout(currentScreen: BottomSheetScreen,onCloseBottomSheet :()->Unit,showIconClose:Boolean=true) {
    BottomSheetWithCloseDialog(onCloseBottomSheet,showIconClose=showIconClose){
        when(currentScreen){
            is BottomSheetScreen.SelectWarehouseModal ->
                SelectWarehouseModal(context=currentScreen.context,selected=currentScreen.selected)
            is BottomSheetScreen.SelectTypeModal ->
                SelectTypeModal(selected=currentScreen.selected)
            is BottomSheetScreen.ShowMessageModal ->
                ShowMessageModal(message =currentScreen.message )
            is BottomSheetScreen.SelectCountryModal ->
                SelectCountryModal(selected=currentScreen.selected)
            is BottomSheetScreen.SelectWitOptionsModal ->
                SelectWitOptionsModal(title = currentScreen.title, listOptions = currentScreen.listOptions, selected = currentScreen.selected)
            is BottomSheetScreen.SelectOriginModal ->
                SelectTypescanModal(
                    idHeader=currentScreen.idHeader,
                    value=currentScreen.value,
                    context = currentScreen.context,
                    type = currentScreen.type,
                    selected = currentScreen.selected,
                    whsOrigin=currentScreen.whsOrigin,
                    objType=currentScreen.objType,
                    onCloseBottomSheet=onCloseBottomSheet
                )
            is BottomSheetScreen.SelectDestineModal ->
                SelectDestineModal(
                    objType=currentScreen.objType,
                    value = currentScreen.value,context=currentScreen.context,
                    stockTransferBodyViewModel = currentScreen.stockTransferBodyViewModel,
                    selected = currentScreen.selected,
                    wareHouseOrigin=currentScreen.wareHouseOrigin,
                    wareHouseDestine=currentScreen.wareHouseDestine,
                    onCloseBottomSheet=onCloseBottomSheet
                )
            is BottomSheetScreen.SelectFormHeaderTask ->SelectFormHeaderTask(
                payloadForm=currentScreen.payloadForm,
                stockTransferHeaderViewModel=currentScreen.stockTransferHeaderViewModel,
                taskManagement=currentScreen.taskManagement,
                context=currentScreen.context,onSendBody=currentScreen.onSendBody,onCloseBottomSheet=onCloseBottomSheet)
        }
    }
}

sealed class BottomSheetScreen(){
    class SelectWarehouseModal(val context: Context,val selected: (WarehouseBinLocation) -> Unit) : BottomSheetScreen()
    class SelectWitOptionsModal(val title: String,val listOptions:List<Options>,val selected:(Options)->Unit) : BottomSheetScreen()
    class SelectTypeModal(val selected: (TypeInventario) -> Unit) : BottomSheetScreen()
    class ShowMessageModal(val message:String) : BottomSheetScreen()
    class SelectCountryModal(val selected: (CountryLocation) -> Unit):BottomSheetScreen()
    class SelectOriginModal(val idHeader:String,val value:zebraPayload=zebraPayload(), val context: Context, val type:TypeReadSKU,
                            val selected: (List<StockTransferBodyPayload>) -> Unit,val whsOrigin:String,val objType:Int):BottomSheetScreen()
    class SelectDestineModal(val objType:Int=0,val value:zebraPayload = zebraPayload(),val context: Context,val wareHouseOrigin: String,
                             val wareHouseDestine:String,
                             val stockTransferBodyViewModel:StockTransferBodyViewModel,
                             val selected: (List<StockTransferPayloadVal>) -> Unit): BottomSheetScreen()
    class SelectFormHeaderTask(
        val payloadForm:TaskMngmtDataForm,
        val stockTransferHeaderViewModel:StockTransferHeaderViewModel,
        val taskManagement:TaskMngmtAndHeaderDoc,val context: Context,val onSendBody:(StockTransferHeaderRI)->Unit ):BottomSheetScreen()
}

@Composable
fun SelectFormHeaderTask(
    payloadForm:TaskMngmtDataForm,
    stockTransferHeaderViewModel:StockTransferHeaderViewModel,
    taskManagement:TaskMngmtAndHeaderDoc,context:Context,onSendBody :(StockTransferHeaderRI)->Unit , onCloseBottomSheet :()->Unit ) {

    val resultUpdateValue = stockTransferHeaderViewModel.result.collectAsState()



    val payloadFormx: TaskMngmtDataForm = stockTransferHeaderViewModel.form.collectAsState().value

    when(resultUpdateValue.value.request){
        "",
        "vacio"->{}
        "cargando"->{
            CustomProgressDialog("Actulizando documento...")
        }
        "ok"->{
            onSendBody(resultUpdateValue.value)
            stockTransferHeaderViewModel.resetResultHeader()
        }
        else->{
            Toast.makeText(context, "${resultUpdateValue.value.status}", Toast.LENGTH_LONG).show()
            stockTransferHeaderViewModel.resetResultHeader()
        }
    }

    Column(modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)) {
        Text(
            text = " ${taskManagement.Task.Documento} N°${taskManagement.Task.DocNum}",
            modifier = Modifier.padding(start = 20.dp, bottom = 10.dp, end = 50.dp)
        )

        Divider()

        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ){
            formBsHeaderTask(
                taskMngmtDataForm=payloadFormx,
                taskManagement=taskManagement,
                onClose = {
                    onCloseBottomSheet()
                },
                onChange={
                    stockTransferHeaderViewModel.onFormChanged(it)
                },
                onPress = {
                    stockTransferHeaderViewModel.updateHeader(
                        taskMngmtDataForm=it
                    )
                }
            )
        }
    }

}


@Composable
fun SelectWitOptionsModal(title:String,listOptions:List<Options>,selected:(Options)->Unit){

    Column(modifier=Modifier.padding(top=20.dp, bottom = 10.dp)){
        Text(text=title,color=Color.Gray,modifier=Modifier.padding(start=20.dp,bottom=10.dp))
        Divider(modifier= Modifier
            .fillMaxWidth(0.8f)
            .padding(start = 20.dp, bottom = 10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ){
            itemsIndexed(listOptions) { _, line ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = {
                            selected(line)
                        })
                        .height(55.dp)
                        .padding(start = 25.dp), verticalAlignment = Alignment.CenterVertically
                ){
                    Icon(painter = painterResource(id = line.icono), contentDescription = null, tint = AzulVistony202)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column{
                        Text(text = line.text , color = Color.DarkGray)
                    }

                }
            }
        }
    }
}

/*fun masCercano(numeros: List<StockTransferSubBody>, num: Double): Double {
    var cercano = 0.0
    var diferencia = Double.MAX_VALUE //inicializado valor máximo de variable de tipo int
    for (i in numeros.indices) {
        if (numeros[i].Quantity == num) {
            return numeros[i].Quantity
        } else {
            if (abs(numeros[i].Quantity - num) < diferencia) {
                cercano = numeros[i].Quantity
                diferencia = abs(numeros[i].Quantity - num)
            }
        }
    }

    return cercano
}*/

@Composable
fun SelectDestineModal(
    objType:Int=0,
    value:zebraPayload=zebraPayload(),
    context:Context,
    stockTransferBodyViewModel:StockTransferBodyViewModel,
    wareHouseOrigin: String,
    wareHouseDestine: String,
    selected: (List<StockTransferPayloadVal>) -> Unit,
    onCloseBottomSheet :()->Unit
){
    val suggestionViewModel: SuggestionViewModel = viewModel(
        factory = SuggestionViewModel.SuggestionViewModelFactory()
    )

    if(value.Payload.isNotEmpty() && value.Type in listOf("LABEL-TYPE-QRCODE","LABEL-TYPE-EAN128")){
        stockTransferBodyViewModel.getBodyAndSubBody(value)

        if(value.Type=="LABEL-TYPE-QRCODE" && objType!=1701){
            suggestionViewModel.getSuggestionList("PICKING",wareHouseDestine)
        }
    }

    Rowasd(
        objType=objType,
        value=value,
        stockTransferBodyViewModel=stockTransferBodyViewModel,
        suggestionViewModel=suggestionViewModel,
        context=context,
        wareHouseOrigin=wareHouseOrigin,
        wareHouseDestine=wareHouseDestine,
        onSelect={
          selected( it )
        },Onclose={
            onCloseBottomSheet()
        }
    )
}

@Composable
fun Rowasd(
    objType:Int,
    value:zebraPayload,
    stockTransferBodyViewModel:StockTransferBodyViewModel,
    suggestionViewModel: SuggestionViewModel,
    context: Context,
    wareHouseOrigin:String,
    wareHouseDestine:String,
    onSelect: (List<StockTransferPayloadVal>) -> Unit,
    Onclose :()->Unit
){
    val stockTrnsfBySbRspnsValue = stockTransferBodyViewModel.stockTransferBodyAndSubBodyResponse.collectAsState()
    val suggestionValue = suggestionViewModel.suggtn.collectAsState()

    //var stockTransferBandSRpsValue by remember { mutableStateOf(StockTransferBodyAndSubBodyResponse()) }

    /*when(stockTransferBodyAndSubBodyResponseValue.value.status){
        ""->{}
        "cargando"->{
            Column(modifier=Modifier.padding(top=20.dp, bottom = 10.dp)) {
                Text(
                    text = " Buscando Producto en la ficha",
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 20.dp, bottom = 10.dp, end = 50.dp)
                )
                Column(modifier= Modifier.padding(20.dp)) {
                    Text("...")
                }
            }
        }
        "ok"->{
            stockTransferBandSRpsValue=stockTransferBodyAndSubBodyResponseValue.value
            stockTransferBodyViewModel.resetBodyAndSubBodyState()
        }
        else->{

            stockTransferBandSRpsValue=stockTransferBodyAndSubBodyResponseValue.value
            stockTransferBodyViewModel.resetBodyAndSubBodyState()
        }
    }*/

    when(stockTrnsfBySbRspnsValue.value.status){
        ""->{}
        "cargando"->{
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
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
        "ok"->{

            val mergedList = stockTrnsfBySbRspnsValue.value.response.merge()

            if(stockTrnsfBySbRspnsValue.value.type==TypeCode.SSCC){
                Column(
                    modifier=Modifier.padding(top=20.dp, bottom = 10.dp)
                ){

                    Text(
                        text = " SSCC ${mergedList[0].Sscc}",
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 20.dp, bottom = 10.dp, end = 50.dp)
                    )

                    Column(
                        modifier= Modifier
                            .padding(start = 20.dp, end = 20.dp)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ){
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier= Modifier.fillMaxWidth()
                        ) {
                            if(objType==22){
                                Text(text ="Proveedor")
                                Text(text = " ${wareHouseOrigin}",color= Color.Gray)
                            }else{
                                Text(text ="Almacén Orig.")
                                Text(text = " ${wareHouseOrigin}",color= Color.Gray)
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier= Modifier.fillMaxWidth()
                        ){
                            if(objType==1701){
                                /*Text(text ="Proveedor")
                                Text(text = " ${wareHouseOrigin}",color= Color.Gray)*/
                            }else{
                                Text(text ="Almacén Dst.")
                                Text(text = " ${wareHouseDestine}",color= Color.Gray)
                            }
                        }

                        Divider()
                        Text("")

                        LazyRow(modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.6f)){
                            items(items = mergedList, itemContent = { item ->
                                Card(
                                    backgroundColor = if(item.Quantity<=item.QuantityDestine){Color.Gray}else{Color.White},
                                    elevation = 10.dp,
                                    modifier = Modifier.fillMaxWidth().padding(5.dp)
                                ){
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row{
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_baseline_palet_on_24),
                                                contentDescription = "Palet",
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = " Código "+item.ItemCode,
                                                style = MaterialTheme.typography.subtitle1,
                                                color = MaterialTheme.colors.primary
                                            )

                                        }

                                        Text(
                                            maxLines=2,
                                            overflow=TextOverflow.Ellipsis,
                                            modifier=Modifier.widthIn(max=200.dp),
                                            text = item.ItemName,
                                            style = MaterialTheme.typography.subtitle2,
                                            color = MaterialTheme.colors.onBackground
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))
                                        /////
                                        Row {
                                            Text(
                                                text = "Cantidad Encontrada: ",
                                                style = MaterialTheme.typography.subtitle1,
                                                color = MaterialTheme.colors.onBackground
                                            )
                                            Text(
                                                text = ""+item.Quantity,
                                                style = MaterialTheme.typography.subtitle1,
                                                color = MaterialTheme.colors.primary
                                            )
                                        }
                                        Row {
                                            Text(
                                                text = "Cantidad Ubicada: ",
                                                style = MaterialTheme.typography.subtitle1,
                                                color = MaterialTheme.colors.onBackground
                                            )
                                            Text(
                                                text = ""+item.QuantityDestine,
                                                style = MaterialTheme.typography.subtitle1,
                                                color = MaterialTheme.colors.primary
                                            )
                                        }
                                        Row {
                                            Text(
                                                text = "Lote: ",
                                                style = MaterialTheme.typography.subtitle1,
                                                color = MaterialTheme.colors.onBackground
                                            )
                                            Text(
                                                text = item.Batch,
                                                style = MaterialTheme.typography.subtitle1,
                                                color = MaterialTheme.colors.primary
                                            )
                                        }
                                    }
                                }

                            })
                        }

                        Text("")

                        var  saldo = mergedList.map {
                            it.Quantity==it.QuantityDestine
                        }

                        val result = saldo.reduce { acc, b -> acc && b }

                        /////////////////////////////////////UBICACION SUGERIDA DESTINO/////////////////////////////////////
                        if(result){
                            Text("No hay articulos pendientes de ubicar en este SSCC")
                        }else{
                            /////////////////////////////////////TIPO DE UBICACION/////////////////////////////////////
                            var typeLocation by remember { mutableStateOf("") }
                            var destineLocation by remember { mutableStateOf(BinLocation()) }

                            if(objType!=1701){
                                Text("Tipo de ubicación "+if(typeLocation.isNullOrEmpty()){""}else{"[${typeLocation}]"}, fontWeight = FontWeight.Bold)

                                showTypeLocation(
                                    objType=objType,
                                    //type=stockTrnsfBySbRspnsValue.value.type,
                                    value=typeLocation,
                                    onSelect={
                                        typeLocation=it
                                        suggestionViewModel.getSuggestionList(it,wareHouseDestine)
                                    }
                                )
                            }

                            Text("")
                            ////////////////////////////////////////////////////////////////////////
                            if(objType!=1701 && typeLocation.isNotEmpty()){ //ES DIFERENTE A PICKING
                                Text("Ubicación destino "+if(destineLocation.text.isNullOrEmpty()){""}else{"[${destineLocation.text}]"}, fontWeight = FontWeight.Bold)
                                xddVs2(
                                    stockTransferBandSRpsValue=mergedList,
                                    suggestions=suggestionValue.value,
                                    value=if(destineLocation.text.isNotEmpty() || destineLocation.text!=value.Payload){value.Payload}else{""},
                                    onLoader = {
                                        Log.e("JEPICAME","LA UBICACIO SUGERIDA ES "+it.text )
                                        if(it.text!=destineLocation.text || value.Payload.split("-").size<3){
                                            destineLocation=it
                                        }
                                    }
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ){
                                Button(
                                    colors= ButtonDefaults.buttonColors(backgroundColor = Color.Gray),
                                    onClick = {

                                        Log.e("JEPICAME","VA A AGREGARLO")

                                        //val tempList = mergedList.filter{it.Quantity<=it.QuantityDestine}.map{
                                        val tempList = mergedList.map{
                                            StockTransferPayloadVal(
                                                quantity=it.Quantity, //_StockTransferBody
                                                idBody = it._StockTransferBody, // stockTransferBandSRpsValue.stockTransferBody._id,
                                                batch=it.Batch,
                                                sscc=it.Sscc!!,
                                                origin=listOf(
                                                    ManyToOne(
                                                        id=it._StockTransferSubBody,
                                                        locationCode = it.LocationCode,
                                                        locationName = it.LocationName,
                                                        quantityNow =0.0,
                                                        quantityUsed = it.Quantity,
                                                        quantityAvailable = 0.0
                                                    )
                                                ),
                                                destine=destineLocation
                                            )
                                        }

                                        Log.e("JEPICAME","VA A AGREGARLO "+tempList.size)

                                        onSelect(tempList)
                                    }
                                ){
                                    Text(
                                        text = "Agregar",
                                        color=Color.White
                                    )
                                }
                                Button( onClick = {
                                    Onclose()
                                }
                                ){
                                    Text(
                                        text = "Cancelar"
                                    )
                                }
                            }
                        }

                    }
                }
            }
            else{
                if(mergedList.size!=1){
                    showMessageModalError("Solo los códigos SSCC pueden albergar más de un producto")
                }else{
                    Column(
                        modifier=Modifier.padding(top=20.dp, bottom = 10.dp)
                    ){
                        Text(
                            text = " ${mergedList[0].ItemName}",
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 20.dp, bottom = 10.dp, end = 50.dp)
                        )

                        Column(
                            modifier= Modifier
                                .padding(start = 20.dp, end = 20.dp)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                        ){
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier= Modifier.fillMaxWidth()
                            ) {
                                if(objType==22){
                                    Text(text ="Proveedor")
                                    Text(text = " ${wareHouseOrigin}",color= Color.Gray)
                                }else{
                                    Text(text ="Almacén Orig.")
                                    Text(text = " ${wareHouseOrigin}",color= Color.Gray)
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier= Modifier.fillMaxWidth()
                            ){
                                if(objType==1701){
                                    /*Text(text ="Proveedor")
                                    Text(text = " ${wareHouseOrigin}",color= Color.Gray)*/
                                }else{
                                    Text(text ="Almacén Dst.")
                                    Text(text = " ${wareHouseDestine}",color= Color.Gray)
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier= Modifier.fillMaxWidth()
                            ){
                                Text(text ="Lote")
                                Text(text = " ${mergedList[0].Batch}",color= Color.Gray)
                            }

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier= Modifier.fillMaxWidth()
                            ){
                                Text(text ="Cantidad pendiente")
                                Text(text = " ${mergedList[0].Quantity - mergedList[0].QuantityDestine}",color= Color.Gray)
                            }

                            Divider()
                            Text("")

                            /////////////////////////////////////TIPO DE UBICACION/////////////////////////////////////
                            var destineLocation by remember { mutableStateOf(BinLocation()) }

                            if(objType!=1701){ //ES DIFERENTE A PICKING
                                Text("Ubicación destino sugerida "+if(destineLocation.text.isNullOrEmpty()){""}else{"[${destineLocation.text}]"}, fontWeight = FontWeight.Bold)
                                Text("")

                                xddVs2(
                                    stockTransferBandSRpsValue=mergedList,
                                    suggestions=suggestionValue.value,
                                    value=if(destineLocation.text.isNotEmpty() || destineLocation.text!=value.Payload){value.Payload}else{""},
                                    onLoader = {
                                        if(it.text!=destineLocation.text || value.Payload.split("-").size<3){
                                            destineLocation=it
                                        }
                                    }
                                )
                            }

                           FormLocationDestine(
                               quantityText=if(objType==1701){"${mergedList[0].Quantity - mergedList[0].QuantityDestine}"}else{"1.00"},
                               onPress={

                                    val locationOrigins:List<ManyToOne> = calculateBinLocationOrigin(it.quantity,mergedList)

                                    if(locationOrigins.isEmpty()){
                                        Toast.makeText(context, "Stock Insuficiente", Toast.LENGTH_LONG).show()
                                    }else{
                                        if(destineLocation.text.isNullOrEmpty() && objType!=1701){
                                            Toast.makeText(context, "Es necesario ingresar una ubicación destino", Toast.LENGTH_LONG).show()
                                        }else{
                                            when(objType){
                                                1701->{
                                                    if(mergedList.sumOf{it.Quantity}==it.quantity){
                                                        it.idBody=mergedList[0]._StockTransferBody
                                                        it.batch=mergedList[0].Batch
                                                        it.origin=locationOrigins
                                                        it.destine=destineLocation
                                                        onSelect( listOf(it))
                                                    }else{
                                                        Toast.makeText(context, "Es necesario que la cantidad ingresada sea igual a la cantidad solicitada.", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                                else->{
                                                    it.idBody=mergedList[0]._StockTransferBody
                                                    it.batch=mergedList[0].Batch
                                                    it.origin=locationOrigins
                                                    it.destine=destineLocation

                                                    onSelect( listOf(it))
                                                }
                                            }
                                        }
                                    }

                               },onClosePressed={
                                   Onclose()
                               }
                           )
                        }
                    }
                }
            }


   //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/*
            if( stockTransferBandSRpsValue.stockTransferSubBody.sumOf { it.Quantity } <= stockTransferBandSRpsValue.quantityDestine ) {
                showMessageModal("El producto ${stockTransferBandSRpsValue.stockTransferBody.ItemCode} con lote ${stockTransferBandSRpsValue.stockTransferSubBody[0].Batch}, no tiene stock pendiente de ubicar.")
            }
            else{
                Column(
                    modifier=Modifier.padding(top=20.dp, bottom = 10.dp)
                ){
                    if(value.Payload.length==18){
                        Text(
                            text = " SSCC ${value.Payload}",
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 20.dp, bottom = 10.dp, end = 50.dp)
                        )
                    }else{
                        Text(
                            text = " ${stockTransferBandSRpsValue.stockTransferBody.ItemName}",
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 20.dp, bottom = 10.dp, end = 50.dp)
                        )
                    }

                    Column(
                        modifier= Modifier
                        .padding(start = 20.dp, end = 20.dp)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                    ){
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier= Modifier.fillMaxWidth()
                        ) {
                            if(objType==22){
                                Text(text ="Proveedor")
                                Text(text = " ${wareHouseOrigin}",color= Color.Gray)
                            }else{
                                Text(text ="Almacén Orig.")
                                Text(text = " ${wareHouseOrigin}",color= Color.Gray)
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier= Modifier.fillMaxWidth()
                        ){
                            if(objType==1701){
                                /*Text(text ="Proveedor")
                                Text(text = " ${wareHouseOrigin}",color= Color.Gray)*/
                            }else{
                                Text(text ="Almacén Dst.")
                                Text(text = " ${wareHouseDestine}",color= Color.Gray)
                            }
                        }

                        if(stockTransferBandSRpsValue.stockTransferSubBody[0].Sscc!!.length!=18){
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier= Modifier.fillMaxWidth()
                            ){
                                Text(text ="Lote")
                                Text(text = " ${stockTransferBandSRpsValue.stockTransferSubBody[0].Batch}",color= Color.Gray)
                            }

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier= Modifier.fillMaxWidth()
                            ){
                                Text(text ="Cantidad pendiente")
                                Text(text = " ${(stockTransferBandSRpsValue.stockTransferSubBody.sumOf { it.Quantity } )-(stockTransferBandSRpsValue.quantityDestine)}",color= Color.Gray)
                            }

                            Divider()
                            Text("")
                        }
                        else{
                            Divider()
                            Text("")

                            LazyRow(modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.6f)) {
                                items(items = stockTransferBandSRpsValue.stockTransferSubBody, itemContent = { item ->

                                    Card(
                                        backgroundColor = Color.White,
                                        elevation = 10.dp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(5.dp)
                                    ){
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = stockTransferBandSRpsValue.stockTransferBody.ItemCode +"\n"+stockTransferBandSRpsValue.stockTransferBody.ItemName,
                                                style = MaterialTheme.typography.subtitle2,
                                                color = MaterialTheme.colors.onBackground
                                            )
                                            Row {
                                                Text(
                                                    text = "Cantidad: ",
                                                    style = MaterialTheme.typography.subtitle1,
                                                    color = MaterialTheme.colors.onBackground
                                                )
                                                Text(
                                                    text = ""+item.Quantity,
                                                    style = MaterialTheme.typography.subtitle1,
                                                    color = MaterialTheme.colors.primary
                                                )
                                            }
                                            Row {
                                                Text(
                                                    text = "Lote: ",
                                                    style = MaterialTheme.typography.subtitle1,
                                                    color = MaterialTheme.colors.onBackground
                                                )
                                                Text(
                                                    text = item.Batch,
                                                    style = MaterialTheme.typography.subtitle1,
                                                    color = MaterialTheme.colors.primary
                                                )
                                            }
                                        }
                                    }

                                })
                            }
                            Text("")
                        }

                        /////////////////////////////////////UBICACION SUGERIDA DESTINO/////////////////////////////////////

                        var destineLocation by remember { mutableStateOf(BinLocation()) }
                        if(objType!=1701){ //ES DIFERENTE A PICKING
                            Text("Ubicación destino sugerida "+if(destineLocation.text.isNullOrEmpty()){""}else{"[${destineLocation.text}]"}, fontWeight = FontWeight.Bold)
                            Text("")

                            xdd(
                                stockTransferBandSRpsValue=stockTransferBandSRpsValue,
                                suggestions=suggestionValue.value,
                                value=if(destineLocation.text.isNotEmpty() || destineLocation.text!=value.Payload){value.Payload}else{""},
                                onLoader = {
                                    if(it.text!=destineLocation.text || value.Payload.split("-").size<3){
                                        destineLocation=it
                                    }
                                }
                            )
                        }

                        if(stockTransferBandSRpsValue.stockTransferSubBody[0].Sscc!!.length!=18){
                            FormLocationDestine(
                                onPress={
                                    val locationOrigins:List<ManyToOne> = calculateBinLocationOrigin(it.quantity,stockTransferBandSRpsValue.stockTransferSubBody)

                                    if(locationOrigins.isEmpty()){
                                        Toast.makeText(context, "Stock Insuficiente", Toast.LENGTH_LONG).show()
                                    }else{
                                        if(destineLocation.text.isNullOrEmpty() && objType!=1701){
                                            Toast.makeText(context, "Es necesario ingresar una ubicación destino", Toast.LENGTH_LONG).show()
                                        }else{
                                            when(objType){
                                                1701->{
                                                    if(stockTransferBandSRpsValue.stockTransferSubBody.sumOf{it.Quantity}==it.quantity){
                                                        it.idBody=stockTransferBandSRpsValue.stockTransferBody._id
                                                        it.batch=stockTransferBandSRpsValue.stockTransferSubBody[0].Batch
                                                        it.origin=locationOrigins
                                                        it.destine=destineLocation
                                                        onSelect( listOf(it))
                                                    }else{
                                                        Toast.makeText(context, "Es necesario que la cantidad ingresada sea igual a la cantidad solicitada.", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                                else->{
                                                    it.idBody=stockTransferBandSRpsValue.stockTransferBody._id
                                                    it.batch=stockTransferBandSRpsValue.stockTransferSubBody[0].Batch
                                                    it.origin=locationOrigins
                                                    it.destine=destineLocation

                                                    onSelect( listOf(it))
                                                }
                                            }
                                        }
                                    }

                                },onClosePressed={
                                    Onclose()
                                }
                            )
                        }
                        else{
                            Text("")
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ){
                                Button(
                                    colors= ButtonDefaults.buttonColors(backgroundColor = Color.Gray),
                                    onClick = {
                                        val tempList = stockTransferBandSRpsValue.stockTransferSubBody.map{
                                            StockTransferPayloadVal(
                                                quantity=it.Quantity,
                                                idBody = stockTransferBandSRpsValue.stockTransferBody._id,
                                                batch=it.Batch,
                                                origin=listOf(
                                                    ManyToOne(
                                                        id=it._id,
                                                        locationCode = it.LocationCode,
                                                        locationName = it.LocationName,
                                                        quantityNow =0.0,
                                                        quantityUsed = it.Quantity,
                                                        quantityAvailable = 0.0
                                                    )
                                                ),
                                                destine=destineLocation
                                            )
                                        }

                                        Log.e("JEPCIAME","TODO OK")
                                        onSelect(tempList)
                                    }
                                ){
                                    Text(
                                        text = "Agregar",
                                        color=Color.White
                                    )
                                }
                                Button( onClick = {
                                    Onclose()
                                }
                                ){
                                    Text(
                                        text = "Cancelar"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            */
        }
        else->{
            showMessageModalError(stockTrnsfBySbRspnsValue.value.status)
        }
    }
    /*
        coroutineScope.launch {

            val visibleItemsInfo = listState.layoutInfo.visibleItemsInfo
            val visibleSet = visibleItemsInfo.map { it.index }.toSet()
            if (locationDestine.indexOf(binLocation) == visibleItemsInfo.last().index) {
                listState.animateScrollToItem(index=locationDestine.indexOf(binLocation))

            } else if (visibleSet.contains(locationDestine.indexOf(binLocation)) && locationDestine.indexOf(binLocation) != 0) {
                listState.animateScrollToItem(index=(locationDestine.indexOf(binLocation) - 1))
            }

        }
    */

}

@Composable
private fun showMessageModalError(mensaje:String){
    Column(modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)) {
        Text(
            text = " Ocurrio un error",
            color = Color.Gray,
            modifier = Modifier.padding(start = 20.dp, bottom = 10.dp, end = 50.dp)
        )

        Divider()

        Column(
            modifier = Modifier
                .padding(20.dp)
        ) {
            Text(mensaje)
        }
    }
}

private fun calculateBinLocationOrigin(cantidadSolicitada:Double,listSubBody:List<MergedStockTransfer>):List<ManyToOne>{

    val mutableList: MutableList<ManyToOne> = object : ArrayList<ManyToOne>(){}
    /////////////////////////////////////////////////////
    //var xd=masCercano(stockTransferBandSRpsValue.stockTransferSubBody,cantidadSolicitada.toDouble())
    Log.e("JEPICAME","ANTES DE LIMPIAR "+mutableList.size)

    mutableList.clear()
    Log.e("JEPICAME","DSP DE LIMPIAR "+mutableList.size)
    /////////////////////////////////////////////////////
    var data=listSubBody
    var resto:Double= cantidadSolicitada

    if( cantidadSolicitada <= listSubBody.sumOf{ (it.Quantity - it.Destine.sum("Quantity").toDouble() ) } ){

        data=data.sortedByDescending { it.Quantity }
        data.forEachIndexed{ i,item->

            if(resto==0.0 || item.Destine.sum("Quantity").toDouble()==item.Quantity){
                Log.e("JEPICAME","ENTRO A IF RETURN")
                return@forEachIndexed
            }
            //7  > 3
            if((item.Quantity - item.Destine.sum("Quantity").toDouble())>resto){
                var temp=(item.Quantity- item.Destine.sum("Quantity").toDouble())-resto

                mutableList.add(ManyToOne(
                    id=item._StockTransferSubBody,
                    locationCode = item.LocationCode,
                    locationName = item.LocationName,
                    quantityNow =item.Quantity,
                    quantityUsed = resto,
                    quantityAvailable = temp
                ))

                resto-=resto
                Log.e("JEPICAME","ENTRO A TENIA MAYOR A SOLICITADO")
            }else if((item.Quantity- item.Destine.sum("Quantity").toDouble())==resto){

                mutableList.add(ManyToOne(
                    id=item._StockTransferSubBody,
                    locationCode = item.LocationCode,
                    locationName = item.LocationName,
                    quantityNow =item.Quantity,
                    quantityUsed = (item.Quantity- item.Destine.sum("Quantity").toDouble()),
                    quantityAvailable = 0.0
                ))
                resto=0.0
                // 20 < 1
                Log.e("JEPICAME","ENTRO A TENIA IGUAL A SOLICITADO")
            }else if((item.Quantity - item.Destine.sum("Quantity").toDouble())< resto){

                mutableList.add(ManyToOne(
                    id=item._StockTransferSubBody,
                    locationCode = item.LocationCode,
                    locationName = item.LocationName,
                    quantityNow =(item.Quantity- item.Destine.sum("Quantity").toDouble()),
                    quantityUsed = (item.Quantity-item.Destine.sum("Quantity").toDouble()),
                    quantityAvailable = 0.0
                ))

                resto-=(item.Quantity- item.Destine.sum("Quantity").toDouble())

                Log.e("JEPICAME","ENTRO A TENIA MENOR A LO SOLICITADO")
            }

        }

        /////////////////////////////////////////////////////

        mutableList.forEach{
            Log.e("JEPICAME "+mutableList.size,"Para "+cantidadSolicitada+" se utilizara de "+it.locationName +" de "+it.quantityNow+" la cantidad de "+it.quantityUsed +" y queda en la ubicación "+it.quantityAvailable)
        }

    }else{
        Log.e("JEPICAME","Stock insuficiente "+listSubBody.sumOf{ it.Quantity } +" Se solicito "+cantidadSolicitada+" ref=>"+listSubBody[0]._StockTransferBody)
    }
    /////////////////////////////////////////////////////

    return mutableList
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FormLocationDestine(
    quantityText:String,
    onPress:(StockTransferPayloadVal)->Unit,
    onClosePressed: () -> Unit
){

    var quantity by remember { mutableStateOf(quantityText) }
    var optionAdd by remember { mutableStateOf(false) }
    var optionCancell by remember { mutableStateOf(false) }
    var haveError by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        enabled= true,
        singleLine=true,
        value = quantity+"",
        onValueChange = { quantity = it },
        label = { Text(text = "Cantidad a ubicar") },
        placeholder = { Text(text = "Ingresar la cantidad a ubicar") },
        trailingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = AzulVistony202) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,imeAction = ImeAction.Next ),
        keyboardActions = KeyboardActions(
            onNext = {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
        )
    )

    Text("")

    if(optionAdd){
        Text(text = "¿Está seguro de realizar esta operación? ", color = Color.Red)
        Text(text = " ")
    }

    if(optionCancell){
        Text(text = "¿Está seguro de cancelar esta operación? ", color = Color.Red)
        Text(text = " ")
    }

    if(haveError.isNotEmpty()){
        Text(text = haveError, color = Color.Red)
        Text(text = " ")
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ){
        Button(
            colors= ButtonDefaults.buttonColors(backgroundColor = Color.Gray),
            onClick = {
                if(optionAdd){
                    try{
                        if(quantity.toDouble()!=0.0){
                            focusManager.clearFocus()
                            onPress(
                                StockTransferPayloadVal(
                                    quantity = quantity.toDouble()
                                )
                            )
                        }else{
                            haveError="La cantidad debe ser mayor o igual a 1.00 "
                            quantity="1.0"
                        }

                    }catch(e:Exception){
                        haveError="La cantidad ingresada no es valida"
                        quantity="1.0"
                    }

                    optionAdd=false
                    optionCancell=false
                }else{
                    optionCancell=false
                    optionAdd=true
                    haveError=""
                }
            }){
            Text(
                text = "Agregar",
                color=Color.White
            )
        }
        Button( onClick = {
            if(optionCancell){
                optionAdd=false
                optionCancell=false
                focusManager.clearFocus()

                onClosePressed()
            }else{
                optionAdd=false
                optionCancell=true
                haveError=""
            }
        }) {
            Text(
                text = "Cancelar"
            )
        }
    }
}

@Composable
fun SelectTypescanModal(
    idHeader:String,
    context:Context,
    value:zebraPayload=zebraPayload(),
    type:TypeReadSKU,
    whsOrigin:String="",
    objType:Int=0,
    selected: (List<StockTransferBodyPayload>) -> Unit,
    onCloseBottomSheet :()->Unit){

    val itemsViewModel: ItemsViewModel = viewModel(
        factory = ItemsViewModel.ArticleViewModelFactory("scan")
    )

    val warehouseViewModel: WarehouseViewModel = viewModel(
        factory = WarehouseViewModel.WarehouseViewModelFactory("",whsOrigin,objType)
    )

    val stockTransferBodyViewModel: StockTransferBodyViewModel = viewModel(
        factory = StockTransferBodyViewModel.StockTransferBodyViewModelModelFactory(idHeader)
    )

    val qualityViewModel: QualityViewModel = viewModel(
        factory = QualityViewModel.QualityViewModelFactory()
    )


    if(value.Payload.split("-").size==4 && value.Type!="LABEL-TYPE-QRCODE"){
        warehouseViewModel.getLocations(value.Payload,whsOrigin)
    }else if(value.Payload.isNotEmpty()){
        Log.e("Jepicame idJehdaer","=>SE EJEUCTA DESDE 1158")
        itemsViewModel.getArticle(
            value=value.Payload,
            idHeader=idHeader,
            typeInventario=""
        )
    }

    qualityViewModel.getQuality(objType)

    ASJDHASJKD(
        wareHouseOrigin=whsOrigin,
        idHeader=idHeader,
        context=context,
        qualityViewModel=qualityViewModel,
        itemsViewModel = itemsViewModel,
        warehouseViewModel=warehouseViewModel,
        documentBodyVm=stockTransferBodyViewModel,
        type=type,
        objType=objType,
        onPress = {
            selected(it)
        }, onClosePressed = {
            onCloseBottomSheet()
        }
    )
}

@Composable
private fun ASJDHASJKD(
    wareHouseOrigin:String,
    idHeader:String,
    context: Context,
    itemsViewModel: ItemsViewModel,
    warehouseViewModel:WarehouseViewModel,
    documentBodyVm:StockTransferBodyViewModel,
   qualityViewModel: QualityViewModel,
   type:TypeReadSKU=TypeReadSKU.HANDHELD,
   objType:Int,
   onPress:(List<StockTransferBodyPayload>)->Unit,
   onClosePressed: () -> Unit
){

    val articleValue = itemsViewModel.article.collectAsState()
    val warehouseValue = warehouseViewModel.location.collectAsState()
    val qualityValue = qualityViewModel.quality.collectAsState()
    val documentBodyValue = documentBodyVm.documentBody.collectAsState()

    if(articleValue.value.items.isNotEmpty() && articleValue.value.items[0].defaultBinLocation.isNotEmpty()){
        warehouseViewModel.getLocations(articleValue.value.items[0].defaultBinLocation,wareHouseOrigin)
    }

    var binLocation by remember { mutableStateOf(LocationResponse()) }
    var quality by remember { mutableStateOf(QualityControlResponse()) }
    var itemResponse:ItemsResponse by remember { mutableStateOf(ItemsResponse()) }


    when(qualityValue.value.status){
        ""->{}
        "cargando"->{
            //CustomProgressDialog("Buscando articulo...")
        }
        "ok"->{
            quality=qualityValue.value
            qualityViewModel.resetQualityStatus()
        }
        "vacio"->{
            // Toast.makeText(context, "El código escaneado no se encuentra en el maestro de articulos", Toast.LENGTH_LONG).show()
            //itemsViewModel.resetArticleStatus()
        }
        else->{
            Toast.makeText(context, "Ocurrio un error:\n ${articleValue.value.status}", Toast.LENGTH_LONG).show()
            //itemsViewModel.resetArticleStatus()
        }
    }

    //22 ES codigo de recepcion de mercaderia de las orden de compra
    if(objType==22){
        when(documentBodyValue.value.status){
            ""->{}
            "cargando"->{
                CustomProgressDialog("Buscando articulo...")
            }
            "ok"->{

                itemResponse=ItemsResponse(
                    items= listOf(
                        ItemResponse(
                            item=Items(ItemName = documentBodyValue.value.body.ItemName, ItemCode = documentBodyValue.value.body.ItemCode)
                        )
                    )
                )
                documentBodyVm.resetDocumentBody()
            }
            "vacio"->{
                Toast.makeText(context, "El código escaneado no se encuentra en el maestro de articulos", Toast.LENGTH_LONG).show()
                documentBodyVm.resetDocumentBody()
            }
            else->{
                Toast.makeText(context, "Ocurrio un error:\n ${articleValue.value.status}", Toast.LENGTH_LONG).show()
                documentBodyVm.resetDocumentBody()
            }
        }
    }
    else{
        when(articleValue.value.status){
            ""->{}
            "cargando"->{
                if(articleValue.value.type==TypeCode.QR){
                    CustomProgressDialog("Buscando articulo...")
                }else{
                    CustomProgressDialog("Buscando SSCC...")
                }
            }
            "ok"->{
                if(articleValue.value.type==TypeCode.SSCC){
                    if(wareHouseOrigin==articleValue.value.warehouse){
                        itemResponse=articleValue.value
                    }else{
                        Toast.makeText(context, "No puedes generar esta operación por que el almacén origen es $wareHouseOrigin y el palet escaneado actualmente se encuentra en el almacén ${articleValue.value.warehouse}", Toast.LENGTH_LONG).show()
                    }
                }else{
                    itemResponse=articleValue.value
                }

                itemsViewModel.resetArticleStatus()
            }
            "vacio"->{
                Toast.makeText(context, "El código escaneado no se encuentra en el maestro de articulos", Toast.LENGTH_LONG).show()
                itemsViewModel.resetArticleStatus()
            }
            else->{
                Toast.makeText(context, articleValue.value.status, Toast.LENGTH_LONG).show()
                itemsViewModel.resetArticleStatus()
            }
        }
    }

    when(warehouseValue.value.status){
        ""->{}
        "cargando"->{
            CustomProgressDialog("Buscando ubicación...")
        }
        "ok"->{
            binLocation=warehouseValue.value
            //warehouseViewModel.resetWarehouseStatus()
            warehouseViewModel.resetLocationStatus()
        }
        "vacio"->{
            Toast.makeText(context, "El código escaneado no representa una ubicación del almacén origen.", Toast.LENGTH_LONG).show()
            warehouseViewModel.resetLocationStatus()
        }
        else->{
            Toast.makeText(context, "Ocurrio un error:\n ${warehouseValue.value.status}", Toast.LENGTH_LONG).show()
            warehouseViewModel.resetLocationStatus()
        }
    }

    if(itemResponse.items.isNotEmpty()){
        Column(modifier=Modifier.padding(top=20.dp)){

            if(itemResponse.type==TypeCode.SSCC){
                Text(
                    //+" x"+itemResponse.items.size
                    text = if(itemResponse.nameSscc.isNullOrEmpty()){"Codigo del SSCC [XXXX]"}else{"SSCC "+itemResponse.nameSscc},
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 20.dp, bottom = 10.dp,end=50.dp)
                )
            }else{
                Text(
                    text = if(itemResponse.items.isNullOrEmpty() && itemResponse.items[0].item.ItemName.isNullOrEmpty()){"Registrar artículo"}else{itemResponse.items[0].item.ItemName},
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 20.dp, bottom = 10.dp,end=50.dp)
                )
            }

            Divider(modifier = Modifier.fillMaxWidth(0.8f).padding(start = 20.dp, bottom = 10.dp))

            Column(modifier= Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())){
                when(type){
                    TypeReadSKU.CAMERA->{
                        Text("Opción no habilitada.")
                        /*CameraForm(
                            calledFor= CallFor.Article,
                            context = context,
                            itemsViewModel= itemsViewModel,
                            cameraProviderFuture=cameraProviderFuture,
                            cameraProvider=cameraProvider
                        )*/
                    }
                    TypeReadSKU.HANDHELD,
                    TypeReadSKU.KEYBOARD->{
                        // cameraProvider.unbindAll()

                        if(quality.status=="ok"||quality.status=="" ){
                            formHandheld(
                                quality=quality,
                                objType=objType,
                                itemsResponse=itemResponse,
                                TopresponseLocationAndItem=ResponseLocationAndItem(locationResponse = binLocation,itemResponse=itemResponse.items[0]),
                                onPress={

                                    /*if(objType==22){
                                        if(articleValue.value.documentBody.TotalQuantity+it.Quantity>articleValue.value.documentBody.Quantity){
                                            Toast.makeText(context, "La cantidad ingresada no puede ser mayor a la cantidad asignada en el documento.", Toast.LENGTH_LONG).show()
                                        }else{
                                            onPress(it)
                                        }
                                    }else{*/

                                    onPress(it)

                                    itemResponse=ItemsResponse()
                                    // }

                                },onClosePressed={
                                    /*if(objType==22){
                                        documentBodyVm.resetDocumentBody()
                                    }*/

                                    onClosePressed()
                                },
                                onSearch = {
                                    if(objType==22){
                                        documentBodyVm.getArticleFromBody(it)
                                    }else{
                                        Log.e("Jepicame idJehdaer","=>SE EJEUCTA DESDE 1387")
                                        itemsViewModel.getArticle(
                                            value=it,
                                            idHeader=idHeader,
                                            typeInventario=""
                                        )
                                    }
                                }
                            )

                        }

                    }
                    else->{}
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun formHandheld(
    itemsResponse:ItemsResponse=ItemsResponse(),
    quality:QualityControlResponse,
    objType:Int,
    TopresponseLocationAndItem:ResponseLocationAndItem=ResponseLocationAndItem(),
    onSearch:(String)->Unit,
    onPress:(List<StockTransferBodyPayload>)->Unit,
    onClosePressed: () -> Unit){

    var responseLocationAndItem by  remember {mutableStateOf(TopresponseLocationAndItem)}

    responseLocationAndItem=TopresponseLocationAndItem

    var idArticle by    remember {mutableStateOf(responseLocationAndItem.itemResponse.item.ItemCode)}
    var textLote by   remember { mutableStateOf( responseLocationAndItem.itemResponse.lote)}

    var idLocation by    remember {mutableStateOf(responseLocationAndItem.locationResponse.location.AbsEntry )}
    var textLocation by   remember {mutableStateOf( responseLocationAndItem.locationResponse.location.BinCode )}
    var HassLocation by    remember {mutableStateOf( responseLocationAndItem.locationResponse.EnableBinLocations )}

    var quantity by  remember {mutableStateOf(""+responseLocationAndItem.itemResponse.quantity)}
    var quanlityVal by    remember {mutableStateOf(QualityControl_Collection())}

    var optionAdd by  remember {mutableStateOf(false)}
    var optionCancell by remember {mutableStateOf(false)}
    var haveError by remember  {mutableStateOf("")}

    val keyboardController = LocalSoftwareKeyboardController.current

    idLocation = responseLocationAndItem.locationResponse.location.AbsEntry
    textLocation = responseLocationAndItem.locationResponse.location.BinCode

    if(!responseLocationAndItem.itemResponse.item.ItemCode.isNullOrEmpty()){
        idArticle =responseLocationAndItem.itemResponse.item.ItemCode
    }

    if(!responseLocationAndItem.itemResponse.lote.isNullOrEmpty()){
        textLote =responseLocationAndItem.itemResponse.lote
    }

    HassLocation=responseLocationAndItem.locationResponse.EnableBinLocations

    if(responseLocationAndItem.itemResponse.quantity!=1.0){
        keyboardController?.hide()
        quantity=""+responseLocationAndItem.itemResponse.quantity
    }

    if(itemsResponse.type== TypeCode.QR){
        OutlinedTextField(
            enabled= !responseLocationAndItem.itemResponse.item.ItemCode.isNullOrEmpty(),
            singleLine=true,
            value = if(responseLocationAndItem.itemResponse.item.ItemCode.isNullOrEmpty()){idArticle}else{responseLocationAndItem.itemResponse.item.ItemCode},
            onValueChange = {
                idArticle = it
            },
            label = { Text(text = "Código del artículo") },
            placeholder = { Text(text = "Ingresar código del artículo") },
            trailingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = AzulVistony202) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text,imeAction = ImeAction.Search ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    onSearch(idArticle)
                }
            )
        )

        Text(text = " ")

        OutlinedTextField(
            enabled= responseLocationAndItem.itemResponse.lote.isNullOrEmpty(),
            singleLine=true,
            value = if(responseLocationAndItem.itemResponse.lote.isNullOrEmpty()){textLote}else{responseLocationAndItem.itemResponse.lote},
            onValueChange = { textLote = it },
            label = { Text(text = "Lote") },
            placeholder = { Text(text = "Ingresar el lote") },
            trailingIcon = { Icon(painter = painterResource(id = R.drawable.ic_baseline_box_24), contentDescription = null, tint = AzulVistony202) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text,imeAction = ImeAction.Go ),
            keyboardActions = KeyboardActions(
                onGo = {keyboardController?.hide()}
            )
        )

        Text(text = " ")

        OutlinedTextField(
            enabled= responseLocationAndItem.itemResponse.quantity==1.0,
            singleLine=true,
            value = quantity,
            onValueChange = {
                if (it.isEmpty() || it.matches("[0-9]{1,13}(\\.[0-9]*)?".toRegex())) quantity = it
            },
            label = { Text(text = "Cantidad") },
            placeholder = { Text(text = "Ingresar la cantidad") },
            trailingIcon = { Icon(painter = painterResource(id = R.drawable.ic_baseline_numbers_24), contentDescription = null, tint = AzulVistony202) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,imeAction = ImeAction.Go ),
            keyboardActions = KeyboardActions(
                onGo = {keyboardController?.hide()}
            )
        )
    }
    else{
        Text("El palet contiene ${itemsResponse.items.size} producto(s)")

        LazyRow(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f)) {
            items(items = itemsResponse.items, itemContent = { item ->

                Card(
                    backgroundColor = Color.White,
                    elevation = 10.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row{
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_palet_on_24),
                                contentDescription = "Palet",
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = " Código "+item.item.ItemCode,
                                style = MaterialTheme.typography.subtitle1,
                                color = MaterialTheme.colors.primary
                            )

                        }

                        Text(
                            maxLines=2,
                            overflow=TextOverflow.Ellipsis,
                            modifier=Modifier.widthIn(max=200.dp),
                            text = item.item.ItemName,
                            style = MaterialTheme.typography.subtitle2,
                            color = MaterialTheme.colors.onBackground
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Row {
                            Text(
                                text = "Cantidad: ",
                                style = MaterialTheme.typography.subtitle1,
                                color = MaterialTheme.colors.onBackground
                            )
                            Text(
                                text = ""+item.quantity,
                                style = MaterialTheme.typography.subtitle1,
                                color = MaterialTheme.colors.primary
                            )
                        }
                        Row {
                            Text(
                                text = "Lote: ",
                                style = MaterialTheme.typography.subtitle1,
                                color = MaterialTheme.colors.onBackground
                            )
                            Text(
                                text = item.lote,
                                style = MaterialTheme.typography.subtitle1,
                                color = MaterialTheme.colors.primary
                            )
                        }
                        Row {
                            Text(
                                text = "Fecha: ",
                                style = MaterialTheme.typography.subtitle1,
                                color = MaterialTheme.colors.onBackground
                            )
                            Text(
                                text = item.expireDate,
                                style = MaterialTheme.typography.subtitle1,
                                color = MaterialTheme.colors.primary
                            )
                        }
                    }
                }

                /*Card(
                    backgroundColor = Color.White,
                    elevation = 10.dp,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxWidth()
                        .padding(5.dp)
                ){
                    Column(modifier= Modifier.padding(10.dp)){
                        Text(
                            item.item.ItemCode +" "+item.item.ItemName
                        )
                        Text(
                            "Cantidad: " +item.quantity
                        )
                        Text(
                            "Lote: " +item.lote
                        )
                        Text(
                            "Fecha: "+item.expireDate
                        )
                    }
                }*/
            })
        }
    }

    if(quality.data.Collection.size>0){
        ListBox(
            quality=quality.data,
            Onselected = {
                quanlityVal=it
            }
        )
    }

    if(objType!=22){
        OutlinedTextField(
            enabled=(HassLocation=="tYES" && idLocation==0),
            singleLine=true,
            value = responseLocationAndItem.locationResponse.location.BinCode,
            onValueChange = { /*textLocation = it*/ },
            label = { Text(text = "Ubicación") },
            placeholder = { Text(text = "Ingresar la ubicación") },
            trailingIcon = { Icon(painter = painterResource(id = R.drawable.ic_baseline_rack_24), contentDescription = null, tint = AzulVistony202) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text,imeAction = ImeAction.Search ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                }
            )
        )
    }

    Text(text = " ")

    if(optionAdd){
        Text(text = "¿Está seguro de realizar esta operación? ", color = Color.Red)
        Text(text = " ")
    }

    if(optionCancell){
        Text(text = "¿Está seguro de cancelar esta operación? ", color = Color.Red)
        Text(text = " ")
    }

    if(haveError.isNotEmpty()){
        Text(text = haveError, color = Color.Red)
        Text(text = " ")
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ){
        Button(
            colors= ButtonDefaults.buttonColors(backgroundColor = Color.Gray),
            onClick = {
                if(optionAdd){
                    try{
                        if(quantity.toDouble()!=0.0){
                            if(responseLocationAndItem.itemResponse.item.ItemCode.isNullOrEmpty()){
                                haveError="Es necesario buscar en el documento el artículo a ingresar"
                            }else{
                                if(responseLocationAndItem.locationResponse.location.BinCode.isNullOrEmpty()){
                                    haveError="Es necesario ingresar una ubicación"
                                }else{

                                    val listPayloadBody:List<StockTransferBodyPayload> = itemsResponse.items.map {

                                        if(itemsResponse.type==TypeCode.SSCC){
                                            StockTransferBodyPayload(
                                                ItemCode=  it.item.ItemCode,
                                                ItemName = it.item.ItemName,
                                                Batch = it.lote,
                                                Sscc=itemsResponse.nameSscc,
                                                LocationCode = ""+responseLocationAndItem.locationResponse.location.AbsEntry,
                                                LocationName = responseLocationAndItem.locationResponse.location.BinCode,
                                                Quantity = it.quantity,
                                                Quality =quanlityVal
                                            )
                                        }else{
                                            StockTransferBodyPayload(
                                                ItemCode=  responseLocationAndItem.itemResponse.item.ItemCode,
                                                ItemName = responseLocationAndItem.itemResponse.item.ItemName,
                                                Batch = if(responseLocationAndItem.itemResponse.lote.isNullOrEmpty()){textLote}else{responseLocationAndItem.itemResponse.lote},
                                                LocationCode = ""+responseLocationAndItem.locationResponse.location.AbsEntry,
                                                LocationName = responseLocationAndItem.locationResponse.location.BinCode,
                                                Quantity = quantity.toDouble(),
                                                Quality =quanlityVal
                                            )
                                        }
                                    }

                                    onPress(listPayloadBody)

                                    Log.e("JEPICAME","=>se limpiara agregar antes "+responseLocationAndItem.itemResponse.item.ItemCode)
                                    responseLocationAndItem=ResponseLocationAndItem(
                                        locationResponse = LocationResponse(),
                                        itemResponse = ItemResponse(
                                            item = Items(ItemCode="")
                                        )
                                    )

                                    Log.e("JEPICAME","=>se limpiara agregar dsp "+responseLocationAndItem.itemResponse.item.ItemCode)
                                }
                            }
                        }else{
                            haveError="La cantidad debe ser mayor o igual a 1.00 "
                            quantity="1.0"
                        }

                    }catch(e:Exception){
                        haveError="La cantidad ingresada no es valida"
                        quantity="1.0"
                    }

                    optionAdd=false
                    optionCancell=false
                }else{
                    optionCancell=false
                    optionAdd=true
                    haveError=""
                }
            }){
            Text(
                text = "Agregar",
                color=Color.White
            )
        }
        Button( onClick = {
            if(optionCancell){
                optionAdd=false
                optionCancell=false

                onClosePressed()

                responseLocationAndItem=ResponseLocationAndItem()
            }else{
                optionAdd=false
                optionCancell=true
                haveError=""
            }
        }) {
            Text(
                text = "Cancelar"
            )
        }
    }

    Text(text = " ")
}

@Composable
private fun ListBox(quality:QualityControl,Onselected: (QualityControl_Collection) -> Unit){

    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("") }

    var textfieldSize by remember { mutableStateOf(Size.Zero)}

    val icon = if (expanded)
        Icons.Filled.ArrowBack
    else
        Icons.Filled.ArrowDropDown

    Log.e("JEPICAME","==>LISTADO PRINT")

    Column() {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {
                //selectedText = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textfieldSize = coordinates.size.toSize()
                },
            label = {Text("Control de calidad")},
            trailingIcon = {
                Icon(icon,"contentDescription",
                    Modifier.clickable { expanded = !expanded })
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ){
            quality.Collection.forEach { label ->
                DropdownMenuItem(onClick = {
                    expanded=!expanded
                    Onselected(label)
                    selectedText = label.Dscription.toString()
                }) {
                    Text(text = label.Dscription.toString())
                }
            }
        }


    }

    Text(text = " ")
}

@Composable
fun SelectCountryModal(selected: (CountryLocation) -> Unit){
    val listLocation = listOf(
        CountryLocation("PE","PERÚ"),
        CountryLocation("PY","PARAGUAY"),
        CountryLocation("CL","CHILE"),
        CountryLocation("EC","ECUADOR")
    )

    Column(modifier=Modifier.padding(top=20.dp, bottom = 10.dp)){
        Text(text="Selecciona tu locación",color=Color.Gray,modifier=Modifier.padding(start=20.dp,bottom=10.dp))
        Divider(modifier= Modifier
            .fillMaxWidth(0.8f)
            .padding(start = 20.dp, bottom = 10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            itemsIndexed(listLocation) { _, line ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = {
                            selected(line)
                        })
                        .height(55.dp)
                        .padding(start = 25.dp), verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(painter = painterResource(id = R.drawable.ic_baseline_domain_24), contentDescription = null, tint = AzulVistony202)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column{
                        Text(text = line.text, color = Color.DarkGray)
                    }

                }
            }
        }
    }
}

@Composable
fun SelectWarehouseModal(context: Context, selected: (WarehouseBinLocation) -> Unit){

    val werehouseViewModel: WarehouseViewModel = viewModel(
        factory = WarehouseViewModel.WarehouseViewModelFactory ("init")
    )

    val warehouseValue = werehouseViewModel.almacenes.collectAsState()

    when(warehouseValue.value.status){
        ""->{}
        "cargando"->{
            CustomProgressDialog("listando almacenes...")
        }
        "ok"->{
            Column(modifier=Modifier.padding(top=20.dp, bottom = 10.dp)) {
                Text(
                    text = "Selecciona tu almacén",
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 20.dp, bottom = 10.dp)
                )
                Divider(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(start = 20.dp, bottom = 10.dp)
                )

                LazyColumn(
                    contentPadding = PaddingValues(16.dp)
                ){
                    itemsIndexed(warehouseValue.value.warehouse) { index, line ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = {
                                    selected(
                                        WarehouseBinLocation(
                                            warehouse = line,
                                            defaultLocation = warehouseValue.value.defaultLocation[index]
                                        )
                                    )
                                })
                                //.height(55.dp)
                                .padding(start = 25.dp), verticalAlignment = Alignment.CenterVertically
                        ){
                            Icon(painter = painterResource(id = R.drawable.ic_baseline_domain_24), contentDescription = null, tint = AzulVistony202)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column{
                                Text(text = line.WarehouseCode, color = Color.DarkGray)
                                Text(text = line.WarehouseName, color = Color.DarkGray)
                                if(warehouseValue.value.defaultLocation[index]!="-" && warehouseValue.value.defaultLocation[index]!="+"){
                                    Text(text =warehouseValue.value.defaultLocation[index], color = Color.Gray)
                                }else{
                                    Text(text= if(line.WmsLocation!="Y"){"No controla ubicaciones"}else{"Controla Ubicaciones"}, color = Color.Gray)
                                }
                                Text("")
                            }
                        }
                    }
                }

            }


            //werehouseViewModel.resetWarehouseStatus()
        }
        "vacio"->{
            Toast.makeText(context, "El maestro de almacenes esta vacío", Toast.LENGTH_LONG).show()
            //werehouseViewModel.resetWarehouseStatus()
        }
        else->{
            Toast.makeText(context, "Ocurrio un error:\n ${warehouseValue.value.status}", Toast.LENGTH_LONG).show()
            //werehouseViewModel.resetWarehouseStatus()
        }
    }
}

@Composable
fun SelectTypeModal(selected: (TypeInventario) -> Unit){

    val listTypeInventory = listOf(
        TypeInventario("CP","Conteo de Producción"),
        TypeInventario("RI","Recepción Importación"),
        TypeInventario("RP","Recepción Producción"),
        TypeInventario("RS","Recepción Sucursales"),
        TypeInventario("IG","Inventario General"),
        TypeInventario("IC","Inventario Cíclico"),
        TypeInventario("PC","Picking Clientes"),
        TypeInventario("PS","Picking Sucursales"),
        TypeInventario("PI","Picking Induvis"),
        TypeInventario("IL","Ingreso Layout"),
        TypeInventario("SL","Salida Layout"),
        TypeInventario("EMI","Entrada Mercancia Interna"),
        TypeInventario("EME","Entrada Mercancia Externa"),
        TypeInventario("ED","Entrada Por Devolución"),

        TypeInventario("LI","Logística Inversa"),
        TypeInventario("DS","Despacho Sucursal"),
        TypeInventario("DC","Despacho Cliente"),
        TypeInventario("OT","Otros")
    )

    Column(modifier=Modifier.padding(top=20.dp, bottom = 10.dp)) {
        Text(
            text = "Selecciona el tipo de conteo",
            color = Color.Gray,
            modifier = Modifier.padding(start = 20.dp, bottom = 10.dp)
        )
        Divider(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(start = 20.dp, bottom = 10.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp)
        ) {
            itemsIndexed(listTypeInventory) { _, line ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = {
                            selected(line)
                        })
                        .height(55.dp)
                        .padding(start = 25.dp), verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(painter = painterResource(id = R.drawable.ic_baseline_domain_24), contentDescription = null, tint = AzulVistony202)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column{
                        Text(text = line.text, color = Color.DarkGray)
                    }

                }
            }
        }

    }
}

@Composable
fun ShowMessageModal(message:String){
    Column(modifier=Modifier.padding(top=20.dp, bottom = 10.dp)) {
        Text(
            text = " Ocurrio un error",
            color = Color.Gray,
            modifier = Modifier.padding(start = 20.dp, bottom = 10.dp, end = 50.dp)
        )

        Divider()

        Column(modifier= Modifier
            .padding(20.dp)) {
            Text(message)
        }
    }
}


@Composable
fun BottomSheetWithCloseDialog(onClosePressed: () -> Unit,closeButtonColor: Color = Color.White,showIconClose:Boolean,content: @Composable() () -> Unit){
    Box{

        if(showIconClose){
            IconButton(
                onClick = onClosePressed,
                modifier = Modifier
                    .background(Color.Gray)
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(30.dp)
            ) {
                Icon(Icons.Filled.Close, tint = closeButtonColor, contentDescription = null)
            }
        }

        content()

    }
}



