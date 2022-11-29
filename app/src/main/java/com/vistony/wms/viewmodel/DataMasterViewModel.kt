package com.vistony.wms.viewmodel;

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vistony.wms.model.*
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.syncSession
import io.realm.mongodb.sync.SyncConfiguration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.bson.Document

internal class DataMasterViewModel: ViewModel() {
    val realm: Realm = Realm.getInstance(Realm.getDefaultConfiguration())
    val customUserData: Document = realm.syncSession.user.customData

    val config = SyncConfiguration
        .Builder(realm.syncSession.user, customUserData?.getString("Branch") ?: "")
        .build()

    val _dataMaster = MutableStateFlow(ListDataMasterResponse())
    val dataMaster: StateFlow<ListDataMasterResponse> get() = _dataMaster

    class DataMasterViewModelFactory() : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DataMasterViewModel() as T
        }
    }

    init{
        getQuality()
        getItemGroup()
    }

    fun getQuality() {

        val master: DataMasterItem? =_dataMaster.value.values.find{ xd -> xd.nombre.equals("quality")}

        if (master != null) {
            master.status="cargando"
            master.size=0

            Realm.getInstanceAsync(config, object : Realm.Callback() {
                override fun onSuccess(r: Realm) {

                    val qualityRpta = r.where(QualityControl::class.java).findAll()

                    val temp: List<QualityControl> = qualityRpta.subList(0, qualityRpta.size)

                    if (temp.isNotEmpty()) {
                        master.status= "ok"
                        master.size=temp.size
                    } else {
                        master.status= "vacio"
                        master.size=temp.size
                    }
                }
                override fun onError(exception: Throwable) {
                    master.status= "vacio"
                    master.size=0
                    master.errorMessage=exception.message.toString()
                }
            })
        }
    }

    fun getItemGroup() {

        val master: DataMasterItem? =_dataMaster.value.values.find{ xd -> xd.nombre.equals("itemgroup")}

        if (master != null) {
            master.status="cargando"
            master.size=0

            Realm.getInstanceAsync(config, object : Realm.Callback() {
                override fun onSuccess(r: Realm) {

                    val qualityRpta = r.where(ItemGroup::class.java).findAll()

                    val temp: List<ItemGroup> = qualityRpta.subList(0, qualityRpta.size)

                    if (temp.isNotEmpty()) {
                        master.status= "ok"
                        master.size=temp.size
                    } else {
                        master.status= "vacio"
                        master.size=temp.size
                    }
                }
                override fun onError(exception: Throwable) {
                    master.status= "vacio"
                    master.size=0
                    master.errorMessage=exception.message.toString()
                }
            })
        }
    }

    fun getTaskType(){

    }
}