package com.vistony.wms.component

import android.content.Context
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.common.util.concurrent.ListenableFuture
import com.vistony.wms.R
import com.vistony.wms.enum_.CallFor
import com.vistony.wms.enum_.TypeReadSKU
import com.vistony.wms.model.UpdateLine
import com.vistony.wms.screen.CameraForm
import com.vistony.wms.ui.theme.AzulVistony202
import com.vistony.wms.viewmodel.WarehouseViewModel

@Composable
fun CustomProgressDialog(text:String){
    Dialog(
        onDismissRequest = { false },
        DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(140.dp)
                .background(Color.White, shape = RoundedCornerShape(8.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                CircularProgressIndicator(color= AzulVistony202)
                Text(
                    modifier = Modifier.padding(top=10.dp),
                    text = text,
                    textAlign = Center,
                    color= AzulVistony202
                )
            }

        }
    }
}

@Composable
fun CustomDialogQuestion(openDialog:(Boolean)->Unit){

    AlertDialog(
        onDismissRequest = {
            openDialog(false)
        },
        title = {
            Text(text = "Eliminar línea")
        },
        text = {
            Text(text="¿Está seguro de que desea eliminar este línea en el documento actual?")
        },
        confirmButton = {
            Button(
                onClick = { openDialog(false) }
            ) {
                Text("Cancelar")
            }
        },
        dismissButton = {
            Button(
                onClick = { openDialog(true) },
                colors= ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("Eliminar",color=Color.White)
            }
        }
    )
}

open class FlagDialog(
    var status:Boolean=false,
    var flag:String=""
)

@Composable
fun CustomDialogResendOrClose(title:String,openDialog:(Boolean)->Unit,flag:String){

    AlertDialog(
        onDismissRequest = {
            openDialog(false)
        },
        title = {
            Text(text = if(flag=="Close"){title}else{"Reenviar a Sap"})
        },
        text = {
            Text(text=if(flag=="Close"){"¿Está seguro de cerrar esta ficha?"}else{"¿Está seguro de reenviar a Sap?"})
        },
        confirmButton = {
            Button(
                onClick = { openDialog(false) }
            ) {
                Text("Cancelar")
            }
        },
        dismissButton = {
            Button(
                onClick = { openDialog(true) },
                colors= ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text( if(flag=="Close"){title}else{"Reenviar a Sap"} ,color=Color.White)
            }
        }
    )
}

@Composable
fun CustomDialogCreateConteo(titulo:String,mensaje:String,openDialog:(Boolean)->Unit){

    AlertDialog(
        onDismissRequest = {
            openDialog(false)
        },
        title = {
            Text(text = titulo)
        },
        text = {
            Text(text=mensaje)
        },
        confirmButton = {
            Button(
                onClick = { openDialog(false) }
            ) {
                Text("Cancelar")
            }
        },
        dismissButton = {
            Button(
                onClick = { openDialog(true) },
                colors= ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("Crear",color=Color.White)
            }
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CustomDialogChangeNumber(whs:String,context: Context, warehouseViewModel: WarehouseViewModel,typeRead: TypeReadSKU, binLocation:String, itemName:String, location:String?, value:String,valueLote:String, newValue:(UpdateLine)->Unit){

    var locationTemp by remember { mutableStateOf( TextFieldValue( if(location.isNullOrEmpty()){""}else{location} )) }
    var textNumber by remember { mutableStateOf(value) }
    var textLote by remember { mutableStateOf(valueLote) }
    val keyboardController = LocalSoftwareKeyboardController.current

    if(binLocation.isNotEmpty()){
        locationTemp=TextFieldValue(binLocation)
    }

    AlertDialog(
        onDismissRequest = {
            locationTemp=TextFieldValue("")
            newValue(UpdateLine(0.0,""))
        },
        title = {
            Column{
                Text(text = "$itemName ")
                Text(text = " ")
            }
        },
        text = {

            Column{
                val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> = ProcessCameraProvider.getInstance(context)
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                if(typeRead==TypeReadSKU.CAMERA){
                    CameraForm(
                        whs=whs,
                        calledFor=CallFor.Location,
                        context = context,
                        warehouseViewModel = warehouseViewModel,
                        cameraProviderFuture = cameraProviderFuture,
                        cameraProvider = cameraProvider
                    )
                }else{
                    cameraProvider.unbindAll()
                }

                OutlinedTextField(
                    enabled= valueLote.isNullOrEmpty(),
                    singleLine=true,
                    value = textLote,
                    onValueChange = { textLote = it },
                    placeholder = {
                        Text("Ingresar Lote")
                    },
                    trailingIcon = { Icon(painter = painterResource(id = R.drawable.ic_baseline_box_24), contentDescription = null, tint = AzulVistony202) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,imeAction = ImeAction.Go ),
                    keyboardActions = KeyboardActions(
                        onGo = {keyboardController?.hide()}
                    )
                )

                Text(text = " ")

                OutlinedTextField(
                    singleLine=true,
                    value = textNumber,
                    onValueChange = { textNumber = it },
                    placeholder = {
                        Text("Ingresar Cantidad")
                    },
                    trailingIcon = { Icon(painter = painterResource(id = R.drawable.ic_baseline_numbers_24), contentDescription = null, tint = AzulVistony202) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,imeAction = ImeAction.Go ),
                    keyboardActions = KeyboardActions(
                        onGo = {keyboardController?.hide()}
                    )
                )
                Text(text = " ")

                OutlinedTextField(
                    enabled=true,
                    singleLine=true,
                    value = locationTemp,
                    onValueChange = { locationTemp = it },
                    placeholder = {
                        Text("Ingresar Ubicación")
                    },
                    trailingIcon = { Icon(painter = painterResource(id = R.drawable.ic_baseline_rack_24), contentDescription = null, tint = AzulVistony202) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text,imeAction = ImeAction.Go ),
                    keyboardActions = KeyboardActions(
                        onGo = {keyboardController?.hide()}
                    )
                )

            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try{
                        keyboardController?.hide()
                        val numeric:Double=textNumber.toDouble()

                        newValue(
                            UpdateLine(
                                count=numeric,
                                locationName=locationTemp.text,
                                locationCode=binLocation,
                                lote=textLote
                            )
                        )
                    }catch(e:Exception){
                        textNumber="1"
                    }
                },
                colors= ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("Confirmar",color=Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    locationTemp=TextFieldValue("")
                    newValue(UpdateLine(0.0,""))
                }
            ) {
                Text("Cancelar")
            }
        }
    )
}

/*
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CustomDialogChangeOnlyNumber(context: Context, warehouseViewModel: WarehouseViewModel,typeRead: TypeReadSKU, itemName:String,value:String,valueLote:String, newValue:(UpdateLineMerchandise)->Unit){

    var textNumber by remember { mutableStateOf(value) }
    var textLote by remember { mutableStateOf(valueLote) }
    val keyboardController = LocalSoftwareKeyboardController.current

    AlertDialog(
        onDismissRequest = {
            newValue(UpdateLineMerchandise(0.0,""))
        },
        title = {
            Column{
                Text(text = "$itemName ")
                Text(text = " ")
            }
        },
        text = {

            Column{
                val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> = ProcessCameraProvider.getInstance(context)
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                if(typeRead==TypeReadSKU.CAMERA){
                    CameraForm(
                        calledFor=CallFor.Location,
                        context = context,
                        warehouseViewModel = warehouseViewModel,
                        cameraProviderFuture = cameraProviderFuture,
                        cameraProvider = cameraProvider
                    )
                }else{
                    cameraProvider.unbindAll()
                }

                OutlinedTextField(
                    enabled= valueLote.isNullOrEmpty(),
                    singleLine=true,
                    value = textLote,
                    onValueChange = { textLote = it },
                    placeholder = {
                        Text("Ingresar Lote")
                    },
                    trailingIcon = { Icon(painter = painterResource(id = R.drawable.ic_baseline_box_24), contentDescription = null, tint = AzulVistony202) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,imeAction = ImeAction.Go ),
                    keyboardActions = KeyboardActions(
                        onGo = {keyboardController?.hide()}
                    )
                )

                Text(text = " ")

                OutlinedTextField(
                    singleLine=true,
                    value = textNumber,
                    onValueChange = { textNumber = it },
                    placeholder = {
                        Text("Ingresar Cantidad")
                    },
                    trailingIcon = { Icon(painter = painterResource(id = R.drawable.ic_baseline_numbers_24), contentDescription = null, tint = AzulVistony202) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,imeAction = ImeAction.Go ),
                    keyboardActions = KeyboardActions(
                        onGo = {keyboardController?.hide()}
                    )
                )

            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try{
                        keyboardController?.hide()
                        val numeric:Double=textNumber.toDouble()

                        newValue(UpdateLineMerchandise(numeric,"locationTemp",textLote))
                    }catch(e:Exception){
                        textNumber="1"
                    }
                },
                colors= ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("Confirmar",color=Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    newValue(UpdateLineMerchandise(0.0,""))
                }
            ) {
                Text("Cancelar")
            }
        }
    )
}
*/

@Composable
fun CustomDialogSignOut(onPress:(Boolean)->Unit){
    AlertDialog(
        onDismissRequest = {
            onPress(false)
        },
        title = {
            Text(text = "Cerrar sesión")
        },
        text = {
            Column{
                Text(text="¿Está seguro en cerrar sesión en este dispositivo?")
                Spacer(modifier=Modifier.height(10.dp))
                Text(text="Al \"continuar\" el sistema quitara toda su información de este dispositivo",color=Color.Gray)
            }
        },
        confirmButton = {
            Button(
                onClick = { onPress(false) }
            ) {
                Text("Cancelar")
            }
        },
        dismissButton = {
            Button(
                enabled = false,
                onClick = {
                    onPress(true)
                },
                colors= ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("Continuar",color=Color.White)
            }
        }
    )
}
