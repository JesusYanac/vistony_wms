package com.vistony.wms.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vistony.wms.model.ItemResponse
import com.vistony.wms.model.Items
import com.vistony.wms.model.ItemsResponse
import com.vistony.wms.model.Sscc
import com.vistony.wms.model.TransfersLayout
import com.vistony.wms.model.TransfersLayoutDetail
import com.vistony.wms.num.TypeCode
import com.vistony.wms.util.APIService
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.syncSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.bson.Document
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date


class TransferStockViewModel: ViewModel() {

    private val _articulo = MutableStateFlow(ItemsResponse())
    val article: StateFlow<ItemsResponse> get() = _articulo

    //Declaración de variables
    // Variables para obtener la lista de TransfersLayout
    private val _transfersLayoutList = MutableStateFlow<List<TransfersLayout>>(emptyList())
    // Variables para almacenar el valor escaneado
    private val _lastPayloadCodeScanned = MutableStateFlow("")
    private val _lastTypeCodeScanned = MutableStateFlow("")
    // Variables para trabajar el dominio de datos
    private val _lastCodeQRScanned = MutableStateFlow("")
    private val _lastCodeBar39Scanned = MutableStateFlow("")
    private val _lastCodeBar128Scanned = MutableStateFlow("")
    // Variables para controlar la visibilidad del popup
    private val _showPopup = MutableStateFlow(false)

    // Variables para la cabecera de la transferencia
    private val _owner = MutableStateFlow("0")
    private val _codeSAP = MutableStateFlow("0")

    // variables del popup (detail layout transfer)
    private val _codeProduct = MutableStateFlow("0")
    private val _nameProduct = MutableStateFlow("(indefinido)")
    private val _codeSSCC = MutableStateFlow("")
    private val _amount = MutableStateFlow("0")
    private val _batch = MutableStateFlow("0")
    private val _codeWarehouseOrigen = MutableStateFlow("(vacio)")
    private val _codeWarehouseDestino = MutableStateFlow("(vacio)")

    val transfersLayoutList: StateFlow<List<TransfersLayout>> get() = _transfersLayoutList
    val lastPayloadCodeScanned: StateFlow<String> get() = _lastPayloadCodeScanned
    val lastTypeCodeScanned: StateFlow<String> get() = _lastTypeCodeScanned
    val lastCodeQRScanned: StateFlow<String> get() = _lastCodeQRScanned
    val lastCodeBar39Scanned: StateFlow<String> get() = _lastCodeBar39Scanned
    val lastCodeBar128Scanned: StateFlow<String> get() = _lastCodeBar128Scanned
    val showPopup: StateFlow<Boolean> get() = _showPopup
    val owner: StateFlow<String> get() = _owner
    val codeSAP: StateFlow<String> get() = _codeSAP
    val codeProduct: StateFlow<String> get() = _codeProduct
    val nameProduct: StateFlow<String> get() = _nameProduct
    val codeSSCC: StateFlow<String> get() = _codeSSCC
    val amount: StateFlow<String> get() = _amount
    val batch: StateFlow<String> get() = _batch
    val codeWarehouseOrigen: StateFlow<String> get() = _codeWarehouseOrigen
    val codeWarehouseDestino: StateFlow<String> get() = _codeWarehouseDestino

    // Métodos para alternar el estado del popup
    fun openPopUp() {
        _showPopup.value = true
    }
    fun closePopUp() {
        _showPopup.value = false
    }
    class TransferStockViewModelFactory : ViewModelProvider.Factory {

        @Suppress
            (  "UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TransferStockViewModel() as T
        }
    }
    init {
        Log.e("jesusdebug", "init")
        loadTransfersLayoutList()
    }

    fun loadTransfersLayoutList() {
        try {
            Log.e("jesusdebug", "loadTransfersLayoutList")
            val transfersLayoutList = Realm.getDefaultInstance()
                .where(TransfersLayout::class.java)
                .sort("createAt", Sort.DESCENDING)
                .limit(50)
                .findAll()

            _transfersLayoutList.value = transfersLayoutList

            Log.e("jesusdebug1", "loadTransfersLayoutList: ${_transfersLayoutList.value.get(0).owner}")
            Log.e("jesusdebug1", "loadTransfersLayoutList: ${_transfersLayoutList.value.toString()}")

        } catch (e: Exception) {
            Log.e("jesusdebug", "catch: $e")
        } finally {
            Realm.getDefaultInstance()?.close()
        }
    }
    // Función para insertar una nueva transferencia en Realm
    fun insertTransfersLayout() {
        // Crea un nuevo objeto TransfersLayout
        val transfersLayout = buildTransfersLayout()

        // Realiza la transacción de Realm
        Realm.getDefaultConfiguration()?.let { config ->
            Realm.getInstanceAsync(config, object : Realm.Callback() {
                override fun onSuccess(realm: Realm) {
                    if (realm.isClosed) {
                        Log.e("jesusdebug", "Realm está cerrado al insertar")
                        return
                    }

                    try {
                        Log.d("jesusdebug", "Éxito al obtener instancia de Realm")
                        realm.executeTransactionAsync({ transactionRealm ->
                            transactionRealm.insertOrUpdate(transfersLayout)
                        }, {
                            Log.d("jesusdebug", "Transacción exitosa")
                            loadTransfersLayoutList()
                        }, { error ->
                            Log.e("jesusdebug", "Falló la transacción: $error")
                        })

                        Log.d("jesusdebug", "¿Éxito al insertar?")
                    } catch (e: Exception) {
                        Log.e("jesusdebug", "Excepción al insertar :$e")
                    } finally {
                        realm.close()
                    }
                }

                override fun onError(exception: Throwable) {
                    Log.e("jesusdebug", "Error al obtener instancia de Realm")
                }
            })
        }
    }
    private fun buildTransfersLayout(): TransfersLayout {
        val transfersLayout = TransfersLayout()
        val time = Date()
        val customUserData : Document? = Realm.getInstance(Realm.getDefaultConfiguration()).syncSession.user.customData
        val employeeId=customUserData?.getInteger("EmployeeId")?:0
        transfersLayout.codeSAP = _codeSAP.value.toInt()
        transfersLayout.createAt = time
        transfersLayout.closeAt = time
        transfersLayout.arrivalTimeSap = time
        transfersLayout.owner = employeeId
        Log.e("jesusdebug", "transfersLayout: $transfersLayout")
        Log.e("jesusdebug", "codeSAP: ${_codeSAP.value}")
        Log.e("jesusdebug", "createAt: ${time}")
        Log.e("jesusdebug", "closeAt: ${time}")
        Log.e("jesusdebug", "arrivalTimeSap: ${time}")
        Log.e("jesusdebug", "owner: ${_owner.value}")


        // Create a new TransfersLayoutDetail object
        val transfersLayoutDetail = TransfersLayoutDetail()

        transfersLayoutDetail.itemCode = _codeProduct.value
        transfersLayoutDetail.sscc = _codeSSCC.value
        transfersLayoutDetail.itemName = _nameProduct.value
        transfersLayoutDetail.batch = _batch.value
        transfersLayoutDetail.quantity = _amount.value.toDouble()
        transfersLayoutDetail.binOrigin = _codeWarehouseOrigen.value
        transfersLayoutDetail.binDestine = _codeWarehouseDestino.value

        Log.e("jesusdebug", "transfersLayoutDetail: $transfersLayoutDetail")
        Log.e("jesusdebug", "itemCode: ${_codeProduct.value}")
        Log.e("jesusdebug", "sscc: ${_codeSSCC.value}")
        Log.e("jesusdebug", "itemName: ${_nameProduct.value}")
        Log.e("jesusdebug", "batch: ${_batch.value}")
        Log.e("jesusdebug", "quantity: ${_amount.value.toDouble()}")
        Log.e("jesusdebug", "binOrigin: ${_codeWarehouseOrigen.value}")
        Log.e("jesusdebug", "binDestine: ${_codeWarehouseDestino.value}")

        // Add the detail object to the transfersLayout
        transfersLayout.detail.add(transfersLayoutDetail)

        return transfersLayout
    }


    fun handleScannedData(type: String, code: String, context: Context) {
        _lastTypeCodeScanned.value = type
        _lastPayloadCodeScanned.value = code
        when (type) {
            "LABEL-TYPE-QRCODE" -> handleQRCodeScan()
            "LABEL-TYPE-CODE39" -> handleCode39Scan()
            "LABEL-TYPE-EAN128" -> handleCodeEAN128Scan()
            else -> showQRCodeMismatchError(context = context)
        }
        openPopUp()
    }

    fun handleQRCodeScan() {
        _lastCodeQRScanned.value = _lastPayloadCodeScanned.value
        // obtener datos del rotulado
        _codeProduct.value = _lastCodeQRScanned.value.split("|")[0]
        _nameProduct.value = _lastCodeQRScanned.value.split("|")[2]
        _batch.value = _lastCodeQRScanned.value.split("|")[1]
    }
    fun handleWarehouseCodeScan() {
        val code = _lastCodeBar39Scanned.value
        when {
            _codeWarehouseOrigen.value == "(vacio)" -> _codeWarehouseOrigen.value = code
            _codeWarehouseDestino.value == "(vacio)" && _codeWarehouseOrigen.value != code -> _codeWarehouseDestino.value = code
            _codeWarehouseOrigen.value == code -> _codeWarehouseOrigen.value = "(vacio)"
            _codeWarehouseDestino.value == code -> _codeWarehouseDestino.value = "(vacio)"
        }
        Log.e("jesusdebug", "codeWarehouseOrigen: ${_codeWarehouseOrigen.value}")
        Log.e("jesusdebug", "codeWarehouseDestino: ${_codeWarehouseDestino.value}")
    }
    fun handleCode39Scan() {
        _lastCodeBar39Scanned.value = _lastPayloadCodeScanned.value
        handleWarehouseCodeScan()
    }
    fun handleCodeEAN128Scan() {
        _lastCodeBar128Scanned.value = _lastPayloadCodeScanned.value
        _codeSSCC.value = _lastPayloadCodeScanned.value
        // realizar consulta SSCC
        getSSCC(_lastCodeBar128Scanned.value)
        // guardar datos
    }
    fun showQRCodeMismatchError(context: Context) {
        Toast.makeText(context, "El rotulado escaneado no corresponde a un código QR", Toast.LENGTH_LONG).show()
    }


    fun isDataValid(): Boolean {
        return (_codeProduct.value != "0"
                && _nameProduct.value != "(indefinido)"
                && _batch.value != "0"
                && _amount.value != "0"
                && _codeWarehouseOrigen.value != "(vacio)"
                && _codeWarehouseDestino.value != "(vacio)")
    }

    fun setAmount(it: String) {
        _amount.value = it
    }
    fun getSSCC(codeSSCC: String) {
        val code:String = codeSSCC.substring(2)
        Log.e("jesusdebug", "getSSCC: $code")
        viewModelScope.launch(Dispatchers.Default) {
            APIService.getInstance()
                .getSscc(code, "NaN")
                .enqueue(object : Callback<Sscc> {
                    override fun onResponse(call: Call<Sscc>, response: Response<Sscc>) {
                        Log.e("jesusdebug", "se recibió response: $response")
                        closePopUp()
                        if (response.isSuccessful) {

                            val items: List<ItemResponse> = response.body()?.data!!.vISWMSSCC1Collection.map {
                                ItemResponse(
                                    item = Items(
                                        ItemName = it.uDscription,
                                        ItemCode = it.uItemCode
                                    ),
                                    status = "ok",
                                    lote = it.uBatch,
                                    quantity = it.uQuantity.toDouble(),
                                    expireDate = it.exDate,
                                    inDate = it.uDate

                                )
                            }


                            Log.e("jesusdebug", "ItemResponse:")
                            for (item in items) {
                                Log.e("jesusdebug", "---------------------------------------------")
                                Log.e("jesusdebug", "  item: ${item.item}")
                                Log.e("jesusdebug", "  status: ${item.status}")
                                Log.e("jesusdebug", "  lote: ${item.lote}")
                                _batch.value = item.lote
                                Log.e("jesusdebug", "  quantity: ${item.quantity}")
                                _amount.value = item.quantity.toString()

                                Log.e("jesusdebug", "  expireDate: ${item.expireDate}")
                                Log.e("jesusdebug", "  inDate: ${item.inDate}")
                                Log.e("jesusdebug", "---------------------------------------------")

                                Log.e("jesusdebug", "  itemName: ${item.item.ItemName}")
                                _nameProduct.value = item.item.ItemName
                                Log.e("jesusdebug", "  itemCodeProduct: ${item.item.ItemCode}")
                                _codeProduct.value = item.item.ItemCode
                                Log.e("jesusdebug", "  itemQtyPallet: ${item.item.QtyPallet}")
                                _codeSAP.value = "0"
                                Log.e("jesusdebug", "---------------------------------------------")

                            }

                            _articulo.value = ItemsResponse(
                                items = items,
                                type = TypeCode.SSCC,
                                nameSscc = response.body()?.data!!.code,
                                defaultLocation = response.body()?.data!!.uBtringinCode,
                                status = "ok",
                                statusSscc = response.body()?.data!!.status,
                                warehouse = response.body()?.data!!.uWhsCode,
                                //tracking = response.body()?.data!!.TrackingCollection
                            )

                            Log.e("jesusdebug", "articulo:")
                            Log.e("jesusdebug", "  items: ${_articulo.value.items}")
                            Log.e("jesusdebug", "  type: ${_articulo.value.type}")
                            Log.e("jesusdebug", "  nameSscc: ${_articulo.value.nameSscc}")
                            Log.e("jesusdebug", "  defaultLocation: ${_articulo.value.defaultLocation}")
                            Log.e("jesusdebug", "  status: ${_articulo.value.status}")
                            Log.e("jesusdebug", "  statusSscc: ${_articulo.value.statusSscc}")
                            Log.e("jesusdebug", "  warehouse: ${_articulo.value.warehouse}")
                            Log.e("jesusdebug", "---------------------------------------------")

                        } else {
                            if (response.code() == 424) {
                                _articulo.value =
                                    ItemsResponse(items = emptyList(), status = "El código SSCC $code, no se encontró en SAP", type = TypeCode.SSCC)
                            } else {
                                _articulo.value =
                                    ItemsResponse(items = emptyList(), status = "El servidor respondió con código ${response.code()}", type = TypeCode.SSCC)
                            }
                        }
                        openPopUp()
                    }

                    override fun onFailure(call: Call<Sscc>, error: Throwable) {
                        Log.e("JEPICAME", "ERRRP => ${error.message}")
                        _articulo.value = ItemsResponse(items = emptyList(), status = "${error.message}", type = TypeCode.SSCC)
                    }
                })
        }
    }


}
