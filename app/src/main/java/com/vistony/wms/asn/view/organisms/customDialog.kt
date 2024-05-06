package com.vistony.wms.asn.view.organisms

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.vistony.wms.R
import com.vistony.wms.asn.mvvm.ASNViewModel
import com.vistony.wms.asn.view.molecules.listPrinterSection
import com.vistony.wms.asn.view.molecules.statusPrinter
import com.vistony.wms.component.ButtonCircle
import com.vistony.wms.component.Cell
import com.vistony.wms.component.DialogView
import com.vistony.wms.component.FlagDialog
import com.vistony.wms.component.lockMessageScreen
import com.vistony.wms.ui.theme.RedVistony
import com.vistony.wms.viewmodel.ItemsViewModel
import com.vistony.wms.viewmodel.PrintViewModel

@Composable
fun DialogLPNDelete(
    context: Context,
    asnViewModel: ASNViewModel,
    onClickAccept:() -> Unit,
    onClickCancel:() -> Unit,
    body:() -> Unit,
) {

    val dialogLPNDelteStatus=asnViewModel.dialogLPNDeleteStatus.collectAsState()
    val dialogLPNDeleteTittle=asnViewModel.dialogLPNDeleteTittle.collectAsState()
    val indexDelete=asnViewModel.indexDelete.collectAsState()
    if(dialogLPNDelteStatus.value)
    {
        DialogView(
            dialogLPNDeleteTittle.value, "Desea eliminar la linea "+(indexDelete.value+1)+"?",
            onClickCancel = {
                onClickCancel()
            }, onClickAccept = {
                onClickAccept()
            }, statusButtonAccept = true, statusButtonIcon = false, context = context
        ) {
            body()
        }
    }
}

@Composable
fun DialogShowPrinter(
    printViewModel: PrintViewModel,
    asnViewModel: ASNViewModel,
    itemsViewModel: ItemsViewModel,

    ){
    val flagModal = remember { mutableStateOf(FlagDialog()) }
    val dialogPrintStatus= asnViewModel.dialogPrintStatus.collectAsState()
    val appContext = LocalContext.current
    val print = printViewModel.print.collectAsState()
    val printBottomBar = asnViewModel.printBottomBar.collectAsState()

    if (dialogPrintStatus.value!!) {
        DialogView(
            "IMPRESIÓN", "Seleccione la impresora:", onClickCancel = {
                asnViewModel.updateDialogPrintStatus(false)
            }, onClickAccept = {
                asnViewModel.updateDialogPrintStatus(false)
            }, statusButtonAccept = false, statusButtonIcon = false, context = appContext
        ) {
            Column() {
                Row {

                    Column(modifier = Modifier.weight(0.5f), horizontalAlignment = Alignment.Start) {
                        Cell(text = "Impresora seleccionada: ", title = true, textAlign = TextAlign.Start)
                    }
                    Column(modifier = Modifier.weight(0.5f), horizontalAlignment = Alignment.Start) {
                        Cell(text = printBottomBar.value, title = true, textAlign = TextAlign.Start)
                    }
                }
                Row(
                    Modifier
                        .padding(10.dp)
                        .align(Alignment.CenterHorizontally)
                )
                {
                    Column(modifier = Modifier.weight(0.75f)) {
                        statusPrinter(
                            printViewModel,
                            itemsViewModel,
                            onResponse = {
                                flagModal.value = it
                            })

                        Log.e("JEPICAME","SE EJCUTA ASJKASJKASJK")

                        if(flagModal.value.status){
                            lockMessageScreen(
                                text=flagModal.value.flag,
                                close={
                                    flagModal.value= FlagDialog(false,"")
                                    Log.e("JEPICAME","SE EJCUTA ASJKASJKASJK estado actula "+ flagModal.value.flag)
                                }
                            )
                        }

                        listPrinterSection(
                            viewModel = printViewModel,
                            value = print.value.printer,
                            onSelect = {
                                Log.e("REOS","ASNPage-onSelect"+it.ip)
                                it.ip
                                asnViewModel.updatePrintBottomBar(it.name)
                                asnViewModel.updatePrint(it.ip)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))
                    Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(0.25f)) {
                        ButtonCircle(OnClick =
                        {
                            if(asnViewModel.validateStatusPrintAssigned())
                            {
                                asnViewModel.sendDataASNPrint()
                                Toast.makeText(appContext, "Imprimiendo...", Toast.LENGTH_SHORT).show()
                            }else
                            {
                                Toast.makeText(appContext, "Debe seleccionar una impresora", Toast.LENGTH_SHORT).show()
                            }


                        },
                            color =  RedVistony
                            , size = DpSize(70.dp,70.dp)
                        )
                        {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_baseline_print_24),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DialogASNDelete(
    context: Context,
    asnViewModel: ASNViewModel,
    onClickAccept:() -> Unit,
    onClickCancel:() -> Unit,
    body:() -> Unit,
) {

    val dialogASNDeleteStatus=asnViewModel.dialogASNDeleteStatus.collectAsState()
    val dialogLPNDeleteTittle=asnViewModel.dialogLPNDeleteTittle.collectAsState()

    if(dialogASNDeleteStatus.value)
    {
        DialogView(
            dialogLPNDeleteTittle.value, "Desea eliminar la linea ",
            onClickCancel = {
                onClickCancel()
            }, onClickAccept = {
                onClickAccept()
            }, statusButtonAccept = true, statusButtonIcon = false, context = context
        ) {
            body()
        }
    }
}