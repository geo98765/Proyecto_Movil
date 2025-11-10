package com.example.controltarjetas

import InstitucionFinancieraRepository
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.controltarjetas.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class AhorroViewModel(application: Application) : AndroidViewModel(application) {

    private val ahorroRepository: AhorroRepository
    private val institucionRepository: InstitucionFinancieraRepository

    // Estado de filtros
    private val _estadoFiltros = MutableStateFlow(EstadoFiltrosAhorro())
    val estadoFiltros: StateFlow<EstadoFiltrosAhorro> = _estadoFiltros.asStateFlow()

    // Datos observables
    val todosAhorros: Flow<List<Ahorro>>
    val todasInstituciones: Flow<List<InstitucionFinanciera>>
    val totalesPorTipo: Flow<List<TotalPorTipo>>

    init {
        val database = AppDatabase.getDatabase(application)
        ahorroRepository = AhorroRepository(database.ahorroDao())
        institucionRepository = InstitucionFinancieraRepository(database.institucionFinancieraDao())

        todosAhorros = ahorroRepository.todosAhorros
        todasInstituciones = institucionRepository.todasInstituciones
        totalesPorTipo = ahorroRepository.totalesPorTipo
    }

    // ==================== FILTROS ====================

    fun actualizarFiltros(nuevoEstado: EstadoFiltrosAhorro) {
        _estadoFiltros.value = nuevoEstado
    }

    fun filtrarAhorros(
        ahorros: List<Ahorro>,
        instituciones: List<InstitucionFinanciera>
    ): List<Ahorro> {
        var resultado = ahorros

        // Crear mapa de instituciones para acceso rápido
        val mapaInstituciones = instituciones.associateBy { it.id }

        // Filtrar por tipo de inversión
        if (_estadoFiltros.value.tipoInversion != FiltroTipoInversion.TODOS) {
            val tipoFiltro = when (_estadoFiltros.value.tipoInversion) {
                FiltroTipoInversion.TARJETA -> "Tarjeta"
                FiltroTipoInversion.ACCIONES -> "Acciones"
                FiltroTipoInversion.CRIPTO -> "Cripto"
                FiltroTipoInversion.CETES -> "CETES"
                else -> null
            }
            resultado = resultado.filter {
                mapaInstituciones[it.institucionId]?.tipoInversion == tipoFiltro
            }
        }

        // Filtrar por institución
        _estadoFiltros.value.institucionId?.let { institucionId ->
            resultado = resultado.filter { it.institucionId == institucionId }
        }

        // Filtrar por búsqueda
        if (_estadoFiltros.value.busqueda.isNotBlank()) {
            val busqueda = _estadoFiltros.value.busqueda.lowercase()
            resultado = resultado.filter {
                it.nombre.lowercase().contains(busqueda) ||
                        it.descripcion?.lowercase()?.contains(busqueda) == true ||
                        mapaInstituciones[it.institucionId]?.nombreInstitucion?.lowercase()?.contains(busqueda) == true
            }
        }

        // Ordenar
        resultado = when (_estadoFiltros.value.orden) {
            OrdenAhorro.MONTO_DESC -> resultado.sortedByDescending {
                val institucion = mapaInstituciones[it.institucionId]
                it.calcularValorTotal(institucion?.tipoInversion ?: "")
            }
            OrdenAhorro.MONTO_ASC -> resultado.sortedBy {
                val institucion = mapaInstituciones[it.institucionId]
                it.calcularValorTotal(institucion?.tipoInversion ?: "")
            }
            OrdenAhorro.FECHA_DESC -> resultado.sortedByDescending { it.fechaCreacion }
            OrdenAhorro.FECHA_ASC -> resultado.sortedBy { it.fechaCreacion }
            OrdenAhorro.NOMBRE_ASC -> resultado.sortedBy { it.nombre }
            OrdenAhorro.NOMBRE_DESC -> resultado.sortedByDescending { it.nombre }
        }

        return resultado
    }

    // ==================== CRUD ====================

    fun insertar(ahorro: Ahorro) = viewModelScope.launch {
        ahorroRepository.insertar(ahorro)
    }

    fun actualizar(ahorro: Ahorro) = viewModelScope.launch {
        ahorroRepository.actualizar(ahorro)
    }

    fun eliminar(ahorro: Ahorro) = viewModelScope.launch {
        ahorroRepository.eliminar(ahorro)
    }

    suspend fun obtenerPorId(id: Int): Ahorro? {
        return ahorroRepository.obtenerPorId(id)
    }

    // ==================== RENDIMIENTOS ====================

    /**
     * Calcula el rendimiento proyectado para un ahorro
     */
    fun calcularRendimiento(
        monto: Double,
        tasaAnual: Double,
        dias: Int
    ): Double {
        // Fórmula: Rendimiento = Monto * (Tasa / 365) * Días
        return monto * (tasaAnual / 100 / 365) * dias
    }

    /**
     * Calcula rendimientos por período
     */
    fun calcularRendimientosPorPeriodo(
        monto: Double,
        tasaAnual: Double
    ): RendimientosPeriodo {
        return RendimientosPeriodo(
            diario = calcularRendimiento(monto, tasaAnual, 1),
            semanal = calcularRendimiento(monto, tasaAnual, 7),
            mensual = calcularRendimiento(monto, tasaAnual, 30),
            anual = monto * (tasaAnual / 100)
        )
    }

    /**
     * Obtener ahorros que generan rendimientos (Tarjeta y CETES)
     */
    fun obtenerAhorrosConRendimiento(
        ahorros: List<Ahorro>,
        instituciones: List<InstitucionFinanciera>
    ): List<AhorroConRendimiento> {
        val mapaInstituciones = instituciones.associateBy { it.id }

        return ahorros.mapNotNull { ahorro ->
            val institucion = mapaInstituciones[ahorro.institucionId] ?: return@mapNotNull null
            val rendimientoAnual = institucion.rendimientoAnual ?: return@mapNotNull null

            // Solo Tarjeta y CETES tienen rendimientos configurables
            if (institucion.tipoInversion != "Tarjeta" && institucion.tipoInversion != "CETES") {
                return@mapNotNull null
            }

            val monto = when (institucion.tipoInversion) {
                "Tarjeta" -> ahorro.montoTarjeta ?: 0.0
                "CETES" -> ahorro.montoCetes ?: 0.0
                else -> 0.0
            }

            if (monto <= 0) return@mapNotNull null

            AhorroConRendimiento(
                ahorro = ahorro,
                institucion = institucion,
                monto = monto,
                rendimientos = calcularRendimientosPorPeriodo(monto, rendimientoAnual)
            )
        }
    }
}

// ==================== DATA CLASSES ====================

data class RendimientosPeriodo(
    val diario: Double,
    val semanal: Double,
    val mensual: Double,
    val anual: Double
)

data class AhorroConRendimiento(
    val ahorro: Ahorro,
    val institucion: InstitucionFinanciera,
    val monto: Double,
    val rendimientos: RendimientosPeriodo
)

enum class PeriodoRendimiento {
    DIARIO,
    SEMANAL,
    MENSUAL,
    ANUAL
}