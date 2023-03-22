package com.vistony.wms.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.vistony.wms.R
import com.vistony.wms.component.*
import com.vistony.wms.num.TypeReadSKU
import com.vistony.wms.model.*
import com.vistony.wms.ui.theme.*
import com.vistony.wms.viewmodel.*
import kotlinx.coroutines.launch
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class
)
@Composable
fun MerchandiseDetailScreen(navController: NavHostController, context: Context,idMerchandise:String,status:String,zebraViewModel: ZebraViewModel,whsOrigin:String,whsDestine:String,objType:Int) {

    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    val stockTransferHeaderViewModel: StockTransferHeaderViewModel = viewModel(
        factory = StockTransferHeaderViewModel.StockTransferHeaderViewModelFactory(TaskManagement(ObjType=objType))
    )

    val stockTransferBodyViewModel: StockTransferBodyViewModel = viewModel(
        factory = StockTransferBodyViewModel.StockTransferBodyViewModelModelFactory(idMerchandise)
    )

    val stockTransferSubBodyViewModel: StockTransferSubBodyViewModel = viewModel(
        factory = StockTransferSubBodyViewModel.StockTransferSubBodyViewModelModelFactory()
    )

    val merchandiseBodyValue = stockTransferBodyViewModel.merchandiseBody.collectAsState()
    val stockTransferSubBodyValue = stockTransferSubBodyViewModel.stockTransferSubBody.collectAsState()
    val destineValue = stockTransferBodyViewModel.destine.collectAsState()

    val zebraValue = zebraViewModel.data.collectAsState()

    val modal = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden, confirmStateChange = {false})
    val scope = rememberCoroutineScope()

    var currentBottomSheet: BottomSheetScreen? by remember { mutableStateOf(null)}

    val closeSheet: () -> Unit = {
        scope.launch {
            modal.hide()
        }
    }

    when(destineValue.value){
        "cargando",
        ""->{}
        "ok"->{
            stockTransferBodyViewModel.resetDestineState()
        }
        else->{
            Toast.makeText(context, destineValue.value, Toast.LENGTH_SHORT).show()
            stockTransferBodyViewModel.resetDestineState()
        }
    }

    val openSheet: (BottomSheetScreen) -> Unit = {
        scope.launch {
            currentBottomSheet = it
            modal.animateTo(ModalBottomSheetValue.Expanded)
        }
    }

    if(zebraValue.value.Payload.isNotEmpty()){

        when(status){
            "Abierto"->{

                openSheet(
                    BottomSheetScreen.SelectOriginModal(
                        idHeader=idMerchandise,
                        whsOrigin=whsOrigin,
                        context=context,
                        value=zebraValue.value,
                        type= TypeReadSKU.HANDHELD,
                        objType=objType,
                        selected = {
                            stockTransferBodyViewModel.insertData(it,objType)
                            closeSheet()
                        }
                    )
                )

            }
            "OrigenCerrado"->{
                openSheet(
                    BottomSheetScreen.SelectDestineModal(
                        objType=objType,
                        value=zebraValue.value,
                        context=context,
                        stockTransferBodyViewModel=stockTransferBodyViewModel,
                        selected = {
                            stockTransferBodyViewModel.addDestine(it)
                            closeSheet()
                        },
                        wareHouseOrigin = whsOrigin,
                        wareHouseDestine = whsDestine
                    )
                )
            }
        }

        zebraViewModel.setData(zebraPayload())
    }

    ModalBottomSheetLayout(
        sheetState = modal,
        sheetContent = {
            Box(modifier = Modifier.defaultMinSize(minHeight = 1.dp)) {
                currentBottomSheet?.let { currentSheet ->
                    SheetLayout(currentSheet, closeSheet,showIconClose=true)
                }
            }
        }
    ){
        Scaffold(
            topBar = {

                if(status!="FichaCerrada"){
                    TopBarTitleCamera(
                        title=when(objType){ 67->{"Transferencia de stock"}671->{"Slotting"}22->{"Pedido de compra"}1701->{"Hoja de Alistado"}else->{"N/A"}},
                        status= status,
                        objType=objType,
                        permission=cameraPermissionState,
                        onClick={

                            if(it==TypeReadSKU.CERRAR_ORIGEN||it==TypeReadSKU.CERRAR_FICHA){

                                stockTransferHeaderViewModel.updateHeaderStatus(
                                    objType=objType,
                                    idInventory = ObjectId(idMerchandise),
                                    newStatus = if(it==TypeReadSKU.CERRAR_ORIGEN){"OrigenCerrado"}else{"FichaCerrada"}
                                )

                                //COMO TODO SE VA  AGESTIONAR DESDE LA VENTA TAREAS, TODO REGRESA A LA VENTA TAREAS
                                navController.navigate("TaskManager")

                                /*when(objType){
                                    67->{
                                        navController.navigate("Merchandise/objType=${objType}")
                                    }
                                    671->{
                                        navController.navigate("Slotting")
                                    }
                                    1701,/*->{
                                        var status=merchandiseBodyValue.value.stockTransferBody.forEach {
                                            it.subBody.filter { subBody ->
                                                subBody.Status!="Completo"
                                            }.single()
                                        }

                                        Log.e("JEPICAME","===>> ESTADO"+status)
                                    }*/
                                    22->{


                                        navController.navigate("TaskManager")
                                    }
                                    else->{

                                    }
                                }*/

                                /*{
                                    popUpTo(navController.context.toString()) {
                                        inclusive = true
                                    }
                                }*/
                                //stockTransferBodyViewModel.getData()
                            }else{

                                openSheet(
                                    BottomSheetScreen.SelectOriginModal(
                                        idHeader=idMerchandise,
                                        whsOrigin=whsOrigin,
                                        context=context,
                                        type=it,
                                        objType=objType,
                                        selected = {
                                            stockTransferBodyViewModel.insertData(it,objType)
                                            closeSheet()
                                        }
                                    )
                                )


                            }

                        }
                    )
                }else{
                    TopBar(
                        title=when(objType){ 67->{"Transferencia de stock"}671->{"Slotting"}22->{"Pedido de compra"}1701->{"Hoja de Alistado"}else->{"N/A"}},
                        firstColor = Color.DarkGray,
                        secondColor = Color.Gray
                    )
                }
            }
        ){

            divContainer(
                navController= navController,
                status=status,
                objType=objType,
                whsOrigin=whsOrigin,
                whsDestine=whsDestine,
                context = context,
                merchandiseBody = merchandiseBodyValue.value,
                stockTransferSubBodyRI=stockTransferSubBodyValue.value,
                stockTransferBodyViewModel=stockTransferBodyViewModel,
                stockTransferSubBodyViewModel=stockTransferSubBodyViewModel,
                onPressDestine = {

                    //SOLO PARA DOCUMENTO 22
                    openSheet(
                        BottomSheetScreen.SelectDestineModal(
                            objType=objType,
                            value= zebraPayload(Payload="${it.itemCode}|${it.batch}",Type="LABEL-TYPE-QRCODE"),
                            context=context,
                            stockTransferBodyViewModel=stockTransferBodyViewModel,
                            selected = { value->
                                stockTransferBodyViewModel.addDestine(value)
                                closeSheet()
                            },
                            wareHouseOrigin = whsOrigin,
                            wareHouseDestine = it.whsDestine
                        )
                    )

                }
            )

        }
    }
}

@Composable
private fun divContainer(navController: NavController,
     status:String, whsOrigin:String,whsDestine:String, context:Context,
     objType: Int,
     merchandiseBody:StockTransferBodyResponse = StockTransferBodyResponse(),
     stockTransferSubBodyRI: StockTransferSubBodyRI=StockTransferSubBodyRI(),
     stockTransferBodyViewModel: StockTransferBodyViewModel,
     stockTransferSubBodyViewModel: StockTransferSubBodyViewModel,
     onPressDestine: (DocumentLongPress) -> Unit
){

    when(stockTransferSubBodyRI.status){
        ""->{}
        "cargando"->{
            CustomProgressDialog("Cargando...")
        }
        "ok"->{
            stockTransferBodyViewModel.getBodyList()
        }
        "error"->{
            Toast.makeText(context, "Ocurrio un error al intentar eliminar\n${stockTransferSubBodyRI.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier= Modifier.fillMaxSize()
    ){

        val openDialog = remember { mutableStateOf("") }

        Column(
            modifier = Modifier.padding(start=20.dp,top=10.dp,end=20.dp)
        ){
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier= Modifier.fillMaxWidth()
            ) {
                when(objType){
                    22->{
                        Text(text ="$whsOrigin - ${whsDestine.replace("+"," ")}",color= Color.Gray, textAlign = TextAlign.Center)
                    }
                    1701->{
                        Text(text ="Almacén Origen")
                        Text(text = " ${merchandiseBody.wareHouseOrigin}",color= Color.Gray)
                    }
                    else->{
                        Text(text ="Almacén Orig > Dst")
                        Text(text = " ${merchandiseBody.wareHouseOrigin} > ${merchandiseBody.wareHouseDestine}",color= Color.Gray)
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier= Modifier.fillMaxWidth()
            ) {
                Text(text ="Fecha")
                Text(text = " ${merchandiseBody.createAt}",color= Color.Gray)
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier= Modifier.fillMaxWidth()
            ) {
                Text(text ="Estado")
                Text(text = " ${merchandiseBody.trasnferenceStatus}",color= AzulVistony202)
            }
        }

        Text(
            text="Num. Artículos ${merchandiseBody.stockTransferBody.size}",
            color= RedVistony202,
            modifier= Modifier
                .padding(end = 20.dp)
                .align(Alignment.End)
        )

        TabRowDefaults.Divider(modifier= Modifier.padding(top=10.dp))

        if(openDialog.value.isNotEmpty()){
            CustomDialogQuestion(openDialog={ response ->

                if(response){
                    stockTransferSubBodyViewModel.delete(ObjectId(openDialog.value))
                }

                openDialog.value=""
            })
        }

        when(merchandiseBody.status){
            ""->{}
            "cargando"->{
                CustomProgressDialog("Cargando...")
            }
            "ok-data"->{
                dataad(
                    navController=navController,
                    stockTransferSubBodyViewModel=stockTransferSubBodyViewModel,
                    context=context,
                    objType=objType,
                    status=status,
                    listBody=merchandiseBody.stockTransferBody,
                    onPressBody = {
                        //Toast.makeText(context, "SE VA A EDITAR", Toast.LENGTH_SHORT).show()
                    },
                    onLongPressBody={
                        openDialog.value = it.toHexString()
                    },
                    onLongPressDestine = {
                        onPressDestine(it)
                    }
                )
            }
            "ok"->{
                stockTransferBodyViewModel.getBodyList()
            }
            "vacio"->{
                Toast.makeText(context, "No hay registros que mostrar...", Toast.LENGTH_SHORT).show()
                stockTransferBodyViewModel.resetBodyState()
            }
            else->{
                Toast.makeText(context, "Ocurrio un error:\n ${merchandiseBody.status}", Toast.LENGTH_SHORT).show()
                stockTransferBodyViewModel.resetBodyState()
            }
        }

    }
}

@Composable
private fun dataad(
    navController:NavController,
    stockTransferSubBodyViewModel:StockTransferSubBodyViewModel,
 //   warehouseViewModel:WarehouseViewModel,
    context:Context,
    objType:Int,
  //  typeRead:TypeReadSKU,
   // binLocation:String,
    status:String,
    listBody:List<StockTransferBodyAndSubBody> = emptyList(),
    onPressBody:(ObjectId)->Unit,
    onLongPressBody:(ObjectId)->Unit,
    onLongPressDestine:(DocumentLongPress)->Unit
){

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(10.dp)
    ) {
        itemsIndexed(listBody) { _,line ->

            ExpandableListItem(
                objType=objType,
                status=status,
                body=line.body,
                subBody=line.subBody,
                context = context,
                onPressBody={
                    if(status=="Abierto"){
                        onPressBody(it._id)
                    }else{
                        Log.e("JEPICAME","nvController=>"+navController.currentBackStackEntry?.destination?.route)
                        Log.e("JEPICAME","nvController=>"+navController.currentBackStackEntry?.destination?.navigatorName)
                        Log.e("JEPICAME","nvController=>"+navController.currentBackStackEntry?.destination?.displayName)
                        //Log.e("JEPICAME","nvController=>"+navController.currentBackStackEntry?.destination?.addDeepLink())

                        navController.navigate("StockTransferDestine/SubBody=${it._id.toHexString()}&Producto=${URLEncoder.encode(line.body.ItemName, StandardCharsets.UTF_8.toString())}&objType=${objType}")
                        /*{
                            popUpTo(navController.previousBackStackEntry?.destination?.route!!) { inclusive = true }
                        }*/



                    }
                },
                onLongPressBody={

                    it.whsDestine=line.body.Warehouse //ALMACEN DESTINO PARA OBJ 22
                    it.itemCode=line.body.ItemCode

                    if(status=="Abierto"){
                        onLongPressBody(it._id)
                    }else{
                        if(objType==22){
                            onLongPressDestine(it)
                        }else{
                            Toast.makeText(context, "La ficha no esta abierta para ejecutar esta opción", Toast.LENGTH_SHORT).show()
                        }
                    }

                }
            )
        }
    }




}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
private fun ExpandableListItem(
    objType: Int,
    status:String,
    body: StockTransferBody,
    subBody:List<StockTransferSubBody>,
    context: Context,
   onPressBody:(StockTransferSubBody) ->Unit,
   onLongPressBody:(DocumentLongPress) ->Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .background(AzulVistony1)) {


            HeaderBody(
                itemName=body.ItemName,
                itemCode=" Código ${body.ItemCode}",
                num= ""+subBody.size,
                numOf= ""+subBody.filter { it.Status=="Completo" }.size,
                objType=objType,
                total2=body.Quantity,
                count=""+body.TotalQuantity,
                status=status,
                context = context,
                onPressBody={
                    expanded = !expanded
                }
            )

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 300, easing = FastOutLinearInEasing)
                ),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 300, easing = FastOutLinearInEasing)
                )
            ) {
                Column{
                    Row{

                        when(objType){
                            22->{
                                if(status == "Abierto"){
                                    TableCell(text = "Lote", weight = .5f,title=true)
                                    TableCell(text = "Cantidad", weight = .5f,title=true)
                                }else{
                                    TableCell(text = "Lote", weight = .4f,title=true)
                                    TableCell(text = "Cantidad Ubicada", weight = .3f,title=true)
                                    TableCell(text = "Pendiente de Ubicar", weight = .3f,title=true)
                                }
                            }
                            else->{
                                if(status == "Abierto"){
                                    TableCell(text = "Ubicación", weight = .4f,title=true)
                                    TableCell(text = "Lote", weight = .3f,title=true)
                                    TableCell(text = "Cantidad", weight = .3f,title=true)
                                }else{
                                    TableCell(text = "Ubicación\nLote", weight = .4f,title=true)
                                    TableCell(text = "Cantidad Ubicada", weight = .3f,title=true)
                                    TableCell(text = "Pendiente de Ubicar", weight = .3f,title=true)
                                }
                            }

                        }

                    }

                    Divider(modifier = Modifier.height(1.dp))

                    subBody.forEach{
                        Column(modifier=Modifier.combinedClickable(
                            onClick = {
                                onPressBody(it)
                            },
                            onLongClick = {
                                onLongPressBody( DocumentLongPress(_id=it._id, batch = it.Batch))
                            })){
                            Row(
                                modifier=Modifier
                                    .fillMaxWidth()
                                    .background(AzulVistony2)
                                   /* .combinedClickable(
                                        onClick = {
                                            onPressBody(it)
                                        },
                                        onLongClick = {
                                            onLongPressBody( DocumentLongPress(_id=it._id, batch = it.Batch))
                                        })*/,
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ){

                                when(objType){
                                    22->{
                                        if(status=="Abierto"){
                                            TableCell(text = " ${it.Batch}", weight = .5f)
                                            TableCell(text = " ${it.Quantity} ", weight = .5f)
                                        }else{

                                            val pendiente=(it.Quantity) - (it.Destine.sum("Quantity").toDouble()).toDouble()
                                            var colores=Color.Unspecified

                                            if(pendiente > 0.00){
                                                colores=Color.Red
                                            }

                                            TableCell(text = " ${it.Batch}", weight = .4f,color = colores)
                                            TableCell(text = " ${BigDecimal(it.Destine.sum("Quantity").toString()).setScale(2, RoundingMode.HALF_UP)} de ${it.Quantity}", weight = .3f,color = colores)
                                            TableCell(text = " ${BigDecimal(pendiente).setScale(2, RoundingMode.HALF_UP)}", weight = .3f, color = colores)
                                        }
                                    }
                                    else->{
                                        if(status=="Abierto"){
                                            TableCell(text = " ${it.LocationName}", weight = .4f)
                                            TableCell(text = " ${it.Batch}", weight = .3f)
                                            TableCell(text = " ${it.Quantity} ", weight = .3f)
                                        }else{

                                            val pendiente=(it.Quantity) - (it.Destine.sum("Quantity").toDouble()).toDouble()
                                            var colores=Color.Unspecified

                                            if(pendiente > 0.00){
                                                colores=Color.Red
                                            }

                                            TableCell(text = " ${it.LocationName}\n${it.Batch}", weight = .4f,color = colores)
                                            TableCell(text = " ${BigDecimal(it.Destine.sum("Quantity").toString()).setScale(2, RoundingMode.HALF_UP)} de ${it.Quantity}", weight = .3f,color = colores)
                                            TableCell(text = " ${BigDecimal(pendiente).setScale(2, RoundingMode.HALF_UP)}", weight = .3f, color = colores)
                                        }
                                    }
                                }
                            }

                            if(it.Sscc!=null && it.Sscc!!.isNotEmpty() ){

                                val pendiente=(it.Quantity) - (it.Destine.sum("Quantity").toDouble()).toDouble()
                                var colores=Color.Unspecified

                                if(pendiente > 0.00){
                                    colores=Color.Red
                                }

                                Row(modifier=Modifier.fillMaxWidth().background(AzulVistony2)){
                                    if(status=="Abierto"){
                                        Text(text= "SSCC ${it.Sscc!!}", textDecoration = TextDecoration.Underline,modifier=Modifier.padding(start=7.dp))
                                    }else{
                                        Text(text= "SSCC ${it.Sscc!!}",color=colores, textDecoration = TextDecoration.Underline,modifier=Modifier.padding(start=7.dp))
                                    }
                                }
                            }
                        }

                    }
                    Text("",modifier=Modifier.fillMaxWidth().background(AzulVistony2))
                }

            }
        }
    }

}

@Composable
private fun RowScope.TableCell(
    text: String,
    weight: Float,
    title:Boolean=false,
    color:Color=Color.Unspecified
) {
    if(title){
        Text(
            text = text,
            modifier=
            Modifier
                .weight(weight)
                .padding(7.dp),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }else{
        Text(
            text = text,
            modifier= Modifier
                .weight(weight)
                .padding(start=7.dp,top=7.dp,end=7.dp),fontSize = 13.sp,
            color = color
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeaderBody(
    objType:Int=0,
    itemName: String,
    itemCode: String,
    num: String,
    numOf:String,
    count: String,
    total2:Double=0.0,
    status: String,
    context: Context,
    onPressBody:() ->Unit
) {
    Column(
        horizontalAlignment=Alignment.Start,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(10.dp)
            .clickable {
                onPressBody()
            }
    ){
        Row(modifier=Modifier.fillMaxWidth().combinedClickable(
            onClick = {},
            onLongClick = {
                val urlStr = "https://wms.vistony.pe/vs1.0/Article/Photo?Name="+itemCode
                try{
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(Uri.parse(urlStr),"application/pdf")
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    context.startActivity(intent)
                }catch(e:Exception){
                    Toast.makeText(context,"Ocurrio un error al abrir el adjunto\n${e.message}",Toast.LENGTH_SHORT).show()
                }
            })
        ){
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_insert_drive_file_24),
                contentDescription = null,
                modifier = Modifier
                    .padding(vertical = 5.dp)
                    .size(20.dp),
                tint = if(numOf==num){AzulVistony201}else{RedVistony201}
            )
            Text(text = " $itemName", fontWeight = FontWeight.Bold,fontSize = TextUnit.Unspecified ,modifier= Modifier
                .fillMaxHeight()
                .weight(.8f))
        }

        Row(modifier=Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween){
            Column(modifier=Modifier.weight(0.5f)){
                Text(text = "$itemCode ")
                Text(text = " Num. Detalle $num")
            }
            Column(modifier=Modifier.weight(0.5f), horizontalAlignment = Alignment.End){

                when(objType){
                    22->{
                        when(status){
                            "Abierto"->{
                                Text(text = "$count/$total2",color=Color.Blue)
                                Text(text = "Avance",color=Color.Blue)
                            }
                            "FichaCerrada"-> {
                                Text(text = "$count Cantidad")
                                Text(text = "$numOf/$num Avance",color=Color.Blue)
                            }"OrigenCerrado"-> {
                                Text(text = "$count Cantidad")
                                Text(text = "$numOf/$num Avance",color=Color.Blue)
                            }
                        }
                    }
                    1701->{
                        when(status){
                            "Abierto"->{
                                Text(text = "El estado ´Abierto´ no es valido",color=Color.Red)
                            }
                            "FichaCerrada"-> {
                                Text(text = "")
                                Text(text = "$numOf/$num Avance",color=Color.Blue)
                            }"OrigenCerrado"-> {
                                Text(text = "")
                                Text(text = "$numOf/$num Avance",color=Color.Blue)
                            }
                        }
                    }
                    else->{
                        when(status){
                            "Abierto"->{
                                Text(text = "$count ")
                                Text(text = "Cantidad ")
                            }
                            "FichaCerrada"-> {
                                Text(text = "$count Cantidad")
                                Text(text = "$numOf/$num Avance",color=Color.Blue)
                            }"OrigenCerrado"-> {
                                Text(text = "$count Cantidad")
                                Text(text = "$numOf/$num Avance",color=Color.Blue)
                            }
                        }
                    }
                }
            }
        }
    }
}
