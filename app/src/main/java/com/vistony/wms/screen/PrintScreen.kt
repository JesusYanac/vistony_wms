package com.vistony.wms.screen

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.vistony.wms.component.InputBox
import com.vistony.wms.component.TopBar
import com.vistony.wms.component.lockScreen
import com.vistony.wms.model.Print
import com.vistony.wms.model.PrintMachines
import com.vistony.wms.ui.theme.AzulVistony202
import com.vistony.wms.ui.theme.ColorDestine
import com.vistony.wms.viewmodel.PrintViewModel

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PrintScreen(navController: NavHostController, context: Context){

    val printViewModel: PrintViewModel = viewModel(
        factory = PrintViewModel.PrintViewModelFactory()
    )

    val statusPrint = printViewModel.statusPrint.collectAsState()

    Scaffold(
        topBar = {
            TopBar(title="Imprimir etiquetas")
        }
    ){
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
                    printViewModel.sendPrint(it)
                },
                onCancel = {
                    navController.navigateUp()
                }
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun divPrint(viewModel: PrintViewModel,onContinue:(Print)->Unit,onCancel:()->Unit){

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
            viewModel.setPrint(
                print=Print(printer=print.value.printer,itemCode = it, itemName = print.value.itemName, itemUom = print.value.itemUom , itemDate =print.value.itemDate,itemBatch = print.value.itemBatch,quantityString=print.value.quantityString)
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
                            .fillMaxSize()
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
