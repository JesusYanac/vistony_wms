package com.vistony.wms.viewmodel

import android.util.Log
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
import org.bson.types.ObjectId
import java.util.*

class TaskManagementViewModel: ViewModel() {

    private var realm: Realm = Realm.getInstance(Realm.getDefaultConfiguration())

    private val _task = MutableStateFlow(TaskManagementResponse())
    val task: StateFlow<TaskManagementResponse> get() = _task

    class TaskManagementViewModelFactory(): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TaskManagementViewModel() as T
        }
    }

    init {
        getAllTask()
    }

    fun resetTaskStatus(){
        _task.value= TaskManagementResponse(data= emptyList(),status="")
    }

    fun getAllTask(){

        _task.value= TaskManagementResponse(data= emptyList(),status="cargando")

        Realm.getInstanceAsync(realm.configuration, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {

                val taskMngmt = r.where(TaskManagement::class.java)
                    .sort("DateAssignment", Sort.DESCENDING)
                    .findAll()

                taskMngmt?.let { data: RealmResults<TaskManagement> ->

                    val taskTemp:List<TaskManagement> = data.subList(0, data.size)

                    if(taskTemp.isNotEmpty()){

                        val list: MutableList<TaskMngmtAndHeaderDoc> = mutableListOf()

                        taskTemp.forEach{

                            val documentHeader = r.where(StockTransferHeader::class.java)
                                .equalTo("_TaskManagement", it._id)
                                .equalTo("ObjType", it.ObjType)
                                .findFirst()

                            list.add(TaskMngmtAndHeaderDoc(Task=it,Document=documentHeader!!))
                        }

                        _task.value =  TaskManagementResponse(data=list,status = "ok")
                    }else{
                        _task.value =  TaskManagementResponse(data=emptyList(), status = "vacio")
                    }
                }
            }
            override fun onError(exception: Throwable) {
                _task.value =  TaskManagementResponse(data=emptyList(), status = " ${exception.message}")
            }
        })
    }
}