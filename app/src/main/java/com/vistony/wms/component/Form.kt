package com.vistony.wms.component

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vistony.wms.BuildConfig
import com.vistony.wms.R
import com.vistony.wms.model.*
import com.vistony.wms.num.TypeCode
import com.vistony.wms.ui.theme.*
import com.vistony.wms.util.DatasourceSingleton
import com.vistony.wms.viewmodel.LoginViewModel
import com.vistony.wms.viewmodel.SuggestionViewModel
import com.vistony.wms.viewmodel.WarehouseViewModel
import java.time.LocalDateTime
import java.util.*


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun formLogin(loginViewModel: LoginViewModel, context:Context, open: (BottomSheetScreen) -> Unit, close:() ->Unit){

    var codeWorker by remember { mutableStateOf(TextFieldValue("")) }
    var passwordWorker by remember { mutableStateOf(TextFieldValue("")) }
    var location by remember { mutableStateOf(TextFieldValue("PERÚ")) }
    var locationVal by remember { mutableStateOf("PE") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier= Modifier
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
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
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Transparent)
                    .clickable(
                        onClick = {
                            open(
                                BottomSheetScreen.SelectCountryModal(selected = {

                                    location = TextFieldValue(it.text)
                                    Log.e("logindebug", "text: "+it.text)
                                    Log.e("logindebug", "value: "+it.value)

                                    locationVal = it.value

                                    Log.e("logindebug", "nuevo valor: "+locationVal)

                                    /*if (locationVal == "RO") {
                                        locationVal = "PE"
                                    }*/

                                    when(it.value){
                                        "PE" -> { // Perú
                                            DatasourceSingleton.updateApiUrl(BuildConfig.API_URL_PE_PRO)
                                            DatasourceSingleton.updatePort(8082)
                                            DatasourceSingleton.updateDatabaseKey("")
                                        }
                                        "BO" -> { // Bolivia
                                            DatasourceSingleton.updateApiUrl(BuildConfig.API_URL_BO_PRO)
                                            DatasourceSingleton.updatePort(8082)
                                            DatasourceSingleton.updateDatabaseKey("")
                                        }
                                        "CL" -> { // Chile
                                            DatasourceSingleton.updateApiUrl(BuildConfig.API_URL_CL_PRO)
                                            DatasourceSingleton.updatePort(8082)
                                            DatasourceSingleton.updateDatabaseKey("")
                                        }
                                        "PY" -> { // Paraguay
                                            DatasourceSingleton.updateApiUrl(BuildConfig.API_URL_PY_PRO)
                                            DatasourceSingleton.updatePort(8082)
                                            DatasourceSingleton.updateDatabaseKey("")
                                        }
                                        "EC" -> { // Ecuador
                                            DatasourceSingleton.updateApiUrl(BuildConfig.API_URL_EC_PRO)
                                            DatasourceSingleton.updatePort(8082)
                                            DatasourceSingleton.updateDatabaseKey("")
                                        }
                                        "RO" -> { // ROFALAB
                                            locationVal = "PE"

                                            Log.e("logindebug", "nuevo valor: "+locationVal)
                                            DatasourceSingleton.updateApiUrl(BuildConfig.API_URL_RO_PRO)
                                            DatasourceSingleton.updatePort(8082)
                                            DatasourceSingleton.updateDatabaseKey("")
                                        }
                                        else -> { // Perú
                                            DatasourceSingleton.updateApiUrl(BuildConfig.API_URL_PE_PRO)
                                            DatasourceSingleton.updatePort(8082)
                                            DatasourceSingleton.updateDatabaseKey("appwms-bckdu")
                                        }
                                    }

                                    loginViewModel.setSelectedDatabase(DatasourceSingleton.databaseKey)

                                    //aqui debo modificar las ip

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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,imeAction = ImeAction.Go),
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

        Spacer(modifier=Modifier.padding(10.dp))

        Button(
            enabled= true,
            shape= RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp, bottom = 5.dp),
            onClick = {

                if(codeWorker.text.isNotEmpty()){
                    if(passwordWorker.text.isNotEmpty()){
                        Log.e("logindebug","Se ha pulsado el boton")
                        Log.e("logindebug", "locationVal: "+locationVal)
                        Log.e("logindebug", "codeWorker: "+codeWorker.text)
                        Log.e("logindebug", "passwordWorker: "+passwordWorker.text)
                        Log.e("logindebug", "enviando el login")
                        loginViewModel.login(Login("${locationVal}-${codeWorker.text}", passwordWorker.text,locationVal))
                    }else{
                        Toast.makeText(context, "Ingrese una contraseña valida", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(context, "Ingrese un codigo de colaborador", Toast.LENGTH_SHORT).show()
                }

            },
            colors = ButtonDefaults.textButtonColors(
                backgroundColor = AzulVistony201
            )
        ){
            Text(color= Color.White,text = " Ingresar", fontSize = 20.sp)
        }

        Text(
            text="Vs ${BuildConfig.VERSION_NAME} ",
            modifier=Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color=Color.Gray
        )

        Spacer(modifier=Modifier.padding(5.dp))
    }
}

@Composable
fun formCreateInventoryHeader(context: Context,onPress: (InventoryPayload) -> Unit, open: (BottomSheetScreen) -> Unit, close:() ->Unit){
    Column(
        modifier= Modifier
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ){

        var nombre by remember { mutableStateOf(TextFieldValue("")) }
        var type by remember { mutableStateOf(TextFieldValue("")) }
        var typeActividad by remember { mutableStateOf(TextFieldValue("")) }
        var messageError by remember { mutableStateOf("") }
        var location by remember { mutableStateOf(WarehouseBinLocation()) }

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
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Characters
            ),
            label = { Text(text = "Nombre de la ficha") },
            placeholder = { Text(text = "") },
            onValueChange = {
                nombre=it
            }
        )
        //Text("")

        Box{
            OutlinedTextField(
                enabled=false,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.DarkGray,
                    unfocusedBorderColor = Color.DarkGray,
                    disabledTextColor = Color.DarkGray,
                    disabledLabelColor = Color.DarkGray
                ),
                value = TextFieldValue(location.warehouse.WarehouseCode),
                trailingIcon = { Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                label = { Text(text = "Almacén") },
                placeholder = { Text(text = "") },
                onValueChange = {}
            )

            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Transparent)
                    .clickable(
                        onClick = {
                            open(
                                BottomSheetScreen.SelectWarehouseModal(
                                    context = context,
                                    selected = {
                                        location = it
                                        close()
                                    })
                            )
                        }
                    )
            )
        }
        //Text("")
        Box{
            OutlinedTextField(
                enabled=false,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.DarkGray,
                    unfocusedBorderColor = Color.DarkGray,
                    disabledTextColor = Color.DarkGray,
                    disabledLabelColor = Color.DarkGray
                ),
                value = type,
                trailingIcon = { Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth(),
                   // .padding(top = 10.dp, bottom = 10.dp),

                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                label = { Text(text = "Tipo de conteo") },
                placeholder = { Text(text = "") },
                onValueChange = {}
            )

            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Transparent)
                    .clickable(
                        onClick = {
                            open(
                                BottomSheetScreen.SelectTypeModal(
                                    selected = {
                                        type = TextFieldValue(it.text)
                                        close()
                                    })
                            )
                        }
                    )
            )
        }

        /*

        Box{
            OutlinedTextField(
                enabled=false,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.DarkGray,
                    unfocusedBorderColor = Color.DarkGray,
                    disabledTextColor = Color.DarkGray,
                    disabledLabelColor = Color.DarkGray
                ),
                value = typeActividad,
                trailingIcon = { Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                label = { Text(text = "Tipo de actividad") },
                placeholder = { Text(text = "") },
                onValueChange = {}
            )

            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Transparent)
                    .clickable(
                        onClick = {
                            open(
                                BottomSheetScreen.SelectTypeModal(
                                    selected = {
                                        typeActividad = TextFieldValue(it.text)
                                        close()
                                    })
                            )
                        }
                    )
            )
        }
*/

        if(messageError.isNotEmpty()){
            Text(
                text=messageError,
                color= RedVistony202
            )
        }



        Spacer(modifier= Modifier.padding(10.dp))

        Button(
            enabled= true,
            shape= RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            onClick = {

                if(nombre.text.isEmpty()){
                    messageError="*Es necesario que se ingrese un nombre para el conteo."
                }else{
                    if(location.warehouse.WarehouseCode .isEmpty()){
                        messageError="*Es necesario seleccionar un almacen."
                    }else{
                        if(type.text.isEmpty()){
                            messageError="*Es necesario seleccionar el tipo de conteo."
                        }else{
                            messageError=""

                            onPress(
                                InventoryPayload(
                                    inventory=Inventory(
                                        type=type.text,
                                        name=nombre.text,
                                        wareHouse = location.warehouse.WarehouseCode,
                                    ),
                                    defaultLocation = location.defaultLocation
                                )
                            )
                        }
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

@Composable
fun formCreateInventoryEntryOrExit(objType:Int,context: Context, onPress: (StockTransferHeader) -> Unit, open: (BottomSheetScreen) -> Unit, close:() ->Unit){

    val listWareHouse:MutableList<Options> = mutableListOf()
    val werehouseViewModel: WarehouseViewModel = viewModel(
        factory = WarehouseViewModel.WarehouseViewModelFactory ("init","")
    )

    val warehouseValue = werehouseViewModel.almacenes.collectAsState()

    when(warehouseValue.value.status){
        ""->{}
        "cargando"->{
            CustomProgressDialog("listando almacenes...")
        }
        "ok"->{

          warehouseValue.value.warehouse.forEach{ wareHouse ->

                listWareHouse.add(Options(
                    value=wareHouse.WarehouseCode,
                    text=wareHouse.WarehouseCode+" - "+wareHouse.WarehouseName,
                    icono=R.drawable.ic_baseline_box_24
                ))
            }

            //werehouseViewModel.resetWarehouseStatus()
        }
        "vacio"->{
            Toast.makeText(context, "El maestro de almacenes esta vacío", Toast.LENGTH_SHORT).show()
            //werehouseViewModel.resetWarehouseStatus()
        }
        else->{
            Toast.makeText(context, "Ocurrio un error:\n ${warehouseValue.value.status}", Toast.LENGTH_SHORT).show()
            //werehouseViewModel.resetWarehouseStatus()
        }
    }

    /////////////////////////////////////////
    /////////////////////////////////////////
    /////////////////////////////////////////
    /////////////////////////////////////////

    Column(
        modifier= Modifier
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ){

        val fecha by remember { mutableStateOf(LocalDateTime.now()) }
        var referencia by remember { mutableStateOf("") }
        var comentario by remember { mutableStateOf("") }
        var motivoTraslado by remember { mutableStateOf( Options(value="11",text="SALIDA POR TRANSFERENCIA ENTRE ALMACENES",icono= R.drawable.ic_baseline_box_24)) }
        var almacenOrigin by remember { mutableStateOf(Options()) }
        var almacenDestine by remember { mutableStateOf(Options()) }

        var messageError by remember { mutableStateOf("") }


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
            value = fecha.toString(),
            trailingIcon = { Icon(imageVector = Icons.Default.DateRange , contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Characters
            ),
            label = { Text(text = "Fecha de movimiento") },
            placeholder = { Text(text = "") },
            onValueChange = {}
        )

        /*OutlinedTextField(
            enabled=true,
            singleLine=true,
            maxLines=1,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.DarkGray,
                unfocusedBorderColor = Color.DarkGray,
                disabledTextColor = Color.DarkGray,
                disabledLabelColor = Color.DarkGray
            ),
            value = referencia,
            trailingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Characters
            ),
            label = { Text(text = "N° de referencia") },
            placeholder = { Text(text = "") },
            onValueChange = {
                if (it.length <= 11) referencia = it
            }
        )*/

        Box{
            OutlinedTextField(
                enabled=false,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.DarkGray,
                    unfocusedBorderColor = Color.DarkGray,
                    disabledTextColor = Color.DarkGray,
                    disabledLabelColor = Color.DarkGray
                ),
                value = almacenOrigin.text,
                trailingIcon = { Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 10.dp),

                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                label = { Text(text = "Almacén origen") },
                placeholder = { Text(text = "") },
                onValueChange = {}
            )

            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Transparent)
                    .clickable(
                        onClick = {
                            open(
                                BottomSheetScreen.SelectWitOptionsModal(
                                    title = "Seleciona un almacén",
                                    listOptions = listWareHouse,
                                    selected = {
                                        almacenOrigin=it

                                        if(objType==6701){
                                            almacenDestine=it
                                        }

                                        close()
                                    })
                            )
                        }
                    )
            )
        }

        Box{
            OutlinedTextField(
                enabled=false,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.DarkGray,
                    unfocusedBorderColor = Color.DarkGray,
                    disabledTextColor = Color.DarkGray,
                    disabledLabelColor = Color.DarkGray
                ),
                value = almacenDestine.text,
                trailingIcon = { Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 10.dp),

                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                label = { Text(text = "Almacén destino") },
                placeholder = { Text(text = "") },
                onValueChange = {}
            )

            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Transparent)
                    .clickable(
                        onClick = {

                            if(objType==67){
                                open(
                                    BottomSheetScreen.SelectWitOptionsModal(
                                        title = "Seleciona un almacén",
                                        listOptions = listWareHouse,
                                        selected = {
                                            almacenDestine=it
                                            close()
                                        })
                                )
                            }
                        }
                    )
            )
        }


        Box{
            OutlinedTextField(
                enabled=false,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.DarkGray,
                    unfocusedBorderColor = Color.DarkGray,
                    disabledTextColor = Color.DarkGray,
                    disabledLabelColor = Color.DarkGray
                ),
                value = motivoTraslado.text,
                trailingIcon = { Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 10.dp),

                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                label = { Text(text = "Motivo de traslado") },
                placeholder = { Text(text = "") },
                onValueChange = {}
            )

            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Transparent)
                    .clickable(
                        onClick = {
                            open(
                                BottomSheetScreen.SelectWitOptionsModal(
                                    title = "Seleciona un motivo de traslado",
                                    listOptions = mutableListOf(
                                        Options(value="11",text="SALIDA POR TRANSFERENCIA ENTRE ALMACENES",icono= R.drawable.ic_baseline_box_24)
                                    ),
                                    selected = {
                                        motivoTraslado=it
                                        close()
                                    })
                            )
                        }
                    )
            )
        }

        /*
        Box{
            OutlinedTextField(
                enabled=false,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.DarkGray,
                    unfocusedBorderColor = Color.DarkGray,
                    disabledTextColor = Color.DarkGray,
                    disabledLabelColor = Color.DarkGray
                ),
                value = tipoMovimiento.text,
                trailingIcon = { Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 10.dp),

                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                label = { Text(text = "Tipo de Movimiento") },
                placeholder = { Text(text = "") },
                onValueChange = {}
            )

            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Transparent)
                    .clickable(
                        onClick = {
                            open(
                                BottomSheetScreen.SelectWitOptionsModal(
                                    title = "Seleciona un tipo de movimiento",
                                    listOptions = listOf(
                                        Options(value="11",text="Tipo 1",icono= R.drawable.ic_baseline_box_24),
                                        Options(value="12",text="Tipo 2",icono= R.drawable.ic_baseline_box_24),
                                        Options(value="13",text="Tipo 3",icono= R.drawable.ic_baseline_box_24),
                                        Options(value="14",text="Tipo 4",icono= R.drawable.ic_baseline_box_24)
                                    ),
                                    selected = {
                                        tipoMovimiento=it
                                        close()
                                    })
                            )
                        }
                    )
            )
        }
*/

        OutlinedTextField(
            enabled=true,
            singleLine=false,
            maxLines=5,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.DarkGray,
                unfocusedBorderColor = Color.DarkGray,
                disabledTextColor = Color.DarkGray,
                disabledLabelColor = Color.DarkGray
            ),
            value = comentario,
            trailingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Characters
            ),
            label = { Text(text = "Comentario") },
            placeholder = { Text(text = "") },
            onValueChange = {
                comentario=it
            }
        )

        /*OutlinedTextField(
            enabled=true,
            singleLine=false,
            maxLines=5,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.DarkGray,
                unfocusedBorderColor = Color.DarkGray,
                disabledTextColor = Color.DarkGray,
                disabledLabelColor = Color.DarkGray
            ),
            value = comentarioAsiento,
            trailingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Characters
            ),
            label = { Text(text = "Comentario del asiento") },
            placeholder = { Text(text = "") },
            onValueChange = {
                comentarioAsiento=it
            }
        )*/

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

                /*if(tipoMovimiento.value.isEmpty()){
                    messageError="*Es necesario que se seleciones el tipo de movimiento a realizar."
                }else{*/
                    if(almacenDestine.value.isEmpty()){
                        messageError="*Es necesario seleciones un almacén de destino."
                    }else{
                        if(motivoTraslado.value.isEmpty()){
                            messageError="*Es necesario que seleciones un motivo de traslado."
                        }else{
                            if(almacenOrigin.value.isEmpty()){
                                messageError="*Es necesario que seleciones un almacén de origen."
                            }else{
                                messageError=""
                                onPress(
                                    StockTransferHeader(
                                        Comment= comentario,
                                        CreateAt= Date(),
                                        NumReference= referencia,
                                        PriceList= -1,
                                        ObjType=objType,
                                        Motive=motivoTraslado.value,
                                        WarehouseDestine = almacenDestine.value,
                                        WarehouseOrigin = almacenOrigin.value
                                    )
                                )
                            }
                        }
                    }
                //}
            },
            colors = ButtonDefaults.textButtonColors(
                backgroundColor = AzulVistony201
            )
        ){
            Text(color= Color.White,text = " Crear", fontSize = 20.sp)
        }

    }
}
/*@Composable
fun showTypeLocation(objType: Int,value:String, onSelect:(String)->Unit){

    val listState = rememberLazyListState()
    val list = listOf("PICKING", "PUT AWAY", "STAGING", "QUALITY")

    LazyRow(modifier=Modifier.background(ColorOrigin), state = listState){
        itemsIndexed(list) { index, line ->
            Card(
                elevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .selectable(
                        selected = value == line,
                        onClick = {
                            onSelect(line)
                        }
                    )
            ){
                Box(modifier=Modifier.background(if(value == line){Color.LightGray}else{Color.Unspecified})){
                    Text(line,modifier=Modifier.padding(5.dp))
                }
            }
        }
    }
}*/

@Composable
fun xddVs2(stockTransferBandSRpsValue:List<MergedStockTransfer>, suggestions: SuggestionViewModel, value:String, onLoader:(BinLocation)->Unit, onTryAgain:()->Unit){

    val suggestionValue = suggestions.suggtn.collectAsState()
    val listState = rememberLazyListState()
    Log.e("REOS","Form-xddVs2-suggestionValue.value.status: "+suggestionValue.value.status)
    Log.e("REOS","Form-xddVs2-suggestionValue.value.Data: "+suggestionValue.value.Data)

    when(suggestionValue.value.status){
        ""->{
            Text("Ubicaciones sugeridas")
        }
        "cargando"->{
            Row(modifier= Modifier.padding(5.dp)) {
                CircularProgressIndicator(color= AzulVistony202,modifier=Modifier.fillMaxSize(0.1f))
                Text( "Buscando...",color=Color.Gray, fontSize = 12.sp,modifier=Modifier.padding(start=5.dp))
            }
        }
        "OK"->{
            if(suggestionValue.value.Data.isEmpty()){
                TextButton(
                    onClick = {
                        onTryAgain()
                    }
                ){
                    Text("No hay ubicaciones sugeridas para este producto en esta operación.",color=Color.Red)
                }
            }
            else{
                LazyRow(modifier=Modifier.background(ColorDestine), state = listState){
                    itemsIndexed(suggestionValue.value.Data) { index, line ->
                        Card(
                            elevation = 4.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                                .selectable(selected = value == line.BinCode,
                                    onClick = {
                                        //No se debe seleccionar la ubicación debe pistolearse para asegurarse de que esta fisicamente en la ubicación
                                        //onLoader(BinLocation(id=""+line.AbsEntry,text=line.BinCode))
                                    }
                                )
                        ){
                            Box(modifier=Modifier.background(if(value == line.BinCode){Color.LightGray}else{Color.Unspecified})){
                                Text(line.BinCode,modifier=Modifier.padding(5.dp))
                            }
                        }
                    }
                }
            }
            Log.e("REOS","Form-xddVs2-value: "+value.toString())
            if(value.split("-").size>=3){
                val resultSearch=suggestionValue.value.Data.filter(){ it.BinCode==value }

                if(resultSearch.isNotEmpty()){
                    val threeFilter=stockTransferBandSRpsValue.filter {  it.LocationName== value }

                    if(threeFilter.isNotEmpty()){
                        Text("La ubicación $value no puede ser origen y destino al mismo tiempo",color=Color.Red)
                       // onLoader(BinLocation())
                    }else{
                        onLoader(BinLocation(id=""+resultSearch[0].AbsEntry ,text=resultSearch[0].BinCode))
                    }
                }else{
                    Text("La ubicación leída no corresponde a una ubicación sugerida",color=Color.Red)
                    //onLoader(BinLocation())
                }
            }
        }
        else->{
            TextButton(
                onClick = {
                    onTryAgain()
                }
            ){
                Text(" Ocurrio un error, volver a intentar \n"+suggestionValue.value.status,color=Color.Red)
            }
        }
    }

    Text("")
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun formBsHeaderTask(
    taskMngmtDataForm:TaskMngmtDataForm,
    taskManagement: TaskMngmtAndHeaderDoc,
    onClose:()->Unit,onChange:(TaskMngmtDataForm)->Unit,onPress:(TaskMngmtDataForm)->Unit,

){

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    /*var numSerie by remember {mutableStateOf(taskManagement.Document.SerieDocument)}
    var numCorrelativo:String by  remember {mutableStateOf(taskManagement.Document.CorrelativoDocument)}
    var comentario:String by  remember {mutableStateOf(taskManagement.Document.Comment)}*/

    Text(taskManagement.Task.CardCode+" - "+taskManagement.Task.CardName,color = Color.Gray)

    Text("")

    Log.e("JEPICMAR","SOY VACIO=>"+taskManagement.Document.SerieDocument+"<<<<")

   /* if(taskManagement.Document.SerieDocument.isNotEmpty()){
        numSerie=taskManagement.Document.SerieDocument
    }else{
        numSerie=""
    }

    if(taskManagement.Document.CorrelativoDocument.isNotEmpty()) {
        numCorrelativo = taskManagement.Document.CorrelativoDocument
    }else{
        numCorrelativo=""
    }

    if(taskManagement.Document.Comment.isNotEmpty()){
        comentario=taskManagement.Document.Comment
    }else{
        comentario=""
    }*/


    OutlinedTextField(
        enabled= true,
        singleLine=true,
        value = taskMngmtDataForm.serie,
        onValueChange = {
            if (it.length <= 5){
                onChange(
                    TaskMngmtDataForm(
                        comentario = taskMngmtDataForm.comentario,
                        serie = it,
                        correlativo = taskMngmtDataForm.correlativo
                    )
                )
            }
        },
        label = { Text(text = "Número de serie de la guia") },
        placeholder = { Text(text = "Ingresar la serie de la guia") },
        trailingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = AzulVistony202) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text,imeAction = ImeAction.Next ),
        keyboardActions = KeyboardActions(
            onNext = {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
        )
    )

    OutlinedTextField(
        enabled= true,
        singleLine=true,
        value =   taskMngmtDataForm.correlativo,
        onValueChange = {
            if (it.length <= 11){
                onChange(
                    TaskMngmtDataForm(
                        comentario = taskMngmtDataForm.comentario,
                        serie = taskMngmtDataForm.serie,
                        correlativo = it
                    )
                )
            }
        },
        label = { Text(text = "Número de correlativo de la guia") },
        placeholder = { Text(text = "Ingresar el correlativo de la guia") },
        trailingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = AzulVistony202) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword,imeAction = ImeAction.Next ),
        keyboardActions = KeyboardActions(
            onNext = {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
        )
    )

    OutlinedTextField(
        enabled= true,
        singleLine=false,
        maxLines=10,
        value = taskMngmtDataForm.comentario,
        onValueChange = {
            onChange(
                TaskMngmtDataForm(
                    comentario = it,
                    serie = taskMngmtDataForm.serie,
                    correlativo = taskMngmtDataForm.correlativo
                )
            )
        },
        label = { Text(text = "Comentario") },
        placeholder = { Text(text = "Ingresar un comentario") },
        trailingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = AzulVistony202) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, capitalization = KeyboardCapitalization.Characters,imeAction = ImeAction.Next ),
        keyboardActions = KeyboardActions(
            onNext = {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
        )
    )

    Text("")

    btnBsHeaderTask(taskManagement.Task.Status,taskMngmtDataForm, onCancel = {

        //numSerie=""
        //numCorrelativo=""
        //comentario=""

        onClose()
    }, onContinue = {
        it.documentHeader=taskManagement.Task._id

        //numSerie=""
        //numCorrelativo=""
        //comentario=""

        onPress(it)
    })




}

@Composable
fun InputBox(
    enabled:Boolean=true,
    value:String="",
    onChange: (String) -> Unit={},
    label:String="",
    placeholder:String="",
    keyboardOptions:KeyboardOptions=KeyboardOptions(),
    keyboardActions:KeyboardActions=KeyboardActions()
){
    OutlinedTextField(
        enabled= enabled,
        singleLine=true,
        value = value,
        onValueChange = {
            onChange(it)
        },
        label = { Text(text = label) },
        placeholder = { Text(text = placeholder) },
        trailingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = AzulVistony202) },
        keyboardOptions =keyboardOptions,
        keyboardActions = keyboardActions
    )
}


@Composable
fun CountryDropdown() {
    var expanded by remember { mutableStateOf(false) }
    val countries = listOf(
        "Perú",
        "Chile",
        "Argentina",
        "Brasil",
        "Colombia",
        )
    var selectedCountry by remember { mutableStateOf(countries[0]) }

    Box(modifier = Modifier.fillMaxWidth().wrapContentSize(Alignment.TopStart)) {
        Text(selectedCountry, modifier = Modifier.clickable(onClick = { expanded = true }))
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            countries.forEach { country ->
                DropdownMenuItem(onClick = {
                    selectedCountry = country
                    expanded = false
                }) {
                    Text(country)
                }
            }
        }
    }
}