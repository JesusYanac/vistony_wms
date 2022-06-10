package com.vistony.wms.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import com.vistony.wms.R
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vistony.wms.model.ErrorNetwork
import com.vistony.wms.ui.theme.RedVistony201


@Composable
fun CatchErrorView(data: ErrorNetwork, execute:() -> Unit, content: @Composable() () -> Unit){
    if(data.title.isNotEmpty() && data.text.isNotEmpty()){
        Column(
            modifier = Modifier.fillMaxSize().background(Color.White)
        ){
            Box(
                modifier = Modifier.padding(bottom = 60.dp),
                contentAlignment = Alignment.BottomCenter
            ){

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(text = data.title, color = RedVistony201, textAlign = TextAlign.Center, fontSize = 20.sp)
                    Text(
                        text = data.text,
                        color = Color.Gray,
                        modifier = Modifier.padding(top=10.dp,start=30.dp,end=30.dp),
                        textAlign = TextAlign.Center
                    )
                }
                Button(
                    enabled= true,
                    shape= RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(0.8f),
                    onClick = {
                        execute()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = RedVistony201
                    )
                ){
                    Text(color= Color.White,text = "Volver a Intentar", fontSize = 20.sp)
                }
            }

        }
    }else{
        content()
    }
}