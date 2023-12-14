package com.vistony.wms.screen

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.vistony.wms.R
import com.vistony.wms.component.*
import com.vistony.wms.model.LoginResponse
import com.vistony.wms.model.Options
import com.vistony.wms.model.StockTransferHeader
import com.vistony.wms.model.TaskManagement
import com.vistony.wms.ui.theme.AzulVistony201
import com.vistony.wms.ui.theme.AzulVistony202
import com.vistony.wms.util.Routes
import com.vistony.wms.util.RoutesOptionDashboard
import com.vistony.wms.viewmodel.LoginViewModel
import com.vistony.wms.viewmodel.StockTransferHeaderViewModel
import com.vistony.wms.viewmodel.TaskManagementViewModel
import kotlinx.coroutines.launch
import java.util.*

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun DashboardScreen(navController: NavHostController,user: LoginResponse,context: Context){

    BackHandler {}

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

    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.LoginViewModelFactory(context)
    )


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
                TopBarDashboard(
                    title="Hola ${user.FirstName}",
                    navController = navController,
                    loginViewModel = loginViewModel,
                    context=context

                )
            }
        ){
            DashboardSection(
                options = RoutesOptionDashboard,
                open= openSheet,
                close= closeSheet,
                user=user,
                context=context,
                navController=navController
            )
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun DashboardSection(options: List<Routes>, user:LoginResponse, navController: NavHostController, context: Context, open: (BottomSheetScreen) -> Unit, close:() ->Unit) {
    val merchandiseViewModel: StockTransferHeaderViewModel = viewModel(
        factory = StockTransferHeaderViewModel.StockTransferHeaderViewModelFactory(TaskManagement(ObjType = 67))
    )
    val taskManagementViewModel: TaskManagementViewModel = viewModel(
        factory = TaskManagementViewModel.TaskManagementViewModelFactory()
    )

    val _merchandiseViewModel = merchandiseViewModel.MerchandiseHeaderValue.collectAsState()
    Log.e("REOS","Dashboard-DashboardSection-_merchandiseViewModel.value.id: "+_merchandiseViewModel.value.id)
    if(_merchandiseViewModel.value.id.isNotEmpty()){
        if(_merchandiseViewModel.value.id == "error"){
            Toast.makeText(context, "Ocurrio un error al crear la transferencia de stock.", Toast.LENGTH_SHORT).show()
        }else{
            var taskManagement=TaskManagement(ObjType = 67)
            navController.navigate("MerchandiseMovementDetail/idMerchandise=${_merchandiseViewModel.value.id}&status=${_merchandiseViewModel.value.status}&whs=${_merchandiseViewModel.value.whs}&whsDestine=${_merchandiseViewModel.value.whsDestine}&objType=${taskManagement.ObjType}")
            /*{
                popUpTo(Routes.MerchandiseMovementCreate.route) { inclusive = true }
            }*/

            merchandiseViewModel.resetMerchandiseHeader()
            //navController?.navigate("TaskManager")
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        Text(
            text = "Menu de opciones",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(start=20.dp,top=20.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Locación: ${user.Location.uppercase(Locale.ROOT)}",
            modifier = Modifier.padding(start=20.dp,bottom=20.dp),
            textAlign = TextAlign.Center,
            color=Color.Gray
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 7.5.dp, end = 7.5.dp,bottom = 10.dp),
            modifier = Modifier.fillMaxHeight()
        ){
            items(options.size) { i ->
                CourseItem(
                    options=options[i],
                    onPress={ route ->
                        if(route == Routes.Almacenamiento.route){
                            Toast.makeText(context, "Es necesario configurar este modulo.", Toast.LENGTH_SHORT).show()
                        }
                        else{

                            if(options[i].value!=0){
                                navController.navigate(route.replace("{objType}",""+options[i].value))
                            }else{
                                if(options[i].route ==  Routes.ImprimirEtiqueta.route){

                                    val typePrinters:MutableList<Options> = mutableListOf()

                                    typePrinters.add(
                                        Options(
                                            value= Routes.ImprimirEtiqueta.route,
                                            text= "Rotulado QR",
                                            icono= R.drawable.ic_baseline_print_24
                                        )
                                    )

                                    typePrinters.add(
                                        Options(
                                            value= Routes.ImprimirEtiquetaSSCC.route,
                                            text= "Rotulado palet existente",
                                            icono=R.drawable.ic_baseline_print_24
                                        )
                                    )

                                    open(
                                        BottomSheetScreen.SelectWitOptionsModal(
                                            title = "Seleciona un tipo de rotulado",
                                            listOptions = typePrinters,
                                            selected = {
                                                navController.navigate(it.value.replace("{objType}",""+it.value))
                                                close()
                                            })
                                    )
                                }
                                else if(options[i].route ==  Routes.Recepcion.route) {

                                    val typePrinters:MutableList<Options> = mutableListOf()

                                    typePrinters.add(
                                        Options(
                                            //value= Routes.MerchandiseMovementCreate.route.replace("{objType}",""+Routes.Merchandise.value),
                                            value= Routes.MerchandiseMovementCreate.route.replace("{objType}",""+Routes.Merchandise.value),
                                            text= "Producción",
                                            icono= R.drawable.ic_baseline_factory_24,

                                        )
                                    )

                                    typePrinters.add(
                                        Options(
                                            value= Routes.ImprimirEtiquetaSSCC.route,
                                            text= "Compras",
                                            icono=R.drawable.ic_baseline_recepcion_24,
                                            enabled = false
                                        )
                                    )

                                    open(
                                        BottomSheetScreen.SelectWitOptionsModal(
                                            title = "Seleciona un tipo de recepción",
                                            listOptions = typePrinters,
                                            selected = {
                                                if(it.text.equals("Producción"))
                                                {

                                                    merchandiseViewModel.addMerchandiseHeader(
                                                        StockTransferHeader(
                                                            Comment= "Recepción de Producción|AN001-EXP-RP-01",
                                                            CreateAt= Date(),
                                                            NumReference= "",
                                                            PriceList= -1,
                                                            ObjType=67,
                                                            Motive="11",
                                                            WarehouseDestine = "AN001",
                                                            WarehouseOrigin = "AN001"
                                                        ),
                                                        "reception"
                                                    )
                                                    close()

                                                }else {
                                                    navController.navigate(it.value.replace("{objType}",""+it.value))
                                                    close()
                                                }
                                            })
                                    )
                                }else
                                {
                                    navController.navigate(route)
                                }

                            }


                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CourseItem(
    options: Routes,
    onPress:(String)->Unit
){
    BoxWithConstraints(

        modifier = Modifier
            .padding(7.5.dp).clickable {
                if(options.title in listOf("Recepción","Maestros","Toma de inventario","Mis tareas","Imprimir rotulados","Tracking del Palet","Recibo de producción", "Transferencia de Stock")){
                    onPress(options.route)
                }
            }
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if(options.title in listOf("Recepción","Maestros","Toma de inventario","Mis tareas","Imprimir rotulados","Tracking del Palet","Recibo de producción", "Transferencia de Stock")){
                    AzulVistony201
                }else{
                    Color.Gray
                }
            )
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp)
        ){
            Text(
                text = options.title,
                color=Color.White,
                style = MaterialTheme.typography.h6,
                lineHeight = 24.sp,
                modifier = Modifier.align(Alignment.TopStart)
            )

            Icon(
                painter = painterResource(id = options.icon),
                tint= Color.White,
                contentDescription = null,
                modifier = Modifier.align(Alignment.BottomStart).size(35.dp).padding(vertical = 6.dp)
            )

            Text(
                text = "Ver",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if(options.title in listOf("Recepción","Maestros","Toma de inventario","Mis tareas","Imprimir rotulados","Tracking del Palet","Recibo de producción", "Transferencia de Stock")){
                            AzulVistony202
                        }else{
                            Color.Gray
                        }
                    )
                    .padding(vertical = 6.dp, horizontal = 15.dp)
            )
        }
    }
}

