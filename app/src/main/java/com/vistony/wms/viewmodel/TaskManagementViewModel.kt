package com.vistony.wms.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vistony.wms.model.*
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.bson.types.ObjectId
import java.util.*

class TaskManagementViewModel: ViewModel() {

    private var realm: Realm = Realm.getInstance(Realm.getDefaultConfiguration())

    private val _task = MutableStateFlow(TaskManagementResponse())
    val task: StateFlow<TaskManagementResponse> get() = _task

    private val _taskUnit = MutableStateFlow(TaskManagementResponse())
    val taskUnit: StateFlow<TaskManagementResponse> get() = _taskUnit

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
        try {
            _task.value = TaskManagementResponse(data = emptyList(), status = "cargando")

            Realm.getInstanceAsync(realm.configuration, object : Realm.Callback() {
                override fun onSuccess(r: Realm) {
                    Log.e("REOS", "TaskManagementViewModel-getAllTask-r: " + r)
                    val currentDate = Date()
                    val calendar = Calendar.getInstance()
                    calendar.time = currentDate
                    calendar.add(Calendar.DATE, -3)
                    val threeDaysAgo = calendar.time

                    val combinedResults = mutableListOf<TaskManagement>()

                    val taskMngmt = r.where(TaskManagement::class.java)
                        .greaterThanOrEqualTo("DateAssignment", threeDaysAgo)
                        .equalTo("Status", "Terminado")
                        .sort("DateAssignment", Sort.DESCENDING)
                        .findAll()

                    val taskMngmt2 = r.where(TaskManagement::class.java)
                        //.greaterThanOrEqualTo("DateAssignment", threeDaysAgo)
                        .notEqualTo("Status", "Terminado")
                        .sort("DateAssignment", Sort.DESCENDING)
                        .findAll()

                    combinedResults.addAll(taskMngmt)
                    combinedResults.addAll(taskMngmt2)
                    combinedResults.sortByDescending { it.DateAssignment }
                    //combinedResults?.let { data: RealmResults<TaskManagement> ->
                    combinedResults?.let { data ->
                        Log.e("REOS", "TaskManagementViewModel-getAllTask-data: " + data.toString())
                        val taskTemp: List<TaskManagement> = data.subList(0, data.size)

                        if (taskTemp.isNotEmpty()) {

                            val list: MutableList<TaskMngmtAndHeaderDoc> = mutableListOf()

                            taskTemp.forEach {

                                val documentHeader = r.where(StockTransferHeader::class.java)
                                    .equalTo("_TaskManagement", it._id)
                                    .equalTo("ObjType", it.ObjType)
                                    .findFirst()

                                if (documentHeader == null) {
                                    Log.e("REOS", "TaskManagementViewModel-getAllTask-id" + it._id)
                                    _task.value = TaskManagementResponse(
                                        data = list,
                                        status = "La tarea no tiene un documento relacionado."
                                    )
                                } else {
                                    list.add(
                                        TaskMngmtAndHeaderDoc(
                                            Task = it,
                                            Document = documentHeader!!
                                        )
                                    )
                                }
                            }

                            _task.value = TaskManagementResponse(data = list, status = "ok")
                        } else {
                            _task.value =
                                TaskManagementResponse(data = emptyList(), status = "vacio")
                        }
                    }
                }

                override fun onError(exception: Throwable) {
                    Log.e("REOS", "TaskManagementViewModel-getAllTask-exception: " + exception)
                    _task.value =
                        TaskManagementResponse(data = emptyList(), status = " ${exception.message}")
                }
            })
        }catch (e:Exception){
            Log.e("REOS","TaskManagementViewModel-getAllTask-Error: "+e.toString())
        }
    }

    fun getTask(idMenchandise:ObjectId){

        _taskUnit.value= TaskManagementResponse(data= emptyList(),status="cargando")

        Realm.getInstanceAsync(realm.configuration, object : Realm.Callback() {
            override fun onSuccess(r: Realm) {
                Log.e("REOS","TaskManagementViewModel-getAllTask-r: "+r)

                val taskMngmt = r.where(TaskManagement::class.java)
                    .equalTo("_id", idMenchandise)
                    .sort("DateAssignment", Sort.DESCENDING)
                    .findAll()

                taskMngmt?.let { data ->
                    Log.e("REOS","TaskManagementViewModel-getAllTask-data: "+data.toString())
                    val taskTemp:List<TaskManagement> = data.subList(0, data.size)

                    if(taskTemp.isNotEmpty()){

                        val list: MutableList<TaskMngmtAndHeaderDoc> = mutableListOf()

                        taskTemp.forEach{

                            val documentHeader = r.where(StockTransferHeader::class.java)
                                .equalTo("_TaskManagement", it._id)
                                .equalTo("ObjType", it.ObjType)
                                .findFirst()

                            if(documentHeader==null){
                                Log.e("REOS","TaskManagementViewModel-getAllTask-id"+it._id)
                                _taskUnit.value =  TaskManagementResponse(data=list,status = "La tarea no tiene un documento relacionado.")
                            }else{
                                list.add(TaskMngmtAndHeaderDoc(Task=it,Document=documentHeader!!))
                            }
                        }

                        _taskUnit.value =  TaskManagementResponse(data=list,status = "ok")
                    }else{
                        _taskUnit.value =  TaskManagementResponse(data=emptyList(), status = "vacio")
                    }
                }
            }
            override fun onError(exception: Throwable) {
                Log.e("REOS","TaskManagementViewModel-getAllTask-exception: "+exception)
                _taskUnit.value =  TaskManagementResponse(data=emptyList(), status = " ${exception.message}")
            }
        })
    }
}