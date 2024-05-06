package com.vistony.wms.asn.mvvm

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ASNViewModel(
    private val asnRepository: ASNRepository,
    private val context: Context): ViewModel()  {

    private val _resultPreASN = MutableStateFlow(PreASNEntity())
    val resultPreASN: StateFlow<PreASNEntity> get() = _resultPreASN

    private val _showLoadingDialog= MutableStateFlow(false)
    val showLoadingDialog: StateFlow<Boolean> get() = _showLoadingDialog

    private val _showDialogQuantityReport= MutableStateFlow(false)
    val showDialogQuantityReport: StateFlow<Boolean> get() = _showDialogQuantityReport

    private val _index= MutableStateFlow(0)
    val index: StateFlow<Int> get() = _index

    private val _resultASN = MutableStateFlow(ASNEntity())
    val resultASN: StateFlow<ASNEntity> get() = _resultASN

    private val _dialogEditQuantyTittle= MutableStateFlow("")
    val dialogEditQuantyTittle: StateFlow<String> get() = _dialogEditQuantyTittle

    private val _printBottomBar= MutableStateFlow("")
    val printBottomBar: StateFlow<String> get() = _printBottomBar

    private val _dialogPrintStatus= MutableStateFlow(false)
    val dialogPrintStatus: StateFlow<Boolean> get() = _dialogPrintStatus

    private val _dialogLPNDeleteStatus= MutableStateFlow(false)
    val dialogLPNDeleteStatus: StateFlow<Boolean> get() = _dialogLPNDeleteStatus

    private val _dialogLPNDeleteTittle= MutableStateFlow("")
    val dialogLPNDeleteTittle: StateFlow<String> get() = _dialogLPNDeleteTittle

    private val _indexDelete= MutableStateFlow(0)
    val indexDelete: StateFlow<Int> get() = _indexDelete

    private val _resultASNresponse = MutableStateFlow(ASNHeaderResponseEntity())
    val resultASNresponse: StateFlow<ASNHeaderResponseEntity> get() = _resultASNresponse

    private val _asnNumber= MutableStateFlow("")
    val asnNumber: StateFlow<String> get() = _asnNumber

    private val _dialogASNDeleteStatus= MutableStateFlow(false)
    val dialogASNDeleteStatus: StateFlow<Boolean> get() = _dialogASNDeleteStatus

    class ASNViewModelFactory(
        private val asnRepository: ASNRepository,
        private val context: Context
    ): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ASNViewModel(
                asnRepository,
                context
            ) as T
        }
    }

    init{
        //getInvoices()
        viewModelScope.launch {
            // Observar cambios en invoicesRepository.invoices
            asnRepository.resultPreASN.collectLatest { newResult ->
                _resultPreASN.value = newResult
                Log.e("REOS", "ASNViewModel-init-_resultPreASN.value:" +_resultPreASN.value)
            }
        }
        viewModelScope.launch {
            // Observar cambios en invoicesRepository.invoices
            asnRepository.resultASNresponse.collect { newResult ->
                //val currentList = _result.value
                //val updatedList = currentList.copy(data = newResult.data)
                //_result.value = updatedList
                _resultASNresponse.value = newResult
                Log.e("REOS", "ASNViewModel-init-_resultASNresponse.value:" +_resultASNresponse.value)
            }
        }
    }

    fun resetPreASN() {
        _resultPreASN.value = PreASNEntity()
    }

    fun resetASN() {
        _resultASN.value = ASNEntity()
    }


    fun getDataPreASN(code:String,batch:String){
        viewModelScope.launch {
            //_resultPreASN.value= PreASNEntity(status = "Loading", message = "Cargando...")
            asnRepository.getDataPreASN(code, batch)
        }
    }

    fun updateStatusShowDialog(status: Boolean) {
        _showLoadingDialog.value = status
    }

    fun updateStatusShowDialogQuantityReport(status: Boolean) {
        _showDialogQuantityReport.value = status
    }

    fun updateIndex(index: Int) {
        _index.value = index
    }

    fun chargeDataASNHead(preASNEntity: PreASN) {
        val data = convertPreASNEntityToASNEntity(preASNEntity)
        _resultASN.value = ASNEntity(status = "Y", message = "", data = data)
    }

    fun addDetailLpnCode(lpnCode: String) {
        try {
            Log.e("REOS", "ASNCreate-addDetailToASN-_resultASN.value:" +_resultASN.value)
            _resultASN.value = ASNEntity( data = ConvertaddDetailLpnCode(_resultASN.value, lpnCode), status = "Y", message = "")
            Log.e("REOS", "ASNCreate-addDetailToASN-_resultASN.value:" +_resultASN.value)
        }catch (e:Exception){

        }
    }

    fun updateDialogEditQuantyTittle(tittle: String) {
        _dialogEditQuantyTittle.value = tittle
    }

    fun updateResultQuantityDetail(quantityReport: String) {
        _resultASN.value= ASNEntity(status = "Y", message = "", data = ConvertUpdateResultQuantityDetail(_resultASN.value, index.value, quantityReport))
    }

    fun updatePrint(ipAddress: String) {
        _resultASN.value= ASNEntity(status = "Y", message = "", data = _resultASN.value.data, ipAddress = ipAddress)
    }

    fun sendDataASNPrint() {
        Log.e("REOS", "ASNViewModel-sendDataASNAPI-_resultASN.value:" +_resultASN.value)
        asnRepository.sendDataASNPrint(_resultASN.value)
    }

    fun updatePrintBottomBar(tittle: String) {
        _printBottomBar.value = tittle
    }

    fun updateDialogPrintStatus(status: Boolean) {
        _dialogPrintStatus.value = status
    }

    fun updateDialogDeleteStatus(status: Boolean) {
        _dialogLPNDeleteStatus.value = status
    }

    fun updateDialogDeleteTittle(status: String) {
        _dialogLPNDeleteTittle.value = status
    }

    fun updateIndexDelete(indexDelete: Int) {
        _indexDelete.value = indexDelete
    }

    fun deleteLPN() {
        _resultASN.value = ASNEntity(status = "Y", message = "", data = ConvertDeleteLPN(_resultASN.value, indexDelete.value))
        Log.e("REOS", "ASNViewModel-deleteLPN-indexDelete.value:" + indexDelete.value)
        Log.e("REOS", "ASNViewModel-deleteLPN-_resultASN.value:" + _resultASN.value)
    }

    fun validateStatusASN(): Boolean {
        return ConvertValidateStatusASN(resultASN.value)
    }

    fun updateasnNumber(asnNumber: String) {
        _asnNumber.value = asnNumber
    }

    fun validateStatusPrintAssigned(): Boolean {
        return ConvertValidateStatusPrintAssigned(_resultASN.value)
    }

    fun validateStatusHeadASN(): Boolean {
        return ConvertValidateStatusHeadASN(_resultASN.value)
    }

    fun updatedialogASNDeleteStatus(status: Boolean) {
        _dialogASNDeleteStatus.value = status
    }

}