package com.vistony.wms.screen

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
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
import com.vistony.wms.component.CustomDialogQuestion
import com.vistony.wms.component.CustomDialogSendSap
import com.vistony.wms.component.CustomProgressDialog
import com.vistony.wms.component.TopBar
import com.vistony.wms.model.Counting
import com.vistony.wms.model.Inventory
import com.vistony.wms.ui.theme.AzulVistony201
import com.vistony.wms.ui.theme.AzulVistony202
import com.vistony.wms.ui.theme.RedVistony202
import com.vistony.wms.viewmodel.InventoryViewModel
import org.bson.types.ObjectId

@Composable
fun HistoryInventoryScreen(navController: NavHostController,context: Context){

    val inventoryViewModel: InventoryViewModel = viewModel(
        factory = InventoryViewModel.InventoryViewModelFactory()
    )

    val inventoryValue = inventoryViewModel.inventories.collectAsState()

    Scaffold(
        topBar = {
            TopBar(title="Historial de recuentos")
        }
    ){



        LazyColumn(modifier = Modifier.fillMaxHeight()) {
            item{
                Row(
                    horizontalArrangement= Arrangement.SpaceBetween,
                    verticalAlignment= Alignment.CenterVertically,
                    modifier= Modifier
                        .padding(10.dp)
                        .fillMaxWidth()

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
            }
            items(inventoryValue.value.inventory){ inventory ->

                when(inventoryValue.value.status){
                    ""->{}
                    "cargando"->{
                        CustomProgressDialog("Listando fichas...")
                    }
                    "ok"->{

                        val openDialog = remember { mutableStateOf(false) }

                        if(openDialog.value){
                            CustomDialogSendSap(openDialog={ response ->
                                if(response){
                                    inventoryViewModel.updateStatusClose(inventory._id)
                                }

                                openDialog.value=false

                            })
                        }


                        ExpandableListItem(
                            inventory=inventory,
                            navController=navController,
                            ownerName = inventoryValue.value.ownerName,
                            onPresChangeStatus={
                                openDialog.value=true
                            }
                        )
                    }
                    "vacio"->{
                        Toast.makeText(context, "No hay historico a mostrar", Toast.LENGTH_SHORT).show()
                        inventoryViewModel.resetArticleStatus()
                    }
                    else->{
                        Toast.makeText(context, "Ocurrio un error:\n ${inventoryValue.value.status}", Toast.LENGTH_SHORT).show()
                        inventoryViewModel.resetArticleStatus()
                    }

                }
            }
            item{
                Spacer(modifier=Modifier.height(15.dp))
            }
        }


    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ExpandableListItem(inventory: Inventory,navController: NavHostController,ownerName:String=" ",onPresChangeStatus:() ->Unit) {
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
                                navController.navigate("InventoryCounting/idInventory=${inventory._id}&whs=${inventory.wareHouse}&status=${inventory.status}")
                            },
                        tint = if(inventory.status=="Cerrado"){Color.Gray}else{AzulVistony201}
                    )
                    if(inventory.status=="Abierto"){
                        Text("Ver", textAlign = TextAlign.Center)
                    }
                }

                TitleAndSubtitle(
                    title = inventory.name,
                    owner = ownerName,
                    subtitle = inventory.createAt.toString()
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
                        title="Tipo",
                        date="${inventory.type} "
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(modifier = Modifier.height(1.dp))
                    Spacer(modifier = Modifier.height(10.dp))

                    ExtraItem(item = Item(
                        "Estado",
                        "${inventory.status} "
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(modifier = Modifier.height(1.dp))
                    Spacer(modifier = Modifier.height(10.dp))

                    ExtraItem(item = Item(
                        "N° SAP",
                        if(inventory.codeSAP==0){"# "}else{"${inventory.codeSAP} " }
                    )
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

                    TextButton(
                        modifier=Modifier.padding(top=10.dp,bottom=5.dp).fillMaxWidth(),
                        enabled = inventory.status=="Abierto",
                        onClick = {
                            onPresChangeStatus()
                        }) {
                        Text(text="Enviar a SAP",color= if(inventory.status=="Abierto"){AzulVistony202}else{Color.Gray})
                    }

                }
            }
        }
    }
}

@Composable
fun TitleAndSubtitle(
    title: String,
    owner: String,
    subtitle: String
) {
    Column(
        horizontalAlignment=Alignment.Start,
        verticalArrangement = Arrangement.Center,
        modifier=Modifier.padding(10.dp)
    ) {
        Text(text = title, fontWeight = FontWeight.Bold)
        Text(text = "$owner ",color=Color.Gray)
        Text(text = subtitle,color=Color.Gray)
    }
}

@Composable
fun ExtraItem(item: Item) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier=Modifier.fillMaxWidth()
    ) {
        Text(text = item.title)
        Text(text = item.date,color=Color.Gray)
    }
}

data class Item (
    val title: String,
    val date: String
)