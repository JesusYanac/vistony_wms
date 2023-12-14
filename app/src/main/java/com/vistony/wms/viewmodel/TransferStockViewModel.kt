package com.vistony.wms.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vistony.wms.model.TransfersLayout
import com.vistony.wms.model.Warehouses
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.syncSession
import io.realm.mongodb.sync.SyncConfiguration
import org.bson.Document


class TransferStockViewModel: ViewModel() {

    //private var realm: Realm = Realm.getInstance(Realm.getDefaultConfiguration())
    //private val customUserData: Document? = realm.syncSession.user.customData

    /*private var configBranch = SyncConfiguration
        .Builder(realm.syncSession.user, customUserData?.getString("Branch") ?: "")
        .build()*/

    // LiveData para la lista de transferencias
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
    // Función para recuperar la lista de transferencias desde Realm
    /*fun loadTransfersLayoutList2() {
        Log.e("jesusdebug", "loadTransfersLayoutList")
        realm.executeTransactionAsync { realm ->
            val results: RealmResults<TransfersLayout> = realm.where(TransfersLayout::class.java).findAll()
            _transfersLayoutList.postValue(realm.copyFromRealm(results))
        }
    }*/

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
                        val transfersLayoutList = realm.where(TransfersLayout::class.java).findAll()
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
    fun insertTransfersLayout(codePalet: String, codeAlmacen1: String, codeAlmacen2: String, cantidad: String) {
        val transfersLayout = TransfersLayout()
        transfersLayout.codeSAP = 0
        if(codePalet.contains("|")){
            transfersLayout.detail.first()!!.itemCode = codePalet.split("|")[0]
            transfersLayout.detail.first()!!.itemName = codePalet.split("|")[1]
            transfersLayout.detail.first()!!.batch = codePalet.split("|")[2]
            //Entrada manual cuando se escanea Qr
            transfersLayout.detail.first()!!.quantity = cantidad.toDouble()
        }else{
            transfersLayout.detail.first()!!.sscc = codePalet

            //consultar el sscc y ontener itemCode, itemName, batch, quantity
            transfersLayout.detail.first()!!.itemCode = ""
            transfersLayout.detail.first()!!.itemName = ""
            transfersLayout.detail.first()!!.batch = ""
            transfersLayout.detail.first()!!.quantity = 0.0
        }

        transfersLayout.detail.first()!!.binOrigin = codeAlmacen1
        transfersLayout.detail.first()!!.binDestine = codeAlmacen2
        /*realm.executeTransactionAsync { realm ->
            realm.insertOrUpdate(transfersLayout)
        }*/
        Realm.getDefaultConfiguration()?.let {
            Realm.getInstanceAsync(it, object : Realm.Callback() {
                override fun onSuccess(realm: Realm) {
                    try {
                        Log.e("jesusdebug", "onSuccess insert")
                        if (realm.isClosed) {
                            Log.e("jesusdebug", "realm is closed insert")
                            return
                        }
                        realm.insertOrUpdate(transfersLayout)
                        Log.e("jesusdebug", "exito? insert")

                    } catch (e: Exception) {
                        Log.e("jesusdebug", "catch insert")
                    }
                }

                override fun onError(exception: Throwable) {
                    Log.e("jesusdebug", "onError insert")
                }
            })
        }
    }



    /*override fun onCleared() {
        super.onCleared()
        realm.close()
    }*/
}
