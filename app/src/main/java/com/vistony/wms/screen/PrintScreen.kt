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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
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
fun PrintQrScreen(
    navController: NavHostController,
    context: Context,
    zebraViewModel: ZebraViewModel
){
    val zebraValue = zebraViewModel.data.collectAsState()

    val printViewModel: PrintViewModel = viewModel(
        factory = PrintViewModel.PrintViewModelFactory()
    )


    val statusPrint = printViewModel.statusPrint.collectAsState()

    Scaffold(
        topBar = {
            TopBarWithBackPress(
                title="Imprimir rotulado QR",
                onButtonClicked = {
                    navController.navigateUp()
                }
            )

        }
    ){

        if (zebraValue.value.Payload.isNotEmpty()) {
            Log.d("jesusdebug", "Se escaneó: "+zebraValue.value.Payload)
        }
        when(statusPrint.value){
            ""->{}
            "cargando"->{
                lockScreen("Imprimiendo...")
            }
            "ok"->{
                Toast.makeText(context,"Imprimiendo...", Toast.LENGTH_LONG).show()
            }
            else->{
                Toast.makeText(context,statusPrint.value, Toast.LENGTH_LONG).show()
            }
        }

        Column(modifier=Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
            divPrint(
                viewModel = printViewModel,
                onContinue = {
                    Log.d("jesusdebug","onContinue")
                    printViewModel.sendPrint(it)
                },
                onCancel = {
                    navController.navigateUp()
                }
            )
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PrintSSccScreen(navController: NavHostController, context: Context,zebraViewModel:ZebraViewModel){

    val flagModal = remember { mutableStateOf(FlagDialog()) }

    val printViewModel: PrintViewModel = viewModel(
        factory = PrintViewModel.PrintViewModelFactory()
    )

    val itemsViewModel: ItemsViewModel = viewModel(
        factory = ItemsViewModel.ArticleViewModelFactory("scan")
    )

    val warehouseViewModel: WarehouseViewModel = viewModel(
        factory = WarehouseViewModel.WarehouseViewModelFactory("","",0)
    )

    val zebraValue = zebraViewModel.data.collectAsState()
    val articleValue = itemsViewModel.article.collectAsState()
    val locationValue = warehouseViewModel.location.collectAsState()

    if(zebraValue.value.Payload.isNotEmpty()){
        when(zebraValue.value.Type){
            "LABEL-TYPE-QRCODE"->{
                itemsViewModel.getArticle(value=zebraValue.value.Payload)
            }
            "LABEL-TYPE-CODE39"->{
                warehouseViewModel.verificationLocation(binCode = zebraValue.value.Payload, AbsEntry = "")
            }
            else->{
                Toast.makeText(context, "El rotulado escaneado no corresponde a un código QR", Toast.LENGTH_LONG).show()
            }
        }
        zebraViewModel.setData(zebraPayload())
    }

    Scaffold(
        topBar = {
            TopBar(title="Imprimir rotulado y crear SSCC")
        }
    ){

        statusPrinter(printViewModel,itemsViewModel,warehouseViewModel, onResponse = {
            flagModal.value=it
        })

        if(flagModal.value.status){
            lockMessageScreen(
                text=flagModal.value.flag,
                close={
                    flagModal.value= FlagDialog(false,"")
                }
            )
        }

        Log.e("JEPICAE","ME EJEUOT")

        when(articleValue.value.status){
            ""->{
                Column(
                    modifier=Modifier.padding(top=20.dp, bottom = 10.dp).fillMaxWidth().fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_qr_code_scanner_24),
                        contentDescription = "Favorite Icon",
                        modifier = Modifier.size(150.dp)
                    )

                    Text(
                        text = "ESCANEA UN QR",
                        color = Color.Gray,
                        modifier = Modifier.padding(top= 25.dp)
                    )
                }
            }
            "cargando"->{
                CustomProgressDialog("Buscando articulo...")
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
                        locationValue=locationValue.value,
                        printViewModel=printViewModel,
                        onContinue = {

                            printViewModel.sendPrintSSCC(
                                PrintSSCC(
                                    //ItemCode = it.itemCode,
                                    ItemCode = articleValue.value.items[0].item.ItemCode,
                                   // Batch = it.itemBatch,
                                    Batch = articleValue.value.items[0].lote,
                                    PrinterIP = it.printer.ip,
                                    PortNum = it.printer.port.toInt(),
                                    Warehouse = it.warehouse,
                                    BinCode = it.binCode,
                                    AbsEntry = it.absEntry,
                                    //Transfer = it.flagTransfer,
                                    Transfer = "N",
                                    QuantityPallet=it.quantityPallet
                                )
                            )
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
private fun statusPrinter(printViewModel: PrintViewModel,itemsViewModel:ItemsViewModel,warehouseViewModel:WarehouseViewModel,onResponse:(FlagDialog)->Unit){
    val statusPrint = printViewModel.statusPrint.collectAsState()

    when(statusPrint.value){
        ""->{}
        "cargando"->{
            lockScreen("Imprimiendo...")
        }
        "ok"->{
            itemsViewModel.resetArticleStatus()
            printViewModel.resetStatusPrint()
            printViewModel.resetItemStatus()
            warehouseViewModel.resetLocationStatus()

            onResponse(FlagDialog(true,"Rotulado impreso"))
        }
        else->{
            printViewModel.resetStatusPrint()
            onResponse(FlagDialog(true,statusPrint.value))
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DecimalNumberInput(quantity:Double?,onChange:(String)->Unit) {
    val decimalNumber = remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        enabled= true,
        singleLine=true,
        value = decimalNumber.value,
        onValueChange = {
            if (it.isEmpty() || it.matches(Regex("^[0-9]*\\.?[0-9]*$"))) {
                decimalNumber.value=it
                onChange(it)
            }
        },
        label = { Text(text = "Cantidad por pallet") },
        placeholder = { Text(text = if(quantity == null){"Ingresa la cantidad por pallet"}else{"La cantidad en SAP es $quantity"}) },
        trailingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = AzulVistony202) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,imeAction = ImeAction.Go ),
        keyboardActions = KeyboardActions(
            onGo = {
                keyboardController?.hide()
                focusManager.clearFocus()
            }
        )
    )
}

@Composable
private fun divPrintSSCC(
    articleValue:ItemsResponse,
    locationValue:LocationResponse,
    printViewModel:PrintViewModel
    ,onContinue:(Print)->Unit,
    onCancel:()->Unit
){

    val print = printViewModel.print.collectAsState()
    val checked = remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ){
        Text(text = " ${articleValue.items[0].item.ItemName}", color = Color.Gray, textAlign = TextAlign.Center)
    }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ){
        Text(text = "Código")
        Text(text = " ${articleValue.items[0].item.ItemCode}", color = Color.Gray)
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ){
        Text(text = "Lote")
        Text(text = " ${articleValue.items[0].lote }", color = Color.Gray)
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ){
        DecimalNumberInput(
            quantity=articleValue.items[0].item.QtyPallet,
            onChange={
                printViewModel.setPrint(
                    print=Print(
                        printer= print.value.printer,
                        itemCode = print.value.itemCode,
                        itemName = print.value.itemName,
                        itemUom = print.value.itemUom,
                        itemDate =print.value.itemDate,
                        itemBatch = print.value.itemBatch,
                        quantityString = print.value.quantityString,
                        warehouse = print.value.warehouse,
                        quantityPallet=if(it.isNotEmpty()){it.toDouble()}else{0.0},
                        binCode =  print.value.binCode,
                        absEntry =  print.value.absEntry,
                        flagTransfer=print.value.flagTransfer
                    )
                )
            }
        )
    }

    Divider()
    Text("")

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ){
        Text(text = "Almacén")
        if(locationValue.status=="ok"){
            Text(text = locationValue.location.Warehouse, fontWeight= FontWeight.Bold)
        }
    }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ){
        Text(text = "Ubicación")
        if(locationValue.status=="ok"){
            Text(text = locationValue.location.BinCode, fontWeight= FontWeight.Bold)
        }
    }

    if(locationValue.status=="ok"){
        printViewModel.setPrint(
            print=Print(
                printer= print.value.printer,
                itemCode = print.value.itemCode,
                itemName = print.value.itemName,
                itemUom = print.value.itemUom,
                itemDate =print.value.itemDate,
                itemBatch = print.value.itemBatch,
                quantityString =print.value.quantityString,
                quantityPallet=print.value.quantityPallet,
                warehouse = locationValue.location.Warehouse,
                binCode =  locationValue.location.BinCode,
                absEntry =  locationValue.location.AbsEntry,
                flagTransfer=print.value.flagTransfer
            )
        )
    }

    Text("")
    Text("Enviar a: ${print.value.printer.name}",fontWeight= FontWeight.Bold)
    Text("")

    listPrinterSection(
        viewModel = printViewModel,
        value=print.value.printer,
        onSelect = {
            printViewModel.setPrint(
                print=Print(
                    printer=PrintMachines(ip = it.ip, port = it.port,name=it.name),
                    itemCode = articleValue.items[0].item.ItemCode,
                    itemName = articleValue.items[0].item.ItemName,
                    itemUom = print.value.itemUom,
                    itemDate =print.value.itemDate,
                    itemBatch = articleValue.items[0].lote,
                    quantityString =print.value.quantityString,
                    quantityPallet=print.value.quantityPallet,
                    warehouse = print.value.warehouse,
                    binCode =  print.value.binCode,
                    absEntry = print.value.absEntry,
                    flagTransfer = print.value.flagTransfer
                )
            )
        }
    )

   /* Text("")

    LabelledCheckbox(
        checked = checked.value,
        onCheckedChange = {
            checked.value = it

            printViewModel.setPrint(
                print=Print(
                    printer = print.value.printer,
                    itemCode = print.value.itemCode,
                    itemName = print.value.itemName,
                    itemUom = print.value.itemUom,
                    itemDate = print.value.itemDate,
                    itemBatch = print.value.itemBatch,
                    quantityString = print.value.quantityString,
                    warehouse = locationValue.location.Warehouse,
                    binCode = locationValue.location.BinCode,
                    absEntry = locationValue.location.AbsEntry,
                    flagTransfer= if (it) { "Y" } else { "N" }
                )
            )
        },
        label = "Realizar transferencia"
    )*/

    Text("")

    var haveError by remember { mutableStateOf("") }

    if(haveError.isNotEmpty()){
        Text(haveError,color=Color.Red)
        Text("")
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ){
        Button(
            colors= ButtonDefaults.buttonColors(backgroundColor = Color.Gray),
            onClick = {
                if(print.value.warehouse.isNotEmpty() && print.value.absEntry!=0){
                    if(print.value.printer.ip.isNotEmpty()){
                        if(print.value.quantityPallet>0.00){
                            haveError=""
                            onContinue(print.value)
                        }else{
                            haveError="*Es necesario ingresar la cantidad por pallet"
                        }
                    }else{
                        haveError="*Es necesario seleccionar una impresora"
                    }
                }else{
                    haveError="*Es necesario escanear la ubicación donde se posicionará el palet"
                }

            }){
            Text(
                text = "Imprimir y Crear",
                color= Color.White
            )
        }
        Button( onClick = {
            haveError=""
            onCancel()
        }){
            Text(
                text = "Cancelar"
            )
        }
    }

    /*
    if(locationValue.status=="ok"){
        printViewModel.sendTerminationReport(
            print=Print(
                printer= print.value.printer,
                itemCode = print.value.itemCode,
                itemName = print.value.itemName,
                itemUom = print.value.itemUom,
                itemDate =print.value.itemDate,
                itemBatch = print.value.itemBatch,
                quantityString =print.value.quantityString,
                quantityPallet=print.value.quantityPallet,
                warehouse = locationValue.location.Warehouse,
                binCode =  locationValue.location.BinCode,
                absEntry =  locationValue.location.AbsEntry,
                flagTransfer=print.value.flagTransfer
            )
        )
    }*/
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun divPrint(viewModel: PrintViewModel,onContinue:(Print)->Unit,onCancel:()->Unit){
    Log.d("jesusdebug", "DivPrint")

    val print = viewModel.print.collectAsState()

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    InputBox(
        value=print.value.itemCode,
        label="Código del artículo",
        placeholder = "Ingresa el código a buscar",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text,imeAction = ImeAction.Search ),
        keyboardActions = KeyboardActions(
            onSearch = {
                viewModel.getArticle(print.value.itemCode)
                focusManager.clearFocus()
                keyboardController?.hide()
            }
        ),
        onChange = {
            Log.d("jesusdebug", "Se escaneo: "+it)
            val text: String = it.replace("*","")
            val itemCode = text.split("|")[0]
            val batch = text.split("|")[1]
            val name = text.split("|")[2]
            viewModel.setPrint(
                print=Print(printer=print.value.printer,itemCode = itemCode, itemName = name, itemUom = print.value.itemUom , itemDate =print.value.itemDate,itemBatch = batch,quantityString=print.value.quantityString)
            )
        }
    )

    Text("")

    InputBox(
        enabled= false,
        value=print.value.itemName,
        label="Nombre del artículo"
    )

    Text("")

    InputBox(
        value=print.value.itemBatch,
        label="Lote del artículo",
        placeholder = "Ingresa el lote",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text,imeAction = ImeAction.Next ),
        keyboardActions = KeyboardActions(
            onNext = {
               focusManager.moveFocus(FocusDirection.Down)
            }
        ),
        onChange = {
            viewModel.setPrint(
                print=Print(printer=print.value.printer,itemCode = print.value.itemCode, itemName = print.value.itemName, itemUom = print.value.itemUom , itemDate =print.value.itemDate,quantityString=print.value.quantityString,itemBatch = it)
            )
        }
    )

    Text("")

    InputBox(
        value=print.value.itemDate,
        label="Fecha del artículo",
        placeholder = "Ingresa fecha",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text,imeAction = ImeAction.Next ),
        keyboardActions = KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }
        ),
        onChange = {
            viewModel.setPrint(
                print=Print(printer=print.value.printer,itemCode = print.value.itemCode, itemName = print.value.itemName, itemUom = print.value.itemUom ,itemBatch = print.value.itemBatch,quantityString=print.value.quantityString, itemDate =it)
            )
        }
    )

    Text("")

    InputBox(
        value=""+print.value.quantityString,
        label="Cantidad a imprimir",
        placeholder = "Ingresa una cantidad",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword,imeAction = ImeAction.Next ),
        keyboardActions = KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }
        ),
        onChange = {
            viewModel.setPrint(
                print=Print(printer=print.value.printer,itemCode = print.value.itemCode, itemName = print.value.itemName, itemUom = print.value.itemUom ,itemBatch = print.value.itemBatch,itemDate=print.value.itemDate,quantityString =it)
            )
        }
    )

    Text("")

    InputBox(
        enabled= false,
        value=print.value.itemUom,
        label="Unidad de medida"
    )

    Text("")
    Text("Enviar a: ${print.value.printer.name}",fontWeight= FontWeight.Bold)
    Text("")

    listPrinterSection(
        viewModel = viewModel,
        value=print.value.printer,
        onSelect = {
            viewModel.setPrint(
                print=Print(printer=PrintMachines(ip = it.ip, port = it.port,name=it.name),itemCode = print.value.itemCode, itemName = print.value.itemName, itemUom = print.value.itemUom , itemDate =print.value.itemDate,itemBatch = print.value.itemBatch, quantityString =print.value.quantityString)
            )
        }
    )

    Text("")

    var haveError by remember { mutableStateOf("") }

    if(haveError.isNotEmpty()){
        Text(haveError,color=Color.Red)
        Text("")
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ){
        Button(
            colors= ButtonDefaults.buttonColors(backgroundColor = Color.Gray),
            onClick = {
                Log.d("jesusdebug", "Se mando a imprimir")
                var quantityPrint:Int=0

                try{
                    quantityPrint=print.value.quantityString.toInt()
                    print.value.quantity=quantityPrint

                    haveError=""
                    focusManager.clearFocus()
                    Log.d("jesusdebug", "Se mando a imprimir: "+print.value)
                    onContinue(
                        print.value
                    )

                }catch(e:Exception){
                    haveError="*La cantidad a imprimir no es valida"
                }
                
                if(print.value.itemCode.isNotEmpty()){
                    if(print.value.itemBatch.isNotEmpty()){
                        if(print.value.itemBatch.isNotEmpty()){
                            if(print.value.printer.ip.isNotEmpty()){

                                var quantityPrint:Int=0

                                try{
                                    quantityPrint=print.value.quantityString.toInt()
                                    print.value.quantity=quantityPrint

                                    haveError=""
                                    focusManager.clearFocus()
                                    onContinue(
                                        print.value
                                    )

                                }catch(e:Exception){
                                    haveError="*La cantidad a imprimir no es valida"
                                }
                            }else{
                                haveError="*Es necesario seleccionar una impresora"
                            }
                        }else{
                            haveError="*Es necesario ingresar la fecha a imprimir"
                        }
                    }else{
                        haveError="*Es necesario ingresar el lote a imprimir"
                    }
                }else{
                    haveError="*Es necesario ingresar el artículo a imprimir"
                }
            }){
            Text(
                text = "Imprimir",
                color= Color.White
            )
        }
        Button( onClick = {
            haveError=""
            focusManager.clearFocus()
            onCancel()
        }){
            Text(
                text = "Cancelar"
            )
        }
    }


}

@Composable
private fun listPrinterSection(viewModel: PrintViewModel,value:PrintMachines,onSelect:(PrintMachines)->Unit){

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
                                            name=line.name,
                                            ip=line.uIPAdress,
                                            port = line.uPort
                                        )
                                    )
                                }
                            )
                    ){
                        Box(modifier= Modifier.background(if(value.ip == line.uIPAdress && value.port == line.uPort){Color.LightGray}else{Color.Unspecified})){
                            Column{
                                Text(line.name,modifier= Modifier.padding(5.dp))
                                Text(line.uIPAdress,modifier= Modifier.padding(bottom = 5.dp,start=5.dp,end=5.dp),color=Color.Gray)
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
