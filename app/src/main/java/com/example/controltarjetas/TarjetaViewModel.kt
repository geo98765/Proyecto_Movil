package com.example.controltarjetas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.controltarjetas.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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