package com.vistony.wms.component

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
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
import com.vistony.wms.enum_.OptionsInventory
import com.vistony.wms.enum_.TypeReadSKU
import com.vistony.wms.util.Routes

@SuppressLint("PermissionLaunchedDuringComposition")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TopBarTitle(
    title: String = "",
    status:Boolean=true,
    permission:PermissionState,
    onClick:(TypeReadSKU) -> Unit
){
    Column{

        val bodyContent = remember { mutableStateOf(TypeReadSKU.HANDHELD) }

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

                TopAppBarDropdownMenu(bodyContent)


                if (bodyContent.value == TypeReadSKU.CAMERA){
                    if(!permission.hasPermission){
                        permission.launchPermissionRequest()
                    }
                }

                onClick(bodyContent.value)

               /* IconButton(
                    enabled = status,
                    onClick = {

                    if (typeRead == TypeReadSKU.KEYBOARD){
                        if(permission.hasPermission){
                            typeRead=TypeReadSKU.CAMERA
                        }else{
                            permission.launchPermissionRequest()
                        }

                    }else{
                        typeRead=TypeReadSKU.KEYBOARD
                    }

                    onClick(typeRead)
                }){

                    var iconOption=when(typeRead){
                        TypeReadSKU.KEYBOARD ->{
                            R.drawable.ic_baseline_keyboard_24
                        }
                        TypeReadSKU.CAMERA->{
                            R.drawable.ic_baseline_camera_alt_24
                        }
                        TypeReadSKU.HANDHELD->{

                        }
                    }

                    Icon(
                        painter = painterResource(id = iconOption),
                        tint= if(status){Color.White}else{Color.Gray},
                        contentDescription = null
                    )
                },
              */

            },
            backgroundColor = Color.Transparent,
            elevation = 0.dp
        )
    }
}


@Composable
fun TopAppBarDropdownMenu(bodyContent: MutableState<TypeReadSKU>) {
    val expanded = remember { mutableStateOf(false) }

    val listOptions = listOf(
        OptionsInventory(TypeReadSKU.KEYBOARD,"Manual",R.drawable.ic_baseline_keyboard_24),
        OptionsInventory(TypeReadSKU.CAMERA,"Camara",R.drawable.ic_baseline_camera_alt_24),
        OptionsInventory(TypeReadSKU.HANDHELD,"Handheld",R.drawable.ic_baseline_qr_code_scanner_24),
    )

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
            DropdownMenuItem(onClick = {
                expanded.value = false
                bodyContent.value = it.type
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
    title: String = ""
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
            backgroundColor = Color.Transparent,
            elevation = 0.dp
        )
    }
}