package com.vistony.wms.model.finditem

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FindItemViewModel(
    private val findItemRepository: FindItemRepository,
    private val Imei:String,
    private val context: Context
): ViewModel()  {
    private val _result = MutableStateFlow(FindItemEntity())
    val result: StateFlow<FindItemEntity> get() = _result

    class FindItemViewModelFactory(
        private val findItemRepository: FindItemRepository,
        private val Imei:String,
        private val context: Context
    ): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FindItemViewModel(
                findItemRepository,
                Imei,
                context
            ) as T
        }
    }


    init{
        //getInvoices()
        viewModelScope.launch {
            // Observar cambios en invoicesRepository.invoices
            findItemRepository.result.collect { newResult ->
                // Actualizar el valor de _invoices cuando haya cambios
                _result.value = newResult
            }
        }
    }

    fun reset() {
        _result.value = FindItemEntity()
    }

    fun getFindItem(itemCode:String)
    {
        viewModelScope.launch {
            findItemRepository.getFindItem(Imei,itemCode)
        }
    }


}