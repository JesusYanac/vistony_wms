package com.vistony.wms.viewmodel

import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ZebraViewModel: ViewModel() {

    private val _data = MutableStateFlow("")
    val data: StateFlow<String> get() = _data

    class ZebraViewModelFactory(): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ZebraViewModel() as T
        }
    }

    fun setData(data:String){
        _data.value=data
    }
}