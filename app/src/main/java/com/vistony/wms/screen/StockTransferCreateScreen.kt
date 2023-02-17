@file:OptIn(ExperimentalFoundationApi::class)

package com.vistony.wms.screen

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.vistony.wms.component.*
import com.vistony.wms.model.StockTransferHeader
import com.vistony.wms.model.TaskManagement
import com.vistony.wms.model.TransactionDocument
import com.vistony.wms.viewmodel.StockTransferHeaderViewModel
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun MerchandiseCreateScreen(navController: NavHostController, context: Context,objType: TaskManagement){

    val titleTask=when(objType.ObjType){
        0->{""}
        67->{"Crear transferencia de stock"}
        671->{"Crear slotting"}
        else->{""}
    }

    val merchandiseViewModel: StockTransferHeaderViewModel = viewModel(
        factory = StockTransferHeaderViewModel.StockTransferHeaderViewModelFactory(objType)
    )

    val _merchandiseViewModel = merchandiseViewModel.MerchandiseHeaderValue.collectAsState()

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

                TopBarWithBackPress(title=titleTask, onButtonClicked = {
                    navController.popBackStack()
                })
            }
        ){
            if(_merchandiseViewModel.value.id.isNotEmpty()){
                if(_merchandiseViewModel.value.id == "error"){
                    Toast.makeText(context, "Ocurrio un error al crear la transferencia de stock.", Toast.LENGTH_SHORT).show()
                }else{

                    /*
                     *navArgument("locationId") { type = NavType.IntType },navArgument("locationText") { type = NavType.StringType }
                     */

                    navController.navigate("MerchandiseMovementDetail/idMerchandise=${_merchandiseViewModel.value.id}&status=${_merchandiseViewModel.value.status}&whs=${_merchandiseViewModel.value.whs}&whsDestine=${_merchandiseViewModel.value.whsDestine}&objType=${objType.ObjType}")
                    merchandiseViewModel.resetMerchandiseHeader()
                }
            }else{
                DivContainerFun(
                    context=context,
                    onPressed = {
                        merchandiseViewModel.addMerchandiseHeader(it)
                    },
                    objType=objType,
                    openSheet,closeSheet)
            }
        }
    }
}

@Composable
private fun DivContainerFun(context: Context, onPressed: (StockTransferHeader) -> Unit,objType:TaskManagement, open: (BottomSheetScreen) -> Unit, close:() ->Unit){
    var showDialog by remember { mutableStateOf(false) }
    var merchandiseHeaderTemp: StockTransferHeader by remember { mutableStateOf(StockTransferHeader()) }

    if(showDialog){
        CustomDialogCreateConteo(
            titulo="Crear transferencia",
            mensaje="¿Está seguro de crear esta ficha de transferencia?",
            openDialog={ response ->
            if(response){
                onPressed(merchandiseHeaderTemp)
            }

            showDialog=false

        })
    }
    formCreateInventoryEntryOrExit(
        objType.ObjType,
        context,onPress= {
            merchandiseHeaderTemp=it
            showDialog=true
    },open,close)
}