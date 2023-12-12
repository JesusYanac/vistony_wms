@file:OptIn(ExperimentalFoundationApi::class)

package com.vistony.wms.screen

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.vistony.wms.R
import com.vistony.wms.component.*
import com.vistony.wms.model.*
import com.vistony.wms.num.OptionsDowns
import com.vistony.wms.ui.theme.*
import com.vistony.wms.util.Routes
import com.vistony.wms.viewmodel.StockTransferHeaderViewModel
import com.vistony.wms.viewmodel.TaskManagementViewModel
import io.realm.Realm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun TaskManagerScreen(navController: NavHostController, context: Context,users: Users) {
    val listWareHouse:MutableList<Options> = mutableListOf()
    val modal = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden, confirmStateChange = {false})
    val scope = rememberCoroutineScope()


    LaunchedEffect(modal.currentValue) {
        when (modal.currentValue) {
            ModalBottomSheetValue.Hidden -> {
                listWareHouse.clear()
            }
            ModalBottomSheetValue.Expanded -> {
            }
            else -> {}
        }
    }


    var currentBottomSheet: BottomSheetScreen? by remember { mutableStateOf(null) }
    val closeSheet: () -> Unit = { scope.launch { modal.hide() }}

    val openSheet: (BottomSheetScreen) -> Unit = {
        scope.launch {
            currentBottomSheet = it
            modal.animateTo(ModalBottomSheetValue.Expanded)
        }
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
                val listOptions = listOf(
                    OptionsDowns(" Registrar tarea",R.drawable.ic_baseline_insert_drive_file_24),
                )

                TopBarTitleWithOptions(
                    options=listOptions,
                    title=
                    "Mis tareas"
                    ,
                    onClick={

                         /*listWareHouse.add(
                            Options(
                                value=Routes.MerchandiseMovementCreate.route.replace("{objType}",""+Routes.Recepcion.value),
                                text= Routes.Recepcion.title,
                                icono=Routes.Recepcion.icon,
                                enabled = true,
                                subMenu=true,
                                subMenuVisible = false
                            )
                        )*/

                        listWareHouse.add(
                            Options(
                                value=Routes.MerchandiseMovementCreate.route.replace("{objType}",""+Routes.Merchandise.value),
                                text= Routes.MerchandiseMovementCreate.title,
                                icono=Routes.MerchandiseMovementCreate.icon
                            )
                        )

                        listWareHouse.add(
                            Options(
                                value= Routes.MerchandiseMovementCreate.route.replace("{objType}",""+Routes.Slotting.value),
                                text= Routes.Slotting.title,
                                icono=Routes.Slotting.icon
                            )
                        )

                        listWareHouse.add(
                            Options(
                                value=Routes.Picking.route,
                                text= Routes.Picking.title,
                                icono=Routes.Picking.icon,
                                enabled=false
                            )
                        )

                        listWareHouse.add(
                            Options(
                                value=Routes.Packing.route,
                                text= Routes.Packing.title,
                                icono=Routes.Packing.icon,
                                enabled=false
                            )
                        )

                        openSheet(
                            BottomSheetScreen.SelectWitOptionsModal(
                                title = "Seleciona un tipo de tarea",
                                listOptions = listWareHouse,
                                selected = {
                                    navController.navigate(it.value.replace("{objType}",""+it.value))
                                    closeSheet()

                                })
                        )

                    },
                    navController = navController,
                    form = "TaskManagementScreen",
                    users = users
                )

            }
        ){
            val taskManagementViewModel: TaskManagementViewModel = viewModel(
                factory = TaskManagementViewModel.TaskManagementViewModelFactory()
            )

            val stockTransferHeaderViewModel: StockTransferHeaderViewModel = viewModel(
                factory = StockTransferHeaderViewModel.StockTransferHeaderViewModelFactory(TaskManagement(),"Task")
            )

            body(
                taskManagementViewModel=taskManagementViewModel,
                context = context,
                onClick={

                    if(it.Task.Type=="Libre"){
                        navController.navigate("MerchandiseMovementDetail/idMerchandise=${it.Document._id}&status=${it.Document.Status}&whs=${URLEncoder.encode(it.Document.WarehouseOrigin, StandardCharsets.UTF_8.toString())}&whsDestine=${URLEncoder.encode(it.Document.WarehouseDestine, StandardCharsets.UTF_8.toString())}&objType=${it.Task.ObjType}")
                        closeSheet()
                    }else{
                        if(it.Task.ObjType==1701){ //PICKING
                            navController.navigate("MerchandiseMovementDetail/idMerchandise=${it.Document._id}&status=${it.Document.Status}&whs=${it.Document.WarehouseOrigin}&whsDestine=NULL&objType=${it.Task.ObjType}")
                            closeSheet()
                        }else{
                            stockTransferHeaderViewModel.getMerchandise(it.Task,
                                TaskMngmtDataForm(
                                    serie = it.Document.SerieDocument,
                                    correlativo = it.Document.CorrelativoDocument,
                                    comentario = it.Document.Comment
                                )
                            )

                            openSheet(
                                BottomSheetScreen.SelectFormHeaderTask(
                                    payloadForm=TaskMngmtDataForm(
                                        serie = it.Document.SerieDocument,
                                        correlativo = it.Document.CorrelativoDocument,
                                        comentario = it.Document.Comment
                                    ),
                                    stockTransferHeaderViewModel=stockTransferHeaderViewModel,
                                    taskManagement=it,
                                    context = context,
                                    onSendBody = {
                                        navController.navigate("MerchandiseMovementDetail/idMerchandise=${it.id}&status=${it.status}&whs=${URLEncoder.encode(it.cardCode, StandardCharsets.UTF_8.toString())}&whsDestine=${URLEncoder.encode(it.cardName, StandardCharsets.UTF_8.toString())}&objType=${it.objType}")
                                        closeSheet()
                                    }
                                )
                            )
                        }
                    }
                }
            )

        }
    }

}

@Composable
private fun body(taskManagementViewModel: TaskManagementViewModel,context: Context,onClick:(TaskMngmtAndHeaderDoc)->Unit){
    val taskValue = taskManagementViewModel.task.collectAsState()
    val agrupados = taskValue.value.data.groupBy {  Instant.ofEpochMilli(it.Task.DateAssignment.time).atZone(ZoneId.systemDefault()).toLocalDate() }

    LazyColumn {
        item{
            Column(modifier= Modifier.padding(top=5.dp,start=15.dp,end=15.dp)){
                Row(
                    horizontalArrangement= Arrangement.SpaceBetween,
                    verticalAlignment= Alignment.CenterVertically,
                    modifier= Modifier.fillMaxWidth()
                ){
                    Text("Número de tareas: ${taskValue.value.data.size}",color= Color.Gray)
                    TextButton(
                        onClick = {
                            taskManagementViewModel.getAllTask()
                        }
                    ){
                        Text(text="Actualizar",color= RedVistony202)
                    }
                }
            }
        }
        agrupados.forEach { (date, objects) ->
            stickyHeader {

                Card(
                    elevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 15.dp, top = 15.dp, end = 15.dp)
                ){
                    Column(modifier=Modifier.background(AzulVistony1), horizontalAlignment = Alignment.CenterHorizontally){

                        if(LocalDate.now().toString()==date.toString()){
                            Text(text = "Hoy", textAlign = TextAlign.Center, color = Color.White,modifier=Modifier.padding(vertical = 5.dp))
                        }else if(LocalDate.now().plusDays(-1).toString()==date.toString()){
                            Text(text = "Ayer", textAlign = TextAlign.Center, color = Color.White,modifier=Modifier.padding(vertical = 5.dp))
                        }
                        else{
                            Text(text = date.toString(), textAlign = TextAlign.Center, color = Color.White,modifier=Modifier.padding(vertical = 5.dp))
                        }
                    }
                }

            }
            items(objects) { data ->

                when(taskValue.value.status){
                    ""->{}
                    "cargando"->{
                        CustomProgressDialog("Cargando tareas...")
                    }
                    "ok"->{
                        formHeaderTask(
                            task = data,
                            onClick = {
                                onClick(it)
                            }
                        )
                    }
                    "vacio"->{
                        Toast.makeText(context, "No hay tareas asignadas", Toast.LENGTH_LONG).show()
                        taskManagementViewModel.resetTaskStatus()
                    }
                    else->{
                        Toast.makeText(context, "Ocurrio un error:\n ${taskValue.value.status}", Toast.LENGTH_LONG).show()
                        taskManagementViewModel.resetTaskStatus()
                    }
                }
            }
        }
        item{
            Spacer(modifier= Modifier.height(15.dp))
        }
    }
}

@Composable
private fun formHeaderTask(task:TaskMngmtAndHeaderDoc,onClick:(TaskMngmtAndHeaderDoc)->Unit){
    var expanded by remember { mutableStateOf(false) }

    Card(
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, top = 15.dp, end = 15.dp)
            .clickable(onClick = {
                expanded = !expanded
            })
    ){
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(end = 32.dp)
                    .fillMaxWidth()
            ) {

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_insert_drive_file_24),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(30.dp)
                            .clickable {
                                onClick(task)
                                /*
                                // Obtener una instancia de Realm
                                val realm = Realm.getDefaultInstance()

                                // Insertar un objeto en la base de datos
                                realm.executeTransaction { transactionRealm ->
                                    val newItem = transactionRealm.createObject(TaskManagement::class.java)
                                    /*newItem. = "1"
                                    newItem.title = "Completar la tarea"
                                    newItem.completed = false*/
                                }

                                // Realizar una consulta para obtener todos los elementos
                                val items = realm.where(TaskManagement::class.java).findAll()

                                for (item in items) {
                                    println(item.title)
                                }

                                // Cerrar la instancia de Realm cuando ya no sea necesaria
                                realm.close()*/
                            },
                        tint = if (task.Task.Status == "Terminado"||task.Task.Status == "Cancelado") {
                            Color.Gray
                        } else {

                            if(task.Task.Response.isEmpty()){
                                AzulVistony201
                            }else{
                                Color.Red
                            }

                        }
                    )
                    Text("Ver", textAlign = TextAlign.Center)

                }

                TitleAndSubtitle(
                    title = task.Task.Documento,
                    type = "N° SAP "+task.Task.DocNum,
                    status = "Tarea "+task.Task.Status,
                    dateAssigment = task.Task.DateAssignment.toString()
                )

                if(expanded){
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "ArrowIcon",
                        tint = Color.DarkGray,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                           /* .graphicsLayer(
                                rotationZ = animateFloatAsState(
                                    if (expanded) 180f else 0f
                                ).value,
                            )*/
                    )
                }else{
                    if(task.Task.Type!="Libre"){
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = "ArrowIcon",
                            tint = Color.DarkGray,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                            /* .graphicsLayer(
                                 rotationZ = animateFloatAsState(
                                     if (expanded) 180f else 0f
                                 ).value,
                             )*/
                        )
                    }else{
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "ArrowIcon",
                            tint = Color.DarkGray,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .graphicsLayer(
                                 rotationZ = animateFloatAsState( 0f).value,
                             )
                        )
                    }
                }
            }

            formBodyTask(
                expanded=expanded,
                data=task.Task
            )
        }
    }

}

@Composable
private fun formBodyTask(expanded:Boolean,data:TaskManagement){
    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically(
            animationSpec = tween(durationMillis = 300, easing = FastOutLinearInEasing)
        ),
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = 300, easing = FastOutLinearInEasing)
        )
    ) {
        Divider(modifier = Modifier.height(1.dp))
        Column(modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 15.dp)) {

            if(data.Type!="Libre"){
                Spacer(modifier = Modifier.height(6.dp))

                ExtraItem(
                    item = Item(
                        title="Fecha de Asignación",
                        date= data.DateAssignment.getUIStringTimeStampWithDate()
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))
                Divider(modifier = Modifier.height(1.dp))

                Spacer(modifier = Modifier.height(6.dp))

                ExtraItem(
                    item = Item(
                        title="Fecha de Programación",
                        date= data.ScheduledTime.getUIStringTimeStampWithDate()
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))
                Divider(modifier = Modifier.height(1.dp))
            }


            Spacer(modifier = Modifier.height(6.dp))

            ExtraItem(item = Item(
                title="Fecha de Inicio",
                date=if(data.StartDate.getUIStringTimeStampWithDate() != "02-ene.-0001 18:51"){"${data.StartDate.getUIStringTimeStampWithDate()} "}else{" "}
            )
            )

            Spacer(modifier = Modifier.height(6.dp))
            Divider(modifier = Modifier.height(1.dp))
            Spacer(modifier = Modifier.height(6.dp))

            ExtraItem(item = Item(
                title="Fecha de Term.",
                date=if(data.EndDate.getUIStringTimeStampWithDate() != "02-ene.-0001 18:51" && data.EndDate.getUIStringTimeStampWithDate() != data.StartDate.getUIStringTimeStampWithDate()){"${data.EndDate.getUIStringTimeStampWithDate()} "}else{" "}
            )
            )

            Spacer(modifier = Modifier.height(6.dp))
            Divider(modifier = Modifier.height(1.dp))
            Spacer(modifier = Modifier.height(6.dp))

            ExtraItem(item = Item(
                title="Tipo de Tarea",
                date=data.Type
            )
            )

            Spacer(modifier = Modifier.height(6.dp))
            Divider(modifier = Modifier.height(1.dp))
            Spacer(modifier = Modifier.height(6.dp))


            Row(
                horizontalArrangement = Arrangement.Center,
                modifier=Modifier.padding(start=5.dp,end=5.dp).fillMaxWidth()
            ){
                Text(data.CardCode+" - "+data.CardName, textAlign = TextAlign.Center,color=Color.Gray)
            }

            if(data.Response.isNotEmpty()){
                /*ExtraItem(
                    item = Item(
                    title="Estado de Tarea",
                    date="# ",),
                    status="Cerrado"
                )*/
                Text("")
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier=Modifier.fillMaxWidth()
                ){
                    Text(text = data.Response,color=Color.Red)
                }
            }


            /*TextButton(
                modifier= Modifier
                    .padding(top = 10.dp, bottom = 5.dp)
                    .fillMaxWidth(),
                enabled = merchandise.Status=="Abierto",
                onClick = {
                    onPresChangeStatus("Close")
                }) {
                Text(text="Cerrar ficha",color= if(merchandise.Status=="Abierto"){
                    AzulVistony202
                }else{Color.Gray})
            }*/

        }
    }
}