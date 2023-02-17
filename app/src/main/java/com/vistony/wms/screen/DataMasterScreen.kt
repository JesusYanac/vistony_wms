package com.vistony.wms.screen

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.vistony.wms.R
import com.vistony.wms.component.TopBar
import com.vistony.wms.model.StockTransferHeader
import com.vistony.wms.ui.theme.AzulVistony202
import com.vistony.wms.util.Paremetros
import com.vistony.wms.viewmodel.DataMasterViewModel
import com.vistony.wms.viewmodel.ItemsViewModel
import com.vistony.wms.viewmodel.StockTransferHeaderViewModel
import com.vistony.wms.viewmodel.WarehouseViewModel
import java.util.*

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun DataMasterScreen(navController: NavHostController, context: Context) {

    val dataMasterViewModel: DataMasterViewModel = viewModel(
        factory = DataMasterViewModel.DataMasterViewModelFactory()
    )

    val itemsViewModel: ItemsViewModel = viewModel(
        factory = ItemsViewModel.ArticleViewModelFactory("init")
    )

    val warehouseViewModel: WarehouseViewModel = viewModel(
        factory = WarehouseViewModel.WarehouseViewModelFactory("init")
    )

    val dataMasterValue = dataMasterViewModel.dataMaster.collectAsState()
    val articleValue = itemsViewModel.articles.collectAsState()
    val warehouseValue = warehouseViewModel.almacenes.collectAsState()

    Scaffold(
        topBar = {
            TopBar("Parametros")
        }
    ){
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp)
        ){
            rowParameter(
                title="Descargar Almacenes",
                subTitle=warehouseValue.value.status,
                size=warehouseValue.value.warehouse.size,
                fecha=warehouseValue.value.fechaDescarga,
                onPressed = {
                    navController.navigate("MasterWarehouse")
                }
            )

            rowParameter(
                title="Descargar Artículos",
                subTitle=articleValue.value.status,
                size=articleValue.value.listArticle.size,
                fecha=articleValue.value.fechaDescarga,
                onPressed = {
                    navController.navigate("MasterArticle")
                }
            )

            rowParameter(
                title="Descargar T. de Tareas",
                subTitle=articleValue.value.status,
                size=articleValue.value.listArticle.size,
                fecha=articleValue.value.fechaDescarga,
                onPressed = {
                    //navController.navigate("MasterArticle")
                }
            )
        }
    }
}

@Composable
private fun rowParameter(title:String="",subTitle:String="",size:Int=0,fecha:Date=Date(), onPressed: () -> Unit){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {
                onPressed()
            })
            .height(55.dp)
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Icon(painter = painterResource(id = R.drawable.ic_baseline_cloud_download_24), contentDescription = null, tint = AzulVistony202)
        Spacer(modifier = Modifier.width(10.dp))
        Column{
            Text(text = title, color = Color.DarkGray)
            Text(text = subTitle, color = Color.Gray)
        }
        Column(
            modifier=Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ){
            Text(text = "$size", color = Color.DarkGray)
            Text(text = fecha.getUIStringTimeStampWithDate(), color = Color.Gray, fontSize = 12.sp)
        }
    }
}
