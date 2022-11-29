package com.vistony.wms.screen

import android.Manifest
import android.R.attr.*
import android.content.Context
import android.util.Log
import android.util.Size
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import com.vistony.wms.enum_.CallFor
import com.vistony.wms.enum_.TypeReadSKU
import com.vistony.wms.model.Counting
import com.vistony.wms.model.CountingResponse
import com.vistony.wms.model.UpdateLine
import com.vistony.wms.ui.theme.AzulVistony201
import com.vistony.wms.ui.theme.RedVistony202
import com.vistony.wms.util.BarCodeAnalyser
import com.vistony.wms.viewmodel.ItemsViewModel
import com.vistony.wms.viewmodel.CountViewModel
import com.vistony.wms.viewmodel.WarehouseViewModel
import com.vistony.wms.viewmodel.ZebraViewModel
import org.bson.types.ObjectId
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(navController: NavHostController,whs:String,idInventory:String,status:String,zebraViewModel: ZebraViewModel){

    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    var typeRead by remember { mutableStateOf(TypeReadSKU.HANDHELD) }

    val homeViewModel: CountViewModel = viewModel(
        factory = CountViewModel.HomeViewModelFactory(idInventory)
    )

    val itemsViewModel: ItemsViewModel = viewModel(
        factory = ItemsViewModel.ArticleViewModelFactory("scan")
    )

    val warehouseViewModel: WarehouseViewModel = viewModel(
        factory = WarehouseViewModel.WarehouseViewModelFactory("")
    )

    val homeValue = homeViewModel.counting.collectAsState()
    val articleValue = itemsViewModel.article.collectAsState()
    val zebraValue = zebraViewModel.data.collectAsState()
    val warehouseValue = warehouseViewModel.location.collectAsState()

    var binLocationText by remember { mutableStateOf("") }

    if(zebraValue.value.isNotEmpty()){

        if(zebraValue.value.length>=2 && zebraValue.value[0]=='B'){
            warehouseViewModel.getLocations(zebraValue.value,whs)
        }else{
            binLocationText=""
            itemsViewModel.getArticle(zebraValue.value)
        }

        zebraViewModel.setData("")
    }else{
        binLocationText=""
    }

    Scaffold(
        topBar = {
            TopBarTitleCamera(
                title="Conteo de inventario",
                status= "Abierto"
                ,
                permission=cameraPermissionState,
                onClick={
                    typeRead=it
                }
            )
        }
    ){
        when(articleValue.value.status){
            ""->{}
            "cargando"->{
                CustomProgressDialog("Buscando articulo...")
            }
            "ok"->{
                val body:Counting= Counting()

                body.itemCode=articleValue.value.article.ItemCode
                body.itemName=articleValue.value.article.ItemName
                body.lote=articleValue.value.lote
                body.location = ""+Calendar.getInstance().time

                body.quantity=1.0

                homeViewModel.insertData(body)
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
            ""->{
                binLocationText=""
            }
            "cargando"->{
                CustomProgressDialog("Buscando ubicación...")
            }
            "ok"->{
                binLocationText=warehouseValue.value.location.BinCode
                //binLocationText=" "+Calendar.getInstance().time
                warehouseViewModel.resetLocationStatus()
            }
            "vacio"->{
                Toast.makeText(context, "El código escaneado no se encuentra en el maestro de ubicaciones", Toast.LENGTH_SHORT).show()
                warehouseViewModel.resetLocationStatus()
            }
            else->{
                Toast.makeText(context, "Ocurrio un error:\n ${warehouseValue.value.status}", Toast.LENGTH_SHORT).show()
                warehouseViewModel.resetLocationStatus()
            }
        }

        divContainer(
            binLocation=binLocationText,
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
private fun divContainer(binLocation:String, status:String, whs:String, context:Context, typeRead:TypeReadSKU, counting: CountingResponse=CountingResponse(), homeViewModel: CountViewModel, itemsViewModel: ItemsViewModel, warehouseViewModel:WarehouseViewModel){
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
        }

        if(status=="Abierto"){

            when(typeRead){
                TypeReadSKU.CAMERA->{
                    CameraForm(
                        whs=whs,
                        calledFor=CallFor.Article,
                        context = context,
                        itemsViewModel= itemsViewModel,
                        cameraProviderFuture=cameraProviderFuture,
                        cameraProvider=cameraProvider
                    )
                }
                TypeReadSKU.KEYBOARD-> {
                    cameraProvider.unbindAll()

                    Divider()

                    formHandheld(
                        onPress={
                            itemsViewModel.getArticle(it)
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
                    Text(
                        text="Num. Artículos ${counting.counting.size}",
                        color= RedVistony202,
                        modifier= Modifier
                            .padding(end = 20.dp)
                            .align(Alignment.End)
                    )
                }
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
                    whs=whs,
                    warehouseViewModel=warehouseViewModel,
                    context=context,
                    typeRead=typeRead,
                    binLocation=binLocation,
                    status=status,
                    listBody=counting.counting,
                    onChangeQuantity = { lineUpdate->
                       // xd=""
                        homeViewModel.updateQuantity(lineUpdate)

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
            .padding(20.dp),
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
private fun dataad(whs:String,warehouseViewModel:WarehouseViewModel,context:Context,typeRead:TypeReadSKU ,binLocation:String,status:String,listBody:List<Counting> = emptyList(),onChangeQuantity:(Counting)->Unit,onDeleteArticle:(ObjectId)->Unit){

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
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Column(
                        Modifier
                            .weight(0.75f)
                            .padding(20.dp)
                    ){
                        var colorLocation=if(line.location.isNullOrEmpty()){Color.Red}else{Color.Unspecified}
                        var colorLote=if(line.lote.isNullOrEmpty()){Color.Red}else{Color.Gray}

                        Text(
                            text="Artículo ${line.itemCode} ",
                            color=colorLocation
                        )
                        Text(
                            text="${line.itemName} ",
                            color=if(line.location.isNullOrEmpty()){Color.Red}else{Color.Gray},
                            fontSize =13.sp
                        )

                        Text(
                            text=if(line.location.isNullOrEmpty()){"SIN UBICACIÓN"}else{"Ubicación ${line.location} "},
                            color=colorLocation
                        )
                        Text(
                            text=if(line.lote.isNullOrEmpty()){"SIN LOTE"}else{"Lote ${line.lote} "},
                            color=colorLote
                        )
                    }

                    Column(
                        Modifier.weight(0.25f)
                    ){
                        Stepper(
                            whs=whs,
                            warehouseViewModel=warehouseViewModel,
                            context=context,
                            typeRead=typeRead,
                            binLocation=binLocation,
                            itemName=line.itemName,
                            status=(status=="Abierto"),
                            location=line.location,
                            count=line.quantity,
                            lote=line.lote,
                            onCountChanged={

                                val lineUpdate=Counting()
                                lineUpdate._id=line._id
                                lineUpdate.quantity = it.count
                                lineUpdate.location = it.locationName
                                lineUpdate.inventoryId = line.inventoryId
                                lineUpdate.lote=it.lote

                                onChangeQuantity(lineUpdate)
                            }
                        )
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
private fun Stepper(whs:String,context:Context,warehouseViewModel: WarehouseViewModel,typeRead:TypeReadSKU ,binLocation:String,itemName:String,status:Boolean,location:String?,count: Double,lote:String, onCountChanged: (UpdateLine) -> Unit) {
    var text by remember { mutableStateOf("$count") }
    var textLote by remember { mutableStateOf(lote) }
    var visible by remember { mutableStateOf(false) }

    if(visible){

        CustomDialogChangeNumber(
            whs=whs,
            context=context,
            warehouseViewModel=warehouseViewModel,
            typeRead=typeRead,
            binLocation=binLocation,
            itemName=itemName,
            location=location,
            value=text,
            valueLote = textLote,
            newValue = {

                if(it.count!=0.0){
                    onCountChanged(it)
                }

                visible=!visible
            }
        )
    }

    TextField(
        modifier=Modifier.clickable {
            if(status){
                visible=true
            }
        },
        enabled=false,
        value = text,
        onValueChange = { text = it }
    )

}

@Composable
fun CameraForm(whs:String,calledFor:CallFor, context:Context, warehouseViewModel: WarehouseViewModel= WarehouseViewModel(""), itemsViewModel:ItemsViewModel=ItemsViewModel(""), cameraProviderFuture: ListenableFuture<ProcessCameraProvider>, cameraProvider: ProcessCameraProvider){

    var flash by remember { mutableStateOf(false) }

    Divider()

    Box{
        CameraPreview(
            flash=flash,
            cameraProviderFuture=cameraProviderFuture,
            cameraProvider=cameraProvider,
            context=context,
            valueText = { textDecode ->

                Log.e("JEPICAME","==>"+textDecode)

                if(calledFor== CallFor.Article){
                    itemsViewModel.getArticle(textDecode)

                }else if(calledFor== CallFor.Location){
                    warehouseViewModel.getLocations(textDecode,whs)
                }
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
            .padding(20.dp),
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
