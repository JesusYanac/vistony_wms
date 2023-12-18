import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vistony.wms.R
import com.vistony.wms.component.Editext
import com.vistony.wms.component.Editext2
import com.vistony.wms.screen.TableCell
import com.vistony.wms.ui.theme.AzulVistony202
import com.vistony.wms.viewmodel.TransferStockViewModel

@Composable
fun TransferStockDialog(
    transferStockViewModel: TransferStockViewModel
) {

    val showPopup = transferStockViewModel.showPopup.collectAsState()
    val codeProduct: State<String> = transferStockViewModel.codeProduct.collectAsState()
    val nameProduct: State<String> = transferStockViewModel.nameProduct.collectAsState()
    val amount: State<String> = transferStockViewModel.amount.collectAsState()
    val batch: State<String> = transferStockViewModel.batch.collectAsState()
    val codeSSCC: State<String> = transferStockViewModel.codeSSCC.collectAsState()

    val codeWarehouseOrigen: State<String> = transferStockViewModel.codeWarehouseOrigen.collectAsState()
    val codeWarehouseDestino: State<String> = transferStockViewModel.codeWarehouseDestino.collectAsState()

    val nombre = remember { mutableStateOf("") }
    val codigoProducto = remember { mutableStateOf("") }
    val lote = remember { mutableStateOf("") }
    val cantidad = remember { mutableStateOf("") }

    nombre.value = nameProduct.value
    codigoProducto.value = codeProduct.value
    lote.value = batch.value
    cantidad.value = amount.value

    val almacenOrigen = remember { mutableStateOf("") }
    val almacenDestino = remember { mutableStateOf("") }

    almacenOrigen.value = codeWarehouseOrigen.value
    almacenDestino.value = codeWarehouseDestino.value


    if (showPopup.value) {
        AlertDialog(
            onDismissRequest = {
                transferStockViewModel.closePopUp()
            },
            title = {
                Text(nombre.value, textAlign = TextAlign.Center)
            },

            text = {
                // Mostrar el código escaneado en el cuerpo del modal
                Box(modifier = Modifier.padding(vertical = 24.dp)) {
                    LazyColumn {
                        item {
                            Editext(
                                status = false,
                                text = codigoProducto,
                                placeholder = "Ingrese Codigo",
                                label = "Codigo",
                                painter = painterResource(id = R.drawable.ic_baseline_file_copy_24),
                                keyboardType = KeyboardType.Number,
                                limitCharacters = 254
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        item {
                            Editext2(
                                status = (codeSSCC.value == "0" || codeSSCC.value == ""),
                                text = cantidad,
                                placeholder = "Ingrese Cantidad",
                                label = "Cantidad",
                                painter = painterResource(id = R.drawable.ic_baseline_numbers_24),
                                keyboardType = KeyboardType.Number,
                                limitCharacters = 254,
                                transferStockViewModel = transferStockViewModel
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        item {
                            Editext(
                                status = false,
                                text = lote,
                                placeholder = "Ingrese Lote",
                                label = "Lote",
                                painter = painterResource(id = R.drawable.ic_baseline_numbers_24),
                                keyboardType = KeyboardType.Number,
                                limitCharacters = 254
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        item {
                            Editext(
                                status = false,
                                text = almacenOrigen,
                                placeholder = "Ingrese almacén origen",
                                label = "Almacen Origen",
                                painter = painterResource(id = R.drawable.ic_baseline_packing_24),
                                keyboardType = KeyboardType.Text,
                                limitCharacters = 254
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        item {
                            Editext(
                                status = false,
                                text = almacenDestino,
                                placeholder = "Ingrese almacén destino",
                                label = "Almacen Destino",
                                painter = painterResource(id = R.drawable.ic_baseline_packing_24),
                                keyboardType = KeyboardType.Text,
                                limitCharacters = 254
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ButtonView(
                        description = "Cancelar",
                        OnClick = {
                            transferStockViewModel.closePopUp()
                        },
                        context = LocalContext.current,
                        backGroundColor = Color.Gray,
                        textColor = Color.White,
                        status = true
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    ButtonView(
                        description = "Confirmar",
                        OnClick = {
                            val all_IsOk = transferStockViewModel.isDataValid();
                            if(all_IsOk){
                                transferStockViewModel.insertTransfersLayout() // devolver true luego de validar los campos en el popup

                                //transferStockViewModel.loadTransfersLayoutList()
                                transferStockViewModel.closePopUp()
                            }
                        },
                        context = LocalContext.current,
                        backGroundColor = AzulVistony202,
                        textColor = Color.White,
                        status = true
                    )
                }
            },
        )
    }
}


@Composable
fun RowScope.ButtonView(
    description:String
    ,OnClick:() ->Unit
    ,status: Boolean=false
    ,context: Context
    ,backGroundColor: Color = Color.Unspecified
    ,textColor: Color = Color.Unspecified,
) {
    Box(
        modifier = Modifier
            //.size(200.dp)
            .weight(1f)
            .height(50.dp)
            //.fillMaxWidth()

            .background(
                backGroundColor, RoundedCornerShape(4.dp)
            )
            .clickable {
                if (status) {
                    OnClick()
                } else {
                    Toast
                        .makeText(
                            context,
                            "El Boton se encuenta deshabilitado",
                            Toast.LENGTH_LONG
                        )
                        .show()
                }
            },
        contentAlignment = Alignment.Center,


        ) {
        Row()
        {
            TableCell(
                text = description,
                color = textColor,
                title = true,
                weight = 1f,
                textAlign = TextAlign.Center
            )
        }
    }
}