package com.vistony.wms.component

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vistony.wms.model.Inventory
import com.vistony.wms.model.Login
import com.vistony.wms.ui.theme.AzulVistony201
import com.vistony.wms.ui.theme.AzulVistony202
import com.vistony.wms.ui.theme.RedVistony202
import com.vistony.wms.viewmodel.LoginViewModel


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun formLogin(loginViewModel: LoginViewModel, context:Context, open: (BottomSheetScreen) -> Unit, close:() ->Unit){

    var codeWorker by remember { mutableStateOf(TextFieldValue("")) }
    var passwordWorker by remember { mutableStateOf(TextFieldValue("")) }
    var location by remember { mutableStateOf(TextFieldValue("PERÚ")) }
    var locationVal by remember { mutableStateOf("PE") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier=Modifier.padding(20.dp).verticalScroll(rememberScrollState())
    ){

        Box{

            OutlinedTextField(
                enabled=false,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.DarkGray,
                    unfocusedBorderColor = Color.DarkGray,
                    disabledTextColor = Color.DarkGray,
                    disabledLabelColor = Color.DarkGray
                ),
                maxLines=1,
                singleLine = true,
                value = location,
                trailingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 10.dp),
                label = { Text(text = "Locación") },
                placeholder = { Text(text = "") },
                onValueChange = {
                    location=it
                }
            )

            Spacer(
                modifier = Modifier.matchParentSize().background(Color.Transparent)
                    .clickable(
                        onClick = {
                            open(
                                BottomSheetScreen.SelectCountryModal(selected = {
                                    location=TextFieldValue(it.text)
                                    locationVal=it.value
                                    close()
                                })
                            )
                        }
                    )
            )
        }

        OutlinedTextField(
            enabled=true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.DarkGray,
                unfocusedBorderColor = Color.DarkGray,
                disabledTextColor = Color.DarkGray,
                disabledLabelColor = Color.DarkGray
            ),
            maxLines=1,
            singleLine = true,
            value = codeWorker,
            trailingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 10.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword,imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(
                onGo = {keyboardController?.hide()}
            ),
            label = { Text(text = "Codigo de colaborador") },
            placeholder = { Text(text = "") },
            onValueChange = {
                codeWorker=it
            }
        )

        OutlinedTextField(
            enabled=true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.DarkGray,
                unfocusedBorderColor = Color.DarkGray,
                disabledTextColor = Color.DarkGray,
                disabledLabelColor = Color.DarkGray
            ),
            maxLines=1,
            singleLine = true,
            value = passwordWorker,
            trailingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 10.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password,imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(
                onGo = {keyboardController?.hide()}
            ),
            label = { Text(text = "Contraseña") },
            placeholder = { Text(text = "") },
            onValueChange = {
                passwordWorker=it
            }
        )

        Spacer(modifier=Modifier.padding(20.dp))

        Button(
            enabled= true,
            shape= RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            onClick = {

                if(codeWorker.text.isNotEmpty()){
                    if(passwordWorker.text.isNotEmpty()){
                        loginViewModel.login(Login("${locationVal}-${codeWorker.text}", passwordWorker.text,locationVal))
                    }else{
                        Toast.makeText(context, "Ingrese una contraseña valida", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(context, "Ingrese un codigo de colaborado", Toast.LENGTH_SHORT).show()
                }

            },
            colors = ButtonDefaults.textButtonColors(
                backgroundColor = AzulVistony201
            )
        ){
            Text(color= Color.White,text = " Ingresar", fontSize = 20.sp)
        }
    }
}

@Composable
fun formCreateInventoryHeader(context: Context,onPress: (Inventory) -> Unit, open: (BottomSheetScreen) -> Unit, close:() ->Unit){
    Column(
        modifier= Modifier.padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ){

        var nombre by remember { mutableStateOf(TextFieldValue("")) }
        var tipo by remember { mutableStateOf(TextFieldValue("Simple")) }
        var messageError by remember { mutableStateOf("") }
        var location by remember { mutableStateOf(TextFieldValue("")) }

        OutlinedTextField(
            enabled=true,
            singleLine=true,
            maxLines=1,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.DarkGray,
                unfocusedBorderColor = Color.DarkGray,
                disabledTextColor = Color.DarkGray,
                disabledLabelColor = Color.DarkGray
            ),
            value = nombre,
            trailingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 10.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            label = { Text(text = "Nombre del recuento") },
            placeholder = { Text(text = "") },
            onValueChange = {
                nombre=it
            }
        )

        Box{
            OutlinedTextField(
                enabled=false,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.DarkGray,
                    unfocusedBorderColor = Color.DarkGray,
                    disabledTextColor = Color.DarkGray,
                    disabledLabelColor = Color.DarkGray
                ),
                value = location,
                trailingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 10.dp),

                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                label = { Text(text = "Almacén") },
                placeholder = { Text(text = "") },
                onValueChange = {}
            )

            Spacer(
                modifier = Modifier.matchParentSize().background(Color.Transparent)
                    .clickable(
                        onClick = {
                            open(
                                BottomSheetScreen.SelectWarehouseModal(
                                    context=context,
                                    selected = {

                                    location=TextFieldValue(it.code)
                                    close()
                                })
                            )
                        }
                    )
            )
        }


        OutlinedTextField(
            enabled=false,
            singleLine=true,
            maxLines=1,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.DarkGray,
                unfocusedBorderColor = Color.DarkGray,
                disabledTextColor = Color.DarkGray,
                disabledLabelColor = Color.DarkGray
            ),
            value = tipo,
            trailingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 10.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            label = { Text(text = "Tipo de recuento") },
            placeholder = { Text(text = "") },
            onValueChange = {
                tipo=it
            }
        )

        Row(
           modifier = Modifier.fillMaxWidth(),// padding(8.dp).clickable(){},
            horizontalArrangement=Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isChecked = remember { mutableStateOf(true) }

            Checkbox(
                // modifier=Modifier.padding(top=15.dp),
                checked = isChecked.value,
                onCheckedChange = {
                    isChecked.value = it
                },
                enabled=false,
                colors = CheckboxDefaults.colors(
                    checkedColor = AzulVistony202,
                    uncheckedColor = Color.DarkGray,
                    checkmarkColor =  Color.White,

                    )
            )
            Text(color=Color.Gray,text = "Conteo ciego", modifier = Modifier.padding(start = 10.dp))
        }

       Text(
            text=messageError,
            color= RedVistony202
       )


        Spacer(modifier= Modifier.padding(10.dp))

        Button(
            enabled= true,
            shape= RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            onClick = {

                if(nombre.text.isEmpty()){
                    messageError="*Es necesario que se ingrese un nombre al recuento."
                }else{
                    if(location.text.isEmpty()){
                        messageError="*Es necesario que su usuario este asignado a un almacen."
                    }else{
                        messageError=""

                        onPress(

                            Inventory(
                                type=tipo.text,
                                name=nombre.text,
                                wareHouse = location.text,
                            )
                        )
                    }
                }
            },
            colors = ButtonDefaults.textButtonColors(
                backgroundColor = AzulVistony201
            )
        ){
            Text(color= Color.White,text = " Crear", fontSize = 20.sp)
        }

    }
}