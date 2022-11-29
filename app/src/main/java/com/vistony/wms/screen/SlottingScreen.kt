@file:OptIn(ExperimentalComposeUiApi::class)

package com.vistony.wms.screen

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
import com.vistony.wms.enum_.OptionsDowns
import com.vistony.wms.model.StockTransferHeader
import com.vistony.wms.model.TaskManagement
import com.vistony.wms.model.TransactionDocument
import com.vistony.wms.ui.theme.AzulVistony201
import com.vistony.wms.util.Routes
import com.vistony.wms.viewmodel.StockTransferHeaderViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun SlottingScreen(navController: NavHostController, context: Context,objType: TaskManagement){
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

    val stockTransferHeaderViewModel: StockTransferHeaderViewModel = viewModel(
        factory = StockTransferHeaderViewModel.StockTransferHeaderViewModelFactory(objType)
    )

    val stockTrsferHeaderdValue = stockTransferHeaderViewModel.merchandise.collectAsState()

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
                    OptionsDowns(" Crear slotting",R.drawable.ic_baseline_insert_drive_file_24),
                )

                TopBarTitleWithOptions(
                    options=listOptions,
                    title= Routes.Slotting.title ,
                    onClick={
                        navController.navigate("MerchandiseMovementCreate/objType=${objType.ObjType}")
                    }
                )
            }
        ){
            LazyColumn(modifier = Modifier.fillMaxHeight()) {
                item {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Número de slotting: ${stockTrsferHeaderdValue.value.stockTransferHeader.size}",
                                color = Color.Gray
                            )
                            TextButton(
                                onClick = {
                                    stockTransferHeaderViewModel.getMerchandise(objType)
                                }
                            ) {
                                Text("Actualizar")
                            }
                        }
                        Text(text = "${stockTrsferHeaderdValue.value.ownerName } ",color= Color.Gray)
                    }
                }
                items(items=stockTrsferHeaderdValue.value.stockTransferHeader ){ merchandiseI ->

                    when(stockTrsferHeaderdValue.value.status){
                        ""->{}
                        "cargando"->{
                            CustomProgressDialog("Listando fichas...")

                        }
                        "ok"->{

                            val openDialog = remember { mutableStateOf(FlagDialog()) }

                            if(openDialog.value.status){
                                CustomDialogResendOrClose(
                                    title="Cerrar Transferencia",
                                    openDialog={ response ->
                                        if(response){
                                            if(openDialog.value.flag=="Close"){
                                                stockTransferHeaderViewModel.updateStatusClose(merchandiseI._id)
                                            }else if(openDialog.value.flag=="Resend"){
                                                stockTransferHeaderViewModel.resendToSap(merchandiseI._id)
                                            }

                                        }
                                        openDialog.value= FlagDialog(false)
                                    },
                                    flag=openDialog.value.flag
                                )
                            }

                            ExpandableListItem(
                                merchandise=merchandiseI,
                                navController=navController,
                                objType=objType,
                                onPresChangeStatus={
                                    openDialog.value= FlagDialog(
                                        status = true,
                                        flag=it
                                    )
                                }
                            )

                        }
                        "vacio"->{
                            Toast.makeText(context, "No hay historico a mostrar", Toast.LENGTH_SHORT).show()
                            stockTransferHeaderViewModel.resetMerchandiseHeader()
                        }
                        else->{
                            Toast.makeText(context, "Ocurrio un error:\n ${stockTrsferHeaderdValue.value.status}", Toast.LENGTH_SHORT).show()
                            stockTransferHeaderViewModel.resetMerchandiseHeader()
                        }

                    }

                }
                item{
                    Spacer(modifier= Modifier.height(15.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ExpandableListItem(merchandise: StockTransferHeader, navController: NavHostController,objType:TaskManagement, onPresChangeStatus:(String) ->Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, top = 15.dp, end = 15.dp)
            .clickable(onClick = { expanded = !expanded })
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(end = 32.dp)
                    .fillMaxWidth()
            ) {

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxHeight()
                ){
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_insert_drive_file_24),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(30.dp)
                            .clickable {
                                navController.navigate("MerchandiseMovementDetail/idMerchandise=${merchandise._id}&status=${merchandise.Status}&whs=${merchandise.WarehouseOrigin}&whsDestine=${merchandise.WarehouseDestine}&objType=${objType.ObjType}")
                            },
                        tint = if(merchandise.Status=="FichaCerrada"){Color.Gray}else{
                            AzulVistony201
                        }
                    )
                    when(merchandise.Status){
                        "Abierto"->{
                            Text("Abrir", textAlign = TextAlign.Center)
                        }
                        "OrigenCerrado"->{
                            Text("Abrir", textAlign = TextAlign.Center)
                        }
                        else->{
                            Text("Ver", textAlign = TextAlign.Center)
                        }
                    }
                }

                TitleAndSubtitle(
                    title = "Almacén Orig. ${merchandise.WarehouseOrigin}",
                    type = "Almacén Dst. ${merchandise.WarehouseDestine}",
                    status = "Estado ${merchandise.Status}"
                )

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "ArrowIcon",
                    tint=Color.DarkGray,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .graphicsLayer(
                            rotationZ = animateFloatAsState(
                                if (expanded) 180f else 0f
                            ).value,
                        )

                )
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
                Divider(modifier = Modifier.height(1.dp))
                Column(modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 15.dp)) {

                    Spacer(modifier = Modifier.height(10.dp))

                    ExtraItem(item = Item(
                        title="Comentario",
                        date=merchandise.Comment
                    )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(modifier = Modifier.height(1.dp))
                    Spacer(modifier = Modifier.height(10.dp))

                    ExtraItem(item = Item(
                        title="Fecha de Inicio",
                        date="${merchandise.CreateAt.getUIStringTimeStampWithDate()} "
                    )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(modifier = Modifier.height(1.dp))
                    Spacer(modifier = Modifier.height(10.dp))

                    ExtraItem(item = Item(
                        title="Fecha de Finalización",
                        date=if(merchandise.CreateAt != merchandise.CloseAt){"${merchandise.CloseAt.getUIStringTimeStampWithDate()} "}else{" "}
                    )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(modifier = Modifier.height(1.dp))
                    Spacer(modifier = Modifier.height(10.dp))

                    ExtraItem(
                        item = Item(
                            title="N° SAP",
                            date=if(merchandise.CodeSAP==0){"# "}else{"${merchandise.CodeSAP} " }
                        ),
                        onClick={
                            onPresChangeStatus("Resend")
                        },
                        status=merchandise.Status
                    )

                    if(merchandise.Response.isNotEmpty()){

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(modifier = Modifier.height(1.dp))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier=Modifier.fillMaxWidth()
                        ) {
                            Text(text = merchandise.Response,color=Color.Red)
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
    }
}

