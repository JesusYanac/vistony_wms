package com.vistony.wms.component

import ButtonView
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vistony.wms.R
import com.vistony.wms.ui.theme.RedVistony
import com.vistony.wms.ui.theme.typography

@Composable
fun DialogFindItemView(
    tittle: String,
    subtittle:String,
    onClickCancel: () -> Unit,
    onClickAccept: () -> Unit,
    statusButtonAccept:Boolean,
    statusButtonIcon:Boolean,
    context: Context,
    content: @Composable () -> Unit,
) {
    Log.e("REOS", "DialogView-DialogView-tittle: " +tittle)
    Log.e("REOS", "DialogView-DialogView-subtittle: " +subtittle)
    Dialog(
        onDismissRequest = onClickCancel
    ) {

        Box(
            modifier = Modifier
            //.height(400.dp)
        ) {
            Column(
                modifier = Modifier
            ) {
                Spacer(modifier = Modifier.height(0.dp))
                Box(
                    modifier = Modifier
                        //.height(920.dp)
                        .fillMaxSize()
                        .background(
                            //color = MaterialTheme.colorScheme.onPrimary,
                            color = MaterialTheme.colors.onPrimary,
                            shape = RoundedCornerShape(25.dp, 10.dp, 25.dp, 10.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(15.dp))
                        Row() {
                            TableCell(text= tittle!!,color= Color.Black, title = true, weight = 1f, textAlign = TextAlign.Center)
                        }
                        if(!subtittle.equals(""))
                        {
                            Spacer(modifier = Modifier.height(5.dp))
                            Row() {
                                TableCell(
                                    text = subtittle!!,
                                    color = Color.Gray,
                                    title = false,
                                    weight = 1f,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Box(
                            modifier = Modifier.fillMaxSize()
                                //.size(550.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topEndPercent = 10,
                                        bottomStartPercent = 10, topStartPercent = 10, bottomEndPercent = 10
                                    )
                                )
                        )
                        {
                            content.invoke()
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row() {
                            ButtonView(
                                description = "Cerrar",
                                OnClick = onClickCancel,
                                status = true,
                                context=context,
                                backGroundColor = RedVistony,
                                textColor = Color.White
                            )
                            if(statusButtonAccept)
                            {
                                Spacer(modifier = Modifier.width(10.dp))
                                ButtonView(
                                    description = "Aceptar",
                                    OnClick = onClickAccept,
                                    status = true,
                                    context=context,
                                    backGroundColor = RedVistony,
                                    textColor = Color.White
                                )
                            }
                        }
                    }
                }
            }/*
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(
                        RoundedCornerShape(
                            topEndPercent = 50,
                            bottomStartPercent = 50, topStartPercent = 50, bottomEndPercent = 50
                        )
                    )
                    .background(
                        Color.White
                    )
                    .align(Alignment.TopCenter)
            )

            Image(
                painter = painterResource(id = R.mipmap.logo_vistony),
                contentDescription = "Google Maps", // decorative
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    //Set Image size to 40 dp
                    .size(120.dp)
                    .align(Alignment.TopCenter)
                    .padding(0.dp, 20.dp, 0.dp, 0.dp)
            )*/
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Editext(
    status: Boolean,
    text: MutableState<String>,
    placeholder:String,
    label:String,
    painter: Painter,
    keyboardType: KeyboardType,
    statusMaxCharacter:Boolean=true
){
    val keyboardController = LocalSoftwareKeyboardController.current
    val maxCharacters = 254 // Establece el límite máximo de caracteres

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.padding(10.dp).fillMaxWidth()
    ) {
        OutlinedTextField(
            enabled= status,
            singleLine=false,
            value = text.value,
            onValueChange =
            {
                if (it.length <= maxCharacters)
                {
                    text.value = it
                }
            },
            placeholder = {
                Text(text = placeholder, fontSize = 14.sp)
            },
            label = { Text(label, fontSize = 14.sp) },
            trailingIcon = { if (statusMaxCharacter) {Icon(painter = painter, contentDescription = null, tint = RedVistony)} },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType,imeAction = ImeAction.Go ),
            keyboardActions = KeyboardActions(
                onGo = {
                    keyboardController?.hide()
                },
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (statusMaxCharacter)
        {
            Text(
                text = "${text.value.length}/$maxCharacters",
                style = MaterialTheme.typography.caption,
                color = if (text.value.length > maxCharacters) Color.Red else Color.Black,
                modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp, 0.dp)
            )
        }
    }
}

@Composable
fun ButtonCircle(
    OnClick:() ->Unit
    ,size: DpSize = DpSize(40.dp,40.dp)
    ,color: Color= RedVistony
    ,roundedCornerShape: RoundedCornerShape= CircleShape
    ,content: @Composable () -> Unit
){
    Box(
        modifier = Modifier
            .size(size)
            .background(color, roundedCornerShape)
            .clickable { OnClick() }
        ,
        contentAlignment = Alignment.Center
    ) {
        content.invoke()
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    title:Boolean=false,
    color: Color = Color.Unspecified,
    textAlign: TextAlign = TextAlign.Center,
    fontSise: TextUnit = 15.sp
) {
    if (title) {
        Text(
            text = text,
            modifier =
            Modifier
                .weight(weight)
                .padding(2.dp),
            fontSize = fontSise,
            //fontWeight = FontWeight.Bold,
            color = color,
            textAlign = textAlign,
            style = typography.h5,
        )
    } else {
        Text(
            text = text,
            modifier = Modifier
                .weight(weight)
                .padding(2.dp),
            color = color,
            fontSize = fontSise,
            textAlign = textAlign, style = typography.body1
        )
    }
}

@Composable
fun CardView(
    cardtTittle: @Composable () -> Unit,
    cardContent: @Composable () -> Unit,
    cardBottom: @Composable () -> Unit
){
    Card(
        modifier = Modifier
            .padding(10.dp),
        elevation = 10.dp
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        )
        {
            cardtTittle()
            cardContent()
            cardBottom()
        }
    }
}