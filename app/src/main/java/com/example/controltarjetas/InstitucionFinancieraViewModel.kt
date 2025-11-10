package com.example.controltarjetas

import InstitucionFinancieraRepository
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.controltarjetas.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class InstitucionFinancieraViewModel(application: Application) : AndroidViewModel(application) {

    private val institucionRepository: InstitucionFinancieraRepository

    val todasInstituciones: Flow<List<InstitucionFinanciera>>

    init {
        val database = AppDatabase.getDatabase(application)
        institucionRepository = InstitucionFinancieraRepository(database.institucionFinancieraDao())
        todasInstituciones = institucionRepository.todasInstituciones
    }

    // ==================== CRUD ====================

    fun insertar(institucion: InstitucionFinanciera) = viewModelScope.launch {
        institucionRepository.insertar(institucion)
    }

    fun actualizar(institucion: InstitucionFinanciera) = viewModelScope.launch {
        institucionRepository.actualizar(institucion)
    }

    fun eliminar(institucion: InstitucionFinanciera) = viewModelScope.launch {
        institucionRepository.eliminar(institucion)
    }

    suspend fun obtenerInstitucionPorId(id: Int): InstitucionFinanciera? {
        return institucionRepository.obtenerPorId(id)
    }

    fun obtenerPorTipo(tipo: String): Flow<List<InstitucionFinanciera>> {
        return institucionRepository.obtenerPorTipo(tipo)
    }
}