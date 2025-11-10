package com.example.controltarjetas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.controltarjetas.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AhorroViewModel(application: Application) : AndroidViewModel(application) {

    private val ahorroRepository: AhorroRepository

    val todosAhorros: Flow<List<Ahorro>>
    val totalesPorTipo: Flow<List<TotalPorTipo>>

    init {
        val database = AppDatabase.getDatabase(application)
        ahorroRepository = AhorroRepository(database.ahorroDao())

        todosAhorros = ahorroRepository.todosAhorros
        totalesPorTipo = ahorroRepository.totalesPorTipo
    }

    fun insertarAhorro(ahorro: Ahorro) = viewModelScope.launch {
        ahorroRepository.insertar(ahorro)
    }

    fun actualizarAhorro(ahorro: Ahorro) = viewModelScope.launch {
        ahorroRepository.actualizar(ahorro)
    }

    fun eliminarAhorro(ahorro: Ahorro) = viewModelScope.launch {
        ahorroRepository.eliminar(ahorro)
    }

    suspend fun obtenerAhorroPorId(id: Int): Ahorro? {
        return ahorroRepository.obtenerPorId(id)
    }

    fun obtenerAhorrosPorInstitucion(institucionId: Int): Flow<List<Ahorro>> {
        return ahorroRepository.obtenerAhorrosPorInstitucion(institucionId)
    }

    fun calcularTotalAhorros(ahorros: List<Ahorro>): Double {
        return ahorros.sumOf { it.calcularValorTotal(tipoInversion = ahorros.toString()) }
    }
}