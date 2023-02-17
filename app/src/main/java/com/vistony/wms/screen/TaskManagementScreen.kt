@file:OptIn(ExperimentalFoundationApi::class)

package com.vistony.wms.screen

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.vistony.wms.num.OptionsDowns
import com.vistony.wms.model.TaskManagement
import com.vistony.wms.model.TaskMngmtAndHeaderDoc
import com.vistony.wms.model.TaskMngmtDataForm
import com.vistony.wms.ui.theme.AzulVistony201
import com.vistony.wms.ui.theme.RedVistony202
import com.vistony.wms.viewmodel.StockTransferHeaderViewModel
import com.vistony.wms.viewmodel.TaskManagementViewModel
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun TaskManagerScreen(navController: NavHostController, context: Context) {

    val modal = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden, confirmStateChange = {false})
    val scope = rememberCoroutineScope()

    var currentBottomSheet: BottomSheetScreen? by remember {
        mutableStateOf(null)
    }

    val closeSheet: () -> Unit = {
        scope.launch {
            modal.hide()
        }
    }

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
                    title= "Tareas asignadas" ,
                    onClick={
                        //navController.navigate("MerchandiseMovementCreate/objType=$objType")
                    }
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

    LazyColumn(modifier = Modifier.fillMaxHeight()) {
        item{
            Column(modifier= Modifier.padding(10.dp)){
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

                //Text(text = "${inventoryValue.value.ownerName } ",color= Color.Gray)
            }
        }
        items(taskValue.value.data){ data ->

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
                    Toast.makeText(context, "No hay tereas asignadas", Toast.LENGTH_LONG).show()
                    taskManagementViewModel.resetTaskStatus()
                }
                else->{
                    Toast.makeText(context, "Ocurrio un error:\n ${taskValue.value.status}", Toast.LENGTH_LONG).show()
                    taskManagementViewModel.resetTaskStatus()
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
                            },
                        tint = if (task.Task.Status == "Terminado") {
                            Color.Gray
                        } else {

                            if(task.Task.Response.isEmpty()){
                                AzulVistony201
                            }else{
                                Color.Red
                            }

                        }
                    )
                   // if (task.Status == "Pendiente") {
                        Text("Ver", textAlign = TextAlign.Center)
                   // }
                }

                TitleAndSubtitle(
                    title = task.Task.Documento,
                    type = "N° SAP "+task.Task.DocNum,
                    status = "Estado "+task.Task.Status
                )

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "ArrowIcon",
                    tint = Color.DarkGray,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .graphicsLayer(
                            rotationZ = animateFloatAsState(
                                if (expanded) 180f else 0f
                            ).value,
                        )
                )
            }

            formBodyTask(
                expanded=expanded,
                data=task.Task
            )
        }
    }

}

@OptIn(ExperimentalAnimationApi::class)
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

            Spacer(modifier = Modifier.height(10.dp))

            ExtraItem(item = Item(
                title="Fecha de Asignación",
                date= data.DateAssignment.getUIStringTimeStampWithDate()
            )
            )

            Spacer(modifier = Modifier.height(10.dp))
            Divider(modifier = Modifier.height(1.dp))
            Spacer(modifier = Modifier.height(10.dp))

            ExtraItem(item = Item(
                title="Fecha de Inicio",
                date=if(data.StartDate.getUIStringTimeStampWithDate() != "02-ene.-0001 18:51"){"${data.StartDate.getUIStringTimeStampWithDate()} "}else{" "}
            )
            )

            Spacer(modifier = Modifier.height(10.dp))
            Divider(modifier = Modifier.height(1.dp))
            Spacer(modifier = Modifier.height(10.dp))

            ExtraItem(item = Item(
                title="Fecha de Term.",
                date=if(data.EndDate.getUIStringTimeStampWithDate() != "02-ene.-0001 18:51"){"${data.EndDate.getUIStringTimeStampWithDate()} "}else{" "}
            )
            )

            Spacer(modifier = Modifier.height(10.dp))
            Divider(modifier = Modifier.height(1.dp))
            Spacer(modifier = Modifier.height(10.dp))

            ExtraItem(item = Item(
                title="Tipo de Tarea",
                date=data.Type
            )
            )

            Spacer(modifier = Modifier.height(10.dp))
            Divider(modifier = Modifier.height(1.dp))
            Spacer(modifier = Modifier.height(10.dp))


            Row(
                horizontalArrangement = Arrangement.Center,
                modifier=Modifier.padding(start=5.dp,end=5.dp).fillMaxWidth()
            ){
                Text(data.CardCode+" - "+data.CardName, textAlign = TextAlign.Center,color=Color.Gray)
            }

            if(data.Response.isNotEmpty()){
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