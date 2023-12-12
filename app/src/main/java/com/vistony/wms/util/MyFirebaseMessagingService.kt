package com.vistony.wms.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vistony.wms.MainActivity
import com.vistony.wms.R
import com.vistony.wms.model.Users
import com.vistony.wms.viewmodel.LoginViewModel
import io.realm.Realm
import io.realm.kotlin.syncSession

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        // Actualiza el token en el servidor
        Log.e("JEPICAMEEE","=>"+token)
      //  sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        // Envía el token al servidor para almacenarlo y usarlo para enviar notificaciones

        val realm = Realm.getDefaultInstance()
        val user = realm.where(Users::class.java)
            .equalTo("id", realm.syncSession.user.id)
            .findFirst()

        user?.updateFirebaseToken(token!!)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Maneja las notificaciones entrantes aquí
        val title = remoteMessage.notification?.title
        val body = remoteMessage.notification?.body

        // Crear un intent para abrir la actividad al hacer clic en la notificación
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        // Crear la notificación
        val channelId = getString(R.string.default_notification_channel_id)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_baseline_box_24)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // Obtener el administrador de notificaciones y mostrar la notificación
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            channelId,
            "Channel human readable title",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
        notificationManager.notify(0, notificationBuilder.build())

    }
}
