package com.vistony.wms.screen

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vistony.wms.model.BinLocations
import com.vistony.wms.model.zebraPayload
import com.vistony.wms.ui.theme.AzulVistony201
import com.vistony.wms.ui.theme.AzulVistony202
import com.vistony.wms.ui.theme.RedVistony
import com.vistony.wms.viewmodel.BlockLocationViewModel
//import com.vistony.wms.viewmodel.blockLocationViewModel
import com.vistony.wms.viewmodel.ZebraViewModel
import io.realm.RealmList

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun BlockLocation(
    navController: NavController,
    context: Context,
    zebraViewModel: ZebraViewModel
) {

    val zebraValue = zebraViewModel.data.collectAsState()

    val blockLocationViewModel: BlockLocationViewModel = viewModel(
        factory = BlockLocationViewModel.BlockLocationViewModelFactory( )
    )

    val showPopup : State<Boolean> = blockLocationViewModel.showPopup.collectAsState()

    if( showPopup.value ){
        Log.e("jesusdebug", "showPopup")
            Popup(
                onDismissRequest = { blockLocationViewModel.closePopUp() },
                content = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = Color.White)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("¿Desea bloquear la ubicación?")
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = {
                                        // Lógica para bloquear la ubicación
                                        blockLocationViewModel.updateStatusLocation("N")
                                        blockLocationViewModel.closePopUp()
                                    },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text("Desbloquear")
                                }
                                Button(
                                    onClick = {
                                        // Lógica para desbloquear la ubicación
                                        blockLocationViewModel.updateStatusLocation("Y")
                                        blockLocationViewModel.closePopUp()
                                    },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = RedVistony),
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text("Bloquear", color = Color.White)
                                }
                            }
                        }
                    }
                },

            )
    }
    if (zebraValue.value.Payload.isNotEmpty()) {
        LocalFocusManager.current.clearFocus()
        blockLocationViewModel.handleScannedData(
            type = zebraValue.value.Type,
            code = zebraValue.value.Payload,
            context = context
        )
        zebraViewModel.setData(zebraPayload())
        zebraValue.value.Payload = ""  // esta linea fue añadida, se puede borrar si genera problemas
    }
    Scaffold(
        topBar = {
            TopAppBar(
                elevation = 0.dp,
                modifier = Modifier.background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            AzulVistony202,
                            AzulVistony201
                        )
                    )
                ),
                title = {
                    Text(
                        text = "Bloqueo de Ubicación",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                },
                backgroundColor = Color.Transparent
            )
        },
    ){
        LocationList(context = context, blockLocationViewModel = blockLocationViewModel)
    }

}


@Composable
fun ListItem(location: BinLocations, blockLocationViewModel: BlockLocationViewModel) {
Box(modifier = Modifier
    .padding(8.dp)
    .background(color = if (location.LockPick == "Y") Color.Red else Color.Green)
    .clickable {
        blockLocationViewModel.setLastPayloadCodeScanned(location.BinCode)
        blockLocationViewModel.openPopUp()
    }) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier
            .weight( 1.0f )){
            Text(text = "Código de Ubicación: ${location.BinCode}")
        }
        Spacer(modifier = Modifier.width(16.dp))
        Box(modifier = Modifier
            .weight( 1.0f )){
            Column {
                Text(text = "Almacén: ${location.Warehouse}")
                Text(text = if (location.LockPick == "Y") "Bloqueado" else "Libre")
            }
        }
    }

}}
@Composable
fun LocationList(context: Context, blockLocationViewModel: BlockLocationViewModel) {
    val filteredLocations : State<List<BinLocations>> = blockLocationViewModel.filteredBinLocations.collectAsState()
    val searchInput : State<String> = blockLocationViewModel.lastPayloadCodeScanned.collectAsState()

    if (filteredLocations.value.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No hay ubicaciones")
        }
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
    ){
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ){
            TextField(
                value = searchInput.value,
                onValueChange = {
                    blockLocationViewModel.filterLocationsByCode(it)
                    blockLocationViewModel.setLastPayloadCodeScanned(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                placeholder = { Text(text = "Buscar por Código")},
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { blockLocationViewModel.filterLocationsByCode("") }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null
                        )
                    }
                }
            )
        }
        LazyColumn {
            items(filteredLocations.value.size) { location ->
                ListItem(location = filteredLocations.value[location], blockLocationViewModel = blockLocationViewModel)
            }
        }
    }
}