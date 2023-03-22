package com.vistony.wms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vistony.wms.model.*
import io.realm.Realm
import io.realm.kotlin.syncSession
import io.realm.mongodb.sync.SyncConfiguration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class QualityViewModel(): ViewModel() {

    private var realm: Realm = Realm.getInstance(Realm.getDefaultConfiguration())

    private var configCountry = SyncConfiguration
        .Builder(realm.syncSession.user, "public")
        .build()

    private val _quality = MutableStateFlow(QualityControlResponse())
    val quality: StateFlow<QualityControlResponse> get() = _quality

    class QualityViewModelFactory() : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return QualityViewModel() as T
        }
    }

    fun resetQualityStatus() {
        _quality.value = QualityControlResponse(data = QualityControl(), status = "")
    }

    fun getQuality(objType:Int) {

        _quality.value = QualityControlResponse(data = QualityControl(), status = "cargando")

        Realm.getInstanceAsync(configCountry, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {

                val qualityRpta = r.where(QualityControl::class.java)
                    .equalTo("ObjType",objType)
                   // .findAll()
                    .findFirst()

                if (qualityRpta!=null) {
                   // val countTemp:List<QualityControl> = qualityRpta.subList(0, qualityRpta.size)

                    _quality.value = QualityControlResponse(data = qualityRpta, status = "ok")
                } else {
                    _quality.value =QualityControlResponse(data = QualityControl(), status = "vacio")
                }
            }

            override fun onError(exception: Throwable) {
                _quality.value = QualityControlResponse(data = QualityControl(), status = " ${exception.message}")
            }
        })
    }
}