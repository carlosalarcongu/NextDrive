package com.carlosalarcongu.nextdrive.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.app.NotificationCompat
import com.carlosalarcongu.nextdrive.MainActivity

// 1. PREFERENCIAS GLOBALES (Modificado para el mapa)
data class UserPrefs(
    val unitDist: String = "Kilómetros",
    val unitCurr: String = "Euros (€)",
    val unitVol: String = "Litros",
    val useVibration: Boolean = true,
    val dashboardOrder: String = "IMAGE,INFO_PARKING,DOCS,INTERVALS,EXPENSES",
    val showFuelControls: Boolean = true // NUEVO: Mostrar controles del mapa
)

val LocalUserPrefs = staticCompositionLocalOf<UserPrefs> { error("Preferencias no proveídas") }

// 2. FUNCIÓN DE VIBRACIÓN RESPONSIVA
fun triggerVibration(context: Context, prefs: UserPrefs) {
    if (prefs.useVibration) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }
}

// 3. RECEPTOR DE LA ALARMA DEL PARQUÍMETRO
class ParkingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "nextdrive_parking"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Parquímetro", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val tapIntent = Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
        val pendingIntent = PendingIntent.getActivity(context, 0, tapIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("¡Aparcamiento a punto de caducar!")
            .setContentText("Quedan menos de 15 minutos de tu ticket ORA.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}