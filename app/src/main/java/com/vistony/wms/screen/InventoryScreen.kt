package com.vistony.wms.screen

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.vistony.wms.R
import com.vistony.wms.component.*
import com.vistony.wms.num.OptionsDowns
import com.vistony.wms.model.Inventory
import com.vistony.wms.ui.theme.AzulVistony201
import com.vistony.wms.ui.theme.RedVistony202
import com.vistony.wms.util.ConvertdatefordateSAP
import com.vistony.wms.util.ConvertdatefordateSAP2
import com.vistony.wms.util.Routes
import com.vistony.wms.viewmodel.InventoryViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun InventoryScreen(navController: NavHostController, context: Context){

    val inventoryViewModel: InventoryViewModel = viewModel(
        factory = InventoryViewModel.InventoryViewModelFactory("")
    )

    val inventoryValue = inventoryViewModel.inventories.collectAsState()

    Scaffold(
        topBar = {

            val listOptions = listOf(
                OptionsDowns(" Crear nueva ficha",R.drawable.ic_baseline_insert_drive_file_24),
            )

            TopBarTitleWithOptions(
                options=listOptions,
                title= Routes.Inventory.title ,
                onClick={

                    navController.navigate(Routes.InventoryCreate.route) {
                        launchSingleTop = true
                    }

                },
                navController=navController,
                form = "InventoryScreen",
                users = null
            )
        }
    ){
        LazyColumn(modifier = Modifier.fillMaxHeight()) {
            item{
                Column(modifier= Modifier.padding(10.dp)){
                    Row(
                        horizontalArrangement= Arrangement.SpaceBetween,
                        verticalAlignment= Alignment.CenterVertically,
                        modifier= Modifier.fillMaxWidth()
                    ){
                        Text("Número de fichas: ${inventoryValue.value.inventory.size}",color= Color.Gray)
                        TextButton(
                            onClick = {
                                inventoryViewModel.getData()
                            }
                        ){
                            Text(text="Actualizar",color= RedVistony202)
                        }
                    }

                    Text(text = "${inventoryValue.value.ownerName } ",color=Color.Gray)
                }

            }
            items(inventoryValue.value.inventory){ inventory ->

                when(inventoryValue.value.status){
                    ""->{}
                    "cargando"->{
                        CustomProgressDialog("Listando fichas...")
                    }
                    "ok"->{

                        val openDialog = remember { mutableStateOf(FlagDialog()) }

                        if(openDialog.value.status){
                            CustomDialogResendOrClose(
                                title="Cerrar Conteo",
                                openDialog={ response ->
                                    if(response){
                                        if(openDialog.value.flag=="Close"){
                                            inventoryViewModel.updateStatusClose(inventory._id)
                                        }else if(openDialog.value.flag=="Resend"){
                                            inventoryViewModel.resendToSap(inventory._id)
                                        }

                                    }
                                    openDialog.value=FlagDialog(false)
                                },
                                flag=openDialog.value.flag
                            )
                        }

                        ExpandableListItem(
                            inventory=inventory,
                            navController=navController,
                            onPresChangeStatus={
                                openDialog.value= FlagDialog(
                                    status = true,
                                    flag=it
                                )
                            }
                        )
                    }
                    "vacio"->{
                        Toast.makeText(context, "No hay historico a mostrar", Toast.LENGTH_SHORT).show()
                        inventoryViewModel.resetIdInventoryHeader()
                    }
                    else->{
                        Toast.makeText(context, "Ocurrio un error:\n ${inventoryValue.value.status}", Toast.LENGTH_SHORT).show()
                        inventoryViewModel.resetIdInventoryHeader()
                    }

                }
            }
            item{
                Spacer(modifier=Modifier.height(15.dp))
            }
        }


    }
}

@Composable
private fun ExpandableListItem(inventory: Inventory,navController: NavHostController,onPresChangeStatus:(String) ->Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, top = 15.dp, end = 15.dp)
            .clickable(onClick = { expanded = !expanded })
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(end = 32.dp)
                    .fillMaxWidth()
            ) {

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxHeight()
                ){
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_insert_drive_file_24),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(30.dp)
                            .clickable {
                                navController.navigate("InventoryCounting/idInventory=${inventory._id}&whs=${inventory.wareHouse}&status=${inventory.status}&defaultLocation=${URLEncoder.encode( if(inventory.defaultLocation==null){"-"}else{inventory.defaultLocation}, StandardCharsets.UTF_8.toString())}&typeInventory=${URLEncoder.encode( inventory.type,StandardCharsets.UTF_8.toString())}")
                            },
                        tint = if(inventory.status=="Cerrado"){Color.Gray}else{AzulVistony201}
                    )
                    if(inventory.status=="Abierto"){
                        Text("Ver", textAlign = TextAlign.Center)
                    }
                }

                TitleAndSubtitle(
                    title = inventory.name,
                    type = inventory.type,
                    status = inventory.status,
                    dateAssigment = inventory.arrivalTimeSap.toString()
                )

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "ArrowIcon",
                    tint=Color.DarkGray,
                    modifier = Modifier
                        .align(CenterVertically)
                        .graphicsLayer(
                            rotationZ = animateFloatAsState(
                                if (expanded) 180f else 0f
                            ).value,
                        )

                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 300, easing = FastOutLinearInEasing)
                ),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 300, easing = FastOutLinearInEasing)
                )
            ) {
                Divider(modifier = Modifier.height(1.dp))
                Column(modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 15.dp)) {

                    Spacer(modifier = Modifier.height(10.dp))

                    ExtraItem(item = Item(
                            title="Almacen",
                            date=inventory.wareHouse
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(modifier = Modifier.height(1.dp))
                    Spacer(modifier = Modifier.height(10.dp))

                    ExtraItem(item = Item(
                        title="Fecha de Inicio",
                        date="${inventory.createAt.getUIStringTimeStampWithDate()} "
                    )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(modifier = Modifier.height(1.dp))
                    Spacer(modifier = Modifier.height(10.dp))

                    ExtraItem(item = Item(
                            title="Fecha de Fin",
                            date=if(inventory.createAt != inventory.closeAt){"${inventory.closeAt.getUIStringTimeStampWithDate()} "}else{" "}
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(modifier = Modifier.height(1.dp))
                    Spacer(modifier = Modifier.height(10.dp))

                    ExtraItem(
                        item = Item(
                            title="N° SAP",
                            date=if(inventory.codeSAP==0){"# "}else{"${inventory.codeSAP} " }
                        ),
                        onClick={

                            Log.e("REOS","InventoryScreen-ExtraItem-inventory.codeSAP: "+inventory.codeSAP)
                            Log.e("REOS","InventoryScreen-ExtraItem-inventory.type: "+inventory.type)
                            Log.e("REOS","InventoryScreen-ExtraItem-inventory.status: "+inventory.status)
                            Log.e("REOS","InventoryScreen-ExtraItem-inventory.response: "+inventory.response)
                            Log.e("REOS","InventoryScreen-ExtraItem-inventory.coment: "+inventory.coment)
                            Log.e("REOS","InventoryScreen-ExtraItem-inventory.owner: "+inventory.owner)

                            onPresChangeStatus("Resend")
                        },
                        status=inventory.status
                    )

                    if(inventory.response.isNotEmpty()){

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(modifier = Modifier.height(1.dp))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier=Modifier.fillMaxWidth()
                        ) {
                            Text(text = inventory.response,color=Color.Gray)
                        }
                    }

                    /*TextButton(
                        modifier= Modifier
                            .padding(top = 10.dp, bottom = 5.dp)
                            .fillMaxWidth(),
                        enabled = inventory.status=="Abierto",
                        onClick = {
                            onPresChangeStatus("Close")
                        }) {
                        Text(text="Cerrar ficha",color= if(inventory.status=="Abierto"){AzulVistony202}else{Color.Gray})
                    }*/

                }
            }
        }
    }
}

fun Date.getUIStringTimeStampWithDate(): String {
    val dateFormat = SimpleDateFormat("dd-MMM-yyyy HH:mm",Locale.getDefault())
    dateFormat.timeZone = TimeZone.getDefault()
    return dateFormat.format(this)
}

@Composable
fun TitleAndSubtitle(
    title: String,
    type: String,
    status: String,
    dateAssigment:String
) {

    Column(
        horizontalAlignment=Alignment.Start,
        verticalArrangement = Arrangement.Center,
        modifier=Modifier.padding(10.dp)
    ) {
        Text(text = title, fontWeight = FontWeight.Bold)
        Text(text = "$type ",color=Color.Gray)
        Text(text = "$status ",color=Color.Gray)
        Text(text = //"$dateAssigment "
               "Fecha "+ ConvertdatefordateSAP2(dateAssigment).toString()
            ,color=Color.Gray)
    }
}

@Composable
fun ExtraItem(item: Item,onClick:()->Unit={},status:String="") {

    Log.e("REOS","InventoryScreen-ExtraItem-item.date: "+item.date)
    Log.e("REOS","InventoryScreen-ExtraItem-item.status: "+status)
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier=Modifier.fillMaxWidth()
    ) {
        Text(text = item.title)

        if(item.date=="# " && status=="Cerrado"){
            TextButton(onClick = {
                onClick()
            }){
                Text(text = "Reenviar",color=Color.Gray)
            }
        }else{
            Text(text = item.date,color=Color.Gray)
        }
    }
}

data class Item (
    val title: String,
    val date: String
)