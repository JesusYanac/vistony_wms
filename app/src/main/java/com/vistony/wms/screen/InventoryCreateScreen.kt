package com.vistony.wms.screen

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
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
import com.vistony.wms.model.Inventory
import com.vistony.wms.model.InventoryPayload
import com.vistony.wms.viewmodel.InventoryViewModel
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun RecuentoScreen(navController: NavHostController,context: Context){

    val inventoryViewModel: InventoryViewModel = viewModel(
        factory = InventoryViewModel.InventoryViewModelFactory()
    )

    val idInventoryHeader = inventoryViewModel.idInventoryHeader.collectAsState()

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
                TopBar(title="Crear ficha de inventario")
            }
        ){

            if(idInventoryHeader.value.idInventoryHeader.isNotEmpty()){
                if(idInventoryHeader.value.idInventoryHeader == "error"){
                    Toast.makeText(context, "Ocurrio un error al crear la ficha.", Toast.LENGTH_SHORT).show()
                }else{
                    navController.navigate("InventoryCounting/idInventory=${idInventoryHeader.value.idInventoryHeader}&whs=${idInventoryHeader.value.idWhs}&status=Abierto&defaultLocation=${URLEncoder.encode(idInventoryHeader.value.defaultLocation, StandardCharsets.UTF_8.toString())}&typeInventory=${URLEncoder.encode(idInventoryHeader.value.type, StandardCharsets.UTF_8.toString())}"){
                        popUpTo("Recuento") { inclusive = true }
                    }
                    inventoryViewModel.resetIdInventoryHeader()
                }

            }else{
                DivContainerFun(context=context, onPressed = {
                    inventoryViewModel.addInventoryHeader(it)
                },openSheet,closeSheet)
            }
        }
    }
}

@Composable
private fun DivContainerFun(context:Context, onPressed: (InventoryPayload) -> Unit, open: (BottomSheetScreen) -> Unit, close:() ->Unit){
    var showDialog by remember { mutableStateOf(false) }
    var invetoryTemp:InventoryPayload by remember { mutableStateOf(InventoryPayload()) }

    if(showDialog){
        CustomDialogCreateConteo(
            titulo="Crear ficha de inventario",
            mensaje="¿Está seguro de crear esta ficha de inventario?",
            openDialog={ response ->
            if(response){
                onPressed(invetoryTemp)
            }

            showDialog=false

        })
    }

    formCreateInventoryHeader(
        context=context,
        onPress ={
            invetoryTemp=it
            showDialog=true
        },
        open=open,
        close=close
    )
}