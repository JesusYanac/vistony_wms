package com.vistony.wms.component

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vistony.wms.R
import com.vistony.wms.model.CountryLocation
import com.vistony.wms.model.TypeInventario
import com.vistony.wms.model.Warehouse
import com.vistony.wms.ui.theme.AzulVistony202
import com.vistony.wms.viewmodel.InventoryViewModel
import com.vistony.wms.viewmodel.WarehouseViewModel

@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@Composable
fun SheetLayout(currentScreen: BottomSheetScreen,onCloseBottomSheet :()->Unit,showIconClose:Boolean=true) {
    BottomSheetWithCloseDialog(onCloseBottomSheet,showIconClose=showIconClose){
        when(currentScreen){
            is BottomSheetScreen.SelectWarehouseModal ->
                SelectWarehouseModal(context=currentScreen.context,selected=currentScreen.selected)
            is BottomSheetScreen.SelectTypeModal ->
                SelectTypeModal(selected=currentScreen.selected)
            is BottomSheetScreen.SelectCountryModal ->
                SelectCountryModal(selected=currentScreen.selected)
        }
    }
}

sealed class BottomSheetScreen(){
    class SelectWarehouseModal(val context: Context,val selected: (Warehouse) -> Unit) : BottomSheetScreen()
    class SelectTypeModal(val selected: (TypeInventario) -> Unit) : BottomSheetScreen()
    class SelectCountryModal(val selected: (CountryLocation) -> Unit):BottomSheetScreen()
}
@Composable
fun SelectCountryModal(selected: (CountryLocation) -> Unit){
    val listLocation = listOf(
        CountryLocation("PE","PERÚ"),
        CountryLocation("PY","PARAGUAY"),
        CountryLocation("CL","CHILE"),
        CountryLocation("EC","ECUADOR")
    )

    Column(modifier=Modifier.padding(top=20.dp, bottom = 10.dp)){
        Text(text="Selecciona tu locación",color=Color.Gray,modifier=Modifier.padding(start=20.dp,bottom=10.dp))
        Divider(modifier=Modifier.fillMaxWidth(0.8f).padding(start=20.dp,bottom=10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            itemsIndexed(listLocation) { _, line ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = {
                            selected(line)
                        })
                        .height(55.dp)
                        .padding(start = 25.dp), verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(painter = painterResource(id = R.drawable.ic_baseline_domain_24), contentDescription = null, tint = AzulVistony202)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column{
                        Text(text = line.text, color = Color.DarkGray)
                    }

                }
            }
        }


    }
}

@Composable
fun SelectWarehouseModal(context: Context, selected: (Warehouse) -> Unit){

    val werehouseViewModel: WarehouseViewModel = viewModel(
        factory = WarehouseViewModel.WarehouseViewModelFactory ("init")
    )

    val warehouseValue = werehouseViewModel.almacenes.collectAsState()

    Log.e("JEPICAME","=>>>"+warehouseValue.value.status)

    when(warehouseValue.value.status){
        ""->{}
        "cargando"->{
            CustomProgressDialog("listando almacenes...")
        }
        "ok"->{


            Column(modifier=Modifier.padding(top=20.dp, bottom = 10.dp)) {
                Text(
                    text = "Selecciona tu almacén",
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 20.dp, bottom = 10.dp)
                )
                Divider(
                    modifier = Modifier.fillMaxWidth(0.8f).padding(start = 20.dp, bottom = 10.dp)
                )

                LazyColumn(
                    contentPadding = PaddingValues(16.dp)
                ) {
                    itemsIndexed(warehouseValue.value.warehouse) { _, line ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = {
                                    selected(line)
                                })
                                .height(55.dp)
                                .padding(start = 25.dp), verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(painter = painterResource(id = R.drawable.ic_baseline_domain_24), contentDescription = null, tint = AzulVistony202)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column{
                                Text(text = line.code, color = Color.DarkGray)
                                Text(text = line.name, color = Color.DarkGray)
                            }

                        }
                    }
                }

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
}

@Composable
fun SelectTypeModal(selected: (TypeInventario) -> Unit){

    val listTypeInventory = listOf(
        TypeInventario("RI","Recepción Importación"),
        TypeInventario("RP","Recepción Producción"),
        TypeInventario("RS","Recepción Sucursales"),
        TypeInventario("IG","Inventario General"),
        TypeInventario("IC","Inventario Cíclico"),
        TypeInventario("PC","Picking Clientes"),
        TypeInventario("PS","Picking Sucursales"),
        TypeInventario("PI","Picking Induvis"),
        TypeInventario("OT","Otros")
    )

    Column(modifier=Modifier.padding(top=20.dp, bottom = 10.dp)) {
        Text(
            text = "Selecciona el tipo de conteo",
            color = Color.Gray,
            modifier = Modifier.padding(start = 20.dp, bottom = 10.dp)
        )
        Divider(
            modifier = Modifier.fillMaxWidth(0.8f).padding(start = 20.dp, bottom = 10.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp)
        ) {
            itemsIndexed(listTypeInventory) { _, line ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = {
                            selected(line)
                        })
                        .height(55.dp)
                        .padding(start = 25.dp), verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(painter = painterResource(id = R.drawable.ic_baseline_domain_24), contentDescription = null, tint = AzulVistony202)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column{
                        Text(text = line.text, color = Color.DarkGray)
                    }

                }
            }
        }

    }
}

@Composable
fun BottomSheetWithCloseDialog(onClosePressed: () -> Unit,closeButtonColor: Color = Color.White,showIconClose:Boolean,content: @Composable() () -> Unit){
    Box{

        if(showIconClose){
            IconButton(
                onClick = onClosePressed,
                modifier = Modifier
                    .background(Color.Gray)
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(30.dp)
            ) {
                Icon(Icons.Filled.Close, tint = closeButtonColor, contentDescription = null)
            }
        }

        content()

    }
}



