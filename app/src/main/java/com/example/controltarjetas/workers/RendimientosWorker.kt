package com.example.controltarjetas.workers

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.*
import com.example.controltarjetas.data.AppDatabase
import com.example.controltarjetas.data.RendimientoDiario
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

/**
 * Worker para actualizar los rendimientos diarios de las inversiones
 * Se ejecuta automáticamente cada día a las 00:01
 */
class RendimientosWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            val ahorroDao = database.ahorroDao()
            val institucionDao = database.institucionFinancieraDao()
            val rendimientoDao = database.rendimientoDiarioDao()

            // Obtener fecha de hoy
            val hoy = LocalDate.now()

            // Obtener todos los ahorros con rendimiento
            val ahorros = ahorroDao.obtenerTodosAhorrosDirecto()
            val instituciones = institucionDao.obtenerTodasInstitucionesDirecto()

            ahorros.forEach { ahorro ->
                val institucion = instituciones.find { it.id == ahorro.institucionId }

                // Solo procesar si la institución tiene rendimiento
                if (institucion?.rendimientoAnual != null && institucion.rendimientoAnual > 0) {
                    val valorInicial = when (institucion.tipoInversion) {
                        "Tarjeta" -> ahorro.montoTarjeta ?: 0.0
                        "Acciones" -> (ahorro.cantidadAcciones ?: 0.0) * (ahorro.precioCompraAccion ?: 0.0)
                        "Cripto" -> (ahorro.cantidadCripto ?: 0.0) * (ahorro.precioCompraCripto ?: 0.0)
                        "CETES" -> ahorro.montoCetes ?: 0.0
                        else -> 0.0
                    }

                    // Calcular rendimiento diario
                    val rendimientoDiario = valorInicial * (institucion.rendimientoAnual / 100) / 365

                    // Guardar en la base de datos
                    val rendimientoDiarioEntity = RendimientoDiario(
                        ahorroId = ahorro.id,
                        fecha = hoy.toString(),
                        rendimiento = rendimientoDiario,
                        valorBase = valorInicial,
                        tasaAplicada = institucion.rendimientoAnual
                    )

                    rendimientoDao.insertar(rendimientoDiarioEntity)
                }
            }

            // Limpiar registros antiguos (más de 90 días)
            val fechaLimite = hoy.minusDays(90).toString()
            rendimientoDao.eliminarAnterioresA(fechaLimite)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    companion object {
        private const val WORK_NAME = "RendimientosWorker"

        /**
         * Programa el worker para ejecutarse diariamente
         */
        @RequiresApi(Build.VERSION_CODES.O)
        fun programarActualizacionDiaria(context: Context) {
            // Calcular delay inicial para que se ejecute a las 00:01
            val ahora = LocalDate.now().atTime(0, 0)
            val proximaEjecucion = if (LocalDate.now().atStartOfDay() == ahora) {
                ahora.plusDays(1)
            } else {
                ahora
            }

            val delayInicial = ChronoUnit.MINUTES.between(
                LocalDate.now().atStartOfDay(),
                proximaEjecucion
            )

            // Crear constraints para la ejecución
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
                .build()

            // Crear la petición de trabajo periódico
            val workRequest = PeriodicWorkRequestBuilder<RendimientosWorker>(
                repeatInterval = 1,
                repeatIntervalTimeUnit = TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .setInitialDelay(delayInicial, TimeUnit.MINUTES)
                .addTag(WORK_NAME)
                .build()

            // Programar el trabajo
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        /**
         * Cancela la actualización automática
         */
        fun cancelarActualizacion(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        /**
         * Ejecuta manualmente la actualización (para testing)
         */
        fun ejecutarManualmente(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<RendimientosWorker>()
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}

