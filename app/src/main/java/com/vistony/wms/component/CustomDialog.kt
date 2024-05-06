package com.vistony.wms.component

import ButtonView
import android.content.Context
import android.util.Log
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.text.isDigitsOnly
import com.google.common.util.concurrent.ListenableFuture
import com.vistony.wms.R
import com.vistony.wms.model.Counting
import com.vistony.wms.model.CustomCounting
import com.vistony.wms.model.UpdateLine
import com.vistony.wms.num.TypeCode
import com.vistony.wms.num.TypeReadSKU
import com.vistony.wms.screen.CameraForm
import com.vistony.wms.ui.theme.AzulVistony202
import com.vistony.wms.ui.theme.RedVistony
import com.vistony.wms.viewmodel.ItemsViewModel
import com.vistony.wms.viewmodel.WarehouseViewModel
import com.vistony.wms.viewmodel.ZebraViewModel

@Composable
fun lockScreen(text: String){
    Popup(
        onDismissRequest = {},
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            excludeFromSystemGesture = true,
        )
    ){
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray.copy(0.9f))
        ) {
            Text(text, color = Color.White)
        }
    }
}

@Composable
fun lockMessageScreen(text: String,close:()->Unit){
    Popup(
        onDismissRequest = {},
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            excludeFromSystemGesture = true,
        )
    ){
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray.copy(0.9f))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally){
                Text(text, color = Color.White)
                Row(modifier=Modifier.padding(vertical = 2.dp),horizontalArrangement = Arrangement.Center){
                    Button(onClick = { close() }, colors = ButtonDefaults.textButtonColors(
                        backgroundColor = Color.Red
                    )) {
                        Text(" Ok ",color=Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomProgressDialog(text:String){
    Dialog(
        onDismissRequest = { false },
        DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(140.dp)
                .background(Color.White, shape = RoundedCornerShape(8.dp))
        ) {
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
                    text = text,
                    textAlign = Center,
                    color= AzulVistony202
                )
            }

        }
    }
}

@Composable
fun CustomDialogQuestion(openDialog:(Boolean)->Unit){

    AlertDialog(
        onDismissRequest = {
            openDialog(false)
        },
        title = {
            Text(text = "Eliminar línea")
        },
        text = {
            Text(text="¿Está seguro de que desea eliminar esta línea en el documento actual?")
        },
        confirmButton = {
            Button(
                onClick = { openDialog(false) }
            ){
                Text("Cancelar")
            }
        },
        dismissButton = {
            Button(
                onClick = { openDialog(true) },
                colors= ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("Eliminar",color=Color.White)
            }
        }
    )
}

open class FlagDialog(
    var status:Boolean=false,
    var flag:String=""
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CustomDialogVs2(
    zebraViewModel: ZebraViewModel,
    defaultLocation:String,
    context:Context,
    customCounting: CustomCounting,
    typeRead: TypeReadSKU,
    itemsViewModel: ItemsViewModel,
    newValue:(List<Counting>)->Unit
){
    Log.d("jesusdebug", "CustomDialogVs2 customCounting: ${customCounting.defaultLocationSSCC}")
    Log.d("jesusdebug", "CustomDialogVs2 defaultLocation: $defaultLocation")

    var locationTemp by remember { mutableStateOf( TextFieldValue(  if(customCounting.defaultLocationSSCC.isEmpty()){ if(customCounting.counting.isNotEmpty() && customCounting.counting[0].location.isNotEmpty()){ customCounting.counting[0].location }else{ "" }}else{customCounting.defaultLocationSSCC} ))}
    var textNumber by remember { mutableStateOf( customCounting.counting[0].quantity.toString()) }
    var textLote by remember { mutableStateOf( if(customCounting.counting.isNotEmpty() && customCounting.counting[0].lote.isNotEmpty()){ customCounting.counting[0].lote }else{ "" } ) }
    val keyboardController = LocalSoftwareKeyboardController.current


    val auxLocation: State<String> = itemsViewModel.nameLocation.collectAsState()

    if(customCounting.counting.isNotEmpty() && customCounting.counting[0].location.isNotEmpty()){
        if(customCounting.defaultLocationSSCC.isEmpty()){
            locationTemp=TextFieldValue(customCounting.counting[0].location)
        }else{
            locationTemp=TextFieldValue(customCounting.defaultLocationSSCC)
        }
    }

    if(customCounting.counting.isNotEmpty() && customCounting.counting[0].lote.isNotEmpty()){
        textLote=customCounting.counting[0].lote
    }

    Log.d("jesusdebug", "CustomDialogVs2 locationTemp: $locationTemp")
    Log.d("jesusdebug", "CustomDialogVs2 textNumber: $textNumber")
    Log.d("jesusdebug", "CustomDialogVs2 textLote: $textLote")
    AlertDialog(
        onDismissRequest = {
            locationTemp=TextFieldValue("")
            newValue(listOf(Counting(quantity=0.0, Realm_Id = "N")))
        },
        title = {
            Column{
                if(customCounting.typeCode ==TypeCode.SSCC){
                    Row{
                        Text(text = "${customCounting.typeCode.toString()} ${customCounting.counting[0].sscc }",color= AzulVistony202, textDecoration = TextDecoration.combine(
                            listOf(
                                TextDecoration.Underline
                            )
                        ))
                        Text("x${customCounting.counting.size}")
                    }
                    Text("")
                }else{
                    Text(text = "${customCounting.counting[0].itemName} ")
                }
            }
        },
        text = {

            Column{
                    val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> = ProcessCameraProvider.getInstance(context)
                    val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                    if(typeRead==TypeReadSKU.CAMERA && defaultLocation=="+"){
                        Log.e(
                            "jesusdebug",
                            "CustomDialog-CustomDialogVs2-TypeReadSKU.CAMERA"
                        )
                        CameraForm(
                            context = context,
                            zebraViewModel=zebraViewModel,
                            cameraProviderFuture = cameraProviderFuture,
                            cameraProvider = cameraProvider
                        )
                    }else{
                        cameraProvider.unbindAll()
                    }
                if(customCounting.typeCode==TypeCode.QR){
                    OutlinedTextField(
                        enabled= customCounting.typeCode== TypeCode.QR,
                        singleLine=true,
                        value = textNumber,
                        onValueChange = {
                            textNumber = it

                            },
                        placeholder = {
                            Text(text = "Ingresa una cantidad")
                        },
                        label = { Text("Cantidad")},
                        trailingIcon = { Icon(painter = painterResource(id = R.drawable.ic_baseline_numbers_24), contentDescription = null, tint = AzulVistony202) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,imeAction = ImeAction.Go ),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                Log.e("jesusdebug",textNumber)
                                keyboardController?.hide()}
                        )
                    )
                    Log.e(
                        "REOS",
                        "CustomDialog-CustomDialogVs2-TypeCode.textNumber"+textNumber
                    )

                    OutlinedTextField(
                        enabled= typeRead== TypeReadSKU.KEYBOARD,
                        singleLine=true,
                        value = textLote,
                        onValueChange = { textLote = it },
                        placeholder = {
                            Text(text = "Ingresa el lote")
                        },
                        label = { Text("Lote")},
                        trailingIcon = { Icon(painter = painterResource(id = R.drawable.ic_baseline_box_24), contentDescription = null, tint = AzulVistony202) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text,imeAction = ImeAction.Go ),
                        keyboardActions = KeyboardActions(
                            onGo = {keyboardController?.hide()}
                        )
                    )
                }else{
                    Log.e("jesusdebug","CustomDialog-CustomDialogVs2-else")
                    LazyRow(modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.6f)) {
                        items(items = customCounting.counting, itemContent = { item ->
                            Card(
                                backgroundColor = Color.White,
                                elevation = 10.dp,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .fillMaxWidth()
                                    .padding(5.dp)
                            ){
                                Column(modifier=Modifier.padding(10.dp)){
                                    Text(
                                        item.itemCode +" "+item.itemName
                                    )
                                    Text(
                                        "Cantidad: " +item.quantity
                                    )
                                    Text(
                                        "Lote: " +item.lote
                                    )
                                    Text(
                                        "Fecha: "+item.Realm_Id
                                    )
                                }
                            }
                        })
                    }
                }

                Log.e("jesusdebug",defaultLocation)
                if(defaultLocation!="-" && defaultLocation!="+"){
                    locationTemp=TextFieldValue(defaultLocation)
                }
                Log.e("jesusdebug","defaultLocation"+defaultLocation)
                Log.d("jesusdebug", "CustomDialogVs2: hola mundo locationTemp: $locationTemp")
                if(customCounting.defaultLocationSSCC != "") {
                    itemsViewModel.getNameLocation(AbsEntry = customCounting.defaultLocationSSCC)
                }
                if(defaultLocation!="-"){
                    Log.e("jesusdebug","defaultLocation es diferente de -")
                    if(auxLocation.value==""){
                        itemsViewModel.setNameLocation(locationTemp.text)
                    }
                    OutlinedTextField(
                        enabled= typeRead== TypeReadSKU.KEYBOARD,
                        singleLine=true,
                        value = auxLocation.value,
                        placeholder = {
                            Text(text = "Ingresa una ubicación")
                        },
                        label = { Text("Ubicación")},
                        onValueChange = { itemsViewModel.setNameLocation(it) },
                        trailingIcon = { Icon(painter = painterResource(id = R.drawable.ic_baseline_rack_24), contentDescription = null, tint = AzulVistony202) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text,imeAction = ImeAction.Go ),
                        keyboardActions = KeyboardActions(
                            onGo = {keyboardController?.hide()}
                        )
                    )
                }else{
                    Log.e("jesusdebug","defaultLocation es igual a $defaultLocation")

                    locationTemp=TextFieldValue("NO CONTROLA UBICACIÓN")

                    Log.e("jesusdebug", "no controla ubicacion")
                    itemsViewModel.setNameLocation(locationTemp.text)
                }
            }
        },

        confirmButton = {
            Button(
                onClick = {
                    try{
                        Log.d("jesusdebug","CustomDialog-CustomDialogVs2-try confirmButton")
                       keyboardController?.hide()

                        val teasd:List<Counting> = customCounting.counting.map {

                            Log.e("jesusdebug","==LOTE>"+textLote+"xddd>>"+customCounting.counting.size +" <"+it.quantity +"> "+textNumber.toDouble())
                            Log.d("jesusdebug","CustomDialog-CustomDialogVs2-try confirmButton quantity"+it.quantity)
                            Log.d("jesusdebug","CustomDialog-CustomDialogVs2-try confirmButton location"+it.location)
                            Log.d("jesusdebug","CustomDialog-CustomDialogVs2-try confirmButton lote"+it.lote)
                            Log.d("jesusdebug","CustomDialog-CustomDialogVs2-try confirmButton itemCode"+it.itemCode)
                            Log.d("jesusdebug","CustomDialog-CustomDialogVs2-try confirmButton itemName"+it.itemName)
                            Log.d("jesusdebug","CustomDialog-CustomDialogVs2-try confirmButton sscc"+it.sscc)
                            Log.d("jesusdebug","CustomDialog-CustomDialogVs2-try confirmButton interfaz"+it.interfaz)


                            Counting(
                                quantity= if(customCounting.typeCode==TypeCode.QR){ textNumber.toDouble() }else{ it.quantity},
                                location = auxLocation.value,
                                lote= it.lote.ifEmpty { textLote },
                                itemCode=it.itemCode,
                                itemName = it.itemName,
                                sscc=it.sscc,
                                interfaz = if(it.interfaz.isEmpty() && it.itemCode.isEmpty()){ TypeReadSKU.KEYBOARD.toString() }else{it.interfaz}
                            )
                        }

                        newValue(teasd)
                    }catch(e:Exception){
                        textNumber="1"
                    }
                },
                colors= ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("Confirmar",color=Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    locationTemp=TextFieldValue("")
                    newValue(listOf( Counting(quantity = 0.0, Realm_Id = "Y")))
                }
            ) {
                Text("Cancelar")
            }
        }
    )
}


@Composable
fun CustomDialogResendOrClose(title:String,openDialog:(Boolean)->Unit,flag:String){

    AlertDialog(
        onDismissRequest = {
            openDialog(false)
        },
        title = {
            Text(text = if(flag=="Close"){title}else{"Reenviar a Sap"})
        },
        text = {
            Text(text=if(flag=="Close"){"¿Está seguro de cerrar esta ficha?"}else{"¿Está seguro de reenviar a Sap?"})
        },
        confirmButton = {
            Button(
                onClick = { openDialog(false) }
            ) {
                Text("Cancelar")
            }
        },
        dismissButton = {
            Button(
                onClick = { openDialog(true) },
                colors= ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text( if(flag=="Close"){"Cerrar Ficha"}else{"Reenviar a Sap"} ,color=Color.White)
            }
        }
    )
}

@Composable
fun CustomDialogCreateConteo(titulo:String,mensaje:String,openDialog:(Boolean)->Unit){

    AlertDialog(
        onDismissRequest = {
            openDialog(false)
        },
        title = {
            Text(text = titulo)
        },
        text = {
            Text(text=mensaje)
        },
        confirmButton = {
            Button(
                onClick = { openDialog(false) }
            ) {
                Text("Cancelar")
            }
        },
        dismissButton = {
            Button(
                onClick = { openDialog(true) },
                colors= ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("Crear",color=Color.White)
            }
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CustomDialogChangeNumber(context: Context, zebraViewModel: ZebraViewModel,warehouseViewModel: WarehouseViewModel, typeRead: TypeReadSKU, binLocation:String, itemName:String, location:String?, value:String, newValue:(UpdateLine)->Unit){

    var locationTemp by remember { mutableStateOf( TextFieldValue( if(location.isNullOrEmpty()){""}else{location} )) }
    var textNumber by remember { mutableStateOf(value) }
    val keyboardController = LocalSoftwareKeyboardController.current

    if(binLocation.isNotEmpty()){
        locationTemp=TextFieldValue(binLocation)
    }

    AlertDialog(
        onDismissRequest = {
            locationTemp=TextFieldValue("")
            newValue(UpdateLine(0.0,""))
        },
        title = {
            Column{
                Text(text = "$itemName ")
                Text(text = " ")
            }
        },
        text = {

            Column{
                val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> = ProcessCameraProvider.getInstance(context)
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                if(typeRead==TypeReadSKU.CAMERA){
                    CameraForm(
                        context = context,
                        zebraViewModel=zebraViewModel,
                        cameraProviderFuture = cameraProviderFuture,
                        cameraProvider = cameraProvider
                    )
                }else{
                    cameraProvider.unbindAll()
                }

                OutlinedTextField(
                    singleLine=true,
                    value = textNumber,
                    onValueChange = { textNumber = it },
                    placeholder = {
                        Text("Ingresar Cantidad")
                    },
                    trailingIcon = { Icon(painter = painterResource(id = R.drawable.ic_baseline_numbers_24), contentDescription = null, tint = AzulVistony202) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,imeAction = ImeAction.Go ),
                    keyboardActions = KeyboardActions(
                        onGo = {keyboardController?.hide()}
                    )
                )
                Text(text = " ")

                OutlinedTextField(
                    enabled=true,
                    singleLine=true,
                    value = locationTemp,
                    onValueChange = { locationTemp = it },
                    placeholder = {
                        Text("Ingresar Ubicación")
                    },
                    trailingIcon = { Icon(painter = painterResource(id = R.drawable.ic_baseline_rack_24), contentDescription = null, tint = AzulVistony202) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text,imeAction = ImeAction.Go ),
                    keyboardActions = KeyboardActions(
                        onGo = {keyboardController?.hide()}
                    )
                )

            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try{
                        keyboardController?.hide()
                        val numeric:Double=textNumber.toDouble()

                        newValue(UpdateLine(numeric,locationTemp.text))
                    }catch(e:Exception){
                        textNumber="1"
                    }
                },
                colors= ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("Confirmar",color=Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    locationTemp=TextFieldValue("")
                    newValue(UpdateLine(0.0,""))
                }
            ) {
                Text("Cancelar")
            }
        }
    )
}

/*
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CustomDialogChangeOnlyNumber(context: Context, warehouseViewModel: WarehouseViewModel,typeRead: TypeReadSKU, itemName:String,value:String,valueLote:String, newValue:(UpdateLineMerchandise)->Unit){

    var textNumber by remember { mutableStateOf(value) }
    var textLote by remember { mutableStateOf(valueLote) }
    val keyboardController = LocalSoftwareKeyboardController.current

    AlertDialog(
        onDismissRequest = {
            newValue(UpdateLineMerchandise(0.0,""))
        },
        title = {
            Column{
                Text(text = "$itemName ")
                Text(text = " ")
            }
        },
        text = {

            Column{
                val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> = ProcessCameraProvider.getInstance(context)
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                if(typeRead==TypeReadSKU.CAMERA){
                    CameraForm(
                        calledFor=CallFor.Location,
                        context = context,
                        warehouseViewModel = warehouseViewModel,
                        cameraProviderFuture = cameraProviderFuture,
                        cameraProvider = cameraProvider
                    )
                }else{
                    cameraProvider.unbindAll()
                }

                OutlinedTextField(
                    enabled= valueLote.isNullOrEmpty(),
                    singleLine=true,
                    value = textLote,
                    onValueChange = { textLote = it },
                    placeholder = {
                        Text("Ingresar Lote")
                    },
                    trailingIcon = { Icon(painter = painterResource(id = R.drawable.ic_baseline_box_24), contentDescription = null, tint = AzulVistony202) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,imeAction = ImeAction.Go ),
                    keyboardActions = KeyboardActions(
                        onGo = {keyboardController?.hide()}
                    )
                )

                Text(text = " ")

                OutlinedTextField(
                    singleLine=true,
                    value = textNumber,
                    onValueChange = { textNumber = it },
                    placeholder = {
                        Text("Ingresar Cantidad")
                    },
                    trailingIcon = { Icon(painter = painterResource(id = R.drawable.ic_baseline_numbers_24), contentDescription = null, tint = AzulVistony202) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,imeAction = ImeAction.Go ),
                    keyboardActions = KeyboardActions(
                        onGo = {keyboardController?.hide()}
                    )
                )

            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try{
                        keyboardController?.hide()
                        val numeric:Double=textNumber.toDouble()

                        newValue(UpdateLineMerchandise(numeric,"locationTemp",textLote))
                    }catch(e:Exception){
                        textNumber="1"
                    }
                },
                colors= ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("Confirmar",color=Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    newValue(UpdateLineMerchandise(0.0,""))
                }
            ) {
                Text("Cancelar")
            }
        }
    )
}
*/

@Composable
fun CustomDialogSignOut(onPress:(Boolean)->Unit){
    AlertDialog(
        onDismissRequest = {
            onPress(false)
        },
        title = {
            Text(text = "Cerrar sesión")
        },
        text = {
            Column{
                Text(text="¿Está seguro en cerrar sesión en este dispositivo?")
                Spacer(modifier=Modifier.height(10.dp))
                Text(text="Al \"continuar\" el sistema quitara toda su información de este dispositivo",color=Color.Gray)
            }
        },
        confirmButton = {
            Button(
                onClick = { onPress(false) }
            ) {
                Text("Cancelar")
            }
        },
        dismissButton = {
            Button(
                //enabled = false,
                onClick = {
                    onPress(true)
                },
                colors= ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("Continuar",color=Color.White)
            }
        }
    )
}

@Composable
fun CustomDialogLoading(
    showDialog: Boolean,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            buttons = { },
            title = { },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(30.dp),
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Loading...")
                }
            }
        )
    }
}


@Composable
fun DialogView(
    tittle: String,
    subtittle: String,
    onClickCancel: () -> Unit,
    onClickAccept: () -> Unit,
    statusButtonAccept: Boolean,
    statusButtonIcon: Boolean,
    context: Context,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onClickCancel
    ) {
        Box(
            modifier = Modifier
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colors.onPrimary,
                            shape = RoundedCornerShape(25.dp, 10.dp, 25.dp, 10.dp)
                        ),
                ) {
                    // Imagen que sobresale por la parte superior
                    // Contenido del diálogo
                    Column(
                        modifier = Modifier
                            .padding(16.dp, 16.dp, 16.dp, 16.dp)
                        //.fillMaxHeight(),
                        // verticalArrangement = Arrangement.Center
                    ) {
                        Row() {
                            TableCell(
                                text = tittle!!,
                                color = Color.Black,
                                title = true,
                                weight = 1f,
                                textAlign = TextAlign.Center
                            )
                        }
                        if(!subtittle.equals(""))
                        {
                            Spacer(modifier = Modifier.height(5.dp))
                            Row() {
                                TableCell(
                                    text = subtittle!!,
                                    color = Color.Gray,
                                    title = false,
                                    weight = 1f,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        content.invoke()
                        Spacer(modifier = Modifier.height(4.dp))
                        Row() {
                            ButtonView(
                                description = "Cerrar",
                                OnClick = onClickCancel,
                                status = true,
                                IconActive = statusButtonIcon,
                                context=context,
                                backGroundColor = RedVistony,
                                textColor = Color.White
                            )
                            if(statusButtonAccept)
                            {
                                Spacer(modifier = Modifier.width(10.dp))
                                ButtonView(
                                    description = "Aceptar",
                                    OnClick = onClickAccept,
                                    status = true,
                                    IconActive = statusButtonIcon,
                                    context=context,
                                    backGroundColor = RedVistony,
                                    textColor = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}