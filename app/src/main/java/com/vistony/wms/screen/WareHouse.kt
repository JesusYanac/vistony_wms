package com.vistony.wms.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.vistony.wms.model.Article
import com.vistony.wms.model.Warehouse
import com.vistony.wms.viewmodel.ArticleViewModel
import com.vistony.wms.viewmodel.WarehouseViewModel
import io.realm.mongodb.sync.SyncConfiguration

@Composable
fun WarehouseScreen(navController: NavHostController){

    val warehouseViewModel: WarehouseViewModel = viewModel(
        factory = WarehouseViewModel.WarehouseViewModelFactory("init")
    )

    val warehouseValue = warehouseViewModel.almacenes.collectAsState()

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
            Text("${warehouse.code} ")
            Text("Codigo: ${warehouse.name}",color=Color.Gray)
            Text("Cdg Postal: ${warehouse.zipCode}",color=Color.Gray)
            Text("Ubicaciones: ${numLocation}",color=Color.Gray)
            var status=if(warehouse.status=="N"){"Inactivo"}else{"Activo"}
            Text("Estado: $status",color=Color.Gray)
        }
    }
}