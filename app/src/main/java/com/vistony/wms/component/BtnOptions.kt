package com.vistony.wms.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import com.vistony.wms.model.*

@Composable
fun btnBsHeaderTask(status:String,taskMngmtDataForm:TaskMngmtDataForm,/*numSerie:String,numCorrelativo:String,comentario:String,*/onCancel:()->Unit,onContinue:(TaskMngmtDataForm)->Unit){
    val focusManager = LocalFocusManager.current
    var haveError by remember { mutableStateOf("") }

    if(haveError.isNotEmpty()){
        Text(haveError,color=Color.Red)
        Text("")
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ){
        Button(
            colors= ButtonDefaults.buttonColors(backgroundColor = Color.Gray),
            onClick = {
                if(taskMngmtDataForm.serie.length in 3..5){
                    if(taskMngmtDataForm.correlativo.length in 8..10){
                        haveError=""
                        focusManager.clearFocus()
                        onContinue(
                            taskMngmtDataForm
                        )
                    }else{
                        haveError="*Es necesario que el número del correlativo tenga más de 8 dígitos"
                    }
                }else{
                    haveError="*Es necesario que el número de serie tenga más de 3 dígitos"
                }
            }){
            Text(
                text = when(status){"Terminado"->{"Ver"} "Asignado"->{"Iniciar Tarea"} "En Curso"->{ "Continuar"} else->{"N/A"}},
                color= Color.White
            )
        }
        Button( onClick = {
            haveError=""
            focusManager.clearFocus()
            onCancel()
        }){
            Text(
                text = "Cancelar"
            )
        }
    }
}