package com.vistony.wms.component

import android.util.Log
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
import com.vistony.wms.R
import com.vistony.wms.model.UpdateLine
import com.vistony.wms.ui.theme.AzulVistony202
import java.util.*

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
            Text(text = "Borrar artículo")
        },
        text = {
            Text(text="¿Está seguro de que desea eliminar este artículo de la ficha de recuento?")
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


@Composable
fun CustomDialogSendSap(openDialog:(Boolean)->Unit){

    AlertDialog(
        onDismissRequest = {
            openDialog(false)
        },
        title = {
            Text(text = "Enviar a SAP")
        },
        text = {
            Text(text="¿Está seguro de cerrar la ficha y enviar a SAP?")
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
                Text("Enviar",color=Color.White)
            }
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CustomDialogChangeNumber(binLocation:String,itemName:String,location:String="", value:String, newValue:(UpdateLine)->Unit){

    Log.e("JEPICAME","REGRESO DE NEUVO location: "+location+" binLocation:"+binLocation  )

    var locationTemp by remember { mutableStateOf( TextFieldValue(location)) }
    var textNumber by remember { mutableStateOf(value) }
    val keyboardController = LocalSoftwareKeyboardController.current

    if(binLocation.isNotEmpty()){
        locationTemp=TextFieldValue(binLocation)
    }

    Log.e("JEPICAME","---"+locationTemp.text )


    AlertDialog(
        onDismissRequest = {
            locationTemp=TextFieldValue("")
            newValue(UpdateLine(0,""))
        },
        title = {
            Column{
                Text(text = "$itemName ")
                Text(text = " ")
            }
        },
        text = {

            Column{
                OutlinedTextField(
                    singleLine=true,
                    value = textNumber,
                    onValueChange = { textNumber = it },
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
                        val numeric:Long=textNumber.toLong()

                        newValue(UpdateLine(numeric,locationTemp.text))
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
                    newValue(UpdateLine(0,""))
                }
            ) {
                Text("Cancelar")
            }
        }
    )
}

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
