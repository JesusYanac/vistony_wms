package com.vistony.wms.component

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.vistony.wms.ui.theme.AzulVistony201
import com.vistony.wms.ui.theme.AzulVistony202
import com.vistony.wms.R
import com.vistony.wms.enum_.*
import com.vistony.wms.util.Routes

@SuppressLint("PermissionLaunchedDuringComposition")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TopBarTitleCamera(
    title: String = "",
    status:String="",
    objType:Int=0,
    permission:PermissionState,
    onClick:(TypeReadSKU) -> Unit
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
                Text(
                    text = title,
                    color= Color.White
                )
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
                })
            },
            backgroundColor = Color.Transparent,
            elevation = 0.dp
        )
    }
}


@Composable
private fun DropdownMenuCamera(objType:Int,status:String,bodyContent: (TypeReadSKU)->Unit) {
    val expanded = remember { mutableStateOf(false) }

    val listOptions: MutableList<OptionsInventory> = mutableListOf()

    when(objType){
        67->{}
        671->{}
        22->{ //Orden de Compra
            if(status!="OrigenCerrado"){
                listOptions.add(OptionsInventory(TypeReadSKU.KEYBOARD,"Manual",R.drawable.ic_baseline_keyboard_24))
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

        //if(objType=="StockTransfer"){
            Divider()
            Text(" Cambiar estados", color = Color.LightGray)
            DropdownMenuItem(onClick={
                expanded.value = false
                bodyContent(if(status=="OrigenCerrado"){TypeReadSKU.CERRAR_FICHA}else{TypeReadSKU.CERRAR_ORIGEN})
            }){
                Row{
                    Icon(
                        imageVector = Icons.Filled.Close,
                        tint= AzulVistony202,
                        contentDescription = null
                    )
                    Text(if(status=="OrigenCerrado"){"  Cerrar Ficha"}else{"  Cerrar Origen"})
                }
            }
        //}

    }
}


@SuppressLint("PermissionLaunchedDuringComposition")
@Composable
fun TopBarTitleWithOptions(
    title: String = "",
    options:List<OptionsDowns>,
    onClick:() -> Unit
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
    navController:NavController
){

    var viewOrNot by remember { mutableStateOf(false ) }

    if(viewOrNot){
        CustomDialogSignOut(
            onPress={
                viewOrNot=!viewOrNot
                if(it){
                    navController.navigate("Login/status=logout")
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
    secondColor:Color=AzulVistony201
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