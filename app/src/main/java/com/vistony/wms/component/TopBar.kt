package com.vistony.wms.component

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.vistony.wms.MainActivity
import com.vistony.wms.ui.theme.AzulVistony201
import com.vistony.wms.ui.theme.AzulVistony202
import com.vistony.wms.R
import com.vistony.wms.model.Activity
import com.vistony.wms.model.Users
import com.vistony.wms.num.*
import com.vistony.wms.util.Routes
import com.vistony.wms.viewmodel.LoginViewModel

@SuppressLint("PermissionLaunchedDuringComposition")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TopBarTitleCamera(
    title: MutableState<String> = remember {mutableStateOf("") },
    status:String="",
    objType:Int=0,
    permission:PermissionState,
    onClick:(TypeReadSKU) -> Unit,
    navController: NavHostController,
    form:String,
    commentReception:String
){
    Column{

        TopAppBar(
            modifier= Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AzulVistony202,
                        AzulVistony201
                    )
                )),
            title = {
                Row(modifier = Modifier
                    //.padding(10.dp)
                ){

                    when (form)
                    {
                         "StockTransferDetailScreen",
                         //"InventoryDetailScreen"
                        ->{
                            Icon(
                                Icons.Filled.ArrowBack,
                                tint = Color.White,
                                contentDescription = null,
                                modifier = Modifier.clickable { navController.navigate("TaskManager") }
                            )
                            Spacer(modifier=Modifier.padding(10.dp))
                        }
                    }

                    Text(
                        text = title.value,
                        color= Color.White
                    )
                }
            },
            actions = {

                DropdownMenuCamera(
                    status=status,
                    objType=objType,
                    bodyContent={
                    if (it == TypeReadSKU.CAMERA){
                        if(!permission.hasPermission){
                            permission.launchPermissionRequest()
                        }
                    }

                    onClick(it)
                },
                    commentReception = commentReception
                )
            },
            backgroundColor = Color.Transparent,
            elevation = 0.dp
        )
    }
}


@Composable
private fun DropdownMenuCamera(
    objType:Int,
    status:String,
    bodyContent: (TypeReadSKU)->Unit,
    commentReception:String

) {
    Log.e("JEPICAME","=>objtype "+objType)
    Log.e("REOS","TopBar-DropdownMenuCamera-status: "+status)
    Log.e("REOS","TopBar-DropdownMenuCamera-objType: "+objType)
    Log.e("REOS","TopBar-DropdownMenuCamera-commentReception: "+commentReception)
    val expanded = remember { mutableStateOf(false) }
    //Spacer(modifier=Modifier.padding(30.dp))
    val listOptions: MutableList<OptionsInventory> = mutableListOf()

    when(objType){

        22,18,1250000001,67,6701//,67
        ->{ //Orden de Compra,Factura de Reserva,
            //if(status!="OrigenCerrado"){
            /*if(status=="OrigenCerrado"){
                listOptions.add(OptionsInventory(TypeReadSKU.KEYBOARD,"Manual",R.drawable.ic_baseline_keyboard_24))
            }*/
            listOptions.add(OptionsInventory(TypeReadSKU.KEYBOARD,"Manual",R.drawable.ic_baseline_keyboard_24))
        }
        //67,6701->{}
        100->{
            if(status=="Abierto"){
                listOptions.add(OptionsInventory(TypeReadSKU.KEYBOARD,"Manual",R.drawable.ic_baseline_keyboard_24))
                listOptions.add(OptionsInventory(TypeReadSKU.CAMERA,"Camara",R.drawable.ic_baseline_camera_alt_24))
                listOptions.add(OptionsInventory(TypeReadSKU.HANDHELD,"Handheld",R.drawable.ic_baseline_qr_code_scanner_24))
                //listOptions.add(OptionsInventory(TypeReadSKU.HANDHELD,"Handheld Barra",R.drawable.ic_baseline_qr_code_scanner_24))
            }
        }
        else->{}
    }

    Box(
        Modifier
            .wrapContentSize(Alignment.TopEnd)
    ) {
        IconButton(onClick = {
            expanded.value = true
        }) {
            Icon(
                Icons.Filled.MoreVert,
                contentDescription = "More Menu",
                tint = Color.White
            )
        }
    }

    DropdownMenu(
        modifier=Modifier.fillMaxWidth(0.5f),
        expanded = expanded.value,
        onDismissRequest = { expanded.value = false },
    ) {
        //Spacer(modifier=Modifier.padding(30.dp))
        listOptions.forEachIndexed { i,it ->
            if(i==0){
                Text(" Tipos de entrada", color = Color.LightGray)
            }
            DropdownMenuItem(onClick = {
                expanded.value = false
                bodyContent(it.type)
            }) {
                Row{
                    Icon(
                        painter = painterResource(id = it.icon),
                        tint= AzulVistony202,
                        contentDescription = null
                    )
                    Text("  ${it.text}")
                }
            }

            if(i!=listOptions.size-1){
                Divider()
            }
        }

        Divider()
        Text(" Cambiar estados", color = Color.LightGray)
        if(objType in setOf(67,6701))
        {
            DropdownMenuItem(onClick = {
                expanded.value = false
                bodyContent(TypeReadSKU.CANCELAR_FICHA)
            })
            {
                Row(
                    //modifier = Modifier.padding(15.dp)

                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        tint = AzulVistony202,
                        contentDescription = null
                    )
                    Text("  Cancelar Ficha",)
                }
            }
        }

        DropdownMenuItem(onClick={
            expanded.value = false

            when(objType){
                Routes.Inventory.value->{
                    bodyContent(
                        when(status){
                            "Abierto"->{
                                TypeReadSKU.CERRAR_FICHA
                            }
                            "FichaCerrada"->{
                                TypeReadSKU.REENVIAR_FICHA
                            }
                            else->{
                                TypeReadSKU.CERRAR_ORIGEN
                            }
                        }
                    )
                }
                else->{

                    bodyContent(if(status=="OrigenCerrado"){
                        TypeReadSKU.CERRAR_FICHA

                    }else{
                        if(objType==22||objType==18||objType==67
                            //||objType==1250000001||objType==67
                        )
                        {
                            if(objType==67&&commentReception.equals("Recepción de Producción")||objType in setOf(22,18))
                            {
                                TypeReadSKU.CERRAR_FICHA
                            }else{
                                TypeReadSKU.CERRAR_ORIGEN
                            }
                            //TypeReadSKU.CERRAR_FICHA
                        }else {
                            TypeReadSKU.CERRAR_ORIGEN
                        }

                    }
                    )
                }
            }


        }){
            Row{

                when(objType){
                    Routes.Inventory.value->{

                        if(status=="Abierto"){
                            Icon(
                                imageVector = Icons.Filled.Close ,
                                tint= AzulVistony202,
                                contentDescription = null
                            )
                            Text("  Cerrar Ficha")
                        }else{
                            Icon(
                                imageVector = Icons.Filled.Refresh ,
                                tint= AzulVistony202,
                                contentDescription = null
                            )
                            Text("  Reenviar Ficha")
                        }
                    }
                    else->{
                        Icon(
                            imageVector = Icons.Filled.Close,
                            tint= AzulVistony202,
                            contentDescription = null
                        )
                        Text(if(status=="OrigenCerrado"){"  Cerrar Ficha"}else{
                            if(objType==22||objType==18||objType==67
                                //||objType==1250000001||objType==67
                            )
                            {
                                if(objType==67&&commentReception.equals("Recepción de Producción")||objType in setOf(22,18))
                                {
                                    "  Cerrar Ficha"
                                }else{
                                    "  Cerrar Origen"
                                }

                            }else {
                                "  Cerrar Origen"
                            }
                        })
                    }
                }
            }
        }
    }
}


@SuppressLint("PermissionLaunchedDuringComposition")
@Composable
fun TopBarTitleWithOptions(
    title: String = "",
    options:List<OptionsDowns>,
    onClick:() -> Unit,
    navController: NavHostController,
    form:String,
    users: Users?
){
    Column{

        TopAppBar(
            modifier= Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AzulVistony202,
                        AzulVistony201
                    )
                )),
            title = {
                if(users!=null)
                {
                    when (form)
                    {
                        "TaskManagementScreen"->{
                            Icon(
                                Icons.Filled.ArrowBack,
                                tint = Color.White,
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    //navController.navigate("Dashboard/userName=${users?.FirstName}&userWhs=AN001&userId=${users?.EmployeeId}&location=${users?.Branch}")
                                    //navController.popBackStack()
                                }
                            )
                            Spacer(modifier=Modifier.padding(10.dp))
                        }
                    }
                }

                Text(
                    text = title,
                    color= Color.White
                )
            },
            actions = {
                DropdownMenuMerchandise(
                    options=options,
                    onClick={
                        onClick()
                    })
            },
            backgroundColor = Color.Transparent,
            elevation = 0.dp
        )
    }
}

@Composable
private fun DropdownMenuMerchandise(options:List<OptionsDowns>, onClick: () -> Unit) {
    val expanded = remember { mutableStateOf(false) }

    Box(
        Modifier
            .wrapContentSize(Alignment.TopEnd)
    ) {
        IconButton(onClick = {
            expanded.value = true
        }) {
            Icon(
                Icons.Filled.MoreVert,
                contentDescription = "More Menu",
                tint = Color.White
            )
        }
    }

    DropdownMenu(
        modifier=Modifier.fillMaxWidth(0.5f),
        expanded = expanded.value,
        onDismissRequest = { expanded.value = false },
    ) {

        options.forEachIndexed { i,it ->
            DropdownMenuItem(onClick = {
                expanded.value = false
                onClick()
            }) {
                Row{
                    Icon(
                        painter = painterResource(id = it.icon),
                        tint= AzulVistony202,
                        contentDescription = null
                    )
                    Text("  ${it.text}")
                }
            }

            if(options.size>2){
                Divider()
            }
        }

    }
}


@Composable
fun TopBarDashboard(
    title: String = "",
    navController:NavController,
    loginViewModel: LoginViewModel,
    context: Context,
    //activity: Activity
){

    var viewOrNot by remember { mutableStateOf(false ) }

    if(viewOrNot){
        CustomDialogSignOut(
            onPress={
                viewOrNot=!viewOrNot
                if(it){
                    Log.e("REOS","TopBar-TopBarDashboard-entroPress")
                    //navController.navigate("Login/status=logout")
                    loginViewModel.onLogout(context = context)
                    /*val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                    finish()*/

                }
            }
        )
    }

    Column{

        TopAppBar(
            modifier= Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AzulVistony202,
                        AzulVistony201
                    )
                )),
            title = {
                Text(
                    text = title,
                    color= Color.White
                )
            },
            actions = {
                IconButton(
                    onClick = {
                        viewOrNot=!viewOrNot
                    }){
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_exit_to_app_24),
                        tint=Color.White,
                        contentDescription = null
                    )
                }
            },
            backgroundColor = Color.Transparent,
            elevation = 0.dp
        )
    }
}

@Composable
fun TopBar(
    title: String = "",
    firstColor:Color=AzulVistony202,
    secondColor:Color=AzulVistony201,
    navController: NavHostController?=null,
    form:String=""
){
    Column{

        TopAppBar(
            modifier= Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        firstColor,
                        secondColor
                    )
                )),
            title = {
                when (form)
                {
                    "StockTransferDetailScreen",
                    "InventoryDetailScreen"
                    ->{
                        Icon(
                            Icons.Filled.ArrowBack,
                            tint = Color.White,
                            contentDescription = null,
                            modifier = Modifier.clickable { navController?.navigate("TaskManager") }
                        )
                        Spacer(modifier=Modifier.padding(10.dp))
                    }
                }
                Text(
                    text = title,
                    color= Color.White
                )

            },
            backgroundColor = Color.Transparent,
            elevation = 0.dp
        )
    }
}

@Composable
fun TopBarWithBackPress(title: String,onButtonClicked: () -> Unit) {
    TopAppBar(
        elevation = 0.dp,
        modifier=Modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    AzulVistony202,
                    AzulVistony201
                )
            )
        ),
        title = {
            Text(
                text = title,
                color= Color.White
            )
        },
        navigationIcon = {IconButton(onClick = { onButtonClicked() } ) {Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White)}},
        backgroundColor = Color.Transparent
    )
}