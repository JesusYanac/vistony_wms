package com.vistony.wms.asn.view.page

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint.Align
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vistony.wms.R
import com.vistony.wms.asn.mvvm.ASNRepository
import com.vistony.wms.asn.mvvm.ASNViewModel
import com.vistony.wms.asn.mvvm.PreASN
import com.vistony.wms.asn.mvvm.countElements
import com.vistony.wms.asn.mvvm.getDataBarCode
import com.vistony.wms.asn.view.organisms.DialogShowPrinter
import com.vistony.wms.asn.view.template.ASNtemplate
import com.vistony.wms.component.ButtonCircle
import com.vistony.wms.component.Cell
import com.vistony.wms.component.CustomDialogLoading
import com.vistony.wms.component.DialogView
import com.vistony.wms.component.EditextGeneric
import com.vistony.wms.component.FlagDialog
import com.vistony.wms.component.TextWithDivider
import com.vistony.wms.component.lockMessageScreen
import com.vistony.wms.component.lockScreen
import com.vistony.wms.model.Print
import com.vistony.wms.model.PrintMachines
import com.vistony.wms.model.zebraPayload
import com.vistony.wms.ui.theme.AzulVistony201
import com.vistony.wms.ui.theme.AzulVistony202
import com.vistony.wms.ui.theme.BlueVistony
import com.vistony.wms.ui.theme.ColorDestine
import com.vistony.wms.ui.theme.RedVistony
import com.vistony.wms.util.APIService
import com.vistony.wms.viewmodel.ItemsViewModel
import com.vistony.wms.viewmodel.PrintViewModel
import com.vistony.wms.viewmodel.ZebraViewModel

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ASNpage1(
    navController: NavController,
    context: Context,
    zebraViewModel: ZebraViewModel
) {
    //val apiService = APIService.getInstance()
    val asnRepository = ASNRepository(
        //apiService
    )
    val asnViewModel: ASNViewModel = viewModel(
        factory = ASNViewModel.ASNViewModelFactory(
            asnRepository,context
        )
    )

    ASNpage(navController, context, zebraViewModel, asnViewModel)
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ASNpage(
    navController: NavController,
    context: Context,
    zebraViewModel: ZebraViewModel,
    asnViewModel: ASNViewModel
) {
    val zebraValue = zebraViewModel.data.collectAsState()

    val printViewModel: PrintViewModel = viewModel(
        factory = PrintViewModel.PrintViewModelFactory()
    )
    val itemsViewModel: ItemsViewModel = viewModel(
        factory = ItemsViewModel.ArticleViewModelFactory("scan")
    )
    val resultASN = asnViewModel.resultASN.collectAsState()
    val resultPreASN = asnViewModel.resultPreASN.collectAsState()
    val showDialog = asnViewModel.showLoadingDialog.collectAsState()
    Log.e("REOS", "ASNpage.ASNpage.resultASN.value: ${resultASN.value}")
    Log.e("REOS", "ASNpage.ASNpage.resultPreASN.value: ${resultPreASN.value}")
    //Recibe codigo de barra y envia a API el codigo de barra
    if (!zebraValue.value.Payload.isNullOrEmpty()){
        try {
            Log.e("REOS", "ASNCreate.countElements(zebraValue.value.Payload): ${countElements(zebraValue.value.Payload)}")
            when (countElements(zebraValue.value.Payload)) {
                1 -> {
                    if (asnViewModel.validateStatusHeadASN()){
                        asnViewModel.addDetailLpnCode(zebraValue.value.Payload)
                    }else {
                        Toast.makeText(context, "Debe scanear el codigo de caja para iniciar", Toast.LENGTH_LONG).show()
                    }
                }
                3 -> {
                    val dataBarCode = getDataBarCode(zebraValue.value.Payload)
                    Log.e("REOS", "ASNCreate.dataBarCode: ${dataBarCode}")
                    if (asnViewModel.validateStatusHeadASN()){
                        Toast.makeText(context, "Ya cuenta con un codigo caja scaneado", Toast.LENGTH_LONG).show()
                    }else {
                        asnViewModel.resetASN()
                        asnViewModel.resetPreASN()
                        asnViewModel.updateStatusShowDialog(true)
                        asnViewModel.getDataPreASN(
                            dataBarCode["01"].toString(),
                            dataBarCode["10"].toString()
                        )
                    }
                }
            }
        }catch (e:Exception){
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }
        zebraViewModel.setData(zebraPayload())
    }


    //TODO: Validar que exista el PreASN en el Objeto
    when (resultPreASN.value.status){
        "Y" -> {
            if (resultASN.value.data.isEmpty()){
                asnViewModel.chargeDataASNHead(resultPreASN.value.data.last())
            }
            asnViewModel.updateStatusShowDialog(false)
        }
        "N" -> {
            asnViewModel.updateStatusShowDialog(false)
        }
        /*"Loading"->{
            asnViewModel.updateStatusShowDialog(true)
        }*/
        "" -> {
            asnViewModel.updateStatusShowDialog(false)
        }

    }

    DialogShowPrinter(printViewModel,asnViewModel,itemsViewModel)

    Scaffold(
        bottomBar = {


        },
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
                        text = "Crear ASN",
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
                backgroundColor = Color.Transparent,
                actions = {
                    Row {
                        IconButton(
                            onClick = {
                                if (asnViewModel.validateStatusASN()) {
                                    asnViewModel.updateDialogPrintStatus(true)
                                }
                                else {
                                    Toast.makeText(context, "No hay articulos para imprimir", Toast.LENGTH_LONG).show()
                                }
                            },
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_print_24),
                                contentDescription = "search",
                                tint = Color.White
                            )
                        }
                    }
                }
            )
        }
    ){
    when (resultASN.value.status) {
        "Y" -> {
            asnViewModel.updateStatusShowDialog(false)
            ASNtemplate(asnViewModel)
        }
        "N" -> {
            asnViewModel.updateStatusShowDialog(false)
            Column(
                modifier= Modifier
                    .padding(top = 20.dp, bottom = 10.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_palet_on_24),
                    contentDescription = "Favorite Icon",
                    modifier = Modifier.size(150.dp)
                )

                Text(
                    text = "ESCANEA EL CODIGO DE\n BARRAS PARA INICIAR",
                    color = Color.Gray,
                    modifier = Modifier.padding(top= 25.dp)
                )
            }
        }
    }
}
    if (showDialog.value) {
        CustomDialogLoading(showDialog = true, onDismiss = {})
    }
}









