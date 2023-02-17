package com.vistony.wms.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.vistony.wms.ObservableObject

class DWReceiver() : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        ObservableObject.instance.updateValue(intent)

    }
}
