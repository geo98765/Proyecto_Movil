package com.example.controltarjetas.notifications

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class NotificationScheduler(private val context: Context) {

    fun scheduleNotifications() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val notificationWork = PeriodicWorkRequestBuilder<NotificationWorker>(
            24, TimeUnit.HOURS  // Revisar cada 24 horas
        )
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.HOURS)  // Primera revisión en 1 hora
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "tarjetas_notifications",
            ExistingPeriodicWorkPolicy.KEEP,
            notificationWork
        )
    }

    fun cancelNotifications() {
        WorkManager.getInstance(context).cancelUniqueWork("tarjetas_notifications")
    }
}