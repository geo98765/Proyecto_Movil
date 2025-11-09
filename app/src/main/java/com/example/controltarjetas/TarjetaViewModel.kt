package com.example.controltarjetas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.controltarjetas.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.UUID

class TarjetaViewModel(application: Application) : AndroidViewModel(application) {

    private val tarjetaRepository: TarjetaRepository
    private val bancoRepository: BancoRepository
    private val historialRepository: HistorialRepository

    // Datos observables - Tarjetas
    val todasLasTarjetas: Flow<List<Tarjeta>>
    val tarjetasPendientes: Flow<List<Tarjeta>>
    val deudaTotal: Flow<Double?>

    val deudasPorMes: Flow<List<DeudaMensual>>
    val deudasPorBanco: Flow<List<DeudaPorBanco>>
    val pagosPorFecha: Flow<List<PagoMensual>>

    // Datos observables - Bancos
    val todosBancos: Flow<List<Banco>>

    // Datos observables - Historial
    val todoHistorial: Flow<List<HistorialPago>>

    init {
        val database = AppDatabase.getDatabase(application)
        tarjetaRepository = TarjetaRepository(database.tarjetaDao())
        bancoRepository = BancoRepository(database.bancoDao())
        historialRepository = HistorialRepository(database.historialPagoDao())

        todasLasTarjetas = tarjetaRepository.todasLasTarjetas
        tarjetasPendientes = tarjetaRepository.tarjetasPendientes
        deudaTotal = tarjetaRepository.deudaTotal
        todosBancos = bancoRepository.todosBancos
        todoHistorial = historialRepository.todoHistorial

        deudasPorMes = database.tarjetaDao().obtenerDeudasPorMes()
        deudasPorBanco = database.tarjetaDao().obtenerDeudasPorBanco()
        pagosPorFecha = database.historialPagoDao().obtenerPagosPorFecha()
    }

    // ==================== TARJETAS ====================

    fun insertarTarjeta(tarjeta: Tarjeta) = viewModelScope.launch {
        tarjetaRepository.insertar(tarjeta)
    }

    fun actualizarTarjeta(tarjeta: Tarjeta) = viewModelScope.launch {
        tarjetaRepository.actualizar(tarjeta)
    }

    fun eliminarTarjeta(tarjeta: Tarjeta) = viewModelScope.launch {
        tarjetaRepository.eliminar(tarjeta)
    }

    suspend fun obtenerTarjetaPorId(id: Int): Tarjeta? {
        return tarjetaRepository.obtenerPorId(id)
    }

    // Marcar como pagada y mover al historial
    fun marcarComoPagadaYGuardarHistorial(
        tarjeta: Tarjeta,
        banco: Banco,
        onCompletado: () -> Unit
    ) = viewModelScope.launch {
        // Crear registro en historial
        val historialPago = HistorialPago(
            bancoId = banco.id,
            nombreBanco = banco.nombreBanco,
            deudaTotal = tarjeta.deudaTotal,
            pagoMinimo = tarjeta.pagoMinimo,
            fechaLimitePago = tarjeta.fechaLimitePago,
            fechaPago = LocalDate.now().toString(),
            notas = tarjeta.notas
        )
        historialRepository.insertar(historialPago)

        // Eliminar tarjeta actual
        tarjetaRepository.eliminarPorId(tarjeta.id)

        onCompletado()
    }

    suspend fun obtenerTarjetaPorBancoYMes(bancoId: Int, mesPeriodo: String): Tarjeta? {
        return tarjetaRepository.obtenerTarjetaPorBancoYMes(bancoId, mesPeriodo)
    }

    // ==================== MESES SIN INTERESES (MSI) ====================

    /**
     * Crea automáticamente las tarjetas MSI para cada mes
     */
    fun crearTarjetasMSI(
        bancoId: Int,
        descripcion: String,
        montoTotal: Double,
        meses: Int,
        mesInicio: YearMonth,
        onCompletado: () -> Unit
    ) = viewModelScope.launch {
        try {
            // Obtener el banco para calcular fechas
            val banco = bancoRepository.obtenerPorId(bancoId)

            if (banco == null || banco.diaPago == null) {
                // No se puede crear sin día de pago
                return@launch
            }

            // Generar ID único para agrupar todas las tarjetas MSI
            val msiGrupoId = "msi_${UUID.randomUUID()}"

            // Calcular monto mensual
            val montoPorMes = montoTotal / meses

            // Crear una tarjeta para cada mes
            for (mesActual in 1..meses) {
                val mesPeriodo = mesInicio.plusMonths((mesActual - 1).toLong())

                // Calcular fecha límite de pago
                val ultimoDiaDelMes = mesPeriodo.lengthOfMonth()
                val diaAjustado = banco.diaPago.coerceIn(1, ultimoDiaDelMes)
                val fechaLimite = mesPeriodo.atDay(diaAjustado)

                val tarjeta = Tarjeta(
                    bancoId = bancoId,
                    tipoTarjeta = "Crédito",
                    deudaTotal = montoPorMes,
                    pagoMinimo = null, // MSI no tiene pago mínimo, se paga completo
                    fechaLimitePago = fechaLimite.toString(),
                    mesPeriodo = mesPeriodo.toString(),
                    notas = null,
                    esMSI = true,
                    msiGrupoId = msiGrupoId,
                    msiDescripcion = descripcion,
                    msiMesActual = mesActual,
                    msiMesesTotal = meses,
                    msiMontoTotal = montoTotal,
                    msiMontoPorMes = montoPorMes
                )

                tarjetaRepository.insertar(tarjeta)
            }

            onCompletado()
        } catch (e: Exception) {
            // Manejar error
            e.printStackTrace()
        }
    }

    /**
     * Eliminar todas las tarjetas de un grupo MSI
     */
    fun eliminarGrupoMSI(msiGrupoId: String) = viewModelScope.launch {
        // Esta función requeriría un método en el DAO
        // Por ahora, se eliminarían individualmente
    }

    // ==================== BANCOS ====================

    fun insertarBanco(banco: Banco) = viewModelScope.launch {
        bancoRepository.insertar(banco)
    }

    fun actualizarBanco(banco: Banco) = viewModelScope.launch {
        bancoRepository.actualizar(banco)
    }

    fun eliminarBanco(banco: Banco) = viewModelScope.launch {
        bancoRepository.eliminar(banco)
    }

    suspend fun obtenerBancoPorId(id: Int): Banco? {
        return bancoRepository.obtenerPorId(id)
    }

    // ==================== HISTORIAL ====================

    fun obtenerHistorialPorBanco(bancoId: Int): Flow<List<HistorialPago>> {
        return historialRepository.obtenerHistorialPorBanco(bancoId)
    }

    fun eliminarHistorial(id: Int) = viewModelScope.launch {
        historialRepository.eliminar(id)
    }
}