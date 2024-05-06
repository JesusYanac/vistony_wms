package com.vistony.wms.asn.view.template

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.vistony.wms.R
import com.vistony.wms.asn.mvvm.ASNViewModel
import com.vistony.wms.asn.mvvm.PreASN
import com.vistony.wms.asn.mvvm.PreASNEntity
import com.vistony.wms.asn.view.organisms.DialogASNDelete
import com.vistony.wms.asn.view.organisms.DialogLPNDelete
import com.vistony.wms.component.ButtonCircle
import com.vistony.wms.component.Cell
import com.vistony.wms.component.DialogView
import com.vistony.wms.component.EditextGeneric
import com.vistony.wms.component.TextWithDivider
import com.vistony.wms.ui.theme.RedVistony

@Composable
fun ASNtemplate(
    asnViewModel: ASNViewModel
){
    var showDialogQuantityReport = asnViewModel.showDialogQuantityReport.collectAsState()
    val resultASN = asnViewModel.resultASN.collectAsState()
    val context = LocalContext.current
    val updateDialogEditQuantyTittle = asnViewModel.dialogEditQuantyTittle.collectAsState()
    val resultASNresponse=asnViewModel.resultASNresponse.collectAsState()
    val asnNumber = asnViewModel.asnNumber.collectAsState()

    Log.e("REOS", "ASNtemplate-ASNtemplate-resultASN.value:" +resultASN.value)

    when(resultASNresponse.value.status)
    {
        "Y" -> {
            asnViewModel.updateasnNumber(resultASNresponse.value.data?.number ?: "")
        }
    }

    if(showDialogQuantityReport.value)
    {
        DialogView(
            updateDialogEditQuantyTittle.value, "",
            onClickCancel = {
                asnViewModel.updateStatusShowDialogQuantityReport(false)
            }, onClickAccept = {
                asnViewModel.updateStatusShowDialogQuantityReport(false)
            }, statusButtonAccept = true, statusButtonIcon = false, context = context
        ) {
            EditextGeneric(
                true,
                "",
                "Ingrese Cantidad",
                "Cantidad Reportada",
                painterResource(id = R.drawable.ic_baseline_numbers_24),
                KeyboardType.Number,
                resultEditText = {  result ->
                    asnViewModel.updateResultQuantityDetail(result)
                }
            )
        }
    }

    DialogLPNDelete(
        context =  context
        , asnViewModel =  asnViewModel
        , onClickAccept = {
            asnViewModel.deleteLPN()
            //onClick.value
            //asnViewModel.handleEvent(Event.EventOne)
            //asnViewModel.resetASN()
            asnViewModel.updateDialogDeleteStatus(false)
                          }
        , onClickCancel = {asnViewModel.updateDialogDeleteStatus(false)}
        , body = {})

    DialogASNDelete(
        context =  context
        , asnViewModel =  asnViewModel
        , onClickAccept = {
            //asnViewModel.deleteLPN()
            //onClick.value
            //asnViewModel.handleEvent(Event.EventOne)
            asnViewModel.resetASN()
            asnViewModel.resetPreASN()
            asnViewModel.updatedialogASNDeleteStatus(false)
        }
        , onClickCancel = {asnViewModel.updatedialogASNDeleteStatus(false)}
        , body = {})

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        // Contenido de LazyColumn
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
        {

                for (line in resultASN.value.data) {
                    // Tu código aquí
                    Card(elevation = 10.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    )
                    {
                        Column(
                            modifier = Modifier
                                .padding(15.dp)
                                .fillMaxWidth()
                        ) {
                            Row() {
                                Column (horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(0.8f)){

                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(0.2f)
                                        .clickable {
                                            asnViewModel.updatedialogASNDeleteStatus(true)
                                            asnViewModel.updateDialogDeleteTittle("Eliminar ASN")
                                            //asnViewModel.updateIndexDelete(index2)
                                            //asnViewModel.updateOnClick(asnViewModel.resetASN())
                                        }
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.baseline_delete_24),
                                        contentDescription = null,
                                        tint = Color.Red,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(5.dp))
                                }
                            }
                            TextWithDivider(text = "ASN ${asnNumber.value}")
                            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)) {
                                Cell(
                                    text = line.U_ItemCode + " " + line.U_ItemName,
                                    title = true,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Row(
                                //horizontalArrangement = Arrangement.Start,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier.weight(0.5f)
                                ) {
                                    Cell(
                                        text = "N° OF",
                                        title = false,
                                        Color.Gray
                                    )
                                    Cell(
                                        text = line.U_Ref2,
                                        title = true,
                                        textAlign = TextAlign.End
                                    )
                                    Spacer(modifier = Modifier.height(5.dp))
                                }
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier.weight(0.5f)
                                ) {
                                    Cell(
                                        text = "Lote",
                                        title = false,
                                        Color.Gray
                                    )
                                    Cell(
                                        text = line.batch,
                                        title = true,
                                        textAlign = TextAlign.End
                                    )
                                    Spacer(modifier = Modifier.height(5.dp))
                                }
                            }
                            TextWithDivider(text = "LPN ("+ resultASN.value.data.last().detail.size + ")")
                            LazyColumn(
                                modifier = Modifier
                                    //.weight(1f)
                                    .fillMaxWidth()
                            ) {
                                itemsIndexed(line.detail) { index2, line2 ->
                                    Card(elevation = 10.dp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp)
                                    )
                                    {
                                        Column(modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp))
                                        {
                                            Row {
                                                Column(
                                                    horizontalAlignment = Alignment.End,
                                                    modifier = Modifier.weight(0.6f)
                                                ) {
                                                    Spacer(modifier = Modifier.height(5.dp))
                                                }
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier
                                                        .weight(0.2f)
                                                        .clickable() {
                                                            asnViewModel.updateStatusShowDialogQuantityReport(
                                                                true
                                                            )
                                                            asnViewModel.updateIndex(index2)
                                                            asnViewModel.updateDialogEditQuantyTittle(
                                                                line2.U_LpnCode
                                                            )
                                                        }
                                                ) {
                                                    Icon(
                                                        imageVector = ImageVector.vectorResource(R.drawable.baseline_edit_24),
                                                        contentDescription = null,
                                                        tint = Color.Red,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.height(5.dp))
                                                }
                                                Column(
                                                    horizontalAlignment = Alignment.End,
                                                    modifier = Modifier
                                                        .weight(0.2f)
                                                        .clickable() {
                                                            asnViewModel.updateDialogDeleteStatus(
                                                                true
                                                            )
                                                            asnViewModel.updateDialogDeleteTittle("Eliminar LPN")
                                                            asnViewModel.updateIndexDelete(index2)
                                                        }
                                                ) {
                                                    Icon(
                                                        imageVector = ImageVector.vectorResource(R.drawable.baseline_delete_24),
                                                        contentDescription = null,
                                                        tint = Color.Red,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.height(5.dp))
                                                }
                                            }
                                            Row {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier.weight(0.1f)
                                                ) {
                                                    Cell(
                                                        text = line2.id + ")",
                                                        title = true,
                                                        textAlign = TextAlign.End
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Column(
                                                    horizontalAlignment = Alignment.End,
                                                    modifier = Modifier.weight(0.9f)
                                                ) {
                                                    Cell(
                                                        text = line2.U_LpnCode,
                                                        title = true,
                                                        textAlign = TextAlign.End
                                                    )
                                                }
                                            }
                                                Row() {
                                                    Column(
                                                        horizontalAlignment = Alignment.Start,
                                                        modifier = Modifier.weight(0.5f)
                                                    ) {
                                                        Cell(
                                                            text = "Cantidad",
                                                            title = false,
                                                            Color.Gray
                                                        )
                                                        Spacer(modifier = Modifier.height(5.dp))
                                                    }
                                                    Column(
                                                        horizontalAlignment = Alignment.End,
                                                        modifier = Modifier.weight(0.5f)
                                                    ) {
                                                        Cell(
                                                            text =  line2.U_Quantity,
                                                            title = true,
                                                            textAlign = TextAlign.End
                                                        )
                                                    }

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }
}


