package com.vistony.wms.screen

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.gson.annotations.SerializedName
import com.vistony.wms.R
import com.vistony.wms.component.*
import com.vistony.wms.model.*
import com.vistony.wms.num.TypeCode
import com.vistony.wms.ui.theme.AzulVistony202
import com.vistony.wms.ui.theme.ColorDestine
import com.vistony.wms.viewmodel.ItemsViewModel
import com.vistony.wms.viewmodel.PrintViewModel
import com.vistony.wms.viewmodel.WarehouseViewModel
import com.vistony.wms.viewmodel.ZebraViewModel

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PrintQrUnidadesScreen(
    navController: NavHostController,
    context: Context,
    zebraViewModel: ZebraViewModel
){
    val zebraValue = zebraViewModel.data.collectAsState()

    val printViewModel: PrintViewModel = viewModel(
        factory = PrintViewModel.PrintViewModelFactory()
    )
    printViewModel.setFlagPrint("Zebra_DM")


    val statusPrint = printViewModel.statusPrint.collectAsState()

    if (zebraValue.value.Payload.isNotEmpty()) {
        Log.d("jesusdebug", "Se escaneó: "+zebraValue.value.Payload)
    }
    Scaffold(
        topBar = {
            TopBarWithBackPress(
                title="Rotulado Unidades QR",
                onButtonClicked = {
                    navController.navigateUp()
                }
            )

        },
        floatingActionButton = {
            Box(modifier = Modifier
                .padding(10.dp)
                .background(color = AzulVistony202)) {
                FloatingActionButton(
                    onClick = {
                        Log.d("jesusdebug","onClick")
                        printViewModel.setStatusPrint("buscando")
                    },
                    backgroundColor = AzulVistony202
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search_white_24dp),
                        contentDescription = "Imprimir",
                        tint = Color.White
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        isFloatingActionButtonDocked = true,
        bottomBar = {
        }

    ){

        when(statusPrint.value){
            ""->{}
            "cargando"->{
                lockScreen("Imprimiendo...")
            }
            "buscando"->{
                lockSearchScreen("Buscando",printViewModel)
            }
            "ok"->{
                Toast.makeText(context,"Imprimiendo...", Toast.LENGTH_LONG).show()
            }
            else->{
                Toast.makeText(context,statusPrint.value, Toast.LENGTH_LONG).show()
            }
        }

        Column(modifier= Modifier
            .padding(20.dp)
            .verticalScroll(rememberScrollState())) {
            divPrint(
                viewModel = printViewModel,
                onContinue = {
                    Log.d("jesusdebug","onContinue")
                    printViewModel.sendPrint(it)
                },
                onCancel = {
                    navController.navigateUp()
                }
            )
        }
    }
}

