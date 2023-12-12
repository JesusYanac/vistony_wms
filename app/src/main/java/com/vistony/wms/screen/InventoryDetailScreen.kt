package com.vistony.wms.screen

import android.Manifest
import android.R.attr.*
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.util.Size
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.common.util.concurrent.ListenableFuture
import com.vistony.wms.component.*
import com.vistony.wms.num.TypeCode
import com.vistony.wms.num.TypeReadSKU
import com.vistony.wms.model.*
import com.vistony.wms.ui.theme.AzulVistony201
import com.vistony.wms.ui.theme.RedVistony202
import com.vistony.wms.util.BarCodeAnalyser
import com.vistony.wms.util.Routes
import com.vistony.wms.viewmodel.CountViewModel
import com.vistony.wms.viewmodel.ItemsViewModel
import com.vistony.wms.viewmodel.InventoryViewModel
import com.vistony.wms.viewmodel.WarehouseViewModel
import com.vistony.wms.viewmodel.ZebraViewModel
import org.bson.types.ObjectId
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(navController: NavHostController,whs:String,idInventory:String,status:String,defaultLocation:String,zebraViewModel: ZebraViewModel,typeInventory:String){

    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    var typeRead by remember { mutableStateOf(TypeReadSKU.HANDHELD) }

    val homeViewModel: CountViewModel = viewModel(
        factory = CountViewModel.CountViewModelFactory(idInventory)
    )

    val itemsViewModel: ItemsViewModel = viewModel(
        factory = ItemsViewModel.ArticleViewModelFactory("scan")
    )

    val warehouseViewModel: WarehouseViewModel = viewModel(
        factory = WarehouseViewModel.WarehouseViewModelFactory("","",0)
    )

    val homeValue = homeViewModel.counting.collectAsState()
    val dataObs = homeViewModel.data.collectAsState()
    val articleValue = itemsViewModel.article.collectAsState()
    val zebraValue = zebraViewModel.data.collectAsState()
    val warehouseValue = warehouseViewModel.location.collectAsState()

    val openDialog = remember { mutableStateOf(FlagDialog()) }
    var flagModal = remember { mutableStateOf(FlagDialog()) }

    Log.e(
        "REOS",
        "CustomDialog-CustomDialogVs2-zebraValue.value.Payload"+zebraValue.value.Payload
    )
    Log.e(
        "REOS",
        "CustomDialog-CustomDialogVs2-zebraValue.value.Type"+zebraValue.value.Type
    )
    if(zebraValue.value.Payload.isNotEmpty()){
        if(
            zebraValue.value.Payload.split("-").size==4
            && zebraValue.value.Type!="LABEL-TYPE-QRCODE"
            //&& zebraValue.value.Payload[0] =='B'
            //zebraValue.value.Payload.split("-").size>=2
           // &&
            //zebraValue.value.Payload[0]=='B'
        ){
            Log.e(
                "REOS",
                "CustomDialog-CustomDialogVs2-zebraValue.Entro-zebraValue.value.Payload[0]=='B'"
            )
            if(defaultLocation=="+"){
                Log.e(
                    "REOS",
                    "CustomDialog-CustomDialogVs2-zebraValue..Entro-zebraValue.value.Payload[0]=='B'-+"
                )
                Log.e(
                    "REOS",
                    "CustomDialog-CustomDialogVs2-zebraValue.value.Payload+"+zebraValue.value.Payload
                )
                Log.e(
                    "REOS",
                    "CustomDialog-CustomDialogVs2-zebraValue.value.whs+"+whs
                )
                warehouseViewModel.getLocations(zebraValue.value.Payload,whs, -1)

            }
        }else{

            itemsViewModel.getArticle(value=zebraValue.value.Payload,typeInventario=typeInventory, idHeader = idInventory)

        }
        zebraViewModel.setData(zebraPayload())
    }
    var titleMutable:MutableState<String> = remember {mutableStateOf("") }
    titleMutable.value="Conteo de inventario"
    Scaffold(
        topBar = {

            if(status=="Abierto"){
                TopBarTitleCamera(
                    title=titleMutable,
                    status= status,
                    objType=Routes.Inventory.value, //100
                    permission=cameraPermissionState,
                    onClick={
                        when(it){
                            TypeReadSKU.CERRAR_FICHA->{
                                openDialog.value=FlagDialog(status = true,flag="Close")
                            }
                            else->{
                                typeRead=it
                            }
                        }
                    },
                    navController,
                    "InventoryDetailScreen",
                    ""
                )
            }else{
                TopBar(title="Conteo de inventario")
            }

        }
    ){

        if(flagModal.value.status){
            lockMessageScreen(flagModal.value.flag,close={
                flagModal.value=FlagDialog()
            })
        }

       when(homeValue.value.statusEvent){
            ""->{}
            "cargando"->{
                lockScreen("Ejecutando...")
            }
            "ok"->{
                navController.navigateUp()
                homeViewModel.resetCountingState()
            }
            else->{
                Toast.makeText(context,homeValue.value.statusEvent, Toast.LENGTH_SHORT).show()
                homeViewModel.resetCountingState()
            }
        }

        when(articleValue.value.status){
            ""->{}
            "cargando"->{
                if(articleValue.value.type==TypeCode.QR){
                    CustomProgressDialog("Buscando articulo...")
                }else{
                    CustomProgressDialog("Buscando SSCC...")
                }
            }
            "locked"->{
                //este producto tiene la presentación bloqueada
                 flagModal.value = FlagDialog(true,"La presentación de este artículo esta bloqueado.")
                 itemsViewModel.resetArticleStatus()
            }
            "ok"->{
                val countings:List<Counting> = articleValue.value.items.map {

                    Counting(
                        sscc = articleValue.value.nameSscc,
                        interfaz = typeRead.toString(),
                        itemCode=it.item.ItemCode,
                        itemName=it.item.ItemName,
                        lote=it.lote,
                        location = if(dataObs.value.counting.isNotEmpty()){dataObs.value.counting[0].location}else{""},
                        quantity=if(dataObs.value.counting.isNotEmpty() && dataObs.value.counting[0].quantity>0.0 ){dataObs.value.counting[0].quantity}else{it.quantity}, //if(it.quantity>1.0){ it.quantity}else{dataObs.value.counting[0].quantity},
                        Realm_Id=it.expireDate
                    )
                }

                homeViewModel.writeData(body=
                    CustomCounting(
                        counting = countings,
                        typeCode = articleValue.value.type,
                        defaultLocationSSCC=articleValue.value.defaultLocation
                    )
                )

                itemsViewModel.resetArticleStatus()
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

        when(warehouseValue.value.status){
            ""->{}
            "cargando"->{
                CustomProgressDialog("Buscando ubicación...")
            }
            "ok"-> {
                var tempp: List<Counting> = emptyList()

                if(dataObs.value.counting.isNullOrEmpty()){
                    tempp= listOf(
                        Counting(
                            location = warehouseValue.value.location.BinCode
                        )
                    )
                }else{
                    tempp = dataObs.value.counting.map { counting ->

                        //dataObs.value.
                        Counting(
                            interfaz=typeRead.toString(),
                            sscc=counting.sscc,
                            itemCode = counting.itemCode,
                            itemName = counting.itemName,
                            lote = counting.lote,
                            quantity = counting.quantity,
                            location = warehouseValue.value.location.BinCode,
                            Realm_Id = counting.Realm_Id
                        )
                    }
                }

                homeViewModel.writeData(body=
                    CustomCounting(
                        counting = tempp,
                        typeCode = dataObs.value.typeCode, //articleValue.value.type,
                        defaultLocationSSCC = articleValue.value.defaultLocation
                    )
                )

                warehouseViewModel.resetLocationStatus()
            }
            "vacio"->{
                Toast.makeText(context, "El código escaneado no se encuentra en el maestro de ubicaciones", Toast.LENGTH_SHORT).show()
                warehouseViewModel.resetLocationStatus()
            }
            else->{
                Toast.makeText(context, warehouseValue.value.status, Toast.LENGTH_SHORT).show()
                warehouseViewModel.resetLocationStatus()
            }
        }

        if(dataObs.value.counting.isNotEmpty()){
            CustomDialogVs2(
                zebraViewModel=zebraViewModel,
                defaultLocation=defaultLocation,
                context=context,
                customCounting=dataObs.value,
                typeRead=typeRead,
                newValue = {
                    it.forEach { counting ->
                        if(counting.Realm_Id=="Y"){
                            homeViewModel.writeData(CustomCounting())
                        }else{
                            if(counting.quantity==0.0 && counting.location.isEmpty() && counting.itemName.isEmpty()){
                                homeViewModel.writeData(CustomCounting())
                            }else{
                                if(counting.itemName.isEmpty() && counting.location.isNotEmpty()){

                                    counting.quantity=0.0
                                    counting.itemCode="0000000"
                                    counting.itemName="UBICACIÓN VACIA"

                                    homeViewModel.insertData(counting)
                                    homeViewModel.writeData(CustomCounting())

                                }else{
                                    if(counting.quantity>0.0 && counting.location.isNotEmpty()){
                                        homeViewModel.insertData(counting)
                                        homeViewModel.writeData(CustomCounting())
                                    }
                                }
                            }

                            ///////////////////////
                        }
                    }
                }
            )
        }


        if(openDialog.value.status){
            CustomDialogResendOrClose(
                title="Conteo de inventario",
                flag=openDialog.value.flag,
                openDialog={ response ->
                    if(response){
                        if(openDialog.value.flag=="Close"){
                            homeViewModel.updateStatusClose()
                        }else if(openDialog.value.flag=="Resend"){
                            homeViewModel.resendToSap()
                        }
                    }
                    openDialog.value=FlagDialog(false)
                }
            )
        }

        divContainer(
            defaultLocation=defaultLocation,
            zebraViewModel=zebraViewModel,
            binLocation=if(dataObs.value.counting.isNullOrEmpty()){""}else{dataObs.value.counting[0].location},
           // binLocation=binLocationText,
            status=status,
            whs=whs,
            context = context,
            typeRead = typeRead,
            counting = homeValue.value,
            homeViewModel=homeViewModel,
            itemsViewModel=itemsViewModel,
            warehouseViewModel=warehouseViewModel
        )

    }
}

@Composable
private fun divContainer(defaultLocation:String, zebraViewModel:ZebraViewModel, binLocation:String, status:String, whs:String, context:Context, typeRead:TypeReadSKU, counting: CountingResponse=CountingResponse(), homeViewModel: CountViewModel, itemsViewModel: ItemsViewModel, warehouseViewModel:WarehouseViewModel){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier=Modifier.fillMaxSize()
    ){
        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> = ProcessCameraProvider.getInstance(context)
        val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

        Column(
            modifier = Modifier.padding(start=20.dp,top=20.dp,end=20.dp)
        ){
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier=Modifier.fillMaxWidth()
            ) {
                Text(text ="Nombre")
                Text(text = "${counting.nameInventory} ",color=Color.Gray)
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier=Modifier.fillMaxWidth()
            ) {
                Text(text ="Almacen")
                Text(text = "$whs ",color=Color.Gray)
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier=Modifier.fillMaxWidth()
            ) {
                Text(text ="Ubicación ")

                when(defaultLocation){
                    "-"->{
                        Text(text = "No controlada",color=Color.Gray)
                    }
                    "+"->{
                        Text(text = "Multiple",color=Color.Gray)
                    }
                    else->{
                        Text(text = "$defaultLocation",color=Color.Gray)
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier=Modifier.fillMaxWidth()
            ) {
                Text(text ="Estado")
                Text(text = "$status ",color=Color.Gray)
            }
        }

        if(status=="Abierto"){

            when(typeRead){
                TypeReadSKU.CAMERA->{
                    Log.e(
                        "REOS",
                        "InventoryDetailScreen-divContainer-TypeReadSKU.CAMERA"
                    )
                    CameraForm(
                        zebraViewModel=zebraViewModel,
                        context = context,
                        cameraProviderFuture=cameraProviderFuture,
                        cameraProvider=cameraProvider
                    )
                }
                TypeReadSKU.KEYBOARD-> {
                    Log.e(
                        "REOS",
                        "InventoryDetailScreen-divContainer-TypeReadSKU.KEYBOARD"
                    )
                    cameraProvider.unbindAll()

                    Divider()

                    formHandheld(
                        onPress={ payloadX ->
                            itemsViewModel.getArticle(payloadX)
                        }
                    )

                    Text(
                        text="Num. Artículos ${counting.counting.size}",
                        color= RedVistony202,
                        modifier= Modifier
                            .padding(bottom = 10.dp, end = 20.dp)
                            .align(Alignment.End)
                    )
                }
                TypeReadSKU.HANDHELD->{
                    Log.e(
                        "REOS",
                        "InventoryDetailScreen-divContainer-TypeReadSKU.HANDHELD"
                    )
                    Text(
                        text="Num. Artículos ${counting.counting.size}",
                        color= RedVistony202,
                        modifier= Modifier
                            .padding(end = 20.dp)
                            .align(Alignment.End)
                    )
                }
                else->{}
            }
        }else{
            Text(
                text="Num. Artículos ${counting.counting.size}",
                color= RedVistony202,
                modifier= Modifier
                    .padding(end = 20.dp)
                    .align(Alignment.End)
            )
        }

        Divider(modifier=Modifier.padding(top=10.dp))

        when(counting.status){
            ""->{}
            "cargando"->{
                CustomProgressDialog("Cargando...")
            }
            "ok-data"->{
                dataad(
                    //whs=whs,
                    warehouseViewModel=warehouseViewModel,
                    context=context,
                    zebraViewModel=zebraViewModel,
                    typeRead=typeRead,
                    binLocation=binLocation,
                    status=status,
                    listBody=counting.counting,
                    onChangeQuantity = { lineUpdate->
                       // xd=""
                        //homeViewModel.updateQuantity(lineUpdate)

                    },
                    onDeleteArticle={ idLine ->
                        homeViewModel.deleteData(idLine)
                    }
                )
            }
            "ok"->{
                homeViewModel.getData()
            }
            "vacio"->{
                Toast.makeText(context, "No hay registros que mostrar...", Toast.LENGTH_SHORT).show()
                homeViewModel.resetCountingState()
            }
            else->{
                Toast.makeText(context, "Ocurrio un error:\n ${counting.status}", Toast.LENGTH_SHORT).show()
                homeViewModel.resetCountingState()
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun formHandheld(
    onPress:(String)->Unit)
{
    var textCode by remember { mutableStateOf(TextFieldValue("")) }
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        enabled=true,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color.DarkGray,
            unfocusedBorderColor = Color.DarkGray,
            disabledTextColor = Color.DarkGray,
            disabledLabelColor = Color.DarkGray
        ),
        maxLines=1,
        singleLine = true,
        value = textCode,
        trailingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(start=20.dp,end=20.dp, bottom =20.dp,top=10.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
                onPress(textCode.text)
            }
        ),
        label = { Text(text = "Ingresar codigo del artículo") },
        placeholder = { Text(text = "") },
        onValueChange = {
            textCode = it
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun dataad(warehouseViewModel:WarehouseViewModel,zebraViewModel:ZebraViewModel,context:Context,typeRead:TypeReadSKU ,binLocation:String,status:String,listBody:List<Counting> = emptyList(),onChangeQuantity:(Counting)->Unit,onDeleteArticle:(ObjectId)->Unit){

    val openDialog = remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        itemsIndexed(listBody) { _,line ->

            Card(
                elevation = 8.dp,
                modifier= Modifier
                    .padding(bottom = 16.dp)
                    .combinedClickable(
                        onClick = { },
                        onLongClick = {
                            if (status == "Abierto") {
                                openDialog.value = line._id.toHexString()
                            }
                        }
                    )

            ){
                Column{
                    Row(
                        modifier = Modifier.fillMaxSize().background(if(line.itemCode=="0000000"){Color.Red.copy(0.5f)}else{Color.Unspecified}),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Column(
                            Modifier
                                .weight(0.75f)
                                .padding(20.dp)
                        ){
                            var colorLocation=if(line.location.isNullOrEmpty()){Color.Red}else{Color.Unspecified}
                            var colorLote=if(line.lote.isNullOrEmpty()){Color.Red}else{Color.Gray}

                            if(line.itemCode!="0000000"){
                                Text(
                                    text="Artículo ${line.itemCode} ",
                                    color=colorLocation
                                )

                                Text(
                                    text="${line.itemName} ",
                                    color=if(line.location.isNullOrEmpty()){Color.Red}else{Color.Gray},
                                    fontSize =13.sp
                                )
                            }

                            if(line.itemCode!="0000000"){
                                if(line.location!="NO CONTROLA UBICACIÓN"){
                                    Text(
                                        text=if(line.location.isNullOrEmpty()){"SIN UBICACIÓN"}else{"Ubicación ${line.location} "},
                                        color=colorLocation
                                    )
                                }
                            }else{
                                Text(
                                    text=if(line.location.isNullOrEmpty()){"SIN UBICACIÓN"}else{"Ubicación ${line.location} esta vacia "},
                                    color=colorLocation
                                )
                            }


                            if(line.itemCode!="0000000"){
                                Text(
                                    text=if(line.lote.isNullOrEmpty()){"SIN LOTE"}else{"Lote ${line.lote} "},
                                    color=colorLote
                                )
                            }
                        }

                        if(line.itemCode!="0000000") {
                            Column(
                                Modifier.weight(0.25f)
                            ) {
                                Stepper(
                                    warehouseViewModel = warehouseViewModel,
                                    context = context,
                                    typeRead = typeRead,
                                    zebraViewModel=zebraViewModel,
                                    binLocation = binLocation,
                                    itemName = line.itemName,
                                    status = (status == "Abierto"),
                                    location = line.location,
                                    count = line.quantity,
                                    //lote = line.lote,
                                    onCountChanged = {

                                        val lineUpdate = Counting()
                                        lineUpdate._id = line._id
                                        lineUpdate.quantity = it.count
                                        lineUpdate.location = it.locationName
                                        lineUpdate.inventoryId = line.inventoryId
                                       // lineUpdate.lote = it.lote

                                        onChangeQuantity(lineUpdate)
                                    }
                                )
                            }
                        }
                    }
                    if(line.sscc.isNotEmpty() ){
                        Text(text="SSCC ${line.sscc}",modifier=Modifier.padding(start=20.dp,bottom=20.dp))
                    }
                }
            }
        }
    }

    if(openDialog.value.isNotEmpty()){
        CustomDialogQuestion(openDialog={ response ->

            if(response){
                onDeleteArticle(ObjectId(openDialog.value))
            }

            openDialog.value=""
        })
    }
}

@Composable
private fun Stepper(context:Context,zebraViewModel:ZebraViewModel,warehouseViewModel: WarehouseViewModel,typeRead:TypeReadSKU ,binLocation:String,itemName:String,status:Boolean,location:String?,count: Double, onCountChanged: (UpdateLine) -> Unit) {
    var text by remember { mutableStateOf("$count") }
    var visible by remember { mutableStateOf(false) }

    if(visible){

        CustomDialogChangeNumber(
            context=context,
            warehouseViewModel=warehouseViewModel,
            typeRead=typeRead,
            zebraViewModel=zebraViewModel,
            binLocation=binLocation,
            itemName=itemName,
            location=location,
            value=text,
            //valueLote = textLote,
            newValue = {

                if(itemName=="UBICACIÓN VACIA"){
                    it.count=0.0
                    onCountChanged(it)
                }else{
                    if(it.count>=1.0){
                        onCountChanged(it)
                    }
                }

                visible=!visible
            }
        )
    }

    TextField(
        /*modifier=Modifier.clickable {
            if(status){
                visible=true
            }
        },*/
        enabled=false,
        value = text,
        onValueChange = { text = it }
    )

}

@Composable
fun CameraForm(zebraViewModel:ZebraViewModel,context:Context, cameraProviderFuture: ListenableFuture<ProcessCameraProvider>, cameraProvider: ProcessCameraProvider){

    var flash by remember { mutableStateOf(false) }

    Divider()

    Box{
        CameraPreview(
            flash=flash,
            cameraProviderFuture=cameraProviderFuture,
            cameraProvider=cameraProvider,
            context=context,
            valueText = { textDecode ->
                zebraViewModel.setData( zebraPayload(Payload=textDecode,Type="LABEL-TYPE-CAMERA"))
            }
        )

        Button(
            modifier=Modifier.padding(top=10.dp,start=10.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = AzulVistony201),
            onClick = {
                flash=!flash
            }
        ){
            Text(text = if(flash){"Apagar"}else{"Prender"},color=Color.White)
        }
    }
}

@Composable
private fun CameraPreview(
    flash:Boolean,
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    cameraProvider: ProcessCameraProvider,
    context:Context,
    valueText:(String)-> Unit) {

    val lifecycleOwner = LocalLifecycleOwner.current
    var preview by remember { mutableStateOf<Preview?>(null) }

    val barCodeVal = remember { mutableStateOf("") }

    AndroidView(
        factory = { AndroidViewContext ->

            PreviewView(AndroidViewContext).apply {
                this.scaleType = PreviewView.ScaleType.FILL_CENTER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        },
        modifier = Modifier
            .fillMaxWidth(1f)
            .fillMaxHeight(0.3f)
            .padding(20.dp).clipToBounds(),
        update = { previewView ->

            val cameraSelector: CameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()
            val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()


            cameraProviderFuture.addListener({
                preview = Preview.Builder()
                    .build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                val barcodeAnalyser = BarCodeAnalyser { barcodes ->
                    barcodes.forEach { barcode ->
                        barcode.rawValue?.let { barcodeValue ->
                            //VALUE SCAN|
                            valueText(barcodeValue)
                        }
                    }
                }

                val imageAnalysis: ImageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1024,720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, barcodeAnalyser)
                    }

                try {

                    cameraProvider.unbindAll()

                    val camera=cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )


                    val factory: MeteringPointFactory =SurfaceOrientedMeteringPointFactory(0.5f, 0.5f)
                    val point = factory.createPoint(x.toFloat(), y.toFloat())
                    val builder = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)

                    // auto calling cancelFocusAndMetering in 5 seconds
                    // auto calling cancelFocusAndMetering in 5 seconds

                    builder.setAutoCancelDuration(5, TimeUnit.SECONDS)
                    val action = builder.build()

                    camera.cameraControl.startFocusAndMetering(action)


                    ///////////////////////////////
                   if (camera.cameraInfo .hasFlashUnit()) {
                        camera.cameraControl.enableTorch(flash)
                   }

                } catch (e: Exception) {
                    Log.d("JEPICAME", "CameraPreview: ${e.localizedMessage}")
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}
