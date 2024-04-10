package com.vistony.wms.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vistony.wms.model.Activity
import com.vistony.wms.model.BinLocations
import com.vistony.wms.model.DefaultLocation
import com.vistony.wms.model.Inventory
import com.vistony.wms.model.ItemGroup
import com.vistony.wms.model.ItemResponse
import com.vistony.wms.model.Items
import com.vistony.wms.model.ItemsResponse
import com.vistony.wms.model.ListItems
import com.vistony.wms.model.Printer
import com.vistony.wms.model.ProductionReceipt
import com.vistony.wms.model.QualityControl
import com.vistony.wms.model.Sscc
import com.vistony.wms.model.StockTransferBody
import com.vistony.wms.model.StockTransferHeader
import com.vistony.wms.model.StockTransferSubBody
import com.vistony.wms.model.TaskManagement
import com.vistony.wms.model.TransfersLayout
import com.vistony.wms.model.TransfersLayoutDetail
import com.vistony.wms.model.Users
import com.vistony.wms.model.WarehouseResponse
import com.vistony.wms.model.Warehouses
import com.vistony.wms.num.TypeCode
import com.vistony.wms.util.APIService
import com.vistony.wms.util.DatasourceSingleton
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.syncSession
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.Credentials
import io.realm.mongodb.sync.SyncConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.bson.Document
import org.bson.types.ObjectId
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date
import java.util.concurrent.Flow


class BlockLocationViewModel: ViewModel() {
    
    //Declaración de variables
    private val _binLocations = MutableStateFlow<List<BinLocations>>(emptyList())
    val binLocations: StateFlow<List<BinLocations>> get() = _binLocations

    private val _filteredBinLocations = MutableStateFlow<List<BinLocations>>(emptyList())
    val filteredBinLocations: StateFlow<List<BinLocations>> get() = _filteredBinLocations
    
    private  val _showPopup = MutableStateFlow(false)
    val showPopup: StateFlow<Boolean> get() = _showPopup

    private val _scaffoldMessage = MutableStateFlow("")
    val scaffoldMessage: StateFlow<String> get() = _scaffoldMessage

    private val _lastPayloadCodeScanned = MutableStateFlow("")
    val lastPayloadCodeScanned: StateFlow<String> get() = _lastPayloadCodeScanned

    //Realm
    private var realm: Realm = Realm.getInstance(Realm.getDefaultConfiguration())
    private val customUserData: Document? = realm.syncSession.user.customData

    private var configBranch = SyncConfiguration
        .Builder(realm.syncSession.user, customUserData?.getString("Branch") ?: "")
        .allowQueriesOnUiThread(true)
        .allowWritesOnUiThread(true)
        .build()

    // Métodos para alternar el estado del popup
    fun openPopUp() {
        _showPopup.value = true
    }
    fun closePopUp() {
        // Cerramos el popup
        _showPopup.value = false

    }
    class BlockLocationViewModelFactory : ViewModelProvider.Factory {
        @Suppress
            (  "UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BlockLocationViewModel() as T
        }
    }
    init {
        Log.e("jesusdebug", "init")
        loadBinLocationLayoutList()
    }

    private fun loadBinLocationLayoutList() {
        try {
            Log.e("jesusdebug", "loadBinLocationLayoutList")
            Realm.getInstanceAsync(configBranch, object : Realm.Callback() {
                override fun onSuccess(realm: Realm) {
                    val whsx = realm.where(BinLocations::class.java)
                        .equalTo("Realm_Id", customUserData?.getString("Branch") ?: "")
                        .findAll()

                    _binLocations.value = whsx
                    _filteredBinLocations.value = whsx
                }

            })

        } catch (e: Exception) {
            Log.e("jesusdebug", "catch: $e")
        }
    }
    // Función para insertar una nueva transferencia en Realm

    fun handleScannedData(type: String, code: String, context: Context) {
        when (type) {
            "LABEL-TYPE-QRCODE" -> handleQRCodeScan()
            "LABEL-TYPE-CODE39" -> filterLocationsByCode(code)
            "LABEL-TYPE-EAN128" -> handleCodeEAN128Scan()
            else -> _scaffoldMessage.value = "No se reconoce el codigo escaneado"
        }
        openPopUp()
    }

    private fun handleQRCodeScan() {
        _scaffoldMessage.value = "Solo se puede bloquear ubicaciones"
    }

    private fun handleCodeEAN128Scan() {
        _scaffoldMessage.value = "Solo se puede bloquear ubicaciones"
    }

    fun filterLocationsByCode(code: String) {
        _lastPayloadCodeScanned.value = code
        if (code.isEmpty() || code.isBlank() || code == "") {
            _filteredBinLocations.value = _binLocations.value
        } else {
            _filteredBinLocations.value = _binLocations.value.filter { it.BinCode.contains(code) }
        }
    }

    fun updateStatusLocation(lockpick: String) {
        Realm.getInstanceAsync(configBranch, object : Realm.Callback() {
            override fun onSuccess(realm: Realm) {
                val whsx: BinLocations? = realm.where(BinLocations::class.java)
                    .equalTo("BinCode", _lastPayloadCodeScanned.value)
                    .findFirst()

                //ejecutar transaccion y modificar el campo LockPick de la ubicacion y ponerle "N"
                realm.executeTransaction {
                    whsx?.LockPick = lockpick
                }
                realm.close()
            }

            override fun onError(exception: Throwable) {
                _scaffoldMessage.value = "Error de Base de datos"
            }
        })
    }

    fun setLastPayloadCodeScanned(it: String) {
        _lastPayloadCodeScanned.value = it
    }

    private fun showScaffoldMessage(context: Context) {
        Toast.makeText(context, _scaffoldMessage.value, Toast.LENGTH_LONG).show()
    }

}
