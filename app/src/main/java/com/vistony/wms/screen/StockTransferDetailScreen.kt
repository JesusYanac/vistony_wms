package com.vistony.wms.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.BackHandler
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
import com.vistony.wms.util.ConvertdatefordateSAP2
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
fun MerchandiseDetailScreen(
    navController: NavHostController,
    context: Context, idMerchandise:String,
    status:String,
    zebraViewModel: ZebraViewModel,
    whsOrigin:String,
    wareHouseDestine:String,
    objType:Int,
    //DateAssignment:String

) {

    /*BackHandler(enabled = true,onBack={

        navController.navigate("TaskManager")
        Log.e("JEPCIAME","SOLO RETROCEDE Y QUEDA")
    })*/

    //val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    val stockTransferHeaderViewModel: StockTransferHeaderViewModel = viewModel(
        factory = StockTransferHeaderViewModel.StockTransferHeaderViewModelFactory(
            TaskManagement(
                ObjType = objType
            )
        )
    )
    val taskManagementViewModel: TaskManagementViewModel = viewModel(
        factory = TaskManagementViewModel.TaskManagementViewModelFactory()
    )

    val stockTransferBodyViewModel: StockTransferBodyViewModel = viewModel(
        factory = StockTransferBodyViewModel.StockTransferBodyViewModelModelFactory(idMerchandise)
    )
    val stockTransferHeaderValue = stockTransferHeaderViewModel.stockTransferHeaderResponse.collectAsState()
    val merchandiseBodyValue = stockTransferBodyViewModel.merchandiseBody.collectAsState()
    val destineValue = stockTransferBodyViewModel.destine.collectAsState()
    val zebraValue = zebraViewModel.data.collectAsState()
    val modal = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmStateChange = { false })
    val scope = rememberCoroutineScope()
    var currentBottomSheet: BottomSheetScreen? by remember { mutableStateOf(null) }
    val StatusScan = remember {
        mutableStateOf("")
    }
    val StatusOpenFormAddDestiny = remember {mutableStateOf("N") }
    val DateAssignmentTask = remember {mutableStateOf("") }
    val DateEndTask = remember {mutableStateOf("") }

    val closeSheet: () -> Unit = {
        scope.launch {
            Log.e("JEPICAME", "===> DEBERIA CERARSE EL MODAL")
            modal.hide()
            //StatusOpenFormAddDestiny.value="N"
        }
    }
    val commentReception = remember {mutableStateOf("") }
    val locationReception = remember {mutableStateOf("") }
    val wareHouseDestineMutable = remember {mutableStateOf("") }
    val taskManagementValue = taskManagementViewModel.taskUnit.collectAsState()
    val payloadMutable = remember {mutableStateOf("") }

    Log.e("REOS", "StockTransferDetailScreen-MerchandiseDetailScreen-taskManagementValue.value.data.size: "+taskManagementValue.value.data.size)
    Log.e("REOS", "StockTransferDetailScreen-MerchandiseDetailScreen-DateAssignmentTask.value: "+DateAssignmentTask.value)
    Log.e("REOS", "StockTransferDetailScreen-MerchandiseDetailScreen-DateEndTask.value: "+DateEndTask.value)
    if(objType==1250000001)
    {
        for(i in 0 until stockTransferHeaderValue.value.stockTransferHeader.size )
        {
            if(taskManagementValue.value.data.isNullOrEmpty())
            {
                Log.e("REOS", "StockTransferDetailScreen-MerchandiseDetailScreen-stockTransferHeaderValue.value.stockTransferHeader.get(i)._TaskManagement: "+stockTransferHeaderValue.value.stockTransferHeader.get(i)._TaskManagement)
                taskManagementViewModel.getTask(stockTransferHeaderValue.value.stockTransferHeader.get(i)._TaskManagement)

            }
        }

        if(DateAssignmentTask.value.isNullOrEmpty())
        {
            for(j in 0 until taskManagementValue.value.data.size)
            {
                DateAssignmentTask.value=taskManagementValue.value.data.get(j).Task.DateAssignment.toString()
                DateEndTask.value=taskManagementValue.value.data.get(j).Task.EndDate.toString()
            }
        }
    }






    when (destineValue.value) {
        "cargando",
        "" -> {
        }
        "ok" -> {
            stockTransferBodyViewModel.resetDestineState()
        }
        else -> {
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

    Log.e("REOS", "StockTransferDetailScreen-MerchandiseDetailScreen-StatusScan: "+StatusScan.value)
    Log.e("REOS", "StockTransferDetailScreen-MerchandiseDetailScreen-zebraValue.value.Payload: "+zebraValue.value.Payload)
    Log.e("REOS", "StockTransferDetailScreen-MerchandiseDetailScreen-whsOrigin: "+whsOrigin)
    Log.e("REOS", "StockTransferDetailScreen-MerchandiseDetailScreen-wareHouseDestine: "+wareHouseDestine)
    Log.e("REOS","StockTransferDetailScreen-MerchandiseDetailScreen-StatusOpenFormAddDestiny.value: "+StatusOpenFormAddDestiny.value)
    Log.e("REOS", "StockTransferDetailScreen-MerchandiseDetailScreen-zebraValue.value.Type: "+zebraValue.value.Type)


    if(!stockTransferHeaderValue.value.stockTransferHeader.isNullOrEmpty())
    {
        for(i in 0 until stockTransferHeaderValue.value.stockTransferHeader.size)
        {
            val elements = stockTransferHeaderValue.value.stockTransferHeader.get(i).Comment.split("|", limit = 2)
            for (j in 0 until elements.size) {
                when (j) {
                    0 -> {
                        commentReception.value = elements.get(j)
                    }
                    1 -> {
                        locationReception.value = elements.get(j)
                    }
                }
            }
        }
    }else  {
        stockTransferHeaderViewModel.getMerchandiseCode(idMerchandise,objType)
    }

/*
    var itemCodeHandheld: String = ""
    var loteHandheld: String = ""
    if(!zebraValue.value.Payload.isNullOrEmpty())
    {
        payloadMutable.value=zebraValue.value.Payload
        val elements = zebraValue.value.Payload.split("|", limit = 3)
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
    }
*/
    Log.e("REOS", "StockTransferDetailScreen-MerchandiseDetailScreen-payloadMutable.value: "+payloadMutable.value)
    Log.e("REOS","StockTransferDetailScreen-MerchandiseDetailScreen-status: "+status)
    Log.e("REOS","StockTransferDetailScreen-MerchandiseDetailScreen-TypeReadSKU: "+TypeReadSKU.values().toString())

    //Receive Response handheld and open Modal
    if (!zebraValue.value.Payload.isNullOrEmpty())
    {
        val elements = zebraValue.value.Payload.split("-", limit = 4)
        //if(itemCodeHandheld.length !=20) {
            when (objType) {
                67, 6701, 1250000001 -> {
                    //assess status Task


                        when (status) {
                            "Abierto" -> {
                                if(!(objType==67&&commentReception.value.equals("Recepción de Producción")&&elements.size==4))
                                {
                                    openSheet(
                                        BottomSheetScreen.SelectOriginModal(
                                            idHeader = idMerchandise,
                                            whsOrigin = whsOrigin,
                                            context = context,
                                            value = zebraValue.value,
                                            type = TypeReadSKU.HANDHELD,
                                            objType = objType,
                                            selected = {
                                                stockTransferBodyViewModel.insertData(it, objType)
                                                closeSheet()
                                            },
                                            StatusScan = StatusScan,
                                            merchandiseBody = merchandiseBodyValue.value,
                                            commentReception = commentReception.value,
                                            locationReception = locationReception.value,
                                        )
                                    )
                                }else {
                                    openSheet(
                                        BottomSheetScreen.SelectDestineModal(
                                            objType = objType,
                                            value = zebraValue.value,
                                            context = context,
                                            stockTransferBodyViewModel = stockTransferBodyViewModel,
                                            selected = {
                                                stockTransferBodyViewModel.addDestine(it, objType)
                                                closeSheet()
                                            },
                                            wareHouseOrigin = whsOrigin,
                                            wareHouseDestine = if (objType == 1250000001) {
                                                wareHouseDestineMutable.value
                                            } else {
                                                wareHouseDestine
                                            },
                                            StatusOpenFormAddDestiny = StatusOpenFormAddDestiny,
                                            payLoadMutable = payloadMutable,
                                            commentReception = commentReception.value,
                                            locationReception = locationReception.value,
                                        )
                                    )
                                }
                                //zebraViewModel.setData(zebraPayload(Payload = "", Type = ""))
                            }
                            "OrigenCerrado" -> {
                                openSheet(
                                    BottomSheetScreen.SelectDestineModal(
                                        objType = objType,
                                        value = zebraValue.value,
                                        context = context,
                                        stockTransferBodyViewModel = stockTransferBodyViewModel,
                                        selected = {
                                            stockTransferBodyViewModel.addDestine(it, objType)
                                            closeSheet()
                                        },
                                        wareHouseOrigin = whsOrigin,
                                        wareHouseDestine = if (objType == 1250000001) {
                                            wareHouseDestineMutable.value
                                        } else {
                                            wareHouseDestine
                                        },
                                        StatusOpenFormAddDestiny = StatusOpenFormAddDestiny,
                                        payLoadMutable = payloadMutable,
                                        commentReception = commentReception.value,
                                        locationReception = locationReception.value,
                                    )
                                )
                                //zebraViewModel.setData(zebraPayload(Payload = "", Type = ""))
                            }
                        }
                    zebraViewModel.setData(zebraPayload(Payload = "", Type = ""))
                }
                /*1701 -> {

                    //assess status Task
                    when (status) {
                        "OrigenCerrado" -> {
                            if(elements.size!=4)
                            {
                                openSheet(
                                    BottomSheetScreen.SelectOriginModal(
                                        idHeader = idMerchandise,
                                        whsOrigin = whsOrigin,
                                        context = context,
                                        value = zebraValue.value,
                                        type = TypeReadSKU.HANDHELD,
                                        objType = objType,
                                        selected = {
                                            stockTransferBodyViewModel.insertData(it, objType)
                                            closeSheet()
                                        },
                                        StatusScan = StatusScan,
                                        merchandiseBody = merchandiseBodyValue.value,
                                        commentReception = commentReception.value,
                                        locationReception = locationReception.value,
                                    )
                                )
                            }else {
                                openSheet(
                                    BottomSheetScreen.SelectDestineModal(
                                        objType = objType,
                                        value = zebraValue.value,
                                        context = context,
                                        stockTransferBodyViewModel = stockTransferBodyViewModel,
                                        selected = {
                                            stockTransferBodyViewModel.addDestine(it, objType)
                                            closeSheet()
                                        },
                                        wareHouseOrigin = whsOrigin,
                                        wareHouseDestine = if (objType == 1250000001) {
                                            wareHouseDestineMutable.value
                                        } else {
                                            wareHouseDestine
                                        },
                                        StatusOpenFormAddDestiny = StatusOpenFormAddDestiny,
                                        payLoadMutable = payloadMutable,
                                        commentReception = commentReception.value,
                                        locationReception = locationReception.value,
                                    )
                                )
                            }
                        }
                    }
                    zebraViewModel.setData(zebraPayload(Payload = "", Type = ""))
                }*/
                22,18,1701 -> {
                    //val elements = zebraValue.value.Payload.split("-", limit = 4)
                    //assess status Task
                    when (status) {
                        "Abierto" -> {
                            if(elements.size!=4)
                            {
                                openSheet(
                                    BottomSheetScreen.SelectOriginModal(
                                        idHeader = idMerchandise,
                                        whsOrigin = whsOrigin,
                                        context = context,
                                        value = zebraValue.value,
                                        type = TypeReadSKU.HANDHELD,
                                        objType = objType,
                                        selected = {
                                            stockTransferBodyViewModel.insertData(it, objType)
                                            closeSheet()
                                        },
                                        StatusScan = StatusScan,
                                        merchandiseBody = merchandiseBodyValue.value,
                                        commentReception = commentReception.value,
                                        locationReception = locationReception.value,
                                    )
                                )
                            }else {
                                openSheet(
                                    BottomSheetScreen.SelectDestineModal(
                                        objType = objType,
                                        value = zebraValue.value,
                                        context = context,
                                        stockTransferBodyViewModel = stockTransferBodyViewModel,
                                        selected = {
                                            stockTransferBodyViewModel.addDestine(it, objType)
                                            closeSheet()
                                        },
                                        wareHouseOrigin = whsOrigin,
                                        wareHouseDestine = if(objType==18){wareHouseDestineMutable.value}else{wareHouseDestine},
                                        StatusOpenFormAddDestiny = StatusOpenFormAddDestiny,
                                        payLoadMutable = payloadMutable,
                                        commentReception = commentReception.value,
                                        locationReception = locationReception.value,
                                    )
                                )
                            }
                        }
                        "OrigenCerrado" -> {
                            if(objType==1701)
                            {
                                if (elements.size != 4) {
                                    openSheet(
                                        BottomSheetScreen.SelectOriginModal(
                                            idHeader = idMerchandise,
                                            whsOrigin = whsOrigin,
                                            context = context,
                                            value = zebraValue.value,
                                            type = TypeReadSKU.HANDHELD,
                                            objType = objType,
                                            selected = {
                                                stockTransferBodyViewModel.insertData(it, objType)
                                                closeSheet()
                                            },
                                            StatusScan = StatusScan,
                                            merchandiseBody = merchandiseBodyValue.value,
                                            commentReception = commentReception.value,
                                            locationReception = locationReception.value,
                                        )
                                    )
                                } else {
                                    openSheet(
                                        BottomSheetScreen.SelectDestineModal(
                                            objType = objType,
                                            value = zebraValue.value,
                                            context = context,
                                            stockTransferBodyViewModel = stockTransferBodyViewModel,
                                            selected = {
                                                stockTransferBodyViewModel.addDestine(it, objType)
                                                closeSheet()
                                            },
                                            wareHouseOrigin = whsOrigin,
                                            wareHouseDestine = if (objType == 18) {
                                                wareHouseDestineMutable.value
                                            } else {
                                                wareHouseDestine
                                            },
                                            StatusOpenFormAddDestiny = StatusOpenFormAddDestiny,
                                            payLoadMutable = payloadMutable,
                                            commentReception = commentReception.value,
                                            locationReception = locationReception.value,
                                        )
                                    )
                                }
                            }
                        }
                    }
                    zebraViewModel.setData(zebraPayload(Payload = "", Type = ""))
                }

            }
        //}
        //zebraViewModel.setData(zebraPayload(Payload = "", Type = ""))
    }

    var titleMutable:MutableState<String> = remember {mutableStateOf("") }
    when(objType)
    {
        67->{if(commentReception.value.equals("Recepción de Producción")){titleMutable.value=commentReception.value}else{titleMutable.value="Transferencia de stock"}}
        6701->{
            titleMutable.value="Slotting"
        }
        22->{titleMutable.value="Pedido de compra"}
        1701->{titleMutable.value="Picking List"}
        18->{titleMutable.value="Factura de reserva"}
        1250000001->
        {titleMutable.value="Solicitud de Traslado"}
        234000031->{titleMutable.value="Logistica Inversa"}
        else->{titleMutable.value="N/A"}
    }

    //Charge tittle Modal Stock Transfer
    if(!commentReception.value.isNullOrEmpty())
    {
        if(commentReception.value.equals("Recepción de Producción")&&objType==67)
        {
            titleMutable.value=commentReception.value
        }
    }

    Log.e("REOS","StockTransferDetailScreen-MerchandiseDetailScreen-merchandiseBodyValue.value: "+merchandiseBodyValue.value)
    Log.e("REOS","StockTransferDetailScreen-MerchandiseDetailScreen-titleMutable.value: "+titleMutable.value)
    Log.e("REOS","StockTransferDetailScreen-MerchandiseDetailScreen-commentReception.value: "+commentReception.value)
    Log.e("REOS","StockTransferDetailScreen-MerchandiseDetailScreen-locationReception.value: "+locationReception.value)

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


                if(status !in  setOf("FichaCerrada","Cancelado")){
                    TopBarTitleCamera(
                        title=titleMutable,
                        status= status,
                        objType=objType,
                        permission=cameraPermissionState,
                        onClick={
                            var destinySourceEmpty:Int=0
                            var itemCode:String=""
                            var batch:String=""
                            var quantity:Double=0.0
                            var destinyGoalEmpty:Int=0
                            if(it==TypeReadSKU.CERRAR_ORIGEN||it==TypeReadSKU.CERRAR_FICHA||it==TypeReadSKU.CANCELAR_FICHA){
                                for(i in 0 until merchandiseBodyValue.value.stockTransferBody.size)
                                {
                                    Log.e("REOS","StockTransferDetailScreen-merchandiseBodyValue.value.stockTransferBody: "+merchandiseBodyValue.value.stockTransferBody.get(i).subBody.size)
                                    for(j in 0 until merchandiseBodyValue.value.stockTransferBody.get(i).subBody.size)
                                    {
                                        Log.e("REOS","StockTransferDetailScreen-merchandiseBodyValue.value.stockTransferBody.get(i).subBody.get(j).Batch: "+merchandiseBodyValue.value.stockTransferBody.get(i).subBody.get(j).Batch)
                                        Log.e("REOS","StockTransferDetailScreen-merchandiseBodyValue.value.stockTransferBody.get(i).subBody.get(j).Quantity: "+merchandiseBodyValue.value.stockTransferBody.get(i).subBody.get(j).Quantity)
                                        Log.e("REOS","StockTransferDetailScreen-merchandiseBodyValue.value.stockTransferBody.get(i).subBody.get(j).Destine.size: "+merchandiseBodyValue.value.stockTransferBody.get(i).subBody.get(j).Destine.size)
                                        if(
                                            merchandiseBodyValue.value.stockTransferBody.get(i).subBody.get(j).LocationName.toString().isNullOrEmpty()
                                            &&merchandiseBodyValue.value.stockTransferBody.get(i).subBody.get(j).LocationCode.toString().isNullOrEmpty()
                                        )
                                        {
                                            itemCode=merchandiseBodyValue.value.stockTransferBody.get(i).body.ItemCode
                                            batch=merchandiseBodyValue.value.stockTransferBody.get(i).subBody.get(j).Batch
                                            quantity=merchandiseBodyValue.value.stockTransferBody.get(i).subBody.get(j).Quantity
                                            destinySourceEmpty++
                                        }
                                        if(merchandiseBodyValue.value.stockTransferBody.get(i).subBody.get(j).Destine.size==0)
                                        {
                                            itemCode=merchandiseBodyValue.value.stockTransferBody.get(i).body.ItemCode
                                            batch=merchandiseBodyValue.value.stockTransferBody.get(i).subBody.get(j).Batch
                                            quantity=merchandiseBodyValue.value.stockTransferBody.get(i).subBody.get(j).Quantity
                                            destinyGoalEmpty++
                                        }
                                    }
                                }

                                if(objType in setOf(67,6701,1250000001,1701))
                                {
                                    if(it==TypeReadSKU.CERRAR_ORIGEN)
                                    {
                                        if(destinySourceEmpty==0)
                                        {
                                            stockTransferHeaderViewModel.updateHeaderStatus(
                                                objType=objType,
                                                idInventory = ObjectId(idMerchandise),
                                                newStatus = if(it==TypeReadSKU.CERRAR_ORIGEN){"OrigenCerrado"}else{"FichaCerrada"}
                                            )
                                            navController.navigate("TaskManager")
                                        }else {
                                            Toast.makeText(context, "Es necesario asignar una ubicación de origen a todas las lineas, para cerrar el origen, itemCode:"+itemCode+" batch: "+batch+" quantity: "+quantity , Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    else if(it==TypeReadSKU.CERRAR_FICHA)
                                    {
                                        if(commentReception.value.equals("Recepción de Producción")&&objType==67)
                                        {
                                            stockTransferHeaderViewModel.updateHeaderStatus(
                                                objType=objType,
                                                idInventory = ObjectId(idMerchandise),
                                                newStatus = if(it==TypeReadSKU.CERRAR_ORIGEN){"OrigenCerrado"}else{"FichaCerrada"}
                                            )
                                            navController.navigate("TaskManager")
                                        }else{
                                            if(destinyGoalEmpty==0)
                                            {
                                                stockTransferHeaderViewModel.updateHeaderStatus(
                                                    objType=objType,
                                                    idInventory = ObjectId(idMerchandise),
                                                    newStatus = if(it==TypeReadSKU.CERRAR_ORIGEN){"OrigenCerrado"}else{"FichaCerrada"}
                                                )
                                                navController.navigate("TaskManager")
                                            }else {
                                                Toast.makeText(context, "Es necesario asignar una ubicación de destino a todas las lineas , para cerrar el origen, itemCode:"+itemCode+" batch: "+batch+" quantity: "+quantity , Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }else if (it==TypeReadSKU.CANCELAR_FICHA)
                                    {
                                        stockTransferHeaderViewModel.updateHeaderStatus(
                                            objType=objType,
                                            idInventory = ObjectId(idMerchandise),
                                            newStatus = //if(it==TypeReadSKU.CERRAR_ORIGEN){"OrigenCerrado"}else{"FichaCerrada"}
                                            "Cancelado"
                                        )
                                        navController.navigate("TaskManager")
                                    }
                                }else {
                                    stockTransferHeaderViewModel.updateHeaderStatus(
                                        objType=objType,
                                        idInventory = ObjectId(idMerchandise),
                                        newStatus = if(it==TypeReadSKU.CERRAR_ORIGEN){"OrigenCerrado"}else{"FichaCerrada"}
                                    )
                                    navController.navigate("TaskManager")
                                }


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
                                        },
                                        StatusScan=StatusScan,
                                        merchandiseBody=merchandiseBodyValue.value,
                                        commentReception = commentReception.value,
                                        locationReception = locationReception.value,
                                    )
                                )
                            }

                        },
                        navController,
                        "StockTransferDetailScreen",
                        commentReception = commentReception.value
                    )
                }else{
                    TopBar(
                        title=when(objType){ 67->{"Transferencia de stock"}6701->{"Slotting"}22->{"Pedido de compra"}1701->{"Picking List"}18->{"Factura de reserva"}1250000001->{"Solicitud de Traslado"}234000031->{"Logistica Inversa"}else->{"N/A"}},
                        firstColor = Color.DarkGray,
                        secondColor = Color.Gray,
                        navController,
                        "StockTransferDetailScreen"
                    )
                }
            }
        ){

            /*Log.e("JEPICAME","==================>"+stockTransferSubBodyValue.value.status)
            when(stockTransferSubBodyValue.value.status){
                ""->{}
                "cargando"->{
                    CustomProgressDialog("Cargando...")
                }
                "ok"->{
                    //stockTransferBodyViewModel.getBodyList()
                }
                "error"->{
                    Toast.makeText(context, "Ocurrio un error al intentar eliminar\n${stockTransferSubBodyValue.value.message}", Toast.LENGTH_SHORT).show()
                    stockTransferBodyViewModel.resetBodyState()
                }
            }*/

            divContainer(
                navController= navController,
                status=status,
                objType=objType,
                whsOrigin=whsOrigin,
                whsDestine=wareHouseDestine,
                context = context,
                merchandiseBody = merchandiseBodyValue.value,
               // stockTransferSubBodyRI=stockTransferSubBodyValue.value,
                stockTransferBodyViewModel=stockTransferBodyViewModel,
                onPressDelete={
                    stockTransferBodyViewModel.delete(it)
                },
                onPressDestine = {
                    if(objType in setOf(22,1250000001,18))
                    {
                        wareHouseDestineMutable.value =it.whsDestine
                    }
                    //SOLO PARA DOCUMENTO 22
                    Log.e("REOS","StockTransferDetailScreen-MerchandiseDetailScreen-onPressDestine: ")
                    Log.e("REOS","StockTransferDetailScreen-MerchandiseDetailScreen-it.itemCode: "+it.itemCode)
                    Log.e("REOS","StockTransferDetailScreen-MerchandiseDetailScreen-it.batch: "+it.batch)
                    Log.e("REOS","StockTransferDetailScreen-MerchandiseDetailScreen-it.whsDestine: "+it.whsDestine)
                    Log.e("REOS","StockTransferDetailScreen-MerchandiseDetailScreen-payloadMutable.value: "+payloadMutable.value)
                    Log.e("REOS","StockTransferDetailScreen-MerchandiseDetailScreen-it.locationName: "+it.locationName)

                    openSheet(
                        BottomSheetScreen.SelectDestineModal(
                            objType=objType,
                            value=
                            if(objType in setOf(67,6701,1250000001,1701))
                            {
                                zebraPayload(Payload="${it.itemCode}|${it.batch}|${it.locationName}" ,Type="LABEL-TYPE-QRCODE")
                            }else {
                                zebraPayload(Payload="${it.itemCode}|${it.batch}" ,Type="LABEL-TYPE-QRCODE")
                            }
                            ,
                            //value=zebraValue.value,
                            context=context,
                            stockTransferBodyViewModel=stockTransferBodyViewModel,
                            selected = { value->
                                Log.e("REOS","StockTransferDetailScreen-MerchandiseDetailScreen-value: "+value)
                                stockTransferBodyViewModel.addDestine(value,objType)
                                closeSheet()
                                //StatusOpenFormAddDestiny.value="Y"

                            },
                            wareHouseOrigin = whsOrigin,
                            //wareHouseDestine = it.whsDestine,
                            wareHouseDestine = if(objType in setOf(22,1250000001,18))
                            {
                                it.whsDestine
                            } else {wareHouseDestine},
                            StatusOpenFormAddDestiny=StatusOpenFormAddDestiny,
                            payLoadMutable = payloadMutable,
                            commentReception = commentReception.value,
                            locationReception = locationReception.value,
                        )

                    )
                },
                commentReception = commentReception.value,
                locationReception = locationReception.value,
                DateAssignmentTask = DateAssignmentTask.value,
                DateEndTask = DateEndTask.value
            )
        }
    }
}

@Composable
private fun divContainer(navController: NavController,
     status:String, whsOrigin:String,whsDestine:String, context:Context,
     objType: Int,
     merchandiseBody:StockTransferBodyResponse = StockTransferBodyResponse(),
    // stockTransferSubBodyRI: StockTransferSubBodyRI=StockTransferSubBodyRI(),
    stockTransferBodyViewModel: StockTransferBodyViewModel,
    // stockTransferSubBodyViewModel: StockTransferSubBodyViewModel,
     onPressDestine: (DocumentLongPress) -> Unit,
     onPressDelete: (ObjectId) -> Unit,
     commentReception:String,
     locationReception:String,
     DateAssignmentTask:String,
     DateEndTask:String
){

    Log.e("JEPICAME","DSP DE ELIMNAR ENTRO AQUI")


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
                    22,18->{
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

            Log.e("REOD","StockTransferDetailScreen-divContainer-merchandiseBody.createAt: "+merchandiseBody.createAt)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier= Modifier.fillMaxWidth()
            ) {
                Text(text ="Fecha")
                Text(text =

                (if(objType==1250000001&&!DateEndTask.isNullOrEmpty()&&!DateEndTask.isNullOrEmpty())
                {
                    if(DateEndTask.isNullOrEmpty()){ConvertdatefordateSAP2(DateAssignmentTask)}else{ConvertdatefordateSAP2(DateEndTask)}
                }
                else{
                    " ${merchandiseBody.createAt}"})!!

                    ,color= Color.Gray)
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier= Modifier.fillMaxWidth()
            ) {
                Text(text ="Estado")
                Text(text = " ${merchandiseBody.trasnferenceStatus}",color= AzulVistony202)
            }
        }

        var countAdvance:Int=0

        for (i in 0 until merchandiseBody.stockTransferBody.size)
        {
            var quantityDestine:Double=0.0
            for (j in 0 until merchandiseBody.stockTransferBody.get(i).subBody.size)
            {
                Log.e("REOS","StockTransferDetailScreen-divContainer-merchandiseBody.stockTransferBody.get(i).subBody.get(j).Destine.size: "+merchandiseBody.stockTransferBody.get(i).subBody.get(j).Destine.size)
                for(k in 0 until merchandiseBody.stockTransferBody.get(i).subBody.get(j).Destine.size)
                {
                    Log.e("REOS","StockTransferDetailScreen-divContainer-merchandiseBody.stockTransferBody.get(i).subBody.get(j).Destine.get(k)?.Quantity!!.toDouble(): "+merchandiseBody.stockTransferBody.get(i).subBody.get(j).Destine.get(k)?.Quantity!!.toDouble())
                    quantityDestine=quantityDestine+ merchandiseBody.stockTransferBody.get(i).subBody.get(j).Destine.get(k)?.Quantity!!.toDouble()
                }
                Log.e("REOS","StockTransferDetailScreen-divContainer-quantityDestine: "+quantityDestine)
                Log.e("REOS","StockTransferDetailScreen-divContainer-merchandiseBody.stockTransferBody.get(i).subBody.get(j).Quantity: "+merchandiseBody.stockTransferBody.get(i).subBody.get(j).Quantity)
                Log.e("REOS","StockTransferDetailScreen-divContainer-merchandiseBody.stockTransferBody.get(i).body.Quantity: "+merchandiseBody.stockTransferBody.get(i).body.Quantity)
                if(quantityDestine!=0.0)
                {
                    if(merchandiseBody.stockTransferBody.get(i).subBody.get(j).Quantity ==quantityDestine)
                    {
                        countAdvance++
                    }
                }
            }
        }

        Log.e("REOS","StockTransferDetailScreen-divContainer-countAdvance: "+countAdvance)
        Text(
            text="Cant. Artículos ${merchandiseBody.stockTransferBody.size}",
            color= RedVistony202,
            modifier= Modifier
                .padding(end = 20.dp)
                .align(Alignment.End)
        )
        Text(
            text="Cant. Recepcionados ${countAdvance}",
            color= RedVistony202,
            modifier= Modifier
                .padding(end = 20.dp)
                .align(Alignment.End)
        )
        TabRowDefaults.Divider(modifier= Modifier.padding(top=10.dp))

        if(openDialog.value.isNotEmpty()){
            CustomDialogQuestion(openDialog={ response ->
                if(response){
                    onPressDelete(ObjectId(openDialog.value))
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
                    },
                    commentReception,
                    locationReception
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
 //   warehouseViewModel:WarehouseViewModel,
    context:Context,
    objType:Int,
  //  typeRead:TypeReadSKU,
   // binLocation:String,
    status:String,
    listBody:List<StockTransferBodyAndSubBody> = emptyList(),
    onPressBody:(ObjectId)->Unit,
    onLongPressBody:(ObjectId)->Unit,
    onLongPressDestine:(DocumentLongPress)->Unit,
    commentReception:String,
    locationReception:String
){
    Log.e("REOS","StockTransferDetailScreen-dataad-listBody: "+listBody.toString())
    Log.e("REOS","StockTransferDetailScreen-dataad-commentReception: "+commentReception)
    Log.e("REOS","StockTransferDetailScreen-dataad-locationReception: "+locationReception)
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(10.dp)
    ) {
        itemsIndexed(listBody) { _,line ->

                    Log.e("REOS","StockTransferDetailScreen-dataad-line.body.Warehouse: "+line.body.Warehouse)
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

                        //navController.navigate("StockTransferDestine/SubBody=${it._id.toHexString()}&Producto=${URLEncoder.encode(line.body.ItemName, StandardCharsets.UTF_8.toString())}&objType=${objType}")
                        /*{
                            popUpTo(navController.previousBackStackEntry?.destination?.route!!) { inclusive = true }
                        }*/

                    }
                },
                onLongPressBody={

                    it.whsDestine=line.body.Warehouse //ALMACEN DESTINO PARA OBJ 22
                    it.itemCode=line.body.ItemCode
                    //it.locationName=line.body.
                    Log.e("REOS","StockTransferDetailScreen-dataad-line.body.Warehouse: "+line.body.Warehouse)
                    Log.e("REOS","StockTransferDetailScreen-dataad-line.body.ItemCode: "+line.body.ItemCode)
                    Log.e("REOS","StockTransferDetailScreen-dataad-it: "+it.locationName)
                    when(status)
                    {
                        "Abierto"->
                        {
                            if(objType in setOf(67,6701,1250000001,1701,234000031))
                            {
                                if(!(objType==67&&commentReception.equals("Recepción de Producción")))
                                {
                                    Toast.makeText(context, "El estado debe ser OrigenCerrado, para asignar un destino", Toast.LENGTH_SHORT).show()
                                }
                                else {
                                    onLongPressDestine(it)
                                }
                            }else {
                                onLongPressDestine(it)
                            }
                        }
                        "OrigenCerrado"->
                        {
                            onLongPressDestine(it)
                        }
                        "FichaCerrada"->
                        {
                            Toast.makeText(context, "El estado FichaCerrada, no puede asignar un destino", Toast.LENGTH_SHORT).show()
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

    Log.e("REOS","StockTransferDetailScreen-ExpandableListItem-body: "+body)
    Log.e("REOS","StockTransferDetailScreen-ExpandableListItem-subBody: "+subBody.toString())
    Log.e("REOS","StockTransferDetailScreen-ExpandableListItem-subBody: "+subBody)




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
                sku=body.Sku,
                num= ""+subBody.size,
                numOf= ""+subBody.filter { it.Status=="Completo" }.size,
                objType=objType,
                total2=body.Quantity,
                count=""+body.TotalQuantity,
                //count=""+body.Quantity,
                status=status,
                context = context,
                onPressBody={
                    expanded = !expanded
                },
                subBody = subBody
            )

            if(objType in setOf(67,6701,1250000001))
            {
                var lastLocation:String=""
                var locationSource:String=""
                var locationDestine:String=""
                for (i in 0 until subBody.size)
                {
                    Log.e("REOS","StockTransferDetailScreen-ExpandableListItem-subBody.get(i).Destine.size: "+subBody.get(i).Destine.size)
                    for (j in 0 until subBody.get(i).Destine.size )
                    {
                        Log.e("REOS","StockTransferDetailScreen-ExpandableListItem-subBody.get(i).Destine.get(j)?.LocationName.toString(): "+subBody.get(i).Destine.get(j)?.LocationName.toString())
                        lastLocation=subBody.get(i).Destine.get(j)?.LocationName.toString()
                        if(j==0)
                        {
                            locationSource=subBody.get(i).Destine.get(j)?.LocationName.toString()
                        }
                        else if(j==1)
                        {
                            locationDestine=subBody.get(i).Destine.get(j)?.LocationName.toString()
                        }
                    }
                }
            }

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
                            22,18,1701//,1250000001,67
                            ->{
                                if(objType==1701)
                                {
                                    TableCell(text = "Ubic.Sugerida -\nUbic.Confirmada", weight = .3f,title=true)
                                    TableCell(text = "Lote", weight = .3f,title=true)
                                    TableCell(text = "Cantidad Ubicada", weight = .3f,title=true,textAlign = TextAlign.End)
                                    // TableCell(text = "Pendiente de Ubicar", weight = .3f,title=true)
                                }else {
                                    TableCell(text = "Ubicación", weight = .4f,title=true)
                                    TableCell(text = "Lote", weight = .3f,title=true)
                                    TableCell(text = "Cantidad", weight = .3f,title=true, textAlign = TextAlign.End)
                                }


                            }
                            else->{
                                if(status == "Abierto"){
                                    if(objType in setOf(67,6701,1250000001))
                                    {
                                        /*TableCell(text = "Origen -> Destino", weight = .4f,title=true)
                                        TableCell(text = "Lote", weight = .3f,title=true)
                                        TableCell(text = "Cantidad", weight = .3f,title=true,textAlign = TextAlign.End)*/
                                        TableCell(text = "Ubicación\nLote", weight = .4f,title=true)
                                        TableCell(text = "Cantidad Ubicada", weight = .3f,title=true,textAlign = TextAlign.End)
                                        TableCell(text = "Pendiente de Ubicar", weight = .3f,title=true, textAlign = TextAlign.End)
                                    }else {
                                        TableCell(text = "Ubicación", weight = .4f,title=true)
                                        TableCell(text = "Lote", weight = .3f,title=true)
                                        TableCell(text = "Cantidad", weight = .3f,title=true,textAlign = TextAlign.End)
                                    }

                                }
                                else if(status == "FichaCerrada"){
                                    if(objType in setOf(67,6701,1250000001))
                                    {
                                        TableCell(text = "Ubic.Origen -\nUbic.Destino", weight = .3f,title=true)
                                        TableCell(text = "Lote", weight = .3f,title=true)
                                        TableCell(text = "Cantidad Ubicada", weight = .3f,title=true,textAlign = TextAlign.End)
                                       // TableCell(text = "Pendiente de Ubicar", weight = .3f,title=true)
                                    }else {
                                        TableCell(text = "Ubicación", weight = .4f,title=true)
                                        TableCell(text = "Lote", weight = .3f,title=true)
                                        TableCell(text = "Cantidad", weight = .3f,title=true,textAlign = TextAlign.End)
                                    }
                                }
                                else{
                                    if(objType in setOf(67,6701,1250000001))
                                    {
                                        /*TableCell(text = "Origen -> Destino", weight = .4f,title=true)
                                        TableCell(text = "Lote", weight = .3f,title=true)
                                        TableCell(text = "Cantidad", weight = .3f,title=true,textAlign = TextAlign.End)*/
                                        TableCell(text = "Ubicación\nLote", weight = .4f,title=true)
                                        TableCell(text = "Cantidad Ubicada", weight = .3f,title=true,textAlign = TextAlign.End)
                                        TableCell(text = "Pendiente de Ubicar", weight = .3f,title=true, textAlign = TextAlign.End)
                                    }else {
                                        TableCell(text = "Ubicación\nLote", weight = .4f,title=true)
                                        TableCell(text = "Cantidad Ubicada", weight = .3f,title=true,textAlign = TextAlign.End)
                                        TableCell(text = "Pendiente de Ubicar", weight = .3f,title=true, textAlign = TextAlign.End)
                                    }

                                }
                            }

                        }

                    }


                    Divider(modifier = Modifier.height(1.dp))

                    subBody.forEach{
                        /*if(objType==67)
                        {*/
                            var locationName:String=""
                            var locationSource:String=""
                            var locationDestine:String=""

                        locationSource=it.LocationName
                        locationName=it.LocationName
                        Log.e("REOS","StockTransferDetailScreen-ExpandableListItem-onLongClick-it.LocationName: "+it.LocationName)
                            for(i in 0 until it.Destine.size)
                            {
                                //locationName= it.Destine.get(i)!!.LocationName.toString()
                                Log.e("REOS","StockTransferDetailScreen-ExpandableListItem-onLongClick-it.Destine.get(i)!!.LocationName.toString(): "+it.Destine.get(i)!!.LocationName.toString())
                                if(i==0)
                                {
                                    locationDestine=it.Destine.get(i)!!.LocationName.toString()
                                }
                                /*else if(i==1)
                                {
                                    locationDestine=it.Destine.get(i)!!.LocationName.toString()
                                }*/
                            }
                        //}
                        Log.e("REOS","StockTransferDetailScreen-ExpandableListItem-onLongClick-locationSource: "+locationSource)
                        Log.e("REOS","StockTransferDetailScreen-ExpandableListItem-onLongClick-locationDestine: "+locationDestine)

                        Column(modifier=Modifier.combinedClickable(
                            onClick = {
                                onPressBody(it)
                            },
                            onLongClick = {
                                Log.e("REOS","StockTransferDetailScreen-ExpandableListItem-onLongClick-it._id"+it._id)
                                Log.e("REOS","StockTransferDetailScreen-ExpandableListItem-onLongClick-it.Batch"+it.Batch)
                                Log.e("REOS","StockTransferDetailScreen-ExpandableListItem-onLongClick-it.Batch"+it.LocationName)
                                onLongPressBody( DocumentLongPress(_id=it._id, batch = it.Batch,locationName=it.LocationName))
                            })){
                            Row(
                                modifier=Modifier
                                    .fillMaxWidth()
                                    .background(AzulVistony2)
                                        /*.combinedClickable(
                                        onClick = {
                                            onPressBody(it)
                                        },
                                        onLongClick = {
                                            onLongPressBody( DocumentLongPress(_id=it._id, batch = it.Batch))
                                        })*/,
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ){
                                var colores=Color.Unspecified

                                when(objType){
                                    /*18//,1250000001,67
                                    -> {
                                        TableCell(text = " ${locationName}", weight = .4f)
                                        TableCell(text = " ${it.Batch}", weight = .3f)
                                        TableCell(text = " ${it.Quantity} ", weight = .3f, textAlign = TextAlign.End)
                                    }*/
                                    22,18,1701//,1250000001,67
                                    -> {
                                        if(objType==1701)
                                        {
                                            TableCell(text = "${it.LocationName} -\n${locationDestine}", weight = .3f,color = colores)
                                            //TableCell(text = locationDestine, weight = .3f,color = colores)
                                            TableCell(text = it.Batch, weight = .3f,color = colores)
                                            TableCell(text = " ${BigDecimal(it.Destine.sum("Quantity").toString()).setScale(2, RoundingMode.HALF_UP)} de ${it.Quantity}", weight = .3f,color = colores, textAlign = TextAlign.End)
                                            //TableCell(text = " ${BigDecimal(pendiente).setScale(2, RoundingMode.HALF_UP)}", weight = .3f, color = colores,textAlign = TextAlign.End)
                                        }else {
                                            TableCell(text = " ${locationDestine}", weight = .4f)
                                            TableCell(text = " ${it.Batch}", weight = .3f)
                                            TableCell(text = " ${it.Quantity} ", weight = .3f, textAlign = TextAlign.End)
                                        }

                                    }

                                    else->{
                                        if(status=="Abierto"){
                                            val pendiente=(it.Quantity) - (it.Destine.sum("Quantity").toDouble()).toDouble()

                                            if(pendiente > 0.00){
                                                colores=Color.Red
                                            }
                                            if(objType in setOf(67,6701,1250000001))
                                            {
                                                /*TableCell(text = locationSource+" -> "+locationDestine , weight = .4f,color = colores)
                                                TableCell(text = it.Batch, weight = .3f,color = colores)
                                                TableCell(text = it.Quantity.toString(), weight = .3f, color = colores, textAlign  = TextAlign.End )*/
                                                TableCell(text = " ${if(it.LocationName.isNullOrEmpty()){locationName} else {it.LocationName}}\n${it.Batch}", weight = .4f,color = colores)
                                                TableCell(text = " ${BigDecimal(it.Destine.sum("Quantity").toString()).setScale(2, RoundingMode.HALF_UP)} de ${it.Quantity}", weight = .3f,color = colores, textAlign = TextAlign.End)
                                                TableCell(text = " ${BigDecimal(pendiente).setScale(2, RoundingMode.HALF_UP)}", weight = .3f, color = colores,textAlign = TextAlign.End)
                                            }else {
                                                TableCell(text = if(it.LocationName.isNullOrEmpty()){locationName} else {it.LocationName}, weight = .4f)
                                                TableCell(text = " ${it.Batch}", weight = .3f)
                                                TableCell(text = " ${it.Quantity} ", weight = .3f,textAlign = TextAlign.End)
                                            }
                                        }else{

                                            val pendiente=(it.Quantity) - (it.Destine.sum("Quantity").toDouble()).toDouble()

                                            if(pendiente > 0.00){
                                                colores=Color.Red
                                            }

                                            if(objType in setOf(67,6701,1250000001))
                                            {
                                                if(status=="FichaCerrada")
                                                {
                                                    TableCell(text = "${it.LocationName} -\n${locationDestine}", weight = .3f,color = colores)
                                                    //TableCell(text = locationDestine, weight = .3f,color = colores)
                                                    TableCell(text = it.Batch, weight = .3f,color = colores)
                                                    TableCell(text = " ${BigDecimal(it.Destine.sum("Quantity").toString()).setScale(2, RoundingMode.HALF_UP)} de ${it.Quantity}", weight = .3f,color = colores, textAlign = TextAlign.End)
                                                    //TableCell(text = " ${BigDecimal(pendiente).setScale(2, RoundingMode.HALF_UP)}", weight = .3f, color = colores,textAlign = TextAlign.End)
                                                }else{
                                                    TableCell(text = " ${if(it.LocationName.isNullOrEmpty()){locationName} else {it.LocationName}}\n${it.Batch}", weight = .4f,color = colores)
                                                    TableCell(text = " ${BigDecimal(it.Destine.sum("Quantity").toString()).setScale(2, RoundingMode.HALF_UP)} de ${it.Quantity}", weight = .3f,color = colores, textAlign = TextAlign.End)
                                                    TableCell(text = " ${BigDecimal(pendiente).setScale(2, RoundingMode.HALF_UP)}", weight = .3f, color = colores,textAlign = TextAlign.End)
                                                }


                                            }else {
                                                TableCell(text = " ${if(it.LocationName.isNullOrEmpty()){locationName} else {it.LocationName}}\n${it.Batch}", weight = .4f,color = colores)
                                                TableCell(text = " ${BigDecimal(it.Destine.sum("Quantity").toString()).setScale(2, RoundingMode.HALF_UP)} de ${it.Quantity}", weight = .3f,color = colores, textAlign = TextAlign.End)
                                                TableCell(text = " ${BigDecimal(pendiente).setScale(2, RoundingMode.HALF_UP)}", weight = .3f, color = colores,textAlign = TextAlign.End)
                                            }
                                        }
                                    }
                                }
                            }

                            //Configuracion unica SSCC 08/08/2023 11:37
                            /*if(it.Sscc!=null && it.Sscc!!.isNotEmpty() ){

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
                            }*/
                        }
                        /*Column {
                            Row (){
                                TableCell(text = "Codigo", weight = .4f,title=true)
                                TableCell(text = "Ubicacion Destino", weight = .3f,title=true)
                                TableCell(text = "Cantidad", weight = .3f,title=true,textAlign = TextAlign.End)
                            }
                            var colores=Color.Unspecified
                            it.Destine.forEach{
                                Row(
                                    modifier=Modifier
                                        //.fillMaxWidth()
                                        .background(AzulVistony2),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TableCell(text = it.LocationCode.toString() , weight = .4f,color = colores)
                                    TableCell(text = it.LocationName.toString(), weight = .3f,color = colores)
                                    TableCell(text = it.Quantity.toString(), weight = .3f, color = colores, textAlign  = TextAlign.End )
                                }
                            }
                        }*/

                    }

                    if(subBody.isEmpty()){
                        Text("Sin registros",modifier=Modifier.fillMaxWidth().background(AzulVistony2), textAlign = TextAlign.Center)
                    }else{
                        Text("",modifier=Modifier.fillMaxWidth().background(AzulVistony2))
                    }

                }

            }
        }
    }

}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    title:Boolean=false,
    color:Color=Color.Unspecified,
    textAlign: TextAlign=TextAlign.Left
) {
    if(title){
        Text(
            text = text,
            modifier=
            Modifier
                .weight(weight)
                .padding(7.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign =textAlign
        )
    }else{
        Text(
            text = text,
            modifier= Modifier
                .weight(weight)
                .padding(start=7.dp,top=7.dp,end=7.dp),fontSize = 13.sp,
            color = color,
            textAlign = textAlign

        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeaderBody(
    objType:Int=0,
    itemName: String,
    itemCode: String,
    sku: String?,
    num: String,
    numOf:String,
    count: String,
    total2:Double=0.0,
    status: String,
    context: Context,
    onPressBody:() ->Unit,
    subBody:List<StockTransferSubBody>,
) {
    Log.e("REOS","StockTransferDetailScreen-HeaderBody-numOf:"+numOf)
    Log.e("REOS","StockTransferDetailScreen-HeaderBody-num:"+num)
    Log.e("REOS","StockTransferDetailScreen-HeaderBody-sku:"+sku)
    Log.e("REOS","StockTransferDetailScreen-HeaderBody-itemCode:"+itemCode)
    Log.e("REOS","StockTransferDetailScreen-HeaderBody-count:"+count)
    Log.e("REOS","StockTransferDetailScreen-HeaderBody-total2:"+total2)

    var QuantityDestiny:Double=0.0
    for (i in 0 until subBody.size)
    {
        Log.e("REOS","StockTransferDetailScreen-ExpandableListItem-subBody.get(i).Destine.size: "+subBody.get(i).Destine.size)
        for (j in 0 until subBody.get(i).Destine.size )
        {
            Log.e("REOS","StockTransferDetailScreen-ExpandableListItem-subBody.get(i).Destine.get(j)?.Quantity: "+subBody.get(i).Destine.get(j)?.Quantity)
            QuantityDestiny=QuantityDestiny+ subBody.get(i).Destine.get(j)?.Quantity!!

        }
    }
    Log.e("REOS","StockTransferDetailScreen-HeaderBody-QuantityDestiny:"+QuantityDestiny)
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
            /*onLongClick = {
                val urlStr = "https://wms.vistony.pe/vs1.0/Article/Photo?Name="+itemCode
                try{
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(Uri.parse(urlStr),"application/pdf")
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    context.startActivity(intent)
                }catch(e:Exception){
                    Toast.makeText(context,"Ocurrio un error al abrir el adjunto\n${e.message}",Toast.LENGTH_SHORT).show()
                }
            }*/
        )
        ){
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_insert_drive_file_24),
                contentDescription = null,
                modifier = Modifier
                    .padding(vertical = 5.dp)
                    .size(20.dp),
                tint =
                if(status !in setOf("FichaCerrada","Cancelado"))
                {
                    if(count.toDouble()==0.0)
                    {
                        AzulVistony201
                    }else if(count.toDouble()>0.0&&count.toDouble()<total2.toDouble()) {
                        RedVistony201
                    }
                    else {
                            Color.Green
                    }
                }else {
                    Color.Gray
                }
                /*if(
                    //count.toDouble()>0&&total2.toDouble()!=QuantityDestiny

                    if(objType==1250000001||objType==1701||objType==234000031){total2.toDouble()}else{count.toDouble()}!=QuantityDestiny
                    ){RedVistony201}else if(
                    //count.toDouble()>=total2&&
                    //count.toDouble()
                    if(objType==1250000001||objType==1701||objType==234000031){total2.toDouble()}else{count.toDouble()}
                    ==QuantityDestiny

                ){Color.Green}else{AzulVistony201}*/
            )
            Text(text = " $itemName", fontWeight = FontWeight.Bold,fontSize = TextUnit.Unspecified ,modifier= Modifier
                .fillMaxHeight()
                .weight(.8f))
        }

        Row(modifier=Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween){
            Column(modifier=Modifier.weight(0.5f)){
                Text(text = "$itemCode ")
                if(!sku.isNullOrEmpty()){
                    Text(text = " EAN13 $sku")
                }
                Text(text = " Num. Detalle $num")
            }
            Column(modifier=Modifier.weight(0.5f), horizontalAlignment = Alignment.End){

                when(objType){
                    22,18//,1250000001,67
                    ->{
                        /*when(status){
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
                        }*/
                        Text(text = "$count/$total2",color=Color.Blue)
                        Text(text = "Avance",color=Color.Blue)
                    }
                    /*1701->{
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
                    }*/
                    1250000001->{
                        when(status){
                            "Abierto"->{
                                /*Text(text = "$total2 ")
                                Text(text = "Cantidad ")*/
                                /*Text(text = "$total2 Cantidad")
                                Text(text = "$numOf/$num Avance",color=Color.Blue)*/

                                Text(text = "$count/$total2",color=Color.Blue)
                                Text(text = "Avance",color=Color.Blue)
                            }
                            "FichaCerrada"-> {
                                Text(text = "$count/$total2",color=Color.Blue)
                                Text(text = "Avance",color=Color.Blue)
                            }"OrigenCerrado"-> {
                            Text(text = "$count/$total2",color=Color.Blue)
                            Text(text = "Avance",color=Color.Blue)
                            }
                            "Cancelado" -> {
                                Text(text = "$count/$total2",color=Color.Blue)
                                Text(text = "Avance",color=Color.Blue)
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
                            "Cancelado" -> {
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
