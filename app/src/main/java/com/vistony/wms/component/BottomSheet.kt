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
import androidx.compose.ui.platform.LocalContext
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
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vistony.wms.R
import com.vistony.wms.num.TypeReadSKU
import com.vistony.wms.model.*
import com.vistony.wms.num.TypeCode
import com.vistony.wms.ui.theme.AzulVistony201
import com.vistony.wms.ui.theme.AzulVistony202
import com.vistony.wms.viewmodel.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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
                    onCloseBottomSheet={
                        onCloseBottomSheet()
                    },
                    StatusScan=currentScreen.StatusScan,
                    merchandiseBody=currentScreen.merchandiseBody,
                    commentReception=currentScreen.commentReception,
                    locationReception=currentScreen.locationReception
                )
            is BottomSheetScreen.SelectDestineModal ->
                SelectDestineModal(
                    objType=currentScreen.objType,
                    value = currentScreen.value,context=currentScreen.context,
                    stockTransferBodyViewModel = currentScreen.stockTransferBodyViewModel,
                    selected = currentScreen.selected,
                    wareHouseOrigin=currentScreen.wareHouseOrigin,
                    wareHouseDestine=currentScreen.wareHouseDestine,
                    onCloseBottomSheet=onCloseBottomSheet,
                    StatusOpenFormAddDestiny=currentScreen.StatusOpenFormAddDestiny,
                    payLoadMutable = currentScreen.payLoadMutable,
                    commentReception = currentScreen.commentReception,
                    locationReception = currentScreen.locationReception
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
                            val selected: (List<StockTransferBodyPayload>) -> Unit,val whsOrigin:String,val objType:Int,var StatusScan:MutableState<String>,
                            var merchandiseBody:StockTransferBodyResponse,var commentReception:String,var locationReception:String):BottomSheetScreen()
    class SelectDestineModal(val objType:Int=0,val value:zebraPayload = zebraPayload(),val context: Context,val wareHouseOrigin: String,
                             val wareHouseDestine:String,
                             val stockTransferBodyViewModel:StockTransferBodyViewModel,
                             val selected: (List<StockTransferPayloadVal>) -> Unit,
                             var StatusOpenFormAddDestiny: (MutableState<String>),
                             var payLoadMutable: (MutableState<String>),
                             var commentReception:String,var locationReception:String
    ): BottomSheetScreen()
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
fun SelectWitOptionsModal(title:String, listOptions: List<Options>, selected:(Options)->Unit){

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

                Log.e("JEPICAMEE","==>"+line.text)
                Log.e("JEPICAMEE","==>"+line.subMenuVisible)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = {
                            if(line.enabled){
                                if(line.subMenu){
                                    Log.e("JEPICAMEE","==>"+line.subMenuVisible)
                                    line.subMenuVisible=true
                                    Log.e("JEPICAMEE","==>"+line.subMenuVisible)
                                }else{
                                    line.subMenuVisible=false
                                    selected(line)
                                }
                            }
                        }).padding(vertical = 5.dp),
                    horizontalAlignment = Alignment.Start
                ){

                    Row {
                        Icon(painter = painterResource(id = line.icono), contentDescription = null, tint = if(line.enabled){AzulVistony202}else{Color.Gray})
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = line.text , color = if(line.enabled){Color.DarkGray}else{Color.Gray})
                    }

                    Log.e("JEPICAMEE","==>D"+line.subMenuVisible)
                    Log.e("JEPICAMEE","==>D"+line.subMenu)

                    if(line.subMenu && line.subMenuVisible){
                        Divider()
                        Text("")
                        Row(modifier=Modifier.padding(start=20.dp)){
                            Icon(painter = painterResource(id = line.icono), contentDescription = null, tint = if(line.enabled){AzulVistony202}else{Color.Gray})
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Por producción" , color = if(line.enabled){Color.DarkGray}else{Color.Gray})
                        }
                        Text("")
                        Row(modifier=Modifier.padding(start=20.dp)){
                            Icon(painter = painterResource(id = line.icono), contentDescription = null, tint = if(line.enabled){AzulVistony202}else{Color.Gray})
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Por compra" , color = if(line.enabled){Color.DarkGray}else{Color.Gray})
                        }
                    }
                }
                Text("")
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
    onCloseBottomSheet :()->Unit,
    StatusOpenFormAddDestiny: (MutableState<String>),
    payLoadMutable: (MutableState<String>),
    commentReception:String,
    locationReception:String,
){

    //Log.e("REOS","BottomSheet-SelectDestineModal-value2.Type: "+value2.Type)
    Log.e("REOS","BottomSheet-SelectDestineModal-payLoadMutable: "+payLoadMutable)
    Log.e("REOS","BottomSheet-SelectDestineModal-wareHouseDestine: "+wareHouseDestine)
    val suggestionViewModel: SuggestionViewModel = viewModel(
        factory = SuggestionViewModel.SuggestionViewModelFactory()
    )

    Log.e("REOS","BottomSheet-SelectDestineModal-value.Type: "+value.Type)
    Log.e("REOS","BottomSheet-SelectDestineModal-value.Payload: "+value.Payload)
    //Log.e("REOS","==> tipo de encodeado es "+value.Payload)

    if(value.Payload.isNotEmpty() && value.Type in listOf("LABEL-TYPE-QRCODE","LABEL-TYPE-EAN128","LABEL-TYPE-EAN13"
            //Agregado 12/08/2023
            ,"LABEL-TYPE-CODE39"
        )){
        Log.e("REOS","BottomSheet-SelectDestineModal-value: "+value.toString())
        var itemCodeHandheld: String = ""
        var loteHandheld: String = ""
        if(!value.Payload.isNullOrEmpty())
        {
            val elements = value.Payload.split("|", limit = 3)
            if(elements.size>1)
            {
                if(value.Payload.length!=20)
                {
                    stockTransferBodyViewModel.getBodyAndSubBody(value,objType)
                }
            }
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
        },
        onTryAgainSuggestion={
            Toast.makeText(context, "Es necesario volver a escanear el articulo para mostrar las ubicaciones sugeridas", Toast.LENGTH_SHORT).show()
        }
        ,StatusOpenFormAddDestiny=StatusOpenFormAddDestiny
        ,payLoadMutable=payLoadMutable
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
    Onclose :()->Unit,
    onTryAgainSuggestion:()->Unit,
    StatusOpenFormAddDestiny: (MutableState<String>),
    payLoadMutable: (MutableState<String>),
){
    val stockTrnsfBySbRspnsValue = stockTransferBodyViewModel.stockTransferBodyAndSubBodyResponse.collectAsState()
    Log.e("REOS","BottomSheet-SelectDestineModal-wareHouseDestine: "+wareHouseDestine)
    Log.e("REOS","BottomSheet-Rowasd-stockTrnsfBySbRspnsValue: "+stockTrnsfBySbRspnsValue.value.toString())
    Log.e("REOS","BottomSheet-Rowasd-stockTrnsfBySbRspnsValue.value.status: "+stockTrnsfBySbRspnsValue.value.status)
    Log.e("REOS","BottomSheet-Rowasd-payLoadMutable.value: "+payLoadMutable.value)
    Log.e("REOS","BottomSheet-Rowasd-StatusOpenFormAddDestiny.value: "+StatusOpenFormAddDestiny.value)
    Log.e("REOS","BottomSheet-Rowasd-StatusOpenFormAddDestiny.value.Payload: "+value.Payload)
    val warehouseViewModel: WarehouseViewModel = viewModel(
        factory = WarehouseViewModel.WarehouseViewModelFactory(
            "1", "",objType
        )
    )
    val suggestionValue = suggestionViewModel.suggtn.collectAsState()
    val warehouseValue= warehouseViewModel.location.collectAsState()
    var destineLocation by remember { mutableStateOf(BinLocation())}
    val mergedList = stockTrnsfBySbRspnsValue.value.response.merge()
    var LocationName by remember { mutableStateOf("")}
    Log.e("REOS","BottomSheet-Rowasd-warehouseValue.value.location.BinCode: "+warehouseValue.value.location.BinCode)
    Log.e("REOS","BottomSheet-Rowasd-warehouseValue.value.location.AbsEntry: "+warehouseValue.value.location.AbsEntry)
    Log.e("REOS","BottomSheet-Rowasd-objType: "+67)
    Log.e("REOS","BottomSheet-Rowasd-value.Payload: "+value.Payload)
    //Charge Destination 1701 - Picking List
    /*if(objType==1701)
    {
        //if(destineLocation.text.isNullOrEmpty()) {
            for (i in 0 until mergedList.size) {
                if(!destineLocation.text.equals(mergedList.get(i).LocationName))
                {
                    destineLocation = BinLocation(
                        id = mergedList.get(i).LocationCode,
                        text = mergedList.get(i).LocationName
                    )
                }
            }
       // }
    }*/
    //Charge warehose 67 - StockTransfer

    if(objType in setOf(67,22,1701)&&!value.Payload.isNullOrEmpty())
    {
        Log.e("REOS","BottomSheet-Rowasd-objType==67&&!value.Payload.isNullOrEmpty()")
        val elements = value.Payload.split("|", limit = 3)
        if(elements.size==1)
        {
            if (warehouseValue.value.location.BinCode.isNullOrEmpty()) {
                warehouseViewModel.verificationLocation(value.Payload, "")
            }
        }
        else {
            /*if(objType==1701) {
                if (!destineLocation.text.equals(value.Payload) && !warehouseValue.value.location.BinCode.isNullOrEmpty()) {
                    warehouseViewModel.verificationLocation(value.Payload, "")
                }
            }*/
        }
    }

    //CHARGE DESTINATION
    //67 - StockTransfer
    if(objType in setOf(67,22,1701)&&!value.Payload.isNullOrEmpty()&&!warehouseValue.value.location.BinCode.isNullOrEmpty())
    {
        if(destineLocation.text.isNullOrEmpty())
        {
            destineLocation = BinLocation(id = warehouseValue.value.location.AbsEntry.toString(), text =warehouseValue.value.location.BinCode)
        }
        else
        {

            if(objType==1701)
            {
                if (!destineLocation.text.equals(value.Payload)) {
                    warehouseViewModel.verificationLocation(value.Payload, "")
                    destineLocation = BinLocation(
                        id = warehouseValue.value.location.AbsEntry.toString(),
                        text = warehouseValue.value.location.BinCode
                    )
                }
            }
        }
    }


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
            Log.e("REOS","BottomSheet-Rowasd-stockTrnsfBySbRspnsValue.value.status-EntroIF-OK: ")
            StatusOpenFormAddDestiny.value="Y"
            //Charge WareHouse
            //Stock Transfer-
            if(objType in setOf(67,6701))
            {

                if(!payLoadMutable.value.isNullOrEmpty())
                {
                    if(destineLocation.text.isNullOrEmpty())
                    {
                            if (payLoadMutable.value.length!=20)
                            {
                                warehouseViewModel.verificationLocation(payLoadMutable.value,"")
                                /*if(objType==1701)
                                {
                                    for(i in 0 until mergedList.size)
                                    {
                                        destineLocation = BinLocation(id = mergedList.get(i).LocationCode, text =mergedList.get(i).LocationName)
                                        //warehouseViewModel.verificationLocation(mergedList.get(i).LocationName,"")
                                    }
                                }
                                else {

                                }*/
                            }
                        destineLocation = BinLocation(id = warehouseValue.value.location.AbsEntry.toString(), text =warehouseValue.value.location.BinCode)
                        Log.e("REOS","BottomSheet-Rowasd-warehouseValue.value.location.BinCodee: "+warehouseValue.value.location.BinCode)
                        Log.e("REOS","BottomSheet-Rowasd-warehouseValue.value.location.AbsEntry: "+warehouseValue.value.location.AbsEntry)
                    }
                }
            }

            if(objType !in setOf(67,6701))
            {
                if(destineLocation.text.isNullOrEmpty())
                {
                    for (i in 0 until suggestionValue.value.Data.size) {
                        Log.e(
                            "REOS",
                            "BottomSheet-Rowasd-suggestionValue.value.Data.get(i).BinCode: " + suggestionValue.value.Data.get(
                                i
                            ).BinCode
                        )
                        Log.e(
                            "REOS",
                            "BottomSheet-Rowasd-suggestionValue.value.Data.get(i).AbsEntry: " + suggestionValue.value.Data.get(
                                i
                            ).AbsEntry
                        )
                        Log.e(
                            "REOS",
                            "BottomSheet-Rowasd-payLoadMutable.value: " + payLoadMutable.value
                        )
                        if(objType in setOf(67,6701)) {
                            Log.e(
                                "REOS",
                                "BottomSheet-Rowasd-despuesllenado-destineLocation.text: " + destineLocation.text
                            )
                            destineLocation = BinLocation(
                                id = suggestionValue.value.Data.get(i).AbsEntry.toString(),
                                text = payLoadMutable.value
                            )
                        } else {
                            if (suggestionValue.value.Data.get(i).BinCode.equals(payLoadMutable.value)) {
                                destineLocation = BinLocation(
                                    id = suggestionValue.value.Data.get(i).AbsEntry.toString(),
                                    text = suggestionValue.value.Data.get(i).BinCode
                                )
                            }
                        }
                    }
                }
            }

            Log.e("REOS","BottomSheet-Rowasd-destineLocation: "+ destineLocation.toString())
            Log.e("REOS","BottomSheet-Rowasd-stockTrnsfBySbRspnsValue.value.typ: "+ stockTrnsfBySbRspnsValue.value.type)
            Log.e("REOS","BottomSheet-Rowasd-payLoadMutable.value: "+ payLoadMutable.value)
            Log.e("REOS","BottomSheet-Rowasd-despuesllenado-destineLocation.text: "+destineLocation.text )
            Log.e("REOS","BottomSheet-Rowasd-despuesllenado-destineLocation.id: "+destineLocation.id )
            if(stockTrnsfBySbRspnsValue.value.type==TypeCode.SSCC){
                Log.e("REOS","BottomSheet-Rowasd-TypeCode.SSCC-Entro")
                if(mergedList[0].Quantity == mergedList[0].QuantityDestine){
                    showMessageModalError("No hay stock pendiente de ubicar para este producto y lote")
                }else{
                    Log.e("JEPICAME","VALOREE@@@=>"+ destineLocation.text +"<>"+ BinLocation().text)
                    if(objType!=1701 && value.Payload.isNotEmpty() &&  destineLocation.text == BinLocation().text){
                        Log.e("REOS","BottomSheet-TypeCode.SSCC-Rowasd-payLoadMutable.value: "+ payLoadMutable.value)
                        if(objType in setOf(67,6701))
                        {
                            suggestionViewModel.getSuggestionList(
                                SuggestionPut(
                                    ItemCode = mergedList[0].Sscc!!,
                                    WareHouse = wareHouseDestine,
                                    Document = "$objType",
                                    BatchNumber = mergedList[0].Batch,
                                ),
                                objType.toString()
                            )
                        }else {
                            suggestionViewModel.getSuggestionList(
                                SuggestionPut(
                                    ItemCode = mergedList[0].Sscc!!,
                                    WareHouse = wareHouseDestine,
                                    Document = "$objType",
                                    BatchNumber = mergedList[0].Batch,
                                ),
                                objType.toString()
                            )
                        }
                    }
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
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(5.dp)
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
                                                    text = "Ubicación Origen: ",
                                                    style = MaterialTheme.typography.subtitle1,
                                                    color = MaterialTheme.colors.onBackground
                                                )

                                                Text(
                                                    text = item.LocationName,
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
                            }
                            else{
                                /////////////////////////////////////TIPO DE UBICACION/////////////////////////////////////

                                //var typeLocation by remember { mutableStateOf("") }
                                Text("")
                                ////////////////////////////////////////////////////////////////////////
                                if(objType!=1701 /*&& typeLocation.isNotEmpty()*/){ //ES DIFERENTE A PICKING
                                    Text("Ubicación destino "+if(destineLocation.text.isNullOrEmpty()){""}else{"[${destineLocation.text}]"}, fontWeight = FontWeight.Bold)
                                    xddVs2(
                                        stockTransferBandSRpsValue=mergedList,
                                        suggestions=suggestionViewModel,
                                        value=if(destineLocation.text.isNotEmpty() || destineLocation.text!=value.Payload){value.Payload}else{""},
                                        onLoader = {

                                            if(it.text!=destineLocation.text || value.Payload.split("-").size<3){
                                                destineLocation=it
                                            }
                                        },
                                        onTryAgain={
                                            onTryAgainSuggestion()
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
                                            //StatusOpenFormAddDestiny.value="N"
                                        }
                                    ){
                                        Text(
                                            text = "Agregar",
                                            color=Color.White
                                        )
                                    }
                                    Button( onClick = {
                                        Onclose()
                                        //StatusOpenFormAddDestiny.value="N"
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
            }
            else{
                Log.e("REOS","BottomSheet-Rowasd-mergedList.size: "+mergedList.size)
                for(i in 0 until mergedList.size)
                {
                    Log.e("REOS","BottomSheet-Rowasd-mergedList.get(i).ItemCode: "+mergedList.get(i).ItemCode)
                    Log.e("REOS","BottomSheet-Rowasd-mergedList.get(i).Batch: "+mergedList.get(i).Batch)
                    Log.e("REOS","BottomSheet-Rowasd-mergedList.get(i).LocationName: "+mergedList.get(i).LocationName)
                    LocationName=mergedList.get(i).LocationName

                }
                if(mergedList.size!=1){
                    showMessageModalError("Solo los códigos SSCC pueden albergar más de un producto")
                }else{
                    Log.e("REOS","BottomSheet-Rowasd-mergedList[0].Quantitye: "+mergedList[0].Quantity)
                    Log.e("REOS","BottomSheet-Rowasd-mergedList[0].QuantityDestine: "+mergedList[0].QuantityDestine)
                    if(mergedList[0].Quantity == mergedList[0].QuantityDestine&&(objType !in setOf(67,6701))){
                        showMessageModalError("No hay stock pendiente de ubicar para este producto y lote")
                    }else{
                        Log.e("REOS","BottomSheet-Rowasd-objTypee: "+objType)
                        Log.e("REOS","BottomSheet-Rowasd-mergedList[0].Quantitye: "+mergedList[0].Quantity)
                        Log.e("REOS","BottomSheet-Rowasd-destineLocation.text: "+destineLocation.text)
                        Log.e("REOS","BottomSheet-Rowasd-BinLocation().text: "+BinLocation().text)
                        Log.e("JEPICAME","VALOREE@@@=>"+ destineLocation.text +"<>"+ BinLocation().text)
                        Log.e("REOS","BottomSheet-Rowasd-value.Payload: "+value.Payload)
                        if(objType!=1701 && value.Payload.isNotEmpty() && destineLocation.text == BinLocation().text){
                            Log.e("REOS","BottomSheet-Rowasd-SuggestionPut-mergedList[0].ItemCode: "+mergedList[0].ItemCode)
                            Log.e("REOS","BottomSheet-Rowasd-SuggestionPut-wareHouseDestine: "+wareHouseDestine)
                            Log.e("REOS","BottomSheet-Rowasd-SuggestionPut-mergedList[0].Batch: "+ mergedList[0].Batch)

                            if(objType in setOf(67,6701))
                            {
                                suggestionViewModel.getSuggestionList(
                                    SuggestionPut(
                                        ItemCode = mergedList[0].ItemCode,
                                        //WareHouseCode = wareHouseDestine,
                                        WareHouse = wareHouseDestine,
                                        Document = "$objType",
                                        //BatchNumber =  mergedList[0].Batch,
                                    ),
                                    objType.toString()
                                )
                            }else
                            {
                                suggestionViewModel.getSuggestionList(
                                    SuggestionPut(
                                        ItemCode = mergedList[0].ItemCode,
                                        WareHouse = wareHouseDestine,
                                        Document = "$objType",
                                    ),
                                    objType.toString()
                                )
                            }
                        }
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

                                /*if(objType==67)
                                {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier= Modifier.fillMaxWidth()
                                    ){
                                        Text(text ="Cantidad ")
                                        Text(text = " ${mergedList[0].Quantity}",color= Color.Gray)
                                    }
                                }else {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier= Modifier.fillMaxWidth()
                                    ){
                                        Text(text ="Cantidad pendiente")
                                        Text(text = " ${mergedList[0].Quantity - mergedList[0].QuantityDestine}",color= Color.Gray)
                                    }
                                }*/
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
                                Log.e("REOS","BottomSheet-Rowasd-destineLocation.text: "+destineLocation.text)
                                if(objType!in setOf(
                                        0
                                        //1701
                                    )){ //ES DIFERENTE A PICKING
                                    if(objType==1701) {
                                        Text(
                                            text="Ubicación sugerida: "+LocationName, fontWeight = FontWeight.Bold
                                        )
                                        Text("")
                                            }

                                    Text(if(objType==1701){"Ubicación confirmada: "}else{"Ubicación destino "}  +if(destineLocation.text.isNullOrEmpty()){""}else{"[${destineLocation.text}]"}, fontWeight = FontWeight.Bold)
                                    Text("")

                                    if(objType!in setOf(22,1701))
                                    {
                                        xddVs2(
                                            stockTransferBandSRpsValue=mergedList,
                                            suggestions=suggestionViewModel,
                                            value=if(destineLocation.text.isNotEmpty() || destineLocation.text!=value.Payload){value.Payload}else{""},
                                            onLoader = {
                                                if(it.text!=destineLocation.text || value.Payload.split("-").size<3){
                                                    destineLocation=it
                                                }
                                            },onTryAgain={
                                                onTryAgainSuggestion()
                                            }
                                        )
                                    }
                                }

                                Log.e("REOS","BottomSheet-Rowasd-FormLocationDestine.mergedList[0].Quantity : "+mergedList[0].Quantity )
                                Log.e("REOS","BottomSheet-Rowasd-FormLocationDestine.mergedList[0].QuantityDestine: "+mergedList[0].QuantityDestine)
                                Log.e("REOS","BottomSheet-Rowasd-FormLocationDestine.mergedList: "+mergedList.toString())
                                FormLocationDestine(

                                    quantityText=if(objType in setOf(1701,67,6701,234000031,1250000001)){"${mergedList[0].Quantity - mergedList[0].QuantityDestine}"}else{"1.00"},
                                    onPress={

                                        val locationOrigins:List<ManyToOne> = calculateBinLocationOrigin(it.quantity,mergedList,objType)
                                        Log.e("REOS","BottomSheet-Rowasd-FormLocationDestine.locationOrigins: "+locationOrigins.toString())
                                        if(locationOrigins.isEmpty()){
                                            Toast.makeText(context, "Stock Insuficiente", Toast.LENGTH_LONG).show()
                                        }else{
                                            if(destineLocation.text.isNullOrEmpty() && objType!=1701){
                                                Toast.makeText(context, "Es necesario ingresar una ubicación destino", Toast.LENGTH_LONG).show()
                                            }else{
                                                when(objType){
                                                    1701->{
                                                        if(mergedList.sumOf{it.Quantity}==it.quantity){
                                                           // if(destineLocation.text.equals(LocationName)) {
                                                                it.idBody =
                                                                    mergedList[0]._StockTransferBody
                                                                it.batch = mergedList[0].Batch
                                                                it.origin = locationOrigins
                                                                it.destine = destineLocation
                                                                onSelect(listOf(it))
                                                           /* }else {
                                                                Toast.makeText(context, "La ubicación sugerida, debe ser la misma que la ubicacion de destino", Toast.LENGTH_LONG).show()
                                                            }*/
                                                                //StatusOpenFormAddDestiny.value="N"
                                                        }else{
                                                            Toast.makeText(context, "Es necesario que la cantidad ingresada sea igual a la cantidad solicitada.", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                    else->{
                                                        Log.e("REOS","BottomSheet-Rowasd-ButtonAgregarDestiny")
                                                        it.idBody=mergedList[0]._StockTransferBody
                                                        it.batch=mergedList[0].Batch
                                                        it.origin=locationOrigins
                                                        it.destine=destineLocation
                                                        //it.quantity=mergedList[0].Quantity
                                                        Log.e("REOS","BottomSheet-Rowasd-ButtonAgregarDestiny")
                                                        StatusOpenFormAddDestiny.value="N"
                                                        Log.e("REOS","BottomSheet-Rowasd-StatusOpenFormAddDestiny.value: "+StatusOpenFormAddDestiny.value)
                                                        onSelect( listOf(it))
                                                        destineLocation = BinLocation(id = "", text ="")
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
            }
        }
        else->{
            showMessageModalError(stockTrnsfBySbRspnsValue.value.status)
        }
    }
}

@Composable
private fun showMessageModalError(mensaje:String){
    Column(modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)) {
        Text(
            text = " Ocurrio algo inesperado",
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

private fun calculateBinLocationOrigin(cantidadSolicitada:Double,listSubBody:List<MergedStockTransfer>,objType: Int):List<ManyToOne>{

    val mutableList: MutableList<ManyToOne> = object : ArrayList<ManyToOne>(){}
    /////////////////////////////////////////////////////
    //var xd=masCercano(stockTransferBandSRpsValue.stockTransferSubBody,cantidadSolicitada.toDouble())
    Log.e("JEPICAME","ANTES DE LIMPIAR "+mutableList.size)
    Log.e("REOS","BottomSheet-calculateBinLocationOrigin-cantidadSolicitada: "+cantidadSolicitada)
    Log.e("REOS","BottomSheet-calculateBinLocationOrigin-listSubBody: "+listSubBody.size)
    Log.e("REOS","BottomSheet-calculateBinLocationOrigin-objType: "+objType)
    mutableList.clear()
    Log.e("JEPICAME","DSP DE LIMPIAR "+mutableList.size)
    /////////////////////////////////////////////////////
    var data=listSubBody
    var resto:Double= cantidadSolicitada

    if( cantidadSolicitada <= listSubBody.sumOf{ (it.Quantity - it.Destine.sum("Quantity").toDouble()) }||(objType in setOf(67,6701))){
        Log.e("REOS","BottomSheet-calculateBinLocationOrigin-ENTROIF ")
        //Log.e("REOS","BottomSheet-calculateBinLocationOrigin-it.Quantity "+it.Quantity)
        data=data.sortedByDescending { it.Quantity }
        Log.e("REOS","BottomSheet-calculateBinLocationOrigin-data "+data.toString())
        data.forEachIndexed{ i,item->
            Log.e("REOS","BottomSheet-calculateBinLocationOrigin-i "+i)
            Log.e("REOS","BottomSheet-calculateBinLocationOrigin-item "+item)
            Log.e("REOS","BottomSheet-calculateBinLocationOrigin-resto "+resto)
            Log.e("REOS","BottomSheet-calculateBinLocationOrigin-item.Destine.sum(\"Quantity\"): "+item.Destine.sum("Quantity"))
            Log.e("REOS","BottomSheet-calculateBinLocationOrigin-item.Quantity: "+item.Quantity)
            try {
                if(objType !in setOf(67,6701))
                {
                    if(resto==0.0 || item.Destine.sum("Quantity").toDouble()==item.Quantity){
                        Log.e("JEPICAME","ENTRO A IF RETURN")
                        return@forEachIndexed
                    }
                }

                //7  > 3
                Log.e("REOS","BottomSheet-calculateBinLocationOrigin-item.Quantity "+item.Quantity)
                Log.e("REOS","BottomSheet-calculateBinLocationOrigin-item.Destine.sum(\"Quantity\").toDouble() "+item.Destine.sum("Quantity").toDouble())
                Log.e("REOS","BottomSheet-calculateBinLocationOrigin-resto "+resto)
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
            }catch (e:Exception)
            {
                Log.e("REOS","BottomSheet-calculateBinLocationOrigin-error: "+e.toString())
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
                        Log.e("REOS","BottomSheet-FormLocationDestine-e: "+e.toString())
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
    onCloseBottomSheet :()->Unit,
    StatusScan:MutableState<String>,
    merchandiseBody:StockTransferBodyResponse,
    commentReception:String,
    locationReception:String,
){
    Log.e("REOS","BottomSheet-SelectTypescanModal-StatusScan.value: "+StatusScan.value)
    Log.e("REOS","BottomSheet-SelectTypescanModal-whsOrigin: "+whsOrigin)
    Log.e("REOS","BottomSheet-SelectTypescanModal-objType: "+objType)
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

    Log.e("REOS","BottomSheet-SelectTypescanModal-value.Type: "+value.Type.toString())
    Log.e("REOS","BottomSheet-SelectTypescanModal-value.Payload: "+value.Payload.toString())
    if(value.Payload.split("-").size==4 && value.Type!="LABEL-TYPE-QRCODE"){
        if(objType in setOf(67,6701,1250000001))
        {
            warehouseViewModel.getLocations(value.Payload,whsOrigin,objType)
        }
    }else if(value.Payload.isNotEmpty()){
        Log.e("Jepicame idJehdaer","=>SE EJEUCTA DESDE 965 otra vez")
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
        },
        responseHandheld=value.Payload,
        StatusScan=StatusScan,
        merchandiseBody=merchandiseBody,
        commentReception=commentReception,
        locationReception=locationReception
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
   onClosePressed: () -> Unit,
    responseHandheld:String,
    StatusScan:MutableState<String>,
    merchandiseBody:StockTransferBodyResponse,
    commentReception:String,
    locationReception:String,
){
    Log.e("REOS","BottomSheet-ASJDHASJKD-StatusScan.value: "+StatusScan.value)
    Log.e("REOS","BottomSheet-ASJDHASJKD-type"+type)
    val articleValue = itemsViewModel.article.collectAsState()
    val warehouseValue = warehouseViewModel.location.collectAsState()
    val qualityValue = qualityViewModel.quality.collectAsState()
    val documentBodyValue = documentBodyVm.documentBody.collectAsState()

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
            Log.e("REOS","BottomSheet-ASJDHASJKD-qualityValue.value.status-error: "+articleValue.value.status)
        }
    }

    //22 ES codigo de recepcion de mercaderia de las orden de compra
    if(objType==22||objType==18){
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
                Log.e("REOS","BottomSheet-ASJDHASJKD-error: "+articleValue.value.status)
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

                        if(objType==6701&&articleValue.value.type!=TypeCode.SSCC
                            //||objType==1250000001||objType==67
                        ){ //si es sloting y el articulo es un palet la ubicacion origen debe estar de acuerdo a la ubicacion de l pallet
                            Log.e("REOS","BottomSheet-ASJDHASJKD-ok-objType==6701-articleValue.value.defaultLocation: "+articleValue.value.defaultLocation)
                            Log.e("REOS","BottomSheet-ASJDHASJKD-ok-objType==6701-articleValue.value.warehouse: "+articleValue.value.warehouse)
                            warehouseViewModel.getLocations(articleValue.value.defaultLocation  ,articleValue.value.warehouse,objType)
                        }
                    }else{
                        Toast.makeText(context, "No puedes generar esta operación por que el almacén origen es $wareHouseOrigin y el palet escaneado actualmente se encuentra en el almacén ${articleValue.value.warehouse}", Toast.LENGTH_LONG).show()
                        onClosePressed()
                    }
                }else{
                    itemResponse=articleValue.value
                }

                itemsViewModel.resetArticleStatus()
            }
            "vacio"->{
                Toast.makeText(context, "El código escaneado no se encuentra en el maestro de articulos", Toast.LENGTH_LONG).show()
                itemsViewModel.resetArticleStatus()
                onClosePressed()
            }
            else->{
                Toast.makeText(context, articleValue.value.status, Toast.LENGTH_LONG).show()
                itemsViewModel.resetArticleStatus()
                onClosePressed()
            }
        }
    }

    if(articleValue.value.type!=TypeCode.SSCC) {
        when (warehouseValue.value.status) {
            "" -> {}
            "cargando" -> {
                CustomProgressDialog("Buscando ubicación...")
            }
            "ok" -> {
                binLocation = warehouseValue.value
                warehouseViewModel.resetLocationStatus()
            }
            "vacio" -> {
                Toast.makeText(
                    context,
                    "El código escaneado no representa una ubicación del almacén origen.",
                    Toast.LENGTH_LONG
                ).show()
                warehouseViewModel.resetLocationStatus()
            }
            else -> {
                Log.e(
                    "REOS",
                    "BottomSheet-ASJDHASJKD-warehouseValue.value.status: " + warehouseValue.value.status
                )
                Toast.makeText(
                    context,
                    "Ocurrio un error:\n ${warehouseValue.value.status}",
                    Toast.LENGTH_LONG
                ).show()
                warehouseViewModel.resetLocationStatus()
            }
        }
    }

    if( true /*itemResponse.items.isNotEmpty() */){
        Column(modifier=Modifier.padding(top=20.dp)){

            if(itemResponse.type==TypeCode.SSCC && objType!=22){
                Text(
                    text = if(itemResponse.nameSscc.isNullOrEmpty()){"Codigo del SSCC [XXXX]"}else{"SSCC "+itemResponse.nameSscc},
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 20.dp, bottom = 10.dp,end=50.dp)
                )
            }else{

                if(objType==22
                    //||objType==1250000001||objType==67
                )
                {
                    if(responseHandheld.length==12)
                    {
                        Text(
                            text = if(itemResponse.items.isNullOrEmpty()){"Registrar artículo"}else{itemResponse.items[0].item.ItemName},
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 20.dp, bottom = 10.dp,end=50.dp))
                    }

                }else
                {
                    Text(
                        text = if(itemResponse.items.isNullOrEmpty()){"Registrar artículo"}else{itemResponse.items[0].item.ItemName},
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 20.dp, bottom = 10.dp,end=50.dp)
                    )
                }


            }

            Divider(modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(start = 20.dp, bottom = 10.dp))

            Column(modifier= Modifier
                .padding(horizontal=16.dp) //.background(Color.Red)
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
                                TopresponseLocationAndItem=ResponseLocationAndItem(locationResponse = binLocation,itemResponse= if(itemResponse.items.size==0){ ItemResponse() }else{itemResponse.items[0]}),
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
                                    Log.e("REOS","BottomSheet-ASJDHASJKD-it: "+it.toString())
                                    Log.e("REOS","BottomSheet-ASJDHASJKD-itemResponse: "+itemResponse.items)

                                    // }

                                },onClosePressed={
                                    /*if(objType==22){
                                        documentBodyVm.resetDocumentBody()
                                    }*/

                                    onClosePressed()
                                },
                                onSearch = {
                                    if(objType==22
                                        //||objType==1250000001||objType==67
                                    ){
                                        documentBodyVm.getArticleFromBody(it)
                                    }else{
                                        Log.e("Jepicame idJehdaer","=>SE EJEUCTA DESDE 1387")
                                        itemsViewModel.getArticle(
                                            value=it,
                                            idHeader=idHeader,
                                            typeInventario=""
                                        )
                                    }
                                },
                                type=type,
                                responseHandheld=responseHandheld,
                                StatusScan=StatusScan,
                                merchandiseBody=merchandiseBody,
                                commentReception=commentReception,
                                locationReception=locationReception,
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
    onClosePressed: () -> Unit,
    type:TypeReadSKU,
    responseHandheld:String,
    StatusScan:MutableState<String>,
    merchandiseBody:StockTransferBodyResponse,
    commentReception:String,
    locationReception:String,
){
    var appContext= LocalContext.current
    Log.e(
        "REOS",
        "BottomSheet-formHandheld-type: " +type
    )
    Log.e(
        "REOS",
        "BottomSheet-formHandheld-responseHandheld: " + responseHandheld
    )
    var responseLocationAndItem by  remember {mutableStateOf(TopresponseLocationAndItem)}
    Log.e("REOS","BottomSheet-formHandheld-responseLocationAndItem.itemResponse.item.ItemCode: "+ responseLocationAndItem.itemResponse.item.ItemCode)
    Log.e("REOS","BottomSheet-formHandheld-responseLocationAndItem.itemResponse.quantity: "+ responseLocationAndItem.itemResponse.quantity)
    Log.e("REOS","BottomSheet-formHandheld-responseLocationAndItem.itemResponse.defaultBinLocation: "+ responseLocationAndItem.itemResponse.defaultBinLocation)
    responseLocationAndItem.itemResponse.quantity
    responseLocationAndItem=TopresponseLocationAndItem
    //Log.e("REOS","BottomSheet-formHandheld-responseLocationAndItem_con_data:"+ responseLocationAndItem)
    Log.e("REOS","BottomSheet-formHandheld-TopresponseLocationAndItem.itemResponse.item.ItemCode: "+ TopresponseLocationAndItem.itemResponse.item.ItemCode)
    Log.e("REOS","BottomSheet-formHandheld-TopresponseLocationAndItem.itemResponse.quantity: "+ TopresponseLocationAndItem.itemResponse.quantity)
    Log.e("REOS","BottomSheet-formHandheld-TopresponseLocationAndItem.itemResponse.defaultBinLocation: "+ TopresponseLocationAndItem.itemResponse.defaultBinLocation)
    var idArticle by    remember {mutableStateOf(responseLocationAndItem.itemResponse.item.ItemCode)}
    var textLote by   remember { mutableStateOf( responseLocationAndItem.itemResponse.lote)}

    var idLocation by    remember {mutableStateOf(responseLocationAndItem.locationResponse.location.AbsEntry )}
    //var textLocation by   remember {mutableStateOf( responseLocationAndItem.locationResponse.location.BinCode )}
    var HassLocation by    remember {mutableStateOf( responseLocationAndItem.locationResponse.EnableBinLocations )}

    var quantity by  remember {mutableStateOf(""+responseLocationAndItem.itemResponse.quantity)}
    var quanlityVal by    remember {mutableStateOf(QualityControl_Collection())}

    var optionAdd by  remember {mutableStateOf(false)}
    var optionCancell by remember {mutableStateOf(false)}
    var haveError by remember  {mutableStateOf("")}
    val keyboardController = LocalSoftwareKeyboardController.current
    var textLocation by remember { mutableStateOf("") }
    var EnableBinLocations by remember { mutableStateOf("") }
    val warehouseViewModel: WarehouseViewModel = viewModel(
        factory = WarehouseViewModel.WarehouseViewModelFactory(
            "1", "",0
        )
    )
    var warehouseViewModelResponse=warehouseViewModel.location.collectAsState()
    var almacenesResponse=warehouseViewModel.almacenes.collectAsState()
    Log.e(
        "REOS",
        "BottomSheet-formHandheld-warehouseViewModelResponse.value.location.AbsEntry: " + warehouseViewModelResponse.value.location.AbsEntry
    )
    Log.e("REOS", "BottomSheet-formHandheld-itemsResponse.defaultLocation: " + itemsResponse.defaultLocation)
    Log.e(
        "REOS",
        "BottomSheet-formHandheld-warehouseViewModelResponse.value.location.AbsEntry: " + warehouseViewModelResponse.value.location.AbsEntry
    )
    Log.e("REOS", "BottomSheet-formHandheld-almacenesResponse.value.warehouse.size: " + almacenesResponse.value.warehouse.size)
    Log.e("REOS", "BottomSheet-formHandheld-responseLocationAndItem.locationResponse.EnableBinLocations: " + responseLocationAndItem.locationResponse.EnableBinLocations)
    if(objType==22)
    {
       for(i in 0 until merchandiseBody.stockTransferBody.size)
       {
           if(merchandiseBody.stockTransferBody.get(i).body.ItemCode==idArticle)
           {

               if(almacenesResponse.value.warehouse.size==0)
               {
                   warehouseViewModel.getWarehouse(merchandiseBody.stockTransferBody.get(i).body.Warehouse)
               }
           }
       }
    }

    if(almacenesResponse.value.warehouse.size>0)
    {
        for (i in 0 until almacenesResponse.value.warehouse.size)
        {
            Log.e("REOS", "BottomSheet-formHandheld-almacenesResponse.value.warehouse.get(i).WarehouseName: " + almacenesResponse.value.warehouse.get(i).WarehouseName)
            Log.e("REOS", "BottomSheet-formHandheld-almacenesResponse.value.warehouse.get(i).WarehouseCode: " + almacenesResponse.value.warehouse.get(i).WarehouseCode)
            Log.e("REOS", "BottomSheet-formHandheld-almacenesResponse.value.warehouse.get(i).EnableBinLocations: " + almacenesResponse.value.warehouse.get(i).EnableBinLocations)
            EnableBinLocations=almacenesResponse.value.warehouse.get(i).EnableBinLocations
        }
    }


    if(itemsResponse.type == TypeCode.SSCC)
    {
        if(!itemsResponse.defaultLocation.isNullOrEmpty()&&!itemsResponse.defaultLocation.equals(responseLocationAndItem.locationResponse.location.BinCode))
        {
            Log.e(
                "REOS",
                "BottomSheet-formHandheld-invoco-warehouseViewModel.verificationLocation"
            )
            warehouseViewModel.verificationLocation(binCode = itemsResponse.defaultLocation.toString(), AbsEntry ="")
        }
    }

    idLocation = responseLocationAndItem.locationResponse.location.AbsEntry
    //textLocation = responseLocationAndItem.locationResponse.location.BinCode

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


    Log.e("REOS", "BottomSheet-formHandheld-locationReception: " + locationReception)
    Log.e("REOS", "BottomSheet-formHandheld-commentReception: " +commentReception)
    Log.e("REOS", "BottomSheet-formHandheld-commentReception: " +commentReception)
    Log.e("REOS", "BottomSheet-formHandheld-itemsResponse.type : " +itemsResponse.type )
    Log.e("REOS", "BottomSheet-formHandheld-TypeCode.SSCC : " +TypeCode.SSCC )
    Log.e("REOS", "BottomSheet-formHandheld-merchandiseBody.wareHouseDestine : " +merchandiseBody.wareHouseDestine )
    Log.e("REOS", "BottomSheet-formHandheld-merchandiseBody.wareHouseOrigin: " +merchandiseBody.wareHouseOrigin)
/*    Log.e("REOS", "BottomSheet-formHandheld-merchandiseBody.stockTransferBody.get(0).body.ItemCode : " +merchandiseBody.stockTransferBody.get(0).body.ItemCode)
    Log.e("REOS", "BottomSheet-formHandheld- merchandiseBody.stockTransferBody.get(0).body.Quantity : " + merchandiseBody.stockTransferBody.get(0).body.Quantity )
    Log.e("REOS", "BottomSheet-formHandheld-merchandiseBody.stockTransferBody.get(0).body.TotalQuantity : " +merchandiseBody.stockTransferBody.get(0).body.TotalQuantity )
    Log.e("REOS", "BottomSheet-formHandheld-merchandiseBody.stockTransferBody.get(0).body.Warehouse: " +merchandiseBody.stockTransferBody.get(0).body.Warehouse )*/



    var itemCodeHandheld:String=""
    var loteHandheld:String=""
    //charge textLocation location current if elements equals one or itemCodeHandheld and loteHandheld if elements equals three
    try {
            if (type.toString().replace(" ", "").equals("HANDHELD")) {
                if(!responseHandheld.isNullOrEmpty())
                {
                    val elements = responseHandheld.split("|", limit = 3)
                    if (!elements.isNullOrEmpty()) {
                        when (elements.size) {
                            //Response Handheld Location
                            1 -> {
                                for (i in 0 until elements.size) {
                                    textLocation = elements.get(i)
                                }
                                if (objType in setOf(67, 6701, 1250000001) && itemsResponse.type != TypeCode.SSCC
                                ) {

                                    if (responseLocationAndItem.locationResponse.location.AbsEntry.toString()
                                            .isNullOrEmpty() || responseLocationAndItem.locationResponse.location.AbsEntry == 0
                                    ) {
                                        if (textLocation.length != 20) {
                                            warehouseViewModel.verificationLocation(
                                                binCode = textLocation,
                                                AbsEntry = ""
                                            )
                                        }
                                    }

                                }
                            }
                            //Response Handheld ItemCode and Batch
                            3 -> {
                                for (i in 0 until elements.size) {
                                    when (i) {
                                        0 -> {
                                            itemCodeHandheld = elements.get(i)
                                        }
                                        1 -> {
                                            loteHandheld = elements.get(i)
                                        }
                                    }
                                }
                                responseLocationAndItem.itemResponse.item.ItemCode =
                                    itemCodeHandheld
                                responseLocationAndItem.itemResponse.lote = loteHandheld

                                if (objType == 67 && commentReception.equals("Recepción de Producción")) {
                                    textLocation = locationReception
                                    if (responseLocationAndItem.locationResponse.location.AbsEntry.toString()
                                            .isNullOrEmpty() || responseLocationAndItem.locationResponse.location.AbsEntry == 0
                                    ) {
                                        if (textLocation.length != 20) {
                                            warehouseViewModel.verificationLocation(
                                                binCode = textLocation,
                                                AbsEntry = ""
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }catch (e:Exception){
        Log.e(
            "REOS",
            "BottomSheet-formHandheld-elementshandheld-error: " +e.toString()
        )
    }


    if(itemsResponse.type== TypeCode.QR//&& (!responseLocationAndItem.itemResponse.item.ItemCode.isNullOrEmpty())
    )
    {
        StatusScan.value=idArticle
        Log.e("REOS","BottomSheet-formHandheld-StatusScan.value: "+StatusScan.value)
        OutlinedTextField(
            //enabled= !responseLocationAndItem.itemResponse.item.ItemCode.isNullOrEmpty(),
            enabled= responseLocationAndItem.itemResponse.item.ItemCode.isNullOrEmpty(),
            singleLine=true,
            value =
            if(responseLocationAndItem.itemResponse.item.ItemCode.isNullOrEmpty())
            {
                idArticle
            }else
            {
                responseLocationAndItem.itemResponse.item.ItemCode
            },
            onValueChange = {
                idArticle = it
                if (idArticle.length>=7&&idArticle.length<=8)
                {
                    responseLocationAndItem.itemResponse.item.ItemCode=idArticle
                }
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
        Text("El pallet contiene ${itemsResponse.items.size} producto(s)")
        var color:Color=if(itemsResponse.statusSscc=="Abierto"){ MaterialTheme.colors.primary}else{ Color.Red}

        LazyRow(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f)) {
            items(items = itemsResponse.items, itemContent = { item ->

                Card(
                    border = if(itemsResponse.statusSscc=="Abierto"){BorderStroke(0.dp,Color.Transparent)}else{BorderStroke(2.dp,Color.Red)},
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
                                color = color
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

                        Spacer(modifier = Modifier.height(12.dp))
                        Row {
                            Text(
                                text = "Cantidad: ",
                                style = MaterialTheme.typography.subtitle1,
                                color = MaterialTheme.colors.onBackground
                            )
                            Text(
                                text = ""+item.quantity,
                                style = MaterialTheme.typography.subtitle1,
                                color = color
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
                                color = color
                            )
                        }
                        Row {
                            Text(
                                text = "Fecha Prod: ",
                                style = MaterialTheme.typography.subtitle1,
                                color = MaterialTheme.colors.onBackground
                            )
                            Text(
                                text = item.inDate,
                                style = MaterialTheme.typography.subtitle1,
                                color = color
                            )
                        }
                        Row {
                            Text(
                                text = "Fecha Vcto: ",
                                style = MaterialTheme.typography.subtitle1,
                                color = MaterialTheme.colors.onBackground
                            )
                            Text(
                                text = item.expireDate,
                                style = MaterialTheme.typography.subtitle1,
                                color = color
                            )
                        }

                        if(objType == 6701||objType ==67||objType ==1250000001){
                            Row {
                                Text(
                                    text = "Ubicación: ",
                                    style = MaterialTheme.typography.subtitle1,
                                    color = MaterialTheme.colors.onBackground
                                )
                                Text(
                                    text = itemsResponse.defaultLocation,
                                    style = MaterialTheme.typography.subtitle1,
                                    color = color
                                )
                            }
                        }
                    }
                }
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

    Text(text = " ")

    //EditBox Location not acces a SSCC
    if(itemsResponse.type != TypeCode.SSCC) {

        if (objType !in setOf(22, 18, 67,1701)) {
            //22 orden de compra y Factura de Proveedores
            //text=responseLocationAndItem.locationResponse.location.BinCode

            if (objType == 6701//||objType ==67
                || objType == 1250000001
            ) {
                //warehouseViewModel.verificationLocation(binCode = textLocation)
                if (itemsResponse.type == TypeCode.QR) {
                    OutlinedTextField(
                        //enabled = (HassLocation == "tYES" && idLocation == 0)
                        enabled = true
                        ,
                        singleLine = true,
                        value = textLocation,
                        onValueChange = {
                            //textLocation = it
                            textLocation = it
                            if(textLocation.length>=13)
                            {
                                warehouseViewModel.verificationLocation(
                                    binCode = textLocation,
                                    AbsEntry = ""
                                )
                            }
                        },
                        label = { Text(text = "Ubicación") },
                        placeholder = { Text(text = "Ingresar la ubicación") },
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_rack_24),
                                contentDescription = null,
                                tint = AzulVistony202
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                keyboardController?.hide()
                                warehouseViewModel.verificationLocation(
                                    binCode = textLocation,
                                    AbsEntry = ""
                                )
                            }
                        )
                    )
                }
            } else {
                //if(itemsResponse.type== TypeCode.QR&&!responseLocationAndItem.itemResponse.item.ItemCode.isNullOrEmpty()) {
                OutlinedTextField(
                    enabled = (HassLocation == "tYES" && idLocation == 0),
                    singleLine = true,
                    value = responseLocationAndItem.locationResponse.location.BinCode,
                    onValueChange = { /*textLocation = it*/ },
                    label = { Text(text = "Ubicación") },
                    placeholder = { Text(text = "Ingresar la ubicación") },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_rack_24),
                            contentDescription = null,
                            tint = AzulVistony202
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            keyboardController?.hide()
                        }
                    )
                )
                //}
            }

        }
        else {
            if (itemsResponse.type == TypeCode.QR && objType !in setOf(22, 18,1701)) {
                OutlinedTextField(
                    enabled = true,
                    singleLine = true,
                    value = textLocation,
                    onValueChange = {
                        //textLocation = textLocation
                        textLocation = it
                        if (textLocation.length == 12) {
                            if (responseLocationAndItem.locationResponse.location.AbsEntry.toString()
                                    .isNullOrEmpty() || responseLocationAndItem.locationResponse.location.AbsEntry == 0
                            ) {
                                warehouseViewModel.verificationLocation(
                                    binCode = textLocation,
                                    AbsEntry = ""
                                )
                            }
                        }
                    },
                    label = { Text(text = "Ubicación_") },
                    placeholder = { Text(text = "Ingresar la ubicación") },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_rack_24),
                            contentDescription = null,
                            tint = AzulVistony202
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            Log.e("REOS", "BottomSheet-formHandheld-text: " + textLocation)
                            keyboardController?.hide()
                            responseLocationAndItem.locationResponse.location.BinCode = textLocation
                            //responseLocationAndItem.locationResponse.location.AbsEntry=text.toInt()
                            //warehouseViewModel.verificationLocation(binCode = text)
                        }
                    )
                )
            }
            /*else if(itemsResponse.type == TypeCode.QR && objType in setOf(22)&&EnableBinLocations.equals("tYES")) {
                OutlinedTextField(
                    enabled = true,
                    singleLine = true,
                    value = textLocation,
                    onValueChange = {
                        //textLocation = textLocation
                        textLocation = it
                        if (textLocation.length == 12) {
                            if (responseLocationAndItem.locationResponse.location.AbsEntry.toString()
                                    .isNullOrEmpty() || responseLocationAndItem.locationResponse.location.AbsEntry == 0
                            ) {
                                warehouseViewModel.verificationLocation(
                                    binCode = textLocation,
                                    AbsEntry = ""
                                )
                            }
                        }
                    },
                    label = { Text(text = "Ubicación_") },
                    placeholder = { Text(text = "Ingresar la ubicación") },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_rack_24),
                            contentDescription = null,
                            tint = AzulVistony202
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            Log.e("REOS", "BottomSheet-formHandheld-text: " + textLocation)
                            keyboardController?.hide()
                            responseLocationAndItem.locationResponse.location.BinCode = textLocation
                            //responseLocationAndItem.locationResponse.location.AbsEntry=text.toInt()
                            //warehouseViewModel.verificationLocation(binCode = text)
                        }
                    )
                )
                }*/
        }
    }

    Log.e("REOS","BottomSheet-formHandheld-responseLocationAndItem.locationResponse.location.BinCode: "+responseLocationAndItem.locationResponse.location.BinCode)
    Log.e("REOS","BottomSheet-formHandheld-responseLocationAndItem.locationResponse.location.AbsEntry: "+responseLocationAndItem.locationResponse.location.AbsEntry)
    Text(text = " ")



    if(optionAdd){
        Text(text = "¿Está seguro de realizar esta operación? ", color = Color.Red)
        Text(text = " ")
    }

    if(optionCancell){
        Text(text = "¿Está seguro de cancelar esta operación? ", color = Color.Red)
        Text(text = " ")
    }

    //var disabledValidQuantity:String="N"
    var disabledValidQuantity by remember { mutableStateOf("N") }
    var textButtonAdd by remember { mutableStateOf(
        if(objType==1701)
        {"Confirmar"}else{"Agregar"}

    ) }
    if(haveError.isNotEmpty()){
        Text(text = haveError, color = Color.Red)
        Text(text = " ")
    }
    Log.e("REOS","BottomSheet-formHandheld-optionAdd: "+optionAdd)
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ){
        if(itemsResponse.statusSscc!="Cerrado"){
            Log.e("REOS","BottomSheet-formHandheld-itemsResponse.statusSscc: "+ itemsResponse.statusSscc)
            Button(
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray),
                onClick = {
                    /*if(itemsResponse.type!=TypeCode.SSCC) {
                        if (textLocation.length!=20)
                        {
                            warehouseViewModel.verificationLocation(binCode = textLocation, AbsEntry = "")
                        }

                    }*/
                    if (optionAdd) {
                        Log.e(
                            "REOS",
                            "BottomSheet-formHandheld-optionAdd-quantity: " +quantity
                        )

                        try {
                            Log.e("Jepicame","=>"+ quantity)
                            if (quantity.toDouble() != 0.0) {
                                Log.e("Jepicame","=difernte de cero>"+ quantity)
                                Log.e("REOS","BottomSheet-formHandheld-idArticle"+ idArticle)
                                try {
                                    Log.e("REOS","BottomSheet-formHandheld-responseLocationAndItem.itemResponse.item.ItemCode: "+ responseLocationAndItem.itemResponse.item.ItemCode)
                                    responseLocationAndItem.itemResponse.item.ItemCode=idArticle
                                }catch (e:Exception){
                                    Log.e("REOS","BottomSheet-formHandheld-responseLocationAndItem.itemResponse.item.ItemCode-error: "+ e.toString())
                                }

                                Log.e("REOS","BottomSheet-formHandheld-responseLocationAndItem.itemResponse.item.ItemCode-validacion: "+ responseLocationAndItem.itemResponse.item.ItemCode)
                                if (responseLocationAndItem.itemResponse.item.ItemCode.isNullOrEmpty()) {
                                    Log.e("REOS","=>"+ quantity)
                                    Log.e("REOS","BottomSheet-formHandheld-quantity-Es necesario buscar en el documento el artículo a ingresar"+ quantity)
                                    haveError = "Es necesario buscar en el documento el artículo a ingresar"
                                } else {

                                    var warehouse:String=""
                                    var quantityline:Double=0.0
                                    var quantityadvance:Double=0.0
                                    var quantityItemcode:Double=0.0
                                    var quantityBatch:Double=0.0
                                    for(i in 0 until merchandiseBody.stockTransferBody.size)
                                    {
                                        Log.e("REOS","BottomSheet-formHandheld-ButtonAgregar-merchandiseBody.stockTransferBody.get(i).body.ItemCode: "+ merchandiseBody.stockTransferBody.get(i).body.ItemCode)
                                        Log.e("REOS","BottomSheet-formHandheld-ButtonAgregar-responseLocationAndItem.itemResponse.item.ItemCode: "+ responseLocationAndItem.itemResponse.item.ItemCode)
                                        if (merchandiseBody.stockTransferBody.get(i).body.ItemCode.equals
                                                (responseLocationAndItem.itemResponse.item.ItemCode))
                                        {
                                            quantityItemcode++

                                            for(j in 0 until merchandiseBody.stockTransferBody.get(i).subBody.size)
                                            {
                                                Log.e("REOS","BottomSheet-formHandheld-ButtonAgregar-erchandiseBody.stockTransferBody.get(i).subBody.get(j).Batch: "+ merchandiseBody.stockTransferBody.get(i).subBody.get(j).Batch)
                                                Log.e("REOS","BottomSheet-formHandheld-ButtonAgregar-responseLocationAndItem.itemResponse.item.lote: "+ responseLocationAndItem.itemResponse.lote)
                                                if(merchandiseBody.stockTransferBody.get(i).subBody.get(j).Batch.equals(responseLocationAndItem.itemResponse.lote))
                                                {
                                                    quantityBatch++
                                                }
                                            }


                                            Log.e("REOS","BottomSheet-formHandheld-ENTRO IF WAREHOUSE ")
                                            warehouse=merchandiseBody.stockTransferBody.get(i).body.Warehouse
                                            if(merchandiseBody.stockTransferBody.get(i).body.Quantity.toString().isNullOrEmpty())
                                            {
                                                quantityline=0.0
                                            }else {
                                                quantityline=merchandiseBody.stockTransferBody.get(i).body.Quantity.toString().toDouble()
                                            }

                                            if(merchandiseBody.stockTransferBody.get(i).body.TotalQuantity.toString().isNullOrEmpty())
                                            {
                                                quantityadvance=0.0
                                            }else {
                                                quantityadvance=merchandiseBody.stockTransferBody.get(i).body.TotalQuantity.toString().toDouble()
                                            }


                                            Log.e("REOS","BottomSheet-formHandheld-merchandiseBody.stockTransferBody.get(i).body.TotalQuantity: "+merchandiseBody.stockTransferBody.get(i).body.TotalQuantity)
                                            Log.e("REOS","BottomSheet-formHandheld-merchandiseBody.stockTransferBody.get(i).body.Quantity: "+merchandiseBody.stockTransferBody.get(i).body.Quantity)
                                            Log.e("REOS","BottomSheet-formHandheld-quantityline: "+quantityline)
                                            Log.e("REOS","BottomSheet-formHandheld-quantityadvance: "+quantityadvance)
                                            //merchandiseBody.stockTransferBody.get(i).body.TotalQuantity
                                            //Log.e("REOS","BottomSheet-formHandheld-quantityline.isDigitsOnly(): "+quantityline.isDigitsOnly())
                                            //Log.e("REOS","BottomSheet-formHandheld-quantityadvance.isDigitsOnly(): "+quantityadvance.isDigitsOnly())
                                        }

                                    }
                                    Log.e("REOS","BottomSheet-formHandheld-quantityItemcode: "+quantityItemcode)
                                    if (quantityItemcode==0.0&& objType in setOf(1701))
                                    {
                                        haveError = "El producto no se encuentra en la lista"
                                        optionAdd = false
                                        disabledValidQuantity="Y"
                                        textButtonAdd="Scanee nuevamente"
                                        optionCancell = false
                                    }
                                    if (quantityBatch==0.0&& objType in setOf(1701))
                                    {
                                        haveError = "El lote no se encuentra vinculado al producto"
                                        optionAdd = false
                                        disabledValidQuantity="Y"
                                        textButtonAdd="Scanee nuevamente"
                                        optionCancell = false
                                    }
                                    else if (responseLocationAndItem.locationResponse.location.BinCode.isNullOrEmpty() && objType !in setOf(22, 18,1250000001,67,1701))
                                    {
                                        haveError = "Es necesario ingresar una ubicación"
                                    }
                                    /*else if(!responseLocationAndItem.locationResponse.location.Warehouse.toString().replace(" ", "").equals(warehouse.toString().replace(" ", ""))&&objType !in setOf(22, 18))
                                    {
                                        haveError = "El Almacen del Producto, no es el mismo que el de la ubicacion"
                                        //Toast.makeText(appContext, "El Almacen del Producto, no es el mismo que el de la ubicacion", Toast.LENGTH_LONG).show()
                                    }*/
                                    /*else if(objType in setOf(22, 18)&&disabledValidQuantity.equals("N"))
                                    {*/
                                        else if(quantity.toDouble()>quantityline.toDouble()&&objType in setOf(22, 18,1250000001)&&disabledValidQuantity.equals("N"))
                                        {
                                            haveError = "La cantidad recepcionada "+quantity+", es mayor a la cantidad de la linea del documento "+quantityline+". "
                                            disabledValidQuantity="Y"
                                            textButtonAdd="Continuar y agregar?"
                                            //Toast.makeText(appContext, "El Almacen del Producto, no es el mismo que el de la ubicacion", Toast.LENGTH_LONG).show()
                                        }
                                        else if((quantity.toDouble()+quantityadvance.toDouble())>quantityline.toDouble()&&objType in setOf(22, 18,1250000001)&&disabledValidQuantity.equals("N"))
                                        {
                                            haveError = "La cantidad recepcionada total "+(quantity.toDouble()+quantityadvance.toDouble())+", es mayor a la cantidad de la linea del documento "+quantityline+". "
                                            disabledValidQuantity="Y"
                                            textButtonAdd="Continuar y agregar?"
                                            //Toast.makeText(appContext, "El Almacen del Producto, no es el mismo que el de la ubicacion", Toast.LENGTH_LONG).show()
                                        }
                                        else if((quantity.toDouble()+quantityadvance.toDouble())>quantityline.toDouble()&&objType in setOf(22, 18,1250000001)&&disabledValidQuantity.equals("N"))
                                        {
                                            haveError = "La cantidad recepcionada total "+(quantity.toDouble()+quantityadvance.toDouble())+", es mayor a la cantidad de la linea del documento "+quantityline+". "
                                            disabledValidQuantity="Y"
                                            textButtonAdd="Continuar y agregar?"
                                            //Toast.makeText(appContext, "El Almacen del Producto, no es el mismo que el de la ubicacion", Toast.LENGTH_LONG).show()
                                        }

                                        /*else
                                        {
                                            disabledValidQuantity="Y"
                                        }*/
                                    //}
                                    /*else if (!quantity.isNullOrEmpty()&&!quantityline.isNullOrEmpty()&&objType in setOf(22, 18)&&disabledValidQuantity.equals("N"))
                                    {
                                        if(quantity.toDouble()>quantityline.toDouble()&&objType in setOf(22, 18)&&disabledValidQuantity.equals("N"))
                                        {
                                            haveError = "La cantidad recepcionada "+quantity+", es mayor a la cantidad de la linea del documento "+quantityline+". "
                                            disabledValidQuantity="Y"
                                            textButtonAdd="Continuar y agregar?"
                                            //Toast.makeText(appContext, "El Almacen del Producto, no es el mismo que el de la ubicacion", Toast.LENGTH_LONG).show()
                                        }
                                        else if((quantity.toDouble()+quantityadvance.toDouble())>quantityline.toDouble()&&objType in setOf(22, 18)&&disabledValidQuantity.equals("N"))
                                        {
                                            haveError = "La cantidad recepcionada total "+(quantity.toDouble()+quantityadvance.toDouble())+", es mayor a la cantidad de la linea del documento "+quantityline+". "
                                            disabledValidQuantity="Y"
                                            textButtonAdd="Continuar y agregar?"
                                            //Toast.makeText(appContext, "El Almacen del Producto, no es el mismo que el de la ubicacion", Toast.LENGTH_LONG).show()
                                        }
                                        else
                                        {
                                            disabledValidQuantity="Y"
                                        }
                                    }*/
                                    else{
                                        Log.e("REOS","BottomSheet-formHandheld-ENTRO AL ELSE")
                                        Log.e("Jepicame","=ENTRO AL ELSE")
                                        Log.e("REOS","BottomSheet-formHandheld-objType: "+ objType)
                                        Log.e("REOS","BottomSheet-formHandheld-quantity: "+ quantity)
                                        try
                                        {

                                            var listPayloadBody: List<StockTransferBodyPayload> = emptyList()
                                            if(objType==22||objType==18
                                                ||objType==1250000001||objType==67||objType==6701||objType==1701
                                            )
                                            {
                                                        var stockTransferBodyPayload: StockTransferBodyPayload =
                                                            StockTransferBodyPayload(
                                                                ItemCode = responseLocationAndItem.itemResponse.item.ItemCode,
                                                                ItemName = responseLocationAndItem.itemResponse.item.ItemName,
                                                                Batch = if (responseLocationAndItem.itemResponse.lote.isNullOrEmpty()) {
                                                                    textLote
                                                                } else {
                                                                    responseLocationAndItem.itemResponse.lote
                                                                },
                                                                Sku = if (responseLocationAndItem.itemResponse.item.Sku == null) {
                                                                    ""
                                                                } else {
                                                                    responseLocationAndItem.itemResponse.item.Sku!!
                                                                },
                                                                LocationCode =
                                                                    /*if(itemsResponse.type == TypeCode.SSCC)
                                                                    {
                                                                        warehouseViewModelResponse.value.location.AbsEntry.toString()
                                                                    }
                                                                    else{*/
                                                                        if (responseLocationAndItem.locationResponse.location.AbsEntry.toString()
                                                                                .isNullOrEmpty()
                                                                        ) {
                                                                            warehouseViewModelResponse.value.location.AbsEntry.toString()
                                                                        } else {
                                                                            responseLocationAndItem.locationResponse.location.AbsEntry.toString()
                                                                        }
                                                                   // }
                                                                ,
                                                                LocationName =
                                                                /*if(itemsResponse.type == TypeCode.SSCC)
                                                                {
                                                                    //itemsResponse.defaultLocation
                                                                    warehouseViewModelResponse.value.location.BinCode
                                                                }
                                                                else {*/
                                                                    if (responseLocationAndItem.locationResponse.location.BinCode.isNullOrEmpty()) {
                                                                        textLocation
                                                                    } else {
                                                                        responseLocationAndItem.locationResponse.location.BinCode
                                                                    }
                                                                //}
                                                                ,
                                                                Quantity = quantity.toDouble(),
                                                                Quality = quanlityVal,
                                                                Sscc = itemsResponse.nameSscc
                                                            )
                                                        listPayloadBody += stockTransferBodyPayload
                                                Log.e(
                                                    "REOS",
                                                    "BottomSheet-formHandheld-listPayloadBody.size-paso22: " +listPayloadBody.size
                                                )
                                            }else {

                                                listPayloadBody
                                                    itemsResponse.items.map {
                                                        Log.e("REOS","BottomSheet-formHandheld-antes-llenar-itemResponse.items")

                                                        if (itemsResponse.type == TypeCode.SSCC ) {
                                                            Log.e(
                                                                "REOS",
                                                                "BottomSheet-formHandheld-entroif-itemsResponse.items.map "
                                                            )
                                                            StockTransferBodyPayload(
                                                                ItemCode = it.item.ItemCode,
                                                                ItemName = it.item.ItemName,
                                                                Batch = it.lote,
                                                                Sscc = itemsResponse.nameSscc,
                                                                LocationCode = "${responseLocationAndItem.locationResponse.location.AbsEntry}",
                                                                LocationName = responseLocationAndItem.locationResponse.location.BinCode,
                                                                Quantity = it.quantity,
                                                                Quality = quanlityVal
                                                            )
                                                        } else
                                                        {
                                                            Log.e(
                                                                "REOS",
                                                                "BottomSheet-formHandheld-entroelse-itemsResponse.items.map "
                                                            )
                                                            Log.e("Jepicame","=ENTRO AL DIFERENTE DEL SSCC "+itemsResponse.type.toString() )
                                                            //Log.e("Jepicame","=ENTRO AL DIFERENTE DEL SSCC "+responseLocationAndItem.itemResponse.item.Sku!! )
                                                            Log.e("Jepicame","=ENTRO AL DIFERENTE DEL SSCC SUPERO ESTO")
                                                            Log.e("Jepicame","=ENTRO AL DIFERENTE DEL LOTE SUPERO ESTO "+if (responseLocationAndItem.itemResponse.lote.isNullOrEmpty()) {
                                                                textLote + "TEXTO"
                                                            } else {
                                                                responseLocationAndItem.itemResponse.lote + "Value Default"
                                                            }  )
                                                            Log.e(
                                                                "REOS",
                                                                "BottomSheet-formHandheld-entroelse-itemsResponse.type: " + itemsResponse.type
                                                            )

                                                            StockTransferBodyPayload(

                                                                ItemCode = responseLocationAndItem.itemResponse.item.ItemCode,
                                                                //ItemCode = it.item.ItemCode,
                                                                ItemName = responseLocationAndItem.itemResponse.item.ItemName,
                                                                //ItemName = it.item.ItemName,
                                                                Batch = if (responseLocationAndItem.itemResponse.lote.isNullOrEmpty()) {
                                                                    textLote
                                                                } else {
                                                                    responseLocationAndItem.itemResponse.lote
                                                                },
                                                                Sku = if(responseLocationAndItem.itemResponse.item.Sku==null){""}else{responseLocationAndItem.itemResponse.item.Sku!!},
                                                                LocationCode = "" + responseLocationAndItem.locationResponse.location.AbsEntry,
                                                                LocationName = responseLocationAndItem.locationResponse.location.BinCode,
                                                                Quantity = quantity.toDouble(),
                                                                Quality = quanlityVal
                                                            )
                                                        }
                                                    }
                                            }

                                            //onPress(listPayloadBody)
                                            if(objType==1701)
                                            {
                                                onPress(emptyList())
                                            }else {
                                                onPress(listPayloadBody)
                                            }


                                        }catch (e:Exception)
                                        {
                                            Log.e(
                                                "REOS",
                                                "BottomSheet-formHandheld-listPayloadBody-e: " + e.toString()
                                            )
                                        }




                                        Log.e(
                                            "JEPICAME",
                                            "=>se limpiara agregar antes " + responseLocationAndItem.itemResponse.item.ItemCode
                                        )
                                        responseLocationAndItem = ResponseLocationAndItem(
                                            locationResponse = LocationResponse(),
                                            itemResponse = ItemResponse(
                                                item = Items(ItemCode = "")
                                            )
                                        )

                                        Log.e(
                                            "JEPICAME",
                                            "=>se limpiara agregar dsp " + responseLocationAndItem.itemResponse.item.ItemCode
                                        )
                                    }
                                }
                            } else {
                                haveError = "La cantidad debe ser mayor o igual a 1.00 "
                                quantity = "1.0"
                            }

                        } catch (e: Exception) {
                            Log.e("Jepicame","=>"+ e.message )
                            Log.e("REOS","BottomSheet-formHandheld-error: "+e.toString())
                            haveError = "La cantidad ingresada no es valida"
                            quantity = "1.0"
                        }

                        optionAdd = false
                        optionCancell = false
                    } else {
                        optionCancell = false
                        optionAdd = true
                        haveError = ""
                    }
                }
            ) {
                Text(
                    text = textButtonAdd,
                    color = Color.White
                )
            }
            Button(onClick = {
                if (optionCancell) {
                    optionAdd = false
                    optionCancell = false

                    onClosePressed()

                    responseLocationAndItem = ResponseLocationAndItem()
                } else {
                    optionAdd = false
                    optionCancell = true
                    haveError = ""
                }
            }) {
                Text(
                    text = "Cancelar"
                )
            }
        }else{
            Text("La etiqueta de este SSCC debe ser destruida",color=Color.Red, textAlign = TextAlign.Center,modifier=Modifier.fillMaxWidth())
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
        CountryLocation("EC","ECUADOR"),
        CountryLocation("BO","BOLIVIA"),
        CountryLocation("RO","ROFALAB"),
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
            Log.e("REOS","BottomSheet-SelectWarehouseModal-error: "+warehouseValue.value.status)
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
                onClick = {
                    onClosePressed()
                },modifier = Modifier
                    .background(Color.Gray)
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(30.dp)
                ){
                    Icon(Icons.Filled.Close, tint = closeButtonColor, contentDescription = null)
                }
        }
        content()
    }
}



