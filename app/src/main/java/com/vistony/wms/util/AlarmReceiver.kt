package com.vistony.wms.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.TimePicker
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.vistony.wms.R
import java.util.*

@Composable
fun AlarmScreen(context:Context) {
    var selectedDate by remember { mutableStateOf(Date()) }
    var selectedTime by remember { mutableStateOf(TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        selectedDate.hours = hourOfDay
        selectedDate.minutes = minute
    }) }
    var alarmOn by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = selectedDate.toString(),
            fontSize = 32.sp,
            color = Color.Black
        )
        Button(onClick = {
            val calendar = Calendar.getInstance()
            calendar.time = selectedDate

            val alarmIntent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0)

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (alarmOn) {
                alarmManager.cancel(pendingIntent)
                alarmOn = false
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                alarmOn = true
            }
        }) {
            Text(text = if (alarmOn) "Desactivar alarma" else "Activar alarma")
        }
        Button(onClick = {
            //TimePickerDialog(context, selectedTime, selectedDate.hours, selectedDate.minutes, true).show()
        }) {
            Text(text = "Configurar hora")
        }
    }
}



class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notification = NotificationCompat.Builder(context!!, "alarm")
            .setContentTitle("Alarma")
            .setContentText("Es hora de levantarse")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(0, notification)

        // Reproducir sonido de alarma
    }
}