package com.vistony.wms.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Card
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.vistony.wms.component.TopBar
import com.vistony.wms.model.Warehouse
import com.vistony.wms.viewmodel.WarehouseViewModel

@Composable
fun WarehouseScreen(navController: NavHostController){

    val warehouseViewModel: WarehouseViewModel = viewModel(
        factory = WarehouseViewModel.WarehouseViewModelFactory("init")
    )

    val warehouseValue = warehouseViewModel.almacenes.collectAsState()

    Log.e("JEPICAME","Almacen =>"+warehouseValue.value.status)



    Scaffold(
        topBar = {
            TopBar(title="Maestro de Almacenes")
        }
    ){
        LazyColumn(modifier = Modifier.fillMaxHeight()) {
                item{
                    Row(
                        horizontalArrangement=Arrangement.SpaceBetween,
                        verticalAlignment=Alignment.CenterVertically,
                        modifier=Modifier.padding(10.dp).fillMaxWidth()

                    ){
                        Text("Número de almacenes: ${warehouseValue.value.warehouse.size}",color= Color.Gray)
                        TextButton(
                            onClick = {
                                warehouseViewModel.getMasterDataWarehouse()
                            }
                        ){
                            Text("Actualizar")
                        }
                    }
                }
                itemsIndexed (warehouseValue.value.warehouse){i, warehouse ->
                    formWhs(warehouse,warehouseValue.value.numLocation[i])
                }
            }
    }
}

@Composable
private fun formWhs(warehouse: Warehouse,numLocation:Int){
    Card(
        elevation = 4.dp,
        modifier=Modifier.padding(10.dp).fillMaxWidth()
    ) {
        Column(
            modifier=Modifier.padding(10.dp)
        ){
            Text("${warehouse.WarehouseCode} ")
            Text("Nombre: ${warehouse.WarehouseName}",color=Color.Gray)
            Text("Sucursal: ${warehouse.Sucursal}",color=Color.Gray)
            Text("Control ubicaciones: "+if(warehouse.EnableBinLocations=="tNO"){"No"}else{"Si"},color=Color.Gray)
            Text("Ubicaciones: ${numLocation}",color=Color.Gray)
        }
    }
}