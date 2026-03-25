package com.carlosalarcongu.nextdrive.ui

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU]) // Simulamos Android 13
class ReminderTest {

    @Test
    fun `scheduleNotification programa la alarma correctamente en AlarmManager`() {
        // 1. Preparamos el contexto y las "sombras" (Shadows) de Robolectric
        val context = ApplicationProvider.getApplicationContext<Context>()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager = shadowOf(alarmManager)

        // 2. Ejecutamos nuestra función con una fecha futura (ej. dentro de 10 días)
        val futureTimeMillis = System.currentTimeMillis() + (10 * 24 * 60 * 60 * 1000)
        scheduleNotification(context, futureTimeMillis, "Test ITV", "Toca pasar la ITV")

        // 3. Comprobamos que el sistema ha registrado la alarma
        val nextAlarm = shadowAlarmManager.nextScheduledAlarm
        assertNotNull("La alarma no se ha programado", nextAlarm)

        // ¡CORRECCIÓN AQUÍ! Añadimos !! a nextAlarm
        assertEquals("El tiempo programado no coincide", futureTimeMillis, nextAlarm!!.triggerAtTime)
    }

    @Test
    fun `ReminderReceiver genera una notificacion en el sistema al recibir el broadcast`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowNotificationManager = shadowOf(notificationManager)

        // 1. Simulamos el Intent que envía el AlarmManager cuando llega la fecha
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("TITLE", "Aviso de Mantenimiento")
            putExtra("MESSAGE", "Cambio de aceite necesario")
        }

        // 2. Ejecutamos el Receiver manualmente
        val receiver = ReminderReceiver()
        receiver.onReceive(context, intent)

        // 3. Comprobamos que se ha creado una notificación en el sistema
        val notifications = shadowNotificationManager.allNotifications
        assertEquals("Debería haber exactamente 1 notificación", 1, notifications.size)

        val notification = notifications[0]
        val title = notification.extras.getString("android.title")
        val text = notification.extras.getString("android.text")

        assertEquals("Aviso de Mantenimiento", title)
        assertEquals("Cambio de aceite necesario", text)
    }
}