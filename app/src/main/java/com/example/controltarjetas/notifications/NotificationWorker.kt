package com.example.controltarjetas.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.controltarjetas.data.AppDatabase
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            val tarjetaDao = database.tarjetaDao()
            val bancoDao = database.bancoDao()

            // Obtener tarjetas pendientes
            val tarjetas = tarjetaDao.obtenerTarjetasPendientes().first()

            val hoy = LocalDate.now()
            val notificationHelper = NotificationHelper(applicationContext)

            tarjetas.forEach { tarjeta ->
                val fechaLimite = LocalDate.parse(tarjeta.fechaLimitePago)
                val diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(hoy, fechaLimite)

                // Notificar 3 días antes
                if (diasRestantes == 3L) {
                    val banco = bancoDao.obtenerBancoPorId(tarjeta.bancoId)
                    notificationHelper.showNotification(
                        title = "⚠️ Recordatorio de Pago",
                        message = "Quedan 3 días para pagar ${banco?.nombreBanco}. Deuda: $${String.format("%.2f", tarjeta.deudaTotal)}",
                        notificationId = tarjeta.id
                    )
                }

                // Notificar el día del vencimiento
                if (diasRestantes == 0L) {
                    val banco = bancoDao.obtenerBancoPorId(tarjeta.bancoId)
                    notificationHelper.showNotification(
                        title = "🚨 ¡Pago HOY!",
                        message = "Hoy vence el pago de ${banco?.nombreBanco}. Deuda: $${String.format("%.2f", tarjeta.deudaTotal)}",
                        notificationId = tarjeta.id + 10000
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}