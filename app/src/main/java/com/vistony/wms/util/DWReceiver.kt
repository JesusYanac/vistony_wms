package com.vistony.wms.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.vistony.wms.ObservableObject
import com.vistony.wms.model.Scan
import com.vistony.wms.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.*

class DWReceiver() : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        Log.e("JEPICAME","ñañañañ")
        ObservableObject.instance.updateValue(intent)

    }
}
