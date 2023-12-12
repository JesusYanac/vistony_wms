package com.vistony.wms.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.vistony.wms.R
import com.vistony.wms.model.Login
import com.vistony.wms.model.TaskManagement
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import io.realm.kotlin.syncSession

class RealmNotificationService : Service() {
    private lateinit var realm: Realm

    override fun onCreate(){
        super.onCreate()
        realm = Realm.getDefaultInstance()

        Log.e("JEPICAMR","====>testeooo")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("JEPICAMR","====>TESTEOO 2")
        val listener = RealmChangeListener<RealmResults<TaskManagement>> { results ->
            val count = results.count()
            // Aquí puedes lanzar la notificación con el número de registros
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel = NotificationChannel("my_channel_id", "My Channel", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
            val notificationBuilder = NotificationCompat.Builder(this, "my_channel_id")
                .setContentTitle("Nuevo registro en Realm")
                .setContentText("Se ha sincronizado un nuevo registro en la base de datos. Actualmente hay $count registros.")
                .setSmallIcon(R.drawable.ic_baseline_cloud_download_24)
                .setAutoCancel(true)
            notificationManager.notify(1, notificationBuilder.build())
        }

        val results = realm.where(TaskManagement::class.java).findAll()
        results.addChangeListener(listener)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
