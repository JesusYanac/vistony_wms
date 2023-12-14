package com.vistony.wms.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vistony.wms.model.TransfersLayout
import com.vistony.wms.model.TransfersLayoutDetail
import io.realm.Realm
import io.realm.Sort


class TransferStockViewModel: ViewModel() {


    private val _transfersLayoutList = MutableLiveData<List<TransfersLayout>>()
    val transfersLayoutList: LiveData<List<TransfersLayout>> get() = _transfersLayoutList

    class TransferStockViewModelFactory(
    ): ViewModelProvider.Factory {
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
        Log.e("jesusdebug", "loadTransfersLayoutList")
        Realm.getDefaultConfiguration()?.let {
            Realm.getInstanceAsync(it, object : Realm.Callback() {
                override fun onSuccess(realm: Realm) {
                    try {
                        Log.e("jesusdebug", "onSuccess")
                        if (realm.isClosed) {
                            return
                        }
                        val transfersLayoutList = realm.where(TransfersLayout::class.java)
                            .sort("createAt", Sort.DESCENDING) // Reemplaza 'timestampField' con el campo de tiempo que estás usando
                            .limit(50).findAll()
                        Log.e("jesusdebug", "TransferStockViewModel-loadTransfersLayoutList-transfersLayoutList $transfersLayoutList")
                        _transfersLayoutList.postValue(transfersLayoutList)
                    } catch (e: Exception) {
                        Log.e("jesusdebug", "catch")
                    }
                }

                override fun onError(exception: Throwable) {
                    Log.e("jesusdebug", "onError")
                }
            })
        }
    }    // Función para insertar una nueva transferencia en Realm
    fun insertTransfersLayout(
        codePalet: String,
        codeAlmacen1: String,
        codeAlmacen2: String,
        cantidad: String,
    ) {
        // Create a new TransfersLayout object
        val transfersLayout = TransfersLayout()
        transfersLayout.codeSAP = 0

        // Create a new TransfersLayoutDetail object
        val transfersLayoutDetail = TransfersLayoutDetail()

        if (codePalet.contains("|")) {
            transfersLayoutDetail.itemCode = codePalet.split("|")[0]
            transfersLayoutDetail.itemName = codePalet.split("|")[1]
            transfersLayoutDetail.batch = codePalet.split("|")[2]
            // Entrada manual cuando se escanea Qr
            transfersLayoutDetail.quantity = cantidad.toDouble()
        } else {
            transfersLayoutDetail.sscc = codePalet

            // Consultar el sscc y obtener itemCode, itemName, batch, quantity
            transfersLayoutDetail.itemCode = ""
            transfersLayoutDetail.itemName = ""
            transfersLayoutDetail.batch = ""
            transfersLayoutDetail.quantity = 0.0
        }

        transfersLayoutDetail.binOrigin = codeAlmacen1
        transfersLayoutDetail.binDestine = codeAlmacen2

        // Add the detail object to the transfersLayout
        transfersLayout.detail.add(transfersLayoutDetail)

        // Perform the Realm transaction
        Realm.getDefaultConfiguration()?.let {
            Realm.getInstanceAsync(it, object : Realm.Callback() {
                override fun onSuccess(realm: Realm) {
                    try {
                        Log.e("jesusdebug", "onSuccess insert")
                        if (realm.isClosed) {
                            Log.e("jesusdebug", "realm is closed insert")
                            return
                        }
                        realm.executeTransactionAsync { transactionRealm ->
                            transactionRealm.insertOrUpdate(transfersLayout)
                        }; Realm.Transaction.OnSuccess {
                            Log.e("jesusdebug", "Transaction succeeded")
                        }; Realm.Transaction.OnError { error ->
                            Log.e("jesusdebug", "Transaction failed: $error")
                        }

                        Log.e("jesusdebug", "exito? insert")

                    } catch (e: Exception) {
                        Log.e("jesusdebug", "catch insert :$e")
                    }
                }

                override fun onError(exception: Throwable) {
                    Log.e("jesusdebug", "onError insert")
                }
            })
        }
    }
}
